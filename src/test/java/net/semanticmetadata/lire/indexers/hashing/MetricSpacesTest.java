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
    String infile = "/media/mlux/27af5468-1841-447c-9ff5-88030faa0d49/MirFlickr/image.lst";
//    String infile = "testdata/images.lst";
//    String infile = "D:\\DataSets\\MirFlickr\\images01.lst";

    public void testHashIndexing() throws IllegalAccessException, IOException, InstantiationException {
//        MetricSpaces.indexReferencePoints(CEDD.class, 10000, 50, new File(infile), new File("dir.cedd1.dat"));
//        MetricSpaces.indexReferencePoints(FCTH.class, 10000, 50, new File(infile), new File("dir.fcth1.dat"));
//        MetricSpaces.indexReferencePoints(PHOG.class, 10000, 50, new File(infile), new File("dir.phog1.dat"));
//        MetricSpaces.indexReferencePoints(OpponentHistogram.class, 10000, 50, new File(infile), new File("dir.ophist1.dat"));
//        MetricSpaces.indexReferencePoints(AutoColorCorrelogram.class, 10000, 50, new File(infile), new File("dir.acc1.dat"));
        MetricSpaces.indexReferencePoints(ColorLayout.class, 10000, 50, new File(infile), new File("dir.cl1.dat"));
    }

    public void testLoadingIndex() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        MetricSpaces.loadReferencePoints(new File("dir.cedd1.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.fcth1.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.phog1.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.ophist1.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.acc1.dat"));


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
        MetricSpaces.loadReferencePoints(new File("dir.cedd1.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.fcth1.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.phog1.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.ophist1.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.acc1.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.cl1.dat"));

        // running the index with metric spaces & text fields
        ParallelIndexer p = new ParallelIndexer(8, "ms-index-mirflickr-10kro-all", new File(infile), GlobalDocumentBuilder.HashingMode.MetricSpaces);
        p.addExtractor(CEDD.class);
        p.addExtractor(FCTH.class);
        p.addExtractor(PHOG.class);
        p.addExtractor(OpponentHistogram.class);
        p.addExtractor(AutoColorCorrelogram.class);
        p.addExtractor(ColorLayout.class);
        p.run();

        // running the index with metric spaces & docvals
        p = new ParallelIndexer(8, "ms-index-mirflickr-docvals", new File(infile), GlobalDocumentBuilder.HashingMode.MetricSpaces, true);
        p.addExtractor(CEDD.class);
        p.addExtractor(FCTH.class);
        p.addExtractor(PHOG.class);
        p.addExtractor(OpponentHistogram.class);
        p.addExtractor(AutoColorCorrelogram.class);
        p.addExtractor(ColorLayout.class);
        p.run();
    }

    public void testSearch() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("ms-index-mirflickr-docval")));
        MetricSpacesImageSearcher is = new MetricSpacesImageSearcher(10, new File("dir.cedd.dat"), 100);
        is.setNumHashesUsedForQuery(15);
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
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("ms-index-mirflickr-10kro")));
        int maxResults = 100;
        int intersectSum = 0;
        System.out.println("CEDD with BaseSimilarity");
        MetricSpacesImageSearcher mis = new MetricSpacesImageSearcher(maxResults, new File("dir.cedd1.dat"), 10000);
//        mis.setNumHashesUsedForQuery(50);
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
//            System.out.println("intersect = " + intersect);
            if (i%10==0) System.out.print('.');
            intersectSum += intersect;
        }
        System.out.printf("\nTime for MetricSpaces vs. linear: %02.3f vs. %02.3f seconds for %d runs at %02.2f recall\n", sm.getTime()/ 1000d, sf.getTime()/1000d, numRuns, ((double) intersectSum / (double) numRuns)/((double) maxResults));
    }
}



