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

package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

public class GaborTest extends TestCase {
    private String[] testFiles = new String[]{"img01.jpg", "img02.jpg", "img03.jpg", "img04.jpg", "img05.jpg", "img06.jpg", "img07.jpg", "img08.jpg", "img09.jpg", "img10.jpg"};
    private String testFilesPath = "./lire/src/test/resources/small/";

    public void testExtraction() throws IOException {
        for (String file : testFiles) {
            Gabor sch = new Gabor();
            BufferedImage image = ImageIO.read(new FileInputStream(testFilesPath + file));
            System.out.println("image = " + image.getWidth() + " x " + image.getHeight());
            sch.extract(image);
            System.out.println("sch = " + sch.getStringRepresentation());
        }
    }

    public void testRetrieval() throws Exception {
        Gabor[] acc = new Gabor[testFiles.length];
        LinkedList<String> vds = new LinkedList<String>();
        for (int i = 0; i < acc.length; i++) {
            System.out.println("Extracting from number " + i);
            acc[i] = new Gabor();
            acc[i].extract(ImageIO.read(new FileInputStream(testFilesPath + testFiles[i])));
            vds.add(acc[i].getStringRepresentation());
        }

        System.out.println("Calculating distance for " + testFiles[5]);
        for (int i = 0; i < acc.length; i++) {
            float distance = acc[i].getDistance(acc[5]);
            System.out.println(testFiles[i] + " distance = " + distance);
        }
        int count = 0;
        for (Iterator<String> iterator = vds.iterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            Gabor a = new Gabor();
            a.setStringRepresentation(s);
            float distance = acc[count].getDistance(a);
            System.out.println(testFiles[count] + " distance = " + distance);
            count++;
        }
    }


}