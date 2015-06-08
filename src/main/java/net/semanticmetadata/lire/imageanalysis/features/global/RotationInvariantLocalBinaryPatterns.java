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
 * Updated: 11.07.13 10:37
 */

package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;

/**
 * A simple implementation of the rotation invariant local binary pattern feature.
 * @author Mathias Lux, mathias@juggle.at
 * Time: 21.06.13 13:51
 */
public class RotationInvariantLocalBinaryPatterns implements GlobalFeature {
    double[] histogram = new double[36];
    // used to find the right bin for the class of rotated LBP features.
    static int[] binTranslate = new int[256];

    static {
        Arrays.fill(binTranslate, 0);
        binTranslate[0] = 0;
        binTranslate[1] = 1;
        binTranslate[3] = 2;
        binTranslate[5] = 3;
        binTranslate[7] = 4;
        binTranslate[9] = 5;
        binTranslate[11] = 6;
        binTranslate[13] = 7;
        binTranslate[15] = 8;
        binTranslate[17] = 9;
        binTranslate[19] = 10;
        binTranslate[21] = 11;
        binTranslate[23] = 12;
        binTranslate[25] = 13;
        binTranslate[27] = 14;
        binTranslate[29] = 15;
        binTranslate[31] = 16;
        binTranslate[37] = 17;
        binTranslate[39] = 18;
        binTranslate[43] = 19;
        binTranslate[45] = 20;
        binTranslate[47] = 21;
        binTranslate[51] = 22;
        binTranslate[53] = 23;
        binTranslate[55] = 24;
        binTranslate[59] = 25;
        binTranslate[61] = 26;
        binTranslate[63] = 27;
        binTranslate[85] = 28;
        binTranslate[87] = 29;
        binTranslate[91] = 30;
        binTranslate[95] = 31;
        binTranslate[111] = 32;
        binTranslate[119] = 33;
        binTranslate[127] = 34;
        binTranslate[255] = 35;
    }

    @Override
    public void extract(BufferedImage image) {
        Arrays.fill(histogram, 0d);
        extractWithRadiusOne(image);
    }

    /**
     * Extracts the classical, radius = 1 version.
     * @param image
     */
    private void extractWithRadiusOne(BufferedImage image) {
        // first convert to intensity only.
        WritableRaster raster = ImageUtils.getGrayscaleImage(image).getRaster();
        // cached pixel array
        int[] pixel = new int[9];
        int[] pattern = new int[8];
        // now fill histogram according to LBP definition.
        for (int x = 0; x < raster.getWidth() - 2; x++) {
            for (int y = 0; y < raster.getHeight() - 2; y++) {
                Arrays.fill(pattern, 0);
                raster.getPixels(x, y, 3, 3, pixel);
                if (pixel[0] >= pixel[4]) pattern[0] = 1;
                if (pixel[1] >= pixel[4]) pattern[1] = 1;
                if (pixel[2] >= pixel[4]) pattern[2] = 1;
                if (pixel[5] >= pixel[4]) pattern[3] = 1;
                if (pixel[8] >= pixel[4]) pattern[4] = 1;
                if (pixel[7] >= pixel[4]) pattern[5] = 1;
                if (pixel[6] >= pixel[4]) pattern[6] = 1;
                if (pixel[3] >= pixel[4]) pattern[7] = 1;
                histogram[getBin(pattern)]++;
            }
        }
        // normalize & quantize histogram.
        double max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(histogram[i], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = Math.floor((histogram[i] / max) * 127);
        }
    }

    private int getBin(int[] pattern) {
        // add the rotation invariant code here ...
        int min = Integer.MAX_VALUE;
        for (int i = 0; i < 8; i++) {
            min = Math.min(getNumber(pattern), min);
            // rotate:
            int tmp = pattern[7];
            for (int j = pattern.length-1; j > 0; j--) {
                pattern[j] = pattern[j-1];
            }
            pattern[0] = tmp;
        }
        return binTranslate[min];
    }

    private int getNumber(int[] pattern) {
        int result = 0;
        int current = 1;
        for (int i = 0; i < pattern.length; i++) {
            if (pattern[i]>0) result+=current;
            current*=2;
        }
        return result;
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        byte[] rep = new byte[histogram.length];
        for (int i = 0; i < histogram.length; i++) {
            rep[i] = (byte) histogram[i];
        }
        return rep;
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        setByteArrayRepresentation(in, 0, in.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            histogram[i - offset] = in[i];
        }
    }

    @Override
    public double[] getFeatureVector() {
        return histogram;
    }

    @Override
    public double getDistance(LireFeature feature) {
        return  MetricsUtils.distL1(histogram, feature.getFeatureVector());
    }

//    @Override
//    public String getStringRepresentation() {
//        throw new UnsupportedOperationException("Not implemented!");
//    }
//
//    @Override
//    public void setStringRepresentation(String s) {
//        throw new UnsupportedOperationException("Not implemented!");
//    }

    @Override
    public String getFeatureName() {
        return "Rotation Invariant LBP";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_ROTATION_INVARIANT_LOCAL_BINARY_PATTERNS;
    }
}
