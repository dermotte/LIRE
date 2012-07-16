/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.utils;

import junit.framework.TestCase;
import net.semanticmetadata.lire.clustering.Cluster;

import java.io.IOException;

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
            float[] test = new float[128];
            for (int j = 0; j < test.length; j++) {
                test[j] = (float) (Math.random() * 1000);
            }
            tc[i] = new Cluster(test);
        }

        Cluster.writeClusters(tc, "test-tmp.dat");

        Cluster[] clusters = Cluster.readClusters("test-tmp.dat");

        for (int i = 0; i < clusters.length; i++) {
            System.out.println(clusters[i].toString().equals(tc[i].toString()));
        }
    }
}
