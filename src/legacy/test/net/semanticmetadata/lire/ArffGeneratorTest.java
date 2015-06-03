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

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram;
import net.semanticmetadata.lire.utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * ...
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Created: 04.05.12, 10:00
 */
public class ArffGeneratorTest extends TestCase {
    public void testGenFile() throws IOException {
        ArrayList<File> images = FileUtils.getAllImageFiles(new File("W:\\Forschung\\Intention_Test_Data\\data\\images"), true);
        // print header
        System.out.println("% generated automatically by LIRe\n" +
                "@relation content_based_features\n" +
                "\n" +
                "@attribute image_filename String");
        System.out.println("@attribute faces numeric");
        System.out.println("@attribute cedd relational");
        for (int i = 0; i < 144; i++) {
            System.out.println("\t@attribute c" + i + " numeric");
        }
        System.out.println("@end cedd");
        System.out.println("@attribute rgb_hist relational");
        for (int i = 0; i < 64; i++) {
            System.out.println("\t@attribute rh" + i + " numeric");
        }
        System.out.println("@end rgb_hist");
        System.out.println("@data");
        // print data
        for (Iterator<File> iterator = images.iterator(); iterator.hasNext(); ) {
            File nextFile = iterator.next();
            try {
                BufferedImage img = ImageIO.read(nextFile);
                // filename
                System.out.print(nextFile.getName() + ",");
                // cedd
                CEDD cedd = new CEDD();
                cedd.extract(img);
                double[] histogram = cedd.getDoubleHistogram();
                for (int i = 0; i < histogram.length; i++) {
                    System.out.print((int) histogram[i]);
                    System.out.print(',');

                }
//                System.out.println();
                // rgb histogram
                SimpleColorHistogram.DEFAULT_NUMBER_OF_BINS = 64;
                SimpleColorHistogram rgb = new SimpleColorHistogram();
                rgb.extract(img);
                histogram = rgb.getDoubleHistogram();
                for (int i = 0; i < histogram.length; i++) {
                    System.out.print((int) histogram[i]);
                    System.out.print(',');
                }
                System.out.println();
            } catch (Exception e) {
                System.out.println("-- ERROR -- " + nextFile.getName() + ", " + e.getMessage());
            }
        }
    }
}
