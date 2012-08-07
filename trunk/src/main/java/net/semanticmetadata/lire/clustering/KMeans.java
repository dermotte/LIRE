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
package net.semanticmetadata.lire.clustering;

import net.semanticmetadata.lire.imageanalysis.Histogram;
import net.semanticmetadata.lire.utils.StatsUtils;

import java.util.*;

/**
 * ...
 * Date: 23.09.2008
 * Time: 12:41:07
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class KMeans {
    protected List<Image> images = new LinkedList<Image>();
    protected int countAllFeatures = 0, numClusters = 256;
    protected ArrayList<float[]> features = null;
    protected Cluster[] clusters = null;
    protected HashMap<float[], Integer> featureIndex = null;

    public KMeans() {

    }

    public KMeans(int numClusters) {
        this.numClusters = numClusters;
    }

    public void addImage(String identifier, List<float[]> features) {
        images.add(new Image(identifier, features));
        countAllFeatures += features.size();
    }

    public int getFeatureCount() {
        return countAllFeatures;
    }

    public void init() {
        // create a set of all features:
        features = new ArrayList<float[]>(countAllFeatures);
        for (Image image : images) {
            if (image.features.size() > 0)
                for (float[] histogram : image.features) {
                    if (!hasNaNs(histogram)) features.add(histogram);
                }
            else {
                System.err.println("Image with no features: " + image.identifier);
            }
        }
        // find first clusters:
        clusters = new Cluster[numClusters];
        Set<Integer> medians = selectInitialMedians(numClusters);
        Iterator<Integer> mediansIterator = medians.iterator();
        for (int i = 0; i < clusters.length; i++) {
            clusters[i] = new Cluster();
            float[] descriptor = features.get(mediansIterator.next());
            System.arraycopy(descriptor, 0, clusters[i].mean, 0, descriptor.length);
        }
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
        for (int i = 0; i < clusters.length; i++) {
            clusters[i].members.clear();
        }
        reOrganizeFeatures();
        recomputeMeans();
        return overallStress();
    }

    protected boolean hasNaNs(float[] histogram) {
        boolean hasNaNs = false;
        for (int i = 0; i < histogram.length; i++) {
            if (Float.isNaN(histogram[i])) {
                hasNaNs = true;
                break;
            }
        }
        if (hasNaNs) {
            System.err.println("Found a NaN in init");
//            System.out.println("image.identifier = " + image.identifier);
            for (int j = 0; j < histogram.length; j++) {
                float v = histogram[j];
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
        for (int k = 0; k < features.size(); k++) {
            float[] f = features.get(k);
            Cluster best = clusters[0];
            double minDistance = clusters[0].getDistance(f);
            for (int i = 1; i < clusters.length; i++) {
                double v = clusters[i].getDistance(f);
                if (minDistance > v) {
                    best = clusters[i];
                    minDistance = v;
                }
            }
            best.members.add(k);
        }
    }

    /**
     * Computes the mean per cluster (averaged vector)
     */
    protected void recomputeMeans() {
        int length = features.get(0).length;
        for (Cluster cluster : clusters) {
            float[] mean = cluster.mean;
            for (int j = 0; j < length; j++) {
                mean[j] = 0;
                for (Integer member : cluster.members) {
                    mean[j] += features.get(member)[j];
                }
                if (cluster.members.size() > 1)
                    mean[j] = mean[j] / (float) cluster.members.size();
            }
        }
    }

    /**
     * Squared error in classification.
     *
     * @return
     */
    protected double overallStress() {
        double v = 0;
        int length = features.get(0).length;
        for (int i = 0; i < clusters.length; i++) {
            for (Integer member : clusters[i].members) {
                float tmpStress = 0;
                for (int j = 0; j < length; j++) {
//                    if (Float.isNaN(features.get(member).descriptor[j])) System.err.println("Error: there is a NaN in cluster " + i + " at member " + member);
                    tmpStress += Math.abs(clusters[i].mean[j] - features.get(member)[j]);
                }
                v += tmpStress;
            }
        }
        return v;
    }

    public Cluster[] getClusters() {
        return clusters;
    }

    public List<Image> getImages() {
        return images;
    }

    /**
     * Set the number of desired clusters.
     *
     * @return
     */
    public int getNumClusters() {
        return numClusters;
    }

    public void setNumClusters(int numClusters) {
        this.numClusters = numClusters;
    }

    private HashMap<float[], Integer> createIndex() {
        featureIndex = new HashMap<float[], Integer>(features.size());
        for (int i = 0; i < clusters.length; i++) {
            Cluster cluster = clusters[i];
            for (Iterator<Integer> fidit = cluster.members.iterator(); fidit.hasNext(); ) {
                int fid = fidit.next();
                featureIndex.put(features.get(fid), i);
            }
        }
        return featureIndex;
    }

    /**
     * Used to find the cluster of a feature actually used in the clustering process (so
     * it is known by the k-means class).
     *
     * @param f the feature to search for
     * @return the index of the Cluster
     */
    public int getClusterOfFeature(Histogram f) {
        if (featureIndex == null) createIndex();
        return featureIndex.get(f);
    }
}

class Image {
    public List<float[]> features;
    public String identifier;
    public float[] localFeatureHistogram = null;
    private final int QUANT_MAX_HISTOGRAM = 256;

    Image(String identifier, List<float[]> features) {
        this.features = new LinkedList<float[]>();
        this.features.addAll(features);
        this.identifier = identifier;
    }

    public float[] getLocalFeatureHistogram() {
        return localFeatureHistogram;
    }

    public void setLocalFeatureHistogram(float[] localFeatureHistogram) {
        this.localFeatureHistogram = localFeatureHistogram;
    }

    public void initHistogram(int bins) {
        localFeatureHistogram = new float[bins];
        for (int i = 0; i < localFeatureHistogram.length; i++) {
            localFeatureHistogram[i] = 0;
        }
    }

    public void normalizeFeatureHistogram() {
        float max = 0;
        for (int i = 0; i < localFeatureHistogram.length; i++) {
            max = Math.max(localFeatureHistogram[i], max);
        }
        for (int i = 0; i < localFeatureHistogram.length; i++) {
            localFeatureHistogram[i] = (localFeatureHistogram[i] * QUANT_MAX_HISTOGRAM) / max;
        }
    }

    public void printHistogram() {
        for (int i = 0; i < localFeatureHistogram.length; i++) {
            System.out.print(localFeatureHistogram[i] + " ");

        }
        System.out.println("");
    }
}
