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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *  
 * Updated: 13.02.15 19:17
 */
package net.semanticmetadata.lire.searchers.custom;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.searchers.*;
import net.semanticmetadata.lire.utils.MetricsUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A ImageSearcher that retrieves just the first result, caches the whole index and optimizes search time by 
 * bundling searches. Please note that as soon as the instance is created, changes in the index are not 
 * reflected.  
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SingleNddCeddImageSearcher extends AbstractImageSearcher {
    protected Logger logger = Logger.getLogger(getClass().getName());
    Class<?> descriptorClass = CEDD.class;
    String fieldName = null;
    protected GlobalFeature cachedInstance = null;
    protected boolean isCaching = true;

    protected ArrayList<double[]> featureCache;
    protected IndexReader reader;

    protected TreeSet<SimpleResult> docs;

    HashMap<double[], LinkedList<Integer>> hashMap;

    protected double maxDistance;
    protected boolean useSimilarityScore = false;

    private boolean halfDimensions = false;

    /**
     * Creates a new ImageSearcher for searching just one single image based on CEDD from a RAM cached data set.
     *
     * @param reader the index reader pointing to the index. It will be cached first, so changes will not be reflected in this instance. 
     */
    public SingleNddCeddImageSearcher(IndexReader reader) {
        init(reader);
    }

    /**
     * Creates a new ImageSearcher for searching just one single image based on CEDD from a RAM cached data set. 
     * Set approximate to true if you want to speed up search and loose accuracy.
     *
     * @param reader the index reader pointing to the index. It will be cached first, so changes will not be reflected in this instance.
     * @param approximate set to true if you want to trade accuracy to speed, setting to true is faster (~ double speed), but less accurate                             
     */
    public SingleNddCeddImageSearcher(IndexReader reader, boolean approximate) {
        this.halfDimensions = approximate;
        init(reader);
    }

    /**
     * Eventually to be used with other LireFeature classes.
     * @param reader
     * @param approximate
     * @param descriptorClass
     */
    public SingleNddCeddImageSearcher(IndexReader reader, boolean approximate, Class descriptorClass, String fieldName) {
        this.halfDimensions = approximate;
        this.descriptorClass = descriptorClass;
        this.fieldName = fieldName;
        init(reader);
    }

    protected void init(IndexReader reader) {
        this.reader = reader;
        if (reader.hasDeletions()) {
            throw new UnsupportedOperationException("The index has to be optimized first to be cached! Use IndexWriter.forceMerge(0) to do this.");
        }
        docs = new TreeSet<SimpleResult>();
        try {
            this.cachedInstance = (GlobalFeature) this.descriptorClass.newInstance();
            if (fieldName == null) fieldName = this.cachedInstance.getFieldName();
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher (" + descriptorClass.getName() + "): " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher (" + descriptorClass.getName() + "): " + e.getMessage());
        }
        // put all respective features into an in-memory cache ...
        if (isCaching && reader != null) {
            int docs = reader.numDocs();
            featureCache = new ArrayList<double[]>(docs);
            try {
                Document d;
                for (int i = 0; i < docs; i++) {
                    d = reader.document(i);
                    cachedInstance.setByteArrayRepresentation(d.getField(fieldName).binaryValue().bytes, d.getField(fieldName).binaryValue().offset, d.getField(fieldName).binaryValue().length);
                    // normalize features,o we can use L1
                    if (!halfDimensions) {
                        featureCache.add(normalize(cachedInstance.getFeatureVector()));
                    } else {
                        featureCache.add(crunch(cachedInstance.getFeatureVector()));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private double[] normalize(double[] doubleHistogram) {
        double[] result = new double[doubleHistogram.length];
        for (int i = 0; i < doubleHistogram.length; i++) {
            result[i] = doubleHistogram[i] / 8d;
        }
        return result;
    }

    /**
     * Reduces dimensions of CEDD to half while normalizing the vector.
     * @param doubleHistogram
     * @return
     */
    private double[] crunch(double[] doubleHistogram) {
        double[] result = new double[doubleHistogram.length / 2];
        for (int i = 0; i < doubleHistogram.length; i += 2) {
            result[i / 2] = doubleHistogram[i] + doubleHistogram[i + 1] / 16d;
        }
        return result;
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("not implemented!");
    }

    /**
     * @param reader
     * @param globalFeature
     * @return the maximum distance found for normalizing.
     * @throws java.io.IOException
     */
    protected double findSimilar(IndexReader reader, GlobalFeature globalFeature) throws IOException {
        maxDistance = -1;

        // clear result set ...
        docs.clear();
        double tmpDistance;

        // we use the in-memory cache to find the matching docs from the index.
        int count = 0;
        double[] doubleHistogram;
        if (!halfDimensions) {
            doubleHistogram = normalize(globalFeature.getFeatureVector());
        } else {
            doubleHistogram = crunch(globalFeature.getFeatureVector());
        }
        double[] tmp;
        int index = -1;
        for (Iterator<double[]> iterator = featureCache.iterator(); iterator.hasNext(); ) {
            tmp = iterator.next();
            tmpDistance = MetricsUtils.distL1(doubleHistogram, tmp);
            assert (tmpDistance >= 0);
            if (tmpDistance < maxDistance) {
                maxDistance = tmpDistance;
                index = count;
            }
            count++;
        }
        this.docs.add(new SimpleResult(maxDistance, index));
        return maxDistance;
    }

    public SimpleResult findMostSimilar(GlobalFeature globalFeature) throws IOException {
        findSimilar(reader, globalFeature);
        return docs.first();
    }

    public SimpleResult[] findMostSimilar(GlobalFeature[] globalFeatures) throws IOException {
        return findMostSimilar(globalFeatures, 0, globalFeatures.length);
    }

    public SimpleResult[] findMostSimilar(GlobalFeature[] globalFeatures, int offset, int length) throws IOException {
        double[] maxDistanceArray = new double[length - offset];
        Arrays.fill(maxDistanceArray, Double.MAX_VALUE);

        double tmpDistance;

        int count = 0;
        double[][] dhs = new double[0][];
        try {
            dhs = new double[length][featureCache.get(0).length];
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < dhs.length; i++) {
            if (!halfDimensions) {
                dhs[i] = normalize(globalFeatures[offset + i].getFeatureVector());
            } else {
                dhs[i] = crunch(globalFeatures[offset + i].getFeatureVector());
            }
        }
        double[] tmp;
        int[] indexes = new int[length];
        Arrays.fill(indexes, -1);
        for (Iterator<double[]> iterator = featureCache.iterator(); iterator.hasNext(); ) {
            tmp = iterator.next();
            for (int i = 0; i < dhs.length; i++) {
                tmpDistance = MetricsUtils.distL1(dhs[i], tmp);
                assert (tmpDistance >= 0);
                if (tmpDistance < maxDistanceArray[i]) {
                    maxDistanceArray[i] = tmpDistance;
                    indexes[i] = count;
                }
            }
            count++;
        }
        SimpleResult[] results = new SimpleResult[length];
        for (int i = 0; i < results.length; i++) {
            if (indexes[i] >= 0 && indexes[i] < reader.maxDoc())
                results[i] = new SimpleResult(maxDistanceArray[i], indexes[i]);
            else
                results[i] = null;
        }
        return results;
    }

    /**
     * Main similarity method called for each and every document in the index.
     *
     * @param document
     * @param globalFeature
     * @return the distance between the given feature and the feature stored in the document.
     */
    protected double getDistance(Document document, GlobalFeature globalFeature) {
        if (document.getField(fieldName).binaryValue() != null && document.getField(fieldName).binaryValue().length > 0) {
            cachedInstance.setByteArrayRepresentation(document.getField(fieldName).binaryValue().bytes, document.getField(fieldName).binaryValue().offset, document.getField(fieldName).binaryValue().length);
            return globalFeature.getDistance(cachedInstance);
        } else {
            logger.warning("No feature stored in this document! (" + descriptorClass.getName() + ")");
        }
        return 0d;
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        SimpleImageSearchHits searchHits = null;
        try {
            GlobalFeature globalFeature = (GlobalFeature) descriptorClass.newInstance();

            if (doc.getField(fieldName).binaryValue() != null && doc.getField(fieldName).binaryValue().length > 0)
                globalFeature.setByteArrayRepresentation(doc.getField(fieldName).binaryValue().bytes, doc.getField(fieldName).binaryValue().offset, doc.getField(fieldName).binaryValue().length);
            double maxDistance = findSimilar(reader, globalFeature);

            if (!useSimilarityScore) {
                searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
            } else {
                searchHits = new SimpleImageSearchHits(this.docs, maxDistance, useSimilarityScore);
            }
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        }
        return searchHits;
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("not implemented!");
    }

    public String toString() {
        return "GenericSearcher using " + descriptorClass.getName();
    }

}
