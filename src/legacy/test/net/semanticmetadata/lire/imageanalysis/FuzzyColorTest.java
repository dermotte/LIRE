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

/**
 * User: mlux
 * Date: 09.10.12
 * Time: 11:50
 */
public class FuzzyColorTest extends TestCase {

    public void testFuzziness() {
        int[] pixel = new int[]{40, 120, 230};

        int[] bins = new int[8];
        double[] amount = new double[8];

        for (int i = 0; i < pixel.length; i++) {
            double[] bt = new double[4];
            bt[0] = 0;
            bt[1] = 0;
            bt[2] = 0;
            bt[3] = 0;
            double val = pixel[i];
            if (val <= 30) bt[0] = 1;
            else if (val >= 55 && val <= 115) bt[1] = 1;
            else if (val >= 140 && val <= 200) bt[2] = 1;
            else if (val >= 225) bt[3] = 1;
            else { // its fuzzy ...
                if (val > 30 && val < 55) {
                    bt[1] = (val - 30d) / 25;
                    bt[0] = 1 - bt[1];
                } else if (val > 115 && val < 140) {
                    bt[2] = (val - 115d) / 25;
                    bt[1] = 1 - bt[2];
                } else if (val > 200 && val < 225) {
                    bt[3] = (val - 115d) / 25;
                    bt[2] = 1 - bt[3];
                }
            }
            for (int j = 0; j < bt.length; j++) {
                if (bt[j] > 0) System.out.printf("%d: %1.2f \t", j, bt[j]);
            }
            System.out.println();
        }

    }


}
