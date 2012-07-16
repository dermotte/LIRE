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
package net.semanticmetadata.lire.imageanalysis.sift;

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
