package net.semanticmetadata.lire.imageanalysis.features.global.centrist;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * <p>CENTRIST (CENsus TRansform hISTogram) descriptor based on the implementation described in Jianxin Wu; Rehg, J.M., "CENTRIST: A Visual Descriptor
 * for Scene Categorization," Pattern Analysis and Machine Intelligence, IEEE Transactions on , vol.33, no.8,
 * pp.1489,1501, Aug. 2011, doi: 10.1109/TPAMI.2010.224, http://ieeexplore.ieee.org/stamp/stamp.jsp?tp=&arnumber=5674051&isnumber=5898466/<p>
 * <p/>
 * <p>This class provides a basic global histogram based on the CENTRIST feature. For the original implementation, which uses a spatial pyramid, please see {@link SpatialPyramidCentrist}.</p>
 *
 * @author Mathias Lux, mathias@juggle.at and Lukas Knoch, lukas.knoch@aau.at
 * @see SpatialPyramidCentrist for the original descriptor of the paper.
 */
public class SimpleCentrist implements GlobalFeature {
    private boolean applyMaxNorm = false;
    double[] histogram;



    @Override
    public void extract(BufferedImage image) {
        histogram = new double[256];
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = 0;
        }
        WritableRaster raster = ImageUtils.getGrayscaleImage(image).getRaster();
        int[] px = new int[3];
        for (int x = 1; x < raster.getWidth() - 1; x++) {
            for (int y = 1; y < raster.getHeight() - 1; y++) {
                raster.getPixel(x, y, px);
                int ctValue = 0;
                int intensity = px[0];
                if (raster.getPixel(x - 1, y - 1, px)[0] <= intensity) ctValue |= (0x01);
                if (raster.getPixel(x, y - 1, px)[0] <= intensity) ctValue |= (0x01 << 1);
                if (raster.getPixel(x + 1, y - 1, px)[0] <= intensity) ctValue |= (0x01 << 2);
                if (raster.getPixel(x - 1, y + 1, px)[0] <= intensity) ctValue |= (0x01 << 3);
                if (raster.getPixel(x, y + 1, px)[0] <= intensity) ctValue |= (0x01 << 4);
                if (raster.getPixel(x + 1, y + 1, px)[0] <= intensity) ctValue |= (0x01 << 5);
                if (raster.getPixel(x - 1, y, px)[0] <= intensity) ctValue |= (0x01 << 6);
                if (raster.getPixel(x + 1, y, px)[0] <= intensity) ctValue |= (0x01 << 7);
                histogram[Math.min(ctValue, 255)]++;
            }
        }
        // Todo: Check if max normalization helps the case. It has not been mentioned in the original paper, but it'd definitely help with robustness against scaling.
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
        return "SimpleCentrist";
    }

    @Override
    public String getFieldName() {
        return "SimpleCentrist";
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
        if (feature instanceof SimpleCentrist)
            return MetricsUtils.distL1(histogram, feature.getFeatureVector());
        else
            return -1d;
    }

    @Override
    public double[] getFeatureVector() {
        return histogram;
    }
}
