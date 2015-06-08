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
import net.semanticmetadata.lire.imageanalysis.features.global.mpeg7.EdgeHistogramImplementation;
import net.semanticmetadata.lire.utils.ConversionUtils;

/**
 * Just a wrapper for the use of LireFeature.
 * Date: 27.08.2008
 * Time: 12:14:19
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class EdgeHistogram extends EdgeHistogramImplementation implements GlobalFeature {
    private int tmp;


    /**
     * Creates a 40 byte array from an edge histogram descriptor.
     * Stuffs 2 numbers into one byte.
     * @return
     */
    @Override
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[edgeHistogram.length/2];
        for (int i = 0; i < result.length; i++) {
            tmp = ((int) (edgeHistogram[(i << 1)])) << 4;
            tmp = (tmp | ((int) (edgeHistogram[(i << 1) + 1])));
            result[i] = (byte) (tmp - 128);
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
        setByteArrayRepresentation(in, 0, in.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        for (int i = 0; i < length; i++) {
            tmp = in[offset+i] + 128;
            edgeHistogram[(i << 1) + 1] = ((tmp & 0x000F));
            edgeHistogram[i << 1] = ((tmp >> 4));
        }
    }


    /*
    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(edgeHistogram);
    }

    public void setByteArrayRepresentation(byte[] in) {
        edgeHistogram = SerializationUtils.toIntArray(in);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        edgeHistogram = SerializationUtils.toIntArray(in, offset, length);
    }
    */
    @Override
    public double[] getFeatureVector() {
        return ConversionUtils.toDouble(edgeHistogram);
    }

    @Override
    public String getFeatureName() {
        return "MPEG-7 Edge Histogram";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM;
    }
}
