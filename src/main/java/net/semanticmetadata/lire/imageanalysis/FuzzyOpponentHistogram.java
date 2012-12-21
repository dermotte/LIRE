package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.StringTokenizer;

/**
 * Simple fuzzy 64 bin Opponent Histogram, based on the Opponent color space as described in van de Sande, Gevers & Snoek (2010)
 * "Evaluating Color Descriptors for Object and Scene Recognition", IEEE PAMI (see BibTeX in the source code). Also there is the
 * rank of the pixel joint.
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 18.12.12
 *         Time: 11:53
 */

/*
@ARTICLE{Sande2010,
    author={van de Sande, K.E.A. and Gevers, T. and Snoek, C.G.M.},
    journal={Pattern Analysis and Machine Intelligence, IEEE Transactions on},
    title={Evaluating Color Descriptors for Object and Scene Recognition},
    year={2010},
    month={sept. },
    volume={32},
    number={9},
    pages={1582 -1596},
    doi={10.1109/TPAMI.2009.154},
    ISSN={0162-8828},
}
*/
public class FuzzyOpponentHistogram extends Histogram implements LireFeature {
    final double sq2 = Math.sqrt(2d);
    final double sq6 = Math.sqrt(3d);
    final double sq3 = Math.sqrt(6d);

    double o1, o2, o3;
    double[] o1f = new double[4];
    double[] o2f = new double[4];
    double[] o3f = new double[4];
    private int[] tmpIntensity = new int[1];


    public void extract(BufferedImage bimg) {
        // extract:
        double[][] histogram = new double[64][9];
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
                getFuzzyMembership(o1, o1f);
                getFuzzyMembership(o2, o2f);
                getFuzzyMembership(o3, o3f);

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

                for (int i = 0; i < o1f.length; i++) {
                    if (o1f[i] == 0) continue;
                    for (int j = 0; j < o2f.length; j++) {
                        if (o2f[j] == 0) continue;
                        for (int k = 0; k < o3f.length; k++) {
                            if (o3f[k] == 0) continue;
                            colorPos = i + j * 3 + k * 3 * 3;
                            histogram[colorPos][rank]+=o1f[i]*o2f[j]*o3f[k];
                        }
                    }
                }
            }
        }
        // normalize with max norm & quantize to [0,127]:
        descriptor = new double[64*9];
        double max = 0;
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++)

                max = Math.max(histogram[i][j], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++)
                descriptor[i+27*j] = Math.floor(127d * (histogram[i][j] / max));
        }
    }

    /**
     * Creates a membership variable for each of the three bins given in out[]
     *
     * @param in
     * @param out the array to put the membership values in.
     */
    private void getFuzzyMembership(double in, double[] out) {
        out[0] = 0d;
        out[1] = 0d;
        out[2] = 0d;
        out[3] = 0d;
        if (in <= 0.15) {
            out[0] = 1d;
        } else if (in > 0.15 && in < 0.25) {
            out[0] = ((in - 0.15) * 10.0);
            out[1] = 1d - out[0];
        } else if (in >= 0.25 && in <= 0.45) {
            out[1] = 1d;
        } else if (in > 0.45 && in < 0.55) {
            out[1] = ((in - 0.45) * 10.0);
            out[2] = 1d - out[1];
        } else if (in >= 0.55 && in <= 0.75) {
            out[2] = 1d;
        } else if (in > 0.75 && in < 0.85) {
            out[2] = ((in - 0.75) * 10.0);
            out[3] = 1d - out[2];
        } else if (in >= 0.85) {
            out[3] = 1d;
        }
    }

    private int getIntensity(int x, int y, WritableRaster grey) {
        grey.getPixel(x, y, tmpIntensity);
        return tmpIntensity[0];
    }



    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[descriptor.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) descriptor[i];
        }
        return result;
    }

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
        return descriptor;
    }

    public float getDistance(LireFeature feature) {
        if (!(feature instanceof FuzzyOpponentHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");
        return (float) MetricsUtils.jsd(((FuzzyOpponentHistogram) feature).descriptor, descriptor);
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(descriptor.length * 2 + 25);
        sb.append("ophist");
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
        if (!st.nextToken().equals("ophist"))
            throw new UnsupportedOperationException("This is not a OpponentHistogram descriptor.");
        descriptor = new double[Integer.parseInt(st.nextToken())];
        for (int i = 0; i < descriptor.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
            descriptor[i] = Integer.parseInt(st.nextToken());
        }

    }
}
