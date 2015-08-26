package net.semanticmetadata.lire.imageanalysis.features;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.features.global.centrist.SimpleCentrist;
import net.semanticmetadata.lire.imageanalysis.features.global.centrist.SpatialPyramidCentrist;
import net.semanticmetadata.lire.utils.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * Test class for the Centrist implementation in LIRE.
 *
 * @author Mathias Lux, mathias@juggle.at, Date: 18.06.2015.
 */
public class TestCentrist extends TestCase {
    public void testExtractionSimpleCentrist() throws IOException {
        ArrayList<File> imageFiles = FileUtils.getAllImageFiles(new File("testdata/ferrari"), true);
        SimpleCentrist sc = new SimpleCentrist();
        for (Iterator<File> iterator = imageFiles.iterator(); iterator.hasNext(); ) {
            File nextImage = iterator.next();
            sc.extract(ImageIO.read(nextImage));
            System.out.println(nextImage.getName() + ": " + Arrays.toString(sc.getFeatureVector()));
        }
    }

    public void testExtractionSpatialPyramidCentrist() throws IOException {
        ArrayList<File> imageFiles = FileUtils.getAllImageFiles(new File("testdata/ferrari"), true);
        SpatialPyramidCentrist sc = new SpatialPyramidCentrist();
        for (Iterator<File> iterator = imageFiles.iterator(); iterator.hasNext(); ) {
            File nextImage = iterator.next();
            sc.extract(ImageIO.read(nextImage));
            System.out.println(nextImage.getName() + ": " + sc.getFeatureVector().length + " entries -> "+ Arrays.toString(sc.getFeatureVector()));
        }
    }
}
