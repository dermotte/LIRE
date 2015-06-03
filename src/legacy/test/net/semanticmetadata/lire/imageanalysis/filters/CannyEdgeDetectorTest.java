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
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 07.04.13 16:10
 */

package net.semanticmetadata.lire.imageanalysis.filters;

import junit.framework.TestCase;
import net.semanticmetadata.lire.utils.cv.PixelClustering;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

/**
 * This file is part of LIRE, a Java library for content based image retrieval.
 *
 * @author Mathias Lux, mathias@juggle.at, 07.04.13
 */
public class CannyEdgeDetectorTest extends TestCase {
    public void testCanny() throws IOException {
        BufferedImage in = ImageIO.read(new File("flower.jpg"));
        CannyEdgeDetector ced = new CannyEdgeDetector(in, 60, 100);
        ImageIO.write(ced.filter(), "png", new File("out.png"));
    }

    public void testComic() throws IOException {
        BufferedImage in = ImageIO.read(new File("flower.jpg"));
        CannyEdgeDetector ced = new CannyEdgeDetector(in, 30, 80);
        BufferedImage filter = ced.filter();
        BufferedImage flower = PixelClustering.clusterPixels(in);

        WritableRaster raster = flower.getRaster();
        Graphics graphics = flower.getGraphics();
        graphics.setColor(Color.black);
        int[] tmp = new int[3];
        for (int x = 0; x < raster.getWidth(); x++) {
            for (int y = 0; y < raster.getHeight(); y++) {
                filter.getRaster().getPixel(x, y, tmp);
                if (tmp[0] < 10) {
                    graphics.fillOval(x, y, 3, 4);
                }
            }
        }
        ImageIO.write(filter, "png", new File("flower-canny.png"));
        ImageIO.write(flower, "png", new File("flower-comic.png"));
    }
}
