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
package net.semanticmetadata.lire.matrix;

/**
 * Date: 07.02.2005
 * Time: 22:10:54
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface SimilarityMatrix {
    /**
     * Calculates the distance between objects using the distance function for k = 0,
     * using . If it has not
     * been computed previously it is computed and stored now.
     *
     * @param o1 Object 1 to compute
     * @param o2 Object 2 to compute
     * @return the distance as float from [0, infinite)
     */
    public float getSimilarity(Object o1, Object o2);

    /**
     * Calculates the Similarity between objects using the Similarity function for k = 0,
     * using . If it has not
     * been computed previously it is computed and stored now.
     *
     * @param index1 index of first object to compute
     * @param index2 index of second object to compute
     * @return the Similarity as float from [0, infinite)
     */
    public float getSimilarity(int index1, int index2);

    /**
     * Used for the heuristic for getting the pivots as described in the paper. This method calls
     * with parameters (row, 0, null, null).
     *
     * @param row defines the row where we want to find the maximum
     * @return the index of the object with maximum Similarity to the row object.
     */
    public int getMaximumSimilarity(int row);

    /**
     * Returns the index of the col with minimum similarity to the given row
     *
     * @param row defines the row where we want to find the minimum
     * @return the index of the object with minimum Similarity to the row object.
     */
    public int getMinimumSimilarity(int row);

    /**
     * Returns the dimension of the matrix
     *
     * @return dimension, which is > 0
     */
    public int getDimension();

    /**
     * Returns the user object for given index number
     *
     * @param rowNumber
     * @return
     * @see #getIndexOfObject(Object)
     */
    public Object getUserObject(int rowNumber);

    /**
     * Returns the index in the matrix of the given user object or -1 if not found
     *
     * @param o the object to search for
     * @return the index number of the object or -1 if not found
     * @see #getUserObject(int)
     */
    public int getIndexOfObject(Object o);

    public DistanceMatrix getDistanceMatrix();
}