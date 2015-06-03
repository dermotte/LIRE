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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.correlogram.DynamicProgrammingAutoCorrelogramExtraction;
import net.semanticmetadata.lire.imageanalysis.correlogram.MLuxAutoCorrelogramExtraction;
import net.semanticmetadata.lire.imageanalysis.correlogram.NaiveAutoCorrelogramExtraction;
import net.semanticmetadata.lire.utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

public class AutoColorCorrelogramTest extends TestCase {
    private String[] testFiles = new String[]{"img01.jpg", "img02.jpg", "img03.jpg", "img04.jpg", "img05.jpg", "img06.jpg", "img07.jpg", "img08.jpg", "img09.jpg", "img10.jpg"};
    private String testFilesPath = "./src/test/resources/small/";
    private static int[] sampleQueries = {284, 77, 108, 416, 144, 534, 898, 104, 67, 10, 607, 165, 343, 973, 591, 659, 812, 231, 261, 224, 227, 914, 427, 810, 979, 716, 253, 708, 751, 269, 531, 699, 835, 370, 642, 504, 297, 970, 929, 20, 669, 434, 201, 9, 575, 631, 730, 7, 546, 816, 431, 235, 289, 111, 862, 184, 857, 624, 323, 393, 465, 905, 581, 626, 212, 459, 722, 322, 584, 540, 194, 704, 410, 267, 349, 371, 909, 403, 724, 573, 539, 812, 831, 600, 667, 672, 454, 873, 452, 48, 322, 424, 952, 277, 565, 388, 149, 966, 524, 36, 528, 75, 337, 655, 836, 698, 230, 259, 897, 652, 590, 757, 673, 937, 676, 650, 297, 434, 358, 789, 484, 975, 318, 12, 506, 38, 979, 732, 957, 904, 852, 635, 620, 28, 59, 732, 84, 788, 562, 913, 173, 508, 32, 16, 882, 847, 320, 185, 268, 230, 259, 931, 653, 968, 838, 906, 596, 140, 880, 847, 297, 77, 983, 536, 494, 530, 870, 922, 467, 186, 254, 727, 439, 241, 12, 947, 561, 160, 740, 705, 619, 571, 745, 774, 845, 507, 156, 936, 473, 830, 88, 66, 204, 737, 770, 445, 358, 707, 95, 349};
    private static String testExtensive = "./lire/wang-data-1000";

    public void testExtraction() throws IOException {
        AutoColorCorrelogram acc = new AutoColorCorrelogram();
        BufferedImage image = ImageIO.read(new FileInputStream(testFilesPath + testFiles[0]));
        System.out.println("image = " + image.getWidth() + " x " + image.getHeight());
        acc.extract(image);
        System.out.println("acc = " + acc.getStringRepresentation());
        // testing string representation
        AutoColorCorrelogram ac2 = new AutoColorCorrelogram();
        ac2.setStringRepresentation(acc.getStringRepresentation());
        float distance = acc.getDistance(ac2);
//        System.out.println("distance = " + distance);
        // testing byte array representation:
        ac2 = new AutoColorCorrelogram();
        ac2.setByteArrayRepresentation(acc.getByteArrayRepresentation());
        distance = acc.getDistance(ac2);
        System.out.println("ac2 = " + ac2.getStringRepresentation());
        System.out.println("distance = " + distance);
    }

    public void testMethodsPerformance() throws IOException {
        AutoColorCorrelogram[] acc = new AutoColorCorrelogram[4];
        int[] D = {1, 3, 5, 7};
        int C = 64;
        acc[0] = new AutoColorCorrelogram(C, D, new MLuxAutoCorrelogramExtraction(AutoColorCorrelogram.Mode.SuperFast));
        acc[1] = new AutoColorCorrelogram(C, D, new MLuxAutoCorrelogramExtraction(AutoColorCorrelogram.Mode.FullNeighbourhood));
        acc[2] = new AutoColorCorrelogram(C, D, new NaiveAutoCorrelogramExtraction());
        acc[3] = new AutoColorCorrelogram(C, D, DynamicProgrammingAutoCorrelogramExtraction.getInstance());
        int[] testSet = {284, 77, 108, 416, 144, 534, 898, 104, 67, 10};

        //reads all images
        BufferedImage[] image = new BufferedImage[testSet.length];
        for (int j = 0; j < testSet.length; j++) {
            int id = testSet[j];
            String file = testExtensive + "/" + id + ".jpg";
            image[j] = ImageIO.read(new FileInputStream(file));
        }


        for (int i = 0; i < 4; i++) {
            long t0 = System.currentTimeMillis();
            for (int j = 0; j < testSet.length; j++) {
                acc[i].extract(image[j]);
                System.out.print(".");
            }
            long tf = System.currentTimeMillis();
            long dt = tf - t0;
            double avt = (double) dt / testSet.length;
            System.out.printf("Method %d: total time %d, average %f\n", i, dt, avt);
        }
    }

    public void testPerformance() throws IOException {
        long ms, sum = 0;
        for (int i = 0; i < sampleQueries.length; i++) {
            int id = sampleQueries[i];
            System.out.println("id = " + id + ": ");
            String file = testExtensive + "/" + id + ".jpg";
            AutoColorCorrelogram acc = new AutoColorCorrelogram();
//            OldColorCorrelogram occ = new OldColorCorrelogram(OldColorCorrelogram.Mode.SuperFast);
            BufferedImage image = ImageIO.read(new FileInputStream(file));
//            occ.extract(image);
            ms = System.currentTimeMillis();
            acc.extract(image);
            ms = System.currentTimeMillis() - ms;
            sum += ms;
//            System.out.println("the same? " + acc.getStringRepresentation().equals(occ.getStringRepresentation()));
        }
        System.out.println("time per image = " + sum / sampleQueries.length);
        System.out.println("sum = " + sum);
    }

    public void testEquality() throws IOException {
        long ms, sum = 0;
        for (int i = 0; i < sampleQueries.length; i++) {
            int id = sampleQueries[i];
            System.out.println("id = " + id + ": ");
            String file = testExtensive + "/" + id + ".jpg";
            AutoColorCorrelogram acc = new AutoColorCorrelogram();
//            OldColorCorrelogram occ = new OldColorCorrelogram(OldColorCorrelogram.Mode.SuperFast);
            BufferedImage image = ImageIO.read(new FileInputStream(file));
//            occ.extract(image);
            acc.extract(image);
//            System.out.println("the same? " + acc.getStringRepresentation().equals(occ.getStringRepresentation()));
//            if (!acc.getStringRepresentation().equals(occ.getStringRepresentation())) {
//                System.out.println("acc.getStringRepresentation() = " + acc.getStringRepresentation());
//                System.out.println("occ.getStringRepresentation() = " + occ.getStringRepresentation());
//            }
        }
    }

    public void testRetrieval() throws Exception {
        AutoColorCorrelogram[] acc = new AutoColorCorrelogram[testFiles.length];
        LinkedList<String> vds = new LinkedList<String>();
        for (int i = 0; i < acc.length; i++) {
            System.out.println("Extracting from number " + i);
            acc[i] = new AutoColorCorrelogram();
            acc[i].extract(ImageIO.read(new FileInputStream(testFilesPath + testFiles[i])));
            vds.add(acc[i].getStringRepresentation());
        }

        System.out.println("Calculating distance for " + testFiles[5]);
        for (int i = 0; i < acc.length; i++) {
            AutoColorCorrelogram autoColorCorrelogram = acc[i];
            float distance = acc[i].getDistance(acc[5]);
            System.out.println(testFiles[i] + " distance = " + distance);
        }
        int count = 0;
        for (Iterator<String> iterator = vds.iterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            AutoColorCorrelogram a = new AutoColorCorrelogram();
            a.setStringRepresentation(s);
            float distance = acc[count].getDistance(a);
            System.out.println(testFiles[count] + " distance = " + distance);
            count++;
        }
    }

    public void testSerialization() throws IOException {
        int bytes = 0;
        int sum = 0;
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("testdata/ferrari"), true);
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext(); ) {
            File next = iterator.next();
            BufferedImage image = ImageIO.read(next);
            AutoColorCorrelogram f1 = new AutoColorCorrelogram();
            AutoColorCorrelogram f2 = new AutoColorCorrelogram();

            f1.extract(image);
            System.out.println("f1.getDoubleHistogram().length = " + f1.getDoubleHistogram().length);
            System.out.println(Arrays.toString(f1.getDoubleHistogram()));
            f2.setByteArrayRepresentation(f1.getByteArrayRepresentation());
//            System.out.println(Arrays.toString(f2.getDoubleHistogram()));
            assertTrue(f2.getDistance(f1) == 0);
//            boolean isSame = true;
//            for (int i = 0; i < f2..length; i++) {
//                if (f1.data[i] != f2.data[i]) isSame=false;
//            }
//            assertTrue(isSame);
        }
        double save = 1d - (double) bytes / (double) sum;
        System.out.println(save * 100 + "% saved");
    }

}
