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
 * ~~~~~~~~~~~~~~~~~~~~
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */
package net.semanticmetadata.lire.imageanalysis;


import net.semanticmetadata.lire.imageanalysis.mpeg7.ScalableColorImpl;
import net.semanticmetadata.lire.utils.ConversionUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Just a wrapper for the use of LireFeature.
 * Date: 27.08.2008
 * Time: 12:12:01
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ScalableColor extends ScalableColorImpl implements LireFeature {

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

    public void setByteArrayRepresentation(byte[] in) {
        int[] result = SerializationUtils.toIntArray(in);
        NumberOfBitplanesDiscarded = result[0];
        NumberOfCoefficients = result[1];
        haarTransformedHistogram = new int[result.length - 2];
        for (int i = 2; i < result.length; i++) {
            haarTransformedHistogram[i - 2] = result[i];
        }
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        int[] result = SerializationUtils.toIntArray(in, offset, length);
        NumberOfBitplanesDiscarded = result[0];
        NumberOfCoefficients = result[1];
        haarTransformedHistogram = new int[result.length - 2];
        for (int i = 2; i < result.length; i++) {
            haarTransformedHistogram[i - 2] = result[i];
        }
    }

    public double[] getDoubleHistogram() {
        int[] result = new int[NumberOfCoefficients];
        for (int i = 2; i < result.length; i++) {
            result[i] = haarTransformedHistogram[i];
        }
        return ConversionUtils.toDouble(result);
    }
}
