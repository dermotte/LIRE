package net.semanticmetadata.lire.indexers.hashing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.*;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.MetricSpacesImageSearcher;
import net.semanticmetadata.lire.utils.StopWatch;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Timer;

/**
 * @author Mathias Lux, mathias@juggle.at, 26.08.2015.
 */
public class MetricSpacesTest extends TestCase {
    String infile = "testdata/images.lst";
//    String infile = "D:\\DataSets\\MirFlickr\\images01.lst";

    public void testHashIndexing() throws IllegalAccessException, IOException, InstantiationException {
        MetricSpaces.indexReferencePoints(CEDD.class, 1000, 50, new File(infile), new File("dir.cedd.dat"));
//        MetricSpaces.index(FCTH.class, 50, 8, new File(infile), new File("dir.fcth.dat"));
//        MetricSpaces.index(PHOG.class, 50, 8, new File(infile), new File("dir.phog.dat"));
    }

    public void testLoadingIndex() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        MetricSpaces.loadReferencePoints(new File("dir.cedd.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.fcth.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.phog.dat"));


        GlobalFeature feature;
        feature = new FCTH();
        feature.extract(ImageIO.read(new FileInputStream("testdata\\ferrari\\black\\2828686873_2fa36f83d7_b.jpg")));
        System.out.println(MetricSpaces.generateHashString(feature));
        feature = new CEDD();
        feature.extract(ImageIO.read(new FileInputStream("testdata\\ferrari\\black\\2828686873_2fa36f83d7_b.jpg")));
        System.out.println(MetricSpaces.generateHashString(feature));
        feature = new PHOG();
        feature.extract(ImageIO.read(new FileInputStream("testdata\\ferrari\\black\\2828686873_2fa36f83d7_b.jpg")));
        System.out.println(MetricSpaces.generateHashString(feature));
        feature = new ColorLayout();
        feature.extract(ImageIO.read(new FileInputStream("testdata\\ferrari\\black\\2828686873_2fa36f83d7_b.jpg")));
        assertTrue(MetricSpaces.generateHashString(feature)==null);
    }

    public void testImageIndexing() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        MetricSpaces.loadReferencePoints(new File("dir.cedd.dat"));
//        MetricSpaces.loadReferencePoints(new File("dir.fcth.dat"));
//        MetricSpaces.loadReferencePoints(new File("dir.phog.dat"));

        ParallelIndexer p = new ParallelIndexer(6, "ms-index-docval", new File(infile), GlobalDocumentBuilder.HashingMode.MetricSpaces);
        p.addExtractor(CEDD.class);
//        p.addExtractor(FCTH.class);
//        p.addExtractor(PHOG.class);
        p.run();
    }

    public void testSearch() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("ms-index")));
        MetricSpacesImageSearcher is = new MetricSpacesImageSearcher(10, new File("dir.cedd.dat"), 100);
//        GenericFastImageSearcher is = new GenericFastImageSearcher(10, CEDD.class, false, reader);
        for (int i = 0; i < 10; i++) {
            ImageSearchHits hits = is.search(reader.document(i), reader);
            for (int j =0; j< hits.length(); j++) {
                System.out.printf("%02d: %06d %02.3f\n", j + 1, hits.documentID(j), hits.score(j));
            }
            System.out.println("------< * >-------");
        }
    }

    public void testSearchAccuracy() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("/media/mlux/SSD02/ms-index-ms-500k")));
        int maxResults = 10;
        int intersectSum = 0;
        MetricSpacesImageSearcher mis = new MetricSpacesImageSearcher(maxResults, new File("ms-cedd.dat"), 1000);
        mis.setNumHashesUsedForQuery(15);
        GenericFastImageSearcher fis = new GenericFastImageSearcher(maxResults, CEDD.class, true, reader);
        StopWatch sm = new StopWatch();
        StopWatch sf = new StopWatch();
        int numRuns = 100;
        for (int i = 0; i < numRuns; i++) {
            Document document = reader.document(i);
            sm.start();
            ImageSearchHits hitsM = mis.search(document, reader);
            sm.stop();
            sf.start();
            ImageSearchHits hitsF = fis.search(document, reader);
            sf.stop();
            HashSet<Integer> setM = new HashSet<>(maxResults);
            HashSet<Integer> setF = new HashSet<>(maxResults);
            for (int j =0; j< hitsM.length(); j++) {
                setM.add(hitsM.documentID(j));
                setF.add(hitsF.documentID(j));
            }
            setF.removeAll(setM);
            int intersect = (maxResults - setF.size());
            System.out.println("intersect = " + intersect);
            intersectSum += intersect;
        }
        System.out.printf("Time for MetricSpaces vs. linear: %02.3f vs. %02.3f seconds for %d runs at %02.2f recall\n", sm.getTime()/ 1000d, sf.getTime()/1000d, numRuns, ((double) intersectSum / (double) numRuns)/((double) maxResults));
    }
}



