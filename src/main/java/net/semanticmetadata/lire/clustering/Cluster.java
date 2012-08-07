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
    float[] mean;
    HashSet<Integer> members = new HashSet<Integer>();

    private double stress = 0;

    public Cluster() {
        this.mean = new float[4 * 4 * 8];
        Arrays.fill(mean, 0f);
    }

    public Cluster(float[] mean) {
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
        return getDistance(f.descriptor);
    }

    public double getDistance(float[] f) {
        double d = 0;
        // now using L1 for faster results ...
        for (int i = 0; i < f.length; i++) {
            d += Math.abs(mean[i] - f[i]);
        }
        return d;
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
        mean = SerializationUtils.toFloatArray(data);
    }

    public static void writeClusters(Cluster[] clusters, String file) throws IOException {
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(SerializationUtils.toBytes(clusters.length));
        for (int i = 0; i < clusters.length; i++) {
            fout.write(clusters[i].getByteRepresentation());
        }
        fout.close();
    }

    public static Cluster[] readClusters(String file) throws IOException {
        FileInputStream fin = new FileInputStream(file);
        byte[] tmp = new byte[4];
        fin.read(tmp, 0, 4);
        Cluster[] result = new Cluster[SerializationUtils.toInt(tmp)];
        tmp = new byte[128 * 4];
        for (int i = 0; i < result.length; i++) {
            int bytesRead = fin.read(tmp, 0, 128 * 4);
            if (bytesRead != 128 * 4) System.err.println("Didn't read enough bytes ...");
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
}
