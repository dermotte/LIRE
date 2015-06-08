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
 * Updated: 11.07.13 10:07
 */

package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.util.Arrays;

/**
 * This class is built the same way as PHOG, but instead of measuring the orientation of gradients, this class uses a
 * histogram of rotation-invariant local binary patterns.
 *
 * @author Mathias Lux, mathias@juggle.at, 06.07.13
 */

public class BinaryPatternsPyramid implements GlobalFeature {
    static ColorConvertOp grayscale = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    int[] tmp255 = {255};
    int[] tmp128 = {128};
    int[] tmp000 = {0};
    int[] tmpPixel = {0};
    // double thresholds for Canny edge detector
    double thresholdLow = 80, thresholdHigh = 100;
    static int[] binTranslate = new int[256];

    int bins = 36;
    double[] histogram = new double[bins + 4 * bins + 4 * 4 * bins];


    static {
        Arrays.fill(binTranslate, 0);
        binTranslate[0] = 0;
        binTranslate[1] = 1;
        binTranslate[3] = 2;
        binTranslate[5] = 3;
        binTranslate[7] = 4;
        binTranslate[9] = 5;
        binTranslate[11] = 6;
        binTranslate[13] = 7;
        binTranslate[15] = 8;
        binTranslate[17] = 9;
        binTranslate[19] = 10;
        binTranslate[21] = 11;
        binTranslate[23] = 12;
        binTranslate[25] = 13;
        binTranslate[27] = 14;
        binTranslate[29] = 15;
        binTranslate[31] = 16;
        binTranslate[37] = 17;
        binTranslate[39] = 18;
        binTranslate[43] = 19;
        binTranslate[45] = 20;
        binTranslate[47] = 21;
        binTranslate[51] = 22;
        binTranslate[53] = 23;
        binTranslate[55] = 24;
        binTranslate[59] = 25;
        binTranslate[61] = 26;
        binTranslate[63] = 27;
        binTranslate[85] = 28;
        binTranslate[87] = 29;
        binTranslate[91] = 30;
        binTranslate[95] = 31;
        binTranslate[111] = 32;
        binTranslate[119] = 33;
        binTranslate[127] = 34;
        binTranslate[255] = 35;
    }

    @Override
    public void extract(BufferedImage bimg) {
        // All for Canny Edge ...
        BufferedImage imgEdges, imgGray;
        double[][] gx = null, gy = null;
        double[][] gd, gm;

        // doing canny edge detection first:
        // filter images:
        imgEdges = grayscale.filter(bimg, new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_BYTE_GRAY));
        imgGray = grayscale.filter(bimg, new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_BYTE_GRAY));
//        gray = gaussian.filter(gray, null);
        // TODO: Combine the next few steps to just iterate through the pixels once!
        gx = new double[imgEdges.getWidth()][imgEdges.getHeight()];
        gy = new double[imgEdges.getWidth()][imgEdges.getHeight()];
        sobelFilter(imgEdges, gx, gy);
//        gx = sobelFilterX(gray);
//        gy = sobelFilterY(gray);
        int width = imgEdges.getWidth();
        int height = imgEdges.getHeight();
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
                gm[x][y] = Math.hypot(gy[x][y], gx[x][y]);
            }
        }
        // Non-maximum suppression
        for (int x = 0; x < width; x++) {
            imgEdges.getRaster().setPixel(x, 0, new int[]{255});
            imgEdges.getRaster().setPixel(x, height - 1, new int[]{255});
        }
        for (int y = 0; y < height; y++) {
            imgEdges.getRaster().setPixel(0, y, new int[]{255});
            imgEdges.getRaster().setPixel(width - 1, y, new int[]{255});
        }
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (gd[x][y] < (Math.PI / 8d) && gd[x][y] >= (-Math.PI / 8d)) {
                    if (gm[x][y] > gm[x + 1][y] && gm[x][y] > gm[x - 1][y])
                        setPixel(x, y, imgEdges, gm[x][y]);
                    else
                        imgEdges.getRaster().setPixel(x, y, tmp255);
                } else if (gd[x][y] < (3d * Math.PI / 8d) && gd[x][y] >= (Math.PI / 8d)) {
                    if (gm[x][y] > gm[x - 1][y - 1] && gm[x][y] > gm[x - 1][y - 1])
                        setPixel(x, y, imgEdges, gm[x][y]);
                    else
                        imgEdges.getRaster().setPixel(x, y, tmp255);
                } else if (gd[x][y] < (-3d * Math.PI / 8d) || gd[x][y] >= (3d * Math.PI / 8d)) {
                    if (gm[x][y] > gm[x][y + 1] && gm[x][y] > gm[x][y + 1])
                        setPixel(x, y, imgEdges, gm[x][y]);
                    else
                        imgEdges.getRaster().setPixel(x, y, tmp255);
                } else if (gd[x][y] < (-Math.PI / 8d) && gd[x][y] >= (-3d * Math.PI / 8d)) {
                    if (gm[x][y] > gm[x + 1][y - 1] && gm[x][y] > gm[x - 1][y + 1])
                        setPixel(x, y, imgEdges, gm[x][y]);
                    else
                        imgEdges.getRaster().setPixel(x, y, tmp255);
                } else {
                    imgEdges.getRaster().setPixel(x, y, tmp255);
                }
            }
        }
        // hysteresis ... walk along lines of strong pixels and make the weak ones strong.
        int[] tmp = {0};
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (imgEdges.getRaster().getPixel(x, y, tmp)[0] < 50) {
                    // It's a strong pixel, lets find the neighbouring weak ones.
                    trackWeakOnes(x, y, imgEdges);
                }
            }
        }
        // removing the single weak pixels.
        for (int x = 2; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {
                if (imgEdges.getRaster().getPixel(x, y, tmp)[0] > 50) {
                    imgEdges.getRaster().setPixel(x, y, tmp255);
                }
            }
        }

        // Canny Edge Detection over ... lets go for the PHOG ...

//        histogram = new double[bins + 4 * bins + 4 * 4 * bins];
        // for level 3:
//        histogram = new double[5 * bins + 4*4*bins + 4*4*4*bins];
        //level0
        System.arraycopy(getHistogram(0, 0, width, height, imgEdges, imgGray, gd), 0, histogram, 0, bins);
        //level1
        System.arraycopy(getHistogram(0, 0, width / 2, height / 2, imgEdges, imgGray, gd),
                0, histogram, bins, bins);
        System.arraycopy(getHistogram(width / 2, 0, width / 2, height / 2, imgEdges, imgGray, gd),
                0, histogram, 2 * bins, bins);
        System.arraycopy(getHistogram(0, height / 2, width / 2, height / 2, imgEdges, imgGray, gd),
                0, histogram, 3 * bins, bins);
        System.arraycopy(getHistogram(width / 2, height / 2, width / 2, height / 2, imgEdges, imgGray, gd),
                0, histogram, 4 * bins, bins);
        // level 2
        int wstep = width / 4;
        int hstep = height / 4;
        int binPos = 5; // the next free section in the histogram
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                System.arraycopy(getHistogram(i * wstep, j * hstep, wstep, hstep, imgEdges, imgGray, gd),
                        0, histogram, binPos * bins, bins);
                binPos++;
            }
        }

        // level 3
//        wstep = width / 8;
//        hstep = height / 8;
//        for (int i = 0; i < 8; i++) {
//            for (int j = 0; j < 8; j++) {
//                System.arraycopy(getHistogram(i * wstep, j * hstep, wstep, hstep, imgEdges, imgGray, gd),
//                        0, histogram, binPos * bins, bins);
//                binPos++;
//            }
//        }

    }

    @Override
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[histogram.length/2];
        int tmp;
        // stuffing two values in one byte.
        for (int i = 0; i < result.length; i++) {
            tmp = ((int) (histogram[(i << 1)])) << 4;
            tmp = (tmp | ((int) (histogram[(2 * i) + 1])));
            result[i] = (byte) (tmp - 128);
        }
        return result;
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        setByteArrayRepresentation(in, 0, in.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        int tmp;
        for (int i = offset; i < length; i++) {
            tmp = in[i]+128;
            histogram[((i-offset) << 1) + 1] = ((double) (tmp & 0x000F));
            histogram[(i-offset) << 1] = ((double) (tmp >> 4));
        }
    }

    @Override
    public double[] getFeatureVector() {
        return histogram;
    }

    @Override
    public double getDistance(LireFeature feature) {
        // chi^2 distance ... as mentioned in the paper.
//        double distance = 0;
//        double lower;
//        for (int i = 0; i < histogram.length; i++) {
//            lower = histogram[i] + ((BinaryPatternsPyramid) feature).histogram[i];
//            if (lower > 0)
//                distance += (histogram[i] - ((BinaryPatternsPyramid) feature).histogram[i]) * (histogram[i] - ((BinaryPatternsPyramid) feature).histogram[i]) / lower;
//        }
//        return (float) distance;
        return MetricsUtils.distL1(histogram, ((BinaryPatternsPyramid) feature).histogram);
    }

    private int getBin(int[] pattern) {
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            min = Math.min(getNumber(pattern), min);
            // rotate:
            int tmp = pattern[7];
            for (int j = pattern.length - 1; j > 0; j--) {
                pattern[j] = pattern[j - 1];
            }
            pattern[0] = tmp;
        }
        return binTranslate[min];
    }

    private int getNumber(int[] pattern) {
        int result = 0;
        int current = 1;
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i] > 0) result += current;
            current *= 2;
        }
        return result;
    }

    private double[] getHistogram(int startX, int startY, int width, int height, BufferedImage edges, BufferedImage original, double gd[][]) {
        int[] tmp = {0};
        double[] result = new double[36];
        double actual = 0;
        int bin;
        int[] pixel = new int[9];
        int[] pattern = new int[8];

        // set initial histogram to 0
        Arrays.fill(result, 0d);
        // find and increment the right bin/s
        for (int x = startX; x < startX + width - 2; x++) {
            for (int y = startY; y < startY + height - 2; y++) {
                if (edges.getRaster().getPixel(x, y, tmp)[0] < 50) {
                    // And now for the binary patterns ...
                    Arrays.fill(pattern, 0);
                    original.getRaster().getPixels(x, y, 3, 3, pixel);
                    if (pixel[0] >= pixel[4]) pattern[0] = 1;
                    if (pixel[1] >= pixel[4]) pattern[1] = 1;
                    if (pixel[2] >= pixel[4]) pattern[2] = 1;
                    if (pixel[5] >= pixel[4]) pattern[3] = 1;
                    if (pixel[8] >= pixel[4]) pattern[4] = 1;
                    if (pixel[7] >= pixel[4]) pattern[5] = 1;
                    if (pixel[6] >= pixel[4]) pattern[6] = 1;
                    if (pixel[3] >= pixel[4]) pattern[7] = 1;
                    result[getBin(pattern)]++;
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
                result[i] = Math.floor(16d * result[i] / max);
                result[i] = Math.min(15d, result[i]);
            }
        }

        return result;
    }

    /**
     * Recursive tracking of weak points.
     *
     * @param x
     * @param y
     * @param gray
     */
    private void trackWeakOnes(int x, int y, BufferedImage gray) {
        for (int xx = x - 1; xx <= x + 1; xx++)
            for (int yy = y - 1; yy <= y + 1; yy++) {
                if (isWeak(xx, yy, gray)) {
                    gray.getRaster().setPixel(xx, yy, tmp000);
                    trackWeakOnes(xx, yy, gray);
                }
            }
    }

    private boolean isWeak(int x, int y, BufferedImage gray) {
        return (gray.getRaster().getPixel(x, y, tmpPixel)[0] > 0 && gray.getRaster().getPixel(x, y, tmpPixel)[0] < 255);
    }

    private void setPixel(int x, int y, BufferedImage gray, double v) {
        if (v > thresholdLow) gray.getRaster().setPixel(x, y, tmp000);
        else if (v > thresholdHigh) gray.getRaster().setPixel(x, y, tmp128);
        else gray.getRaster().setPixel(x, y, tmp255);
    }

    private void sobelFilter(BufferedImage gray, double[][] gx, double[][] gy) {
        int[] tmp = new int[4];
        int tmpSumX = 0, tmpSumY = 0, pix;
        for (int x = 1; x < gray.getWidth() - 1; x++) {
            for (int y = 1; y < gray.getHeight() - 1; y++) {
                tmpSumX = 0;
                tmpSumY = 0;
                pix = gray.getRaster().getPixel(x - 1, y - 1, tmp)[0];
                tmpSumX += pix;
                tmpSumY += pix;
                pix = gray.getRaster().getPixel(x - 1, y, tmp)[0];
                tmpSumX += 2 * pix;
                pix = gray.getRaster().getPixel(x - 1, y + 1, tmp)[0];
                tmpSumX += pix;
                tmpSumY -= pix;

                pix = gray.getRaster().getPixel(x + 1, y - 1, tmp)[0];
                tmpSumX -= pix;
                tmpSumY += pix;
                pix = gray.getRaster().getPixel(x + 1, y, tmp)[0];
                tmpSumX -= 2 * pix;
                pix = gray.getRaster().getPixel(x + 1, y + 1, tmp)[0];
                tmpSumX -= pix;
                tmpSumY -= pix;
                gx[x][y] = tmpSumX;

                tmpSumY += 2 * gray.getRaster().getPixel(x, y - 1, tmp)[0];
                tmpSumY -= 2 * gray.getRaster().getPixel(x, y + 1, tmp)[0];
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
    public String getFeatureName() {
        return "Spatial Pyramid of Local Binary Patterns";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_BINARY_PATTERNS_PYRAMID;
    }
}
