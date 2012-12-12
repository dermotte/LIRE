package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.StringTokenizer;

/**
 * A simple implementation of a joint histogram combining 64-bin RGB and pixel rank.
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class JointHistogram extends Histogram implements LireFeature {
    public void extract(BufferedImage bimg) {
        // extract:
        int[][] histogram = new int[64][9];
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++)
                histogram[i][j] = 0;
        }
        WritableRaster raster = bimg.getRaster();
        int[] px = new int[3];
        for (int x = 1; x < raster.getWidth() - 1; x++) {
            for (int y = 1; y < raster.getHeight() - 1; y++) {
                raster.getPixel(x, y, px);
                int colorPos = (int) Math.round((double) px[2] / 85d) +
                        (int) Math.round((double) px[1] / 85d) * 4 +
                        (int) Math.round((double) px[0] / 85d) * 4 * 4;
                int rank = 0;
                double intensity = getIntensity(px);
                if (getIntensity(raster.getPixel(x - 1, y - 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x, y - 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x + 1, y - 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x - 1, y + 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x, y + 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x + 1, y + 1, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x - 1, y, px)) > intensity) rank++;
                if (getIntensity(raster.getPixel(x + 1, y, px)) > intensity) rank++;
                histogram[colorPos][rank]++;
            }
        }
        // normalize with max norm & quantize to [0,127]:
        descriptor = new float[64 * 9];
        float max = 0;
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++) {
                max = Math.max((float) histogram[i][j], max);
            }
        }
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++) {
                descriptor[i + 64 * j] = (float) Math.floor(127f * ((float) histogram[i][j] / max));
            }
        }
    }


    private double getIntensity(int[] px) {
        return (0.3 * px[0] + 0.6 * px[1] + 0.1 * px[2]);
    }

    public String getStringRepresentation() { // added by mlux
        StringBuilder sb = new StringBuilder(descriptor.length * 2 + 25);
        sb.append("jhist");
        sb.append(' ');
        sb.append(descriptor.length);
        sb.append(' ');
        for (double aData : descriptor) {
            sb.append((int) aData);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String s) { // added by mlux
        StringTokenizer st = new StringTokenizer(s);
        if (!st.nextToken().equals("jhist"))
            throw new UnsupportedOperationException("This is not a JointHistogram descriptor.");
        descriptor = new float[Integer.parseInt(st.nextToken())];
        for (int i = 0; i < descriptor.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
            descriptor[i] = Integer.parseInt(st.nextToken());
        }

    }

    /**
     * Provides a much faster way of serialization.
     *
     * @return a byte array that can be read with the corresponding method.
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#setByteArrayRepresentation(byte[])
     */
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[descriptor.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) descriptor[i];
        }
        return result;
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#getByteArrayRepresentation
     */
    public void setByteArrayRepresentation(byte[] in) {
        descriptor = new float[in.length];
        for (int i = 0; i < descriptor.length; i++) {
            descriptor[i] = in[i];
        }
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        descriptor = new float[length];
        for (int i = offset; i < length; i++) {
            descriptor[i] = in[i];
        }
    }

    public double[] getDoubleHistogram() {
        double[] result = new double[descriptor.length];
        for (int i = 0; i < descriptor.length; i++) {
            result[i] = descriptor[i];
        }
        return result;
    }

    public float getDistance(LireFeature feature) {
        if (!(feature instanceof JointHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");
        return MetricsUtils.jsd(((JointHistogram) feature).descriptor, descriptor);
    }


}
