package net.semanticmetadata.lire.imageanalysis;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.nio.ByteBuffer;
import java.util.HashSet;

import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Descriptor of a single local feature.
 * 
 * @author Mario Taschwer mt@itec.aau.at
 * @version $Id$
 */
public abstract class LocalFeature implements LireFeature, Serializable
{

    private static final long serialVersionUID = -3359651678675364902L;

    /**
     * Convert array of unsigned byte values to double.
     * In fact, a subarray of <code>in</code> determined by <code>offset</code> and <code>length</code>
     * is converted and stored into the array <code>out</code> if its length is equal to <code>length</code>.
     * Otherwise, a new double array of length <code>length</code> is allocated.
     * @return the array passed as <code>out</code>, or a new array if the length of <code>out</code>
     *         is not appropriate or if <code>out</code> is <code>null</code>.
     */
    protected static double[] ubyte2doubleArray(double[] out, byte[] in, int offset, int length)
    {
        if (out == null || out.length != length)
            out = new double[length];
        for (int i=0, j=offset; i < length; i++, j++) {
            out[i] = (in[j] + 256) % 256;
        }
        return out;
    }

    /**
     * Convert array of bits to double.
     * In fact, a subarray of <code>in</code> determined by <code>offset</code> and <code>length</code>
     * (in bytes) is converted and stored into the array <code>out</code> if its length is equal to <code>length</code>.
     * Otherwise, a new double array of length <code>length</code> is allocated.
     * @return the array passed as <code>out</code>, or a new array if the length of <code>out</code>
     *         is not appropriate or if <code>out</code> is <code>null</code>.
     */
    protected static double[] bit2doubleArray(double[] out, byte[] in, int offset, int length)
    {
        final int bitlen = length << 3;    // length in bits
        if (out == null || out.length != bitlen)
            out = new double[bitlen];
        int src = 0;
        for (int i=0, j=offset; i < bitlen; i++) {
            final int shift = i & 7;
            if (shift == 0) {
                src = (in[j] + 256) & 0xff;   // get unsigned byte value
                j++;
            }
            out[i] = (src >> shift) & 1;
        }
        return out;
    }
    
    public double[] descriptor;
    
    protected LocalFeature() {
        // NOP
    }
    
    protected LocalFeature(double[] descriptor) {
		this.descriptor = descriptor;
	}
	
    /**
     * Create a deep copy of this instance.
     */
    public abstract LocalFeature clone();
    
    /**
     * Make this instance a deep copy of the given LocalFeature instance.
     * Should be called by {@link #clone()} to facilitate implementations
     * in subclasses.
     * @return this instance.
     */
    protected LocalFeature copyOf(LocalFeature src)
    {
        final int len = src.descriptor.length;
        descriptor = new double[len];
        System.arraycopy(src.descriptor, 0, descriptor, 0, len);
        return this;
    }
    
    /**
     * This method is empty, because local features are extracted by the
     * corresponding DocumentBuilder and passed to the constructor of the
     * LocalFeature subclass.
     */
	@Override
	public void extract(BufferedImage img) {
	    // NOP
	}

	@Override
	public byte[] getByteArrayRepresentation() {
		return SerializationUtils.toByteArray(descriptor);
	}

	/**
	 * Returns the L2 distance between this and the other feature vector.
	 * If the other feature is not an instance of LocalFeature, -1 will be returned. 
	 */
	@Override
	public float getDistance(LireFeature feature) {
        if (!(feature instanceof LocalFeature)) return -1;
        return (float) MetricsUtils.distL2(descriptor, ((LocalFeature) feature).descriptor);
	}

	/** Returns the feature vector. */
	@Override
	public double[] getDoubleHistogram() {
		return descriptor;
	}

	/** Not implemented. */
	@Override
	public String getStringRepresentation() {
        throw new UnsupportedOperationException("Not implemented!");
	}

	@Override
	public void setByteArrayRepresentation(byte[] in) {
        descriptor = SerializationUtils.toDoubleArray(in);
	}

	@Override
	public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        descriptor = SerializationUtils.toDoubleArray(in, offset, length);
	}

    /** Not implemented. */
	@Override
	public void setStringRepresentation(String arg0) {
        throw new UnsupportedOperationException("Not implemented!");
	}

	/**
	 * Set this instance to the centroid of the given set of LocalFeatures
	 * for clustering purposes.
	 * The default implementation computes the component-wise mean of double values.
	 * It should be overridden by subclasses with different descriptor semantics
	 * (in particular, for binary descriptors).
	 */
	public void setCentroid(HashSet<LocalFeature> features)
	{
	    // we iterate over descriptor in inner loops to optimize cache performance
	    for (int i=0; i < descriptor.length; i++) {
	        descriptor[i] = 0;
	    }
	    for (LocalFeature f : features) {
	        for (int i=0; i < descriptor.length; i++) {
	            descriptor[i] += f.descriptor[i];
	        }
	    }
	    final int n = features.size();
        for (int i=0; i < descriptor.length; i++) {
            descriptor[i] /= n;
        }
	}
}
