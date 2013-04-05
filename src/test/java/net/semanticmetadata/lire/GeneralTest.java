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

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.filter.LsaFilter;
import net.semanticmetadata.lire.filter.RerankFilter;
import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created with IntelliJ IDEA.
 * User: mlux
 * Date: 29.11.12
 * Time: 13:53
 */
public class GeneralTest extends TestCase {
    private String[] testFiles = new String[]{
            "img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG",
            "img06.JPG", "img07.JPG", "img08.JPG", "error.jpg"};
    private String testFilesPath = "./src/test/resources/images/";
    private String indexPath = "test-index";
    private String testExtensive = "./wang-1000";
    private Class[] featureClasses = new Class[]{
            CEDD.class, FCTH.class, JCD.class, AutoColorCorrelogram.class, ColorLayout.class, EdgeHistogram.class,
            Gabor.class, JpegCoefficientHistogram.class,
            ScalableColor.class, SimpleColorHistogram.class, Tamura.class, FuzzyColorHistogram.class, PHOG.class
    };

    private DocumentBuilder[] builders = new DocumentBuilder[]{
            DocumentBuilderFactory.getCEDDDocumentBuilder(),
            DocumentBuilderFactory.getFCTHDocumentBuilder(),
            DocumentBuilderFactory.getJCDDocumentBuilder(),
            DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder(),
            DocumentBuilderFactory.getColorLayoutBuilder(),
            DocumentBuilderFactory.getEdgeHistogramBuilder(),  // 5
            DocumentBuilderFactory.getGaborDocumentBuilder(),
            DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder(), // 7
            DocumentBuilderFactory.getScalableColorBuilder(),
            DocumentBuilderFactory.getColorHistogramDocumentBuilder(),
            DocumentBuilderFactory.getTamuraDocumentBuilder(),              // 10
            DocumentBuilderFactory.getOpponentHistogramDocumentBuilder(),   // 11
            DocumentBuilderFactory.getJointHistogramDocumentBuilder(),       // 12
            new GenericDocumentBuilder(PHOG.class, "phog")
    };

    private ImageSearcher[] searchers = new ImageSearcher[]{
            ImageSearcherFactory.createCEDDImageSearcher(10),
            ImageSearcherFactory.createFCTHImageSearcher(10),
            ImageSearcherFactory.createJCDImageSearcher(10),
            ImageSearcherFactory.createAutoColorCorrelogramImageSearcher(10),
            ImageSearcherFactory.createColorLayoutImageSearcher(10),
            ImageSearcherFactory.createEdgeHistogramImageSearcher(10),
            ImageSearcherFactory.createGaborImageSearcher(10),
            ImageSearcherFactory.createJpegCoefficientHistogramImageSearcher(10),
            ImageSearcherFactory.createScalableColorImageSearcher(10),
            ImageSearcherFactory.createColorHistogramImageSearcher(10),
            ImageSearcherFactory.createTamuraImageSearcher(10),
            ImageSearcherFactory.createOpponentHistogramSearcher(10),
            ImageSearcherFactory.createJointHistogramImageSearcher(10),
            new GenericFastImageSearcher(10, PHOG.class, "phog")
    };

    public void testExtractionAndMetric() throws IOException, IllegalAccessException, InstantiationException {
        for (Class c : featureClasses) {
            LireFeature lireFeature = (LireFeature) c.newInstance();
            LireFeature tmpLireFeature = (LireFeature) c.newInstance();
            for (String file : testFiles) {
                lireFeature.extract(ImageIO.read(new FileInputStream(testFilesPath + file)));
                float delta = 0.0000f;
                assertEquals(lireFeature.getDistance(lireFeature), 0, delta);
//                tmpLireFeature.setStringRepresentation(lireFeature.getStringRepresentation());
//                assertEquals(lireFeature.getDistance(tmpLireFeature), 0, delta);
                tmpLireFeature.setByteArrayRepresentation(lireFeature.getByteArrayRepresentation());
                assertEquals(lireFeature.getDistance(tmpLireFeature), 0, delta);
                tmpLireFeature.setByteArrayRepresentation(lireFeature.getByteArrayRepresentation(), 0, lireFeature.getByteArrayRepresentation().length);
                assertEquals(lireFeature.getDistance(tmpLireFeature), 0, delta);
            }
        }
    }

    public void testCreateAndSearchSmallIndex() throws IOException {
        for (int i = 0, buildersLength = builders.length; i < buildersLength; i++) {
            DocumentBuilder b = builders[i];
            // create an index with a specific builder:
            IndexWriter iw = LuceneUtils.createIndexWriter(indexPath + "-small", true);
            for (String identifier : testFiles) {
                Document doc = b.createDocument(new FileInputStream(testFilesPath + identifier), identifier);
                iw.addDocument(doc);
            }
            iw.close();

            ImageSearcher s = searchers[i];
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath + "-small")));
            for (int k = 0; k < reader.maxDoc(); k++) {
                Document query = reader.document(k);
                ImageSearchHits hits = s.search(query, reader);
                for (int y = 0; y < hits.length(); y++) {
                    Document result = hits.doc(y);
                    if (y == 0) {
                        // check if the first result is the query:
                        assertEquals(result.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0].equals(query.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), true);
                    } else {
                        // check if they are ordered by distance:
                        assertEquals(hits.score(y) < hits.score(y - 1), true);
                    }
                }
            }
        }
    }

    public void testIndexLarge() throws IOException {
//        ArrayList<String> images = FileUtils.getAllImages(new File("C:\\Temp\\testImagelogos"), true);
        ArrayList<String> images = FileUtils.getAllImages(new File("C:\\Java\\Projects\\LireSVN\\testdata\\flickr-10000"), false);
        IndexWriter iw = LuceneUtils.createIndexWriter("index-large", true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        // select one feature for the large index:
        int featureIndex = 13;
        int count = 0;
        long ms = System.currentTimeMillis();
        DocumentBuilder builder = new ChainedDocumentBuilder();
        ((ChainedDocumentBuilder) builder).addBuilder(builders[featureIndex]);
//        ((ChainedDocumentBuilder) builder).addBuilder(builders[0]);
        for (Iterator<String> iterator = images.iterator(); iterator.hasNext(); ) {
            count++;
            if (count > 100 && count % 500 == 0) {
                System.out.println(count + " files indexed. " + (System.currentTimeMillis() - ms) / (count) + " ms per file");
            }
            String file = iterator.next();
            try {
                // try to trim the image first ....
//                BufferedImage img = ImageUtils.trimWhiteSpace(ImageIO.read(new FileInputStream(file)));
//                iw.addDocument(builder.createDocument(img, file));
                iw.addDocument(builder.createDocument(new FileInputStream(file), file));
            } catch (Exception e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        iw.close();
    }

    public void testPerformance() throws IOException {
        System.out.println(" ****************** CEDD OLD ****************** ");
        indexFiles("C:\\Temp\\images1", "index-large-new", 0, true);
    }

    /**
     * There was an error that images with the same score but different documents in the index
     * were not included in the result list. Here's the test for that.
     */
    public void testDuplicatesInIndex() throws IOException {
        indexFiles("src\\test\\resources\\images", "index-large-new", 0, true);
        indexFiles("src\\test\\resources\\images", "index-large-new", 0, false);
        indexFiles("src\\test\\resources\\images", "index-large-new", 0, false);

        ImageSearcher s = searchers[0];
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("index-large-new")));
        Document query = reader.document(0);
        ImageSearchHits hits = s.search(query, reader);
        FileUtils.saveImageResultsToPng("duplicate_", hits, query.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
    }

    private void indexFiles(String dir, String index, int featureIndex, boolean createNewIndex) throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(dir), true);
        IndexWriter iw = LuceneUtils.createIndexWriter(index, createNewIndex, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        // select one feature for the large index:
        int count = 0;
        long ms = System.currentTimeMillis();
        DocumentBuilder builder = new ChainedDocumentBuilder();
        ((ChainedDocumentBuilder) builder).addBuilder(builders[featureIndex]);
//        ((ChainedDocumentBuilder) builder).addBuilder(builders[0]);
        for (Iterator<String> iterator = images.iterator(); iterator.hasNext(); ) {
            count++;
            if (count > 100 && count % 5000 == 0) {
                System.out.println(count + " files indexed. " + (System.currentTimeMillis() - ms) / (count) + " ms per file");
            }
            String file = iterator.next();
            try {
                iw.addDocument(builder.createDocument(new FileInputStream(file), file));
            } catch (Exception e) {
                System.err.println("Error: " + e.getMessage());
            }
        }
        iw.close();
    }

    public void testSearchIndexLarge() throws IOException {

        for (int i = 0; i < 10; i++) {
            int queryDocID = (int) (Math.random() * 800);
//            queryDocID = 877 * (i + 1);
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("index-large")));
            // select one feature for the large index:
            int featureIndex = 13;
            int count = 0;
            long ms = System.currentTimeMillis();
            ImageSearchHits hits = searchers[featureIndex].search(reader.document(queryDocID), reader);
            for (int j = 0; j < hits.length(); j++) {
                String fileName = hits.doc(j).getValues(
                        DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                System.out.println(hits.score(j) + ": \t" + fileName);
            }
//        FileUtils.saveImageResultsToHtml("GeneralTest_testSearchIndexLarge_", hits, reader.document(10).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
            FileUtils.saveImageResultsToPng("GeneralTest_testSearchIndexLarge_" + i + "_", hits, reader.document(queryDocID).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
        }
    }

    public void testSearchRunTime() throws IOException {
        int queryDocID;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("index-large-new")));
        int featureIndex = 0;
        ImageSearchHits hits = searchers[featureIndex].search(reader.document(0), reader);
        hits = searchers[featureIndex].search(reader.document(1), reader);
        long ms = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) {
            queryDocID = i;
            // select one feature for the large index:
            hits = searchers[featureIndex].search(reader.document(queryDocID), reader);
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("ms = " + ms / 100);
    }

    public void testRerankFilters() throws IOException {
        int queryDocID = (int) (Math.random() * 10000);
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("index-large")));
        // select one feature for the large index:
        int featureIndex = 4;
        int count = 0;
        long ms = System.currentTimeMillis();
        ImageSearchHits hits = searchers[featureIndex].search(reader.document(queryDocID), reader);
        RerankFilter rerank = new RerankFilter(featureClasses[0], DocumentBuilder.FIELD_NAME_CEDD);
        LsaFilter lsa = new LsaFilter(featureClasses[0], DocumentBuilder.FIELD_NAME_CEDD);
        FileUtils.saveImageResultsToPng("GeneralTest_rerank_0_old", hits, reader.document(queryDocID).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
        hits = rerank.filter(hits, reader.document(queryDocID));
        FileUtils.saveImageResultsToPng("GeneralTest_rerank_1_new", hits, reader.document(queryDocID).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
        hits = lsa.filter(hits, reader.document(queryDocID));
        FileUtils.saveImageResultsToPng("GeneralTest_rerank_2_lsa", hits, reader.document(queryDocID).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
    }

}
