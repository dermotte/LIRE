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
 * ~~~~~~~~~~~~~~~~~~~~
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package liredemo.flickr;

import junit.framework.TestCase;
import liredemo.indexing.MetadataBuilder;
import liredemo.indexing.ParallelIndexer;
import net.semanticmetadata.lire.*;
import net.semanticmetadata.lire.imageanalysis.bovw.SurfFeatureHistogramBuilder;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * User: mlux
 * Date: 25.10.11
 * Time: 09:33
 */
public class TestParallelIndexer extends TestCase {
    //    private String filePath = "./flickr-10000";
    private String filePath = "C:\\Temp\\flickrphotos";
    private String indexPath = "./index-xtra-flickrbig";

    public void testIndexing() throws IOException {
        List<String> allImages = FileUtils.getAllImages(new File(filePath), true);
        System.out.println("Found " + allImages.size() + " files.");
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true);
//        ParallelIndexer pix = new ParallelIndexer(allImages, new MirFlickrDocumentBuilder());
        ParallelIndexer pix = new ParallelIndexer(allImages, new MetadataBuilder());
        new Thread(pix).start();
        Document doc;
        javax.swing.ProgressMonitor pm = new javax.swing.ProgressMonitor(null, "Indexing ...", "", 0, allImages.size());
        int count = 0;
        while ((doc = pix.getNext()) != null) {
            iw.addDocument(doc);
            pm.setProgress(++count);
            pm.setNote(count + " documents finished");
        }
        iw.close();
    }

    public void testBovwIndexing() throws IOException {
//        SurfFeatureHistogramBuilder indexer = new SurfFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File("./index-mirflickr"))), 8000, 2000);
        SurfFeatureHistogramBuilder indexer = new SurfFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 8000, 2000);
//        LocalFeatureHistogramBuilder.DELETE_LOCAL_FEATURES = true;
        indexer.setProgressMonitor(new javax.swing.ProgressMonitor(null, "", "", 0, 100));
        indexer.index();
    }

    /**
     * Delete all Fields besides the ones needed.
     *
     * @throws IOException
     */
    public void testReduceIndex() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File("./index-mirflickr")));
        IndexWriter writer = LuceneUtils.createIndexWriter(FSDirectory.open(new File("./mirflickr-data-vw")), true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
//        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
//        IndexWriter writer = LuceneUtils.createIndexWriter(indexPath + "-reduced", true);
        int maxDocs = reader.maxDoc();
        Document d;
        for (int i = 0; i < maxDocs; i++) {
            if (!reader.isDeleted(0)) {
                d = reader.document(i);
                Document writeDoc = new Document();
                writeDoc.add(d.getFieldable(DocumentBuilder.FIELD_NAME_CEDD));
//                writeDoc.add(d.getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER));
                writeDoc.add(d.getFieldable("tags"));
                writeDoc.add(d.getFieldable(DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS));
                writer.addDocument(writeDoc);
            }
        }
        writer.close();
    }

    public void testSearchTime() throws IOException {
        ImageSearcher ceddImageSearcher = new VisualWordsImageSearcher(100, DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS);
//        ImageSearcher ceddImageSearcher = ImageSearcherFactory.createCEDDImageSearcher(100);
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
//        IndexReader reader = IndexReader.open(new RAMDirectory(FSDirectory.open(new File(indexPath + "-reduced"))));
        System.out.println("reader.maxDoc() = " + reader.maxDoc());
        for (int i = 0; i < 10; i++) {
            long ms = System.currentTimeMillis();
            ceddImageSearcher.search(reader.document(0), reader);
            System.out.println("s = " + (double) (System.currentTimeMillis() - ms) / 1000d);
        }
    }

    public void testIndexMirflickr() throws IOException {
        List<String> allImages = new LinkedList<String>();
        for (int i = 1; i <= 25000; i++)
            allImages.add("c:/Temp/mirflickr/im" + i + ".jpg");
        System.out.println("Found " + allImages.size() + " files.");
        IndexWriter iw = LuceneUtils.createIndexWriter("./index-mirflickr", true);
        ParallelIndexer pix = new ParallelIndexer(allImages, new MirFlickrDocumentBuilder());
        new Thread(pix).start();
        Document doc;
        javax.swing.ProgressMonitor pm = new javax.swing.ProgressMonitor(null, "Indexing ...", "", 0, allImages.size());
        int count = 0;
        while ((doc = pix.getNext()) != null) {
            iw.addDocument(doc);
            pm.setProgress(++count);
            pm.setNote(count + " documents finished");
        }
        iw.close();

    }

    public void testMirFlickrSearch() throws IOException {
        float avgPrecision = 0f;
        float nullHits = 0f;
        int numDocsAll = 1000;
        for (int docId = 0; docId < numDocsAll; docId++) {
            int docNumber = docId;
//            ImageSearcher imageSearcher = new VisualWordsImageSearcher(6, DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS);
            ImageSearcher imageSearcher = ImageSearcherFactory.createCEDDImageSearcher(10);
            IndexReader reader = IndexReader.open(FSDirectory.open(new File("./index-mirflickr")));
//        IndexReader reader = IndexReader.open(new RAMDirectory(FSDirectory.open(new File(indexPath + "-reduced"))));
//        System.out.println("reader.maxDoc() = " + reader.maxDoc());
            ImageSearchHits hits = imageSearcher.search(reader.document(docNumber), reader);
//            LsaFilter lsa = new LsaFilter(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//            lsa.filter(hits, reader.document(docNumber));
            HashMap<String, Float> hist = new HashMap<String, Float>(250);
//            System.out.println("query tags: " + reader.document(docNumber).getValues("tags")[0]);
            for (int i = 0; i < hits.length(); i++) {
                Document doc = hits.doc(i);
                String tags = doc.getValues("tags")[0];
                String[] t = tags.split("\\s");
                for (int j = 0; j < t.length; j++) {
                    if (t[j].length() > 1 && j > 0) {
                        if (hist.containsKey(t[j])) {
//                            hist.put(t[j], hist.get(t[j]) + 1f / (float) Math.max(j / 2f, 1f));
                            hist.put(t[j], hist.get(t[j]) + 1f);
                        } else {
//                            hist.put(t[j], 1f / ((float) Math.max(j / 2f, 1f)));
                            hist.put(t[j], 1f);
                        }
                    }
                }
            }
            int countHits = 0;
            // IDF
//            for (Iterator<String> iterator = hist.keySet().iterator(); iterator.hasNext(); ) {
//                String s = iterator.next();
//                int docFreq = reader.docFreq(new Term("tags", s));
//                hist.put(s, (float) (hist.get(s)*(Math.log(25000f/(float)docFreq))));
//            }
            for (int c = 0; c < 10; c++) {
                String t = getMaxItem(hist);
                String s = "";
                if (t != null && reader.document(docNumber).getValues("tags") != null
                        && reader.document(docNumber).getValues("tags").length > 0
                        && reader.document(docNumber).getValues("tags")[0].indexOf(t) > -1) {
                    s = "* ";
                    countHits++;
                }
//                System.out.println(s + t + " ("+hist.get(t)+")");
                hist.remove(t);
            }
//            System.out.println("countHits = " + countHits);
            avgPrecision += (float) countHits / 10f;
            if (countHits < 1) nullHits++;
        }
        System.out.println("avgPrecision = " + avgPrecision / (float) numDocsAll);
        System.out.println("nullHits = " + nullHits / (float) numDocsAll);
    }

    private String getMaxItem(HashMap<String, Float> map) {
        String max = null;
        float tmp = 0f;
        for (Iterator<String> iterator = map.keySet().iterator(); iterator.hasNext(); ) {
            String next = iterator.next();
            if (max == null || map.get(next) > tmp) {
                max = next;
                tmp = map.get(max);
            }
        }
        return max;
    }

    class MirFlickrDocumentBuilder extends ChainedDocumentBuilder {
        MirFlickrDocumentBuilder() {
            super();
            super.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
            super.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
            super.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
            super.addBuilder(new SurfDocumentBuilder());
        }

        @Override
        public Document createDocument(BufferedImage image, String identifier) throws FileNotFoundException {
            Document d = super.createDocument(image, identifier);
            // read the tags and add them to the document.
            StringBuilder file = new StringBuilder(identifier);
            StringBuilder tagFile = new StringBuilder();
            tagFile.append(file.subSequence(0, file.lastIndexOf("im")));
            tagFile.append("meta/tags/tags");
            tagFile.append(file.subSequence(file.lastIndexOf("im") + 2, file.lastIndexOf(".")));
            tagFile.append(".txt");
            try {
                StringBuilder tags = new StringBuilder(256);
                BufferedReader br = new BufferedReader(new FileReader(tagFile.toString()));
                String t = null;
                while ((t = br.readLine()) != null) {
                    tags.append(t);
                    tags.append(' ');
                }
                d.add(new Field("tags", tags.toString(), Field.Store.YES, Field.Index.ANALYZED));
            } catch (FileNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            return d;
        }
    }
}
