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
import net.semanticmetadata.lire.impl.CEDDDocumentBuilder;
import net.semanticmetadata.lire.impl.CEDDImageSearcher;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * This file is part of LIRe
 * Date: 31.01.2006
 * Time: 23:59:45
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class RuntimeTest extends TestCase {
    private String[] testFiles = new String[]{"img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG",
            "img06.JPG", "img07.JPG", "img08.JPG", "img08a.JPG", "error.jpg", "P�ginas de 060305_b_P�gina_1_Imagem_0004_P�gina_08_Imagem_0002.jpg"};
    private String testFilesPath = "./lire/src/test/resources/images/";
    private String indexPath = "test-index";
    private String testExtensive = "./lire/wang-data-1000";

    public void testCreateIndex() throws IOException {
        ChainedDocumentBuilder builder = new ChainedDocumentBuilder();
        builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
        builder.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());

        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath + "-small", true);
        for (String identifier : testFiles) {
            System.out.println("Indexing file " + identifier);
            Document doc = builder.createDocument(new FileInputStream(testFilesPath + identifier), identifier);
            iw.addDocument(doc);
        }
        iw.optimize();
        iw.close();
    }

    public void testCreateCorrelogramIndex() throws IOException {
        String[] testFiles = new String[]{"img01.jpg", "img02.jpg", "img03.jpg", "img04.jpg", "img05.jpg", "img06.jpg", "img07.jpg", "img08.jpg", "img09.jpg", "img10.jpg"};
        String testFilesPath = "./lire/src/test/resources/small/";

        DocumentBuilder builder = DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder();
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath + "-small", true);
        long ms = System.currentTimeMillis();
        for (String identifier : testFiles) {
            Document doc = builder.createDocument(new FileInputStream(testFilesPath + identifier), identifier);
            iw.addDocument(doc);
        }
        System.out.println("Time taken: " + ((System.currentTimeMillis() - ms) / testFiles.length) + " ms");
        iw.optimize();
        iw.close();
    }

    public void testCreateCEDDIndex() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File("c:/temp/flickrphotos"), true);

        ChainedDocumentBuilder builder = new ChainedDocumentBuilder();
        builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
        builder.addBuilder(new CEDDDocumentBuilder());
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath + "-cedd", true);
        int count = 0;
        long ms = System.currentTimeMillis();
        for (String identifier : images) {
            try {
                Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
                iw.addDocument(doc);
            } catch (Exception e) {
                System.err.print("\n ;-( ");//e.printStackTrace();
            }
            count++;
            if (count % 100 == 0) System.out.print((100 * count) / images.size() + "% ");
        }
        System.out.println("Time taken: " + ((System.currentTimeMillis() - ms) / testFiles.length) + " ms");
        iw.optimize();
        iw.close();
    }

    public void testCEDDSearch() throws IOException {
        int numsearches = 10;
        IndexReader reader = IndexReader.open(FSDirectory.open(new File("test-index-cedd")));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);

        // This is the new, shiny and fast one ...
        ImageSearcher searcher = new CEDDImageSearcher(30);

        // This is the old and slow one.
//        ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(30);
        FileInputStream imageStream = new FileInputStream("wang-1000/0.jpg");
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        long time = System.currentTimeMillis();
        for (int i = 0; i < numsearches; i++) {
            hits = searcher.search(bimg, reader);
        }
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

//    public void testCreateExtensiveIndex() throws IOException {
//        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
//        indexFiles(images);
//    }

    /**
     * Tests the runtime for creating an index based on the Wang data set.
     *
     * @throws IOException
     */
    public void testCreateBigIndex() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        indexFiles("ColorHist: ", images, DocumentBuilderFactory.getColorHistogramDocumentBuilder(), indexPath + "-extensive");
        indexFiles("CEDD: ", images, DocumentBuilderFactory.getCEDDDocumentBuilder(), indexPath + "-extensive");
        indexFiles("ColorHist: ", images, DocumentBuilderFactory.getColorHistogramDocumentBuilder(), indexPath + "-extensive");
        indexFiles("ACC: ", images, DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder(), indexPath + "-extensive");
        indexFiles("FCTH: ", images, DocumentBuilderFactory.getFCTHDocumentBuilder(), indexPath + "-extensive");
        indexFiles("Gabor: ", images, DocumentBuilderFactory.getGaborDocumentBuilder(), indexPath + "-extensive");
        indexFiles("Tamura: ", images, DocumentBuilderFactory.getTamuraDocumentBuilder(), indexPath + "-extensive");
//        indexFiles("MPEG7: ", images, DocumentBuilderFactory.getExtensiveDocumentBuilder(), indexPath + "-extensive");
        indexFiles("All: ", images, DocumentBuilderFactory.getFullDocumentBuilder(), indexPath + "-extensive");
    }

    private void indexFiles(String prefix, ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        System.out.println(">> Indexing " + images.size() + " files.");
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
            count++;
            if (count % 100 == 0) System.out.print((100 * count) / images.size() + "% ");
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;
        System.out.println("");
        System.out.println(prefix + sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        // iw.optimize();
        iw.close();
    }
}
