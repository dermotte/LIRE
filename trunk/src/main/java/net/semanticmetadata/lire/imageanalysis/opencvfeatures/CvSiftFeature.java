package net.semanticmetadata.lire.imageanalysis.opencvfeatures;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Created by Nektarios on 1/10/2014.
 */
public class CvSiftFeature implements Serializable, LireFeature {
    public float angle;
    public int class_id;
    public int octave;
    public double[] point;
    public float response;
    public float size;
    double[] descriptor;

    public CvSiftFeature() {
    }

    public CvSiftFeature(float ang, int cl, int o, double[] pt, float res, float s, double[] desc) {
        angle = ang;
        class_id = cl;
        octave = o;
        point = pt;
        response = res;
        size = s;
        descriptor = desc;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < descriptor.length; i++) {
            sb.append(descriptor[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public float getDistance(LireFeature f) {
        if (!(f instanceof CvSiftFeature)) return -1;
        return (float) MetricsUtils.distL2(descriptor, ((CvSiftFeature) f).descriptor);
    }

    public String getStringRepresentation() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public void setStringRepresentation(String featureVector) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public void extract(BufferedImage image) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(descriptor);
    }

    public void setByteArrayRepresentation(byte[] in) {
        descriptor = SerializationUtils.toDoubleArray(in);
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        descriptor = SerializationUtils.toDoubleArray(in, offset, length);
    }

    public double[] getDoubleHistogram() {
        return descriptor;
    }

    @Override
    public String getFeatureName() {
        return "cvSIFT";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_CVSIFT;
    }
}