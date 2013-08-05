package net.semanticmetadata.lire.imageanalysis;

import java.awt.image.BufferedImage;

import net.semanticmetadata.lire.imageanalysis.Histogram;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Descriptor of a single local feature.
 * 
 * @author Mario Taschwer mt@itec.aau.at
 * @version 2013-06-27
 */
public abstract class LocalFeature extends Histogram implements LireFeature 
{

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
