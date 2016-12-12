package net.semanticmetadata.lire.imageanalysis.features;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.features.global.ACCID;
import net.semanticmetadata.lire.imageanalysis.features.global.centrist.SimpleCentrist;
import net.semanticmetadata.lire.imageanalysis.features.global.centrist.SpatialPyramidCentrist;
import net.semanticmetadata.lire.utils.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Test class for the Centrist implementation in LIRE.
 *
 * @author Mathias Lux, mathias@juggle.at, Date: 18.06.2015.
 */
public class TestACCID extends TestCase {
    public void testExtraction() throws IOException {
        ArrayList<File> imageFiles = FileUtils.getAllImageFiles(new File("src/test/resources/images"), true);
        ACCID accid = new ACCID();
        for (Iterator<File> iterator = imageFiles.iterator(); iterator.hasNext(); ) {
            File nextImage = iterator.next();
            accid.extract(ImageIO.read(nextImage));
            System.out.println(nextImage.getName() + ": " + Arrays.toString(accid.getFeatureVector()));
        }
    }

    public void testDistance() throws IOException {
        ArrayList<File> imageFiles = FileUtils.getAllImageFiles(new File("src/test/resources/images"), true);
        LinkedList<ACCID> features = new LinkedList<>();
        for (Iterator<File> iterator = imageFiles.iterator(); iterator.hasNext(); ) {
            File nextImage = iterator.next();
            ACCID accid = new ACCID();
            accid.extract(ImageIO.read(nextImage));
            features.add(accid);
            assertEquals(accid.getDistance(accid), 0d);
        }
    }

    public void testSerialization() throws IOException {
        ArrayList<File> imageFiles = FileUtils.getAllImageFiles(new File("src/test/resources/images"), true);
        ACCID accid = new ACCID();
        for (Iterator<File> iterator = imageFiles.iterator(); iterator.hasNext(); ) {
            File nextImage = iterator.next();
            accid.extract(ImageIO.read(nextImage));
            ACCID a2 = new ACCID();
            a2.setByteArrayRepresentation(accid.getByteArrayRepresentation());
            assertEquals(a2.getDistance(accid), 0d);
        }
    }
}
