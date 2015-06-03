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
 * Updated: 10.02.15 09:05
 */
package net.semanticmetadata.lire.imageanalysis.bovw;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.clustering.Cluster;
import net.semanticmetadata.lire.clustering.KMeans;
import net.semanticmetadata.lire.clustering.ParallelKMeans;
import net.semanticmetadata.lire.imageanalysis.Histogram;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * General class creating bag of visual words vocabularies parallel based on k-means. Works with SIFT, SURF and MSER.
 * Date: 24.09.2008
 * Time: 09:38:53
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class BOVWBuilder {
    IndexReader reader;
    // number of documents used to build the vocabulary / clusters.
    private int numDocsForVocabulary = 500;
    private int numClusters = 512;
    private Cluster[] clusters = null;
    DecimalFormat df = (DecimalFormat) NumberFormat.getNumberInstance();
    private ProgressMonitor pm = null;

    protected LireFeature lireFeature;
    protected String localFeatureFieldName;
    protected String visualWordsFieldName;
    protected String localFeatureHistFieldName;
    protected String clusterFile;

    public static boolean DELETE_LOCAL_FEATURES = true;
    private boolean useParallelClustering = true;

    /**
     *
     * @param reader
     * @deprecated
     */
    public BOVWBuilder(IndexReader reader) {
        this.reader = reader;
    }

    /**
     * Creates a new instance of the BOVWBuilder using the given reader. The numDocsForVocabulary
     * indicates how many documents of the index are used to build the vocabulary (clusters).
     *
     * @param reader               the reader used to open the Lucene index,
     * @param numDocsForVocabulary gives the number of documents for building the vocabulary (clusters).
     * @deprecated
     */
    public BOVWBuilder(IndexReader reader, int numDocsForVocabulary) {
        this.reader = reader;
        this.numDocsForVocabulary = numDocsForVocabulary;
    }

    /**
     * Creates a new instance of the BOVWBuilder using the given reader. The numDocsForVocabulary
     * indicates how many documents of the index are used to build the vocabulary (clusters). The numClusters gives
     * the number of clusters k-means should find. Note that this number should be lower than the number of features,
     * otherwise an exception will be thrown while indexing.
     *
     * @param reader               the index reader
     * @param numDocsForVocabulary the number of documents that should be sampled for building the visual vocabulary
     * @param numClusters          the size of the visual vocabulary
     * @deprecated
     */
    public BOVWBuilder(IndexReader reader, int numDocsForVocabulary, int numClusters) {
        this.numDocsForVocabulary = numDocsForVocabulary;
        this.numClusters = numClusters;
        this.reader = reader;
    }

    /**
     * Creates a new instance of the BOVWBuilder using the given reader. TODO: write
     *
     * @param reader               the index reader
     * @param lireFeature          lireFeature used
     */
    public BOVWBuilder(IndexReader reader, LireFeature lireFeature) {
        this.reader = reader;
        this.lireFeature = lireFeature;
    }

    /**
     * Creates a new instance of the BOVWBuilder using the given reader. The numDocsForVocabulary
     * indicates how many documents of the index are used to build the vocabulary (clusters).
     * TODO: write
     *
     * @param reader               the index reader
     * @param lireFeature          lireFeature used
     * @param numDocsForVocabulary the number of documents that should be sampled for building the visual vocabulary
     */
    public BOVWBuilder(IndexReader reader, LireFeature lireFeature, int numDocsForVocabulary) {
        this.numDocsForVocabulary = numDocsForVocabulary;
        this.reader = reader;
        this.lireFeature = lireFeature;
    }

    /**
     * Creates a new instance of the BOVWBuilder using the given reader. The numDocsForVocabulary
     * indicates how many documents of the index are used to build the vocabulary (clusters). The numClusters gives
     * the number of clusters k-means should find. Note that this number should be lower than the number of features,
     * otherwise an exception will be thrown while indexing. TODO: write
     *
     * @param reader               the index reader
     * @param lireFeature          lireFeature used
     * @param numDocsForVocabulary the number of documents that should be sampled for building the visual vocabulary
     * @param numClusters          the size of the visual vocabulary
     */
    public BOVWBuilder(IndexReader reader, LireFeature lireFeature, int numDocsForVocabulary, int numClusters) {
        this.numDocsForVocabulary = numDocsForVocabulary;
        this.numClusters = numClusters;
        this.reader = reader;
        this.lireFeature = lireFeature;
    }

    protected void init() {
        localFeatureFieldName = lireFeature.getFieldName();
        visualWordsFieldName = lireFeature.getFieldName() + DocumentBuilder.FIELD_NAME_BOVW;
        localFeatureHistFieldName = lireFeature.getFieldName()+ DocumentBuilder.FIELD_NAME_BOVW_VECTOR;
        clusterFile = "./clusters-bovw" + lireFeature.getFeatureName() +  ".dat";
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
        init();
        df.setMaximumFractionDigits(3);
        // find the documents for building the vocabulary:
        HashSet<Integer> docIDs = selectVocabularyDocs();
        KMeans k;
        if (useParallelClustering) k = new ParallelKMeans(numClusters);
        else k = new KMeans(numClusters);
        // fill the KMeans object:
        LinkedList<double[]> features = new LinkedList<double[]>();
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        for (Iterator<Integer> iterator = docIDs.iterator(); iterator.hasNext(); ) {
            int nextDoc = iterator.next();
            if (reader.hasDeletions() && !liveDocs.get(nextDoc)) continue; // if it is deleted, just ignore it.
            Document d = reader.document(nextDoc);
            features.clear();
            IndexableField[] fields = d.getFields(localFeatureFieldName);
            String file = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            for (int j = 0; j < fields.length; j++) {
                LireFeature f = getFeatureInstance();
                f.setByteArrayRepresentation(fields[j].binaryValue().bytes, fields[j].binaryValue().offset, fields[j].binaryValue().length);
                // copy the data over to new array ...
                double[] feat = new double[f.getDoubleHistogram().length];
                System.arraycopy(f.getDoubleHistogram(), 0, feat, 0, feat.length);
                features.add(f.getDoubleHistogram());
            }
            k.addImage(file, features);
        }
        if (pm != null) { // set to 5 of 100 before clustering starts.
            pm.setProgress(5);
            pm.setNote("Starting clustering");
        }
        if (k.getFeatureCount() < numClusters) {
            // this cannot work. You need more data points than clusters.
            throw new UnsupportedOperationException("Only " + features.size() + " features found to cluster in " + numClusters + ". Try to use less clusters or more images.");
        }
        // do the clustering:
        System.out.println("Number of local features: " + df.format(k.getFeatureCount()));
        System.out.println("Starting clustering ...");
        k.init();
        System.out.println("Step.");
        double time = System.currentTimeMillis();
        double laststress = k.clusteringStep();

        if (pm != null) { // set to 8 of 100 after first step.
            pm.setProgress(8);
            pm.setNote("Step 1 finished");
        }

        System.out.println(getDuration(time) + " -> Next step.");
        time = System.currentTimeMillis();
        double newStress = k.clusteringStep();

        if (pm != null) { // set to 11 of 100 after second step.
            pm.setProgress(11);
            pm.setNote("Step 2 finished");
        }

        // critical part: Give the difference in between steps as a constraint for accuracy vs. runtime trade off.
        double threshold = Math.max(20d, (double) k.getFeatureCount() / 1000d);
        System.out.println("Threshold = " + df.format(threshold));
        int cstep = 3;
        while (Math.abs(newStress - laststress) > threshold && cstep < 12) {
            System.out.println(getDuration(time) + " -> Next step. Stress difference ~ |" + (int) newStress + " - " + (int) laststress + "| = " + df.format(Math.abs(newStress - laststress)));
            time = System.currentTimeMillis();
            laststress = newStress;
            newStress = k.clusteringStep();
            if (pm != null) { // set to XX of 100 after second step.
                pm.setProgress(cstep * 3 + 5);
                pm.setNote("Step " + cstep + " finished");
            }
            cstep++;
        }
        // Serializing clusters to a file on the disk ...
        clusters = k.getClusters();
//        for (int i = 0; i < clusters.length; i++) {
//            Cluster cluster = clusters[i];
//            System.out.print(cluster.getMembers().size() + ", ");
//        }
//        System.out.println();
        Cluster.writeClusters(clusters, clusterFile);
        //  create & store histograms:
        System.out.println("Creating histograms ...");
        time = System.currentTimeMillis();
//        int[] tmpHist = new int[numClusters];
        IndexWriter iw = LuceneUtils.createIndexWriter(((DirectoryReader) reader).directory(), true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer, 256d);
        if (pm != null) { // set to 50 of 100 after clustering.
            pm.setProgress(50);
            pm.setNote("Clustering finished");
        }
        // parallelized indexing
        LinkedList<Thread> threads = new LinkedList<Thread>();
        int numThreads = 8;
        // careful: copy reader to RAM for faster access when reading ...
//        reader = IndexReader.open(new RAMDirectory(reader.directory()), true);
        int step = reader.maxDoc() / numThreads;
        for (int part = 0; part < numThreads; part++) {
            Indexer indexer = null;
            if (part < numThreads - 1) indexer = new Indexer(part * step, (part + 1) * step, iw, null);
            else indexer = new Indexer(part * step, reader.maxDoc(), iw, pm);
            Thread t = new Thread(indexer);
            threads.add(t);
            t.start();
        }
        for (Iterator<Thread> iterator = threads.iterator(); iterator.hasNext(); ) {
            Thread next = iterator.next();
            try {
                next.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (pm != null) { // set to 50 of 100 after clustering.
            pm.setProgress(95);
            pm.setNote("Indexing finished, optimizing index now.");
        }

        System.out.println(getDuration(time));
        iw.commit();
        // this one does the "old" commit(), it removes the deleted SURF features.
        iw.forceMerge(1);
        iw.close();
        if (pm != null) { // set to 50 of 100 after clustering.
            pm.setProgress(100);
            pm.setNote("Indexing & optimization finished");
            pm.close();
        }
        System.out.println("Finished.");
    }


    public void indexMissing() throws IOException {
        init();
        // Reading clusters from disk:
        clusters = Cluster.readClusters(clusterFile);
        //  create & store histograms:
        System.out.println("Creating histograms ...");
        LireFeature f = getFeatureInstance();

        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);

        // based on bug report from Einav Itamar <einavitamar@gmail.com>
        IndexWriter iw = LuceneUtils.createIndexWriter(((DirectoryReader) reader).directory(),
                false, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        int counter = 0;
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.
            Document d = reader.document(i);
            // Only if there are no values yet:
            if (d.getValues(visualWordsFieldName) == null || d.getValues(visualWordsFieldName).length == 0) {
                createVisualWords(d, f);
                // now write the new one. we use the identifier to update ;)
                iw.updateDocument(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), d);
                counter++;
            }
        }
        System.out.println(counter + " Documents were updated");
        iw.commit();
        // added to permanently remove the deleted docs.
        iw.forceMerge(1);
        iw.close();
        System.out.println("Finished.");
    }

    /**
     * Takes one single document and creates the visual words and adds them to the document. The same document is returned.
     *
     * @param d the document to use for adding the visual words
     * @return
     * @throws IOException
     */
    public Document getVisualWords(Document d) throws IOException {
        init(); // bug report by Haihui Cai
        clusters = Cluster.readClusters(clusterFile);
        LireFeature f = getFeatureInstance();
        createVisualWords(d, f);

        return d;
    }


    private void quantize(double[] histogram) {
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
     * @param f
     * @return the index of the cluster.
     */
    private int clusterForFeature(Histogram f) {
        double distance = clusters[0].getDistance(f);
        double tmp;
        int result = 0;
        for (int i = 1; i < clusters.length; i++) {
            tmp = clusters[i].getDistance(f);
            if (tmp < distance) {
                distance = tmp;
                result = i;
            }
        }
        return result;
    }

    private String arrayToVisualWordString(double[] hist) {
        StringBuilder sb = new StringBuilder(1024);
        for (int i = 0; i < hist.length; i++) {
            int visualWordIndex = (int) hist[i];
            for (int j = 0; j < visualWordIndex; j++) {
                // sb.append('v');
                sb.append(Integer.toHexString(i));
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

//    protected abstract LireFeature getFeatureInstance();

    protected LireFeature getFeatureInstance() {
        LireFeature result = null;
        try {
            result =  lireFeature.getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    private class Indexer implements Runnable {
        int start, end;
        IndexWriter iw;
        ProgressMonitor pm = null;

        private Indexer(int start, int end, IndexWriter iw, ProgressMonitor pm) {
            this.start = start;
            this.end = end;
            this.iw = iw;
            this.pm = pm;
        }

        public void run() {
            LireFeature f = getFeatureInstance();
            for (int i = start; i < end; i++) {
                try {
                    Document d = reader.document(i);
                    createVisualWords(d, f);
                    iw.updateDocument(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), d);
                    if (pm != null) {
                        double len = (double) (end - start);
                        double percent = (double) (i - start) / len * 45d + 50;
                        pm.setProgress((int) percent);
                        pm.setNote("Creating visual words, ~" + (int) percent + "% finished");
                    }
//                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void createVisualWords(Document d, LireFeature f)
    {
        double[] tmpHist = new double[numClusters];
        Arrays.fill(tmpHist, 0d);
        IndexableField[] fields = d.getFields(localFeatureFieldName);
        // remove the fields if they are already there ...
        d.removeField(visualWordsFieldName);
        d.removeField(localFeatureHistFieldName);

        // find the appropriate cluster for each feature:
        for (int j = 0; j < fields.length; j++) {
            f.setByteArrayRepresentation(fields[j].binaryValue().bytes, fields[j].binaryValue().offset, fields[j].binaryValue().length);
            tmpHist[clusterForFeature((Histogram) f)]++;
        }
        //quantize(tmpHist);
        d.add(new TextField(visualWordsFieldName, arrayToVisualWordString(tmpHist), Field.Store.YES));
        d.add(new StoredField(localFeatureHistFieldName, SerializationUtils.toByteArray(tmpHist)));
        // remove local features to save some space if requested:
        if (DELETE_LOCAL_FEATURES) {
            d.removeFields(localFeatureFieldName);
        }

        // for debugging ..
//        System.out.println(d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0] + " " + Arrays.toString(tmpHist));
    }

    private String getDuration(double time) {
        double min = (System.currentTimeMillis() - time) / (1000 * 60);
        double sec = (min - Math.floor(min)) * 60;
        return String.format("%02d:%02d", (int) min, (int) sec);
    }

    public void setProgressMonitor(ProgressMonitor pm) {
        this.pm = pm;
    }

    /**
     * Indicates whether parallel k-means is applied (true) or just the
     * single threaded implementation (false)
     *
     * @return true is parallel k-means
     */
    public boolean getUseParallelClustering() {
        return useParallelClustering;
    }

    /**
     * Indicates whether parallel k-means is applied (true) or just the
     * single threaded implementation (false)
     *
     * @param useParallelClustering set to true if parallel processing should be used.
     */
    public void setUseParallelClustering(boolean useParallelClustering) {
        this.useParallelClustering = useParallelClustering;
    }
}
