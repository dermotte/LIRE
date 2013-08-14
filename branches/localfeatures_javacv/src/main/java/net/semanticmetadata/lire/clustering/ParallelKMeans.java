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

package net.semanticmetadata.lire.clustering;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by IntelliJ IDEA.
 * User: mlux
 * Date: 12.10.11
 * Time: 12:15
 * To change this template use File | Settings | File Templates.
 */
public class ParallelKMeans extends KMeans {

    public ParallelKMeans(int numClusters) {
        super(numClusters);
    }
//
//    /**
//     * Re-shuffle all features.
//     */
//    protected void reOrganizeFeatures() {
//        int numThreads = 8;
//        int step = features.size() / numThreads;
//        LinkedList<FeatureToClass> tasks = new LinkedList<FeatureToClass>();
//        LinkedList<Thread> threads = new LinkedList<Thread>();
//        for (int i = 0; i < numThreads; i++) {
//            FeatureToClass ftc;
//            if (i + 1 < numThreads)
//                ftc = new FeatureToClass(i * step, (i + 1) * step);
//            else
//                ftc = new FeatureToClass(i * step, features.size());
//            Thread thread = new Thread(ftc);
//            thread.start();
//            tasks.add(ftc);
//            threads.add(thread);
//        }
//        for (Iterator<Thread> iterator = threads.iterator(); iterator.hasNext(); ) {
//            Thread next = iterator.next();
//            try {
//                next.join();
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
//        for (Iterator<FeatureToClass> iterator = tasks.iterator(); iterator.hasNext(); ) {
//            FeatureToClass next = iterator.next();
//            HashMap<Integer, Integer> results = next.getResults();
//            for (Iterator<Integer> threadIterator = results.keySet().iterator(); threadIterator.hasNext(); ) {
//                int key = threadIterator.next();
//                clusters[results.get(key)].members.add(key);
//            }
//        }
//    }
//
//    /**
//     * Computes the mean per cluster (averaged vector)
//     */
////    private void recomputeMeans() {
////        System.out.println("Running parallel part");
////        for (int i = 0, clustersLength = clusters.length; i < clustersLength; i++) {
////            int clusterIndex = i;
////            exec.execute(new MyRunnable(clusterIndex));
////        }
////        try {
////            if (!exec.isTerminated()) exec.awaitTermination(10, TimeUnit.MINUTES);
////        } catch (InterruptedException e) {
////            e.printStackTrace();
////        }
////    }
//    private synchronized Cluster getCluster(int index) {
//        return clusters[index];
//    }
//
//    @SuppressWarnings("unused")
//	private void recomputeMeanOfCluster(int clusterIndex) {
//        int length = features.get(0).length;
//        Cluster cluster = getCluster(clusterIndex);
//        double[] mean = cluster.centroid;
//        for (int j = 0; j < length; j++) {
//            mean[j] = 0;
//            for (Iterator<Integer> iterator = cluster.members.iterator(); iterator.hasNext(); ) {
//                Integer member = iterator.next();
//                double v = features.get(member)[j];
//                mean[j] += v;
//            }
//            if (cluster.members.size() > 1)
//                mean[j] = mean[j] / cluster.members.size();
//        }
//        double v = 0;
//        // add up to stress ...
//        for (Integer member : cluster.members) {
//            double tmpStress = 0;
//            for (int k = 0; k < length; k++) {
//                double f = Math.abs(mean[k] - features.get(member)[k]);
//                tmpStress += f;
//            }
//            v += tmpStress;
//        }
//        cluster.setStress(v);
//    }
//
//    private class FeatureToClass implements Runnable {
//        HashMap<Integer, Integer> results;
//        int start, end;
//
//        private FeatureToClass(int start, int end) {
//            this.start = start;
//            this.end = end;
//            results = new HashMap<Integer, Integer>(end - start);
//        }
//
//        public void run() {
//            for (int k = start; k < end; k++) {
//                double[] f = features.get(k);
//                int best = 0;
//                double minDistance = clusters[0].getDistance(f);
//                for (int i = 1; i < clusters.length; i++) {
//                    double v = clusters[i].getDistance(f);
//                    if (minDistance > v) {
//                        best = i;
//                        minDistance = v;
//                    }
//                }
//                results.put(k, best);
//            }
//        }
//
//        public HashMap<Integer, Integer> getResults() {
//            return results;
//        }
//    }
}
