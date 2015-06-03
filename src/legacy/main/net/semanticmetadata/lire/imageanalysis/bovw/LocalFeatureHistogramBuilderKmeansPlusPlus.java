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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 11.07.13 11:21
 */
package net.semanticmetadata.lire.imageanalysis.bovw;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.Histogram;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.DoublePoint;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.util.Bits;

import javax.swing.*;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * General class creating bag of visual words vocabularies parallel based on k-means. Works with SIFT, SURF and MSER.
 * Date: 24.09.2008
 * Time: 09:38:53
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public abstract class LocalFeatureHistogramBuilderKmeansPlusPlus {
    IndexReader reader;
    // number of documents used to build the vocabulary / clusters.
    private int numDocsForVocabulary = 100;
    private int numClusters = 512;
    private LinkedList<double[]> clusters = null;
    DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance();
    private ProgressMonitor pm = null;

    //    protected String localFeatureFieldName = DocumentBuilder.FIELD_NAME_SURF;
//    protected String visualWordsFieldName = DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS;
//    protected String localFeatureHistFieldName = DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM;
    protected String localFeatureFieldName = DocumentBuilder.FIELD_NAME_SURF;
    protected String visualWordsFieldName = DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW;
    protected String localFeatureHistFieldName = DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW_VECTOR;
    protected String clusterFile = "./clusters.dat";
    public static boolean DELETE_LOCAL_FEATURES = true;


    public LocalFeatureHistogramBuilderKmeansPlusPlus(IndexReader reader) {
        this.reader = reader;
    }

    /**
     * Creates a new instance of the LocalFeatureHistogramBuilder using the given reader. The numDocsForVocabulary
     * indicates how many documents of the index are used to build the vocabulary (clusters).
     *
     * @param reader               the reader used to open the Lucene index,
     * @param numDocsForVocabulary gives the number of documents for building the vocabulary (clusters).
     */
    public LocalFeatureHistogramBuilderKmeansPlusPlus(IndexReader reader, int numDocsForVocabulary) {
        this.reader = reader;
        this.numDocsForVocabulary = numDocsForVocabulary;
    }

    /**
     * Creates a new instance of the LocalFeatureHistogramBuilder using the given reader. The numDocsForVocabulary
     * indicates how many documents of the index are used to build the vocabulary (clusters). The numClusters gives
     * the number of clusters k-means should find. Note that this number should be lower than the number of features,
     * otherwise an exception will be thrown while indexing.
     *
     * @param reader               the index reader
     * @param numDocsForVocabulary the number of documents that should be sampled for building the visual vocabulary
     * @param numClusters          the size of the visual vocabulary
     */
    public LocalFeatureHistogramBuilderKmeansPlusPlus(IndexReader reader, int numDocsForVocabulary, int numClusters) {
        this.numDocsForVocabulary = numDocsForVocabulary;
        this.numClusters = numClusters;
        this.reader = reader;
    }

    /**
     * Uses an existing index, where each and every document should have a set of local features. A number of
     * random images (numDocsForVocabulary) is selected and clustered to get a vocabulary of visual words
     * (the cluster means). For all images a histogram on the visual words is created and added to the documents.
     * Pre-existing histograms are deleted, so this method can be used for re-indexing.
     *
     * @throws java.io.IOException
     */
    public void index() throws IOException {
        df.setMaximumFractionDigits(3);
        // find the documents for building the vocabulary:
        HashSet<Integer> docIDs = selectVocabularyDocs();
        System.out.println("Using " + docIDs.size() + " documents to build the vocabulary.");
        KMeansPlusPlusClusterer kpp = new KMeansPlusPlusClusterer(numClusters, 15);
        // fill the KMeans object:
        LinkedList<DoublePoint> features = new LinkedList<DoublePoint>();
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        for (Iterator<Integer> iterator = docIDs.iterator(); iterator.hasNext(); ) {
            int nextDoc = iterator.next();
            if (reader.hasDeletions() && !liveDocs.get(nextDoc)) continue; // if it is deleted, just ignore it.
            Document d = reader.document(nextDoc);
//            features.clear();
            IndexableField[] fields = d.getFields(localFeatureFieldName);
            String file = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            for (int j = 0; j < fields.length; j++) {
                LireFeature f = getFeatureInstance();
                f.setByteArrayRepresentation(fields[j].binaryValue().bytes, fields[j].binaryValue().offset, fields[j].binaryValue().length);
                // copy the data over to new array ...
                double[] feat = new double[f.getDoubleHistogram().length];
                System.arraycopy(f.getDoubleHistogram(), 0, feat, 0, feat.length);
                features.add(new DoublePoint(f.getDoubleHistogram()));
            }
        }
        if (features.size() < numClusters) {
            // this cannot work. You need more data points than clusters.
            throw new UnsupportedOperationException("Only " + features.size() + " features found to cluster in " + numClusters + ". Try to use less clusters or more images.");
        }
        // do the clustering:
        System.out.println("Number of local features: " + df.format(features.size()));
        System.out.println("Starting clustering ...");
        List<CentroidCluster<DoublePoint>> clusterList = kpp.cluster(features);
        // TODO: Serializing clusters to a file on the disk ...
        System.out.println("Clustering finished, " + clusterList.size() + " clusters found");
        clusters = new LinkedList<double[]>();
        for (Iterator<CentroidCluster<DoublePoint>> iterator = clusterList.iterator(); iterator.hasNext(); ) {
            CentroidCluster<DoublePoint> centroidCluster = iterator.next();
            clusters.add(centroidCluster.getCenter().getPoint());
        }
        System.out.println("Creating histograms ...");
        int[] tmpHist = new int[numClusters];
        IndexWriter iw = LuceneUtils.createIndexWriter(((DirectoryReader) reader).directory(), true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer, 256d);

        // careful: copy reader to RAM for faster access when reading ...
//        reader = IndexReader.open(new RAMDirectory(reader.directory()), true);
        LireFeature f = getFeatureInstance();
        for (int i = 0; i < reader.maxDoc(); i++) {
            try {
                if (reader.hasDeletions() && !liveDocs.get(i)) continue;
                for (int j = 0; j < tmpHist.length; j++) {
                    tmpHist[j] = 0;
                }
                Document d = reader.document(i);
                IndexableField[] fields = d.getFields(localFeatureFieldName);
                // remove the fields if they are already there ...
                d.removeField(visualWordsFieldName);
                d.removeField(localFeatureHistFieldName);

                // find the appropriate cluster for each feature:
                for (int j = 0; j < fields.length; j++) {
                    f.setByteArrayRepresentation(fields[j].binaryValue().bytes, fields[j].binaryValue().offset, fields[j].binaryValue().length);
                    tmpHist[clusterForFeature(f, clusters)]++;
                }
//                System.out.println(Arrays.toString(tmpHist));
                d.add(new StoredField(localFeatureHistFieldName, SerializationUtils.toByteArray(normalize(tmpHist))));
                quantize(tmpHist);
                d.add(new TextField(visualWordsFieldName, arrayToVisualWordString(tmpHist), Field.Store.YES));

                // remove local features to save some space if requested:
                if (DELETE_LOCAL_FEATURES) {
                    d.removeFields(localFeatureFieldName);
                }
                // now write the new one. we use the identifier to update ;)
                iw.updateDocument(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        iw.commit();
        // this one does the "old" commit(), it removes the deleted local features.
        iw.forceMerge(1);
        iw.close();
        System.out.println("Finished.");
    }

    /*
    public void indexMissing() throws IOException {
        // Reading clusters from disk:
        clusters = Cluster.readClusters(clusterFile);
        //  create & store histograms:
        System.out.println("Creating histograms ...");
        int[] tmpHist = new int[numClusters];
        LireFeature f = getFeatureInstance();

        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);

        // based on bug report from Einav Itamar <einavitamar@gmail.com>
        IndexWriter iw = LuceneUtils.createIndexWriter(((DirectoryReader) reader).directory(),
                false, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.
            for (int j = 0; j < tmpHist.length; j++) {
                tmpHist[j] = 0;
            }
            Document d = reader.document(i);
            // Only if there are no values yet:
            if (d.getValues(visualWordsFieldName) == null || d.getValues(visualWordsFieldName).length == 0) {
                IndexableField[] fields = d.getFields(localFeatureFieldName);
                // find the appropriate cluster for each feature:
                for (int j = 0; j < fields.length; j++) {
                    f.setByteArrayRepresentation(fields[j].binaryValue().bytes, fields[j].binaryValue().offset, fields[j].binaryValue().length);
                    tmpHist[clusterForFeature((Histogram) f, clusterList)]++;
                }
                normalize(tmpHist);
                d.add(new TextField(visualWordsFieldName, arrayToVisualWordString(tmpHist), Field.Store.YES));
                d.add(new StringField(localFeatureHistFieldName, SerializationUtils.arrayToString(tmpHist), Field.Store.YES));
                // now write the new one. we use the identifier to update ;)
                iw.updateDocument(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), d);
            }
//            }
        }
        iw.commit();
        // added to permanently remove the deleted docs.
        iw.forceMerge(1);
        iw.close();
        System.out.println("Finished.");
    }
    */


    /*
     * Takes one single document and creates the visual words and adds them to the document. The same document is returned.
     *
     * @param d the document to use for adding the visual words
     * @return
     * @throws java.io.IOException
     */
    /*
    public Document getVisualWords(Document d) throws IOException {
        clusters = Cluster.readClusters(clusterFile);
        int[] tmpHist = new int[clusters.length];
        LireFeature f = getFeatureInstance();
        IndexableField[] fields = d.getFields(localFeatureFieldName);
        // find the appropriate cluster for each feature:
        for (int j = 0; j < fields.length; j++) {
            f.setByteArrayRepresentation(fields[j].binaryValue().bytes, fields[j].binaryValue().offset, fields[j].binaryValue().length);
            tmpHist[clusterForFeature(f, clusterList)]++;
        }
        quantize(tmpHist);
        byte[] data = new byte[tmpHist.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = (byte) tmpHist[i];
        }
        d.add(new StoredField(localFeatureHistFieldName, SerializationUtils.toByteArray(tmpHist)));
        d.add(new TextField(visualWordsFieldName, arrayToVisualWordString(tmpHist), Field.Store.YES));
        d.removeFields(localFeatureFieldName);
        return d;
    }
    */

    private double[] normalize(int[] histogram) {
        double[] result = new double[histogram.length];
        double max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(max, histogram[i]);
        }
        for (int i = 0; i < histogram.length; i++) {
            result[i] = ((double) histogram[i]) / max;
        }
        return result;
    }

    private void quantize(int[] histogram) {
        double max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(max, histogram[i]);
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = (int) Math.floor((histogram[i] * 128d) / max);
        }
    }

    /**
     * Find the appropriate cluster for a given feature.
     *
     *
     *
     * @param f
     * @param clusterList
     * @return the index of the cluster.
     */
    private int clusterForFeature(Histogram f, List<double[]> clusterList) {
        double distance = MetricsUtils.distL2(clusterList.get(0), f.getDoubleHistogram());
        double tmp;
        int result = 0;
        int i = 0;
        for (double[] c : clusterList) {
            tmp = MetricsUtils.distL2(c, f.getDoubleHistogram());
            if (tmp < distance) {
                distance = tmp;
                result = i;
            }
            i++;
        }
        return result;
    }

    private String arrayToVisualWordString(int[] hist) {
        StringBuilder sb = new StringBuilder(1024);
        for (int i = 0; i < hist.length; i++) {
            int visualWordIndex = hist[i];
            for (int j = 0; j < visualWordIndex; j++) {
                sb.append('v');
                sb.append(i);
                sb.append(' ');
            }
        }
        return sb.toString();
    }

    private HashSet<Integer> selectVocabularyDocs() throws IOException {
        // need to make sure that this is not running forever ...
        int loopCount = 0;
        float maxDocs = reader.maxDoc();
        int capacity = (int) Math.min(numDocsForVocabulary, maxDocs);
        if (capacity < 0) capacity = (int) (maxDocs / 2);
        HashSet<Integer> result = new HashSet<Integer>(capacity);
        int tmpDocNumber, tmpIndex;
        LinkedList<Integer> docCandidates = new LinkedList<Integer>();
        // three cases:
        //
        // either it's more or the same number as documents
        if (numDocsForVocabulary >= maxDocs) {
            for (int i = 0; i < maxDocs; i++) {
                result.add(i);
            }
            return result;
        } else if (numDocsForVocabulary >= maxDocs - 100) { // or it's slightly less:
            for (int i = 0; i < maxDocs; i++) {
                result.add(i);
            }
            while (result.size() > numDocsForVocabulary) {
                result.remove((int) Math.floor(Math.random() * result.size()));
            }
            return result;
        } else {
            for (int i = 0; i < maxDocs; i++) {
                docCandidates.add(i);
            }
            for (int r = 0; r < capacity; r++) {
                boolean worksFine = false;
                do {
                    tmpIndex = (int) Math.floor(Math.random() * (double) docCandidates.size());
                    tmpDocNumber = docCandidates.get(tmpIndex);
                    docCandidates.remove(tmpIndex);
                    // check if the selected doc number is valid: not null, not deleted and not already chosen.
                    worksFine = (reader.document(tmpDocNumber) != null) && !result.contains(tmpDocNumber);
                } while (!worksFine);
                result.add(tmpDocNumber);
                // need to make sure that this is not running forever ...
                if (loopCount++ > capacity * 100)
                    throw new UnsupportedOperationException("Could not get the documents, maybe there are not enough documents in the index?");
            }
            return result;
        }
    }

    protected abstract LireFeature getFeatureInstance();
}
