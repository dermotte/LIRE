package net.semanticmetadata.lire.indexers;

import junit.framework.TestCase;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.PHOG;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.GenericDocValuesImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
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

    private File infile = new File("testdata/images.lst");
//    private File infile = new File("D:\\DataSets\\Flickrphotos\\dir.txt");
    private String indexPath = "ms-index-docval";

    public void testIndexing() {
        ParallelIndexer p = new ParallelIndexer(8, indexPath, infile, GlobalDocumentBuilder.HashingMode.None, true);
        p.addExtractor(CEDD.class);
        p.addExtractor(FCTH.class);
        p.addExtractor(PHOG.class);
        p.run();
    }

    public void testSearch() throws IOException {
        int numRuns = 10;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        GenericDocValuesImageSearcher is = new GenericDocValuesImageSearcher(4, CEDD.class, reader);
        StopWatch sw1 = new StopWatch();
        StopWatch sw2 = new StopWatch();
        for (int i = 0; i < numRuns; i++) {
            sw1.start();
            ImageSearchHits hits = is.search(reader.document(i), reader);
            sw1.stop();
            for (int j =0; j< hits.length(); j++) {
                System.out.printf("%02d: %06d %02.3f\n", j + 1, hits.documentID(j), hits.score(j));
            }
            System.out.println("------< * >-------");
        }
        for (int i = 0; i < numRuns; i++) {
            sw2.start();
            ImageSearchHits hits = is.search(i);
            sw2.stop();
            for (int j =0; j< hits.length(); j++) {
                System.out.printf("%02d: %06d %02.3f\n", j + 1, hits.documentID(j), hits.score(j));
            }
            System.out.println("------< * >-------");
        }
        System.out.printf("%02.3f vs. %02.3f seconds for %d runs", sw1.getTime()/1000d, sw2.getTime()/1000d, numRuns);
    }
}
