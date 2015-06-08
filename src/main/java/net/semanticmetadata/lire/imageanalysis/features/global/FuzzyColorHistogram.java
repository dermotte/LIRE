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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 11.07.13 10:01
 */

/*
 * FuzzyColorHistogram.java
 *
 * Ported from C#, performance is probably poor
 *
 * FuzzyColorHistogram.cs
 * Part of the Callisto framework
 * (c) 2009 Arthur Pitman. All rights reserved.
 *
 */

package net.semanticmetadata.lire.imageanalysis.features.global;


import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

public class FuzzyColorHistogram implements GlobalFeature {

    protected Color[] binColors;
    protected final int SIZE = 5;
    protected final int SIZE3 = SIZE * SIZE * SIZE;

    protected int[] descriptorValues;


    @Override
    public void extract(BufferedImage bimg) {
        binColors = new Color[SIZE3];

        int counter = 0;
        for (int k = 0; k < SIZE; k++) {
            for (int j = 0; j < SIZE; j++) {
                for (int i = 0; i < SIZE; i++) {
                    binColors[counter] = getColorForBin(i, j, k);
                    counter++;
                }
            }
        }

        double[] histogramA = new double[SIZE3];

        int width = bimg.getWidth();
        int height = bimg.getHeight();

        WritableRaster raster = bimg.getRaster();
        int[] pixel = new int[3];

        for (int j = 0; j < height; j++) {
            for (int i = 0; i < width; i++) {
                raster.getPixel(i, j, pixel);
                int r = pixel[0];
                int g = pixel[1];
                int b = pixel[2];


                for (int k = 0; k < SIZE3; k++) {
                    double rDiff = (double) (binColors[k].getRed()) - r;
                    double gDiff = (double) (binColors[k].getGreen()) - g;
                    double bDiff = (double) (binColors[k].getBlue()) - b;
                    double rdist = rDiff * rDiff + gDiff * gDiff + bDiff * bDiff;
                    histogramA[k] += (10.0 / Math.sqrt(rdist + 1));
                }
            }
        }

        double maxA = 0;
        for (int k = 0; k < SIZE3; k++) {
            if (histogramA[k] > maxA)
                maxA = histogramA[k];
        }

        descriptorValues = new int[SIZE3];
        for (int k = 0; k < SIZE3; k++)
            descriptorValues[k] = (int) (histogramA[k] / maxA * 255);
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(descriptorValues);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        descriptorValues = SerializationUtils.toIntArray(in);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        descriptorValues = SerializationUtils.toIntArray(in, offset, length);
    }

    /**
     * by patch contributed by Franz Graf, franz.graf@gmail.com
     * @return the feature vector as double[]
     */
    @Override
    public double[] getFeatureVector() {
        return SerializationUtils.toDoubleArray(descriptorValues);
    }

    @Override
    public double getDistance(LireFeature vd) {
        if (!(vd instanceof FuzzyColorHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");
        FuzzyColorHistogram target = (FuzzyColorHistogram) vd;
        double distance = 0;
        for (int i = 0; i < SIZE3; i++)
            distance += ((descriptorValues[i] - target.descriptorValues[i]) * (descriptorValues[i] - target.descriptorValues[i]));

        return Math.sqrt(distance / SIZE3);
    }

//    public String getStringRepresentation() { // added by mlux
//        StringBuilder sb = new StringBuilder(descriptorValues.length * 2 + 25);
//        sb.append("fuzzycolorhist");
//        sb.append(' ');
//        sb.append(descriptorValues.length);
//        sb.append(' ');
//        for (double aData : descriptorValues) {
//            sb.append((int) aData);
//            sb.append(' ');
//        }
//        return sb.toString().trim();
//    }
//
//    public void setStringRepresentation(String s) { // added by mlux
//        StringTokenizer st = new StringTokenizer(s);
//        if (!st.nextToken().equals("fuzzycolorhist"))
//            throw new UnsupportedOperationException("This is not a fuzzycolorhist descriptor.");
//        descriptorValues = new int[Integer.parseInt(st.nextToken())];
//        for (int i = 0; i < descriptorValues.length; i++) {
//            if (!st.hasMoreTokens())
//                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
//            descriptorValues[i] = (int) Integer.parseInt(st.nextToken());
//        }
//
//    }


    protected Color getColorForBin(int rBin, int gBin, int bBin) {
        // work out the width of each bin and place the color in the middle
        int binWidth = 256 / SIZE;
        int offset = binWidth / 2;

        return new Color(rBin * binWidth + offset, gBin * binWidth + offset, bBin * binWidth + offset);
    }

    @Override
    public String getFeatureName() {
        return "Fuzzy Color Histogram";
    }

    @Override
    public String getFieldName() {
        return "f_fuzcolhis";
    }
}
