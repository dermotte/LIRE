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
 * (c) 2017 by Savvas Chatzichristofis (savvash@gmail.com), Nektarios Anagnostopoulos (nek.anag@gmail.com) & Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 */

package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.como.*;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * COMO global feature
 *
 * @author Savvas Chatzichristofis, savvash@gmail.com
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * @author Mathias Lux, mathias@juggle.at
 */
public class COMO implements GlobalFeature {
    private byte[] histogram = new byte[144];
    private COMOQuant quants = new COMOQuant();
    private int tmp;
    private COMO tmpFeature;
    private double Result, Temp1, Temp2, TempCount1, TempCount2, TempCount3, iTmp1, iTmp2;

    public COMO() {
    }

    @Override
    public void extract(BufferedImage image) {
        image = ImageUtils.get8BitRGBImage(image);

        int width = image.getWidth(), height = image.getHeight();

        double[][] ImageGrid = new double[width][height];
        int[][] ImageGridRed = new int[width][height];
        int[][] ImageGridGreen = new int[width][height];
        int[][] ImageGridBlue = new int[width][height];
        // extraction is based on a speedup fix from Michael Riegler & Konstantin Pogorelov
        BufferedImage image_rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        image_rgb.getGraphics().drawImage(image, 0, 0, null);
        int[] pixels = ((DataBufferInt) image_rgb.getRaster().getDataBuffer()).getData();
        int pixel, r, g, b;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixel = pixels[y * width + x];
                b = (pixel >> 16) & 0xFF;
                g = (pixel >> 8) & 0xFF;
                r = (pixel) & 0xFF;
                ImageGridRed[x][y] = r;
                ImageGridGreen[x][y] = g;
                ImageGridBlue[x][y] = b;

                ImageGrid[x][y] = Math.round((0.114 * b + 0.587 * g + 0.299 * r));
            }
        }


        int NumberOfBlocks = -1;
        if (Math.min(width, height) >= 80)
            NumberOfBlocks = 1600;
        else if (Math.min(width, height) >= 40)
            NumberOfBlocks = 400;


        int Step_X = 2, Step_Y = 2, TemoMAX_X, TemoMAX_Y;
        if (NumberOfBlocks > 0) {
            double sqrtNumberOfBlocks = Math.sqrt(NumberOfBlocks);
            Step_X = (int) Math.floor(width / sqrtNumberOfBlocks);
            Step_Y = (int) Math.floor(height / sqrtNumberOfBlocks);

            if ((Step_X % 2) != 0) {
                Step_X = Step_X - 1;
            }
            if ((Step_Y % 2) != 0) {
                Step_Y = Step_Y - 1;
            }

            TemoMAX_X = Step_X * (int) sqrtNumberOfBlocks;
            TemoMAX_Y = Step_Y * (int) sqrtNumberOfBlocks;
        } else {
            TemoMAX_X = Step_X * (int) Math.floor(width >> 1);
            TemoMAX_Y = Step_Y * (int) Math.floor(height >> 1);
        }


        double[] COMO = new double[144];
        Arrays.fill(COMO, 0.0);

        double m00, m10, m01, m11, m20, m02, m30, m03, m12, m21, tmpm00, tmpm10, tmpm01, tmpm11, tmpm20, tmpm02;
        double xbar, ybar, temp1, temp2, eta11, eta20, eta02, eta30, eta03, eta21, eta12, iii, jjj, max;
        int ii, jj, MeanRed, MeanGreen, MeanBlue, T, area = Step_Y * Step_X;
        double[] hist = new double[256], moments = new double[7], distances = new double[HuMoments.HuMomentsTable.length];
        int[] HSV, Edges = new int[HuMoments.HuMomentsTable.length];
        RGB2HSV HSVConverter = new RGB2HSV();
        double[] Fuzzy10BinResultTable, Fuzzy24BinResultTable;
        Fuzzy10Bin Fuzzy10 = new Fuzzy10Bin(false);
        Fuzzy24Bin Fuzzy24 = new Fuzzy24Bin(false);

        for (int y = 0; y < TemoMAX_Y; y += Step_Y) {
            for (int x = 0; x < TemoMAX_X; x += Step_X) {
                Arrays.fill(moments, 0.0);
                Arrays.fill(hist, 0.0);
                Arrays.fill(Edges, -1);
                MeanRed = 0;
                MeanGreen = 0;
                MeanBlue = 0;
                m00 = 0.0;
                m10 = 0.0;
                m01 = 0.0;
                m11 = 0.0;
                m20 = 0.0;
                m02 = 0.0;
                m30 = 0.0;
                m03 = 0.0;
                m12 = 0.0;
                m21 = 0.0;

                ii = y;
                iii = 1.0;
                for (double i = y; i < y + Step_Y; i++, ii++, iii++) {
                    jj = x;
                    jjj = 1.0;
                    for (double j = x; j < x + Step_X; j++, jj++, jjj++) {
                        MeanRed += ImageGridRed[jj][ii];
                        MeanGreen += ImageGridGreen[jj][ii];
                        MeanBlue += ImageGridBlue[jj][ii];

                        hist[(int) ImageGrid[jj][ii]]++;
                        tmpm00 = ImageGrid[jj][ii];
                        tmpm10 = (jjj) * tmpm00;
                        tmpm01 = (iii) * tmpm00;
                        tmpm11 = (jjj) * tmpm01;
                        tmpm20 = (jjj) * tmpm10;
                        tmpm02 = (iii) * tmpm01;

                        m00 += tmpm00;
                        m10 += tmpm10;
                        m01 += tmpm01;
                        m11 += tmpm11;
                        m20 += tmpm20;
                        m02 += tmpm02;
                        m30 += (jjj) * tmpm20;
                        m03 += (iii) * tmpm02;
                        m12 += (jjj) * tmpm02;
                        m21 += (jjj) * tmpm11;
                    }
                }

                double entropy = 0.0;
                for (double next : hist) {
                    if (next > 0) {
                        next /= (double) area;
                        entropy -= next * (Math.log(next) / Math.log(2));
                    }
                }

                if (entropy >= 1) {

                    if (m00 == 0) {
                        m00 = Math.ulp(1.0);
                    }

                    xbar = m10 / m00;
                    ybar = m01 / m00;

                    temp1 = m00 * m00;
                    temp2 = Math.pow(m00, 2.5);
                    eta11 = (m11 - ybar * m10) / temp1;
                    eta20 = (m20 - xbar * m10) / temp1;
                    eta02 = (m02 - ybar * m01) / temp1;
                    eta30 = (m30 - 3.0 * xbar * m20 + 2.0 * xbar * xbar * m10) / temp2;
                    eta03 = (m03 - 3.0 * ybar * m02 + 2.0 * ybar * ybar * m01) / temp2;
                    eta21 = (m21 - 2.0 * xbar * m11 - ybar * m20 + 2.0 * xbar * xbar * m01) / temp2;
                    eta12 = (m12 - 2.0 * ybar * m11 - xbar * m02 + 2.0 * ybar * ybar * m10) / temp2;

                    temp1 = (eta30 + eta12) * (eta30 + eta12);
                    temp2 = (eta21 + eta03) * (eta21 + eta03);
                    moments[0] = eta20 + eta02;
                    moments[1] = (eta20 - eta02) * (eta20 - eta02) + 4.0 * eta11 * eta11;
                    moments[2] = (eta30 - 3.0 * eta12) * (eta30 - 3.0 * eta12) + (3.0 * eta21 - eta03) * (3.0 * eta21 - eta03);
                    moments[3] = temp1 + temp2;
                    moments[4] = (eta30 - 3.0 * eta12) * (eta30 + eta12) * (temp1 - 3.0 * temp2) + (3.0 * eta21 - eta03) * (eta21 + eta03) * (3.0 * temp1 - temp2);
                    moments[5] = (eta20 - eta02) * (temp1 - temp2) + 4.0 * eta11 * (eta30 + eta12) * (eta21 + eta03);
                    moments[6] = (3.0 * eta21 - eta03) * (eta30 + eta12) * (temp1 - 3.0 * temp2) - (eta30 - 3.0 * eta12) * (eta21 + eta03) * (3.0 * temp1 - temp2);
//                    moments[7] = eta11 * (temp1 - temp2) - (eta20 - eta02) * (eta30 + eta12) * (eta03 + eta21);

                    distances[0] = MetricsUtils.distL2(moments, HuMoments.HuMomentsTable[0]);
                    max = distances[0];
                    for (int i = 1; i < HuMoments.HuMomentsTable.length; i++) {
                        distances[i] = MetricsUtils.distL2(moments, HuMoments.HuMomentsTable[i]);
                        if (distances[i] > max) {
                            max = distances[i];
                        }
                    }
                    for (int i = 0; i < distances.length; i++) {
                        distances[i] /= max;
                    }

                    T = -1;
                    for (int i = 0; i < distances.length; i++) {
                        if (distances[i] < Thresholds.ThresholdsTable[i]) {
                            Edges[++T] = i;
                        }
                    }

                    MeanRed /= area;
                    MeanGreen /= area;
                    MeanBlue /= area;

                    HSV = HSVConverter.ApplyFilter(MeanRed, MeanGreen, MeanBlue);

                    Fuzzy10BinResultTable = Fuzzy10.ApplyFilter(HSV[0], HSV[1], HSV[2], 2);
                    Fuzzy24BinResultTable = Fuzzy24.ApplyFilter(HSV[0], HSV[1], HSV[2], Fuzzy10BinResultTable, 2);

                    for (int i = 0; i <= T; i++) {
                        for (int j = 0; j < 24; j++) {
                            if (Fuzzy24BinResultTable[j] > 0)
                                COMO[24 * Edges[i] + j] += Fuzzy24BinResultTable[j];
                        }
                    }
                }
            }
        }

        double sum = 0.0;
        for (int i = 0; i < 144; i++) {
            sum += COMO[i];
        }

        for (int i = 0; i < 144; i++) {
            COMO[i] /= sum;
        }

        histogram = quants.apply(COMO);
    }

    @Override
    public double getDistance(LireFeature vd) { // added by mlux
        // Check if instance of the right class ...
        if (!(vd instanceof COMO))
            throw new UnsupportedOperationException("Wrong descriptor.");

        // casting ...
        tmpFeature = (COMO) vd;

        // check if parameters are fitting ...
        if ((tmpFeature.histogram.length != histogram.length))
            throw new UnsupportedOperationException("Histogram lengths or color spaces do not match");

        // Init Tanimoto coefficient
        Result = 0;
        Temp1 = 0;
        Temp2 = 0;
        TempCount1 = 0;
        TempCount2 = 0;
        TempCount3 = 0;

        for (int i = 0; i < tmpFeature.histogram.length; i++) {
            Temp1 += tmpFeature.histogram[i];
            Temp2 += histogram[i];
        }

        if (Temp1 == 0 && Temp2 == 0) return 0d;
        if (Temp1 == 0 || Temp2 == 0) return 100d;

        for (int i = 0; i < tmpFeature.histogram.length; i++) {
            iTmp1 = tmpFeature.histogram[i] / Temp1;
            iTmp2 = histogram[i] / Temp2;
            TempCount1 += iTmp1 * iTmp2;
            TempCount2 += iTmp2 * iTmp2;
            TempCount3 += iTmp1 * iTmp1;

        }

        Result = (100 - 100 * (TempCount1 / (TempCount2 + TempCount3 - TempCount1)));
        return Result;
    }

    public byte[] getByteHistogram() {
        return histogram;
    }

    /**
     * Provides a much faster way of serialization.
     *
     * @return a byte array that can be read with the corresponding method.
     * @see COMO#setByteArrayRepresentation(byte[])
     */
    @Override
    public byte[] getByteArrayRepresentation() {
        // find out the position of the beginning of the trailing zeros.
        int position = -1;
        for (int i = 0; i < histogram.length; i++) {
            if (position == -1) {
                if (histogram[i] == 0) position = i;
            } else if (position > -1) {
                if (histogram[i] != 0) position = -1;
            }
        }
        if (position < 0) position = histogram.length - 1;
        // find out the actual length. two values in one byte, so we have to round up.
        int length = (position + 1) / 2;
        if ((position + 1) % 2 == 1) length = position / 2 + 1;
        byte[] result = new byte[length];
        for (int i = 0; i < result.length; i++) {
            tmp = ((int) (histogram[(i << 1)])) << 4;
            tmp = (tmp | ((int) (histogram[(i << 1) + 1])));
            result[i] = (byte) (tmp - 128);
        }
        return result;
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see COMO#getByteArrayRepresentation
     */
    @Override
    public void setByteArrayRepresentation(byte[] in) {
        setByteArrayRepresentation(in, 0, in.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        if ((length << 1) < histogram.length)
            Arrays.fill(histogram, length << 1, histogram.length, (byte) 0);
        for (int i = offset; i < offset + length; i++) {
            tmp = in[i] + 128;
            histogram[((i - offset) << 1) + 1] = ((byte) (tmp & 0x000F));
            histogram[(i - offset) << 1] = ((byte) (tmp >> 4));
        }
    }

    @Override
    public double[] getFeatureVector() {
        return SerializationUtils.castToDoubleArray(histogram);
    }

    @Override
    public String getFeatureName() {
        return "COMO";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_COMO;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(histogram.length * 2 + 25);
        for (double aData : histogram) {
            sb.append(aData);
            sb.append(' ');
        }
        return getFeatureName() + "{" + sb.toString().trim() + "}";
    }
}
