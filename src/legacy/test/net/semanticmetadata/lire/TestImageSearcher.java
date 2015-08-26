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
 * Updated: 18.01.15 08:07
 */

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.imageanalysis.ScalableColor;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.impl.SimpleResult;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;
import net.semanticmetadata.lire.impl.custom.SingleNddCeddImageSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.BytesRef;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This file is part of Caliph & Emir
 * Date: 03.02.2006
 * Time: 00:29:56
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TestImageSearcher extends TestCase {
    private String[] testFiles = new String[]{"img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG",
            "img06.JPG", "img07.JPG", "img08.JPG", "img08a.JPG"};
    private String testFilesPath = "src/test/resources/images/";
    private String indexPath = "test-index-small";
    private int numsearches = 25;

    private DocumentBuilder getDocumentBuilder() {
        ChainedDocumentBuilder result = new ChainedDocumentBuilder();
        result.addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
        result.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        result.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
        result.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder());
        return result;
    }

    public void testSearch() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createScalableColorImageSearcher(50);
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        long time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(bimg, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with image, averaged on " + numsearches);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(0);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

        document = getDocumentBuilder().createDocument(bimg, testFilesPath + testFiles[0]);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
    }

    public void testFindDuplicates() throws Exception {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(50);
        ImageDuplicates imageDuplicates = searcher.findDuplicates(reader);
        if (imageDuplicates == null) {
            System.out.println("No duplicates found");
            return;
        }
        for (int i = 0; i < imageDuplicates.length(); i++) {
            System.out.println(imageDuplicates.getDuplicate(i).toString());
        }
    }


    public void testCorrelationSearch() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createAutoColorCorrelogramImageSearcher(10);
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        long time = System.currentTimeMillis();
//        for (int i = 0; i < numsearches; i++) {
        hits = searcher.search(bimg, reader);
//        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with image, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(4);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

    }

    public void testCEDDSearch() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(30);
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        long time = System.currentTimeMillis();
//        for (int i = 0; i < numsearches; i++) {
        hits = searcher.search(bimg, reader);
//        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with image, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(4);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

    }

    public void testSimpleColorHistogramSearch() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        ImageSearcher searcher = ImageSearcherFactory.createColorHistogramImageSearcher(30);
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        long time = System.currentTimeMillis();
//        for (int i = 0; i < numsearches; i++) {
        hits = searcher.search(bimg, reader);
//        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with image, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(4);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

    }

    public void testMSERHistogramSearcher() throws IOException {
        String query = "312.jpg";
        VisualWordsImageSearcher searcher = new VisualWordsImageSearcher(25, DocumentBuilder.FIELD_NAME_MSER + DocumentBuilder.FIELD_NAME_BOVW);
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("wang-index")));
        ImageSearchHits hits = searcher.search(findDoc(reader, query), reader);
        saveToHtml("mser", hits, query);
    }

    private Document findDoc(IndexReader reader, String file) throws IOException {
        for (int i = 0; i < reader.numDocs(); i++) {
            Document document = reader.document(i);
            String s = document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            if (s.endsWith(File.separator + file)) {
//                System.out.println("s = " + s);
                return document;
            }
        }
        return null;
    }

    private void saveToHtml(String prefix, ImageSearchHits hits, String queryImage) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("results-" + prefix + ".html"));
        bw.write("<html>\n" +
                "<head><title>Search Results</title></head>\n" +
                "<body bgcolor=\"#FFFFFF\">\n");
        bw.write("<h3>query</h3>\n");
        bw.write("<a href=\"file://" + queryImage + "\"><img src=\"file://" + queryImage + "\"></a><p>\n");
        bw.write("<h3>results</h3>\n");
        for (int i = 0; i < hits.length(); i++) {
            bw.write(hits.score(i) + " - <a href=\"file://" + hits.doc(i).get("descriptorImageIdentifier") + "\"><img src=\"file://" + hits.doc(i).get("descriptorImageIdentifier") + "\"></a><p>\n");
        }
        bw.write("</body>\n" +
                "</html>");
        bw.close();
    }

    public void testCachingSearcher() throws IOException {
        IndexReader ir = DirectoryReader.open(FSDirectory.open(new File("C:\\Temp\\test-100k-cedd-idx")));
        GenericFastImageSearcher is = new GenericFastImageSearcher(1, CEDD.class, true, ir);
        SingleNddCeddImageSearcher nis = new SingleNddCeddImageSearcher(ir);
        LinkedList<Document> q = new LinkedList<Document>();
        for (int i = 0; i < Math.min(1000, ir.maxDoc()); i++) {
            q.add(ir.document(i));
        }

        long time = System.currentTimeMillis();
        int count = 0;
        for (Iterator<Document> iterator = q.iterator(); iterator.hasNext(); ) {
            Document next = iterator.next();
            String id = is.search(next, ir).doc(0).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            CEDD cedd = new CEDD();
            BytesRef binaryValue = next.getBinaryValue(cedd.getFieldName());
            cedd.setByteArrayRepresentation(binaryValue.bytes, binaryValue.offset, binaryValue.length);

            String s = nis.findMostSimilar(cedd).getDocument().getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            String qID = next.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(s.equals(id) + " " + id.equals(qID) + " " + qID.equals(s));
            count++;
            if (count > 100) break;
        }
        long l = System.currentTimeMillis() - time;
        System.out.printf("Tested %d search requests on %d documents: overall time of %d:%02d, %.2f ms per search", count, ir.maxDoc(), l / (1000 * 60), (l / 1000) % 60, ((float) l / (float) count));

    }

    public void testCustomCachingSearcher() throws IOException {
        IndexReader ir = DirectoryReader.open(FSDirectory.open(new File("C:\\Temp\\test-100k-cedd-idx")));
        SingleNddCeddImageSearcher is = new SingleNddCeddImageSearcher(ir);

        LinkedList<LireFeature> q = new LinkedList<LireFeature>();
        for (int i = 0; i < ir.maxDoc(); i++) {
            Document d = ir.document(i);
            CEDD cedd = new CEDD();
            BytesRef binaryValue = d.getBinaryValue(cedd.getFieldName());
            cedd.setByteArrayRepresentation(binaryValue.bytes, binaryValue.offset, binaryValue.length);
            q.add(cedd);
        }

        long time = System.currentTimeMillis();
        int count = 0;
        for (Iterator<LireFeature> iterator = q.iterator(); iterator.hasNext(); ) {
            LireFeature next = iterator.next();
            is.findMostSimilar(next);
            count++;
            if (count > 100) break;
        }
        long l = System.currentTimeMillis() - time;
        System.out.printf("Tested %d search requests on %d documents: overall time of %d:%02d, %.2f ms per search", count, ir.maxDoc(), l / (1000 * 60), (l / 1000) % 60, ((float) l / (float) count));
    }

    public void testCachingSearcherParallel() throws IOException, InterruptedException {
        final IndexReader ir = DirectoryReader.open(FSDirectory.open(new File("C:\\Temp\\test-100k-cedd-idx")));
        SingleNddCeddImageSearcher is = new SingleNddCeddImageSearcher(ir);

        LinkedList<LireFeature> q = new LinkedList<LireFeature>();
        for (int i = 0; i < ir.maxDoc(); i++) {
            Document d = ir.document(i);
            CEDD cedd = new CEDD();
            BytesRef binaryValue = d.getBinaryValue(cedd.getFieldName());
            cedd.setByteArrayRepresentation(binaryValue.bytes, binaryValue.offset, binaryValue.length);
            q.add(cedd);
        }

        int count = 0;
        Thread[] searchers = new Thread[3];
        final LinkedBlockingQueue<LireFeature> queryQueue = new LinkedBlockingQueue<LireFeature>(1000);
        for (int i = 0; i < searchers.length; i++) {
            searchers[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    SingleNddCeddImageSearcher is = new SingleNddCeddImageSearcher(ir);
                    LireFeature remove;
                    while ((remove = queryQueue.remove()) instanceof CEDD) {
                        try {
                            is.findMostSimilar(remove);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            });
            searchers[i].start();
        }
        long time = System.currentTimeMillis();
        for (Iterator<LireFeature> iterator = q.iterator(); iterator.hasNext() && count < 1000; ) {
            LireFeature next = iterator.next();
            try {
                queryQueue.put(next);
                count++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < 8; i++) {
            queryQueue.put(new ScalableColor());
        }
        for (int i = 0; i < searchers.length; i++) {
            searchers[i].join();
        }
        long l = System.currentTimeMillis() - time;
        System.out.printf("Tested %d search requests on %d documents: overall time of %d:%02d, %.2f ms per search", count, ir.maxDoc(), l / (1000 * 60), (l / 1000) % 60, ((float) l / (float) count));
    }

    public void testCachingSearcherParallelWithBundling() throws IOException, InterruptedException {
        final IndexReader ir = DirectoryReader.open(FSDirectory.open(new File("C:\\Temp\\test-100k-cedd-idx")));

        LinkedList<LireFeature> q = new LinkedList<LireFeature>();
        for (int i = 0; i < ir.maxDoc(); i++) {
            Document d = ir.document(i);
            CEDD cedd = new CEDD();
            BytesRef binaryValue = d.getBinaryValue(cedd.getFieldName());
            cedd.setByteArrayRepresentation(binaryValue.bytes, binaryValue.offset, binaryValue.length);
            q.add(cedd);
        }

        int count = 0;
        Thread[] searchers = new Thread[4];
        final LinkedBlockingQueue<WorkItem> queryQueue = new LinkedBlockingQueue<WorkItem>(100);
        for (int i = 0; i < searchers.length; i++) {
            searchers[i] = new Thread(new Runnable() {
                @Override
                public void run() {
                    SingleNddCeddImageSearcher is = new SingleNddCeddImageSearcher(ir);
                    WorkItem remove;
                    while ((remove = queryQueue.remove()).features != null) {
                        try {
                            SimpleResult[] hits = is.findMostSimilar(remove.features);
                            for (int j = 0; j < hits.length; j++) {
                                if (hits[j].getIndexNumber() != remove.id[j]) System.err.println("oops");
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }

            });
            searchers[i].start();
        }
        long time = System.currentTimeMillis();
        LireFeature[] qarr = new LireFeature[10];
        int[] iarr = new int[10];
        int currentIndex = 0;
        int bundleCount = 0;
        Iterator<LireFeature> iterator = q.iterator();
        while (iterator.hasNext() && bundleCount < 200) {
            LireFeature next = iterator.next();
            try {
                iarr[currentIndex] = count;
                qarr[currentIndex++] = next;
                if (currentIndex >= qarr.length) { // do bundled search
                    currentIndex = 0;
                    queryQueue.put(new WorkItem(qarr.clone(), iarr.clone()));
                    bundleCount++;
                }
                count++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        for (int i = 0; i < 8; i++) {
            queryQueue.put(new WorkItem(null, null));
        }
        for (int i = 0; i < searchers.length; i++) {
            searchers[i].join();
        }
        long l = System.currentTimeMillis() - time;
        System.out.printf("Tested %d search requests on %d documents: overall time of %d:%02d, %.2f ms per search", count, ir.maxDoc(), l / (1000 * 60), (l / 1000) % 60, ((float) l / (float) count));
    }

    public void testCachingSearcherBundling() throws IOException {
        IndexReader ir = DirectoryReader.open(FSDirectory.open(new File("C:\\Temp\\test-100k-cedd-idx")));
        SingleNddCeddImageSearcher is = new SingleNddCeddImageSearcher(ir);

        LinkedList<LireFeature> q = new LinkedList<LireFeature>();
        for (int i = 0; i < ir.maxDoc(); i++) {
            Document d = ir.document(i);
            CEDD cedd = new CEDD();
            BytesRef binaryValue = d.getBinaryValue(cedd.getFieldName());
            cedd.setByteArrayRepresentation(binaryValue.bytes, binaryValue.offset, binaryValue.length);
            q.add(cedd);
        }

        long time = System.currentTimeMillis();
        int count = 0;
        LireFeature[] qarr = new LireFeature[10];
        int currentIndex = 0;
        for (Iterator<LireFeature> iterator = q.iterator(); iterator.hasNext(); ) {
            LireFeature next = iterator.next();
            qarr[currentIndex++] = next;
            if (currentIndex >= qarr.length) { // do bundled search
                currentIndex = 0;
                is.findMostSimilar(qarr);
            }
            count++;
            if (count > 999 & currentIndex == 0) break;
        }
        long l = System.currentTimeMillis() - time;
        System.out.printf("Tested %d search requests on %d documents: overall time of %d:%02d, %.2f ms per search", count, ir.maxDoc(), l / (1000 * 60), (l / 1000) % 60, ((float) l / (float) count));
    }

}

class WorkItem {
    LireFeature[] features;
    int[] id;

    public WorkItem(LireFeature[] features, int[] id) {
        this.features = features;
        this.id = id;
    }
}
