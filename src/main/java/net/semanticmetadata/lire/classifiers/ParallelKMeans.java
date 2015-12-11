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

import net.semanticmetadata.lire.builders.DocumentBuilder;

import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Created by Mathias on 12/10/11.
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * @author Lazaros Tsochatzidis, ltsochat@ee.duth.gr
 */
public class ParallelKMeans extends KMeans {
    int numThreads = DocumentBuilder.NUM_OF_THREADS;
    private LinkedBlockingQueue<Item> queue = new LinkedBlockingQueue<Item>(100);

    public ParallelKMeans(int numClusters) {
        super(numClusters);
    }

    /**
     * Re-shuffle all features.
     */
    protected void reOrganizeFeatures() {
        LinkedList<Thread> threads = new LinkedList<Thread>();
        Thread thread;
        Thread p = new Thread(new ProducerForFeatures());
        p.start();
        for (int i = 0; i < numThreads; i++) {
            thread = new Thread(new FeatureToClass());
            thread.start();
            threads.add(thread);
        }
        for (Thread next : threads) {
            try {
                next.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        threads.clear();
    }

    protected void recomputeMeans() {
        LinkedList<Thread> threads = new LinkedList<Thread>();
        Thread p = new Thread(new ProducerForClusters());
        p.start();
        Thread thread;
        for (int i = 0; i < numThreads; i++) {
            thread = new Thread(new MeanOfCluster());
            thread.start();
            threads.add(thread);
        }
        for (Thread next : threads) {
            try {
                next.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        threads.clear();
    }

    protected double overallStress() {
        double v = 0.0;
        LinkedList<ComputeStress> tasks = new LinkedList<ComputeStress>();
        LinkedList<Thread> threads = new LinkedList<Thread>();
        ComputeStress computeStress;
        Thread thread;
        Thread p = new Thread(new ProducerForClusters());
        p.start();
        for (int i = 0; i < numThreads; i++) {
            computeStress = new ComputeStress();
            thread = new Thread(computeStress);
            thread.start();
            tasks.add(computeStress);
            threads.add(thread);
        }
        for (Thread next : threads) {
            try {
                next.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (ComputeStress task : tasks) {
            v += task.getResult();
        }
        tasks.clear();
        threads.clear();
//        for(Cluster cluster : clusters){
//            v += cluster.getStress();
//        }

        return v;
    }


    private class ComputeStress implements Runnable {
        private boolean locallyEnded;
        double result;

        private ComputeStress() {
            this.result  = 0.0;
            this.locallyEnded = false;
        }

        public void run() {
            Item tmp;
            while (!locallyEnded) {
                try {
                    tmp = queue.take();
                    if (tmp.getNum() == -1)  locallyEnded = true;
                    if (!locallyEnded) {    // && tmp != -1
//                        for (Integer member : tmp.getCluster().members) {
//                            for (int j = 0; j < length; j++) {
//                                result += Math.abs(tmp.getCluster().mean[j] - features.get(member)[j]);
//                            }
//                        }
                        result += tmp.getCluster().getStress();
                    }
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
        }

        public double getResult() {
            return result;
        }
    }


    private class MeanOfCluster implements Runnable {
        private boolean locallyEnded;

        private MeanOfCluster() {
            this.locallyEnded = false;
        }

        public void run() {
            Cluster cluster;
            double[] mean, f;
            Item tmp;
            double size, stress;
            while (!locallyEnded) {
                try {
                    tmp = queue.take();
                    if (tmp.getNum() == -1) locallyEnded = true;
                    if (!locallyEnded) {    // && tmp != -1
                        cluster = tmp.getCluster();
                        if (cluster.getSize() == 1) {
                            System.err.println("** There is just one member in cluster " + tmp.getNum());
                        } else if (cluster.getSize() < 1) {
                            System.err.println("** There is NO member in cluster " + tmp.getNum());
                            // fill it with a random member?!?
                            cluster.assignMember(features.get((int) Math.floor(Math.random() * features.size())));
                        }
                        cluster.move();
                    }
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
        }
    }

    private class FeatureToClass implements Runnable {
        private boolean locallyEnded;

        private FeatureToClass() {
            this.locallyEnded = false;
        }

        public void run() {
            double v, minDistance;
            double[] f;
            Item tmp;
            int best;
            while (!locallyEnded) {
                try {
                    tmp = queue.take();
                    if (tmp.getNum() == -1) locallyEnded = true;
                    if (!locallyEnded) {    // && tmp != -1
                        f = tmp.getArray();
                        best = 0;
                        minDistance = clusters[0].getDistance(f);
                        for (int i = 1; i < clusters.length; i++) {
                            v = clusters[i].getDistance(f);
                            if (minDistance > v) {
                                best = i;
                                minDistance = v;
                            }
                        }
                        clusters[best].assignMember(f);
                    }
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
        }
    }


    class ProducerForClusters implements Runnable {
        private ProducerForClusters() {
            queue.clear();
        }

        public void run() {
            int counter = 0;
            for(Cluster cluster : clusters){
                try {
                    queue.put(new Item(counter, cluster));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter++;
            }
            Cluster cluster = null;
            for (int i = 0; i < numThreads * 3; i++)  {
                try {
                    queue.put(new Item(-1, cluster));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class ProducerForFeatures implements Runnable {
        private ProducerForFeatures() {
            queue.clear();
        }

        public void run() {
            int counter = 0;
            for (double[] feature : features) {
                try {
                    queue.put(new Item(counter, feature));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                counter++;
            }
            double[] tmp = null;
            for (int i = 0; i < numThreads * 3; i++)  {
                try {
                    queue.put(new Item(-1, tmp));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Item {
        private double[] array;
        private Cluster cluster;
        private int num;

        Item(int num, double[] array) {
            this.num = num;
            this.array = array;
        }

        Item(int num, Cluster cluster) {
            this.num = num;
            this.cluster = cluster;
        }

        private int getNum() { return num; }

        private Cluster getCluster() { return cluster; }

        private double[] getArray() { return array; }
    }

}