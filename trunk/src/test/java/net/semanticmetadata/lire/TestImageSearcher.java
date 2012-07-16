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

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;

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
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
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
            System.out.println(hits.score(i) + ": " + hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(0);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

        document = getDocumentBuilder().createDocument(bimg, testFilesPath + testFiles[0]);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < 5; i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
    }

    public void testFindDuplicates() throws Exception {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
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
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
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
            System.out.println(hits.score(i) + ": " + hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(4);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

    }

    public void testCEDDSearch() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
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
            System.out.println(hits.score(i) + ": " + hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(4);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

    }

    public void testSimpleColorHistogramSearch() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
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
            System.out.println(hits.score(i) + ": " + hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }
        Document document = hits.doc(4);
        time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(document, reader);
        }
        time = System.currentTimeMillis() - time;
        System.out.println(((float) time / (float) numsearches) + " ms per search with document, averaged on " + numsearches);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + hits.doc(i).getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

    }

    public void testMSERHistogramSearcher() throws IOException {
        String query = "312.jpg";
        VisualWordsImageSearcher searcher = new VisualWordsImageSearcher(25, DocumentBuilder.FIELD_NAME_MSER_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS);
        IndexReader reader = IndexReader.open(FSDirectory.open(new File("wang-index")));
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

}
