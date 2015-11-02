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
 * Updated: 16.01.15 10:26
 */

package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.cedd.*;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.Arrays;

/**
 * The CEDD feature was created, implemented and provided by Savvas A. Chatzichristofis<br/>
 * More information can be found in: Savvas A. Chatzichristofis and Yiannis S. Boutalis,
 * <i>CEDD: Color and Edge Directivity Descriptor. A Compact
 * Descriptor for Image Indexing and Retrieval</i>, A. Gasteratos, M. Vincze, and J.K.
 * Tsotsos (Eds.): ICVS 2008, LNCS 5008, pp. 312-322, 2008.
 *
 * @author: Savvas A. Chatzichristofis, savvash@gmail.com
 */
public class CEDD implements GlobalFeature {
    private double T0;
    private double T1;
    private double T2;
    private double T3;
    private boolean Compact = false;
    //    protected double[] data = new double[144];
    protected byte[] histogram = new byte[144];

    int tmp;
    // for tanimoto:
    private double Result, Temp1, Temp2, TempCount1, TempCount2, TempCount3;
    private CEDD tmpFeature;
    private double iTmp1, iTmp2;



    public CEDD(double Th0, double Th1, double Th2, double Th3, boolean CompactDescriptor) {
        this.T0 = Th0;
        this.T1 = Th1;
        this.T2 = Th2;
        this.T3 = Th3;
        this.Compact = CompactDescriptor;
    }

    public CEDD() {
        this.T0 = 14d;
        this.T1 = 0.68d;
        this.T2 = 0.98d;
        this.T3 = 0.98d;
    }

    // Apply filter
    // signature changed by mlux
    @Override
    public void extract(BufferedImage image) {
        image = ImageUtils.get8BitRGBImage(image);
        Fuzzy10Bin Fuzzy10 = new Fuzzy10Bin(false);
        Fuzzy24Bin Fuzzy24 = new Fuzzy24Bin(false);
        RGB2HSV HSVConverter = new RGB2HSV();
        int[] HSV = new int[3];

        double[] Fuzzy10BinResultTable = new double[10];
        double[] Fuzzy24BinResultTable = new double[24];
        double[] CEDD = new double[144];

        int width = image.getWidth();
        int height = image.getHeight();


        double[][] ImageGrid = new double[width][height];
        double[][] PixelCount = new double[2][2];
        int[][] ImageGridRed = new int[width][height];
        int[][] ImageGridGreen = new int[width][height];
        int[][] ImageGridBlue = new int[width][height];


//please double check from here
        int NumberOfBlocks = -1;

        if (Math.min(width, height) >= 80) NumberOfBlocks = 1600;
        if (Math.min(width, height) < 80 && Math.min(width, height) >= 40) NumberOfBlocks = 400;
        if (Math.min(width, height) < 40) NumberOfBlocks = -1;


        int Step_X = 2;
        int Step_Y = 2;

        if (NumberOfBlocks > 0) {
            Step_X = (int) Math.floor(width / Math.sqrt(NumberOfBlocks));
            Step_Y = (int) Math.floor(height / Math.sqrt(NumberOfBlocks));

            if ((Step_X % 2) != 0) {
                Step_X = Step_X - 1;
            }
            if ((Step_Y % 2) != 0) {
                Step_Y = Step_Y - 1;
            }


        }


// to here


        int[] Edges = new int[6];

        MaskResults MaskValues = new MaskResults();
        Neighborhood PixelsNeighborhood = new Neighborhood();

        for (int i = 0; i < 144; i++) {
            CEDD[i] = 0;
        }
        int pixel, r, g, b;

        // extraction is based on a speedup fix from Michael Riegler & Konstantin Pogorelov
        BufferedImage image_rgb = new BufferedImage(width, height, BufferedImage.TYPE_INT_BGR);
        image_rgb.getGraphics().drawImage(image, 0, 0, null);
        int[] pixels = ((DataBufferInt) image_rgb.getRaster().getDataBuffer()).getData();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixel = pixels[y * width + x];
                b = (pixel >> 16) & 0xFF;
                g = (pixel >> 8) & 0xFF;
                r = (pixel) & 0xFF;
                ImageGridRed[x][y] = r;
                ImageGridGreen[x][y] = g;
                ImageGridBlue[x][y] = b;

                ImageGrid[x][y] = (0.114 * b + 0.587 * g + 0.299 * r);
            }
        }


        int[] CororRed = new int[Step_Y * Step_X];
        int[] CororGreen = new int[Step_Y * Step_X];
        int[] CororBlue = new int[Step_Y * Step_X];

        int[] CororRedTemp = new int[Step_Y * Step_X];
        int[] CororGreenTemp = new int[Step_Y * Step_X];
        int[] CororBlueTemp = new int[Step_Y * Step_X];

        int MeanRed, MeanGreen, MeanBlue;

//plase double check from here

        int TempSum = 0;
        double Max = 0;

        int TemoMAX_X = Step_X * (int) Math.floor(image.getWidth() >> 1);
        int TemoMAX_Y = Step_Y * (int) Math.floor(image.getHeight() >> 1);

        if (NumberOfBlocks > 0) {
            TemoMAX_X = Step_X * (int) Math.sqrt(NumberOfBlocks);
            TemoMAX_Y = Step_Y * (int) Math.sqrt(NumberOfBlocks);
        }


//to here

        for (int y = 0; y < TemoMAX_Y; y += Step_Y) {
            for (int x = 0; x < TemoMAX_X; x += Step_X) {


                MeanRed = 0;
                MeanGreen = 0;
                MeanBlue = 0;
                PixelsNeighborhood.Area1 = 0;
                PixelsNeighborhood.Area2 = 0;
                PixelsNeighborhood.Area3 = 0;
                PixelsNeighborhood.Area4 = 0;
                Edges[0] = -1;
                Edges[1] = -1;
                Edges[2] = -1;
                Edges[3] = -1;
                Edges[4] = -1;
                Edges[5] = -1;

                for (int i = 0; i < 2; i++) {
                    for (int j = 0; j < 2; j++) {
                        PixelCount[i][j] = 0;
                    }
                }

                TempSum = 0;

                for (int i = y; i < y + Step_Y; i++) {
                    for (int j = x; j < x + Step_X; j++) {

                        CororRed[TempSum] = ImageGridRed[j][i];
                        CororGreen[TempSum] = ImageGridGreen[j][i];
                        CororBlue[TempSum] = ImageGridBlue[j][i];

                        CororRedTemp[TempSum] = ImageGridRed[j][i];
                        CororGreenTemp[TempSum] = ImageGridGreen[j][i];
                        CororBlueTemp[TempSum] = ImageGridBlue[j][i];

                        TempSum++;

                        if (j < (x + Step_X / 2) && i < (y + Step_Y / 2)) PixelsNeighborhood.Area1 += (ImageGrid[j][i]);
                        if (j >= (x + Step_X / 2) && i < (y + Step_Y / 2))
                            PixelsNeighborhood.Area2 += (ImageGrid[j][i]);
                        if (j < (x + Step_X / 2) && i >= (y + Step_Y / 2))
                            PixelsNeighborhood.Area3 += (ImageGrid[j][i]);
                        if (j >= (x + Step_X / 2) && i >= (y + Step_Y / 2))
                            PixelsNeighborhood.Area4 += (ImageGrid[j][i]);

                    }
                }

                PixelsNeighborhood.Area1 = (int) (PixelsNeighborhood.Area1 * (4.0 / (Step_X * Step_Y)));

                PixelsNeighborhood.Area2 = (int) (PixelsNeighborhood.Area2 * (4.0 / (Step_X * Step_Y)));

                PixelsNeighborhood.Area3 = (int) (PixelsNeighborhood.Area3 * (4.0 / (Step_X * Step_Y)));

                PixelsNeighborhood.Area4 = (int) (PixelsNeighborhood.Area4 * (4.0 / (Step_X * Step_Y)));


                MaskValues.Mask1 = Math.abs(PixelsNeighborhood.Area1 * 2 + PixelsNeighborhood.Area2 * -2 + PixelsNeighborhood.Area3 * -2 + PixelsNeighborhood.Area4 * 2);
                MaskValues.Mask2 = Math.abs(PixelsNeighborhood.Area1 * 1 + PixelsNeighborhood.Area2 * 1 + PixelsNeighborhood.Area3 * -1 + PixelsNeighborhood.Area4 * -1);
                MaskValues.Mask3 = Math.abs(PixelsNeighborhood.Area1 * 1 + PixelsNeighborhood.Area2 * -1 + PixelsNeighborhood.Area3 * 1 + PixelsNeighborhood.Area4 * -1);
                MaskValues.Mask4 = Math.abs(PixelsNeighborhood.Area1 * Math.sqrt(2) + PixelsNeighborhood.Area2 * 0 + PixelsNeighborhood.Area3 * 0 + PixelsNeighborhood.Area4 * -Math.sqrt(2));
                MaskValues.Mask5 = Math.abs(PixelsNeighborhood.Area1 * 0 + PixelsNeighborhood.Area2 * Math.sqrt(2) + PixelsNeighborhood.Area3 * -Math.sqrt(2) + PixelsNeighborhood.Area4 * 0);

                Max = Math.max(MaskValues.Mask1, Math.max(MaskValues.Mask2, Math.max(MaskValues.Mask3, Math.max(MaskValues.Mask4, MaskValues.Mask5))));


                MaskValues.Mask1 = MaskValues.Mask1 / Max;
                MaskValues.Mask2 = MaskValues.Mask2 / Max;
                MaskValues.Mask3 = MaskValues.Mask3 / Max;
                MaskValues.Mask4 = MaskValues.Mask4 / Max;
                MaskValues.Mask5 = MaskValues.Mask5 / Max;

                int T = -1;

                if (Max < T0) {
                    Edges[0] = 0;
                    T = 0;
                } else {
                    T = -1;

                    if (MaskValues.Mask1 > T1) {
                        T++;
                        Edges[T] = 1;
                    }
                    if (MaskValues.Mask2 > T2) {
                        T++;
                        Edges[T] = 2;
                    }
                    if (MaskValues.Mask3 > T2) {
                        T++;
                        Edges[T] = 3;
                    }
                    if (MaskValues.Mask4 > T3) {
                        T++;
                        Edges[T] = 4;
                    }
                    if (MaskValues.Mask5 > T3) {
                        T++;
                        Edges[T] = 5;
                    }

                }

                for (int i = 0; i < (Step_Y * Step_X); i++) {
                    MeanRed += CororRed[i];
                    MeanGreen += CororGreen[i];
                    MeanBlue += CororBlue[i];
                }

                MeanRed = (int) (MeanRed / (Step_Y * Step_X));
                MeanGreen = (int) (MeanGreen / (Step_Y * Step_X));
                MeanBlue = (int) (MeanBlue / (Step_Y * Step_X));

                HSV = HSVConverter.ApplyFilter(MeanRed, MeanGreen, MeanBlue);

                if (this.Compact == false) {
                    Fuzzy10BinResultTable = Fuzzy10.ApplyFilter(HSV[0], HSV[1], HSV[2], 2);
                    Fuzzy24BinResultTable = Fuzzy24.ApplyFilter(HSV[0], HSV[1], HSV[2], Fuzzy10BinResultTable, 2);

                    for (int i = 0; i <= T; i++) {
                        for (int j = 0; j < 24; j++) {
                            if (Fuzzy24BinResultTable[j] > 0) CEDD[24 * Edges[i] + j] += Fuzzy24BinResultTable[j];
                        }
                    }
                } else {
                    Fuzzy10BinResultTable = Fuzzy10.ApplyFilter(HSV[0], HSV[1], HSV[2], 2);
                    for (int i = 0; i <= T; i++) {
                        for (int j = 0; j < 10; j++) {
                            if (Fuzzy10BinResultTable[j] > 0) CEDD[10 * Edges[i] + j] += Fuzzy10BinResultTable[j];
                        }
                    }
                }
            }
        }

        double Sum = 0;
        for (int i = 0; i < 144; i++) {
            Sum += CEDD[i];
        }

        for (int i = 0; i < 144; i++) {
            CEDD[i] = CEDD[i] / Sum;
        }

        double qCEDD[];


        if (Compact == false) {
            qCEDD = new double[144];
            CEDDQuant quants = new CEDDQuant();
            qCEDD = quants.Apply(CEDD);
        } else {
            qCEDD = new double[60];
            CompactCEDDQuant quants = new CompactCEDDQuant();
            qCEDD = quants.Apply(CEDD);
        }

//        for (int i = 0; i < qCEDD.length; i++)
//            System.out.println(qCEDD[i]);

//        data = qCEDD;  // changed by mlux
        for (int i = 0; i < qCEDD.length; i++) {
            histogram[i] = (byte) qCEDD[i];
        }
    }

    @Override
    public double getDistance(LireFeature vd) { // added by mlux     //TODO: Tanimoto in MetricUtils?
        // Check if instance of the right class ...
        if (!(vd instanceof CEDD))
            throw new UnsupportedOperationException("Wrong descriptor.");

        // casting ...
        tmpFeature = (CEDD) vd;

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

    @SuppressWarnings("unused")
    private double scalarMult(double[] a, double[] b) {
        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            sum += a[i] * b[i];
        }
        return sum;
    }

    public byte[] getByteHistogram() {
        return histogram;
    }

//    public String getStringRepresentation() { // added by mlux
//        StringBuilder sb = new StringBuilder(histogram.length * 2 + 25);
//        sb.append("cedd");
//        sb.append(' ');
//        sb.append(histogram.length);
//        sb.append(' ');
//        for (byte aData : histogram) {
//            sb.append((int) aData);
//            sb.append(' ');
//        }
//        return sb.toString().trim();
//    }
//
//    public void setStringRepresentation(String s) { // added by mlux
//        StringTokenizer st = new StringTokenizer(s);
//        if (!st.nextToken().equals("cedd"))
//            throw new UnsupportedOperationException("This is not a CEDD descriptor.");
//        for (int i = 0; i < histogram.length; i++) {
//            if (!st.hasMoreTokens())
//                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
//            histogram[i] = (byte) Integer.parseInt(st.nextToken());
//        }
//
//    }

    /**
     * Provides a much faster way of serialization.
     *
     * @return a byte array that can be read with the corresponding method.
     * @see CEDD#setByteArrayRepresentation(byte[])
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
        if (position < 0) position = 143;
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
     * @see CEDD#getByteArrayRepresentation
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
        return "CEDD";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_CEDD;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(histogram.length * 2 + 25);
        for (byte aData : histogram) {
            sb.append((int) aData);
            sb.append(' ');
        }
        return "CEDD{" + sb.toString().trim() + "}";
    }
}
