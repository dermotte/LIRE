package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;

/**
 * User: mlux
 * Date: 18.12.12
 * Time: 13:17
 */
public class OpponentHistogramTest extends TestCase {
    public void testExtraction() throws IOException {
        BufferedImage img = ImageIO.read(new FileInputStream("src\\test\\resources\\images\\test_image.png"));
        OpponentHistogram oh = new OpponentHistogram();
        oh.extract(img);
        System.out.println(Arrays.toString(oh.getDoubleHistogram()));
        OpponentHistogram oh2 = new OpponentHistogram();
        oh2.setByteArrayRepresentation(oh.getByteArrayRepresentation());
        System.out.println("oh2.getDistance(oh) = " + oh2.getDistance(oh));
    }
}
