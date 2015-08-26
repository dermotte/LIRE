package net.semanticmetadata.lire.imageanalysis.features.global.centrist;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;

/**
 * <p>CENTRIST (CENsus TRansform hISTogram) descriptor based on the implementation described in Jianxin Wu; Rehg, J.M., "CENTRIST: A Visual Descriptor
 * for Scene Categorization," Pattern Analysis and Machine Intelligence, IEEE Transactions on , vol.33, no.8,
 * pp.1489,1501, Aug. 2011, doi: 10.1109/TPAMI.2010.224, http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=5674051&isnumber=5898466/<p>
 *<p>This class provides the spatial pyramid version, please see {@link SimpleCentrist} for the global histogram.</p>
 *
 * @author Mathias Lux, mathias@juggle.at
 * @see SimpleCentrist for the base descriptor of the paper.
 */
public class SpatialPyramidCentrist implements GlobalFeature {
    private boolean applyMaxNorm = true;
    private int histLength = 256;
    int histogramSize = histLength * 6 + histLength * 4 * 4 + histLength * 9;
    double[] histogram = new double[histogramSize];

    @Override
    public void extract(BufferedImage image) {
        // level 0:
        SimpleCentrist simpleCentrist = new SimpleCentrist();
        simpleCentrist.extract(image);
        System.arraycopy(simpleCentrist.getFeatureVector(), 0, histogram, 0, histLength);
        // level 1:
        int w = image.getWidth() / 2;
        int h = image.getHeight() / 2;
        simpleCentrist.extract(image.getSubimage(0, 0, w, h));
        System.arraycopy(simpleCentrist.getFeatureVector(), 0, histogram, histLength * 1, histLength);
        simpleCentrist.extract(image.getSubimage(w, 0, w, h));
        System.arraycopy(simpleCentrist.getFeatureVector(), 0, histogram, histLength * 2, histLength);
        simpleCentrist.extract(image.getSubimage(0, h, w, h));
        System.arraycopy(simpleCentrist.getFeatureVector(), 0, histogram, histLength * 3, histLength);
        simpleCentrist.extract(image.getSubimage(w, h, w, h));
        System.arraycopy(simpleCentrist.getFeatureVector(), 0, histogram, histLength * 4, histLength);
        // and that's the additional sub image in level one:
        simpleCentrist.extract(image.getSubimage(w/2, h/2, w, h));
        System.arraycopy(simpleCentrist.getFeatureVector(), 0, histogram, histLength * 5, histLength);
        // level 2:
        int wstep = image.getWidth() / 4;
        int hstep = image.getHeight() / 4;
        int binPos = 6; // the next free section in the histogram
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                simpleCentrist.extract(image.getSubimage(i * wstep, j * hstep, wstep, hstep));
                System.arraycopy(simpleCentrist.getFeatureVector(), 0, histogram, histLength * binPos, histLength);
                binPos++;
            }
        }
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                simpleCentrist.extract(image.getSubimage(wstep /2 + i * wstep, hstep / 2 + j * hstep, wstep, hstep));
                System.arraycopy(simpleCentrist.getFeatureVector(), 0, histogram, histLength * binPos, histLength);
                binPos++;
            }
        }
        if (applyMaxNorm) normalize(histogram);
    }

    /**
     * Applies max norm to the histogram.
     * @param in
     */
    private void normalize(double[] in) {
        double max = 0d;
        for (double d : in) {
            max = Math.max(max, d);
        }
        for (int i = 0; i < in.length; i++) {
            in[i] = in[i] / max;
        }
    }

    @Override
    public String getFeatureName() {
        return "Centrist";
    }

    @Override
    public String getFieldName() {
        return "Centrist";
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(histogram);
    }

    @Override
    public void setByteArrayRepresentation(byte[] featureData) {
        setByteArrayRepresentation(featureData, 0, featureData.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] featureData, int offset, int length) {
        histogram = SerializationUtils.toDoubleArray(featureData, offset, length);
    }

    @Override
    public double getDistance(LireFeature feature) {
        if (feature instanceof SpatialPyramidCentrist)
            return MetricsUtils.distL1(histogram, feature.getFeatureVector());
        else
            return -1d;
    }

    @Override
    public double[] getFeatureVector() {
        return histogram;
    }
}
