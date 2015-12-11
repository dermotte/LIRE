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
package net.semanticmetadata.lire.classifiers;

import net.semanticmetadata.lire.utils.StatsUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Mathias on 23/09/2008.
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * @author Lazaros Tsochatzidis, ltsochat@ee.duth.gr
 */
public class KMeans {
    protected int countAllFeatures = 0, numClusters = 512;
    protected int length;
    protected ArrayList<double[]> features = new ArrayList<double[]>();
    protected Cluster[] clusters = null;
//    protected HashMap<double[], Integer> featureIndex = null;


    public KMeans(int numClusters) {
        this.numClusters = numClusters;
    }

    public void addFeature(double[] feature) {
        if (!hasNaNs(feature)){
            features.add(feature);
            countAllFeatures++;
        }
    }

    public void init() {
        if (features.size() < numClusters *2) {
            System.err.println("WARNING: Please note that the number of local features, in this case " + features.size() + ", is" +
                    "smaller than the recommended minimum number, which is two times the number of visual words, in your case 2*" + numClusters +
                    ". Please adapt your data and either use images with more local features or more images for creating the visual vocabulary.");
        }
        if (features.size() < numClusters + 1) {
            System.err.println("CRITICAL: The number of features is smaller than the number of clusters. This cannot work as there has to be at least one " +
                    "feature per cluster. Aborting process now.");
            System.out.println("features: " + features.size());
            System.out.println("clusters: " + numClusters);
            System.exit(1);
        }
        // find first clusters:
        clusters = new Cluster[numClusters];
        Set<Integer> medians = selectInitialMedians(numClusters);
        assert(medians.size() == numClusters); // this has to be the same ...
        Iterator<Integer> mediansIterator = medians.iterator();
        double[] descriptor;
        for (int i = 0; i < clusters.length; i++) {
            descriptor = features.get(mediansIterator.next());
            clusters[i] = new Cluster(new double[descriptor.length]);
            System.arraycopy(descriptor, 0, clusters[i].mean, 0, descriptor.length);
        }
        length = features.get(0).length;
    }

    protected Set<Integer> selectInitialMedians(int numClusters) {
        return StatsUtils.drawSample(numClusters, features.size());
    }

    /**
     * Do one step and return the overall stress (squared error). You should do this until
     * the error is below a threshold or doesn't change a lot in between two subsequent steps.
     *
     * @return
     */
    public double clusteringStep() {
        for (Cluster cluster : clusters) {
            cluster.reset();
        }
        reOrganizeFeatures();
        recomputeMeans();
        return overallStress();
    }

    protected boolean hasNaNs(double[] histogram) {
        boolean hasNaNs = false;
        for (double next : histogram) {
            if (Double.isNaN(next)) {
                hasNaNs = true;
                break;
            }
        }
        if (hasNaNs) {
            System.err.println("Found a NaN in init");
//            System.out.println("image.identifier = " + image.identifier);
            for (double v : histogram) {
                System.out.print(v + ", ");
            }
            System.out.println("");
        }
        return hasNaNs;
    }

    /**
     * Re-shuffle all features.
     */
    protected void reOrganizeFeatures() {
        double[] f;
        Cluster best;
        double v, minDistance;
        for (int k = 0; k < features.size(); k++) {
            f = features.get(k);
            best = clusters[0];
            minDistance = clusters[0].getDistance(f);
            for (int i = 1; i < clusters.length; i++) {
                v = clusters[i].getDistance(f);
                if (minDistance > v) {
                    best = clusters[i];
                    minDistance = v;
                }
            }
            best.assignMember(f);
        }
    }

    /**
     * Computes the mean per cluster (averaged vector)
     */
    protected void recomputeMeans() {
        int length = features.get(0).length;
        Cluster cluster;
        double[] mean;
        for (int i = 0; i < clusters.length; i++) {
            cluster = clusters[i];
            if (cluster.getSize() == 1) {
                System.err.println("** There is just one member in cluster " + i);
            } else if (cluster.getSize() < 1) {
                System.err.println("** There is NO member in cluster " + i);
                // fill it with a random member?!?
                int index = (int) Math.floor(Math.random()*features.size());
                clusters[i].assignMember(features.get(index));
            }
            cluster.move();
        }
    }

    /**
     * Squared error in classification.
     *
     * @return
     */
    protected double overallStress() {
        double v = 0;
        for (Cluster cluster : clusters) {
            v+=cluster.getStress();
        }

        return v;
    }

    /**
     * Get the number of desired clusters.
     *
     * @return
     */
    public int getNumClusters() {
        return numClusters;
    }

    public int getFeatureCount() {
        return countAllFeatures;
    }

    public Cluster[] getClusters() {
        return clusters;
    }

//    public void setNumClusters(int numClusters) {
//        this.numClusters = numClusters;
//    }
//
//    private HashMap<double[], Integer> createIndex() {
//        featureIndex1 = new HashMap<double[], Integer>(features1.size());
//        for (int i = 0; i < clusters1.length; i++) {
//            Cluster cluster = clusters1[i];
//            for (Iterator<Integer> fidit = cluster.members.iterator(); fidit.hasNext(); ) {
//                int fid = fidit.next();
//                featureIndex1.put(features1.get(fid), i);
//            }
//        }
//        return featureIndex1;
//    }
//
//    /**
//     * Used to find the cluster of a feature actually used in the clustering process (so
//     * it is known by the k-means class).
//     *
//     * @param f the feature to search for
//     * @return the index of the Cluster
//     */
//    public int getClusterOfFeature(FeatureVector f) {
//        if (featureIndex1 == null) createIndex();
//        return featureIndex1.get(f);
//    }
}