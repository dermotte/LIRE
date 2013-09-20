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

package net.semanticmetadata.lire.utils.cv;

import net.semanticmetadata.lire.imageanalysis.filters.FastBilateralFilter;
import net.semanticmetadata.lire.imageanalysis.filters.IndexedIntArray;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * A simple pixel clustering algorithm based on RGB, k-means and L2 distance.
 * @author Mathias Lux, mathias@juggle.at - 20.09.13 10:39
 */
public class PixelClustering {
    private static int numberOfColors = 6;

    public static BufferedImage clusterPixels(BufferedImage img) {
        // Apply Bilateral Filtering before actually classifying the pixels ...
        BufferedImage b = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        b.getGraphics().drawImage(img, 0, 0, null);
        IndexedIntArray src = new IndexedIntArray(new int[img.getWidth()*img.getHeight()], 0);
        IndexedIntArray dst = new IndexedIntArray(new int[img.getWidth()*img.getHeight()], 0);
        b.getRaster().getDataElements(0, 0, img.getWidth(), img.getHeight(), src.array);
        FastBilateralFilter fbf = new FastBilateralFilter(img.getWidth(), img.getHeight(), img.getWidth(), 90f, 0.3f);
        fbf.apply(src, dst);
        b.getRaster().setDataElements(0, 0, img.getWidth(), img.getHeight(), dst.array);
/*
        try {
            ImageIO.write(b, "png", new File("out_filtered.png"));
            ImageIO.write(img, "png", new File("out_original.png"));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
*/
        img.getGraphics().drawImage(b, 0, 0, null);
        WritableRaster r = img.getRaster();
        // quantize image colors with k-means:
        ArrayList<double[]> pixels = new ArrayList<double[]>(r.getHeight()*r.getWidth());
        for (int x = 0; x < r.getWidth(); x++) {
            for (int y = 0; y < r.getHeight(); y++) {
                double[] tmpPixel = new double[3];
//                double[] tmpPixel = new double[5]; // use this one if you want connected patches.
                r.getPixel(x, y, tmpPixel);
                assert(tmpPixel[0]<256);
                assert(tmpPixel[1]<256);
                assert(tmpPixel[2]<256);
//                tmpPixel[3] = x*255f/(double)r.getWidth(); // use this one if you want connected patches.
//                tmpPixel[4] = y*255f/(double)r.getHeight();
                pixels.add(tmpPixel);
            }
        }
        // do the k-means
        KMeans km = new KMeans(pixels, numberOfColors);
        for (int i=0; i<25; i++)
            km.step();
        List<double[]> means = km.getMeans();
        double[] tmpPixel = new double[3];
//        double[] tmpPixel = new double[5]; // use this one if you want connected patches.
        for (int x = 0; x < r.getWidth(); x++) {
            for (int y = 0; y < r.getHeight(); y++) {
                r.getPixel(x, y, tmpPixel);
//                tmpPixel[3] = x*255f/(double)r.getWidth(); // use this one if you want connected patches.
//                tmpPixel[4] = y*255f/(double)r.getHeight();
                int num = -1;
                int count = 0;
                double distance=-1, tmpDistance=-1;
                for (Iterator<double[]> iterator = means.iterator(); iterator.hasNext(); ) {
                    double[] next = iterator.next();
                    distance = MetricsUtils.distL2(next, tmpPixel);
                    if (num < 0 || distance < tmpDistance) {
                        num = count;
                        tmpDistance = distance;
                    }
                    count++;
                }
                tmpPixel[0] = Math.floor(means.get(num)[0]);
                tmpPixel[1] = Math.floor(means.get(num)[1]);
                tmpPixel[2] = Math.floor(means.get(num)[2]);
                r.setPixel(x,y,tmpPixel);
            }
        }
        return img;
    }

    public static void main(String[] args) throws IOException {
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("D:\\Temp\\tmp"), false);
        int count = 10;
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext(); ) {
            File next = iterator.next();
            BufferedImage img = ImageIO.read(next);
            BufferedImage toWrite = new BufferedImage(img.getWidth()*2, img.getHeight(), BufferedImage.TYPE_INT_RGB);
            toWrite.getGraphics().drawImage(img, 0, 0, null);
            BufferedImage bufferedImage = clusterPixels(img);
            toWrite.getGraphics().drawImage(bufferedImage, img.getWidth(), 0, null);
            ImageIO.write(toWrite, "png", new File("out_test_"+count+".png"));
            count++;
        }
    }

}

