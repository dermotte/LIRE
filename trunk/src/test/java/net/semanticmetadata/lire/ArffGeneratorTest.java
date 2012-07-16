package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram;
import net.semanticmetadata.lire.utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * ...
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Created: 04.05.12, 10:00
 */
public class ArffGeneratorTest extends TestCase {
    public void testGenFile() throws IOException {
        ArrayList<File> images = FileUtils.getAllImageFiles(new File("W:\\Forschung\\Intention_Test_Data\\data\\images"), true);
        // print header
        System.out.println("% generated automatically by LIRe\n" +
                "@relation content_based_features\n" +
                "\n" +
                "@attribute image_filename String");
        System.out.println("@attribute faces numeric");
        System.out.println("@attribute cedd relational");
        for (int i = 0; i < 144; i++) {
            System.out.println("\t@attribute c" + i + " numeric");
        }
        System.out.println("@end cedd");
        System.out.println("@attribute rgb_hist relational");
        for (int i = 0; i < 64; i++) {
            System.out.println("\t@attribute rh" + i + " numeric");
        }
        System.out.println("@end rgb_hist");
        System.out.println("@data");
        // print data
        for (Iterator<File> iterator = images.iterator(); iterator.hasNext(); ) {
            File nextFile = iterator.next();
            try {
                BufferedImage img = ImageIO.read(nextFile);
                // filename
                System.out.print(nextFile.getName() + ",");
                // cedd
                CEDD cedd = new CEDD();
                cedd.extract(img);
                double[] histogram = cedd.getDoubleHistogram();
                for (int i = 0; i < histogram.length; i++) {
                    System.out.print((int) histogram[i]);
                    System.out.print(',');

                }
//                System.out.println();
                // rgb histogram
                SimpleColorHistogram.DEFAULT_NUMBER_OF_BINS = 64;
                SimpleColorHistogram rgb = new SimpleColorHistogram();
                rgb.extract(img);
                histogram = rgb.getDoubleHistogram();
                for (int i = 0; i < histogram.length; i++) {
                    System.out.print((int) histogram[i]);
                    System.out.print(',');
                }
                System.out.println();
            } catch (Exception e) {
                System.out.println("-- ERROR -- " + nextFile.getName() + ", " + e.getMessage());
            }
        }
    }
}
