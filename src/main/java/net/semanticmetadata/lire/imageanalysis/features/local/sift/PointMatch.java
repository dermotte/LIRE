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
package net.semanticmetadata.lire.imageanalysis.features.local.sift;

import java.util.ArrayList;
import java.util.Collection;

public class PointMatch {
    final private Point p1;

    final public Point getP1() {
        return p1;
    }

    final private Point p2;

    final public Point getP2() {
        return p2;
    }

    private float weight;

    final public float getWeight() {
        return weight;
    }

    final public void setWeight(float weight) {
        this.weight = weight;
    }

    private float distance;

    final public float getDistance() {
        return distance;
    }

    public PointMatch(
            Point p1,
            Point p2,
            float weight) {
        this.p1 = p1;
        this.p2 = p2;

        this.weight = weight;

        distance = Point.distance(p1, p2);
    }

    public PointMatch(
            Point p1,
            Point p2) {
        this.p1 = p1;
        this.p2 = p2;

        weight = 1.0f;

        distance = Point.distance(p1, p2);
    }

    /**
     * apply a model to p1, update distance
     *
     * @param model
     */
    final public void apply(Model model) {
        p1.apply(model);
        distance = Point.distance(p1, p2);
    }

    /**
     * flip symmetrically, weight remains unchanged
     *
     * @param matches
     * @return
     */
    final public static ArrayList<PointMatch> flip(Collection<PointMatch> matches) {
        ArrayList<PointMatch> list = new ArrayList<PointMatch>();
        for (PointMatch match : matches) {
            list.add(
                    new PointMatch(
                            match.p2,
                            match.p1,
                            match.weight));
        }
        return list;
    }

}
