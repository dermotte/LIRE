package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.StringTokenizer;

/**
 * A simple implementation of a joint opponent histogram combining 64-bin RGB and pixel rank.
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class JointOpponentHistogram extends Histogram implements LireFeature {
    private int[] tmpIntensity = new int[1];
    final double sq2 = Math.sqrt(2d);
    final double sq6 = Math.sqrt(3d);
    final double sq3 = Math.sqrt(6d);

    double o1, o2, o3;

    public void extract(BufferedImage bimg) {
        // extract:
        int[][] histogram = new int[64][9];
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++)
                histogram[i][j] = 0;
        }
        WritableRaster grey = ImageUtils.convertImageToGrey(bimg).getRaster();
        WritableRaster raster = bimg.getRaster();
        int[] px = new int[3];
        int[] intens = new int[1];
        int colorPos;
        for (int x = 1; x < raster.getWidth() - 1; x++) {
            for (int y = 1; y < raster.getHeight() - 1; y++) {
                raster.getPixel(x, y, px);
                o1 = (double) (px[0] - px[1]) / sq2;
                o2 = (double) (px[0] + px[1] - 2 * px[2]) / sq6;
                o3 = (double) (px[0] + px[1] + px[2]) / sq3;
                // Normalize ... easier to handle.
                o1 = (o1 + 255d / sq2) / (510d / sq2);
                o2 = (o2 + 510d / sq6) / (1020d / sq6);
                o3 = o3 / (3d * 255d / sq3);
                // get the array position.
                colorPos = (int) Math.min(Math.floor(o1 * 4d), 3d) + (int) Math.min(Math.floor(o2 * 4d), 3d) * 4 + (int) Math.min(3d, Math.floor(o3 * 4d)) * 4 * 4;
                int rank = 0;
                grey.getPixel(x, y, intens);
                if (getIntensity(x - 1, y - 1, grey) > intens[0]) rank++;
                if (getIntensity(x, y - 1, grey) > intens[0]) rank++;
                if (getIntensity(x + 1, y - 1, grey) > intens[0]) rank++;
                if (getIntensity(x - 1, y + 1, grey) > intens[0]) rank++;
                if (getIntensity(x, y + 1, grey) > intens[0]) rank++;
                if (getIntensity(x + 1, y + 1, grey) > intens[0]) rank++;
                if (getIntensity(x - 1, y, grey) > intens[0]) rank++;
                if (getIntensity(x + 1, y, grey) > intens[0]) rank++;
                histogram[colorPos][rank]++;
            }
        }
        // normalize with max norm & quantize to [0,127]:
        descriptor = new double[64 * 9];
        double max = 0;
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++) {
                max = Math.max(histogram[i][j], max);
            }
        }
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++) {
                descriptor[i + 64 * j] = Math.floor(127d * (histogram[i][j] / max));
            }
        }
    }


    private int getIntensity(int x, int y, WritableRaster grey) {
        grey.getPixel(x, y, tmpIntensity);
        return tmpIntensity[0];
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(descriptor.length * 2 + 25);
        sb.append("jophist");
        sb.append(' ');
        sb.append(descriptor.length);
        sb.append(' ');
        for (double aData : descriptor) {
            sb.append((int) aData);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String s) {
        StringTokenizer st = new StringTokenizer(s);
        if (!st.nextToken().equals("jophist"))
            throw new UnsupportedOperationException("This is not a JointOpponentHistogram descriptor.");
        descriptor = new double[Integer.parseInt(st.nextToken())];
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
     * @see CEDD#setByteArrayRepresentation(byte[])
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
     * @see CEDD#getByteArrayRepresentation
     */
    public void setByteArrayRepresentation(byte[] in) {
        descriptor = new double[in.length];
        for (int i = 0; i < descriptor.length; i++) {
            descriptor[i] = in[i];
        }
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        descriptor = new double[length];
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
        if (!(feature instanceof JointOpponentHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");
        return MetricsUtils.jsd(((JointOpponentHistogram) feature).descriptor, descriptor);
    }


}
