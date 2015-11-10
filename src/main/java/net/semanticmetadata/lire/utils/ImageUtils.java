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
 * Updated: 30.11.14 13:51
 */

package net.semanticmetadata.lire.utils;

import com.sun.corba.se.spi.ior.Writeable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * Some little helper methods.<br>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 02.02.2006
 * <br>Time: 23:33:36
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ImageUtils {
    /**
     * Scales down an image into a box of maxSideLength x maxSideLength.
     *
     * @param image         the image to scale down. It remains untouched.
     * @param maxSideLength the maximum side length of the scaled down instance. Has to be > 0.
     * @return the scaled image if the original image is bigger than the scaled version, the original instance otherwise.
     */
    public static BufferedImage scaleImage(BufferedImage image, int maxSideLength) {
        assert (maxSideLength > 0);
        double originalWidth = image.getWidth();
        double originalHeight = image.getHeight();
        double scaleFactor = 0.0;
        if (originalWidth > originalHeight) {
            scaleFactor = ((double) maxSideLength / originalWidth);
        } else {
            scaleFactor = ((double) maxSideLength / originalHeight);
        }
        // create new image
        if (scaleFactor < 1 && (int) Math.round(originalWidth * scaleFactor) > 1 && (int) Math.round(originalHeight * scaleFactor) > 1) {
//            BufferedImage img = new BufferedImage((int) Math.round(originalWidth * scaleFactor), (int) Math.round(originalHeight * scaleFactor), BufferedImage.TYPE_INT_RGB);
            BufferedImage img = new BufferedImage((int) Math.round(originalWidth * scaleFactor), (int) Math.round(originalHeight * scaleFactor), image.getType());
            // fast scale (Java 1.4 & 1.5)
            Graphics g = img.getGraphics();
//        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
            ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
            g.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
            return img;
        } else
            return image;
    }

    /**
     * Scale image to an arbitrary shape not retaining proportions and aspect ratio.
     *
     * @param image
     * @param width
     * @param height
     * @return
     */
    public static BufferedImage scaleImage(BufferedImage image, int width, int height) {
        assert (width > 0 && height > 0);
        // create image of new size
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // fast scale (Java 1.4 & 1.5)
        Graphics g = img.getGraphics();
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
        return img;
    }

    public static BufferedImage cropImage(BufferedImage image, int fromX, int fromY, int width, int height) {
        assert (width > 0 && height > 0);
        int toX = Math.min(fromX + width, image.getWidth());
        int toY = Math.min(fromY + height, image.getHeight());
        // create smaller image
        BufferedImage cropped = new BufferedImage(toX - fromX, toY - fromY, BufferedImage.TYPE_INT_RGB);
        // fast scale (Java 1.4 & 1.5)
        Graphics g = cropped.getGraphics();
        g.drawImage(image, 0, 0, cropped.getWidth(), cropped.getHeight(), fromX, fromY, toX, toY, null);
        return cropped;
    }

    /**
     * Converts an image to grey. Use instead of color conversion op, which yields strange results.
     *
     * @param image
     */
    public static BufferedImage getGrayscaleImage(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        result.getGraphics().drawImage(image, 0, 0, null);
        return result;
    }

    /**
     * Inverts a grey scale image.
     *
     * @param image
     */
    public static void invertImage(BufferedImage image) {
        WritableRaster inRaster = image.getRaster();
        int[] p = new int[3];
//        float v = 0;
        for (int x = 0; x < inRaster.getWidth(); x++) {
            for (int y = 0; y < inRaster.getHeight(); y++) {
                inRaster.getPixel(x, y, p);
                p[0] = 255 - p[0];
                inRaster.setPixel(x, y, p);
            }
        }
    }

    /**
     * Converts an image to a standard internal representation.
     * Taken from OpenIMAJ. Thanks to these guys!
     * http://sourceforge.net/p/openimaj
     *
     * @param bimg
     * @return
     */
    public static BufferedImage createWorkingCopy(BufferedImage bimg) {
        BufferedImage image;
        if (bimg.getType() == BufferedImage.TYPE_INT_RGB) {
            image = bimg;
        } else {
            image = new BufferedImage(bimg.getWidth(), bimg.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = image.createGraphics();
            g2d.drawImage(bimg, null, 0, 0);
        }
        return image;
    }

    /**
     * Trims the white border around an image.
     *
     * @param img
     * @return a new image, hopefully trimmed.
     */
    public static BufferedImage trimWhiteSpace(BufferedImage img) {
        return trimWhiteSpace(img, 250, 0, 0, 0, 0);
    }

    /**
     * Trims the white border around an image.
     *
     * @param img
     * @return a new image, hopefully trimmed.
     */
    public static BufferedImage trimWhiteSpace(BufferedImage img, int whiteThreshold, int startTop, int startRight, int startBottom, int startLeft) {
        return trimWhiteSpace(img, img, whiteThreshold, startTop, startRight, startBottom, startLeft);
    }

    public static BufferedImage trimWhiteSpace(BufferedImage src, BufferedImage tgt, int whiteThreshold, int startTop, int startRight, int startBottom, int startLeft) {
        // idea is to scan lines of an image starting from each side.
        // As soon as a scan line encounters non-white (or non-black) pixels we know there is actual image content.
        WritableRaster raster = getGrayscaleImage(src).getRaster();
        int[] pixels = new int[Math.max(raster.getWidth(), raster.getHeight())];
        int thresholdWhite = whiteThreshold;
        int trimTop = startTop, trimBottom = startBottom, trimLeft = startLeft, trimRight = startRight;
        boolean white = true;
        while (white) {
            raster.getPixels(0, trimTop, raster.getWidth(), 1, pixels);
            for (int i = 0; i < raster.getWidth(); i++) {
                if (pixels[i] < thresholdWhite) white = false;
            }
            if (white) {
                trimTop++;
                // handling white only images ..
                if (trimTop > raster.getHeight() - 10) {
                    System.err.println("Only white ...");
                    return src;
                }
            }
        }
        // bottom:
        white = true;
        while (white) {
            raster.getPixels(0, raster.getHeight() - 1 - trimBottom, raster.getWidth(), 1, pixels);
            for (int i = 0; i < raster.getWidth(); i++) {
                if (pixels[i] < thresholdWhite) white = false;
            }
            if (white) {
                trimBottom++;
            }
        }
        // left:
        white = true;
        while (white) {
            raster.getPixels(trimLeft, 0, 1, raster.getHeight(), pixels);
            for (int i = 0; i < raster.getHeight(); i++) {
                if (pixels[i] < thresholdWhite) white = false;
            }
            if (white) {
                trimLeft++;
            }
        }
        // left:
        white = true;
        while (white) {
            raster.getPixels(raster.getWidth() - 1 - trimRight, 0, 1, raster.getHeight(), pixels);
            for (int i = 0; i < raster.getHeight(); i++) {
                if (pixels[i] < thresholdWhite) white = false;
            }
            if (white) {
                trimRight++;
            }
        }
//        System.out.println("trimTop = " + trimTop);
//        System.out.println("trimBottom = " + trimBottom);
//        System.out.println("trimLeft = " + trimLeft);
//        System.out.println("trimRight = " + trimRight);
        BufferedImage result = new BufferedImage(raster.getWidth() - (trimLeft + trimRight), raster.getHeight() - (trimTop + trimBottom), BufferedImage.TYPE_INT_RGB);
        result.getGraphics().drawImage(tgt, 0, 0, result.getWidth(), result.getHeight(), trimLeft, trimTop, src.getWidth() - trimRight, src.getHeight() - trimBottom, null);
        return result;
    }

    /**
     * Non-local means denoising.
     *
     * @param img
     * @return
     */
    public static BufferedImage denoiseImage(BufferedImage img) {
        double h = 5d;
        double sigma = 2d;
        int distance = 5;

        double sigma2 = 2 * sigma * sigma;
        double h2 = h * h;
        // make it grayscale
        BufferedImage result = getGrayscaleImage(img);
        WritableRaster raster = result.getRaster();
        int[] p = new int[1];  // actual pixel
        int[] pCol = new int[img.getHeight()];  // actual pixels
        int[] pColD = new int[img.getHeight()];  // actual pixel
        // now for each pixel:
        for (int x = 0; x < raster.getWidth(); x++) {
            raster.getPixels(x, 0, 1, raster.getHeight(), pCol);
            for (int y = 0; y < raster.getHeight(); y++) {
                // get pixel value:
//                raster.getPixel(x, y, p);
                if (pCol[y] < 250) { // speed up by only checking non-white pixels.
                    double weightSum = 0;
                    double weight = 0;
                    double graySum = 0;
                    for (int dx = Math.max(0, x - distance); dx < Math.min(raster.getWidth(), x + distance); dx++) {
                        raster.getPixels(dx, 0, 1, raster.getHeight(), pColD);
                        for (int dy = Math.max(0, y - distance); dy < Math.min(y + distance, raster.getHeight()); dy++) {
                            if (dx != x && dy != y) {
                                double d2 = (pCol[y] - pColD[dy]) * (pCol[y] - pColD[dy]);
                                //                        double d2 = (x - dx) * (x - dx) + (y - dy) * (y - dy);
                                weight = Math.exp(-1d / (h2) * Math.max(0d, d2 - sigma2));
                                weightSum += weight;
                                //                        raster.getPixel(dx, dy, dp);
                                graySum += pColD[dy] * weight;
                            }
                        }
                    }
                    p[0] = ((int) (graySum / weightSum));
                    raster.setPixel(x, y, p);
                }
            }
        }
        return result;
    }

    public static BufferedImage removeScratches(BufferedImage img) {
        int thresholdGray = 196;
        int thresholdCount = 12;
        BufferedImage result = getGrayscaleImage(img);
        WritableRaster raster;
        int[] pCol = new int[img.getHeight()];
        int[] pRow = new int[img.getWidth()];
        if (false) { // equalize histogram or not.
            raster = result.getRaster();
            // histogram equalization, uniform:
            int min = 255, max = 0;
            for (int x = 0; x < raster.getWidth(); x++) {
                raster.getPixels(x, 0, 1, raster.getHeight(), pCol);
                for (int y = 0; y < raster.getHeight(); y++) {
                    min = Math.min(pCol[y], min);
                    max = Math.max(pCol[y], max);
                }
            }
            for (int x = 0; x < raster.getWidth(); x++) {
                raster.getPixels(x, 0, 1, raster.getHeight(), pCol);
                for (int y = 0; y < raster.getHeight(); y++) {
                    pCol[y] = ((int) (255* (pCol[y] - min) / ((double) max - min) ));
                }
                raster.setPixels(x, 0, 1, raster.getHeight(), pCol);
            }
        }
        ConvolveOp op = new ConvolveOp(new Kernel(5, 5, ImageUtils.makeGaussianKernel(5, 1.0f)));
        result = op.filter(result, null);
        raster = result.getRaster();
        Arrays.fill(pCol, 255);
        Arrays.fill(pRow, 255);
        int[] p = new int[1];
        // repair from the convolveop
        raster.setPixels(0, 0, 1, raster.getHeight(), pCol);
        raster.setPixels(1, 0, 1, raster.getHeight(), pCol);
        raster.setPixels(raster.getWidth() - 1, 0, 1, raster.getHeight(), pCol);
        raster.setPixels(raster.getWidth() - 2, 0, 1, raster.getHeight(), pCol);
        raster.setPixels(0, 0, raster.getWidth(), 1, pRow);
        raster.setPixels(0, 1, raster.getWidth(), 1, pRow);
        raster.setPixels(0, raster.getHeight() - 1, raster.getWidth(), 1, pRow);
        raster.setPixels(0, raster.getHeight() - 2, raster.getWidth(), 1, pRow);
        // now for each pixel:
        for (int x = 0; x < raster.getWidth(); x++) {
            raster.getPixels(x, 0, 1, raster.getHeight(), pCol);
            for (int y = 0; y < raster.getHeight(); y++) {
                int[] count = {0};
                raster.getPixel(x, y, p);
                if (p[0] < thresholdGray) { // thresholding
                    // track and find out how many are connected.
                    checkNeighbour(raster, x, y, thresholdGray, thresholdCount, count);
                    if (count[0] < thresholdCount) {
                        p[0] = 255;
                    }
                } else {
                    p[0] = 255;
                }
                raster.setPixel(x, y, p);
            }
//            raster.setPixels(x, 0, 1, raster.getHeight(), pCol);
        }
        return result;
    }

    private static void checkNeighbour(WritableRaster raster, int x, int y, int thresholdGray, int thresholdCount, int[] count) {
        if (count[0] > thresholdCount)
            return;
        int[] pixel = new int[1];
        if (x >= raster.getWidth()) return;
        if (y >= raster.getHeight()) return;
        raster.getPixel(x, y, pixel);
        if (pixel[0] < thresholdGray) {
            count[0] += 1;
            if (count[0] < thresholdCount) {
//            checkNeighbour(raster, x, y, thresholdGray, thresholdCount, count);
                checkNeighbour(raster, x, y + 1, thresholdGray, thresholdCount, count);
                checkNeighbour(raster, x + 1, y, thresholdGray, thresholdCount, count);
                checkNeighbour(raster, x + 1, y + 1, thresholdGray, thresholdCount, count);
            }
        }
    }

    /**
     * creates a Gaussian kernel for ConvolveOp for blurring an image
     *
     * @param radius the radius, i.e. 5
     * @param sigma  sigma, i.e. 1.4f
     * @return
     */
    public static float[] makeGaussianKernel(int radius, float sigma) {
        float[] kernel = new float[radius * radius];
        float sum = 0;
        for (int y = 0; y < radius; y++) {
            for (int x = 0; x < radius; x++) {
                int off = y * radius + x;
                int xx = x - radius / 2;
                int yy = y - radius / 2;
                kernel[off] = (float) Math.pow(Math.E, -(xx * xx + yy * yy)
                        / (2 * (sigma * sigma)));
                sum += kernel[off];
            }
        }
        for (int i = 0; i < kernel.length; i++)
            kernel[i] /= sum;
        return kernel;
    }

    public static BufferedImage differenceOfGaussians(BufferedImage image) {
        BufferedImage img1 = getGrayscaleImage(image);
        BufferedImage img2 = getGrayscaleImage(image);

        ConvolveOp gaussian1 = new ConvolveOp(new Kernel(5, 5, ImageUtils.makeGaussianKernel(5, 1.0f)));
        ConvolveOp gaussian2 = new ConvolveOp(new Kernel(5, 5, ImageUtils.makeGaussianKernel(5, 2.0f)));

        img1 = gaussian1.filter(img1, null);
        img2 = gaussian2.filter(img2, null);

        WritableRaster r1 = img1.getRaster();
        WritableRaster r2 = img2.getRaster();
        int[] tmp1 = new int[3];
        int[] tmp2 = new int[3];
        for (int x = 0; x < img1.getWidth(); x++) {
            for (int y = 0; y < img1.getHeight(); y++) {
                r1.getPixel(x, y, tmp1);
                r2.getPixel(x, y, tmp2);
                tmp1[0] = Math.abs(tmp1[0] - tmp2[0]);
                // System.out.println("tmp1 = " + tmp1[0]);
                if (tmp1[0] > 5) tmp1[0] = 0;
                else tmp1[0] = 255;
                r1.setPixel(x, y, tmp1);
            }
        }
        return img1;
    }

    /**
     * Converts an image (RGB, RGBA, ... whatever) to a binary one based on given threshold
     *
     * @param image     the image to convert. Remains untouched.
     * @param threshold the threshold in [0,255]
     * @return a new BufferedImage instance of TYPE_BYTE_GRAY with only 0'S and 255's
     */
    public static BufferedImage thresholdImage(BufferedImage image, int threshold) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        result.getGraphics().drawImage(image, 0, 0, null);
        WritableRaster raster = result.getRaster();
        int[] pixels = new int[image.getWidth()];
        for (int y = 0; y < image.getHeight(); y++) {
            raster.getPixels(0, y, image.getWidth(), 1, pixels);
            for (int i = 0; i < pixels.length; i++) {
                if (pixels[i] < threshold) pixels[i] = 0;
                else pixels[i] = 255;
            }
            raster.setPixels(0, y, image.getWidth(), 1, pixels);
        }
        return result;
    }

    /**
     * Check if the image is fail safe for color based features that are actually using 8 bits per pixel RGB.
     *
     * @param bufferedImage
     * @return
     */
    public static BufferedImage get8BitRGBImage(BufferedImage bufferedImage) {
        // check if it's (i) RGB and (ii) 8 bits per pixel.
        if (bufferedImage.getType() != ColorSpace.TYPE_RGB || bufferedImage.getSampleModel().getSampleSize(0) != 8) {
            BufferedImage img = new BufferedImage(bufferedImage.getWidth(), bufferedImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            img.getGraphics().drawImage(bufferedImage, 0, 0, null);
            bufferedImage = img;
        }
        return bufferedImage;
    }

}
