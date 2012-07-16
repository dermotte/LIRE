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
package net.semanticmetadata.lire.imageanalysis.mpeg7;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;


/**
 * Class for extrcating & comparing MPEG-7 based CBIR descriptor ColorLayout
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ColorLayoutImpl {
    // static final boolean debug = true;
    protected int[][] shape;
    protected int imgYSize, imgXSize;
    protected BufferedImage img;

    protected static int[] availableCoeffNumbers = {1, 3, 6, 10, 15, 21, 28, 64};

    public int[] YCoeff;
    public int[] CbCoeff;
    public int[] CrCoeff;

    protected int numCCoeff = 28, numYCoeff = 64;

    protected static int[] arrayZigZag = {
            0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5,
            12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7, 14, 21, 28,
            35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51,
            58, 59, 52, 45, 38, 31, 39, 46, 53, 60, 61, 54, 47, 55, 62, 63
    };

    protected static double[][] arrayCosin = {
            {
                    3.535534e-01, 3.535534e-01, 3.535534e-01, 3.535534e-01,
                    3.535534e-01, 3.535534e-01, 3.535534e-01, 3.535534e-01
            },
            {
                    4.903926e-01, 4.157348e-01, 2.777851e-01, 9.754516e-02,
                    -9.754516e-02, -2.777851e-01, -4.157348e-01, -4.903926e-01
            },
            {
                    4.619398e-01, 1.913417e-01, -1.913417e-01, -4.619398e-01,
                    -4.619398e-01, -1.913417e-01, 1.913417e-01, 4.619398e-01
            },
            {
                    4.157348e-01, -9.754516e-02, -4.903926e-01, -2.777851e-01,
                    2.777851e-01, 4.903926e-01, 9.754516e-02, -4.157348e-01
            },
            {
                    3.535534e-01, -3.535534e-01, -3.535534e-01, 3.535534e-01,
                    3.535534e-01, -3.535534e-01, -3.535534e-01, 3.535534e-01
            },
            {
                    2.777851e-01, -4.903926e-01, 9.754516e-02, 4.157348e-01,
                    -4.157348e-01, -9.754516e-02, 4.903926e-01, -2.777851e-01
            },
            {
                    1.913417e-01, -4.619398e-01, 4.619398e-01, -1.913417e-01,
                    -1.913417e-01, 4.619398e-01, -4.619398e-01, 1.913417e-01
            },
            {
                    9.754516e-02, -2.777851e-01, 4.157348e-01, -4.903926e-01,
                    4.903926e-01, -4.157348e-01, 2.777851e-01, -9.754516e-02
            }
    };
    protected static int[][] weightMatrix = new int[3][64];
    protected BufferedImage colorLayoutImage;

    public ColorLayoutImpl() {
        // empty constructor, used for new instance if setStringRepresentation is called later.
    }

    /**
     * Create a ColorLayout Object from the given BufferedImage. 6 Y and 3 C Coefficients are used,
     * if you want to use another number you have to set it with the Setters.
     *
     * @param image the input image
     */
    public ColorLayoutImpl(BufferedImage image) {
        this.img = image;
        imgYSize = image.getHeight();
        imgXSize = image.getWidth();
        init();
    }

    /**
     * Uses the method {@link at.lux.imageanalysis.ColorLayoutImpl#setStringRepresentation(String)}
     *
     * @param descriptorValues
     * @see at.lux.imageanalysis.ColorLayoutImpl#setStringRepresentation(String)
     */
    public ColorLayoutImpl(String descriptorValues) {
        setStringRepresentation(descriptorValues);
    }

    /**
     * Create a ColorLayout Object from the given BufferedImage with the desired number of Coefficients
     *
     * @param image          the input image
     * @param numberOfYCoeff desired number of Y Coefficients
     * @param numberOfCCoeff desired number of Cr and Cb Coefficients
     */
    public ColorLayoutImpl(int numberOfYCoeff, int numberOfCCoeff, BufferedImage image) {
        this.numCCoeff = getRightCoeffNumber(numberOfCCoeff);
        this.numYCoeff = getRightCoeffNumber(numberOfYCoeff);
        this.img = image;
        imgYSize = image.getHeight();
        imgXSize = image.getWidth();
        init();
    }

    /**
     * init used by all constructors
     */
    private void init() {
        shape = new int[3][64];
        YCoeff = new int[64];
        CbCoeff = new int[64];
        CrCoeff = new int[64];
        colorLayoutImage = null;
        extract();
    }

    public void extract(BufferedImage bimg) {
        this.img = bimg;
        imgYSize = img.getHeight();
        imgXSize = img.getWidth();
        init();
    }

    private void createShape() {
        int y_axis, x_axis;
        int i, k, x, y, j;
        long[][] sum = new long[3][64];
        int[] cnt = new int[64];
        double yy = 0.0;
        int R, G, B;

        //init of the blocks
        for (i = 0; i < 64; i++) {
            cnt[i] = 0;
            sum[0][i] = 0;
            sum[1][i] = 0;
            sum[2][i] = 0;
            shape[0][i] = 0;
            shape[1][i] = 0;
            shape[2][i] = 0;
        }

        WritableRaster raster = img.getRaster();
        int[] pixel = {0, 0, 0};
        for (y = 0; y < imgYSize; y++) {
            for (x = 0; x < imgXSize; x++) {
                raster.getPixel(x, y, pixel);
                R = pixel[0];
                G = pixel[1];
                B = pixel[2];

                y_axis = (int) (y / (imgYSize / 8.0));
                x_axis = (int) (x / (imgXSize / 8.0));

                k = (y_axis << 3) + x_axis;

                //RGB to YCbCr, partition and average-calculation
                yy = (0.299 * R + 0.587 * G + 0.114 * B) / 256.0;
                sum[0][k] += (int) (219.0 * yy + 16.5); // Y
                sum[1][k] += (int) (224.0 * 0.564 * (B / 256.0 * 1.0 - yy) + 128.5); // Cb
                sum[2][k] += (int) (224.0 * 0.713 * (R / 256.0 * 1.0 - yy) + 128.5); // Cr
                cnt[k]++;
            }
        }

        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                for (k = 0; k < 3; k++) {
                    if (cnt[(i << 3) + j] != 0)
                        shape[k][(i << 3) + j] = (int) (sum[k][(i << 3) + j] / cnt[(i << 3) + j]);
                    else
                        shape[k][(i << 3) + j] = 0;
                }
            }
        }
    }

    private static void Fdct(int[] shapes) {
        int i, j, k;
        double s;
        double[] dct = new double[64];

        //calculation of the cos-values of the second sum
        for (i = 0; i < 8; i++) {
            for (j = 0; j < 8; j++) {
                s = 0.0;
                for (k = 0; k < 8; k++)
                    s += arrayCosin[j][k] * shapes[8 * i + k];
                dct[8 * i + j] = s;
            }
        }

        for (j = 0; j < 8; j++) {
            for (i = 0; i < 8; i++) {
                s = 0.0;
                for (k = 0; k < 8; k++)
                    s += arrayCosin[i][k] * dct[8 * k + j];
                shapes[8 * i + j] = (int) Math.floor(s + 0.499999);
            }
        }
    }

    private static int quant_ydc(int i) {
        int j;
        if (i > 192)
            j = 112 + ((i - 192) >> 2);
        else if (i > 160)
            j = 96 + ((i - 160) >> 1);
        else if (i > 96)
            j = 32 + (i - 96);
        else if (i > 64)
            j = 16 + ((i - 64) >> 1);
        else
            j = i >> 2;

        return j;
    }

    private static int quant_cdc(int i) {
        int j;
        if (i > 191)
            j = 63;
        else if (i > 160)
            j = 56 + ((i - 160) >> 2);
        else if (i > 144)
            j = 48 + ((i - 144) >> 1);
        else if (i > 112)
            j = 16 + (i - 112);
        else if (i > 96)
            j = 8 + ((i - 96) >> 1);
        else if (i > 64)
            j = (i - 64) >> 2;
        else
            j = 0;

        return j;
    }


    private static int quant_ac(int i) {
        int j;

        if (i > 255)
            i = 255;
        //if(i > 239)
        //i = 239;
        if (i < -256)
            i = -256;
        if ((Math.abs(i)) > 127)
            j = 64 + ((Math.abs(i)) >> 2);
        else if ((Math.abs(i)) > 63)
            j = 32 + ((Math.abs(i)) >> 1);
        else
            j = Math.abs(i);
        j = (i < 0) ? -j : j;

        j += 128;
        //j+=132;

        return j;
    }

    private int extract() {

        createShape();

        Fdct(shape[0]);
        Fdct(shape[1]);
        Fdct(shape[2]);

        YCoeff[0] = quant_ydc(shape[0][0] >> 3) >> 1;
        CbCoeff[0] = quant_cdc(shape[1][0] >> 3);
        CrCoeff[0] = quant_cdc(shape[2][0] >> 3);

        //quantization and zig-zagging
        for (int i = 1; i < 64; i++) {
            YCoeff[i] = quant_ac((shape[0][(arrayZigZag[i])]) >> 1) >> 3;
            CbCoeff[i] = quant_ac(shape[1][(arrayZigZag[i])]) >> 3;
            CrCoeff[i] = quant_ac(shape[2][(arrayZigZag[i])]) >> 3;
        }

        setYCoeff(YCoeff);
        setCbCoeff(CbCoeff);
        setCrCoeff(CrCoeff);
        return 0;
    }

    private void setYCoeff(int[] YCoeff) {
        StringBuilder b = new StringBuilder(256);
        for (int i = 0; i < numYCoeff; i++) {
            b.append(YCoeff[i]).append(' ');
        }
//        System.out.println("y:  " + b.toString());
    }


    private void setCbCoeff(int[] CbCoeff) {
        StringBuilder b = new StringBuilder(256);
        for (int i = 0; i < numCCoeff; i++) {
            b.append(CbCoeff[i]).append(' ');
        }
//        System.out.println("cb: " + b.toString());
    }

    private void setCrCoeff(int[] CrCoeff) {
        StringBuilder b = new StringBuilder(256);
        for (int i = 0; i < numCCoeff; i++) {
            b.append(CrCoeff[i]).append(' ');
        }
//        System.out.println("cr: " + b.toString());
    }

    /**
     * Nicht alle Werte sind laut MPEG-7 erlaubt ....
     */
    private static int getRightCoeffNumber(int num) {
        int val = 0;
        if (num <= 1)
            val = 1;
        else if (num <= 3)
            val = 3;
        else if (num <= 6)
            val = 6;
        else if (num <= 10)
            val = 10;
        else if (num <= 15)
            val = 15;
        else if (num <= 21)
            val = 21;
        else if (num <= 28)
            val = 28;
        else if (num > 28) val = 64;
        return val;
    }

    /**
     * Takes two ColorLayout Coeff sets and calculates similarity.
     *
     * @return -1.0 if data is not valid.
     */
    public static double getSimilarity(int[] YCoeff1, int[] CbCoeff1, int[] CrCoeff1, int[] YCoeff2, int[] CbCoeff2, int[] CrCoeff2) {
        int numYCoeff1, numYCoeff2, CCoeff1, CCoeff2, YCoeff, CCoeff;

        //Numbers of the Coefficients of two descriptor values.
        numYCoeff1 = YCoeff1.length;
        numYCoeff2 = YCoeff2.length;
        CCoeff1 = CbCoeff1.length;
        CCoeff2 = CbCoeff2.length;

        //take the minimal Coeff-number
        YCoeff = Math.min(numYCoeff1, numYCoeff2);
        CCoeff = Math.min(CCoeff1, CCoeff2);

        setWeightingValues();

        int j;
        int[] sum = new int[3];
        int diff;
        sum[0] = 0;

        for (j = 0; j < YCoeff; j++) {
            diff = (YCoeff1[j] - YCoeff2[j]);
            sum[0] += (weightMatrix[0][j] * diff * diff);
        }

        sum[1] = 0;
        for (j = 0; j < CCoeff; j++) {
            diff = (CbCoeff1[j] - CbCoeff2[j]);
            sum[1] += (weightMatrix[1][j] * diff * diff);
        }

        sum[2] = 0;
        for (j = 0; j < CCoeff; j++) {
            diff = (CrCoeff1[j] - CrCoeff2[j]);
            sum[2] += (weightMatrix[2][j] * diff * diff);
        }

        //returns the distance between the two desciptor values

        return Math.sqrt(sum[0] * 1.0) + Math.sqrt(sum[1] * 1.0) + Math.sqrt(sum[2] * 1.0);
    }

    private static void setWeightingValues() {
        weightMatrix[0][0] = 2;
        weightMatrix[0][1] = weightMatrix[0][2] = 2;
        weightMatrix[1][0] = 2;
        weightMatrix[1][1] = weightMatrix[1][2] = 1;
        weightMatrix[2][0] = 4;
        weightMatrix[2][1] = weightMatrix[2][2] = 2;

        for (int i = 0; i < 3; i++) {
            for (int j = 3; j < 64; j++)
                weightMatrix[i][j] = 1;
        }
    }

    private static BufferedImage YCrCb2RGB(int[][] rgbSmallImage) {
        BufferedImage br = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        WritableRaster r = br.getRaster();
        double rImage, gImage, bImage;
        int pixel[] = new int[3];

        for (int i = 0; i < 64; i++) {
            rImage = ((rgbSmallImage[0][i] - 16.0) * 256.0) / 219.0;
            gImage = ((rgbSmallImage[1][i] - 128.0) * 256.0) / 224.0;
            bImage = ((rgbSmallImage[2][i] - 128.0) * 256.0) / 224.0;

            pixel[0] = Math.max(0, (int) ((rImage) + (1.402 * bImage) + 0.5)); //R
            pixel[1] = Math.max(0, (int) ((rImage) + (-0.34413 * gImage) + (-0.71414 * bImage) + 0.5));  //G
            pixel[2] = Math.max(0, (int) ((rImage) + (1.772 * gImage) + 0.5)); //B

            r.setPixel(i % 8, i >> 3, pixel);
        }

        return br;
    }

    public BufferedImage getColorLayoutImage() {
        if (colorLayoutImage != null)
            return colorLayoutImage;
        else {
            int[][] smallReImage = new int[3][64];

            // inverse quantization and zig-zagging
            smallReImage[0][0] = IquantYdc((YCoeff[0]));
            smallReImage[1][0] = IquantCdc((CbCoeff[0]));
            smallReImage[2][0] = IquantCdc((CrCoeff[0]));

            for (int i = 1; i < 64; i++) {
                smallReImage[0][(arrayZigZag[i])] = IquantYac((YCoeff[i]));
                smallReImage[1][(arrayZigZag[i])] = IquantCac((CbCoeff[i]));
                smallReImage[2][(arrayZigZag[i])] = IquantCac((CrCoeff[i]));
            }

            // inverse Discrete Cosine Transform
            Idct(smallReImage[0]);
            Idct(smallReImage[1]);
            Idct(smallReImage[2]);

            // YCrCb to RGB
            colorLayoutImage = YCrCb2RGB(smallReImage);
            return colorLayoutImage;
        }
    }

    private static void Idct(int[] iShapes) {
        int u, v, k;
        double s;
        double[] dct = new double[64];

        //calculation of the cos-values of the second sum
        for (u = 0; u < 8; u++) {
            for (v = 0; v < 8; v++) {
                s = 0.0;
                for (k = 0; k < 8; k++)
                    s += arrayCosin[k][v] * iShapes[8 * u + k];
                dct[8 * u + v] = s;
            }
        }

        for (v = 0; v < 8; v++) {
            for (u = 0; u < 8; u++) {
                s = 0.0;
                for (k = 0; k < 8; k++)
                    s += arrayCosin[k][u] * dct[8 * k + v];
                iShapes[8 * u + v] = (int) Math.floor(s + 0.499999);
            }
        }
    }


    private static int IquantYdc(int i) {
        int j;
        i = i << 1;
        if (i > 112)
            j = 194 + ((i - 112) << 2);
        else if (i > 96)
            j = 162 + ((i - 96) << 1);
        else if (i > 32)
            j = 96 + (i - 32);
        else if (i > 16)
            j = 66 + ((i - 16) << 1);

        else
            j = i << 2;

        return j << 3;
    }

    private static int IquantCdc(int i) {
        int j;
        if (i > 63)
            j = 192;
        else if (i > 56)
            j = 162 + ((i - 56) << 2);
        else if (i > 48)
            j = 145 + ((i - 48) << 1);
        else if (i > 16)
            j = 112 + (i - 16);
        else if (i > 8)
            j = 97 + ((i - 8) << 1);
        else if (i > 0)
            j = 66 + (i << 2);
        else
            j = 64;
        return j << 3;
    }

    private static int IquantYac(int i) {
        int j;
        i = i << 3;
        i -= 128;
        if (i > 128)
            i = 128;
        if (i < -128)
            i = -128;
        if ((Math.abs(i)) > 96)
            j = ((Math.abs(i)) << 2) - 256;
        else if ((Math.abs(i)) > 64)
            j = ((Math.abs(i)) << 1) - 64;
        else
            j = Math.abs(i);
        j = (i < 0) ? -j : j;

        return j << 1;
    }

    private static int IquantCac(int i) {
        int j;
        i = i << 3;
        i -= 128;
        if (i > 128)
            i = 128;
        if (i < -128)
            i = -128;
        if ((Math.abs(i)) > 96)
            j = ((Math.abs(i) << 2) - 256);
        else if ((Math.abs(i)) > 64)
            j = ((Math.abs(i) << 1) - 64);
        else
            j = Math.abs(i);
        j = (i < 0) ? -j : j;

        return j;
    }

    public int getNumberOfCCoeff() {
        return numCCoeff;
    }

    public void setNumberOfCCoeff(int numberOfCCoeff) {
        this.numCCoeff = numberOfCCoeff;
    }

    public int getNumberOfYCoeff() {
        return numYCoeff;
    }

    public void setNumberOfYCoeff(int numberOfYCoeff) {
        this.numYCoeff = numberOfYCoeff;
    }


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

    public int[] getYCoeff() {
        return YCoeff;
    }

    public int[] getCbCoeff() {
        return CbCoeff;
    }

    public int[] getCrCoeff() {
        return CrCoeff;
    }
}
