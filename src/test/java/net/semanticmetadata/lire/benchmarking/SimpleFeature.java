package net.semanticmetadata.lire.benchmarking;

import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;

public class SimpleFeature implements LireFeature {
    double[] hist;

    @Override
    public void extract(BufferedImage bimg) {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double[] getDoubleHistogram() {
        return hist;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getDistance(LireFeature feature) {
        double k1 = 1.2, b = 0.75;
        double[] hist1 = new double[hist.length], hist2 = new double[hist.length];
        System.arraycopy(hist, 0, hist1, 0, hist.length);
        System.arraycopy(((SimpleFeature) feature).hist, 0, hist2, 0, hist.length);
        // weighting ...
//        double d1=0d, d2=0d;
//        for (int i = 0; i < hist1.length; i++)
//            d1 += hist1[i];
//        for (int i = 0; i < hist2.length; i++)
//            d2 += hist2[i];
        for (int i = 0; i < hist1.length; i++) {
//            hist1[i] = (hist1[i]*(k1+1.0))/(hist1[i] + k1*(1-b+b*d1/TestNister.avgDocLength)) * Math.log((10200-TestNister.df[i]+0.5)/(TestNister.df[i]+0.5));
//            hist2[i] = (hist2[i]*(k1+1.0))/(hist2[i] + k1*(1-b+b*d1/TestNister.avgDocLength)) * Math.log((10200-TestNister.df[i]+0.5)/(TestNister.df[i]+0.5));
            if (hist1[i] > 0) hist1[i] = 1 + Math.log(hist1[i]);
            if (hist2[i] > 0) hist2[i] = 1 + Math.log(hist2[i]);
        }
//        double dist = (1f - MetricsUtils.cosineCoefficient(hist1, hist2));
//        if (dist<0) {
//            System.out.println(dist);
//            dist = 0;
//        }
        return (float) MetricsUtils.distL2(hist1, hist2);
    }

    @Override
    public String getStringRepresentation() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setStringRepresentation(String s) {
        hist = SerializationUtils.doubleArrayFromString(s);
    }
}
