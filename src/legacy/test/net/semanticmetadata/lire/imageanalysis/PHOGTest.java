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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 20.04.13 09:27
 */

package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;
import net.semanticmetadata.lire.utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Test class for PHOG descriptor.
 * @author Mathias Lux, mathias@juggle.at, 05.04.13
 */
public class PHOGTest extends TestCase {
    public void testExtraction() throws IOException {
        BufferedImage img = ImageIO.read(new File("munch.jpg"));
//        BufferedImage img = ImageIO.read(new File("test.jpg"));
        PHOG p = new PHOG();
        p.extract(img);
        System.out.println(Arrays.toString(p.getDoubleHistogram()));
        byte[] bytes = p.getByteArrayRepresentation();
        PHOG g = new PHOG();
        g.setByteArrayRepresentation(bytes, 0, bytes.length);
        float distance = p.getDistance(p);
        System.out.println("distance = " + distance);
    }

    public void testSerialization() throws IOException {
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("testdata/ferrari"), true);
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext(); ) {
            File next = iterator.next();
            BufferedImage image = ImageIO.read(next);
            PHOG p1 = new PHOG();
            PHOG p2 = new PHOG();

            p1.extract(image);
            System.out.println(Arrays.toString(p1.getDoubleHistogram()));
            p2.setByteArrayRepresentation(p1.getByteArrayRepresentation());
            System.out.println(Arrays.toString(p2.getDoubleHistogram()));
            assertTrue(p2.getDistance(p1) == 0);
        }
    }
}
