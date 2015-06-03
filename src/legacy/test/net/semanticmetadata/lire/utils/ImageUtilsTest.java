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
 * Updated: 11.07.13 09:32
 */

package net.semanticmetadata.lire.utils;

import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.awt.color.ColorSpace;
import java.awt.image.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Mathias Lux, mathias@juggle.at, 04.04.13, 09:30
 */
public class ImageUtilsTest extends TestCase {
    public void testCheckOpen() throws IOException {
        ArrayList<File> allImageFiles = FileUtils.getAllImageFiles(new File("D:\\DataSets\\Yahoo-GC\\test"), true);
        System.out.println("Checking " + allImageFiles.size() + " files for compatibility.");
        BufferedWriter bw = new BufferedWriter(new FileWriter("faulty.txt"));
        long ms = System.currentTimeMillis();
        int count = 0;
        for (Iterator<File> iterator = allImageFiles.iterator(); iterator.hasNext(); ) {
            File next = iterator.next();
            if (!FileUtils.isImageFileCompatible(next)) {
                String s = next.getCanonicalPath();
                bw.write(s + "\n");
                System.out.println(s);
            }
            count++;
            if (count==50) System.out.println("** - " + count + " images analyzed, " + (System.currentTimeMillis()-ms)/count + " ms / image");
            if (count==100) System.out.println("** - " + count + " images analyzed, " + (System.currentTimeMillis()-ms)/count + " ms / image");
            if (count%1000 == 0) System.out.println("** - " + count + " images analyzed, " + (System.currentTimeMillis()-ms)/count + " ms / image");
        }
        bw.close();
    }

    public void testTrim() throws IOException {
        ImageIO.write(ImageUtils.trimWhiteSpace(ImageIO.read(new File("test_trim.png"))), "png", new File("out-trim.png"));
    }

    public void testDifferenceOfGaussians() throws IOException {
        BufferedImage bufferedImage = ImageUtils.differenceOfGaussians(ImageIO.read(new FileInputStream("wang-1000/0.jpg")));
        ImageIO.write(bufferedImage, "png", new FileOutputStream("out-DoG.png"));
    }

    public void testSobel() throws IOException {
        float[] sobelX = {
                -1, 0, 1,
                -2, 0, 2,
                -1, 0, 1,
        };
        BufferedImage image = ImageIO.read(new File("test.jpg"));
        ColorConvertOp grayScaleOp = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage grayImage = grayScaleOp.filter(image, null);
        BufferedImageOp op = new ConvolveOp( new Kernel(3, 3, sobelX) );
        BufferedImage result = op.filter(grayImage, null);
        WritableRaster r = result.getRaster();
        int[] pixel = new int[r.getWidth()];
        double countEdgePixels = 0;
        for (int y = 0; y<r.getHeight();y++) {
//            System.out.println("y = " + y);
            r.getPixels(0, y, r.getWidth(),1, pixel);
            for (int i = 0; i < pixel.length; i++) {
                // create some stat out of the energy ...
                if (pixel[i] > 128) {
                    countEdgePixels++;
                }
            }
        }
        System.out.printf("Edge pixel ratio = %4.4f\n", countEdgePixels/(double) (r.getWidth()*r.getHeight()));
        ImageIO.write(result, "png", new File("out.png"));
    }

    public void testThreshold() throws IOException {
        ImageIO.write(ImageUtils.thresholdImage(ImageIO.read(new File("testdata/wang-1000/0.jpg")), 128), "png", new File("out.png"));
    }

}
