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

import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.util.StringTokenizer;

/**
 * Implementation of a Gabor texture features done by  Marko Keuschnig & Christian Penz<br>
 * Fixed 2011-05-10 based on the comments of Arthur Lin.
 */

public class Gabor implements LireFeature {

    private static final double U_H = .4;
    private static final double U_L = .05;
    private static final int S = 4, T = 4; // filter mask size
    private static final int M = 5, N = 6; // scale & orientation

    private static final int MAX_IMG_HEIGHT = 64;

    private static final double A = Math.pow((U_H / U_L), 1. / (M - 1));
    private static double[] theta = new double[N];
    private static double[] modulationFrequency = new double[M];
    private static double[] sigma_x = new double[M];
    private static double[] sigma_y = new double[M];
    private static double[][][][][] selfSimilarGaborWavelets = new double[S][T][M][N][2];

    private static final double LOG2 = Math.log(2);

    private double[][][][][] gaborWavelet = null;
    private double[] histogram;

    static {
        for (int i = 0; i < N; i++) {
            theta[i] = i * Math.PI / N;
        }
        for (int i = 0; i < M; i++) {
            modulationFrequency[i] = Math.pow(A, i) * U_L;
            sigma_x[i] =
                    (A + 1) * Math.sqrt(2 * LOG2) /
                            (2 * Math.PI * Math.pow(A, i) * (A - 1) * U_L);
            sigma_y[i] = 1 / (2 * Math.PI * Math.tan(Math.PI / (2 * N)) * Math.sqrt(Math.pow(U_H, 2) / (2 * LOG2) - Math.pow(1 / (2 * Math.PI * sigma_x[i]), 2)));

        }
        Gabor gaborFeature = new Gabor();
        double[] selfSimilarGaborWavelet;
        for (int s = 0; s < S; s++) {
            for (int t = 0; t < T; t++) {
                for (int m = 0; m < M; m++) {
                    for (int n = 0; n < N; n++) {
                        selfSimilarGaborWavelet = gaborFeature.selfSimilarGaborWavelet(s, t, m, n);
                        selfSimilarGaborWavelets[s][t][m][n][0] = selfSimilarGaborWavelet[0];
                        selfSimilarGaborWavelets[s][t][m][n][1] = selfSimilarGaborWavelet[1];
                    }
                }
            }
        }
    }


    public double getDistance(double[] targetFeatureVector, double[] queryFeatureVector) {
        double distance = 0;
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                distance += Math.sqrt(Math.pow(queryFeatureVector[m * 2 * N + n * 2] - targetFeatureVector[m * 2 * N + n * 2], 2) + Math.pow(queryFeatureVector[m * 2 * N + n * 2 + 1] - targetFeatureVector[m * 2 * N + n * 2 + 1], 2));
            }
        }

        return distance;
    }

    public double[] getNormalizedFeature(BufferedImage image) {
        return normalize(getFeature(image));
    }

    public double[] normalize(double[] featureVector) {
        int dominantOrientation = 0;
        double orientationVectorSum = 0;
        double orientationVectorSum2 = 0;
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                orientationVectorSum2 += Math.sqrt(Math.pow(featureVector[m * 2 * N + n * 2], 2) + Math.pow(featureVector[m * 2 * N + n * 2 + 1], 2));
            }
            if (orientationVectorSum2 > orientationVectorSum) {
                orientationVectorSum = orientationVectorSum2;
                dominantOrientation = m;
            }
        }

        double[] normalizedFeatureVector = new double[featureVector.length];
        for (int m = dominantOrientation, k = 0; m < M; m++, k++) {
            for (int n = 0; n < N; n++) {
                normalizedFeatureVector[k * 2 * N + n * 2] = featureVector[m * 2 * N + n * 2];
                normalizedFeatureVector[k * 2 * N + n * 2 + 1] = featureVector[m * 2 * N + n * 2 + 1];
            }
        }
        for (int m = 0, k = M - dominantOrientation; m < dominantOrientation; m++, k++) {
            for (int n = 0; n < N; n++) {
                normalizedFeatureVector[k * 2 * N + n * 2] = featureVector[m * 2 * N + n * 2];
                normalizedFeatureVector[k * 2 * N + n * 2 + 1] = featureVector[m * 2 * N + n * 2 + 1];
            }
        }

        return normalizedFeatureVector;
    }

    public BufferedImage grayscale(BufferedImage source) {
        BufferedImageOp op = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        return op.filter(source, null);
    }

    public double[] getFeature(BufferedImage image) {
        image = ImageUtils.scaleImage(image, MAX_IMG_HEIGHT);
        Raster imageRaster = image.getRaster();
        int[][] grayLevel = new int[imageRaster.getWidth()][imageRaster.getHeight()];
        int[] tmp = new int[3];
        for (int i = 0; i < imageRaster.getWidth(); i++) {
            for (int j = 0; j < imageRaster.getHeight(); j++) {
                grayLevel[i][j] = imageRaster.getPixel(i, j, tmp)[0];
            }
        }

        double[] featureVector = new double[M * N * 2];
        double[][] magnitudes = computeMagnitudes(grayLevel);
        int imageSize = image.getWidth() * image.getHeight();
        double[][] magnitudesForVariance = new double[M][N];

        if (this.gaborWavelet == null) {
            precomputeGaborWavelet(grayLevel);
        }

        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                featureVector[m * 2 * N + n * 2] = magnitudes[m][n] / imageSize;
                for (int i = 0; i < magnitudesForVariance.length; i++) {
                    for (int j = 0; j < magnitudesForVariance[0].length; j++) {
                        magnitudesForVariance[i][j] = 0.;
                    }
                }
                for (int x = S; x < image.getWidth(); x++) {
                    for (int y = T; y < image.getHeight(); y++) {
                        magnitudesForVariance[m][n] += Math.pow(Math.sqrt(Math.pow(this.gaborWavelet[x - S][y - T][m][n][0], 2) + Math.pow(this.gaborWavelet[x - S][y - T][m][n][1], 2)) - featureVector[m * 2 * N + n * 2], 2);
                    }
                }

                featureVector[m * 2 * N + n * 2 + 1] = Math.sqrt(magnitudesForVariance[m][n]) / imageSize;
            }
        }
        this.gaborWavelet = null;

        return featureVector;
    }

    private void precomputeGaborWavelet(int[][] image) {
        this.gaborWavelet = new double[image.length - S][image[0].length - T][M][N][2];
        double[] gaborWavelet;
        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                for (int x = S; x < image.length; x++) {
                    for (int y = T; y < image[0].length; y++) {
                        gaborWavelet = gaborWavelet(image, x, y, m, n);
                        this.gaborWavelet[x - S][y - T][m][n][0] = gaborWavelet[0];
                        this.gaborWavelet[x - S][y - T][m][n][1] = gaborWavelet[1];
                    }
                }
            }
        }
    }

    private double[][] computeMagnitudes(int[][] image) {
        double[][] magnitudes = new double[M][N];
        for (int i = 0; i < magnitudes.length; i++) {
            for (int j = 0; j < magnitudes[0].length; j++) {
                magnitudes[i][j] = 0.;
            }
        }

        if (this.gaborWavelet == null) {
            precomputeGaborWavelet(image);
        }

        for (int m = 0; m < M; m++) {
            for (int n = 0; n < N; n++) {
                for (int x = S; x < image.length; x++) {
                    for (int y = T; y < image[0].length; y++) {
                        magnitudes[m][n] += Math.sqrt(Math.pow(this.gaborWavelet[x - S][y - T][m][n][0], 2) + Math.pow(this.gaborWavelet[x - S][y - T][m][n][1], 2));

                    }
                }
            }
        }
        return magnitudes;
    }

    // returns 2 doubles representing the real ([0]) and imaginary ([1]) part of the mother wavelet
    private double[] gaborWavelet(int[][] img, int x, int y, int m, int n) {
        double re = 0;
        double im = 0;
        for (int s = 0; s < S; s++) {
            for (int t = 0; t < T; t++) {
                re += img[x][y] * selfSimilarGaborWavelets[s][t][m][n][0];
                im += img[x][y] * -selfSimilarGaborWavelets[s][t][m][n][1];
            }
        }

        return new double[]{re, im};
    }

    // returns 2 doubles representing the real ([0]) and imaginary ([1]) part of the mother wavelet
    private double[] computeMotherWavelet(double x, double y, int m, int n) {

        return new double[]{
                1 / (2 * Math.PI * sigma_x[m] * sigma_y[m]) *
                        Math.exp(-1 / 2 * (Math.pow(x, 2) / Math.pow(sigma_x[m], 2) + Math.pow(y, 2) / Math.pow(sigma_y[m], 2))) *
                        Math.cos(2 * Math.PI * modulationFrequency[m] * x),
                1 / (2 * Math.PI * sigma_x[m] * sigma_y[m]) *
                        Math.exp(-1 / 2 * (Math.pow(x, 2) / Math.pow(sigma_x[m], 2) + Math.pow(y, 2) / Math.pow(sigma_y[m], 2))) *
                        Math.sin(2 * Math.PI * modulationFrequency[m] * x)};
    }

    private double x_tilde(int x, int y, int m, int n) {
        return
                Math.pow(A, -m) * (x * Math.cos(theta[n]) + y * Math.sin(theta[n]));
    }

    private double y_tilde(int x, int y, int m, int n) {
        return
                Math.pow(A, -m) * (-x * Math.sin(theta[n] + y * Math.cos(theta[n])));
    }

    private double[] selfSimilarGaborWavelet(int x, int y, int m, int n) {
        double[] motherWavelet = computeMotherWavelet(x_tilde(x, y, m, n), y_tilde(x, y, m, n), m, n);
        return new double[]{
                Math.pow(A, -m) * motherWavelet[0],
                Math.pow(A, -m) * motherWavelet[1]};
    }

    public void extract(BufferedImage bimg) {  // added by mlux
        histogram = getNormalizedFeature(bimg);
    }

    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(histogram);
    }

    public void setByteArrayRepresentation(byte[] in) {
        histogram = SerializationUtils.toDoubleArray(in);
    }

    public double[] getDoubleHistogram() {
        return histogram;
    }

    public float getDistance(LireFeature vd) {   // added by mlux
        // Check if instance of the right class ...
        if (!(vd instanceof Gabor))
            throw new UnsupportedOperationException("Wrong descriptor.");

        // casting ...
        Gabor ch = (Gabor) vd;

        // check if parameters are fitting ...
        if ((ch.histogram.length != histogram.length))
            throw new UnsupportedOperationException("Histogram lengths or color spaces do not match");

        return (float) getDistance(histogram, ch.histogram);
    }

    public String getStringRepresentation() {  // added by mlux
        StringBuilder sb = new StringBuilder(histogram.length * 2 + 25);
        sb.append("gabor");
        sb.append(' ');
        sb.append(histogram.length);
        sb.append(' ');
        for (double v : histogram) {
            sb.append(v);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String s) {  // added by mlux
        StringTokenizer st = new StringTokenizer(s);
        String name = st.nextToken();
        if (!name.equals("gabor")) {
            throw new UnsupportedOperationException("This is not a Gabor feature string.");
        }

        /*
        * changes made by Ankit Jain here otherwise the histogram length would be assigned to histogram[i]
        * jankit87@gmail.com
        * */
        histogram = new double[Integer.parseInt(st.nextToken())];

        for (int i = 0; i < histogram.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
            histogram[i] = Double.parseDouble(st.nextToken());
        }
    }
}
