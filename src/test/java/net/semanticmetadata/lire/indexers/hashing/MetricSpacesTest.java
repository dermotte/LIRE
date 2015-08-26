package net.semanticmetadata.lire.indexers.hashing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.*;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * @author Mathias Lux, mathias@juggle.at, 26.08.2015.
 */
public class MetricSpacesTest extends TestCase {
    String infile = "train.lst";

    public void testHashIndexing() throws IllegalAccessException, IOException, InstantiationException {
        MetricSpaces.index(CEDD.class, 2000, 50, new File(infile), new File("dir1.cedd.dat"));
        MetricSpaces.index(FCTH.class, 2000, 50, new File(infile), new File("dir1.fcth.dat"));
        MetricSpaces.index(PHOG.class, 2000, 50, new File(infile), new File("dir1.phog.dat"));
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
        MetricSpaces.loadReferencePoints(new File("dir.fcth.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.phog.dat"));

        ParallelIndexer p = new ParallelIndexer(10, "ms-index-yahoo-gc-1.5M", new File(infile));//, GlobalDocumentBuilder.HashingMode.MetricSpaces);
        p.addExtractor(CEDD.class);
        p.addExtractor(FCTH.class);
        p.addExtractor(PHOG.class);
        p.run();
    }
}
