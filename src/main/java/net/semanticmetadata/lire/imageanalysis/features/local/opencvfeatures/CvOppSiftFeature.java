package net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Created by Nektarios on 1/10/2014.
 */
public class CvOppSiftFeature implements Serializable, LireFeature {
    public float angle;
    public int class_id;
    public int octave;
    public double[] point;
    public float response;
    public float size;
    double[] feature;

    public CvOppSiftFeature() {
    }

    public CvOppSiftFeature(float ang, int cl, int o, double[] pt, float res, float s, double[] feat) {
        angle = ang;
        class_id = cl;
        octave = o;
        point = pt;
        response = res;
        size = s;
        feature = feat;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < feature.length; i++) {
            sb.append(feature[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public float getDistance(LireFeature f) {
        if (!(f instanceof CvOppSiftFeature)) return -1;
        return (float) MetricsUtils.distL2(feature, ((CvOppSiftFeature) f).feature);
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
        return SerializationUtils.toByteArray(feature);
    }

    public void setByteArrayRepresentation(byte[] in) {
        feature = SerializationUtils.toDoubleArray(in);
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        feature = SerializationUtils.toDoubleArray(in, offset, length);
    }

    public double[] getDoubleHistogram() {
        return feature;
    }

    @Override
    public String getFeatureName() {
        return "cvOppSift";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_CVOPPSIFT;
    }
}