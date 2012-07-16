/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.StringTokenizer;

/**
 * User: mlux
 * Date: 25.11.2009
 * Time: 13:54:59
 */
public class GeneralColorLayout implements LireFeature {
    private int[] pixel = new int[3];
    private int[] histogram = new int[269 * 69];

    public void extract(BufferedImage bimg) {
        int w = bimg.getWidth();
        int h = bimg.getHeight();

        if (w != 269 || h != 69) {
            // resize
            bimg = ImageUtils.scaleImage(bimg, 269, 69);
        }
        WritableRaster raster = bimg.getRaster();
        for (int x = 0; x < bimg.getWidth(); x++) {
            for (int y = 0; y < bimg.getHeight(); y++) {
                raster.getPixel(x, y, pixel);
                rgb2hsv(pixel[0], pixel[1], pixel[2], pixel);
                histogram[268 * y + x] = quant(pixel);
            }
        }
    }

    public byte[] getByteArrayRepresentation() {
        throw new UnsupportedOperationException("No implemented!");
    }

    public void setByteArrayRepresentation(byte[] in) {
        throw new UnsupportedOperationException("No implemented!");
    }

    public double[] getDoubleHistogram() {
        throw new UnsupportedOperationException("No implemented!");
    }

    private int quant(int[] pixel) {
        int qH = (int) Math.floor((pixel[0] * 64f) / 360f);    // more granularity in color
        int qS = (int) Math.floor((pixel[2] * 8f) / 100f);
//        int qV = (int) Math.floor((pixel[1] * 2f) / 100f);
        if (qH == 64) qH = 63;
        if (qS == 8) qS = 7;
//        if (qV == 2) qV = 1;
        return (qH) * 8 + qS;
    }

    public float getDistance(LireFeature feature) {
        int[] pixel1 = new int[3];
        int[] pixel2 = new int[3];
        float dist = 0;
        if (feature instanceof GeneralColorLayout) {

            return (float) MetricsUtils.distL1(histogram, ((GeneralColorLayout) feature).histogram);
        } else return -1f;
    }

    private void getPixel(int quantValue, int[] pixel) {
        pixel[0] = (int) Math.floor(quantValue / (4f * 2f)); // h
        pixel[1] = (int) Math.floor((quantValue % (4 * 2)) / (2f)); // s
        pixel[1] = quantValue - pixel[0] * 4 * 2 - pixel[1] * 2;
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(histogram.length * 4);
        sb.append("gencl");
        sb.append(' ');
        sb.append(histogram.length);
        sb.append(' ');
        for (int i = 0; i < histogram.length; i++) {
            sb.append(histogram[i]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String s) {
        StringTokenizer st = new StringTokenizer(s);
        if (!st.nextToken().equals("gencl"))
            throw new UnsupportedOperationException("This might not be the approprioate descriptor ...");
        histogram = new int[Integer.parseInt(st.nextToken())];
        for (int i = 0; i < histogram.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation!");
            histogram[i] = Integer.parseInt(st.nextToken());
        }
    }

    public static void rgb2hsv(int r, int g, int b, int hsv[]) {

        int min;    //Min. value of RGB
        int max;    //Max. value of RGB
        int delMax; //Delta RGB value

        min = Math.min(r, g);
        min = Math.min(min, b);

        max = Math.max(r, g);
        max = Math.max(max, b);

        delMax = max - min;

//        System.out.println("hsv = " + hsv[0] + ", " + hsv[1] + ", "  + hsv[2]);

        float H = 0f, S = 0f;
        float V = max / 255f;

        if (delMax == 0) {
            H = 0f;
            S = 0f;
        } else {
            S = delMax / 255f;
            if (r == max) {
                if (g >= b) {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60;
                } else {
                    H = ((g / 255f - b / 255f) / (float) delMax / 255f) * 60 + 360;
                }
            } else if (g == max) {
                H = (2 + (b / 255f - r / 255f) / (float) delMax / 255f) * 60;
            } else if (b == max) {
                H = (4 + (r / 255f - g / 255f) / (float) delMax / 255f) * 60;
            }
        }
//        System.out.println("H = " + H);
        hsv[0] = (int) (H);
        hsv[1] = (int) (S * 100);
        hsv[2] = (int) (V * 100);
    }

}
