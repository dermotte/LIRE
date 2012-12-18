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

    /**
     * Re-shuffle all features.
     */
    protected void reOrganizeFeatures() {
        int numThreads = 4;
        int step = features.size() / numThreads;
        LinkedList<FeatureToClass> tasks = new LinkedList<FeatureToClass>();
        LinkedList<Thread> threads = new LinkedList<Thread>();
        for (int i = 0; i < numThreads; i++) {
            FeatureToClass ftc;
            if (i + 1 < numThreads)
                ftc = new FeatureToClass(i * step, (i + 1) * step);
            else
                ftc = new FeatureToClass(i * step, features.size());
            Thread thread = new Thread(ftc);
            thread.start();
            tasks.add(ftc);
            threads.add(thread);
        }
        for (Iterator<Thread> iterator = threads.iterator(); iterator.hasNext(); ) {
            Thread next = iterator.next();
            try {
                next.join();
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        for (Iterator<FeatureToClass> iterator = tasks.iterator(); iterator.hasNext(); ) {
            FeatureToClass next = iterator.next();
            HashMap<Integer, Integer> results = next.getResults();
            for (Iterator<Integer> threadIterator = results.keySet().iterator(); threadIterator.hasNext(); ) {
                int key = threadIterator.next();
                clusters[results.get(key)].members.add(key);
            }
        }
    }

    /**
     * Computes the mean per cluster (averaged vector)
     */
//    private void recomputeMeans() {
//        System.out.println("Running parallel part");
//        for (int i = 0, clustersLength = clusters.length; i < clustersLength; i++) {
//            int clusterIndex = i;
//            exec.execute(new MyRunnable(clusterIndex));
//        }
//        try {
//            if (!exec.isTerminated()) exec.awaitTermination(10, TimeUnit.MINUTES);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
    private synchronized Cluster getCluster(int index) {
        return clusters[index];
    }

    private void recomputeMeanOfCluster(int clusterIndex) {
        int length = features.get(0).length;
        Cluster cluster = getCluster(clusterIndex);
        float[] mean = cluster.mean;
        for (int j = 0; j < length; j++) {
            mean[j] = 0;
            for (Iterator<Integer> iterator = cluster.members.iterator(); iterator.hasNext(); ) {
                Integer member = iterator.next();
                double v = features.get(member)[j];
                mean[j] += v;
            }
            if (cluster.members.size() > 1)
                mean[j] = mean[j] / cluster.members.size();
        }
        double v = 0;
        // add up to stress ...
        for (Integer member : cluster.members) {
            float tmpStress = 0;
            for (int k = 0; k < length; k++) {
                double f = Math.abs(mean[k] - features.get(member)[k]);
                tmpStress += f;
            }
            v += tmpStress;
        }
        cluster.setStress(v);
    }

    private class FeatureToClass implements Runnable {
        HashMap<Integer, Integer> results;
        int start, end;

        private FeatureToClass(int start, int end) {
            this.start = start;
            this.end = end;
            results = new HashMap<Integer, Integer>(end - start);
        }

        public void run() {
            for (int k = start; k < end; k++) {
                double[] f = features.get(k);
                int best = 0;
                double minDistance = clusters[0].getDistance(f);
                for (int i = 1; i < clusters.length; i++) {
                    double v = clusters[i].getDistance(f);
                    if (minDistance > v) {
                        best = i;
                        minDistance = v;
                    }
                }
                results.put(k, best);
            }
        }

        public HashMap<Integer, Integer> getResults() {
            return results;
        }
    }
}
