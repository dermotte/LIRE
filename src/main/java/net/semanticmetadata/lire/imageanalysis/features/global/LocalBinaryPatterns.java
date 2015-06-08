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
 * Updated: 11.07.13 10:31
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
 * A simple implementation of the original local binary pattern texture feature.
 * @author Mathias Lux, mathias@juggle.at
 * Time: 21.06.13 13:51
 */
public class LocalBinaryPatterns implements GlobalFeature {
    double[] histogram = new double[256];

    @Override
    public void extract(BufferedImage image) {
        Arrays.fill(histogram, 0d);
        extractRadiusWithOne(image);
    }

    private void extractRadiusWithOne(BufferedImage image) {
        // first convert to intensity only.
        WritableRaster raster = ImageUtils.getGrayscaleImage(image).getRaster();
        // cached pixel array
        int[] pixel = new int[9];
        int bin = 0;
        // now fill histogram according to LBP definition.
        for (int x = 0; x < raster.getWidth() - 2; x++) {
            for (int y = 0; y < raster.getHeight() - 2; y++) {
                raster.getPixels(x, y, 3, 3, pixel);
                if (pixel[0] >= pixel[4]) bin += 1;
                if (pixel[1] >= pixel[4]) bin += 2;
                if (pixel[2] >= pixel[4]) bin += 4;
                if (pixel[5] >= pixel[4]) bin += 8;
                if (pixel[8] >= pixel[4]) bin += 16;
                if (pixel[7] >= pixel[4]) bin += 32;
                if (pixel[6] >= pixel[4]) bin += 64;
                if (pixel[3] >= pixel[4]) bin += 128;
                histogram[bin]++;
                bin = 0;
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

    @SuppressWarnings("unused")
    private void extractWithRadiusTwo(BufferedImage image) {
        // first convert to intensity only.
        WritableRaster raster = ImageUtils.getGrayscaleImage(image).getRaster();
        // cached pixel array
        int[] pixel = new int[25];
        int bin = 0;
        // now fill histogram according to LBP definition.
        for (int x = 0; x < raster.getWidth() - 4; x++) {
            for (int y = 0; y < raster.getHeight() - 4; y++) {
                raster.getPixels(x, y, 5, 5, pixel);
                if (pixel[1] >= pixel[12]) bin += 1;
                if (pixel[2] >= pixel[12]) bin += 2;
                if (pixel[3] >= pixel[12]) bin += 4;
                if (pixel[9] >= pixel[12]) bin += 8;
                if (pixel[14] >= pixel[12]) bin += 16;
                if (pixel[19] >= pixel[12]) bin += 32;
                if (pixel[23] >= pixel[12]) bin += 64;
                if (pixel[22] >= pixel[12]) bin += 128;
                if (pixel[21] >= pixel[12]) bin += 256;
                if (pixel[15] >= pixel[12]) bin += 512;
                if (pixel[10] >= pixel[12]) bin += 1024;
                if (pixel[5] >= pixel[12]) bin += 2048;
                histogram[bin]++;
                bin = 0;
            }
        }
        // normalize & quantize histogram.
        double max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(histogram[i], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = Math.floor((histogram[i] / max) * 128);
        }
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
        for (int i = 0; i < length; i++) {
            histogram[i] = in[i+offset];
        }
    }

    @Override
    public double[] getFeatureVector() {
        return histogram;
    }

    @Override
    public double getDistance(LireFeature feature) {
        return MetricsUtils.distL1(histogram, feature.getFeatureVector());
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
        return "Local Binary Patterns";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_LOCAL_BINARY_PATTERNS;
    }
}
