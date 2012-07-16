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

/*
 * JpegCoefficientHistogram.java
 *
 * Ported from C#, performance is probably poor
 *
 * JpegCoefficientHistogram.cs
 * Part of the Callisto framework
 * (c) 2009 Arthur Pitman. All rights reserved.
 *
 */


package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Comparator;
import java.util.StringTokenizer;

public class JpegCoefficientHistogram implements LireFeature {

    protected int[] descriptorValues;
    protected final int BLOCK_SIZE = 8;

    protected double[][][][] transform;

    double[][][] dctScaler2 = new double[][][]
            {
                    {{0, 0, 0}, {1, 1, 1}, {1.9734043721689, 1.94899230863397, 1.94946285758305}, {2.94332698437659, 2.98796119092502, 2.96024565199338}, {3.80255904577673, 4.37008158805412, 4.35141415401432}, {4.68161529944488, 5.72204262950584, 5.66166566827365}, {5.87064634019722, 7.54306903541829, 7.3857474119367}, {7.78994087483808, 10.2359236535064, 9.95778205927185}},
                    {{1.19274531042455, 1.2152969832815, 1.20171324933339}, {2.2943803290662, 2.14415384022472, 2.15039994134979}, {3.25623650819256, 3.01385037727374, 3.03249686547772}, {4.27045252457434, 4.21446933397605, 4.23815367511332}, {5.17998028000972, 5.78600261610464, 5.81037357341976}, {6.19785417964287, 7.2822799946799, 7.26373154837891}, {7.69684274092057, 9.28633217515052, 9.21658162859184}, {10.0353932177135, 12.1802615431261, 11.9831585607954}},
                    {{2.4646732594343, 2.46387210793402, 2.43507278648256}, {3.49836053443852, 3.27953398208675, 3.26284773577962}, {4.28065963174638, 4.13119763592951, 4.1308271078573}, {5.14409465284924, 5.70765094689331, 5.74219324470954}, {5.98518749895616, 6.93385341523623, 6.96708272307546}, {7.0299847606958, 8.42771919562544, 8.40220952430735}, {8.57907078388286, 10.4160723222439, 10.3679017528836}, {11.0685666474499, 13.4531303115576, 13.2927862368604}},
                    {{3.70846881386602, 3.86908673744653, 3.83210828276994}, {4.80559125796947, 4.88056952563021, 4.82595757558342}, {5.42778400768362, 6.14578620000214, 6.17040735624603}, {6.08929033878163, 7.16832687031443, 7.20751420464431}, {6.86850046640731, 8.26420707459324, 8.29853895971336}, {7.84984618688477, 9.71743532538622, 9.7147262415522}, {9.51903135622904, 11.8234824848058, 11.7845246577222}, {12.0049646114253, 14.8400917638334, 14.7424927473365}},
                    {{4.90318525184508, 5.99581694008571, 5.95615698669246}, {5.97444669974084, 7.12987554548257, 7.1095906114534}, {6.53741678314503, 7.89774782069033, 7.85927030323856}, {7.13754772163163, 8.79910033955154, 8.77810164915207}, {7.7562396411917, 9.81614830137095, 9.77351931847552}, {8.76689841135206, 11.1452991551185, 11.0811843459497}, {10.513368171242, 13.2459275832513, 13.1009567613693}, {13.1979037369898, 16.3323036754675, 16.1491682798314}},
                    {{6.02160192386879, 8.0427560221024, 7.93223007591373}, {7.45458506605638, 9.27703833686395, 9.17127434342562}, {7.94204962999446, 9.98039068148056, 9.90039605113603}, {8.66079232688593, 10.8162595699569, 10.653988942289}, {9.47351300957499, 11.7292236951962, 11.5907955087553}, {10.2689869608636, 13.110178084127, 12.8952102613367}, {12.2305610572211, 15.2364340660148, 14.9924012867386}, {14.3930003174177, 18.3138604479156, 17.9595357309848}},
                    {{7.61123713909278, 10.5887158283599, 10.4192188429678}, {9.20731703451098, 12.0628893168648, 11.9550573473496}, {9.85709132996928, 12.8701941652904, 12.6276025472299}, {10.3165183194159, 13.605842264484, 13.4316611205688}, {11.2692286351072, 14.4761780529619, 14.1929497377358}, {12.4358837402085, 15.9177322372191, 15.568967449971}, {14.5577475321854, 18.1287696351735, 17.5958497696997}, {16.874317177599, 21.112705389718, 20.5780027036999}},
                    {{10.114830426421, 14.4286223409782, 14.081749212272}, {11.6031746911885, 16.1683668751011, 15.6551122883117}, {12.0837641414121, 16.7824928835092, 16.2757903018179}, {12.7704455546218, 17.5347046049121, 17.0202232421845}, {13.5794854546779, 18.3596755686211, 17.7964343726763}, {14.9521289593159, 19.7235148101556, 19.1020295141703}, {17.3688525982791, 21.8409326336491, 21.1185396395873}, {20.69640161027, 24.8034535963609, 23.8084412646294}}
            };


    public void extract(BufferedImage bimg) {
        if (bimg.getColorModel().getColorSpace().getType() != ColorSpace.TYPE_RGB)
            throw new UnsupportedOperationException("Invalid color space (need RGB)");

        transform = createTransformArray();

        int newWidth = bimg.getWidth() - bimg.getWidth() % BLOCK_SIZE;
        int newHeight = bimg.getHeight() - bimg.getHeight() % BLOCK_SIZE;
        int[][][] yuvImage = getYUVImage(bimg.getRaster(), newWidth, newHeight, -128);
        descriptorValues = new int[BLOCK_SIZE * BLOCK_SIZE * 3];
        getComponentHistogram(yuvImage, newWidth, newHeight, 0, descriptorValues);
        getComponentHistogram(yuvImage, newWidth, newHeight, 1, descriptorValues);
        getComponentHistogram(yuvImage, newWidth, newHeight, 2, descriptorValues);
    }

    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(descriptorValues);
    }

    public void setByteArrayRepresentation(byte[] in) {
        descriptorValues = SerializationUtils.toIntArray(in);
    }

    public double[] getDoubleHistogram() {
        double[] result = new double[descriptorValues.length];
        for (int i = 0; i < descriptorValues.length; i++) {
            result[i] = (double) descriptorValues[i];
        }
        return result;
    }

    protected class DctPoint {
        public int i;
        public int j;
        public double v;
    }

    public class DctComparator implements Comparator<DctPoint> {


        public int compare(DctPoint o1, DctPoint o2) {
            // reverse sort
            return Double.compare(o1.v, o2.v) * -1;
        }

    }

    protected void getComponentHistogram(int[][][] yuvImage, int width, int height, int component, int[] descriptorBytes) {

        int hBlockCount = width / BLOCK_SIZE;
        int vBlockCount = height / BLOCK_SIZE;
        double[][] tempHistogram = new double[BLOCK_SIZE][BLOCK_SIZE];

        for (int by = 0; by < vBlockCount; by++) {
            for (int bx = 0; bx < hBlockCount; bx++) {
                double[][] dctValues = new double[BLOCK_SIZE][BLOCK_SIZE];
                for (int v = 0; v < BLOCK_SIZE; v++) {
                    for (int u = 0; u < BLOCK_SIZE; u++) {
                        double t = 0;
                        for (int j = 0; j < BLOCK_SIZE; j++) {
                            for (int i = 0; i < BLOCK_SIZE; i++) {
                                t += yuvImage[bx * BLOCK_SIZE + i][by * BLOCK_SIZE + j][component] * transform[i][j][u][v];

                                // Info Dump
                                // System.out.println(i + ", " + j +", " + u +", " + v + ", " +t + ", " + yuvImage[bx * BLOCK_SIZE + i][by * BLOCK_SIZE + j][component] + ", " + transform[i][j][u][v]);
                            }
                        }
                        double cU = 1;
                        double cV = 1;
                        if (u == 0)
                            cU = 0.707106781;
                        if (v == 0)
                            cV = 0.707106781;
                        dctValues[u][v] = cU * cV * t / 4;
                    }
                }

                // filtering for noise (ensures that c# and java implementations give the same output)
                dctValues[0][0] = 0;
                for (int j = 0; j < BLOCK_SIZE; j++) {
                    for (int i = 0; i < BLOCK_SIZE; i++) {
                        if (Math.abs(dctValues[i][j]) < 0.001)
                            dctValues[i][j] = 0;
                    }
                }

                DctPoint[] dctPoints = new DctPoint[BLOCK_SIZE * BLOCK_SIZE];
                int p = 0;
                for (int j = 0; j < BLOCK_SIZE; j++) {
                    for (int i = 0; i < BLOCK_SIZE; i++) {
                        dctPoints[p] = new DctPoint();
                        dctPoints[p].i = i;
                        dctPoints[p].j = j;
                        dctPoints[p].v = Math.abs(dctScaler2[i][j][component] * dctValues[i][j]);
                        p++;
                    }
                }


                Arrays.sort(dctPoints, new DctComparator());

                int n = 0;
                for (int cc = 0; cc < (BLOCK_SIZE * BLOCK_SIZE); cc++) {
                    tempHistogram[dctPoints[cc].i][dctPoints[cc].j] += 1.0 / (cc + 1);
                    n++;
                    if (n == 8)
                        break;
                }
            }
        }

        double maxPoint = 0;
        for (int j = 0; j < BLOCK_SIZE; j++) {
            for (int i = 0; i < BLOCK_SIZE; i++) {
                if (tempHistogram[i][j] > maxPoint)
                    maxPoint = tempHistogram[i][j];
            }
        }

        int p = BLOCK_SIZE * BLOCK_SIZE * component;
        for (int j = 0; j < BLOCK_SIZE; j++) {
            for (int i = 0; i < BLOCK_SIZE; i++) {
                descriptorBytes[p] = (int) (tempHistogram[i][j] / maxPoint * 255);
                p++;
            }
        }
    }


    public float getDistance(LireFeature vd) {
        if (!(vd instanceof JpegCoefficientHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");
        JpegCoefficientHistogram target = (JpegCoefficientHistogram) vd;
        if (descriptorValues == null)
            throw new UnsupportedOperationException("source descriptor bytes are null");
        if (target.descriptorValues == null)
            throw new UnsupportedOperationException("target descriptor bytes are null");

        int size2 = BLOCK_SIZE * BLOCK_SIZE * 3;
        double distance = 0;

        for (int i = 0; i < size2; i++)
            distance += ((descriptorValues[i] - target.descriptorValues[i]) * (descriptorValues[i] - target.descriptorValues[i]));

        return (float) Math.sqrt(distance / size2);
    }

    public String getStringRepresentation() { // added by mlux
        StringBuilder sb = new StringBuilder(descriptorValues.length * 2 + 25);
        sb.append("jpegcoeffhist");
        sb.append(' ');
        sb.append(descriptorValues.length);
        sb.append(' ');
        for (double aData : descriptorValues) {
            sb.append((int) aData);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String s) { // added by mlux
        StringTokenizer st = new StringTokenizer(s);
        if (!st.nextToken().equals("jpegcoeffhist"))
            throw new UnsupportedOperationException("This is not a jpegcoeffhist descriptor.");
        descriptorValues = new int[Integer.parseInt(st.nextToken())];
        for (int i = 0; i < descriptorValues.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
            descriptorValues[i] = (int) Integer.parseInt(st.nextToken());
        }

    }

    protected int[][][] getYUVImage(WritableRaster raster, int newWidth, int newHeight, int shift) {
        int[][][] yuvImage = new int[newWidth][newHeight][3];
        int[] rgbPixel = new int[3];
        for (int j = 0; j < newHeight; j++) {
            for (int i = 0; i < newWidth; i++) {
                raster.getPixel(i, j, rgbPixel);

                int r = rgbPixel[0];
                int g = rgbPixel[1];
                int b = rgbPixel[2];

                // order: Y, U, V
                yuvImage[i][j][0] = (int) (0.299 * r + 0.587 * g + 0.114 * b) + shift;
                yuvImage[i][j][1] = (int) (128 - 0.1687 * r - 0.3313 * g + 0.5 * b) + shift;
                yuvImage[i][j][2] = (int) (128 + 0.5 * r - 0.4187 * g - 0.0813 * b) + shift;
            }
        }
        return yuvImage;
    }


    protected double[][][][] createTransformArray() {
        double[][][][] t = new double[BLOCK_SIZE][BLOCK_SIZE][BLOCK_SIZE][BLOCK_SIZE];
        for (int v = 0; v < BLOCK_SIZE; v++) {
            for (int u = 0; u < BLOCK_SIZE; u++) {
                for (int j = 0; j < BLOCK_SIZE; j++) {
                    for (int i = 0; i < BLOCK_SIZE; i++) {
                        t[i][j][u][v] = Math.cos(((2 * i + 1) * u * Math.PI) / 2 / BLOCK_SIZE)
                                * Math.cos(((2 * j + 1) * v * Math.PI) / 2 / BLOCK_SIZE);
                    }
                }
            }
        }
        return t;
    }

}
