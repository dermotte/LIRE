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
 * Updated: 20.04.13 09:05
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

public class EdgeHistogramTest extends TestCase {

    public void testSingleFile() throws IOException {
        EdgeHistogram e = new EdgeHistogram();
        e.extract(ImageIO.read(new File("wipo_us_ken.jpg")));
        System.out.println(Arrays.toString(e.getDoubleHistogram()));
        System.out.println(Arrays.toString(e.getByteArrayRepresentation()));

        e.extract(ImageIO.read(new File("wipo_us_fita.jpg")));
        System.out.println(Arrays.toString(e.getDoubleHistogram()));
        System.out.println(Arrays.toString(e.getByteArrayRepresentation()));
    }

    public void testExtraction() throws IOException {
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("testdata/ferrari"), true);
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext(); ) {
            File next = iterator.next();
            BufferedImage image = ImageIO.read(next);
            EdgeHistogram eh1 = new EdgeHistogram();
            EdgeHistogram eh2 = new EdgeHistogram();

            eh1.extract(image);
            System.out.println(" = " + eh1.getStringRepresentation());
            eh2.setByteArrayRepresentation(eh1.getByteArrayRepresentation());
            assertTrue(eh2.getDistance(eh1) == 0);
        }
    }

    public void testSerializationAndReUse() throws IOException, IllegalAccessException, InstantiationException {
        LireFeature f = new EdgeHistogram();
        String[] testFiles = new String[]{"D:\\DataSets\\WIPO-CA\\converted-0\\1001557.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1001714.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1001816.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1001651.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1002071.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1001809.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1001627.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1001611.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1001855.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1002011.png"};
        for (String testFile : testFiles) {
//            f=new EdgeHistogram();
            f.extract(ImageIO.read(new File(testFile)));
            LireFeature f2 = new EdgeHistogram();
//            f2.setByteArrayRepresentation(f.getByteArrayRepresentation(), 0, f.getByteArrayRepresentation().length);
            f2.extract(ImageIO.read(new File(testFile)));
//            f2.getByteArrayRepresentation();
            System.out.println(testFile);
            System.out.println(Arrays.toString(f.getDoubleHistogram()).replaceAll("\\.0,",""));
            System.out.println(Arrays.toString(f2.getDoubleHistogram()).replaceAll("\\.0,", ""));
            System.out.println(f2.getDistance(f));
//            assertEquals(f2.getDistance(f), 0d, 0.000000001);
        }
    }
}
