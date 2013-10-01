/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.imageanalysis.filters;

import net.semanticmetadata.lire.utils.ImageUtils;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

/**
 * This class is a simple implementation of a Canny Edge Detector.
 *
 * @author Mathias Lux, mathias@juggle.at, 05.04.13
 */
public class CannyEdgeDetector {
    static ConvolveOp gaussian = new ConvolveOp(new Kernel(5, 5, ImageUtils.makeGaussianKernel(5, 1.4f)));
    static ColorConvertOp grayscale = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);

    int[] tmp255 = {255};
    int[] tmp128 = {128};
    int[] tmp000 = {0};
    int[] tmpPixel = {0};
    // double thresholds for Canny edge detector
    double thresholdLow = 60, thresholdHigh = 100;

    BufferedImage bimg;

     /**
      * Create a Canny Edge Detector for the given image. Set the thresholds yourself. Use {@link CannyEdgeDetector#filter} to create
      * the edge image.
      * @param image the input image.
      * @param thresholdHigh higher of the thresholds
      * @param thresholdLow lower of the thresholds
      */
    public CannyEdgeDetector(BufferedImage image, double thresholdHigh, double thresholdLow) {
        this.bimg = image;
        this.thresholdHigh = thresholdHigh;
        this.thresholdLow = thresholdLow;
    }

     /**
      * Create a Canny Edge Detector for the given image. Set the thresholds yourself. Use {@link CannyEdgeDetector#filter} to create
      * the edge image.
      * @param bimg
      */
    public CannyEdgeDetector(BufferedImage bimg) {
        this.bimg = bimg;
    }

    /**
     * Returns the edge image in grayscale. Edges are black (int 0) all other pixels are white (int 255)
     * @return the filtered image.
     */
    public BufferedImage filter() {
        // All for Canny Edge ...
        BufferedImage gray;
        double[][] gx, gy; // Sobel outputs in x and y direction.
        double[][] gd, gm; // gradient direction and gradient magnitude

        // doing canny edge detection first:
        // filter images:
        gray = grayscale.filter(bimg, null);
        gray = gaussian.filter(gray, null);
        gx = sobelFilterX(gray);
        gy = sobelFilterY(gray);
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
                gm[x][y] = Math.hypot(gy[x][y], gx[x][y]);
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
                    // check if pixel is a local maximum ...
                    if (gm[x][y] > gm[x + 1][y] && gm[x][y] > gm[x - 1][y])
                        setPixel(x, y, gray, gm[x][y]);
                    else
                        gray.getRaster().setPixel(x, y, tmp255);
                } else if (gd[x][y] < (3d * Math.PI / 8d) && gd[x][y] >= (Math.PI / 8d)) {
                    // check if pixel is a local maximum ...
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
        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {
                if (gray.getRaster().getPixel(x, y, tmp)[0] < 50) {
                    // It's a strong pixel, lets find the neighbouring weak ones.
                    trackWeakOnes(x, y, gray);
                }
            }
        }
        // removing the single weak pixels.
        for (int x = 2; x < width - 2; x++) {
            for (int y = 2; y < height - 2; y++) {
                if (gray.getRaster().getPixel(x, y, tmp)[0] > 50) {
                    gray.getRaster().setPixel(x, y, tmp255);
                }
            }
        }
        return gray;
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

    /**
     * Distinguishes between weak and strong edge points based on the thresholds given.
     * @param x
     * @param y
     * @param gray
     * @param v
     */
    private void setPixel(int x, int y, BufferedImage gray, double v) {
        if (v > thresholdLow) gray.getRaster().setPixel(x, y, tmp000);
        else if (v > thresholdHigh) gray.getRaster().setPixel(x, y, tmp128);
        else gray.getRaster().setPixel(x, y, tmp255);
    }

    private double[][] sobelFilterX(BufferedImage gray) {
        double[][] result = new double[gray.getWidth()][gray.getHeight()];
        int[] tmp = new int[1];
        int tmpSum;
        for (int x = 1; x < gray.getWidth() - 1; x++) {
            for (int y = 1; y < gray.getHeight() - 1; y++) {
                tmpSum = 0;
                tmpSum += gray.getRaster().getPixel(x - 1, y - 1, tmp)[0];
                tmpSum += 2 * gray.getRaster().getPixel(x - 1, y, tmp)[0];
                tmpSum += gray.getRaster().getPixel(x - 1, y + 1, tmp)[0];
                tmpSum -= gray.getRaster().getPixel(x + 1, y - 1, tmp)[0];
                tmpSum -= 2 * gray.getRaster().getPixel(x + 1, y, tmp)[0];
                tmpSum -= gray.getRaster().getPixel(x + 1, y + 1, tmp)[0];
                result[x][y] = tmpSum;
            }
        }
        for (int x = 0; x < gray.getWidth(); x++) {
            result[x][0] = 0;
            result[x][gray.getHeight() - 1] = 0;
        }
        for (int y = 0; y < gray.getHeight(); y++) {
            result[0][y] = 0;
            result[gray.getWidth() - 1][y] = 0;
        }
        return result;
    }

    private double[][] sobelFilterY(BufferedImage gray) {
        double[][] result = new double[gray.getWidth()][gray.getHeight()];
        int[] tmp = new int[1];
        int tmpSum = 0;
        for (int x = 1; x < gray.getWidth() - 1; x++) {
            for (int y = 1; y < gray.getHeight() - 1; y++) {
                tmpSum = 0;
                tmpSum += gray.getRaster().getPixel(x - 1, y - 1, tmp)[0];
                tmpSum += 2 * gray.getRaster().getPixel(x, y - 1, tmp)[0];
                tmpSum += gray.getRaster().getPixel(x + 1, y - 1, tmp)[0];
                tmpSum -= gray.getRaster().getPixel(x - 1, y + 1, tmp)[0];
                tmpSum -= 2 * gray.getRaster().getPixel(x, y + 1, tmp)[0];
                tmpSum -= gray.getRaster().getPixel(x + 1, y + 1, tmp)[0];
                result[x][y] = tmpSum;
            }
        }
        for (int x = 0; x < gray.getWidth(); x++) {
            result[x][0] = 0;
            result[x][gray.getHeight() - 1] = 0;
        }
        for (int y = 0; y < gray.getHeight(); y++) {
            result[0][y] = 0;
            result[gray.getWidth() - 1][y] = 0;
        }
        return result;
    }


}
