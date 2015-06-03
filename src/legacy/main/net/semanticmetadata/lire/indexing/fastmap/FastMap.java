/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
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
package net.semanticmetadata.lire.indexing.fastmap;


/**
 * Date: 13.01.2005
 * Time: 23:18:35
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FastMap implements Runnable {
    private double[][] X;
    /**
     * Pivots
     */
    private int[][] PA;
    private boolean predefinedPivots = false;
    private int col, currentDimension;
    private FastmapDistanceMatrix matrixFastmap;
    int dimensions;

    /**
     * Creates a new FastMap which uses attached FastmapDistanceMatrix and projects to
     * a space with given dimensions
     *
     * @param dimensions    defines the dimensions of the target space
     * @param matrixFastmap the distance matrixFastmapFastmap upon the computation takes place
     */
    public FastMap(FastmapDistanceMatrix matrixFastmap, int dimensions) {
        this.matrixFastmap = matrixFastmap;
        this.dimensions = dimensions;
        init();
    }

    /**
     * Creates a new FastMap which uses attached FastmapDistanceMatrix and projects to
     * a space with given dimensions
     *
     * @param dimensions    defines the dimensions of the target space
     * @param matrixFastmap the distance matrixFastmapFastmap upon the computation takes place
     * @param pivots
     */
    public FastMap(FastmapDistanceMatrix matrixFastmap, int dimensions, int[][] pivots) {
        this.matrixFastmap = matrixFastmap;
        this.dimensions = dimensions;
        init();
        // set the pivots from the parameters:
        predefinedPivots = true;
        PA = pivots;
    }

    private void init() {
        X = new double[matrixFastmap.getDimension()][dimensions];
        PA = new int[2][dimensions];
        col = 0;
        currentDimension = 0;
    }

    public double[][] getPoints() {
        return X;
    }

    public void run() {
        while (fastMap() > 0) ;
    }

    private int fastMap() {
        int k = dimensions - currentDimension;
        if (k <= 0)
            return 0;
        else
            col++;
        // TODO: change if pivots are known before ...
        if (!predefinedPivots) findPivots(k);
        int pivot1 = PA[0][k - 1];
        int pivot2 = PA[1][k - 1];
        if (matrixFastmap.getDistance(pivot1, pivot2, currentDimension, X[pivot1], X[pivot2]) == 0f) {
            for (int i = 0; i < X.length; i++) {
                double[] floats = X[i];
                floats[col - 1] = 0d;
            }
            // and stop here ...
            return 0;
        }

        for (int i = 0; i < matrixFastmap.getDimension(); i++) {
            // X[num1][col-2]-X[num2][col-2]
            double d_ab2 = matrixFastmap.getDistance(pivot1, pivot2, currentDimension, X[pivot1], X[pivot2]);
            double d_ai2 = matrixFastmap.getDistance(pivot1, i, currentDimension, X[pivot1], X[i]);
            double d_bi2 = matrixFastmap.getDistance(pivot2, i, currentDimension, X[pivot2], X[i]);

            X[i][col - 1] = (d_ai2 * d_ai2 + d_ab2 * d_ab2 - d_bi2 * d_bi2) / (2f * d_ab2);
        }
        currentDimension++;
        return k - 1;
    }

    /**
     * Finds the pivots where the other points are interpolated in between. This method is definitely issue
     * to tuning. Currently a greedy approach is implemented.
     *
     * @param k
     */
    private void findPivots(int k) {
        // increase this number to get it more precise, decrease to get it faster.
        int numIterations = 4;
        int randomRow = (int) (Math.random() * matrixFastmap.getDimension());
        int pivot1 = 0;
        int pivot2 = randomRow;
        for (int i = 0; i < numIterations; i++) {
            pivot1 = matrixFastmap.getMaximumDistance(pivot2, currentDimension, X);
            pivot2 = matrixFastmap.getMaximumDistance(pivot1, currentDimension, X);
        }
        PA[1][k - 1] = pivot2;
        PA[0][k - 1] = pivot1;
        // float distance = matrixFastmap.getDistance(pivot1, pivot2, currentDimension, X[pivot1], X[pivot2]);
        // System.out.println("Pivots: " + PA[0][k - 1] + ", " + PA[1][k - 1] + " with distance " + distance + " at k=" + k + " and currentDimension=" + currentDimension);
    }

    public int getIndexOfObject(Object o) {
        return matrixFastmap.getIndexOfObject(o);
    }

    /**
     * The found pivots for later use.
     *
     * @return the pivots.
     */
    public int[][] getPivots() {
        return PA;
    }
}
