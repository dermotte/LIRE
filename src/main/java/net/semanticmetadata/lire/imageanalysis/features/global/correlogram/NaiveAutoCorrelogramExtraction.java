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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.imageanalysis.features.global.correlogram;


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
                    correlogram[c][di] = (float) Math.floor(16d*(correlogram[c][di] / (((float) histogram[c]) * 8.0f * d)));
        }
//		System.out.println("Complexity: "+((float)totalComplexity/(H*W))+"*O(|I|)");
        return correlogram;
    }
}


