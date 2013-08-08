package net.semanticmetadata.lire.imageanalysis;

import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;

import net.semanticmetadata.lire.imageanalysis.Histogram;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Descriptor of a single local feature.
 * 
 * @author Mario Taschwer mt@itec.aau.at
 * @version $Id$
 */
public abstract class LocalFeature extends Histogram implements LireFeature 
{

    /**
     * Create byte array representation from ByteBuffer.
     * The ByteBuffer is supposed to contain one feature descriptor
     * starting at position <code>offset</code>.
     * The result is compatible with {@link #getByteArrayRepresentation()}.
     * 
     * @param dst      destination array of length greater than or equal to <code>length</code>,
     *                 will be filled starting at position 0.
     * @param buffer   source buffer.
     * @param offset   offset in <code>buffer</code> pointing to the first descriptor element.
     * @param length   length of descriptor (in bytes).
     */
    public static void byteArrayFromBuffer(byte[] dst, ByteBuffer buffer, int offset, int length)
    {
        buffer.position(offset);
        for (int i=0; i < length; i++) {
            dst[i] = buffer.get();
        }
    }

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
    
    protected LocalFeature() {
        // NOP
    }
    
    protected LocalFeature(double[] descriptor) {
		this.descriptor = descriptor;
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

}
