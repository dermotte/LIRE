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

package net.semanticmetadata.lire.utils;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: mlux
 * Date: 04.10.12
 * Time: 16:50
 * To change this template use File | Settings | File Templates.
 */
public class QuantizationUtilsTest extends TestCase {
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

    public void rgbHistogramTest() throws IOException {
        int[] histogram = new int[64];
        for (int i = 0; i < histogram.length; i++) histogram[i] = 0;
        BufferedImage img = ImageIO.read(new FileInputStream("wang-1000/0.jpg"));
        WritableRaster raster = img.getRaster();
        int[] px = new int[3];
        for (int x = 0; x < raster.getWidth(); x++) {
            for (int y = 0; y < raster.getHeight(); y++) {
                raster.getPixel(x, y, px);
                int pos = (int) Math.round((double) px[2] / 85d) +
                        (int) Math.round((double) px[1] / 85d) * 4+
                        (int) Math.round((double) px[0] / 85d) * 4*4;
                histogram[pos]++;
            }
        }
    }

    @SuppressWarnings("unused")
	private int getPartition(int value) {
        return (int) Math.round((double) value / 85d);
    }

    @SuppressWarnings("unused")
	private int quant(int[] pixel) {
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
