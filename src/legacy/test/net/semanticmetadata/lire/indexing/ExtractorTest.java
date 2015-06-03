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
 * Updated: 04.05.13 11:18
 */

package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.indexing.tools.Extractor;
import net.semanticmetadata.lire.utils.SerializationUtils;

import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: mlux
 * Date: 12.03.13
 * Time: 13:21
 */
public class ExtractorTest extends TestCase {
    public void testExtraction() {
        Extractor e = new Extractor();
        e.setFileList(new File("imageList.txt"));
        e.setOutFile(new File("out.data"));
        e.addFeature(new CEDD());
        e.run();

        // do it ...
        byte[] tempInt = new byte[4];
        int tmp, tmpFeature;
        byte[] temp = new byte[2064];
        File inputFile = new File("out.data");
        try {
            BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(new FileInputStream("out.data")));
            // read file hashFunctionsFileName length:
            while (in.read(tempInt, 0, 4) > 0) {
                tmp = SerializationUtils.toInt(tempInt);
                // read file hashFunctionsFileName:
                in.read(temp, 0, tmp);
                String filename = new String(temp, 0, tmp);
                // normalize Filename to full path.
                filename = inputFile.getCanonicalPath().substring(0, inputFile.getCanonicalPath().lastIndexOf(inputFile.getName())) + filename;
                System.out.print(filename);
                while ((tmpFeature = in.read()) < 255) {
                    System.out.print(", " + tmpFeature);
                    LireFeature f = (LireFeature) Class.forName(Extractor.features[tmpFeature]).newInstance();
                    // byte[] length ...
                    in.read(tempInt, 0, 4);
                    tmp = SerializationUtils.toInt(tempInt);
                    // read feature byte[]
                    in.read(temp, 0, tmp);
                    f.setByteArrayRepresentation(temp, 0, tmp);
                    // test f ...
                    LireFeature f2 = (LireFeature) Class.forName(Extractor.features[tmpFeature]).newInstance();
                    f2.extract(ImageIO.read(new File(filename)));
                    System.out.println("f2.getDistance(f) = " + f2.getDistance(f));
                }
                System.out.println();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
