package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.filter.LsaFilter;
import net.semanticmetadata.lire.filter.RerankFilter;
import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
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
            ScalableColor.class, SimpleColorHistogram.class, Tamura.class, FuzzyColorHistogram.class
    };

    private DocumentBuilder[] builders = new DocumentBuilder[]{
            DocumentBuilderFactory.getCEDDDocumentBuilder(),
            DocumentBuilderFactory.getFCTHDocumentBuilder(),
            DocumentBuilderFactory.getJCDDocumentBuilder(),
            DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder(),
            DocumentBuilderFactory.getColorLayoutBuilder(),
            DocumentBuilderFactory.getEdgeHistogramBuilder(),
            DocumentBuilderFactory.getGaborDocumentBuilder(),
            DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder(),
            DocumentBuilderFactory.getScalableColorBuilder(),
            DocumentBuilderFactory.getColorHistogramDocumentBuilder(),
            DocumentBuilderFactory.getTamuraDocumentBuilder()
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
            ImageSearcherFactory.createTamuraImageSearcher(10)
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
        ArrayList<String> images = FileUtils.getAllImages(new File("C:\\Java\\Projects\\LireSVN\\testdata\\flickr-10000"), false);
        IndexWriter iw = LuceneUtils.createIndexWriter("index-large", true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        // select one feature for the large index:
        int featureIndex = 4;
        int count = 0;
        long ms = System.currentTimeMillis();
        DocumentBuilder builder = new ChainedDocumentBuilder();
        ((ChainedDocumentBuilder) builder).addBuilder(builders[featureIndex]);
        ((ChainedDocumentBuilder) builder).addBuilder(builders[0]);
        for (Iterator<String> iterator = images.iterator(); iterator.hasNext(); ) {
            count++;
            if (count > 100 && count % 500 == 0) {
                System.out.println(count + " files indexed. " + (System.currentTimeMillis() - ms) / (count) + " ms per file");
            }
            String file = iterator.next();
            iw.addDocument(builder.createDocument(new FileInputStream(file), file));
        }
        iw.close();
    }

    public void testSearchIndexLarge() throws IOException {
        for (int i = 0; i < 10; i++) {
            int queryDocID = (int) (Math.random() * 10000);
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("index-large")));
            // select one feature for the large index:
            int featureIndex = 4;
            int count = 0;
            long ms = System.currentTimeMillis();
            ImageSearchHits hits = searchers[featureIndex].search(reader.document(queryDocID), reader);
//        FileUtils.saveImageResultsToHtml("GeneralTest_testSearchIndexLarge_", hits, reader.document(10).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
            FileUtils.saveImageResultsToPng("GeneralTest_testSearchIndexLarge_" + i + "_", hits, reader.document(queryDocID).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
        }
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
