/*
 * This file is part of the LIRE project: http://lire-project.net
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

package net.semanticmetadata.lire.imageanalysis.features.local.sift;

import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * ...
 * Date: 18.09.2008
 * Time: 16:47:56
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SiftExtractor implements LocalFeatureExtractor{
    // steps
    int steps = 3;
    // initial sigma
    float initial_sigma = 1.6f;
    // background colour
    double bg = 0.0;
    // feature descriptor size
    int fdsize = 4;
    // feature descriptor orientation bins
    int fdbins = 8;
    // size restrictions for scale octaves, use octaves < max_size and > min_size only
    int min_size = 64;
    int max_size = 1024;
    // minimal allowed alignment error in px
    float min_epsilon = 2.0f;
    // maximal allowed alignment error in px
    float max_epsilon = 100.0f;
    float min_inlier_ratio = 0.05f;

    float scale = 1.0f;

    List<SiftFeature> features = null;

    public SiftExtractor() {

    }


    private void align(BufferedImage img1, BufferedImage img2) {
        try {
            List<SiftFeature> fs1 = computeSiftFeatures(img1);
            List<SiftFeature> fs2 = computeSiftFeatures(img2);

            // find the best matching features:
            LinkedList<Found> res = new LinkedList<Found>();
            for (int i = 0; i < fs1.size(); i++) {
                SiftFeature f1 = fs1.get(i);
                for (int j = i + 1; j < fs2.size(); j++) {
                    SiftFeature f2 = fs2.get(j);
                    // determine similarity:
                    float d = f2.descriptorDistance(f1);
                    res.add(new Found(f1, f2, d));
                    Collections.sort(res);
                    if (res.size() > 5) res.removeLast();
                }
            }
            Graphics2D g1 = (Graphics2D) img1.getGraphics();
            g1.setColor(Color.red);
            Graphics2D g2 = (Graphics2D) img2.getGraphics();
            g2.setColor(Color.red);

            for (Iterator<Found> foundIterator = res.iterator(); foundIterator.hasNext(); ) {
                Found found = foundIterator.next();
                System.out.print("f1 = " + found.f1);
                System.out.print(" / f2 = " + found.f2);
                System.out.println(" / d = " + found.d);
                drawSquare(g1, new double[]{found.f1.location[0] / scale, found.f1.location[1] / scale}, fdsize * 1.0 * (double) found.f1.scale / scale, (double) found.f1.orientation);
                drawSquare(g2, new double[]{found.f2.location[0] / scale, found.f2.location[1] / scale}, fdsize * 1.0 * (double) found.f2.scale / scale, (double) found.f2.orientation);
            }
//            ImageIO.write(img1, "png", new File("out1.png"));
//            ImageIO.write(img2, "png", new File("out2.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }
//        BufferedImage img = ImageIO.read(new File("test.jpg"));
    }

    public static void main(String[] args) throws IOException {
        SiftExtractor e = new SiftExtractor();
        e.align(ImageIO.read(new File("c:/temp/image001.png")), ImageIO.read(new File("c:/temp/image002.png")));
    }

    @Override
    public void extract(BufferedImage image) {
        try {
            features = computeSiftFeatures(image);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<? extends LocalFeature> getFeatures() {
        return features;
    }

    @Override
    public Class<? extends LocalFeature> getClassOfFeatures() {
        return SiftFeature.class;
    }

    public List<SiftFeature> computeSiftFeatures(BufferedImage img) throws IOException {
        LinkedList<SiftFeature> fs2 = new LinkedList<SiftFeature>();
        FloatArray2DSIFT sift = new FloatArray2DSIFT(fdsize, fdbins);

        FloatArray2D fa = ImageArrayConverter.ImageToFloatArray2D(img);
        Filter.enhance(fa, 1.0f);

        sift.init(fa, steps, initial_sigma, min_size, max_size);
        fs2.addAll(sift.run(max_size));
        Collections.sort(fs2);
        return fs2;
    }

    static void drawSquare(Graphics2D ip, double[] o, double scale, double orient) {
        scale /= 2;

        double sin = Math.sin(orient);
        double cos = Math.cos(orient);

        int[] x = new int[6];
        int[] y = new int[6];


        x[0] = (int) (o[0] + (sin - cos) * scale);
        y[0] = (int) (o[1] - (sin + cos) * scale);

        x[1] = (int) o[0];
        y[1] = (int) o[1];

        x[2] = (int) (o[0] + (sin + cos) * scale);
        y[2] = (int) (o[1] + (sin - cos) * scale);
        x[3] = (int) (o[0] - (sin - cos) * scale);
        y[3] = (int) (o[1] + (sin + cos) * scale);
        x[4] = (int) (o[0] - (sin + cos) * scale);
        y[4] = (int) (o[1] - (sin - cos) * scale);
        x[5] = x[0];
        y[5] = y[0];

        ip.drawPolygon(new Polygon(x, y, x.length));
    }

}

class Found implements Comparable<Object> {
    SiftFeature f1, f2;
    float d;

    Found(SiftFeature f1, SiftFeature f2, float d) {
        this.f1 = f1;
        this.f2 = f2;
        this.d = d;
    }

    @Override
    public int compareTo(Object o) {
        return (int) Math.signum(d - ((Found) o).d);
    }
}
