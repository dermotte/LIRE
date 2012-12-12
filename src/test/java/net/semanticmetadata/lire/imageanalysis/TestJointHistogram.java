package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * User: mlux
 * Date: 04.12.12
 * Time: 11:01
 */
public class TestJointHistogram extends TestCase {
    public void testExtract() throws IOException {
        BufferedImage img = ImageIO.read(new FileInputStream("C:\\Java\\Projects\\LireSVN\\testdata\\ferrari\\black\\2828686873_2fa36f83d7_b.jpg"));
        JointHistogram jh = new JointHistogram();
        jh.extract(img);
        System.out.println(Arrays.toString(jh.descriptor));

        JointHistogram jh2 = new JointHistogram();
        jh2.setByteArrayRepresentation(jh.getByteArrayRepresentation());

        System.out.println(jh2.getDistance(jh));

    }

    public void testJsd() {
        int i = 0;
        float[] h1 = new float[]{1f};
        float[] h2 = new float[]{6f};
        float result = (float) ((h1[i] > 0 ? (h1[i]) * Math.log((2f * h1[i]) / (h1[i] + h2[i])) : 0) +
                (h2[i] > 0 ? (h2[i]) * Math.log((2f * h2[i]) / (h1[i] + h2[i])) : 0));
        System.out.println("result = " + result);
        System.out.println(h1[i] > 0 ? "1" : "0");

        System.out.println((true ? 1 : 0) + 1);
    }

}
