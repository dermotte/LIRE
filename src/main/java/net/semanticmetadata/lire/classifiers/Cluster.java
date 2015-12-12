/*
 * This file is part of the LIRE project: http://lire-project.net
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
 * This file is part of the LIRE project: http://lire-project.net.
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
package net.semanticmetadata.lire.classifiers;

import net.semanticmetadata.lire.imageanalysis.features.FeatureVector;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Provides a simple implementation for a cluster used with the visual bag of words approach.
 * Date: 26.03.2010
 * Time: 12:10:19
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * @author Lazaros Tsochatzidis, ltsochat@ee.duth.gr
 */
public class Cluster implements Comparable<Object> {
    double[] mean;

    AtomicInteger size;
    AtomicDouble[] newmean;

    private double stress = 0.0;

    public Cluster() {
        this.mean = new double[4 * 4 * 8];
        Arrays.fill(mean, 0d);
        size=new AtomicInteger(0);
        newmean=new AtomicDouble[mean.length];
        for (int i=0;i<mean.length;i++) newmean[i]=new AtomicDouble(0);
    }

    public Cluster(double[] mean) {
        this.mean = mean;
        size=new AtomicInteger(0);
        newmean=new AtomicDouble[mean.length];
        for (int i=0;i<mean.length;i++) newmean[i]=new AtomicDouble(0);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        for (double next : mean) {
            sb.append(next);
            sb.append(';');
        }
        return sb.toString();
    }

    public int compareTo(Object o) {
        return ((Cluster) o).getSize() - getSize();
    }

    public double getDistance(FeatureVector f) {
        return getDistance(f.getFeatureVector());
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

    public double getStress() {
        return stress;
    }

    public void setStress(double stress) {
        this.stress = stress;
    }

    public int getSize() {
        return size.get();
    }

    public void reset() {
        size.set(0);
        for (AtomicDouble ad : newmean) ad.set(0);
    }

    public void assignMember(double[] feat) {
        size.addAndGet(1);
        int i=0;
        for (AtomicDouble ad : newmean) ad.addAndGet(feat[i++]);
    }

    public void move() {
        double lsize=size.get();
        stress=0d;
        double diff;
        for (int i=0;i<mean.length;i++){
            diff=mean[i]-newmean[i].divideAndGet(lsize);
            stress+=lsize*diff*diff;
            mean[i]=newmean[i].get();
        }
    }

    /**
     * Returns the cluster mean
     *
     * @return the cluster mean vector
     */
    public double[] getMean() {
        return mean;
    }


    public static void writeClusters(Cluster[] clusters, String path) throws IOException {
        File file = new File(path);
        if(file.exists()) {
            System.out.println("File " + path + " already exists and will be overwritten!!");
        }
        FileOutputStream fout = new FileOutputStream(file);
        fout.write(SerializationUtils.toBytes(clusters.length));
        fout.write(SerializationUtils.toBytes((clusters[0].getMean()).length));
        for (Cluster cluster : clusters) {
            fout.write(cluster.getByteRepresentation());
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
        int bytesRead;
        for (int i = 0; i < result.length; i++) {
            bytesRead = fin.read(tmp, 0, size * 8);
            if (bytesRead != size * 8) System.err.println("Didn't read enough bytes ...");
            result[i] = new Cluster();
            result[i].setByteRepresentation(tmp);
        }
        fin.close();
        return result;
    }
}
