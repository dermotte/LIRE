package net.semanticmetadata.lire.indexers.hashing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.*;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.GenericDocValuesImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.MetricSpacesImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.StopWatch;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;
import java.util.zip.GZIPInputStream;

/**
 * @author Mathias Lux, mathias@juggle.at, 26.08.2015.
 */
public class MetricSpacesTest extends TestCase {
    private final String indexName = "ms-index-test-01";
    private final String imageDirectory = "src/test/resources/images";
    private File file;
    File infile = new File("tmp-images-list-for-testing.txt");

    @Override
    public void setUp() throws Exception {
        super.setUp();
        // create infile.
        FileUtils.createImagefileList(new File(imageDirectory), infile, false);
    }

    public void testHashIndexing() throws IllegalAccessException, IOException, InstantiationException {
//        MetricSpaces.indexReferencePoints(CEDD.class, 10000, 50, new File(infile), new File("dir.cedd1.dat"));
//        MetricSpaces.indexReferencePoints(FCTH.class, 10000, 50, new File(infile), new File("dir.fcth1.dat"));
//        MetricSpaces.indexReferencePoints(PHOG.class, 10000, 50, new File(infile), new File("dir.phog1.dat"));
//        MetricSpaces.indexReferencePoints(OpponentHistogram.class, 10000, 50, new File(infile), new File("dir.ophist1.dat"));
//        MetricSpaces.indexReferencePoints(AutoColorCorrelogram.class, 10000, 50, new File(infile), new File("dir.acc1.dat"));
        MetricSpaces.indexReferencePoints(ColorLayout.class, 10000, 50, infile, new File("dir.cl1.dat"));
    }

    public void testLoadingIndex() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        MetricSpaces.loadReferencePoints(new GZIPInputStream(new FileInputStream("src/test/resources/metricspaces/refPoints_PHOG.dat.gz")));
        MetricSpaces.loadReferencePoints(new GZIPInputStream(new FileInputStream("src/test/resources/metricspaces/refPoints_CEDD.dat.gz")));


        ArrayList<File> imageFiles = FileUtils.getAllImageFiles(new File(imageDirectory), true);
        for (Iterator<File> iterator = imageFiles.iterator(); iterator.hasNext(); ) {
            GlobalFeature feature;
            feature = new CEDD();
            file = iterator.next();
            feature.extract(ImageIO.read(new FileInputStream(file)));
            System.out.println(MetricSpaces.generateHashString(feature));
            feature = new PHOG();
            feature.extract(ImageIO.read(new FileInputStream(file)));
            System.out.println(MetricSpaces.generateHashString(feature));
        }
    }

    public void testImageIndexing() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        MetricSpaces.loadReferencePoints(new GZIPInputStream(new FileInputStream("src/test/resources/metricspaces/refPoints_PHOG.dat.gz")));
        MetricSpaces.loadReferencePoints(new GZIPInputStream(new FileInputStream("src/test/resources/metricspaces/refPoints_CEDD.dat.gz")));

        // running the index with metric spaces & text fields
        ParallelIndexer p = new ParallelIndexer(8, indexName, imageDirectory, GlobalDocumentBuilder.HashingMode.MetricSpaces);
        p.addExtractor(CEDD.class);
        p.addExtractor(PHOG.class);
        p.run();

        // running the index with metric spaces & docvals
//        p = new ParallelIndexer(8, indexName+"-docValues", imageDirectory, GlobalDocumentBuilder.HashingMode.MetricSpaces, true);
        p = new ParallelIndexer(8, indexName+"-docValues", new File("D:\\DataSets\\Flickrphotos\\dir01.txt"), GlobalDocumentBuilder.HashingMode.MetricSpaces, true);
        p.addExtractor(CEDD.class);
        p.addExtractor(PHOG.class);
        p.run();
    }

    public void testSearch() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName)));
        MetricSpaces.loadReferencePoints(new GZIPInputStream(new FileInputStream("src/test/resources/metricspaces/refPoints_PHOG.dat.gz")));
        MetricSpaces.loadReferencePoints(new GZIPInputStream(new FileInputStream("src/test/resources/metricspaces/refPoints_CEDD.dat.gz")));
        MetricSpacesImageSearcher is = new MetricSpacesImageSearcher(10, new GZIPInputStream(new FileInputStream("src/test/resources/metricspaces/refPoints_CEDD.dat.gz")), 100);
        is.setNumHashesUsedForQuery(15);
//        GenericFastImageSearcher is = new GenericFastImageSearcher(10, CEDD.class, false, reader);
        for (int i = 0; i < 10; i++) {
            ImageSearchHits hits = is.search(reader.document(i), reader);
            for (int j = 0; j < hits.length(); j++) {
                System.out.printf("%02d: %06d %02.3f\n", j + 1, hits.documentID(j), hits.score(j));
            }
            System.out.println("------< * >-------");
        }
    }

    public void testSearchAccuracy() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("/home/mlux/tmp/index")));
//        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexName+"-docValues")));
        int maxResults = 100;
        int intersectSum = 0;
        System.out.println("CEDD with BaseSimilarity");
        MetricSpacesImageSearcher mis = new MetricSpacesImageSearcher(maxResults, new FileInputStream("images_CEDD.msd"), 10000, true, reader);
        System.out.printf("MetricSpacesSearcher with %d posting list entries and %d reference points.\n", mis.getLengthOfPostingList(), mis.getNumberOfReferencePoints());
        mis.setNumHashesUsedForQuery(10);
        GenericDocValuesImageSearcher fis = new GenericDocValuesImageSearcher(maxResults, CEDD.class, reader);
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
            for (int j = 0; j < hitsM.length(); j++) {
                setM.add(hitsM.documentID(j));
                setF.add(hitsF.documentID(j));
            }
            setF.removeAll(setM);
            int intersect = (maxResults - setF.size());
//            System.out.println("intersect = " + intersect);
            if (i % 10 == 0) System.out.print('.');
            intersectSum += intersect;
        }
        System.out.printf("\nTime for MetricSpaces vs. linear: %02.3f vs. %02.3f seconds for %d runs at %02.2f recall\n", sm.getTime() / 1000d, sf.getTime() / 1000d, numRuns, ((double) intersectSum / (double) numRuns) / ((double) maxResults));
    }
}



