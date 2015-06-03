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
 * Updated: 04.05.13 11:18
 */
package net.semanticmetadata.lire.indexing;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.impl.GenericImageSearcher;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Bits;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;


/**
 * This class provides an indexing approach for approximate search based on the work of G. Amato
 * (giuseppe.amato@isti.cnr.it). See also his paper "Approximate Similarity Search in Metric Spaces
 * using Inverted Files"
 * Date: 14.05.2009
 * Time: 14:22:03
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class MetricSpacesInvertedListIndexing {
    public static int numReferenceObjects = 500;
    public static int numReferenceObjectsUsed = 50;

    private static MetricSpacesInvertedListIndexing msili = new MetricSpacesInvertedListIndexing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);

    private Class<? extends LireFeature> featureClass;
    private String featureFieldName;
    private int numHits = 100;

    private ProgressIndicator progress;

    public enum State {
        RoSelection, RoIndexing, Indexing, Idle
    }

    ;

    /**
     * @param featureClass     the feature being used for this new index (e.g. CEDD)
     * @param featureFieldName the field hashFunctionsFileName where to find the feature.
     */
    public MetricSpacesInvertedListIndexing(Class<? extends LireFeature> featureClass, String featureFieldName) {
        this.featureClass = featureClass;
        this.featureFieldName = featureFieldName;
        progress = new ProgressIndicator();
    }

    public static MetricSpacesInvertedListIndexing getDefaultInstance() {
        return msili;
    }

    /**
     * Creates a set of reference objects and stores it in a new index (hashFunctionsFileName "<indexPath>-ro"). Then creates ordered
     * lists of reference object positions for each data item in the index with given feature.
     * Finally a new index (hashFunctionsFileName "<indexPath>-ms") is created where all the original documents as well as the new data
     * are stored.
     *
     * @param indexPath the path to the original index
     * @throws IOException
     */
    public void createIndex(String indexPath) throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();

        if (numDocs < numReferenceObjects) {
            throw new UnsupportedOperationException("Too few documents in index.");
        }

        // progress report
        progress.setNumDocsAll(numDocs);
        progress.setCurrentState(State.RoSelection);

        boolean hasDeletions = reader.hasDeletions();

        // init reference objects:
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath + "-ro", true);
        HashSet<Integer> referenceObjsIds = new HashSet<Integer>(numReferenceObjects);

        double numDocsDouble = (double) numDocs;
        while (referenceObjsIds.size() < numReferenceObjects) {
            referenceObjsIds.add((int) (numDocsDouble * Math.random()));
        }
        int count = 0;

        if (hasDeletions) {
            System.err.println("WARNING: There are deleted docs in your index. You should " +
                    "optimize your index before using this method.");
        }

        // progress report
        progress.setCurrentState(State.RoIndexing);

        // find them in the index and put them into a separate index:
        for (int i : referenceObjsIds) {
            count++;
            Document document = reader.document(i);
            document.add(new Field("ro-id", count + "", StringField.TYPE_STORED));
            iw.addDocument(document);
        }
        iw.commit();
        iw.close();

        // progress report
        progress.setCurrentState(State.Indexing);

        // now find the reference objects for each entry ;)
        IndexReader readerRo = DirectoryReader.open(FSDirectory.open(new File(indexPath + "-ro")));
        ImageSearcher searcher = new GenericImageSearcher(numReferenceObjectsUsed, featureClass, featureFieldName);
        Map<String, Analyzer> analyzerPerField = new HashMap<String, Analyzer>();
        analyzerPerField.put("ro-order", new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
        PerFieldAnalyzerWrapper aWrapper = new PerFieldAnalyzerWrapper(new SimpleAnalyzer(LuceneUtils.LUCENE_VERSION), analyzerPerField);

        iw = new IndexWriter(FSDirectory.open(new File(indexPath)), new IndexWriterConfig(LuceneUtils.LUCENE_VERSION, aWrapper).setOpenMode(IndexWriterConfig.OpenMode.CREATE));
        StringBuilder sb = new StringBuilder(256);
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);

        for (int i = 0; i < numDocs; i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.
            Document document = reader.document(i);
            ImageSearchHits hits = searcher.search(document, readerRo);
            sb.delete(0, sb.length());
            for (int j = 0; j < numReferenceObjectsUsed; j++) {
                sb.append(hits.doc(j).getValues("ro-id")[0]);
                sb.append(' ');
            }
            // System.out.println(sb.toString());
            document.add(new TextField("ro-order", sb.toString(), Field.Store.YES));
            iw.updateDocument(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), document);

            // progress report
            progress.setNumDocsProcessed(progress.getNumDocsProcessed() + 1);

        }
        iw.commit();
        iw.close();

        // progress report
        progress.setCurrentState(State.Idle);

    }

    /**
     * We assume that the initial indexing has been done and a set of reference objects has been
     * found and indexed in the separate fileList. However further documents were added and they
     * now need to get a ranked list of reference objects. So we (i) get all these new documents
     * missing the field "ro-order" and (ii) add this field.
     *
     * @param indexPath the index to update
     * @throws IOException
     */
    public void updateIndex(String indexPath) throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        boolean hasDeletions = reader.hasDeletions();
        int countUpdated = 0;

        IndexReader readerRo = DirectoryReader.open(FSDirectory.open(new File(indexPath + "-ro")));
        ImageSearcher searcher = new GenericImageSearcher(numReferenceObjectsUsed, featureClass, featureFieldName);
        Map<String, Analyzer> perField = new HashMap<String, Analyzer>(1);
        perField.put("ro-order", new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
        PerFieldAnalyzerWrapper aWrapper =
                new PerFieldAnalyzerWrapper(new SimpleAnalyzer(LuceneUtils.LUCENE_VERSION), perField);

        IndexWriter iw = new IndexWriter(FSDirectory.open(new File(indexPath)), new IndexWriterConfig(LuceneUtils.LUCENE_VERSION, aWrapper).setOpenMode(IndexWriterConfig.OpenMode.CREATE));
        StringBuilder sb = new StringBuilder(256);
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);

        for (int i = 0; i < numDocs; i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.
            Document document = reader.document(i);
            if (document.getField("ro-order") == null) {  // if the field is not here we create it.
                ImageSearchHits hits = searcher.search(document, readerRo);
                sb.delete(0, sb.length());
                for (int j = 0; j < numReferenceObjectsUsed; j++) {
                    sb.append(hits.doc(j).getValues("ro-id")[0]);
                    sb.append(' ');
                }
                // System.out.println(sb.toString());
                document.add(new TextField("ro-order", sb.toString(), Field.Store.YES));
                iw.updateDocument(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), document);
                countUpdated++;
            }

            // progress report
            progress.setNumDocsProcessed(progress.getNumDocsProcessed() + 1);

            // debug:
            System.out.println("countUpdated = " + countUpdated);
        }
        iw.commit();
        iw.close();
    }

    /**
     * Provides basic search functions ...
     *
     * @param img
     * @param indexPath
     * @return
     * @throws IOException
     */
    public TopDocs search(BufferedImage img, String indexPath) throws IOException {
        ImageSearcher searcher = new GenericImageSearcher(numReferenceObjectsUsed, featureClass, featureFieldName);
        ImageSearchHits hits = searcher.search(img, DirectoryReader.open(FSDirectory.open(new File(indexPath + "-ro"))));
        StringBuilder sb = new StringBuilder(numReferenceObjectsUsed * 4);
        for (int j = 0; j < numReferenceObjectsUsed; j++) {
            sb.append(hits.doc(j).getValues("ro-id")[0]);
            sb.append(' ');
        }
        return scoreDocs(sb.toString(), DirectoryReader.open(FSDirectory.open(new File(indexPath))));
    }

    /**
     * Provides basic search functions ...
     *
     * @param d
     * @param indexPath
     * @return
     * @throws IOException
     */
    public TopDocs search(Document d, String indexPath) throws IOException {
        if (d.getField("ro-order") != null) // if the document already contains the information on reference object neighbourhood
            return scoreDocs(d.getValues("ro-order")[0], DirectoryReader.open(FSDirectory.open(new File(indexPath))));
        else { // if not we just create it :)
            ImageSearcher searcher = new GenericImageSearcher(numReferenceObjectsUsed, featureClass, featureFieldName);
            ImageSearchHits hits = searcher.search(d, DirectoryReader.open(FSDirectory.open(new File(indexPath + "-ro"))));
            StringBuilder sb = new StringBuilder(numReferenceObjectsUsed * 4);
            for (int j = 0; j < numReferenceObjectsUsed; j++) {
                sb.append(hits.doc(j).getValues("ro-id")[0]);
                sb.append(' ');
            }
            return scoreDocs(sb.toString(), DirectoryReader.open(FSDirectory.open(new File(indexPath))));
        }
    }

    /**
     * Scoring function based on the footrule distance.
     *
     * @param queryString
     * @param reader
     * @return
     * @throws IOException
     */
    protected TopDocs scoreDocs(String queryString, IndexReader reader) throws IOException {
        /*
        // TODO: optimize here ;) Perhaps focus on the most promising results
        StringTokenizer st = new StringTokenizer(queryString);
        int position = 0;
        HashMap<Integer, Integer> doc2score = new HashMap<Integer, Integer>(1000);
        HashMap<Integer, Integer> doc2count = new HashMap<Integer, Integer>(1000);
        int currDoc = 0;
        while (st.hasMoreTokens()) {
            TermPositions tp = reader.termPositions(new Term("ro-order", st.nextToken()));
            while (tp.next()) {
                currDoc = tp.doc();
                // System.out.println(tp.doc() + ": " + tp.nextPosition());
                if (doc2score.get(currDoc) == null) {
                    doc2score.put(currDoc, Math.abs(tp.nextPosition() - position));
                    doc2count.put(currDoc, 1);
                } else {
                    doc2score.put(currDoc, doc2score.get(currDoc) + Math.abs(tp.nextPosition() - position));
                    doc2count.put(currDoc, doc2count.get(currDoc) + 1);
                }

            }
            position++;
        }
        int currdocscore = 0;
        int maxScore = 0, minScore = (position - 1) * position;
        TreeSet<ScoreDoc> results = new TreeSet<ScoreDoc>(new ScoreDocComparator());
        for (Iterator<Integer> iterator = doc2count.keySet().iterator(); iterator.hasNext(); ) {
            currDoc = iterator.next();
            currdocscore = (position - 1) * position -  // max score ... minus actual distance.
                    (doc2score.get(currDoc) + (position - doc2count.get(currDoc)) * (position - 1));
            maxScore = Math.max(maxScore, currdocscore);
            minScore = Math.min(minScore, currdocscore);
            if (results.size() < numHits || currdocscore >= minScore) {
                results.add(new ScoreDoc(currDoc, currdocscore));
            }
        }
        while (results.size() > numHits) results.pollLast();
        return new TopDocs(Math.min(results.size(), numHits), (ScoreDoc[]) results.toArray(new ScoreDoc[results.size()]), maxScore);
        */
        throw new UnsupportedOperationException("Not supported currently in Lucene 4.0");
    }

    public int getNumHits() {
        return numHits;
    }

    public void setNumHits(int numHits) {
        this.numHits = numHits;
    }

    /**
     * Returns a reader for the index consisting the documents with the approximate search information.
     *
     * @param indexPath
     * @return
     * @throws IOException
     */
    public IndexReader getIndexReader(String indexPath) throws IOException {
        return DirectoryReader.open(FSDirectory.open(new File(indexPath)));
    }

    public ProgressIndicator getProgress() {
        return progress;
    }

    public void setProgress(ProgressIndicator progress) {
        this.progress = progress;
    }


    // ******************************************************************************
    // ** Inner class ...
    // ******************************************************************************

    private static class ScoreDocComparator implements Comparator<ScoreDoc> {
        public int compare(ScoreDoc o1, ScoreDoc o2) {
            return (int) Math.signum(o2.score - o1.score);
        }
    }
}
