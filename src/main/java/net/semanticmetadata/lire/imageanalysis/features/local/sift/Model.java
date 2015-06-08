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

import java.awt.geom.AffineTransform;
import java.util.Collection;
import java.util.Random;

/**
 * Abstract class for arbitrary geometric transformation models to be applied
 * to points in n-dimensional space.
 * <p/>
 * Provides methods for generic optimization and model extraction algorithms.
 * Currently, RANSAC and Monte-Carlo minimization implemented.  Needs revision...
 * <p/>
 * TODO A model is planned to be a generic transformation pipeline to be
 * applied to images, volumes or arbitrary sets of n-dimensional points.  E.g.
 * lens transformation of camera images, pose and location of mosaic tiles,
 * non-rigid bending of confocal stacks etc.
 * <p/>
 * License: GPL
 * <p/>
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 * <p/>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p/>
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 * <p/>
 * NOTE:
 * The SIFT-method is protected by U.S. Patent 6,711,293: "Method and
 * apparatus for identifying scale invariant features in an image and use of
 * same for locating an object in an image" by the University of British
 * Columbia.  That is, for commercial applications the permission of the author
 * is required.
 *
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 * @version 0.1b
 */
abstract public class Model {

    // minimal number of point correspondences required to solve the model
    static final public int MIN_SET_SIZE = 0;

    // real random
    //final Random random = new Random( System.currentTimeMillis() );
    // repeatable results
    final static Random rnd = new Random(69997);

    /**
     * error depends on what kind of algorithm is running
     * small error is better than large error
     */
    public double error;

    /**
     * instantiates an empty model with maximally large error
     */
    public Model() {
        error = Double.MAX_VALUE;
    }

    /**
     * fit the model to a minimal set of point correpondences
     * estimates a model to transform match.p2.local to match.p1.world
     *
     * @param min_matches minimal set of point correpondences
     * @return true if a model was estimated
     */
    public abstract boolean fit(PointMatch[] min_matches);

    /**
     * apply the model to a point location
     *
     * @param point
     * @return transformed point
     */
    public abstract float[] apply(float[] point);

    /**
     * apply the model to a point location
     *
     * @param point
     */
    public abstract void applyInPlace(float[] point);

    /**
     * apply the inverse of the model to a point location
     *
     * @param point
     * @return transformed point
     */
    public abstract float[] applyInverse(float[] point);

    /**
     * apply the inverse of the model to a point location
     *
     * @param point
     */
    public abstract void applyInverseInPlace(float[] point);

    /**
     * test the model for a set of point correspondence candidates
     * <p/>
     * clears inliers and fills it with the fitting subset of candidates
     *
     * @param candidates  set of point correspondence candidates
     * @param inliers     set of point correspondences that fit the model
     * @param epsilon     maximal allowed transfer error
     * @param min_inliers minimal ratio of inliers (0.0 => 0%, 1.0 => 100%)
     */
    public boolean test(
            Collection<PointMatch> candidates,
            Collection<PointMatch> inliers,
            double epsilon,
            double min_inlier_ratio) {
        inliers.clear();

        for (PointMatch m : candidates) {
            m.apply(this);
            if (m.getDistance() < epsilon) inliers.add(m);
        }

        float ir = (float) inliers.size() / (float) candidates.size();
        error = 1.0 - ir;
        if (error > 1.0)
            error = 1.0;
        if (error < 0)
            error = 0.0;

        return (ir > min_inlier_ratio);
    }

    /**
     * less than operater to make the models comparable, returns false for error < 0
     *
     * @param m
     * @return false for error < 0, otherwise true if this.error is smaller than m.error
     */
    public boolean betterThan(Model m) {
        if (error < 0) return false;
        return error < m.error;
    }

    /**
     * randomly change the model a bit
     * <p/>
     * estimates the necessary amount of shaking for each single dimensional
     * distance in the set of matches
     *
     * @param matches point matches
     * @param scale   gives a multiplicative factor to each dimensional distance (scales the amount of shaking)
     * @param center  local pivot point for centered shakes (e.g. rotation)
     */
    abstract public void shake(
            Collection<PointMatch> matches,
            float scale,
            float[] center);

    abstract public void minimize(Collection<PointMatch> matches);

    abstract public AffineTransform getAffine();

    /**
     * string to output stream
     */
    abstract public String toString();

    /**
     * clone
     */
    abstract public Model clone();
};
