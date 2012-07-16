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


import net.semanticmetadata.lire.imageanalysis.LireFeature;

/**
 * Date: 13.01.2005
 * Time: 22:47:01
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FeatureDistanceCalculator extends DistanceCalculator {

    /**
     * Allows the distance calculations based on visual descriptors.
     *
     * @param o1
     * @param o2
     * @return
     */
    public double getDistance(Object o1, Object o2) {
        if (o1 instanceof LireFeature && o2 instanceof LireFeature) {
            LireFeature c1 = (LireFeature) o1;
            LireFeature c2 = (LireFeature) o2;
            return c1.getDistance(c2);
        } else {
            return -1d;
        }
    }
}
