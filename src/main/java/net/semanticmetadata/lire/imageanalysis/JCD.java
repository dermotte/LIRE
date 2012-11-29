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
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */
package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;

/**
 * A joint descriptor joining CEDD and FCTH in one histogram.
 *
 * @author: Savvas A. Chatzichristofis, savvash@gmail.com
 */
public class JCD implements LireFeature {
    protected double[] data;

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

    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(data);
    }

    public void setByteArrayRepresentation(byte[] in) {
        data = SerializationUtils.toDoubleArray(in);
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        data = SerializationUtils.toDoubleArray(in, offset, length);
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
        double Result = 0;
        double Temp1 = 0;
        double Temp2 = 0;

        double TempCount1 = 0, TempCount2 = 0, TempCount3 = 0;

        for (int i = 0; i < ((JCD) vd).data.length; i++) {
            Temp1 += ((JCD) vd).data[i];
            Temp2 += data[i];
        }

        if (Temp1 == 0 || Temp2 == 0) Result = 100;
        if (Temp1 == 0 && Temp2 == 0) Result = 0;

        if (Temp1 > 0 && Temp2 > 0) {
            for (int i = 0; i < ((JCD) vd).data.length; i++) {
                TempCount1 += (((JCD) vd).data[i] / Temp1) * (data[i] / Temp2);
                TempCount2 += (data[i] / Temp2) * (data[i] / Temp2);
                TempCount3 += (((JCD) vd).data[i] / Temp1) * (((JCD) vd).data[i] / Temp1);

            }

            Result = (100 - 100 * (TempCount1 / (TempCount2 + TempCount3
                    - TempCount1))); //Tanimoto
        }
        return (float) Result;

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


}
