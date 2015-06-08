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
import net.semanticmetadata.lire.imageanalysis.features.global.mpeg7.ColorLayoutImpl;

/**
 * Just a wrapper for the use of LireFeature.
 * Date: 27.08.2008
 * Time: 12:07:38
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ColorLayout extends ColorLayoutImpl implements GlobalFeature {
    /**
     * Provides a much faster way of serialization.
     *
     * @return a byte array that can be read with the corresponding method.
     * @see CEDD#setByteArrayRepresentation(byte[])
     */
    @Override
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[2 + numYCoeff + 2 * numCCoeff];
        result[0] = (byte) numYCoeff;
        result[1] = (byte) numCCoeff;
        for (int i = 0; i < numYCoeff; i++) {
            result[2 + i] = (byte) YCoeff[i];
        }
        for (int i = 0; i < numCCoeff; i++) {
            result[2 + numYCoeff + i] = (byte) CbCoeff[i];
            result[2 + numYCoeff + numCCoeff + i] = (byte) CrCoeff[i];
        }
        return result;
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see CEDD#getByteArrayRepresentation
     */
    @Override
    public void setByteArrayRepresentation(byte[] in) {
        numYCoeff = in[0];
        numCCoeff = in[1];
        for (int i = 0; i < numYCoeff; i++) {
            YCoeff[i] = in[2 + i];
        }
        for (int i = 0; i < numCCoeff; i++) {
            CbCoeff[i] = in[i + 2 + numYCoeff];
            CrCoeff[i] = in[i + 2 + numYCoeff + numCCoeff];
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        numYCoeff = in[0 + offset];
        numCCoeff = in[1 + offset];
        for (int i = 0; i < numYCoeff; i++) {
            YCoeff[i] = in[2 + i + offset];
        }
        for (int i = 0; i < numCCoeff; i++) {
            CbCoeff[i] = in[i + 2 + numYCoeff + offset];
            CrCoeff[i] = in[i + 2 + numYCoeff + numCCoeff + offset];
        }

    }

    @Override
    public double[] getFeatureVector() {
        double[] result = new double[numYCoeff + numCCoeff * 2];
        for (int i = 0; i < numYCoeff; i++) {
            result[i] = YCoeff[i];
        }
        for (int i = 0; i < numCCoeff; i++) {
            result[i + numYCoeff] = CbCoeff[i];
            result[i + numCCoeff + numYCoeff] = CrCoeff[i];
        }
        return result;
    }

    /**
     * Compares one descriptor to another.
     *
     * @param descriptor
     * @return the distance from [0,infinite) or -1 if descriptor type does not match
     */

    @Override
    public double getDistance(LireFeature descriptor) {
        if (!(descriptor instanceof ColorLayoutImpl)) return -1d;
        ColorLayoutImpl cl = (ColorLayoutImpl) descriptor;
        return getSimilarity(YCoeff, CbCoeff, CrCoeff, cl.YCoeff, cl.CbCoeff, cl.CrCoeff);
    }

    @Override
    public String getFeatureName() {
        return "MPEG-7 Color Layout";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_COLORLAYOUT;
    }
}
