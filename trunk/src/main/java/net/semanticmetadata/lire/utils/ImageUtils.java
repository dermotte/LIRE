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

package net.semanticmetadata.lire.utils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

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
     * @return the scaled image, the
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
        // create smaller image
        BufferedImage img = new BufferedImage((int) (originalWidth * scaleFactor), (int) (originalHeight * scaleFactor), BufferedImage.TYPE_INT_RGB);
        // fast scale (Java 1.4 & 1.5)
        Graphics g = img.getGraphics();
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
        return img;
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
        // create smaller image
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // fast scale (Java 1.4 & 1.5)
        Graphics g = img.getGraphics();
        g.drawImage(image, 0, 0, img.getWidth(), img.getHeight(), null);
        return img;
    }

    public static BufferedImage cropImage(BufferedImage image, int fromX, int fromY, int width, int height) {
        assert (width > 0 && height > 0);
        // create smaller image
        BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        // fast scale (Java 1.4 & 1.5)
        Graphics g = img.getGraphics();
        g.drawImage(image, fromX, fromY, img.getWidth(), img.getHeight(), null);
        return img;
    }

    /**
     * Converts an image to grey. Use instead of color conversion op, which yields strange results.
     *
     * @param image
     */
    public static BufferedImage convertImageToGrey(BufferedImage image) {
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
        float v = 0;
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


}
