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
package net.semanticmetadata.lire.indexing.fastmap;

/**
 * Date: 13.01.2005
 * Time: 22:37:22
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public abstract class DistanceCalculator {
    /**
     * Calculates and returns the distance between two objects. Please note that the
     * distance function has to be symmetric and must obey the triangle inequality.
     * This method is the same as {@link #getDistance(Object, Object, int, double[], double[])}
     * with a k=0.
     *
     * @param o1 Object 1 to compute
     * @param o2 Object 2 to compute
     * @return the distance as float from [0, infinite)  or -1 if objects distance cannot be computed
     */
    abstract public double getDistance(Object o1, Object o2);

    /**
     * Calculates and returns the distance between two objects. Please note that the
     * distance function has to be symmetric and must obey the triangle inequality.
     * distance in k is: d[k+1](o1,o2)^2 = d[k](o1,o2)^2 - (x1[k]-x2[k])^2 .
     *
     * @param o1 Object 1 to compute
     * @param o2 Object 2 to compute
     * @param k  defines the dimension of current fastmap operation
     * @param x1 is needed when k > 0 (see documentation above), all x1[l] with l &lt; k have to be present.
     * @param x2 is needed when k > 0 (see documentation above), all x2[l] with l &lt; k have to be present.
     * @return the distance as float from [0, infinite)  or -1 if objects distance cannot be computes
     */
    public double getDistance(Object o1, Object o2, int k, double[] x1, double[] x2) {
        double originalDistance = getDistance(o1, o2);
        if (k == 0) {
            return originalDistance;
        } else {
            double distance = originalDistance * originalDistance;
            for (int i = 0; i < k; i++) {
                double xDifference = x1[i] - x2[i];
                distance = distance - xDifference * xDifference;
            }
            return Math.sqrt(distance);
        }
    }
}
