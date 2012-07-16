/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */
package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.utils.ConversionUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.StringTokenizer;

/**
 * This class provides a simple color histogram for content based image retrieval.
 * Number of bins is configurable, histogram is normalized to 8 bit per bin (0-255). <br>
 * Defaults are given in the final fields. Available options are given by the enums.
 * <p/>
 * <br/>Date: 14.05.2008
 * <br/>Time: 09:47:10
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class HSVColorHistogram implements LireFeature {
    private static float q_h = 32;
    private static float q_s = 8;
    private static float q_v = 1;
    public static final int DEFAULT_NUMBER_OF_BINS = (int) (q_h * q_s * q_v);
    public static HistogramType DEFAULT_HISTOGRAM_TYPE = HistogramType.HSV;
    public static DistanceFunction DEFAULT_DISTANCE_FUNCTION = DistanceFunction.L2;

    /**
     * Lists possible types for the histogram class
     */
    public enum HistogramType {
        RGB, HSV, Luminance, HMMD
    }

    /**
     * Lists distance functions possible for this histogram class
     */
    public enum DistanceFunction {
        L1, L2, TANIMOTO, JSD
    }

    /**
     * Temporary pixel field ... re used for speed and memory issues ...
     */
    private int[] pixel = new int[3];
    private int[] histogram;
    private HistogramType histogramType;
    private DistanceFunction distFunc;

    /**
     * Default constructor
     */
    public HSVColorHistogram() {
        histogramType = DEFAULT_HISTOGRAM_TYPE;
        histogram = new int[DEFAULT_NUMBER_OF_BINS];
        distFunc = DEFAULT_DISTANCE_FUNCTION;
    }

    /**
     * Constructor for selecting different color spaces as well as a different distance function.
     * Histogram has 256 bins.
     *
     * @param histogramType
     * @param distFunction
     */
    public HSVColorHistogram(HistogramType histogramType, DistanceFunction distFunction) {
        this.histogramType = histogramType;
        distFunc = distFunction;
        histogram = new int[DEFAULT_NUMBER_OF_BINS];
    }

    /**
     * Extracts the color histogram from the given image.
     *
     * @param image
     */
    public void extract(BufferedImage image) {
        if (image.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
            throw new UnsupportedOperationException("Color space not supported. Only RGB.");
        WritableRaster raster = image.getRaster();
        int count = 0;
        for (int x = 0; x < image.getWidth() - 40; x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                raster.getPixel(x, y, pixel);
                rgb2hsv(pixel[0], pixel[1], pixel[2], pixel);
                histogram[quant(pixel)]++;
                count++;
            }
        }
        // normalize(histogram, count);
    }

    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(histogram);
    }

    public void setByteArrayRepresentation(byte[] in) {
        histogram = SerializationUtils.toIntArray(in);
    }

    public double[] getDoubleHistogram() {
        return ConversionUtils.toDouble(histogram);
    }

    private void normalize(int[] histogram, int numPixels) {
        // find max:
        int max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(histogram[i], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = (histogram[i] * 1024) / max;
        }
    }

    private int quant(int[] pixel) {
//            int qH = (int) Math.floor((pixel[0] * 64f) / 360f);    // more granularity in color
//            int qS = (int) Math.floor((pixel[2] * 8f) / 100f);
//            return qH * 7 + qS;
        int qH = (int) Math.floor((pixel[0] * q_h) / 360f);    // more granularity in color
        int qS = (int) Math.floor((pixel[2] * q_s) / 100f);
        int qV = (int) Math.floor((pixel[1] * q_v) / 100f);
        if (qH == q_h) qH = (int) (q_h - 1);
        if (qS == q_s) qS = (int) (q_s - 1);
        if (qV == q_v) qV = (int) (q_v - 1);
        return (qH) * (int) (q_v * q_s) + qS * (int) q_v + qV;
    }

    public float getDistance(LireFeature vd) {
        // Check if instance of the right class ...
        if (!(vd instanceof HSVColorHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");

        // casting ...
        HSVColorHistogram ch = (HSVColorHistogram) vd;

        // check if parameters are fitting ...
        if ((ch.histogram.length != histogram.length) || (ch.histogramType != histogramType))
            throw new UnsupportedOperationException("Histogram lengths or color spaces do not match");

        // do the comparison ...
        double sum = 0;
        if (distFunc == DistanceFunction.JSD)
            return (float) jsd(histogram, ch.histogram);
        else if (distFunc == DistanceFunction.TANIMOTO)
            return (float) tanimoto(histogram, ch.histogram);
        else if (distFunc == DistanceFunction.L1)
            return (float) distL1(histogram, ch.histogram);
        else
            return (float) distL2(histogram, ch.histogram);
    }

    /**
     * Manhattan distance
     *
     * @param h1
     * @param h2
     * @return
     */
    private static double distL1(int[] h1, int[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            sum += Math.abs(h1[i] - h2[i]);
        }
        return sum / h1.length;
    }

    /**
     * Euclidean distance
     *
     * @param h1
     * @param h2
     * @return
     */
    private static double distL2(int[] h1, int[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            sum += (h1[i] - h2[i]) * (h1[i] - h2[i]);
        }
        return Math.sqrt(sum);
    }

    /**
     * Jeffrey Divergence or Jensen-Shannon divergence (JSD) from
     * Deselaers, T.; Keysers, D. & Ney, H. Features for image retrieval: an experimental comparison Inf. Retr., Kluwer Academic Publishers, 2008, 11, 77-107
     *
     * @param h1
     * @param h2
     * @return
     */
    private static double jsd(int[] h1, int[] h2) {
        double sum = 0d;
        for (int i = 0; i < h1.length; i++) {
            sum += h1[i] > 0 ? h1[i] * Math.log(2d * h1[i] / (h1[i] + h2[i])) : 0 +
                    h2[i] > 0 ? h2[i] * Math.log(2d * h2[i] / (h1[i] + h2[i])) : 0;
        }
        return sum;
    }

    private static double tanimoto(int[] h1, int[] h2) {
        double result = 0;
        double tmp1 = 0;
        double tmp2 = 0;

        double tmpCnt1 = 0, tmpCnt2 = 0, tmpCnt3 = 0;

        for (int i = 0; i < h1.length; i++) {
            tmp1 += h1[i];
            tmp2 += h2[i];
        }

        if (tmp1 == 0 || tmp2 == 0) result = 100;
        if (tmp1 == 0 && tmp2 == 0) result = 0;

        if (tmp1 > 0 && tmp2 > 0) {
            for (int i = 0; i < h1.length; i++) {
                tmpCnt1 += (h1[i] / tmp1) * (h2[i] / tmp2);
                tmpCnt2 += (h2[i] / tmp2) * (h2[i] / tmp2);
                tmpCnt3 += (h1[i] / tmp1) * (h1[i] / tmp1);

            }

            result = (100 - 100 * (tmpCnt1 / (tmpCnt2 + tmpCnt3
                    - tmpCnt1))); //Tanimoto
        }
        return result;
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(histogram.length * 4);
        sb.append(histogramType.name());
        sb.append(' ');
        sb.append(histogram.length);
        sb.append(' ');
        for (int i = 0; i < histogram.length; i++) {
            sb.append(histogram[i]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String s) {
        StringTokenizer st = new StringTokenizer(s);
        histogramType = HistogramType.valueOf(st.nextToken());
        histogram = new int[Integer.parseInt(st.nextToken())];
        for (int i = 0; i < histogram.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation!");
            histogram[i] = Integer.parseInt(st.nextToken());
        }
    }

    /* **************************************************************
   * Color Conversion routines ...
   ************************************************************** */

    /**
     * Adapted from ImageJ documentation:
     * http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector//HTMLHelp/farbraumJava.htm
     *
     * @param r
     * @param g
     * @param b
     * @param yuv
     */
    public static void rgb2yuv(int r, int g, int b, int[] yuv) {
        int y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        int u = (int) ((b - y) * 0.492f);
        int v = (int) ((r - y) * 0.877f);

        yuv[0] = y;
        yuv[1] = u;
        yuv[2] = v;
    }

    /**
     * Adapted from ImageJ documentation:
     * http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector//HTMLHelp/farbraumJava.htm
     *
     * @param r
     * @param g
     * @param b
     * @param hsv
     */
    public static void rgb2hsv(int r, int g, int b, int hsv[]) {

        int min;    //Min. value of RGB
        int max;    //Max. value of RGB
        int delMax; //Delta RGB value

        min = Math.min(r, g);
        min = Math.min(min, b);

        max = Math.max(r, g);
        max = Math.max(max, b);

        delMax = max - min;

//        System.out.println("hsv = " + hsv[0] + ", " + hsv[1] + ", "  + hsv[2]);

        float H = 0f, S = 0f;
        float V = max / 255f;

        if (delMax == 0) {
            H = 0f;
            S = 0f;
        } else {
            S = delMax / 255f;
            if (r == max) {
                if (g >= b) {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60;
                } else {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60 + 360;
                }
            } else if (g == max) {
                H = (2 + (b / 255f - r / 255f) / (float) delMax / 255f) * 60;
            } else if (b == max) {
                H = (4 + (r / 255f - g / 255f) / (float) delMax / 255f) * 60;
            }
        }
//        System.out.println("H = " + H);
        hsv[0] = (int) (H);
        hsv[1] = (int) (S * 100);
        hsv[2] = (int) (V * 100);
    }

    /**
     * Adapted under GPL from VizIR: author was adis@ims.tuwien.ac.at
     */
    private static int[] rgb2hmmd(int ir, int ig, int ib) {
        int hmmd[] = new int[5];

        float max = (float) Math.max(Math.max(ir, ig), Math.max(ig, ib));
        float min = (float) Math.min(Math.min(ir, ig), Math.min(ig, ib));

        float diff = (max - min);
        float sum = (float) ((max + min) / 2.);

        float hue = 0;
        if (diff == 0) hue = 0;
        else if (ir == max && (ig - ib) > 0) hue = 60 * (ig - ib) / (max - min);
        else if (ir == max && (ig - ib) <= 0) hue = 60 * (ig - ib) / (max - min) + 360;
        else if (ig == max) hue = (float) (60 * (2. + (ib - ir) / (max - min)));
        else if (ib == max) hue = (float) (60 * (4. + (ir - ig) / (max - min)));

        diff /= 2;

        hmmd[0] = (int) (hue);
        hmmd[1] = (int) (max);
        hmmd[2] = (int) (min);
        hmmd[3] = (int) (diff);
        hmmd[4] = (int) (sum);

        return (hmmd);
    }
}