package net.semanticmetadata.lire.indexers;

import junit.framework.TestCase;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.PHOG;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.GenericDocValuesImageSearcher;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.MetricSpacesImageSearcher;
import net.semanticmetadata.lire.utils.StopWatch;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Created by mlux on 09.09.2015.
 */
public class DocValuesTest extends TestCase {

//    private File infile = new File("testdata/images.lst");
    private File infile = new File("/home/mlux/images3.lst");
    private String indexPath = "ms-index-ms-500k";
    private File mfile = new File("ms-cedd.dat");

    public void testPrepare() throws IllegalAccessException, IOException, InstantiationException {
        MetricSpaces.indexReferencePoints(CEDD.class, 5000, 25, infile, mfile);
    }

    public void testIndexing() throws IllegalAccessException, IOException, InstantiationException, ClassNotFoundException {
        MetricSpaces.loadReferencePoints(mfile);
        ParallelIndexer p = new ParallelIndexer(8, indexPath, infile, GlobalDocumentBuilder.HashingMode.MetricSpaces, false, 10000);
        p.addExtractor(CEDD.class);
        p.addExtractor(FCTH.class);
        p.addExtractor(PHOG.class);
        p.run();
    }

    public void testSearch() throws IOException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        int numRuns = 100;
//        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("ms-index-ms-500k")));
//        IndexReader readerDocVal = DirectoryReader.open(FSDirectory.open(Paths.get("ms-index-docval-500k")));
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("/media/mlux/SSD02/ms-index-ms-500k")));
        IndexReader readerDocVal = DirectoryReader.open(FSDirectory.open(Paths.get("/media/mlux/SSD02/ms-index-docval-500k")));
        System.out.printf("Number of documents: %d\n", reader.maxDoc());
        GenericDocValuesImageSearcher is = new GenericDocValuesImageSearcher(100, FCTH.class, readerDocVal);
        MetricSpacesImageSearcher mis = new MetricSpacesImageSearcher(100, mfile, 500);
        mis.setNumHashesUsedForQuery(15);
        StopWatch sw0 = new StopWatch();
        sw0.start();
        GenericFastImageSearcher gis = new GenericFastImageSearcher(100, CEDD.class, true, reader);
        sw0.stop();
        System.out.printf("Startup latency of cached searcher: %02.3f sec\n", sw0.getTime()/1000d);
        GenericFastImageSearcher lis = new GenericFastImageSearcher(100, CEDD.class, false, reader);
        StopWatch sw1 = new StopWatch();
        StopWatch sw2 = new StopWatch();
        StopWatch sw3 = new StopWatch();
        StopWatch sw4 = new StopWatch();
        for (int i = 0; i < numRuns; i++) {
            sw1.start();
            ImageSearchHits hits = gis.search(reader.document(i), reader);
            sw1.stop();
        }
        for (int i = 0; i < numRuns; i++) {
            sw2.start();
            ImageSearchHits hits = mis.search(reader.document(i), reader);
            sw2.stop();
        }
        for (int i = 0; i < numRuns; i++) {
            sw3.start();
            ImageSearchHits hits = is.search(reader.document(i), reader);
            sw3.stop();
        }
        for (int i = 0; i < numRuns; i++) {
            sw4.start();
//            ImageSearchHits hits = lis.search(reader.document(i), reader);
            sw4.stop();
        }
        System.out.printf(
                "cached   \t%02.3f\n" +
                "metric_s \t%02.3f\n" +
                "DocVal   \t%02.3f\n" +
                "linear   \t%02.3f\n" +
                "in seconds for %d runs", sw1.getTime() / 1000d, sw2.getTime() / 1000d,  sw3.getTime() / 1000d, sw4.getTime() / 1000d, numRuns);
    }
}
