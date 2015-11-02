/*
 * This file is part of the LIRE project: http://lire-project.net
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 11.07.13 10:28
 */
package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.utils.ColorConversion;
import net.semanticmetadata.lire.utils.ConversionUtils;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

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
public class SimpleColorHistogram implements GlobalFeature {
    public static int DEFAULT_NUMBER_OF_BINS = 64;
    public static HistogramType DEFAULT_HISTOGRAM_TYPE = HistogramType.RGB;
    public static DistanceFunction DEFAULT_DISTANCE_FUNCTION = DistanceFunction.JSD;

    private static final int[] quantTable = {
            1, 32, 4, 8, 16, 4, 16, 4, 16, 4,            // Hue, Sum - subspace 0,1,2,3,4 for 256 levels
            1, 16, 4, 4, 8, 4, 8, 4, 8, 4,            // Hue, Sum - subspace 0,1,2,3,4 for 128 levels
            1, 8, 4, 4, 4, 4, 8, 2, 8, 1,            // Hue, Sum - subspace 0,1,2,3,4 for  64 levels
            1, 8, 4, 4, 4, 4, 4, 1, 4, 1};           // Hue, Sum - subspace 0,1,2,3,4 for  32 levels

    public static final int[][] rgbPalette64 = new int[][]{
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
    public static final int[] quant512 = new int[]{18, 55, 91, 128, 165, 201, 238, 256};

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
    @Override
    public void extract(BufferedImage image) {
        image = ImageUtils.get8BitRGBImage(image);
        Arrays.fill(histogram, 0);
        WritableRaster raster = image.getRaster();
        for (int x = 0; x < image.getWidth(); x++) {
            for (int y = 0; y < image.getHeight(); y++) {
                raster.getPixel(x, y, pixel);
                if (histogramType == HistogramType.HSV) {
                    ColorConversion.rgb2hsv(pixel[0], pixel[1], pixel[2], pixel);
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

    @Override
    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(histogram);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        histogram = SerializationUtils.toIntArray(in);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        histogram = SerializationUtils.toIntArray(in, offset, length);
    }

    @Override
    public double[] getFeatureVector() {
        return ConversionUtils.toDouble(histogram);
    }

    private void normalize(int[] histogram, int numPixels) {
        int max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(histogram[i], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = (histogram[i] * 255) / max;
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
                int pos = (int) Math.round((double) pixel[2] / 85d) +
                        (int) Math.round((double) pixel[1] / 85d) * 4 +
                        (int) Math.round((double) pixel[0] / 85d) * 4 * 4;
                return pos;
            }
        }
    }

    @Override
    public double getDistance(LireFeature vd) {
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
            return MetricsUtils.jsd(histogram, ch.histogram);
        else if (distFunc == DistanceFunction.TANIMOTO)
            return MetricsUtils.tanimoto(histogram, ch.histogram);
        else if (distFunc == DistanceFunction.L1)
            return MetricsUtils.distL1(histogram, ch.histogram);
        else
            return MetricsUtils.distL2(histogram, ch.histogram);
    }

//    public String getStringRepresentation() {
//        StringBuilder sb = new StringBuilder(histogram.length * 4);
//        sb.append(histogramType.name());
//        sb.append(' ');
//        sb.append(histogram.length);
//        sb.append(' ');
//        for (int i = 0; i < histogram.length; i++) {
//            sb.append(histogram[i]);
//            sb.append(' ');
//        }
//        return sb.toString().trim();
//    }
//
//    public void setStringRepresentation(String s) {
//        StringTokenizer st = new StringTokenizer(s);
//        histogramType = HistogramType.valueOf(st.nextToken());
//        histogram = new int[Integer.parseInt(st.nextToken())];
//        for (int i = 0; i < histogram.length; i++) {
//            if (!st.hasMoreTokens())
//                throw new IndexOutOfBoundsException("Too few numbers in string representation!");
//            histogram[i] = Integer.parseInt(st.nextToken());
//        }
//    }

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
    public void rgb2yuv(int r, int g, int b, int[] yuv) { //TODO: rgb2yuv Conversion
        int y = (int) (0.299 * r + 0.587 * g + 0.114 * b);
        int u = (int) ((b - y) * 0.492f);
        int v = (int) ((r - y) * 0.877f);

        yuv[0] = y;
        yuv[1] = u;
        yuv[2] = v;
    }


    /**
     * Adapted under GPL from VizIR: author was adis@ims.tuwien.ac.at
     */
    private int[] rgb2hmmd(int ir, int ig, int ib) {    //TODO: rgb2hmmd Conversion
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

    @Override
    public String getFeatureName() {
        return "RGB Color Histogram";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_COLORHISTOGRAM;
    }
}
