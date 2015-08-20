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
 * Updated: 12.07.13 16:56
 */

package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.DataBufferByte;
import java.util.Arrays;

/**
 * The PHOG descriptor is described in Anna Bosch, Andrew Zisserman & Xavier Munoz (2007) "Representing shape with a
 * spatial pyramid kernel", CVIR 2007. It basically combines histograms of edges in several spatial pyramid levels.
 *
 * @author Mathias Lux, mathias@juggle.at, 05.04.13
 */
public class PHOG implements GlobalFeature {
    static ColorConvertOp grayscale = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    int[] tmp255 = {255};
    int[] tmp128 = {128};
    int[] tmp000 = {0};
    int[] tmpPixel = {0};
    int tmp;
    // double thresholds for Canny edge detector
    double thresholdLow = 60, thresholdHigh = 100;

    // And now for PHOG:
    public static int bins = 30;
    double[] tmpHistogram;
    byte[] histogram = new byte[bins + 4 * bins + 4 * 4 * bins];
    //    double[] histogram = new double[5 * bins + 4*4*bins + 4*4*4*bins];
    // used to quantize bins to [0, quantizationFactor]
    // Note that a quantization factor of 127d has better precision, but is not supported by the current serialization method.
    private double quantizationFactor = 15d;

    @Override
    public void extract(BufferedImage bimg) {
        // All for Canny Edge ...
        BufferedImage gray;
        double[][] gx = null, gy = null;
        double[][] gd, gm;

        // doing canny edge detection first:
        // filter images:
        gray = grayscale.filter(bimg, new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_BYTE_GRAY));
//        gray = gaussian.filter(gray, null);
        // TODO: Combine the next few steps to just iterate through the pixels once!
        gx = new double[gray.getWidth()][gray.getHeight()];
        gy = new double[gray.getWidth()][gray.getHeight()];
        sobelFilter(gray, gx, gy);
//        gx = sobelFilterX(gray);
//        gy = sobelFilterY(gray);
        int width = gray.getWidth();
        int height = gray.getHeight();
        gd = new double[width][height];
        gm = new double[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                // setting gradient magnitude and gradient direction
                if (gx[x][y] != 0) {
                    gd[x][y] = Math.atan(gy[x][y] / gx[x][y]);
                } else {
                    gd[x][y] = Math.PI / 2d;
                }
                gm[x][y] = Math.sqrt(gy[x][y] * gy[x][y] + gx[x][y] * gx[x][y]);
//                gm[x][y] = Math.hypot(gy[x][y], gx[x][y]);
            }
        }
        // Non-maximum suppression
        for (int x = 0; x < width; x++) {
            gray.getRaster().setPixel(x, 0, new int[]{255});
            gray.getRaster().setPixel(x, height - 1, new int[]{255});
        }
        for (int y = 0; y < height; y++) {
            gray.getRaster().setPixel(0, y, new int[]{255});
            gray.getRaster().setPixel(width - 1, y, new int[]{255});
        }
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (gd[x][y] < (Math.PI / 8d) && gd[x][y] >= (-Math.PI / 8d)) {
                    if (gm[x][y] > gm[x + 1][y] && gm[x][y] > gm[x - 1][y])
                        setPixel(x, y, gray, gm[x][y]);
                    else
                        gray.getRaster().setPixel(x, y, tmp255);
                } else if (gd[x][y] < (3d * Math.PI / 8d) && gd[x][y] >= (Math.PI / 8d)) {
                    if (gm[x][y] > gm[x - 1][y - 1] && gm[x][y] > gm[x + 1][y + 1])
                        setPixel(x, y, gray, gm[x][y]);
                    else
                        gray.getRaster().setPixel(x, y, tmp255);
                } else if (gd[x][y] < (-3d * Math.PI / 8d) || gd[x][y] >= (3d * Math.PI / 8d)) {
                    if (gm[x][y] > gm[x][y + 1] && gm[x][y] > gm[x][y - 1])
                        setPixel(x, y, gray, gm[x][y]);
                    else
                        gray.getRaster().setPixel(x, y, tmp255);
                } else if (gd[x][y] < (-Math.PI / 8d) && gd[x][y] >= (-3d * Math.PI / 8d)) {
                    if (gm[x][y] > gm[x + 1][y - 1] && gm[x][y] > gm[x - 1][y + 1])
                        setPixel(x, y, gray, gm[x][y]);
                    else
                        gray.getRaster().setPixel(x, y, tmp255);
                } else {
                    gray.getRaster().setPixel(x, y, tmp255);
                }
            }
        }
        // hysteresis ... walk along lines of strong pixels and make the weak ones strong.
        int[] tmp = {0};
        byte[] data = ((DataBufferByte) gray.getRaster().getDataBuffer()).getData();
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (((int) data[(y) * width + (x)] & 0xFF) < 50) {
                    // It's a strong pixel, lets find the neighbouring weak ones.
                    trackWeakOnes(x, y, width, data);
                }
            }
        }
        // removing the single weak pixels.
        for (int x = 2; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {
                if (((int) data[(y) * width + (x)] & 0xFF) > 50) {
                    data[(y) * width + (x)] = (byte) 255;
//                        gray.getRaster().setPixel(x, y, tmp255);
                }
            }
        }

        // Canny Edge Detection over ... lets go for the PHOG ...
        tmpHistogram = new double[bins + 4 * bins + 4 * 4 * bins];
        // for level 3:
//        histogram = new double[5 * bins + 4*4*bins + 4*4*4*bins];
        //level0
        System.arraycopy(getHistogram(0, 0, width, height, gray, gd), 0, tmpHistogram, 0, bins);
        //level1
        System.arraycopy(getHistogram(0, 0, width / 2, height / 2, gray, gd),
                0, tmpHistogram, bins, bins);
        System.arraycopy(getHistogram(width / 2, 0, width / 2, height / 2, gray, gd),
                0, tmpHistogram, 2 * bins, bins);
        System.arraycopy(getHistogram(0, height / 2, width / 2, height / 2, gray, gd),
                0, tmpHistogram, 3 * bins, bins);
        System.arraycopy(getHistogram(width / 2, height / 2, width / 2, height / 2, gray, gd),
                0, tmpHistogram, 4 * bins, bins);
        // level 2
        int wstep = width / 4;
        int hstep = height / 4;
        int binPos = 5; // the next free section in the histogram
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                System.arraycopy(getHistogram(i * wstep, j * hstep, wstep, hstep, gray, gd),
                        0, tmpHistogram, binPos * bins, bins);
                binPos++;
            }
        }
        // finally copy it to the byte[] array to save memory at search time.
        for (int i = 0; i < tmpHistogram.length; i++) {
            histogram[i] = (byte) tmpHistogram[i];
        }
        // level 3
//        wstep = width / 8;
//        hstep = height / 8;
//        for (int i = 0; i< 8; i++) {
//            for (int j=0; j<8; j++) {
//                System.arraycopy(getHistogram(i*wstep, j*hstep, wstep, hstep, gray, gd),
//                        0, histogram, binPos*bins, bins);
//                binPos++;
//            }
//        }
    }

    /**
     * Create and normalize histogram.
     *
     * @param startX
     * @param startY
     * @param width
     * @param height
     * @param gray
     * @param gd
     * @return
     */
    private double[] getHistogram(int startX, int startY, int width, int height, BufferedImage gray, double gd[][]) {
        int[] tmp = {0};
        double[] result = new double[bins];
        double actual = 0;
        int bin;
        // set initial histogram to 0
        for (int i = 0; i < result.length; i++) result[i] = 0;
        // find and increment the right bin/s
        for (int x = startX; x < startX + width; x++) {
            for (int y = startY; y < startY + height; y++) {
                if (gray.getRaster().getPixel(x, y, tmp)[0] < 50) {
                    // it's an edge pixel, so it counts in.
                    actual = (gd[x][y] / Math.PI + 0.5) * (bins);
                    if (actual == Math.floor(actual)) {  // if it's a discrete thing ...
                        bin = ((int) Math.floor(actual));
                        if (bin == bins) bin = 0;
                        result[bin] += 1;
                    } else { // in between: we make it fuzzy ...
                        bin = ((int) Math.floor(actual));
                        if (bin == bins) bin = 0;
                        result[bin] += actual - Math.floor(actual);
                        bin = (int) Math.ceil(actual);
                        if (bin == bins) bin = 0;
                        result[bin] += Math.ceil(actual) - actual;
                    }
                }
            }
        }
        // normalize histogram to max norm.
        double max = 0d;
        for (int i = 0; i < result.length; i++) {
            max = Math.max(result[i], max);
        }
        if (max > 0d) {
            for (int i = 0; i < result.length; i++) {
                // quantize single values to xx steps to compress feature a little bit.
                result[i] = Math.floor(quantizationFactor * result[i] / max);
                result[i] = Math.min(quantizationFactor, result[i]);
            }
        }
        return result;
    }

    /**
     * Recursive tracking of weak points.
     *
     * @param x
     * @param y
     * @param buffer the actual image but only its buffer.
     */
    private void trackWeakOnes(int x, int y, int width, byte[] buffer) {
        for (int xx = x - 1; xx <= x + 1; xx++)
            for (int yy = y - 1; yy <= y + 1; yy++) {
                if (((int) buffer[(yy) * width + (xx)] & 0xFF) > 0 && ((int) buffer[(yy) * width + (xx)] & 0xFF) < 255) {
                    buffer[(yy) * width + (xx)] = (byte) 0;
//                    gray.getRaster().setPixel(xx, yy, tmp000);
                    trackWeakOnes(xx, yy, width, buffer);
                }
            }
    }

    private void setPixel(int x, int y, BufferedImage gray, double v) {
        if (v > thresholdLow) gray.getRaster().setPixel(x, y, tmp000);
        else if (v > thresholdHigh) gray.getRaster().setPixel(x, y, tmp128);
        else gray.getRaster().setPixel(x, y, tmp255);
    }

    private void sobelFilter(BufferedImage gray, double[][] gx, double[][] gy) {
        int[] tmp = new int[4];
        int tmpSumX = 0, tmpSumY = 0, pix, width = gray.getWidth();
        byte[] data = ((DataBufferByte) gray.getRaster().getDataBuffer()).getData();
        for (int x = 1; x < gray.getWidth() - 1; x++) {
            for (int y = 1; y < gray.getHeight() - 1; y++) {
                tmpSumX = 0;
                tmpSumY = 0;
//                    pix = gray.getRaster().getPixel(x - 1, y - 1, tmp)[0];
                pix = (int) data[(y - 1) * width + (x - 1)] & 0xFF;
                tmpSumX += pix;
                tmpSumY += pix;
//                    pix = gray.getRaster().getPixel(x - 1, y, tmp)[0];
                pix = (int) data[(y) * width + (x - 1)] & 0xFF;
                tmpSumX += 2 * pix;
//                    pix = gray.getRaster().getPixel(x - 1, y + 1, tmp)[0];
                pix = (int) data[(y + 1) * width + (x - 1)] & 0xFF;
                tmpSumX += pix;
                tmpSumY -= pix;

//                    pix = gray.getRaster().getPixel(x + 1, y - 1, tmp)[0];
                pix = (int) data[(y - 1) * width + (x + 1)] & 0xFF;
                tmpSumX -= pix;
                tmpSumY += pix;
//                    pix = gray.getRaster().getPixel(x + 1, y, tmp)[0];
                pix = (int) data[(y) * width + (x + 1)] & 0xFF;
                tmpSumX -= 2 * pix;
//                    pix = gray.getRaster().getPixel(x + 1, y + 1, tmp)[0];
                pix = (int) data[(y + 1) * width + (x + 1)] & 0xFF;
                tmpSumX -= pix;
                tmpSumY -= pix;
                gx[x][y] = tmpSumX;

//                    tmpSumY += 2 * gray.getRaster().getPixel(x    , y - 1, tmp)[0];
                tmpSumY += 2 * ((int) data[(y - 1) * width + (x)] & 0xFF);
//                    tmpSumY -= 2 * gray.getRaster().getPixel(x    , y + 1, tmp)[0];
                tmpSumY -= 2 * ((int) data[(y + 1) * width + (x)] & 0xFF);
                gy[x][y] = tmpSumY;

            }
        }
        for (int x = 0; x < gray.getWidth(); x++) {
            gx[x][0] = 0;
            gx[x][gray.getHeight() - 1] = 0;
            gy[x][0] = 0;
            gy[x][gray.getHeight() - 1] = 0;
        }
        for (int y = 0; y < gray.getHeight(); y++) {
            gx[0][y] = 0;
            gx[gray.getWidth() - 1][y] = 0;
            gy[0][y] = 0;
            gy[gray.getWidth() - 1][y] = 0;
        }
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[histogram.length / 2];
        for (int i = 0; i < result.length; i++) {
            tmp = ((int) (histogram[(i << 1)])) << 4;
            tmp = (tmp | ((int) (histogram[(i << 1) + 1])));
            result[i] = (byte) (tmp - 128);
//            result[i] = (byte) histogram[i];
        }
        return result;
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        setByteArrayRepresentation(in, 0, in.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        for (int i = 0; i < length; i++) {
            tmp = in[i + offset] + 128;
            histogram[(i << 1) + 1] = (byte) (tmp & 0x000F);
            histogram[i << 1] = (byte) (tmp >> 4);
        }
    }

    @Override
    public double[] getFeatureVector() {
        return SerializationUtils.castToDoubleArray(histogram);
    }

    @Override
    public double getDistance(LireFeature feature) {
        // chi^2 distance ... as mentioned in the paper.
//        double distance = 0;
//        double lower;
//        for (int i = 0; i < histogram.length; i++) {
//            lower = histogram[i] + ((PHOG) feature).histogram[i];
//            if (lower > 0)
//                distance += (histogram[i] - ((PHOG) feature).histogram[i]) * (histogram[i] - ((PHOG) feature).histogram[i]) / lower;
//        }
//        return (float) distance;
        return MetricsUtils.distL1(histogram, ((PHOG) feature).histogram);
    }

    @Override
    public String toString() {
        return "PHOG{" + Arrays.toString(getFeatureVector()) + "}";
    }

    @Override
    public String getFeatureName() {
        return "PHOG";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_PHOG;
    }
}
