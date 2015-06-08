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
 * Updated: 11.07.13 10:07
 */

package net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.RotationInvariantLocalBinaryPatterns;
import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.image.BufferedImage;

/**
 * A spatial pyramid version of the rotation invariant local binary pattern feature.
 * @author Mathias Lux, mathias@juggle.at
 * Date: 21.06.13, 15:38
 */
public class SPLBP implements GlobalFeature {
    int histogramSize = 36 * 5 + 36 * 4 * 4;
    double[] histogram = new double[histogramSize];

    // Temp:
    int tmp;

    @Override
    public void extract(BufferedImage bimg) {
        // level 0:
        RotationInvariantLocalBinaryPatterns feature = new RotationInvariantLocalBinaryPatterns();
        feature.extract(bimg);
        System.arraycopy(feature.getFeatureVector(), 0, histogram, 0, 36);
        // level 1:
        int w = bimg.getWidth() / 2;
        int h = bimg.getHeight() / 2;
        feature.extract(bimg.getSubimage(0, 0, w, h));
        System.arraycopy(feature.getFeatureVector(), 0, histogram, 36 * 1, 36);
        feature.extract(bimg.getSubimage(w, 0, w, h));
        System.arraycopy(feature.getFeatureVector(), 0, histogram, 36 * 2, 36);
        feature.extract(bimg.getSubimage(0, h, w, h));
        System.arraycopy(feature.getFeatureVector(), 0, histogram, 36 * 3, 36);
        feature.extract(bimg.getSubimage(w, h, w, h));
        System.arraycopy(feature.getFeatureVector(), 0, histogram, 36 * 4, 36);
        // level 2:
        int wstep = bimg.getWidth() / 4;
        int hstep = bimg.getHeight() / 4;
        int binPos = 5; // the next free section in the histogram
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                feature.extract(bimg.getSubimage(i * wstep, j * hstep, wstep, hstep));
                System.arraycopy(feature.getFeatureVector(), 0, histogram, 36 * binPos, 36);
                binPos++;
            }
        }

    }

    /**
     * Provides a faster way of serialization.
     *
     * @return a byte array that can be read with the corresponding method.
     * @see SPCEDD#setByteArrayRepresentation(byte[])
     */
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[histogram.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) histogram[i];
        }
        return result;
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see SPCEDD#getByteArrayRepresentation
     */
    public void setByteArrayRepresentation(byte[] in) {
        setByteArrayRepresentation(in, 0, in.length);
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        for (int i = offset; i < offset + length; i++) {
            tmp = in[i] + 128;
            histogram[i-offset] = in[i];
        }
    }

    @Override
    public double[] getFeatureVector() {
        return histogram;
    }

    @Override
    public double getDistance(LireFeature feature) {
        if (!(feature instanceof SPLBP)) return -1;
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
        return "Spatial Pyramid of Local Binary Patterns (simple)";
    }

    @Override
    public String getFieldName() {
        return "f_splbp";
    }
}
