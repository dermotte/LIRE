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
import net.semanticmetadata.lire.clustering.Cluster;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * Date: 28.09.2010
 * Time: 12:45:27
 * Mathias Lux, mathias@juggle.at
 */
public class SerializationUtilsTest extends TestCase {

    /**
     * Test some basic serialization routines ...
     */
    public void testSerialization() {
        {
            // --- floats
            float[] test = new float[100];
            for (int i = 0; i < test.length; i++) {
                test[i] = (float) (Math.random() * 1000);
            }
            byte[] bytes = SerializationUtils.toByteArray(test);
            float[] floats = SerializationUtils.toFloatArray(bytes);

            for (int i = 0; i < floats.length; i++) {
                assertEquals(floats[i], test[i]);
            }
        }
        {
            // --- doubles

            double[] test = new double[100];
            for (int i = 0; i < test.length; i++) {
                test[i] = (Math.random() * 1000);
            }
            byte[] bytes = SerializationUtils.toByteArray(test);
            double[] floats = SerializationUtils.toDoubleArray(bytes);

            for (int i = 0; i < floats.length; i++) {
                // need to cast to floats due to the loss in precision in conversion.
                assertEquals(floats[i], test[i]);
            }
        }
    }

    public void testLongSerialization() {
        double[] test = new double[100];
        for (int i = 0; i < test.length; i++) {
            test[i] = (Math.random() * 1000);
            long l = Double.doubleToRawLongBits(test[i]);
            assertEquals(SerializationUtils.toLong(SerializationUtils.toBytes(l)), l);
        }

    }

    /**
     * Test serialization of Clusters ...
     */
    public void testClusterSerialization() throws IOException {
        Cluster[] tc = new Cluster[12];
        for (int i = 0; i < tc.length; i++) {
            double[] test = new double[128];
            for (int j = 0; j < test.length; j++) {
                test[j] = (Math.random() * 1000);
            }
            tc[i] = new Cluster(test);
        }

        Cluster.writeClusters(tc, "test-tmp.dat");

        Cluster[] clusters = Cluster.readClusters("test-tmp.dat");

        for (int i = 0; i < clusters.length; i++) {
            System.out.println(clusters[i].toString().equals(tc[i].toString()));
        }
    }

    public void testByteCompression() {
        for (int j = 0; j<10000; j++) {
            byte[] test = new byte[6];
            for (int i = 0; i < test.length; i++) {
                test[i] = (byte) Math.floor(Math.random()*16);
    //            System.out.println(i + " = " + test[i]);
            }
            int tmp = test[0];
            tmp = tmp << 5 | test[1];
            tmp = tmp << 5 | test[2];
            tmp = tmp << 5 | test[3];
            tmp = tmp << 5 | test[4];
            tmp = tmp << 5 | test[5];
            int bitmask = 0x000F;
            assertTrue((tmp & bitmask) == (int) test[5]);
            assertTrue((tmp >> 5 & bitmask) == (int) test[4]);
            assertTrue((tmp >> 10 & bitmask) == (int) test[3]);
            assertTrue((tmp >> 15 & bitmask) == (int) test[2]);
            assertTrue((tmp >> 20 & bitmask) == (int) test[1]);
            assertTrue((tmp >> 25 & bitmask) == (int) test[0]);
        }
    }

    public void testReadCodeBook() throws IOException {
        List<double[]> doubles = SerializationUtils.readCodeBook(new FileInputStream("codebookSCD128.txt"));
        for (Iterator<double[]> iterator = doubles.iterator(); iterator.hasNext(); ) {
            double[] next = iterator.next();
            //System.out.println(Arrays.toString(next));
        }
        SerializationUtils.writeCodeBook(System.out, doubles);
    }
}
