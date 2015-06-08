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
 * Updated: 11.07.13 10:45
 */

package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;

/**
 * The LuminanceLayout Descriptor is intended for grayscale or B/W images. It scales an image down to a very
 * small size and uses this smaller version as a descriptor. Interesting aspect is that white stripes are
 * added to make the small image quadratic.
 *
 * @author Mathias Lux, mathias@juggle.at, 06.04.13
 */
public class LuminanceLayout implements GlobalFeature {
    double[] histogram;
    int tmp;
    static ColorConvertOp grayscale = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY),
            new RenderingHints(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR));
    private int sideLength = 8;

    @Override
    public void extract(BufferedImage bimg) {
        BufferedImage gray = grayscale.filter(bimg, null);
        // contrast enhancement didn't go to well with the wang 1000 data set.
//        enhanceContrast(gray);
        BufferedImage small = new BufferedImage(sideLength, sideLength, BufferedImage.TYPE_BYTE_GRAY);

        double scale = (double) Math.max(gray.getWidth(), gray.getHeight()) / 32d;
        int w = (int) (gray.getWidth() / scale);
        int h = (int) (gray.getHeight() / scale);
        int x = 0, y = 0;
        if (w < sideLength) x = (sideLength - w) / 2;
        if (h < sideLength) y = (sideLength - h) / 2;
        small.getGraphics().fillRect(0, 0, sideLength, sideLength);
//        small.getGraphics().drawImage(gray, 0, 0, 8, 8, null);
        small.getGraphics().drawImage(gray, x, y, w, h, null);

        histogram = new double[sideLength * sideLength];
        small.getRaster().getPixels(0, 0, sideLength, sideLength, histogram);
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = Math.floor(histogram[i] / 8d); // quantize colors to 32 steps ...
        }
//        histogram = jpgDct(histogram);
    }

    @SuppressWarnings("unused")
    private void enhanceContrast(BufferedImage gray) {
        int[] tmp = {0};
        double val;
        int min = 255, max = 0;
        for (int x = 0; x < gray.getWidth(); x++) { // check ...
            for (int y = 0; y < gray.getHeight(); y++) {
                gray.getRaster().getPixel(x, y, tmp);
                min = Math.min(tmp[0], min);
                max = Math.max(tmp[0], max);
            }
        }
        if (max < 255 || min > 0) { // enhance ...
            double scale = (((double) max) - ((double) min)) / 255d;
            for (int x = 0; x < gray.getWidth(); x++) { // check ...
                for (int y = 0; y < gray.getHeight(); y++) {
                    gray.getRaster().getPixel(x, y, tmp);
                    val = Math.floor(((double) (tmp[0] - min)) / scale);
                    tmp[0] = (int) val;
                    gray.getRaster().setPixel(x, y, tmp);
                }
            }
        }
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[histogram.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) histogram[i];
//            System.out.println("result[i]-histogram[i] = " + (result[i] - histogram[i]));
        }
        return result;
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        histogram = new double[in.length];
        for (int i = 0; i < in.length; i++) {
            histogram[i] = (double) in[i];
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        histogram = new double[length];
        for (int i = 0; i < length; i++) {
            histogram[i] = (double) in[i+offset];
        }
    }

    @Override
    public double[] getFeatureVector() {
        return histogram;
    }


    @Override
    public double getDistance(LireFeature feature) {
        return MetricsUtils.distL1(histogram, ((LuminanceLayout) feature).histogram);
    }

//    public String getStringRepresentation() {
//        return null;
//    }
//
//    public void setStringRepresentation(String s) {
//    }

    // just a 8x8 jpeg dct ...
    @SuppressWarnings("unused")
    private double[] jpgDct(double[] histogram) {
        int[] zickzack = new int[]    { 0,  1,  8, 16,  9,  2,  3, 10, 17, 24, 32, 25, 18, 11,  4,  5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13,  6,  7, 14, 21, 28, 35, 42, 49, 56};
        double[] quant = new double[] {16, 5, 6, 7, 6, 5, 8, 7, 7, 7, 9, 8, 8, 9, 12, 20, 13, 12, 11, 11, 12, 25, 18, 18, 15, 20, 28, 25, 30, 30, 28, 25, 28, 27, 32, 36};
//        double[] quant = new double[] {16, 11, 12, 14, 12, 10, 16, 14, 13, 14, 18, 17, 16, 19, 24, 40, 26, 24, 22, 22, 24, 49, 35, 37, 29, 40, 58, 51, 61, 60, 57, 51, 56, 55, 64, 72};
        double[] coeffs = new double[histogram.length];
        int u, v;
        double au, av;
        for (int i = 0; i < coeffs.length; i++) {
            u = i % 8;
            v = i / 8;
            au = Math.sqrt(2d/6d);
            av = Math.sqrt(2d/6d);
            if (u==0) au = Math.sqrt(1d/6d);
            if (v==0) av = Math.sqrt(1d/6d);
            coeffs[i] = 0;
            for (int x = 0; x < 8; x++) {
                for (int y = 0; y < 8; y++) {
                    coeffs[i] += au*av*(histogram[i]-127)*Math.cos((Math.PI/8d)*(x+0.5)*u)*Math.cos((Math.PI/8d)*(y+0.5)*v);
                }
            }
//            coeffs[i] = Math.floor(coeffs[i]);
        }
        double[] result = new double[zickzack.length];
        for (int i = 0; i < zickzack.length; i++) {
//            result[i] = Math.round(coeffs[zickzack[i]]] / quant[i]);
            result[i] = Math.round(coeffs[zickzack[i]] );

        }
        return result;
    }

    @SuppressWarnings("unused")
    private double[] dct(double[] histogram) {
        double[] coeffs = new double[histogram.length / 8];
        double N = histogram.length;
        double min = 0, max = 0;
        for (int i = 0; i < coeffs.length; i++) {
            coeffs[i] = 0;
            for (int j = 0; j < N; j++) {
                coeffs[i] += histogram[j] * Math.cos((Math.PI / N) * (j + 0.5) * (i + 0.5));
            }
            min = Math.min(min, coeffs[i]);
            max = Math.max(max, coeffs[i]);
//            result[i] = Math.round(result[i]/1000d);
        }
        double factor = Math.max(max, Math.abs(min));
        for (int i = 0; i < coeffs.length; i++) {
            coeffs[i] = Math.floor(coeffs[i] / factor * 63d + 63d);
        }
        return coeffs;
    }

    @Override
    public String getFeatureName() {
        return "Luminance Layout";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_LUMINANCE_LAYOUT;
    }
}
