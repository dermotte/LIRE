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
package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.imageanalysis.mpeg7.ColorLayoutImpl;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Just a wrapper for the use of LireFeature.
 * Date: 27.08.2008
 * Time: 12:07:38
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ColorLayout extends ColorLayoutImpl implements LireFeature {

    /*
        public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(256);
        StringBuilder sbtmp = new StringBuilder(256);
        for (int i = 0; i < numYCoeff; i++) {
            sb.append(YCoeff[i]);
            if (i + 1 < numYCoeff) sb.append(' ');
        }
        sb.append("z");
        for (int i = 0; i < numCCoeff; i++) {
            sb.append(CbCoeff[i]);
            if (i + 1 < numCCoeff) sb.append(' ');
            sbtmp.append(CrCoeff[i]);
            if (i + 1 < numCCoeff) sbtmp.append(' ');
        }
        sb.append("z");
        sb.append(sbtmp);
        return sb.toString();
    }

    public void setStringRepresentation(String descriptor) {
        String[] coeffs = descriptor.split("z");
        String[] y = coeffs[0].split(" ");
        String[] cb = coeffs[1].split(" ");
        String[] cr = coeffs[2].split(" ");

        numYCoeff = y.length;
        numCCoeff = Math.min(cb.length, cr.length);

        YCoeff = new int[numYCoeff];
        CbCoeff = new int[numCCoeff];
        CrCoeff = new int[numCCoeff];

        for (int i = 0; i < numYCoeff; i++) {
            YCoeff[i] = Integer.parseInt(y[i]);
        }
        for (int i = 0; i < numCCoeff; i++) {
            CbCoeff[i] = Integer.parseInt(cb[i]);
            CrCoeff[i] = Integer.parseInt(cr[i]);

        }
    }
     */

    /**
     * Provides a much faster way of serialization.
     *
     * @return a byte array that can be read with the corresponding method.
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#setByteArrayRepresentation(byte[])
     */
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[2 * 4 + numYCoeff * 4 + 2 * numCCoeff * 4];
        System.arraycopy(SerializationUtils.toBytes(numYCoeff), 0, result, 0, 4);
        System.arraycopy(SerializationUtils.toBytes(numCCoeff), 0, result, 4, 4);
        System.arraycopy(SerializationUtils.toByteArray(YCoeff), 0, result, 8, numYCoeff * 4);
        System.arraycopy(SerializationUtils.toByteArray(CbCoeff), 0, result, numYCoeff * 4 + 8, numCCoeff * 4);
        System.arraycopy(SerializationUtils.toByteArray(CrCoeff), 0, result, numYCoeff * 4 + numCCoeff * 4 + 8, numCCoeff * 4);
        return result;
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#getByteArrayRepresentation
     */
    public void setByteArrayRepresentation(byte[] in) {
        int[] data = SerializationUtils.toIntArray(in);
        numYCoeff = data[0];
        numCCoeff = data[1];
        YCoeff = new int[numYCoeff];
        CbCoeff = new int[numCCoeff];
        CrCoeff = new int[numCCoeff];
        System.arraycopy(data, 2, YCoeff, 0, numYCoeff);
        System.arraycopy(data, 2 + numYCoeff, CbCoeff, 0, numCCoeff);
        System.arraycopy(data, 2 + numYCoeff + numCCoeff, CrCoeff, 0, numCCoeff);
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        int[] data = SerializationUtils.toIntArray(in, offset, length);
        numYCoeff = data[0];
        numCCoeff = data[1];
        YCoeff = new int[numYCoeff];
        CbCoeff = new int[numCCoeff];
        CrCoeff = new int[numCCoeff];
        System.arraycopy(data, 2, YCoeff, 0, numYCoeff);
        System.arraycopy(data, 2 + numYCoeff, CbCoeff, 0, numCCoeff);
        System.arraycopy(data, 2 + numYCoeff + numCCoeff, CrCoeff, 0, numCCoeff);
    }

    public double[] getDoubleHistogram() {
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

    public float getDistance(LireFeature descriptor) {
        if (!(descriptor instanceof ColorLayoutImpl)) return -1f;
        ColorLayoutImpl cl = (ColorLayoutImpl) descriptor;
        return (float) ColorLayoutImpl.getSimilarity(YCoeff, CbCoeff, CrCoeff, cl.YCoeff, cl.CbCoeff, cl.CrCoeff);
    }
}
