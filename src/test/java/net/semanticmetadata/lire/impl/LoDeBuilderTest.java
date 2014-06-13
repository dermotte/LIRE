package net.semanticmetadata.lire.impl;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Created by mlux_2 on 13.06.2014.
 */
public class LoDeBuilderTest extends TestCase {
    private String testExtensive = "./testdata/wang-1000";

    public void testExtraction() throws IOException {
        LoDeBuilder b = new LoDeBuilder();
        b.createDocument(ImageIO.read(new File(testExtensive + "/1.jpg")), "test");
    }
}
