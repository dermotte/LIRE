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

/*
 * This file is part of the LIRE project: http://www.SemanticMetadata.net/lire.
 *
 * Lire is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Lire is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lire; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2008-2010 by Mathias Lux, mathias@juggle.at
 */
package net.semanticmetadata.lire.clustering;

import net.semanticmetadata.lire.imageanalysis.Histogram;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

/**
 * Provides a simple implementation for a cluster used with the visual bag of words approach.
 * User: Mathias Lux, mathias@juggle.at
 * Date: 26.03.2010
 * Time: 12:10:19
 */
public class Cluster implements Comparable<Object> {
    double[] mean;
    HashSet<Integer> members = new HashSet<Integer>();

    private double stress = 0;

    public Cluster() {
        this.mean = new double[4 * 4 * 8];
        Arrays.fill(mean, 0f);
    }

    public Cluster(double[] mean) {
        this.mean = mean;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        for (Integer integer : members) {
            sb.append(integer);
            sb.append(", ");
        }
        for (int i = 0; i < mean.length; i++) {
            sb.append(mean[i]);
            sb.append(';');
        }
        return sb.toString();
    }

    public int compareTo(Object o) {
        return ((Cluster) o).members.size() - members.size();
    }

    public double getDistance(Histogram f) {
        return getDistance(f.getDoubleHistogram());
    }

    public double getDistance(double[] f) {
//        L1
//        return MetricsUtils.distL1(mean, f);

//        L2
        return MetricsUtils.distL2(mean, f);
    }

    /**
     * Creates a byte array representation from the clusters mean.
     *
     * @return the clusters mean as byte array.
     */
    public byte[] getByteRepresentation() {
        return SerializationUtils.toByteArray(mean);
    }

    public void setByteRepresentation(byte[] data) {
        mean = SerializationUtils.toDoubleArray(data);
    }

    public static void writeClusters(Cluster[] clusters, String file) throws IOException {
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(SerializationUtils.toBytes(clusters.length));
        fout.write(SerializationUtils.toBytes((clusters[0].getMean()).length));
        for (int i = 0; i < clusters.length; i++) {
            fout.write(clusters[i].getByteRepresentation());
        }
        fout.close();
    }

    // TODO: re-visit here to make the length variable (depending on the actual feature size).
    public static Cluster[] readClusters(String file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        byte[] tmp = new byte[4];
        fin.read(tmp, 0, 4);
        Cluster[] result = new Cluster[SerializationUtils.toInt(tmp)];
        fin.read(tmp, 0, 4);
        int size = SerializationUtils.toInt(tmp);
        tmp = new byte[size * 8];
        for (int i = 0; i < result.length; i++) {
            int bytesRead = fin.read(tmp, 0, size * 8);
            if (bytesRead != size * 8) System.err.println("Didn't read enough bytes ...");
            result[i] = new Cluster();
            result[i].setByteRepresentation(tmp);
        }
        fin.close();
        return result;
    }

    public double getStress() {
        return stress;
    }

    public void setStress(double stress) {
        this.stress = stress;
    }

    public HashSet<Integer> getMembers() {
        return members;
    }

    public void setMembers(HashSet<Integer> members) {
        this.members = members;
    }

    /**
     * Returns the cluster mean
     *
     * @return the cluster mean vector
     */
    public double[] getMean() {
        return mean;
    }
}
