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

import net.semanticmetadata.lire.imageanalysis.fcth.*;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.util.StringTokenizer;

/**
 * The FCTH feature was created, implemented and provided by Savvas A. Chatzichristofis<br/>
 * More information can be found in: Savvas A. Chatzichristofis and Yiannis S. Boutalis,
 * <i>FCTH: Fuzzy Color and Texture Histogram - A Low Level Feature for Accurate Image
 * Retrieval</i>, in Proceedings of the Ninth International Workshop on Image Analysis for
 * Multimedia Interactive Services, IEEE, Klagenfurt, May, 2008.
 *
 * @author: Savvas A. Chatzichristofis, savvash@gmail.com
 */

public class FCTH implements LireFeature {
    protected double[] histogram = null;

    public boolean Compact = false;

    // Constructor
    public FCTH() {


    }

    // Apply filter
    public double[] Apply(BufferedImage image) {
        Fuzzy10Bin Fuzzy10 = new Fuzzy10Bin(false);
        Fuzzy24Bin Fuzzy24 = new Fuzzy24Bin(false);
        FuzzyFCTHpart FuccyFCTH = new FuzzyFCTHpart();


        double[] Fuzzy10BinResultTable = new double[10];
        double[] Fuzzy24BinResultTable = new double[24];
        double[] FuzzyHistogram192 = new double[192];


        int Method = 2;
        int width = image.getWidth();
        int height = image.getHeight();


        for (int R = 0; R < 192; R++) {
            FuzzyHistogram192[R] = 0;

        }


        RGB2HSV HSVConverter = new RGB2HSV();
        int[] HSV = new int[3];

        WaveletMatrixPlus Matrix = new WaveletMatrixPlus();


        double[][] ImageGrid = new double[width][height];
        int[][] ImageGridRed = new int[width][height];
        int[][] ImageGridGreen = new int[width][height];
        int[][] ImageGridBlue = new int[width][height];

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int pixel = image.getRGB(x, y);
                ImageGridRed[x][y] = (pixel >> 16) & 0xff;
                ImageGridGreen[x][y] = (pixel >> 8) & 0xff;
                ImageGridBlue[x][y] = (pixel) & 0xff;

                int mean = (int) (0.114 * ImageGridBlue[x][y] + 0.587 * ImageGridGreen[x][y] + 0.299 * ImageGridRed[x][y]);
                ImageGrid[x][y] = mean;
            }
        }


        int NumberOfBlocks = 1600;
        int Step_X = (int) Math.floor(width / Math.sqrt(NumberOfBlocks));
        int Step_Y = (int) Math.floor(height / Math.sqrt(NumberOfBlocks));

        if ((Step_X % 2) != 0) {
            Step_X = Step_X - 1;
        }
        if ((Step_Y % 2) != 0) {
            Step_Y = Step_Y - 1;
        }


        if (Step_Y < 4) Step_Y = 4;
        if (Step_X < 4) Step_X = 4;
        ///
        // Filter

        for (int y = 0; y < height - Step_Y; y += Step_Y) {
            for (int x = 0; x < width - Step_X; x += Step_X) {
                //int[][] BinaryBlock = new int[4][4];
                double[][] Block = new double[4][4];
                int[][] BlockR = new int[4][4];
                int[][] BlockG = new int[4][4];
                int[][] BlockB = new int[4][4];
                int[][] BlockCount = new int[4][4];

                int[] CororRed = new int[Step_Y * Step_X];
                int[] CororGreen = new int[Step_Y * Step_X];
                int[] CororBlue = new int[Step_Y * Step_X];

                int[] CororRedTemp = new int[Step_Y * Step_X];
                int[] CororGreenTemp = new int[Step_Y * Step_X];
                int[] CororBlueTemp = new int[Step_Y * Step_X];

                int MeanRed = 0;
                int MeanGreen = 0;
                int MeanBlue = 0;
                int CurrentPixelX = 0;
                int CurrentPixelY = 0;
                for (int i = 0; i < 4; i++) {

                    for (int j = 0; j < 4; j++) {
                        Block[i][j] = 0;
                        BlockCount[i][j] = 0;
                    }
                }
                //#endregion

                int TempSum = 0;
                for (int i = 0; i < Step_X; i++) {
                    for (int j = 0; j < Step_Y; j++) {
                        CurrentPixelX = 0;
                        CurrentPixelY = 0;

                        if (i >= (Step_X / 4)) CurrentPixelX = 1;
                        if (i >= (Step_X / 2)) CurrentPixelX = 2;
                        if (i >= (3 * Step_X / 4)) CurrentPixelX = 3;

                        if (j >= (Step_Y / 4)) CurrentPixelY = 1;
                        if (j >= (Step_Y / 2)) CurrentPixelY = 2;
                        if (j >= (3 * Step_Y / 4)) CurrentPixelY = 3;

                        Block[CurrentPixelX][CurrentPixelY] += ImageGrid[x + i][y + j];
                        BlockCount[CurrentPixelX][CurrentPixelY]++;

                        BlockR[CurrentPixelX][CurrentPixelY] = ImageGridRed[x + i][y + j];
                        BlockG[CurrentPixelX][CurrentPixelY] = ImageGridGreen[x + i][y + j];
                        BlockB[CurrentPixelX][CurrentPixelY] = ImageGridBlue[x + i][y + j];

                        CororRed[TempSum] = BlockR[CurrentPixelX][CurrentPixelY];
                        CororGreen[TempSum] = BlockG[CurrentPixelX][CurrentPixelY];
                        CororBlue[TempSum] = BlockB[CurrentPixelX][CurrentPixelY];

                        CororRedTemp[TempSum] = BlockR[CurrentPixelX][CurrentPixelY];
                        CororGreenTemp[TempSum] = BlockG[CurrentPixelX][CurrentPixelY];
                        CororBlueTemp[TempSum] = BlockB[CurrentPixelX][CurrentPixelY];


                        TempSum++;
                    }
                }


                for (int i = 0; i < 4; i++) {
                    for (int j = 0; j < 4; j++) {
                        Block[i][j] = Block[i][j] / BlockCount[i][j];
                    }
                }

                Matrix = singlePassThreshold(Block, 1);


                for (int i = 0; i < (Step_Y * Step_X); i++) {
                    MeanRed += CororRed[i];
                    MeanGreen += CororGreen[i];
                    MeanBlue += CororBlue[i];
                }

                MeanRed = (int) (MeanRed / (Step_Y * Step_X));
                MeanGreen = (int) (MeanGreen / (Step_Y * Step_X));
                MeanBlue = (int) (MeanBlue / (Step_Y * Step_X));


                HSV = HSVConverter.ApplyFilter(MeanRed, MeanGreen, MeanBlue);

                if (Compact == false) {
                    Fuzzy10BinResultTable = Fuzzy10.ApplyFilter(HSV[0], HSV[1], HSV[2], Method);
                    Fuzzy24BinResultTable = Fuzzy24.ApplyFilter(HSV[0], HSV[1], HSV[2], Fuzzy10BinResultTable, Method);
                    FuzzyHistogram192 = FuccyFCTH.ApplyFilter(Matrix.F3, Matrix.F2, Matrix.F1, Fuzzy24BinResultTable, Method, 24);

                } else {
                    Fuzzy10BinResultTable = Fuzzy10.ApplyFilter(HSV[0], HSV[1], HSV[2], Method);
                    FuzzyHistogram192 = FuccyFCTH.ApplyFilter(Matrix.F3, Matrix.F2, Matrix.F1, Fuzzy10BinResultTable, Method, 10);

                }

            }


        }

        // end of the filter
        double TotalSum = 0;

        for (int i = 0; i < 192; i++) {


            TotalSum += FuzzyHistogram192[i];


        }

        for (int i = 0; i < 192; i++) {


            FuzzyHistogram192[i] = FuzzyHistogram192[i] / TotalSum;


        }

        FCTHQuant Quant = new FCTHQuant();
        FuzzyHistogram192 = Quant.Apply(FuzzyHistogram192);

        return FuzzyHistogram192;


    }

    private WaveletMatrixPlus singlePassThreshold(double[][] inputMatrix, int level) {


        WaveletMatrixPlus TempMatrix = new WaveletMatrixPlus();
        level = (int) Math.pow(2.0, level - 1);

//GETLENGTH*************
        double[][] resultMatrix = new double[inputMatrix.length][inputMatrix[0].length];

        int xOffset = inputMatrix.length / 2 / level;

        int yOffset = inputMatrix[0].length / 2 / level;

        int currentPixel = 0;

        //double size = inputMatrix.length * inputMatrix[0].length;

        double multiplier = 0;


        for (int y = 0; y < inputMatrix[0].length; y++) {

            for (int x = 0; x < inputMatrix.length; x++) {

                if ((y < inputMatrix[0].length / 2 / level) && (x < inputMatrix.length / 2 / level)) {

                    currentPixel++;

                    resultMatrix[x][y] = (inputMatrix[2 * x][2 * y] + inputMatrix[2 * x + 1][2 * y] + inputMatrix[2 * x][2 * y + 1] + inputMatrix[2 * x + 1][2 * y + 1]) / 4;

                    double vertDiff = (-inputMatrix[2 * x][2 * y] - inputMatrix[2 * x + 1][2 * y] + inputMatrix[2 * x][2 * y + 1] + inputMatrix[2 * x + 1][2 * y + 1]);

                    double horzDiff = (inputMatrix[2 * x][2 * y] - inputMatrix[2 * x + 1][2 * y] + inputMatrix[2 * x][2 * y + 1] - inputMatrix[2 * x + 1][2 * y + 1]);

                    double diagDiff = (-inputMatrix[2 * x][2 * y] + inputMatrix[2 * x + 1][2 * y] + inputMatrix[2 * x][2 * y + 1] - inputMatrix[2 * x + 1][2 * y + 1]);


                    resultMatrix[x + xOffset][y] = (int) (byte) (multiplier + Math.abs(vertDiff));

                    resultMatrix[x][y + yOffset] = (int) (byte) (multiplier + Math.abs(horzDiff));

                    resultMatrix[x + xOffset][y + yOffset] = (int) (byte) (multiplier + Math.abs(diagDiff));


                } else {

                    if ((x >= inputMatrix.length / level) || (y >= inputMatrix[0].length / level))

                    {
                        resultMatrix[x][y] = inputMatrix[x][y];
                    }

                }

            }

        }

        double Temp1 = 0;
        double Temp2 = 0;
        double Temp3 = 0;

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                Temp1 += 0.25 * Math.pow(resultMatrix[2 + i][j], 2);
                Temp2 += 0.25 * Math.pow(resultMatrix[i][2 + j], 2);
                Temp3 += 0.25 * Math.pow(resultMatrix[2 + i][2 + j], 2);


            }

        }

        //double[] MatrixResults = new double[4];


        TempMatrix.F1 = Math.sqrt(Temp1);
        TempMatrix.F2 = Math.sqrt(Temp2);
        TempMatrix.F3 = Math.sqrt(Temp3);

        TempMatrix.Entropy = 0;

        return TempMatrix;

    }

    public void extract(BufferedImage bimg) {
        histogram = Apply(bimg);
    }

    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(histogram);
    }

    public void setByteArrayRepresentation(byte[] in) {
        histogram = SerializationUtils.toDoubleArray(in);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        histogram = SerializationUtils.toDoubleArray(in, offset, length);
    }

    public double[] getDoubleHistogram() {
        return histogram;
    }

    public float getDistance(LireFeature vd) { // added by mlux
        // Check if instance of the right class ...
        if (!(vd instanceof FCTH))
            throw new UnsupportedOperationException("Wrong descriptor.");

        // casting ...
        FCTH ch = (FCTH) vd;

        // check if parameters are fitting ...
        if ((ch.histogram.length != histogram.length))
            throw new UnsupportedOperationException("Histogram lengths or color spaces do not match");

        // Tanimoto coefficient
        double Result = 0;
        double Temp1 = 0;
        double Temp2 = 0;

        double TempCount1 = 0, TempCount2 = 0, TempCount3 = 0;

        for (int i = 0; i < ch.histogram.length; i++) {
            Temp1 += ch.histogram[i];
            Temp2 += histogram[i];
        }

        if (Temp1 == 0 || Temp2 == 0) Result = 100;
        if (Temp1 == 0 && Temp2 == 0) Result = 0;

        if (Temp1 > 0 && Temp2 > 0) {
            for (int i = 0; i < ch.histogram.length; i++) {
                TempCount1 += (ch.histogram[i] / Temp1) * (histogram[i] / Temp2);
                TempCount2 += (histogram[i] / Temp2) * (histogram[i] / Temp2);
                TempCount3 += (ch.histogram[i] / Temp1) * (ch.histogram[i] / Temp1);

            }

            Result = (100 - 100 * (TempCount1 / (TempCount2 + TempCount3
                    - TempCount1))); //Tanimoto
        }
        return (float) Result;

    }

    public String getStringRepresentation() {
        // FCTH is quantized to 3bits / bin ... therefore ints are enough.
        StringBuilder sb = new StringBuilder(histogram.length * 2 + 25);
        sb.append("fcth");
        sb.append(' ');
        sb.append(histogram.length);
        sb.append(' ');
        for (double aData : histogram) {
            sb.append((int) aData);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String s) {
        StringTokenizer st = new StringTokenizer(s);
        if (!st.nextToken().equals("fcth"))
            throw new UnsupportedOperationException("This is not a FCTH descriptor.");
        histogram = new double[Integer.parseInt(st.nextToken())];
        for (int i = 0; i < histogram.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
            histogram[i] = Integer.parseInt(st.nextToken());
        }
    }
}

