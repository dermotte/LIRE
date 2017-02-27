package net.semanticmetadata.lire.imageanalysis.features;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.features.global.COMO;
import net.semanticmetadata.lire.utils.FileUtils;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Test class for the COMO .. or actually any global feature .. implementation in LIRE.
 *
 * @author Mathias Lux, mathias@juggle.at, Date: 27.02. 2017.
 */
public class TestGlobalFeature extends TestCase {
    GlobalFeature gFeat = new COMO();

    public void testExtraction() throws IOException, IllegalAccessException, InstantiationException {
        ArrayList<File> imageFiles = FileUtils.getAllImageFiles(new File("src/test/resources/images"), true);
        GlobalFeature accid = gFeat.getClass().newInstance();
        for (Iterator<File> iterator = imageFiles.iterator(); iterator.hasNext(); ) {
            File nextImage = iterator.next();
            accid.extract(ImageIO.read(nextImage));
            System.out.println(nextImage.getName() + ": " + Arrays.toString(accid.getFeatureVector()));
        }
    }

    public void testDistance() throws IOException, IllegalAccessException, InstantiationException {
        ArrayList<File> imageFiles = FileUtils.getAllImageFiles(new File("src/test/resources/images"), true);
        LinkedList<GlobalFeature> features = new LinkedList<>();
        for (Iterator<File> iterator = imageFiles.iterator(); iterator.hasNext(); ) {
            File nextImage = iterator.next();
            GlobalFeature accid = gFeat.getClass().newInstance();
            accid.extract(ImageIO.read(nextImage));
            features.add(accid);
            assertEquals(accid.getDistance(accid), 0d);
        }
    }

    public void testSerialization() throws IOException, IllegalAccessException, InstantiationException {
        ArrayList<File> imageFiles = FileUtils.getAllImageFiles(new File("src/test/resources/images"), true);
        GlobalFeature accid = gFeat.getClass().newInstance();
        for (Iterator<File> iterator = imageFiles.iterator(); iterator.hasNext(); ) {
            File nextImage = iterator.next();
            accid.extract(ImageIO.read(nextImage));
            GlobalFeature a2 = gFeat.getClass().newInstance();
            a2.setByteArrayRepresentation(accid.getByteArrayRepresentation());
            assertEquals(a2.getDistance(accid), 0d);
        }
    }

}
