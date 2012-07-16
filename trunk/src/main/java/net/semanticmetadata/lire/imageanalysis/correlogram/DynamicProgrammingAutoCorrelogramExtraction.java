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

import java.util.Arrays;


/**
 * DynamicProgrammingAutoCorrelogramExtraction is an implementation of dynamic programming approach to
 * build auto-correlogram feature vector from images. It is based on Huang et al paper,
 * "Image Indexing Using Color Correlograms", CVPR1997. J Huang, S. R Kumar, M. Mitra, W. Zhu, R. Zabih
 *
 * @author Rodrigo Carvalho Rezende <rcrezende@gmail.com> http://www.rodrigorezende.net/
 *         <p/>
 *         I noticed that although this approach has teoretical complexity O(|I|*d) lower than a naive method O(|I|*d²),
 *         in practice is slower and consumes more memory.
 *         It is recomended to use only one object for each thread. So, it is recomended to change the way of instance this class.
 */
public class DynamicProgrammingAutoCorrelogramExtraction implements IAutoCorrelogramFeatureExtractor {

    public static final DynamicProgrammingAutoCorrelogramExtraction instance = new DynamicProgrammingAutoCorrelogramExtraction();

    public static final DynamicProgrammingAutoCorrelogramExtraction getInstance() {
        return instance;
    }

    private DynamicProgrammingAutoCorrelogramExtraction() {
    }

    /* please check the paper to understand this code*/

    /*pixel sets with the same color, so Ic[c] is the set of all pixels with color c*/
    private int[][][] Ic = null;
    /*see Huang paper, ah and av are the lambda^h and lambda^v values*/
    private int[][][][] ah = null;
    private int[][][][] av = null;
    /*represents the sizee of pixel set Ic[c]. So NIc[c] says how many pixels are in the set Ic[c]*/
    private int[] NIc = null;

    /*current maximum bounds, reuses the initialization of buffers smaller than the lasts. This may increase!*/
    private int MAXC = 64;
    private int MAXH = 200;
    private int MAXW = 200;
    private int MAXD = 5;

    /**
     * extract extracts an auto-correlogram from an Image
     * Not thread safe, use one object per thread.
     *
     * @param maxFeatureValue the maximum color value
     * @param distanceSet     the distance windows of auto-correlogram
     * @param img             the image
     * @return float[][] the auto-correlogram A[color][distance]
     */
    public synchronized float[][] extract(int maxFeatureValue, int[] distanceSet, int[][] img) {
        long totalComplexity = 0;
        final int W = img.length;
        final int H = img[0].length;

        final int MAX_D = distanceSet[distanceSet.length - 1];
        //the Autocorrelogram
        final float[][] A = new float[maxFeatureValue][distanceSet.length];
        //Histogram
        final float[] Hi = new float[maxFeatureValue];

        //please, becareful with this buffers in multi-threaded environment
        //lazy initialization of buffers, this object is reused next extraction without reinitialization of buffers
        if (Ic == null || ah == null || av == null || NIc == null || maxFeatureValue > MAXC || W > MAXW || H > MAXH || MAX_D > MAXD) {
            Ic = null;
            NIc = null;
            ah = null;
            av = null;
            System.gc();

            //increase the current bounds
            MAXC = Math.max(MAXC, maxFeatureValue);
            MAXW = Math.max(MAXW, W);
            MAXD = Math.max(MAXD, MAX_D);
            MAXH = Math.max(MAXH, H);

            Ic = new int[MAXC][MAXH * MAXW][2];
            NIc = new int[MAXC];
            ah = new int[MAXD][MAXW][MAXH][MAXC];
            av = new int[MAXD - 1][MAXW][MAXH][MAXC];

        }

        //build color histogram
        for (int x = 0; x < W; ++x)
            for (int y = 0; y < H; ++y) {
                Hi[img[x][y]]++;
                totalComplexity++;
            }

        //defines that Ic doesn't have pixels of any color
        Arrays.fill(NIc, 0);

        //it is necessary to initialize lambda^v and lambda^h only when d=0
        //therefore it was used a separate buffers to the case when d=0.
        //It would be nice if Java had memset() like C, so this wouldn't be necessary.
        //So ah0 and av0 are ah[d=0] and av[d=0]
        //and ah[0..MAX_D-1] and av[0..MAX_D-1] are ah[d=1..d=MAX_D] and av[d=1..d=MAX_D]
        //this approach saves time because only ah0 and av0 are reinitialized every extraction
        int[][][] ah0 = new int[W][H][maxFeatureValue];
        int[][][] av0 = new int[W][H][maxFeatureValue];

        //I do recommend use the Huang paper as a reference of this code.
        //I tried to use the same variables in comments.

        //builds I_c sets and initializes lambda^h and lambda^v when d=0

        //lambda^{c,h}_{(x,y)}(0) = 1 if pixel (x,y) in I_c and 0 otherwise
        //note that lambda^{c,h}_{(x,y)}(0) was already zeroed when ah0 were instanced (same for av0)
        for (int x = 0; x < W; ++x)
            for (int y = 0; y < H; ++y) {
                Ic[img[x][y]][NIc[img[x][y]]][0] = x;
                Ic[img[x][y]][NIc[img[x][y]]][1] = y;
                NIc[img[x][y]]++;
                ah0[x][y][img[x][y]] = av0[x][y][img[x][y]] = 1;

                totalComplexity++;
            }

        //lambda^{c,h}_{(x,y)}(k) = lambda^{c,h}_{(x,y)}(k-1) + lambda^{c,h}_{(x+k,y)}(0)
        //lambda^{c,v}_{(x,y)}(k) = lambda^{c,v}_{(x,y)}(k-1) + lambda^{c,v}_{(x,y+k)}(0)

        for (int x = 0; x < W; ++x)
            for (int y = 0; y < H; ++y) {
                final int c = img[x][y];
                //d=1
                ah[0][x][y][c] = ah0[x][y][c] + ((x + 1 < W) ? ah0[x + 1][y][c] : 0);
                av[0][x][y][c] = av0[x][y][c] + ((y + 1 < H) ? ah0[x][y + 1][c] : 0);
                //d=2..MAX_D
                for (int d = 2; d <= MAX_D; ++d) {
                    ah[d - 1][x][y][c] = ah[d - 2][x][y][c] + ((x + d < W) ? ah0[x + d][y][c] : 0);
                    totalComplexity++;
                }
                for (int d = 2; d <= MAX_D - 1; ++d)
                    av[d - 1][x][y][c] = av[d - 2][x][y][c] + ((y + d < H) ? av0[x][y + d][c] : 0);
            }

        //do the dynamic programming approach:
        //A_{c,d} = sum_{(x,y) in I_c}{
        //   ( lambda^{c,h}_{(x-k,y+k)}(2k) + lambda^{c,h}_{(x-k,y-k)}(2k) +
        //     lambda^{c,v}_{(x-k,y-k+1)}(2k-2) + lambda^{c,v}_{(x+k,y-k+1)}(2k-2) )
        //}

        //I separate the 2k distance in two sums of k,
        //since it allows smaller buffers and better performance.
        //You may understand better seeing the case when d=2..MAX_D

        //the case when d=1:
        for (int c = 0; c < maxFeatureValue; ++c) {
            for (int i = 0; i < NIc[c]; ++i) {
                final int x = Ic[c][i][0];
                final int y = Ic[c][i][1];
                A[c][0] += ((y - 1 >= 0) ? ((x - 1 >= 0) ? ah[0][x - 1][y - 1][c] : 0) + ah[0][x][y - 1][c] : 0) +
                        ((y + 1 < H) ? ((x - 1 >= 0) ? ah[0][x - 1][y + 1][c] : 0) + ah[0][x][y + 1][c] : 0) +
                        ((x - 1 >= 0) ? av0[x - 1][y][c] + av0[x - 1][y][c] : 0) +
                        ((x + 1 < W) ? av0[x + 1][y][c] + av0[x + 1][y][c] : 0);

                totalComplexity++;
            }
        }

        //when d=2..MAX_D
        for (int di = 1; di < distanceSet.length; ++di) {
            final int d = distanceSet[di];
            for (int c = 0; c < maxFeatureValue; ++c) {
                for (int i = 0; i < NIc[c]; ++i) {
                    final int x = Ic[c][i][0];
                    final int y = Ic[c][i][1];

                    A[c][di] += ((y - d >= 0) ? ((x - d >= 0) ? ah[d - 1][x - d][y - d][c] : 0) + ah[d - 1][x][y - d][c] : 0) +
                            ((y + d < H) ? ((x - d >= 0) ? ah[d - 1][x - d][y + d][c] : 0) + ah[d - 1][x][y + d][c] : 0) +
                            ((x - d >= 0) ? ((y - d + 1 >= 0) ? av[d - 2][x - d][y - d + 1][c] : 0) + av[d - 2][x - d][y][c] : 0) +
                            ((x + d < W) ? ((y - d + 1 >= 0) ? av[d - 2][x + d][y - d + 1][c] : 0) + av[d - 2][x + d][y][c] : 0);

                    totalComplexity++;
                }
            }
        }

        //normalize according to the paper
        for (int c = 0; c < maxFeatureValue; ++c)
            for (int di = 0; di < distanceSet.length; ++di) {
                final int d = distanceSet[di];
                if (Hi[c] > 0.0f)
                    A[c][di] /= (Hi[c] * d * 8.0f);
            }


        //ok!!! everything was done!!! ;-)
        //System.out.println("Complexity: "+((float)totalComplexity/(H*W))+"*O(|I|)");
        return A;
    }

    public static void main(String[] args) {
        int[][] I = new int[384][256];
        float[][] A = null;
        long t0, tf;
        int C = 64;
        int[] D = {1, 3, 5, 7};

        for (int i = 0; i < I.length; i++)
            for (int j = 0; j < I[i].length; j++)
                I[i][j] = ((i + 1) * (j * j + 1)) % C;

        tf = System.currentTimeMillis();

        DynamicProgrammingAutoCorrelogramExtraction dynACorrExt = new DynamicProgrammingAutoCorrelogramExtraction();
        for (int i = 0; i < 10; i++) {
            t0 = tf;
            A = dynACorrExt.extract(C, D, I);
            tf = System.currentTimeMillis();
            System.out.println("Exctraction " + (i + 1) + " time: " + (tf - t0) + "ms");
        }
        System.out.println("Please, ignore the first exctraction (buffers initialization)!");
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


