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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Set;

import net.semanticmetadata.lire.imageanalysis.LocalFeature;
import net.semanticmetadata.lire.utils.StatsUtils;

/**
 * ...
 * Date: 23.09.2008
 * Time: 12:41:07
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class KMeans {
    //protected List<Image> images = new LinkedList<Image>();
    protected int numClusters = 500;
    protected ArrayList<LocalFeature> features = new ArrayList<LocalFeature>(5000);
    protected Cluster[] clusters = null;
    //protected HashMap<double[], Integer> featureIndex = null;

    public KMeans() {

    }

    public KMeans(int numClusters) {
        this.numClusters = numClusters;
    }

    /*
    public void addImage(String identifier, List<double[]> features) {
        images.add(new Image(identifier, features));
        countAllFeatures += features.size();
    }
    */

    /**
     * Adds a reference to the given LocalFeature to the internal set of features.
     * The LocalFeature will not be copied.
     */
    public void addFeature(LocalFeature f)
    {
        features.add(f);
    }
    
    public int getFeatureCount() {
        return features.size();
    }

    public void init() {
        if (features.size() < numClusters*2) {
            System.err.println("WARNING: Please note that the number of local features, in this case " + features.size() + ", is" +
                    "smaller than the recommended minimum number, which is two times the number of visual words, in your case 2*" + numClusters +
                    ". Please adapt your data and either use images with more local features or more images for creating the visual vocabulary.");
        }
        if (features.size() < numClusters) {
            throw new IllegalArgumentException(String.format("KMeans: The number of features (%d) is smaller than the number of clusters (%d)!",
                    features.size(), numClusters));
        }
        // find first clusters:
        clusters = new Cluster[numClusters];
        Set<Integer> medians = selectInitialMedians(numClusters);
        Iterator<Integer> mediansIterator = medians.iterator();
        for (int i = 0; i < clusters.length; i++) {
            clusters[i] = new Cluster(features.get(mediansIterator.next()));
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
        for (Cluster c : clusters) {
            c.members.clear();
        }
        reOrganizeFeatures();
        recomputeCentroids();
        return overallStress();
    }

    /**
     * Assign features to the nearest cluster.
     * Assumes that each cluster's member set is empty.
     */
    protected void reOrganizeFeatures() {
        for (LocalFeature f : features) {
            Cluster best = clusters[0];
            float minDistance = f.getDistance(clusters[0].centroid);
            for (int i = 1; i < clusters.length; i++) {
                float d = f.getDistance(clusters[i].centroid);
                if (d < minDistance) {
                    best = clusters[i];
                    minDistance = d;
                }
            }
            best.members.add(f);
        }
    }

    /**
     * Update the centroids based on current cluster memberships.
     */
    protected void recomputeCentroids() {
        for (Cluster c : clusters) {
            c.centroid.setCentroid(c.members);
        }
    }

    /**
     * Compute sum of distances between every LocalFeature and its centroid.
     */
    protected double overallStress() {
        double v = 0;
        for (Cluster c : clusters) {
            for (LocalFeature f : c.members) {
                v += c.centroid.getDistance(f);
            }
        }
        return v;
    }

    public Cluster[] getClusters() {
        return clusters;
    }

    /**
     * Set the number of desired clusters.
     *
     * @return
     */
    /*
    public int getNumClusters() {
        return numClusters;
    }

    public void setNumClusters(int numClusters) {
        this.numClusters = numClusters;
    }
    */
    
    /*
    private HashMap<double[], Integer> createIndex() {
        featureIndex = new HashMap<double[], Integer>(features.size());
        for (int i = 0; i < clusters.length; i++) {
            Cluster cluster = clusters[i];
            for (Iterator<Integer> fidit = cluster.members.iterator(); fidit.hasNext(); ) {
                int fid = fidit.next();
                featureIndex.put(features.get(fid), i);
            }
        }
        return featureIndex;
    }
    */

    /**
     * Used to find the cluster of a feature actually used in the clustering process (so
     * it is known by the k-means class).
     *
     * @param f the feature to search for
     * @return the index of the Cluster
    public int getClusterOfFeature(Histogram f) {
        if (featureIndex == null) createIndex();
        return featureIndex.get(f);
    }
     */
}

/*
class Image {
    public List<double[]> features;
    public String identifier;
    public float[] localFeatureHistogram = null;
    private final int QUANT_MAX_HISTOGRAM = 256;

    Image(String identifier, List<double[]> features) {
        this.features = new LinkedList<double[]>();
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
*/
