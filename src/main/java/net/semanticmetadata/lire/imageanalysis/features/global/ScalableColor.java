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
 * Updated: 11.07.13 10:38
 */
package net.semanticmetadata.lire.imageanalysis.features.global;


import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.mpeg7.ScalableColorImpl;
import net.semanticmetadata.lire.utils.ConversionUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Just a wrapper for the use of LireFeature.
 * Date: 27.08.2008
 * Time: 12:12:01
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ScalableColor extends ScalableColorImpl implements GlobalFeature {

    @Override
    public byte[] getByteArrayRepresentation() {
        /*
        builder.append("scalablecolor;");
        builder.append(NumberOfBitplanesDiscarded);
        builder.append(';');
        builder.append(NumberOfCoefficients);
        builder.append(';');
        for (int i = 0; i < NumberOfCoefficients; i++) {
            builder.append(haarTransformedHistogram[i]);
            if ((i + 1) < NumberOfCoefficients) builder.append(' ');
        }
        return builder.toString();
        */
        int[] result = new int[NumberOfCoefficients + 2];
        result[0] = NumberOfBitplanesDiscarded;
        result[1] = NumberOfCoefficients;
        for (int i = 2; i < result.length; i++) {
            result[i] = haarTransformedHistogram[i - 2];
        }
        return SerializationUtils.toByteArray(result);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        int[] result = SerializationUtils.toIntArray(in);
        NumberOfBitplanesDiscarded = result[0];
        NumberOfCoefficients = result[1];
        haarTransformedHistogram = new int[result.length - 2];
        for (int i = 2; i < result.length; i++) {
            haarTransformedHistogram[i - 2] = result[i];
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        int[] result = SerializationUtils.toIntArray(in, offset, length);
        NumberOfBitplanesDiscarded = result[0];
        NumberOfCoefficients = result[1];
        haarTransformedHistogram = new int[result.length - 2];
        for (int i = 2; i < result.length; i++) {
            haarTransformedHistogram[i - 2] = result[i];
        }
    }

    @Override
    public double[] getFeatureVector() {
        int[] result = new int[NumberOfCoefficients];
        for (int i = 2; i < result.length; i++) {
            result[i] = haarTransformedHistogram[i];
        }
        return ConversionUtils.toDouble(result);
    }

    @Override
    public String getFeatureName() {
        return "MPEG-7 Scalable Color";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_SCALABLECOLOR;
    }
}
