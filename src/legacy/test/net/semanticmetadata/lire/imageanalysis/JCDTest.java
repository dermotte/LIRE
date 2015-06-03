/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 26.04.13 10:22
 */

package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.ImageUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

/**
 * This file is part of LIRE, a Java library for content based image retrieval.
 *
 * @author Mathias Lux, mathias@juggle.at, 20.04.13
 */

public class JCDTest extends TestCase {
    int bytes = 0, sum = 0;

    public void testExtraction() throws IOException {
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("testdata/ferrari"), true);
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext(); ) {
            File next = iterator.next();
            BufferedImage image = ImageIO.read(next);
            JCD f1 = new JCD();
            JCD f2 = new JCD();

            f1.extract(image);
            System.out.println(Arrays.toString(f1.getByteArrayRepresentation()));

            bytes += f1.getByteArrayRepresentation().length;
            sum += 168;
            f2.setByteArrayRepresentation(f1.getByteArrayRepresentation());
            assertTrue(f2.getDistance(f1) == 0);
        }
        double save = 1d - (double) bytes / (double) sum;
        System.out.println(save * 100 + "% saved");


    }

    public void testSerialization() throws IOException, IllegalAccessException, InstantiationException {
        LireFeature f1 = new JCD();
        String[] testFiles = {"D:\\DataSets\\WIPO-CA\\converted-0\\1000038.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1000282.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1000414.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1000489.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1000466.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1000194.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1000248.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1000009.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1001816.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1001809.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1002011.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1001855.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1002863.png",
                "D:\\DataSets\\WIPO-CA\\converted-0\\1002896.png"};
        for (String testFile : testFiles) {
            f1.extract(ImageIO.read(new File(testFile)));
            LireFeature f2 = f1.getClass().newInstance();
            for (int i = 0; i < 10; i++) {
                f2.extract(ImageIO.read(new File(testFile)));
                assertEquals(f1.getDistance(f2), 0, 0.000001f);
            }
            byte[] tmp = new byte[2048];
            Arrays.fill(tmp, (byte) 0x000F);
            byte[] bytes = f1.getByteArrayRepresentation();
            System.arraycopy(bytes, 0, tmp, 12, bytes.length);
            f2.setByteArrayRepresentation(tmp, 12, bytes.length);
            assertEquals(f1.getDistance(f2), 0, 0.0000001f);


        }


    }

    public void testExtractionAndMetric() throws IOException {
        JCD jcd1 = new JCD();
        BufferedImage img = ImageIO.read(new File("out1.jpg"));
        ImageUtils.scaleImage(img, 50);
        jcd1.extract(img);
        System.out.println(Arrays.toString(jcd1.getByteArrayRepresentation()));
        JCD jcd2 = new JCD();
        jcd2.extract(ImageIO.read(new File("src/test/resources/images/img01.JPG")));
        jcd2.setByteArrayRepresentation(jcd1.getByteArrayRepresentation());
        System.out.println(Arrays.toString(jcd2.getByteArrayRepresentation()));
        System.out.println("jcd2.getDistance(jcd1) = " + jcd1.getDistance(jcd2));
        jcd2.setByteArrayRepresentation(jcd1.getByteArrayRepresentation(), 0, jcd1.getByteArrayRepresentation().length);
        System.out.println("jcd2.getDistance(jcd1) = " + jcd1.getDistance(jcd2));
    }

    public void testConcurrentExtraction() throws IOException, IllegalAccessException, InstantiationException {
        // concurrent extraction.
        Class[] featureClasses = new Class[]{
                CEDD.class,
                FCTH.class,
                JCD.class
//                AutoColorCorrelogram.class,
//                ColorLayout.class,
//                EdgeHistogram.class,
//                Gabor.class,
//                JpegCoefficientHistogram.class,
//                ScalableColor.class,
//                SimpleColorHistogram.class,
//                OpponentHistogram.class,
//                LocalBinaryPatterns.class,
//                RotationInvariantLocalBinaryPatterns.class,
//                BinaryPatternsPyramid.class,
//                LuminanceLayout.class,
//                Tamura.class,
//                FuzzyColorHistogram.class,
//                PHOG.class
        };

        for (int j = 0; j < featureClasses.length; j++) {
            Class featureClass = featureClasses[j];
            LireFeature f1 = (LireFeature) featureClass.newInstance();
            Thread t = null;
            BufferedImage read = ImageIO.read(new File("src/test/resources/images/91561.lire.jpg"));
            f1.extract(read);
            for (int i=0; i<16; i++) {
                t = new Thread(new Extractor(read, f1));
                t.start();
            }
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void testSingleFile() throws IOException {
        BufferedImage image = ImageIO.read(new File("gray.jpg"));
        JCD j = new JCD();
        j.extract(image);
        System.out.println(Arrays.toString(j.getDoubleHistogram()));
        System.out.println(Arrays.toString(j.getByteArrayRepresentation()));
    }

    class Extractor implements Runnable {
        private BufferedImage file;
        private LireFeature feature;
        private LireFeature truth;

        Extractor(BufferedImage file, LireFeature truth) {
            try {
                this.file = file;
                this.truth = truth.getClass().newInstance();
                this.truth.setByteArrayRepresentation(truth.getByteArrayRepresentation());
                feature = truth.getClass().newInstance();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            for (int i =0; i<100; i++) {
                try {
                    feature.extract(file);
                    if (feature.getDistance(truth) != 0) {
                        System.err.println(Thread.currentThread().getName() + " ("+feature.getFeatureName()+") : " + feature.getDistance(truth));
                        break;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
