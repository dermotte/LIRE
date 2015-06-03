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

import net.semanticmetadata.lire.matrix.SimilarityMatrix;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * Simulated distance matrix. No internal object is used to create the matrix, but the
 * distance between objects is computed online. Suitable for use with FastMap, but not
 * with HAC, K-Means or alike.
 * Date: 16.09.2008
 * Time: 12:56:13
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class NocacheFastmapDistanceMatrix implements FastmapDistanceMatrix {
    //    private double[][] distance;
    //    protected HashMap<Integer, HashMap> distance;
    private ArrayList<?> objects;
    private HashMap<Object, Integer> objects2position;
    private DistanceCalculator distanceFct;
    private int dimension;
    private boolean distributeObjects = false;

    /**
     * Creates a new distance matrix. Please note that the distance matrix uses storage in quadratic size of
     * the user object count. The DistanceCalculator has to be able to work on those userObjects.
     *
     * @param userObjects      gives the collection of object to be processed
     * @param distanceFunction allows the distance calculation  or -1 if objects distance cannot be computes, has to be a metric
     */
    @SuppressWarnings("unchecked")
    public NocacheFastmapDistanceMatrix(List userObjects, DistanceCalculator distanceFunction) {
        init(distanceFunction, userObjects);
    }

    /**
     * Creates a new distance matrix. Please note that the distance matrix uses storage in quadratic size of
     * the user object count. The DistanceCalculator has to be able to work on those userObjects.
     *
     * @param userObjects      gives the collection of object to be processed
     * @param distanceFunction allows the distance calculation  or -1 if objects distance cannot be computes, has to be a metric
     * @param userObjects      select true if you want to distribute not equal but zero distance objects.
     */
    @SuppressWarnings("unchecked")
    public NocacheFastmapDistanceMatrix(List userObjects, DistanceCalculator distanceFunction, boolean distributeObjects) {
        init(distanceFunction, userObjects);
        this.distributeObjects = distributeObjects;
    }

    @SuppressWarnings("unchecked")
    private void init(DistanceCalculator distanceFunction, List userObjects) {
        distanceFct = distanceFunction;
        // this might be a problem for collections > INT_MAXSIZE
        this.objects = new ArrayList(userObjects.size());
        this.objects.addAll(userObjects);
        dimension = objects.size();
        // init HashMaps ...
        objects2position = new HashMap<Object, Integer>(dimension);
        int count = 0;
        for (Iterator iterator = objects.iterator(); iterator.hasNext(); ) {
            Object o = iterator.next();
            objects2position.put(o, count);
            count++;
        }
    }

    /**
     * Calculates the distance between objects using the distance function for k = 0,
     * using {@link DistanceCalculator#getDistance(Object, Object)}. If it has not
     * been computed previously it is computed and stored now.
     *
     * @param o1 Object 1 to compute
     * @param o2 Object 2 to compute
     * @return the distance as float from [0, infinite)
     */
    public double getDistance(Object o1, Object o2) {
        int num1, num2;
        num1 = objects2position.get(o1);
        num2 = objects2position.get(o2);
        return getDistance(num1, num2);
    }

    /**
     * Calculates the distance between objects using the distance function for k = 0,
     * using {@link DistanceCalculator#getDistance(Object, Object)}. If it has not
     * been computed previously it is computed and stored now.
     *
     * @param index1 index of first object to compute
     * @param index2 index of second object to compute
     * @return the distance as float from [0, infinite)
     */
    public double getDistance(int index1, int index2) {
        int tmp;

        // well that's easy ...
        if (index1 == index2) return 0f;

        // switch ...
        if (index1 > index2) {
            tmp = index1;
            index1 = index2;
            index2 = tmp;
        }

        // compute if not already there ...
        double distance = distanceFct.getDistance(objects.get(index1), objects.get(index2));
        if (distributeObjects && distance == 0) {
            distance = 0.2f;
        }

        return distance;
    }

    /**
     * Calculates and returns the distance between two objects. Please note that the
     * distance function has to be symmetric and must obey the triangle inequality.
     * distance in k is: d[k+1](o1,o2)^2 = d[k](o1,o2)^2 - (x1[k]-x2[k])^2 .
     *
     * @param index1 index of first object to compute
     * @param index2 index of second object to compute
     * @param k      defines the dimension of current fastmap operation
     * @param x1     is needed when k > 0 (see documentation above), all x1[l] with l &lt; k have to be present.
     * @param x2     is needed when k > 0 (see documentation above), all x2[l] with l &lt; k have to be present.
     * @return the distance as float from [0, infinite)
     */
    public double getDistance(int index1, int index2, int k, double[] x1, double[] x2) {
        // kind of speed up ...
        if (index1 == index2) return 0f;

        double originalDistance = getDistance(index1, index2);
        if (k == 0) {
            return originalDistance;
        } else {
            double distance = originalDistance * originalDistance;
            for (int i = 0; i < k; i++) {
                double xDifference = x1[i] - x2[i];
                distance = distance - xDifference * xDifference;
            }
            // fixed based on the comments of Benjamin Sznajder & Michal Shmueli-Scheuer
            // Can get <0 according to http://www.cs.umd.edu/~hjs/pubs/hjaltasonpami03.pdf (section 2.2 )
            return (float) Math.sqrt(Math.abs(distance));
        }
    }

    /**
     * Used for the heuristic for getting the pivots as described in the paper.
     *
     * @param row    defines the row where we want to find the maximum
     * @param k      defines the dimension of current fastmap operation
     * @param points is needed when k > 0 (see documentation above), all x1[l] with l &lt; k have to be present.
     * @return the index of the object with maximum distance to the row object.
     */
    public int getMaximumDistance(int row, int k, double[][] points) {
        double max = 0f;
        int result = 0;
        for (int i = 0; i < dimension; i++) {
            double[] point1 = null;
            double[] point2 = null;
            if (points != null) {
                point1 = points[row];
                point2 = points[i];
            }
            double currentDistance = getDistance(row, i, k, point1, point2);
            if (currentDistance > max) {
                max = currentDistance;
                result = i;
            }
        }
        return result;
    }

    /**
     * Used for the heuristic for getting the pivots as described in the paper. This method calls
     * {@link FastmapDistanceMatrix#getMaximumDistance(int, int, double[][])} with parameters (row, 0, null, null).
     *
     * @param row defines the row where we want to find the maximum
     * @return the index of the object with maximum distance to the row object.
     * @see FastmapDistanceMatrix#getMaximumDistance(int, int, double[][])
     */
    public int getMaximumDistance(int row) {
        return getMaximumDistance(row, 0, null);
    }

    public int getDimension() {
        return dimension;
    }

    /**
     * Returns the user object for given index number
     *
     * @param rowNumber
     * @return
     * @see #getIndexOfObject(Object)
     */
    public Object getUserObject(int rowNumber) {
        return objects.get(rowNumber);
    }

    /**
     * Returns the index in the matrix of the given user object or -1 if not found
     *
     * @param o the object to search for
     * @return the index number of the object or -1 if not found
     * @see #getUserObject(int)
     */
    public int getIndexOfObject(Object o) {
        Integer index = objects2position.get(o);
        if (index == null)
            return -1;
        else
            return index.intValue();
    }

    /**
     * Creates and returns a newly created similarity Matrix from the given
     * distance Matrix
     *
     * @return the similarityMatrix or null if not implemented or possible
     */
    public SimilarityMatrix getSimilarityMatrix() {
        return null;
    }

    /**
     * Normalizes the matrix for all values to [0,1]
     */
    public void normalize() {
        throw new UnsupportedOperationException("There is no internal data structure to normalize.");
    }

}
