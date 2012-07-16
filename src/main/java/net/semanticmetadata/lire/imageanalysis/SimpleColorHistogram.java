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
 * ~~~~~~~~~~~~~~~~~~~~
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
public class SimpleColorHistogram implements LireFeature {
    public static int DEFAULT_NUMBER_OF_BINS = 512;
    public static HistogramType DEFAULT_HISTOGRAM_TYPE = HistogramType.RGB;
    public static DistanceFunction DEFAULT_DISTANCE_FUNCTION = DistanceFunction.L1;

    private static final int[] quantTable = {
            1, 32, 4, 8, 16, 4, 16, 4, 16, 4,            // Hue, Sum - subspace 0,1,2,3,4 for 256 levels
            1, 16, 4, 4, 8, 4, 8, 4, 8, 4,            // Hue, Sum - subspace 0,1,2,3,4 for 128 levels
            1, 8, 4, 4, 4, 4, 8, 2, 8, 1,            // Hue, Sum - subspace 0,1,2,3,4 for  64 levels
            1, 8, 4, 4, 4, 4, 4, 1, 4, 1};           // Hue, Sum - subspace 0,1,2,3,4 for  32 levels

    public static int[][] rgbPalette64 = new int[][]{
            new int[]{0, 0, 0},
            new int[]{0, 0, 85},
            new int[]{0, 0, 170},
            new int[]{0, 0, 255},
            new int[]{0, 85, 0},
            new int[]{0, 85, 85},
            new int[]{0, 85, 170},
            new int[]{0, 85, 255},
            new int[]{0, 170, 0},
            new int[]{0, 170, 85},
            new int[]{0, 170, 170},
            new int[]{0, 170, 255},
            new int[]{0, 255, 0},
            new int[]{0, 255, 85},
            new int[]{0, 255, 170},
            new int[]{0, 255, 255},
            new int[]{85, 0, 0},
            new int[]{85, 0, 85},
            new int[]{85, 0, 170},
            new int[]{85, 0, 255},
            new int[]{85, 85, 0},
            new int[]{85, 85, 85},
            new int[]{85, 85, 170},
            new int[]{85, 85, 255},
            new int[]{85, 170, 0},
            new int[]{85, 170, 85},
            new int[]{85, 170, 170},
            new int[]{85, 170, 255},
            new int[]{85, 255, 0},
            new int[]{85, 255, 85},
            new int[]{85, 255, 170},
            new int[]{85, 255, 255},
            new int[]{170, 0, 0},
            new int[]{170, 0, 85},
            new int[]{170, 0, 170},
            new int[]{170, 0, 255},
            new int[]{170, 85, 0},
            new int[]{170, 85, 85},
            new int[]{170, 85, 170},
            new int[]{170, 85, 255},
            new int[]{170, 170, 0},
            new int[]{170, 170, 85},
            new int[]{170, 170, 170},
            new int[]{170, 170, 255},
            new int[]{170, 255, 0},
            new int[]{170, 255, 85},
            new int[]{170, 255, 170},
            new int[]{170, 255, 255},
            new int[]{255, 0, 0},
            new int[]{255, 0, 85},
            new int[]{255, 0, 170},
            new int[]{255, 0, 255},
            new int[]{255, 85, 0},
            new int[]{255, 85, 85},
            new int[]{255, 85, 170},
            new int[]{255, 85, 255},
            new int[]{255, 170, 0},
            new int[]{255, 170, 85},
            new int[]{255, 170, 170},
            new int[]{255, 170, 255},
            new int[]{255, 255, 0},
            new int[]{255, 255, 85},
            new int[]{255, 255, 170},
            new int[]{255, 255, 255}
    };

    // upper borders for quantization.
    public static int[] quant512 = new int[]{18, 55, 91, 128, 165, 201, 238, 256};

//    public static int[][] rgbPalette512 = new int[512][3];
//
//    public static int[][][] quantTable512 = new int[256][256][256];

//    static {
//        System.out.println("Creating quantization tables ...");
//        int count = 0;
//        for (int i = 0; i < quant512.length; i++) {
//            for (int j = 0; j < quant512.length; j++) {
//                for (int k = 0; k < quant512.length; k++) {
//                    rgbPalette512[count][0] = quant512[i];
//                    rgbPalette512[count][1] = quant512[j];
//                    rgbPalette512[count][2] = quant512[k];
//                    count++;
//                }
//            }
//        }
//
//        // Todo: This is a no go  ... check faster method ...
//        System.out.println("Now the big one ...");
//        for (int i = 0; i < 256; i++) {
//            for (int j = 0; j < 256; j++) {
//                for (int k = 0; k < 256; k++) {
//                    double minDist = Math.abs((rgbPalette512[0][0] - i) + Math.abs((rgbPalette512[0][1] - j)) + Math.abs(rgbPalette512[0][2] - k));
//                    int pos = 0;
//                    for (int l = 1; l < rgbPalette512.length; l++) {
//                        double tmp = Math.abs((rgbPalette512[l][0] - i) + Math.abs((rgbPalette512[l][1] - j)) + Math.abs(rgbPalette512[l][2] - k));
//                        if (tmp <= minDist) {
//                            minDist = tmp;
//                            pos = l;
//                        }
//                        quantTable512[i][j][k] = pos;
//                    }
//                }
//            }
//            System.out.print('.');
//        }
//        System.out.println("static method finished");
//
//    }

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
    public SimpleColorHistogram() {
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
    public SimpleColorHistogram(HistogramType histogramType, DistanceFunction distFunction) {
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
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                raster.getPixel(x, y, pixel);
                if (histogramType == HistogramType.HSV) {
                    rgb2hsv(pixel[0], pixel[1], pixel[2], pixel);
                    histogram[quant(pixel)]++;
                } else if (histogramType == HistogramType.Luminance) {
                    rgb2yuv(pixel[0], pixel[1], pixel[2], pixel);
                } else if (histogramType == HistogramType.HMMD) {
                    histogram[quantHmmd(rgb2hmmd(pixel[0], pixel[1], pixel[2]), DEFAULT_NUMBER_OF_BINS)]++;
                } else // RGB 
                    histogram[quant(pixel)]++;
            }
        }
        normalize(histogram, image.getWidth() * image.getHeight());
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
        int max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(histogram[i], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = (histogram[i] * 256) / max;
        }
    }

    private int quant(int[] pixel) {
        if (histogramType == HistogramType.HSV) {
            int qH = (int) Math.floor(pixel[0] / 11.25);    // more granularity in color
            if (qH == 32) qH--;
            int qV = pixel[1] / 90;
            if (qV == 4) qV--;
            int qS = pixel[2] / 25;
            if (qS == 4) qS--;
            return qH * 16 + qV * 4 + qS;
        } else if (histogramType == HistogramType.HMMD) {
            return quantHmmd(rgb2hmmd(pixel[0], pixel[1], pixel[2]), 255);
        } else if (histogramType == HistogramType.Luminance) {
            return (pixel[0] * histogram.length) / (256);
        } else {
            // just for 512 bins ...
            int bin = 0;
            if (histogram.length == 512) {
                for (int i = 0; i < quant512.length - 1; i++) {
                    if (quant512[i] <= pixel[0] && pixel[0] < quant512[i + 1]) bin += (i + 1);
                    if (quant512[i] <= pixel[1] && pixel[1] < quant512[i + 1]) bin += (i + 1) * 8;
                    if (quant512[i] <= pixel[2] && pixel[2] < quant512[i + 1]) bin += (i + 1) * 8 * 8;
                }
                return bin;
            }
            // and for 64 bins ...
            else {
                double minDist = Math.pow((rgbPalette64[0][0] - pixel[0]), 2) + Math.pow((rgbPalette64[0][1] - pixel[1]), 2) + Math.pow((rgbPalette64[0][2] - pixel[2]), 2);
                int pos = 0;
                for (int i = 1; i < rgbPalette64.length; i++) {
                    double tmp = Math.pow((rgbPalette64[i][0] - pixel[0]), 2) + Math.pow((rgbPalette64[i][1] - pixel[1]), 2) + Math.pow((rgbPalette64[i][2] - pixel[2]), 2);
                    if (tmp <= minDist) {
                        minDist = tmp;
                        pos = i;
                    }
                }
                return pos;
            }
        }
    }

    public float getDistance(LireFeature vd) {
        // Check if instance of the right class ...
        if (!(vd instanceof SimpleColorHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");

        // casting ...
        SimpleColorHistogram ch = (SimpleColorHistogram) vd;

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

    /**
     * Quantize hmmd values based on the MPEG-7 standard.
     *
     * @param hmmd               the HMMD color value
     * @param quantizationLevels only 256, 128, 64 and 32 are allowed.
     * @return the actual bin
     */
    private int quantHmmd(int[] hmmd, int quantizationLevels) {
        int h = 0;
        int offset = 0;    // offset position in the quantization table
        int subspace = 0;
        int q = 0;

        // define the subspace along the Diff axis

        if (hmmd[3] < 7) subspace = 0;
        else if ((hmmd[3] > 6) && (hmmd[3] < 21)) subspace = 1;
        else if ((hmmd[3] > 19) && (hmmd[3] < 61)) subspace = 2;
        else if ((hmmd[3] > 59) && (hmmd[3] < 111)) subspace = 3;
        else if ((hmmd[3] > 109) && (hmmd[3] < 256)) subspace = 4;

        // HMMD Color Space quantization
        // see MPEG7-CSD.pdf

        if (quantizationLevels == 256) {
            offset = 0;
            h = (int) ((hmmd[0] / quantizationLevels) * quantTable[offset + subspace] + (hmmd[4] / quantizationLevels) * quantTable[offset + subspace + 1]);
        } else if (quantizationLevels == 128) {
            offset = 10;
            h = (int) ((hmmd[0] / quantizationLevels) * quantTable[offset + subspace] + (hmmd[4] / quantizationLevels) * quantTable[offset + subspace + 1]);
        } else if (quantizationLevels == 64) {
            offset = 20;
            h = (int) ((hmmd[0] / quantizationLevels) * quantTable[offset + subspace] + (hmmd[4] / quantizationLevels) * quantTable[offset + subspace + 1]);

        } else if (quantizationLevels == 32) {
            offset = 30;
            h = (int) ((hmmd[0] / quantizationLevels) * quantTable[offset + subspace] + (hmmd[4] / quantizationLevels) * quantTable[offset + subspace + 1]);
        }


        return h;
    }
}
