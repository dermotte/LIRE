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
 * Updated: 07.11.14 14:16
 */

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.filter.LsaFilter;
import net.semanticmetadata.lire.filter.RerankFilter;
import net.semanticmetadata.lire.impl.BitSamplingImageSearcher;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.util.BytesRef;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
            "img06.JPG", "img07.JPG", "img08.JPG", "error.jpg", "91561.lire.jpg", "91561.jpg"};
    private String testFilesPath = "./src/test/resources/images/";
    private String indexPath = "test-index";
    private String testExtensive = "./testdata/wang-1000";
    public Class[] featureClasses = new Class[]{
            CEDD.class,
            FCTH.class,
            JCD.class,
            AutoColorCorrelogram.class,
            ColorLayout.class,
            EdgeHistogram.class,
            Gabor.class,
            JpegCoefficientHistogram.class,
            ScalableColor.class,
            SimpleColorHistogram.class,
            OpponentHistogram.class,
            LocalBinaryPatterns.class,
            RotationInvariantLocalBinaryPatterns.class,
            BinaryPatternsPyramid.class,
            LuminanceLayout.class,
            Tamura.class,
            FuzzyColorHistogram.class,
            PHOG.class
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
                System.out.println(c.getName() + ": " + file);
                BufferedImage image = ImageIO.read(new FileInputStream(testFilesPath + file));
//                image = ImageUtils.trimWhiteSpace(image);
                lireFeature.extract(image);
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
                doc.add(new StoredField("video_file", "surgery1.mp4"));
                doc.add(new StoredField("timestamp", "25"));
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
                        System.out.println(result.getValues("video_file")[0]);
                    } else {
                        // check if they are ordered by distance:
                        assertEquals(hits.score(y) < hits.score(y - 1), true);
                    }
                }
            }
        }
    }

    public void testReadIndex() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("ucid-index-39997508")));
        for (int k = 0; k < reader.maxDoc(); k++) {
            Document document = reader.document(k);
            BytesRef b = document.getField("featureCEDDLoDe_Hist").binaryValue();
            double[] doubles = SerializationUtils.toDoubleArray(b.bytes, b.offset, b.length);
            if (document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0].endsWith("00008.png"))
                System.out.println(Arrays.toString(doubles));
        }

        // check lucene tuorials and docs
        IndexSearcher is = new IndexSearcher(reader);
        TopDocs td = is.search(new TermQuery(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, "")), 10);
        for (int i = 0; i < td.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = td.scoreDocs[i];
            Document document = reader.document(scoreDoc.doc);
        }
    }

    public void testIndexLarge() throws IOException {
//        ArrayList<String> images = FileUtils.getAllImages(new File("C:\\Temp\\testImagelogos"), true);
        ArrayList<String> images = FileUtils.getAllImages(new File("testdata/UCID"), false);
        IndexWriter iw = LuceneUtils.createIndexWriter("index-large", true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        // select one feature for the large index:
        int featureIndex = 0;
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
            int featureIndex = 0;
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

    public void testClassify() throws IOException {
        boolean weightByRank = true;
        String[] classes = {"2012", "beach", "food", "london", "music", "nature", "people", "sky", "travel", "wedding"};
        int k = 50;
        // CONFIG
        String fieldName = DocumentBuilder.FIELD_NAME_COLORLAYOUT;
        LireFeature feature = new ColorLayout();
        String indexPath = "E:\\acmgc-cl-idx";
        System.out.println("Tests for feature " + fieldName + " with k=" + k + " - weighting by rank sum: " + weightByRank);
        System.out.println("========================================");
        HashMap<String, Integer> tag2count = new HashMap<String, Integer>(k);
        HashMap<String, Double> tag2weight = new HashMap<String, Double>(k);
        int c = 9;   // used for just one class ...
//        for (int c = 0; c < 10; c++) {
        String classIdentifier = classes[c];
        String listFiles = "D:\\DataSets\\Yahoo-GC\\test\\" + classIdentifier + ".txt";

        // INIT
        int[] confusion = new int[10];
        Arrays.fill(confusion, 0);
        HashMap<String, Integer> class2id = new HashMap<String, Integer>(10);
        for (int i = 0; i < classes.length; i++)
            class2id.put(classes[i], i);

        BufferedReader br = new BufferedReader(new FileReader(listFiles));
        String line;
        IndexReader ir = DirectoryReader.open(MMapDirectory.open(new File(indexPath)));
        // in-memory linear search
//            ImageSearcher bis = new GenericFastImageSearcher(k, feature.getClass(), fieldName, true, ir);
        // hashing based searcher
        BitSamplingImageSearcher bis = new BitSamplingImageSearcher(k, fieldName, fieldName + "_hash", feature, 1000);
        ImageSearchHits hits;
        int count = 0, countCorrect = 0;
        long ms = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {
            try {
                tag2count.clear();
                tag2weight.clear();
                hits = bis.search(ImageIO.read(new File(line)), ir);
                // set tag weights and counts.
                for (int l = 0; l < k; l++) {
                    String tag = getTag(hits.doc(l));
                    if (tag2count.get(tag) == null) tag2count.put(tag, 1);
                    else tag2count.put(tag, tag2count.get(tag) + 1);
                    if (weightByRank) {
                        if (tag2weight.get(tag) == null) tag2weight.put(tag, (double) l);
                        else tag2weight.put(tag, (double) l + tag2weight.get(tag));
                    } else {
                        if (tag2weight.get(tag) == null) tag2weight.put(tag, Double.valueOf(hits.score(l)));
                        else tag2weight.put(tag, (double) l + hits.score(l));
                    }
                }
                // find class:
                int maxCount = 0, maxima = 0;
                String classifiedAs = null;
                for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                    String tag = tagIterator.next();
                    if (tag2count.get(tag) > maxCount) {
                        maxCount = tag2count.get(tag);
                        maxima = 1;
                        classifiedAs = tag;
                    } else if (tag2count.get(tag) == maxCount) {
                        maxima++;
                    }
                }
                // if there are two or more classes with the same number of results, then we take a look at the weights.
                // else the class is alread given in classifiedAs.
                if (maxima > 1) {
                    double minWeight = Double.MAX_VALUE;
                    for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                        String tag = tagIterator.next();
                        if (tag2weight.get(tag) < minWeight) {
                            minWeight = tag2weight.get(tag);
                            classifiedAs = tag;
                        }
                    }
                }
//                    if (tag2.equals(tag3)) tag1 = tag2;
                count++;
                if (classifiedAs.equals(classIdentifier)) countCorrect++;
                // confusion:
                confusion[class2id.get(classifiedAs)]++;
//                    System.out.printf("%10s (%4.3f, %10d, %4d)\n", classifiedAs, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
//            System.out.println("Results for class " + classIdentifier);
        System.out.printf("Class\tAvg. Precision\tCount Test Images\tms per test\n");
        System.out.printf("%s\t%4.5f\t%10d\t%4d\n", classIdentifier, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);
        System.out.printf("Confusion\t");
//            for (int i = 0; i < classes.length; i++) {
//                System.out.printf("%s\t", classes[i]);
//            }
//            System.out.println();
        for (int i = 0; i < classes.length; i++) {
            System.out.printf("%d\t", confusion[i]);
        }
        System.out.println();
//        }
    }

    private String getTag(Document d) {
        StringBuilder ab = new StringBuilder(d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0].replace("E:\\I:\\ACM_complete_dataset\\", ""));
        return ab.substring(0, ab.indexOf("\\")).toString();
    }

    public void testReUse() throws IOException, IllegalAccessException, InstantiationException {
        ArrayList<String> testFiles = FileUtils.getAllImages(new File("testdata/ferrari"), true);
        for (Class c : featureClasses) {
            LireFeature f1 = (LireFeature) c.newInstance();
            System.out.println(c.getName());
            for (String testFile : testFiles) {
                f1.extract(ImageIO.read(new File(testFile)));
                LireFeature f2 = (LireFeature) c.newInstance();
                f2.extract(ImageIO.read(new File(testFile)));
//                System.out.println(Arrays.toString(f1.getDoubleHistogram()));
//                System.out.println(Arrays.toString(f2.getDoubleHistogram()));
                assertEquals(f2.getDistance(f1), 0d, 0.000000001);
                f2.setByteArrayRepresentation(f1.getByteArrayRepresentation());
                assertEquals(f2.getDistance(f1), 0d, 0.000000001);
                byte[] tmp = new byte[1024*100];
                Arrays.fill(tmp, (byte) 0x000F);
                byte[] bytes = f1.getByteArrayRepresentation();
                System.arraycopy(bytes, 0, tmp, 12, bytes.length);
                f2.setByteArrayRepresentation(tmp, 12, bytes.length);
                assertEquals(f2.getDistance(f1), 0d, 0.000000001);
            }
        }
    }


}
