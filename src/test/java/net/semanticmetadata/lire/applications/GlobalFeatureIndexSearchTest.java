package net.semanticmetadata.lire.applications;

import junit.framework.TestCase;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.PHOG;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.GenericDocValuesImageSearcher;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.utils.StopWatch;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.junit.FixMethodOrder;
import org.junit.runners.MethodSorters;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class GlobalFeatureIndexSearchTest extends TestCase {
    private final String indexName = "tmp-test-idx-01";
    private final String indexNameDV = "tmp-test-idx-02";
    private final String imageDirectory = "/home/mlux/tmp/01";
    private final String queryImage = "/home/mlux/tmp/01/3536802596_81a850c6af_m.jpg";
    File infile = new File("tmp-images-list-for-testing.txt");
    private int numRuns = 10;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // create infile.
        // FileUtils.createImagefileList(new File(imageDirectory), infile, false);
    }

    public void testIndexing() {
        ParallelIndexer p = new ParallelIndexer(8, indexName, imageDirectory, GlobalDocumentBuilder.HashingMode.None, false);
        p.addExtractor(CEDD.class);
        p.addExtractor(PHOG.class);
        p.run();
    }

    public void testIndexingDocValues() {
        ParallelIndexer p = new ParallelIndexer(8, indexNameDV, imageDirectory, GlobalDocumentBuilder.HashingMode.None, true);
        p.addExtractor(CEDD.class);
        p.addExtractor(PHOG.class);
        p.run();
    }

    public void testSearchDocValues() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexNameDV)));
        GenericDocValuesImageSearcher is = new GenericDocValuesImageSearcher(10, CEDD.class, reader);
        // run search
        StopWatch sm = new StopWatch();
        BufferedImage qImage = ImageIO.read(new FileInputStream(queryImage));
        ImageSearchHits hits = null;
        for (int i = 0; i<numRuns; i++) {
            sm.start();
            hits = is.search(qImage, reader);
            sm.stop();
        }
        // print results
        for (int i = 0; i < hits.length(); i++) {
            String fileName = reader.document(hits.documentID(i)).getValues(GlobalDocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": \t" + fileName);
        }
        System.out.printf("\nTime for searching: %02.3f ms\n", sm.getTime() / (double) numRuns);
    }

    public void testSearchCaching() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
        GenericFastImageSearcher is = new GenericFastImageSearcher(10, CEDD.class, true, reader);
        // run search
        StopWatch sm = new StopWatch();
        BufferedImage qImage = ImageIO.read(new FileInputStream(queryImage));
        ImageSearchHits hits = null;
        for (int i = 0; i<numRuns; i++) {
            sm.start();
            hits = is.search(qImage, reader);
            sm.stop();
        }
        // print results
        for (int i = 0; i < hits.length(); i++) {
            String fileName = reader.document(hits.documentID(i)).getValues(GlobalDocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": \t" + fileName);
        }
        System.out.printf("\nTime for searching: %02.3f ms\n", sm.getTime() / (double) numRuns);
    }

    public void testSearchPlain() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
        GenericFastImageSearcher is = new GenericFastImageSearcher(10, CEDD.class, false, reader);
        // run search
        StopWatch sm = new StopWatch();
        BufferedImage qImage = ImageIO.read(new FileInputStream(queryImage));
        ImageSearchHits hits = null;
        for (int i = 0; i< numRuns; i++) {
            sm.start();
            hits = is.search(qImage, reader);
            sm.stop();
        }
        // print results
        for (int i = 0; i < hits.length(); i++) {
            String fileName = reader.document(hits.documentID(i)).getValues(GlobalDocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": \t" + fileName);
        }
        System.out.printf("\nTime for searching: %02.3f ms\n", sm.getTime() / (double) numRuns);
    }
}
