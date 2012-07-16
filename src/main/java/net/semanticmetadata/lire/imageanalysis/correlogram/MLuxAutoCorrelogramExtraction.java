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

import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram.Mode;

import java.util.Arrays;

/**
 * This method was developed by Mathias Lux. It is an accumulated auto-correlogram
 *
 * @author Mathias Lux
 * @author Rodrigo Carvalho Rezende <rcrezende@gmail.com> http://www.rodrigorezende.net/
 */
public class MLuxAutoCorrelogramExtraction implements IAutoCorrelogramFeatureExtractor {
    private Mode mode;

    public MLuxAutoCorrelogramExtraction() {
        this(Mode.SuperFast);
    }

    public MLuxAutoCorrelogramExtraction(Mode mode) {
        this.mode = mode;
    }

    /**
     * extract extracts an auto-correlogram from an Image. This method
     * create a cummulated auto-correlogram over different distances
     * instead of standard method. Also, uses a different normalization
     * method
     *
     * @param maxFeatureValue the maximum feature (color) value
     * @param distanceSet     the distance windows of auto-correlogram
     * @param img             the image
     * @return float[][] the auto-correlogram A[color][distance]
     */
    public float[][] extract(int maxFeatureValue, int[] distanceSet, int[][] img) {
        int[] histogram = new int[maxFeatureValue];

        final int W = img.length;
        final int H = img[0].length;
//		 it is not necessary to zero!        
//		for (int i = 0; i < histogram.length; i++) {
//            histogram[i] = 0;
//        }

        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                // for normalization:
                histogram[img[x][y]]++;
            }
        }

        // Find the auto-correlogram.
        float[][] correlogram = new float[maxFeatureValue][distanceSet.length];
//      it is not necessary to zero!        
//        for (int i1 = 0; i1 < correlogram.length; i1++) {
//            for (int j = 0; j < correlogram[i1].length; j++) {
//                correlogram[i1][j] = 0;
//            }
//        }
        int[] tmpCorrelogram = new int[distanceSet.length];
        for (int x = 0; x < W; x++) {
            for (int y = 0; y < H; y++) {
                int color = img[x][y];
                getNumPixelsInNeighbourhood(x, y, img, tmpCorrelogram, maxFeatureValue, distanceSet);
                for (int i = 0; i < distanceSet.length; i++) {
                    // bug fixed based on comments of Rodrigo Carvalho Rezende, rcrezende <at> gmail.com
                    correlogram[color][i] += tmpCorrelogram[i];
                }
            }
        }
        // normalize the correlogram:
        // Note that this is not the common normalization routine described in the paper, but an adapted one.
        float[] max = new float[distanceSet.length];
// it is not necessary to zero!        
//        for (int i = 0; i < max.length; i++) {
//            max[i] = 0;
//
//        }
        for (int c = 0; c < maxFeatureValue; c++) {
            for (int i = 0; i < distanceSet.length; i++) {
                max[i] = Math.max(correlogram[c][i], max[i]);
            }
        }
        for (int c = 0; c < maxFeatureValue; c++) {
            for (int i = 0; i < distanceSet.length; i++) {
                correlogram[c][i] = correlogram[c][i] / max[i];
            }
        }

        return correlogram;
    }

    private void getNumPixelsInNeighbourhood(int x, int y, int[][] quantPixels, int[] correlogramm, int maxFeatureValue, int[] distanceSet) {
        // set to zero for each color at distance 1:
        Arrays.fill(correlogramm, 0);

        for (int di = 0; di < distanceSet.length; di++) {
            int d = distanceSet[di];
            // bug fixed based on comments of Rodrigo Carvalho Rezende, rcrezende <at> gmail.com
            if (di > 0) correlogramm[di] += correlogramm[di - 1]; // -> Wrong! Just the reference
//            if (d > 1) System.arraycopy(correlogramm[d - 2], 0, correlogramm[d - 1], 0, correlogramm[d - 1].length);
            int color = quantPixels[x][y];
            if (mode == Mode.QuarterNeighbourhood) {
                // TODO: does not work properly (possible) -> check if funnny
                for (int td = 0; td < d; td++) {
                    if (isInPicture(x + d, y + td, quantPixels.length, quantPixels[0].length))
                        if (quantPixels[x + d][y + td] == color) correlogramm[di]++;
                    if (isInPicture(x + td, y + d, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x + td][y + d]) correlogramm[di]++;
                    if (isInPicture(x + d, y + d, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x + d][y + d]) correlogramm[di]++;
                }
            } else if (mode == Mode.FullNeighbourhood) {
                //if (isInPicture(x + d, y + d, quantPixels.length, quantPixels[0].length))
                //    correlogramm[quantPixels[x + d][y + d]][d - 1]++;
                for (int i = -d; i <= d; i++) {
                    if (isInPicture(x + i, y + d, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x + i][y + d]) correlogramm[di]++;
                    if (isInPicture(x + i, y - d, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x + i][y - d]) correlogramm[di]++;
                }
                for (int i = -d + 1; i <= d - 1; i++) {
                    if (isInPicture(x + d, y + i, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x + d][y + i]) correlogramm[di]++;
                    if (isInPicture(x - d, y + i, quantPixels.length, quantPixels[0].length))
                        if (color == quantPixels[x - d][y + i]) correlogramm[di]++;
                }
            } else {
                if (isInPicture(x + d, y, quantPixels.length, quantPixels[0].length)) {
                    assert (quantPixels[x + d][y] < maxFeatureValue);
                    if (color == quantPixels[x + d][y]) correlogramm[di]++;
                }
                if (isInPicture(x, y + d, quantPixels.length, quantPixels[0].length)) {
                    assert (quantPixels[x][y + d] < maxFeatureValue);
                    if (color == quantPixels[x][y + d]) correlogramm[di]++;
                }
            }
        }
    }

    private static boolean isInPicture(int x, int y, int maxX, int maxY) {
        // possibly made faster??
        return !(x < 0 || y < 0) && !(y >= maxY || x >= maxX);
    }

}
