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

package net.semanticmetadata.lire.utils.cv;

import net.semanticmetadata.lire.utils.MetricsUtils;

import java.util.*;

/**
 * Simple, re-usable and straight k-means implementation based on double[] feature vectors and L2 distance.
 * User: mlux
 * Date: 20.09.13
 * Time: 10:56
 */
public class KMeans {
    List<double[]> features;
    Cluster[] clusters;

    public KMeans(List<double[]> featureList, int numberOfClusters) {
        features = new ArrayList<double[]>(featureList);
        clusters = new Cluster[numberOfClusters];

        HashSet<double[]> means = new HashSet<double[]>();
        while (means.size() < Math.min(numberOfClusters, featureList.size() / 2)) {
            double[] e = features.get((int) Math.floor(Math.random() * features.size()));
            double[] tmp = new double[e.length];
            System.arraycopy(e, 0, tmp, 0, e.length);
            means.add(tmp);
        }
        // init cluster centers.
        Iterator<double[]> iterator = means.iterator();
        for (int i = 0; i < clusters.length; i++) {
            clusters[i] = new Cluster(iterator.next());
        }
    }

    public double step() {
        // init clusters:
        for (int i = 0; i < clusters.length; i++) {
            clusters[i].clearMembers();
            assert (clusters[i].members.size() == 0);
        }
        // assign to new clusters:
        for (int id = 0; id < features.size(); id++) {
            double tmpDistance = Double.MAX_VALUE;
            int currentCluster = -1;
            for (int i = 0; i < clusters.length; i++) {
                double distance = clusters[i].getDistance(features.get(id));
                assert (distance >= 0);
                if (distance < tmpDistance) {
                    tmpDistance = distance;
                    currentCluster = i;
                }
            }
            clusters[currentCluster].addMember(id);
        }
        // recompute means:
        for (int i = 0; i < clusters.length; i++) {
            clusters[i].recomputeMeans(features);
//            System.out.println(Arrays.toString(clusters[i].center));
        }
        // calculate stress
        double stress = 0d;
        double num = 0;
        for (int i = 0; i < clusters.length; i++) {
            stress += clusters[i].calculateStress(features);
            num += clusters[i].members.size();
        }
        return stress;
    }

    public List<double[]> getMeans() {
        ArrayList<double[]> r = new ArrayList<double[]>(clusters.length);
        for (int i = 0; i < clusters.length; i++) {
            r.add(clusters[i].center);
        }
        return r;
    }

    /**
     * Cluster implementation used in this k-means implementation.
     */
    class Cluster {
        double[] center;
        HashSet<Integer> members;

        public Cluster(double[] center) {
            this.center = center;
            members = new HashSet<Integer>();
        }

        public double getDistance(double[] feature) {
            return MetricsUtils.distL2(center, feature);
        }

        public void clearMembers() {
            members.clear();
        }

        public void addMember(int id) {
            members.add(id);
        }

        public void recomputeMeans(List<double[]> features) {
            if (members.size() > 0) {
                Arrays.fill(center, 0d);
                for (Iterator<Integer> iterator = members.iterator(); iterator.hasNext(); ) {
                    int member = iterator.next();
                    double[] feature = features.get(member);
                    for (int i = 0; i < feature.length; i++) {
//                        assert (feature[i] < 256);
                        center[i] += feature[i];
                    }
                }
                for (int i = 0; i < center.length; i++) {
                    center[i] = center[i] / ((double) members.size());
                }
            }
        }

        public double calculateStress(List<double[]> features) {
            double result = 0d;
            for (Iterator<Integer> iterator = members.iterator(); iterator.hasNext(); ) {
                int member = iterator.next();
                double[] feature = features.get(member);
                result += MetricsUtils.distL2(center, feature);
            }
            return result;
        }
    }


}
