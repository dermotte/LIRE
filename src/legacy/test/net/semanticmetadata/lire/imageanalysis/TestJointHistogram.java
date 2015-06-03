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
 * Updated: 01.07.13 16:15
 */

package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.joint.JointHistogram;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * User: mlux
 * Date: 04.12.12
 * Time: 11:01
 */
public class TestJointHistogram extends TestCase {
    public void testExtract() throws IOException {
        BufferedImage img = ImageIO.read(new FileInputStream("C:\\Java\\Projects\\LireSVN\\testdata\\ferrari\\black\\2828686873_2fa36f83d7_b.jpg"));
        JointHistogram jh = new JointHistogram();
        jh.extract(img);
        System.out.println(Arrays.toString(jh.getDoubleHistogram()));

        JointHistogram jh2 = new JointHistogram();
        jh2.setByteArrayRepresentation(jh.getByteArrayRepresentation());

        System.out.println(jh2.getDistance(jh));

    }

    public void testJsd() {
        int i = 0;
        float[] h1 = new float[]{1f};
        float[] h2 = new float[]{6f};
        float result = (float) ((h1[i] > 0 ? (h1[i]) * Math.log((2f * h1[i]) / (h1[i] + h2[i])) : 0) +
                (h2[i] > 0 ? (h2[i]) * Math.log((2f * h2[i]) / (h1[i] + h2[i])) : 0));
        System.out.println("result = " + result);
        System.out.println(h1[i] > 0 ? "1" : "0");

        System.out.println((true ? 1 : 0) + 1);
    }

}
