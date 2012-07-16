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

package net.semanticmetadata.lire.imageanalysis.correlogram;


/**
 * NaiveAutoCorrelogramExtraction is an implementation of the naice approach to extract auto-correlogram
 * feature vector from images (Full Neighborhood is used). It is based on Huang et al paper,
 * "Image Indexing Using Color Correlograms", CVPR1997. J Huang, S. R Kumar, M. Mitra, W. Zhu, R. Zabih
 * <p/>
 * This method is very similar to MLux FullNeighbourhood algorithm, but
 * doesn't acummulate the feature frequence over different distances and
 * uses the standard normalization.
 *
 * @author Rodrigo Carvalho Rezende <rcrezende@gmail.com> http://www.rodrigorezende.net/
 */
public class NaiveAutoCorrelogramExtraction implements IAutoCorrelogramFeatureExtractor {
    /**
     * extract extracts an auto-correlogram from an Image
     *
     * @param maxFeatureValue the maximum feature (color) value
     * @param distanceSet     the distance windows of auto-correlogram
     * @param img             the image
     * @return float[][] the auto-correlogram A[color][distance]
     */
    public float[][] extract(int maxFeatureValue, int[] distanceSet, int[][] img) {
        long totalComplexity = 0;

        int[] histogram = new int[maxFeatureValue];
        final float[][] correlogram = new float[maxFeatureValue][distanceSet.length];

        final int W = img.length;
        final int H = img[0].length;

        //builds the histogram for normalization
        for (int x = 0; x < W; x++)
            for (int y = 0; y < H; y++) {
                histogram[img[x][y]]++;
                totalComplexity++;
            }

        //for each distance window $d$
        int N_DIST = distanceSet.length;
        for (int di = 0; di < N_DIST; ++di) {
            int d = distanceSet[di];
            //for each pixel $p$
            for (int x = 0; x < W; ++x) {
                for (int y = 0; y < H; ++y) {
                    int c = img[x][y];
                    //counts each pixel in neighborhood (distance $d$) which has the same color of pixel $p$

                    //horizontal
                    for (int dx = -d; dx <= d; dx++) {
                        int X = x + dx, Y = y - d;
                        if (0 <= X && X < W && 0 <= Y && Y < H && img[X][Y] == c) {
                            correlogram[c][di]++;
                            totalComplexity++;
                        }
                        Y = y + d;
                        if (0 <= X && X < W && 0 <= Y && Y < H && img[X][Y] == c) {
                            correlogram[c][di]++;
                            totalComplexity++;
                        }
                    }
                    //vertical
                    for (int dy = -d + 1; dy <= d - 1; dy++) {
                        int X = x - d, Y = y + dy;
                        if (0 <= X && X < W && 0 <= Y && Y < H && img[X][Y] == c) {
                            correlogram[c][di]++;
                            totalComplexity++;
                        }
                        X = x + d;
                        if (0 <= X && X < W && 0 <= Y && Y < H && img[X][Y] == c) {
                            correlogram[c][di]++;
                            totalComplexity++;
                        }
                    }
                }
            }
            //normalize the feature vector
            for (int c = 0; c < maxFeatureValue; ++c)
                if (histogram[c] > 0)
                    correlogram[c][di] = (float) correlogram[c][di] / (((float) histogram[c]) * 8.0f * d);
        }
//		System.out.println("Complexity: "+((float)totalComplexity/(H*W))+"*O(|I|)");
        return correlogram;
    }

    public static void main(String[] args) {
        int[][] I = new int[200][200];
        float[][] A = null;
        long t0, tf;
        int C = 16;
        int[] D = {1, 3, 5, 7, 10};

        for (int i = 0; i < I.length; i++)
            for (int j = 0; j < I[i].length; j++)
                I[i][j] = ((i + 1) * (j * j + 1)) % C;

        tf = System.currentTimeMillis();
        NaiveAutoCorrelogramExtraction naivACorrExt = new NaiveAutoCorrelogramExtraction();
        for (int i = 0; i < 10; i++) {
            t0 = tf;
            A = naivACorrExt.extract(C, D, I);
            tf = System.currentTimeMillis();
            System.out.println("Exctraction " + (i + 1) + " time: " + (tf - t0) + "ms");
        }
        print(A);


    }

    static void print(float[][] M) {
        System.out.println();
        for (int i = 0; i < M.length; i++) {
            for (int j = 0; j < M[i].length; j++) {
                System.out.print(M[i][j] + " ");
            }
            System.out.println();
        }
    }

    static void print(int[][] M) {
        System.out.println();
        for (int i = 0; i < M.length; i++) {
            for (int j = 0; j < M[i].length; j++) {
                System.out.print(M[i][j] + " ");
            }
            System.out.println();
        }
    }
}


