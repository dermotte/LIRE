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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 11.07.13 10:32
 */
package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.DocumentBuilder;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * A joint descriptor joining CEDD and FCTH in one histogram.
 *
 * @author: Savvas A. Chatzichristofis, savvash@gmail.com
 */
public class JCD implements LireFeature {
    int tmp;
    double result = 0;
    double temp1 = 0;
    double temp2 = 0;
    double TempCount1 = 0, TempCount2 = 0, TempCount3 = 0;

    protected double[] data = new double[168];

    public JCD(CEDD cedd, FCTH fcth) {
        init(cedd, fcth);
    }

    public JCD() {
    }


    public void extract(BufferedImage bimg) {
        CEDD c = new CEDD();
        c.extract(bimg);
        FCTH f = new FCTH();
        f.extract(bimg);
        init(c, f);
    }

//    public byte[] getByteArrayRepresentation() {
//        return SerializationUtils.toByteArray(data);
//    }
//
//    public void setByteArrayRepresentation(byte[] in) {
//        data = SerializationUtils.toDoubleArray(in);
//    }
//
//    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
//        data = SerializationUtils.toDoubleArray(in, offset, length);
//    }

    /**
     * Creates a small byte array from an JCD descriptor.
     * Stuffs 2 numbers into one byte and cuts trailing zeros.
     *
     * @return
     */
    public byte[] getByteArrayRepresentation() {
        // find out the position of the beginning of the trailing zeros.
        int position = -1;
        for (int i = 0; i < data.length; i++) {
            if (position == -1) {
                if (data[i] == 0) position = i;
            } else if (position > -1) {
                if (data[i] != 0) position = -1;
            }
        }
        if (position <0) position = data.length -1;
        // find out the actual length. two values in one byte, so we have to round up.
        int length = (position + 1) / 2;
        if ((position + 1) % 2 == 1) length = position / 2 + 1;
        byte[] result = new byte[length];
        for (int i = 0; i < result.length; i++) {
            tmp = ((int) (data[(i << 1)] * 2)) << 4;
            tmp = (tmp | ((int) (data[(i << 1) + 1] * 2)));
            result[i] = (byte) (tmp - 128);
        }
        return result;
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#getByteArrayRepresentation
     */
    public void setByteArrayRepresentation(byte[] in) {
        if (in.length * 2 < data.length) Arrays.fill(data, in.length * 2, data.length - 1, 0);
        for (int i = 0; i < in.length; i++) {
            tmp = in[i] + 128;
            data[(i << 1) + 1] = ((double) (tmp & 0x000F)) / 2d;
            data[i << 1] = ((double) (tmp >> 4)) / 2d;
        }
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        if ((length << 1) < data.length) Arrays.fill(data, length << 1, data.length - 1, 0);
        for (int i = offset; i < offset + length; i++) {
            tmp = in[i] + 128;
            data[((i - offset) << 1) + 1] = ((double) (tmp & 0x000F)) / 2d;
            data[(i - offset) << 1] = ((double) (tmp >> 4)) / 2d;
        }
    }

    public double[] getDoubleHistogram() {
        return data;
    }

    public void init(CEDD c, FCTH f) {
        data = joinHistograms(c.data, f.histogram);
    }

    public float getDistance(LireFeature vd) {
        // Check if instance of the right class ...
        if (!(vd instanceof JCD))
            throw new UnsupportedOperationException("Wrong descriptor.");

        // check if parameters are fitting ...
        if ((((JCD) vd).data.length != data.length))
            throw new UnsupportedOperationException("Histogram lengths or color spaces do not match");

        // Tanimoto coefficient
        result = 0;
        temp1 = 0;
        temp2 = 0;

        TempCount1 = 0;
        TempCount2 = 0;
        TempCount3 = 0;

        for (int i = 0; i < ((JCD) vd).data.length; i++) {
            temp1 += ((JCD) vd).data[i];
            temp2 += data[i];
        }

        if (temp1 == 0 && temp2 == 0) return 0f;
        if (temp1 == 0 || temp2 == 0) return 100f;

        for (int i = 0; i < ((JCD) vd).data.length; i++) {
            TempCount1 += (((JCD) vd).data[i] / temp1) * (data[i] / temp2);
            TempCount2 += (data[i] / temp2) * (data[i] / temp2);
            TempCount3 += (((JCD) vd).data[i] / temp1) * (((JCD) vd).data[i] / temp1);

        }

        result = (100 - 100 * (TempCount1 / (TempCount2 + TempCount3
                - TempCount1))); //Tanimoto
        return (float) result;

    }

    public String getStringRepresentation() {
        throw new UnsupportedOperationException("This is not meant to be used!");
    }

    public void setStringRepresentation(String s) {
        throw new UnsupportedOperationException("This is not meant to be used!");
    }

    private double[] joinHistograms(double[] CEDD, double[] FCTH) {

        double[] JointDescriptor = new double[168];

        double[] TempTable1 = new double[24];
        double[] TempTable2 = new double[24];
        double[] TempTable3 = new double[24];
        double[] TempTable4 = new double[24];

        for (int i = 0; i < 24; i++) {
            TempTable1[i] = FCTH[0 + i] + FCTH[96 + i];
            TempTable2[i] = FCTH[24 + i] + FCTH[120 + i];
            TempTable3[i] = FCTH[48 + i] + FCTH[144 + i];
            TempTable4[i] = FCTH[72 + i] + FCTH[168 + i];

        }


        for (int i = 0; i < 24; i++) {
            JointDescriptor[i] = (TempTable1[i] + CEDD[i]) / 2;
            JointDescriptor[24 + i] = (TempTable2[i] + CEDD[48 + i]) / 2;
            JointDescriptor[48 + i] = CEDD[96 + i];
            JointDescriptor[72 + i] = (TempTable3[i] + CEDD[72 + i]) / 2;
            JointDescriptor[96 + i] = CEDD[120 + i];
            JointDescriptor[120 + i] = TempTable4[i];
            JointDescriptor[144 + i] = CEDD[24 + i];

        }

        return (JointDescriptor);

    }

    @Override
    public String getFeatureName() {
        return "JCD";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_JCD;
    }
}
