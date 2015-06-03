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

import net.semanticmetadata.lire.benchmarking.TestGeneral;
import net.semanticmetadata.lire.imageanalysis.utils.ColorConversion;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by mlux on 25.02.2015.
 */
public class HistogramTest extends TestGeneral {
    public void testCountHueValues() throws IOException {
        int[] histogram = new int[360];
        int[] hsv = new int[3];
        ArrayList<String> images = FileUtils.getAllImages(new File("testdata/ferrari"), true);
        for (Iterator<String> iterator = images.iterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            BufferedImage image = ImageIO.read(new FileInputStream(s));
            WritableRaster raster = image.getRaster();
            int[] pixel = new int[3];
            for (int i = 0; i < raster.getWidth(); i++) {
                for (int j = 0; j < raster.getHeight(); j++) {
                    raster.getPixel(i, j, pixel);
                    ColorConversion.rgb2hsv(pixel[0],pixel[1],pixel[2], hsv);
                    // check for color hue.
                    if (hsv[1] > 15 && hsv[2] > 15)
                        histogram[hsv[0]]++;
                }
            }
        }

        for (int i = 0; i < histogram.length; i++) {
            System.out.printf("%d\t%d\n", i, histogram[i]);
        }
    }
}
