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

package net.semanticmetadata.lire.imageanalysis.mser;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

/**
 * Created by IntelliJ IDEA.
 * User: Shotty
 * Date: 28.06.2010
 * Time: 10:09:59
 */
public class ImageMask {
    protected static int NO_ACCESS = 0;
    protected static int ACCESS = 1;
    protected static int VISITED = 2;

    int[] pixels;
    int[] accessible;
    BufferedImage image;

    public ImageMask(BufferedImage image) {
        this.image = image;
        int[] pixels = new int[image.getHeight() * image.getWidth()];

        Raster ip = image.getRaster();
        int[] p = new int[3];
        // fill all the pixels into the int-Array
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                // get the value of the Pixel
                pixels[getIndex(x, y)] = ip.getPixel(x, y, p)[0];
            }
        }

        this.pixels = pixels;
        accessible = new int[pixels.length];
        for (int i = 0; i < accessible.length; i++) {
            accessible[i] = NO_ACCESS;
        }
    }

    public boolean hasAccess(int idx) {
        return accessible[idx] == ACCESS;
    }

    /**
     * Set pixel accessible and give back if this was possible
     *
     * @param idx index od the pixel
     * @return the grey value of the pixel
     */
    public boolean getAccess(int idx) {
        if (idx < accessible.length &&
                accessible[idx] == NO_ACCESS) {
            accessible[idx] = ACCESS;
            return true;
        }
        return false;
    }

    public int getValue(int idx) {
        return pixels[idx];
    }

    public int getX(int idx) {
        return getX(idx, image.getWidth());
    }

    public int getY(int idx) {
        return getY(idx, image.getWidth());
    }

    public int getIndex(int x, int y) {
        return getIndex(x, y, image.getWidth());
    }

    public ImagePoint getImagePoint(int index) {
        return new ImagePoint(index, getX(index), getY(index));
    }

    public BoundaryPixel getBoundaryPixel(int index) {
        return new BoundaryPixel(getImagePoint(index), image.getWidth(), image.getHeight());
    }


    public static int getX(int idx, int imageWidth) {
        return idx % imageWidth;
    }

    public static int getY(int idx, int imageWidth) {
        return idx / imageWidth;
    }

    public static int getIndex(int x, int y, int imageWidth) {
        return (y * imageWidth) + x;
    }
}
