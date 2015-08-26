package net.semanticmetadata.lire.indexers.hashing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.PHOG;

import java.io.File;
import java.io.IOException;

/**
 * @author Mathias Lux, mathias@juggle.at, 26.08.2015.
 */
public class MetricSpacesTest extends TestCase {
    String infile = "D:\\DataSets\\Flickrphotos\\dir.txt";

    public void testIndexing() throws IllegalAccessException, IOException, InstantiationException {
        MetricSpaces.index(CEDD.class, 1000, 25, new File(infile), new File("dir.cedd.dat"));
        MetricSpaces.index(FCTH.class, 1000, 25, new File(infile), new File("dir.fcth.dat"));
        MetricSpaces.index(PHOG.class, 1000, 25, new File(infile), new File("dir.phog.dat"));
    }

    public void testLoadingIndex() {

    }
}
