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

// import ij.IJ;

import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class TRModel2D extends Model {

    static final public int MIN_SET_SIZE = 2;

    final private AffineTransform affine = new AffineTransform();

    public AffineTransform getAffine() {
        return affine;
    }

    @Override
    public float[] apply(float[] point) {
        float[] transformed = new float[2];
        affine.transform(point, 0, transformed, 0, 1);
        return transformed;
    }

    @Override
    public void applyInPlace(float[] point) {
        affine.transform(point, 0, point, 0, 1);
    }

    @Override
    public float[] applyInverse(float[] point) {
        // the brilliant java.awt.geom.AffineTransform implements transform for float[] but inverseTransform for double[] only...
        double[] double_point = new double[]{point[0], point[1]};
        double[] transformed = new double[2];
        try {
            affine.inverseTransform(double_point, 0, transformed, 0, 1);
        } catch (Exception e) {
            System.err.println("Noninvertible transformation.");
        }
        return new float[]{(float) transformed[0], (float) transformed[1]};
    }

    @Override
    public void applyInverseInPlace(float[] point) {
        float[] temp_point = applyInverse(point);
        point[0] = temp_point[0];
        point[1] = temp_point[1];
    }

    @Override
    public boolean fit(PointMatch[] min_matches) {
        PointMatch m1 = min_matches[0];
        PointMatch m2 = min_matches[1];

        float[] m1_p1 = m1.getP1().getL();
        float[] m2_p1 = m2.getP1().getL();
        float[] m1_p2 = m1.getP2().getW();
        float[] m2_p2 = m2.getP2().getW();

        float x1 = m2_p1[0] - m1_p1[0];
        float y1 = m2_p1[1] - m1_p1[1];
        float x2 = m2_p2[0] - m1_p2[0];
        float y2 = m2_p2[1] - m1_p2[1];
        float l1 = (float) Math.sqrt(x1 * x1 + y1 * y1);
        float l2 = (float) Math.sqrt(x2 * x2 + y2 * y2);

        x1 /= l1;
        x2 /= l2;
        y1 /= l1;
        y2 /= l2;

        //! unrotate (x2,y2)^T to (x1,y1)^T = (1,0)^T getting the sinus and cosinus of the rotation angle
        float cos = x1 * x2 + y1 * y2;
        float sin = x1 * y2 - y1 * x2;

        //m.alpha = atan2( y, x );

        float tx = m1_p2[0] - cos * m1_p1[0] + sin * m1_p1[1];
        float ty = m1_p2[1] - sin * m1_p1[0] - cos * m1_p1[1];
        affine.setTransform(cos, sin, -sin, cos, tx, ty);

//		System.out.println( this );

        return true;
    }

    @Override
    public String toString() {
        return ("[3,3](" + affine + ") " + error);
    }

    public void minimize(Collection<PointMatch> matches) {
        // center of mass:
        float xo1 = 0, yo1 = 0;
        float xo2 = 0, yo2 = 0;
        // Implementing Johannes Schindelin's squared error minimization formula
        // tan(angle) = Sum(x1*y1 + x2y2) / Sum(x1*y2 - x2*y1)
        int length = matches.size();
        // 1 - compute centers of mass, for displacement and origin of rotation

        if (0 == length) return;

        for (PointMatch m : matches) {
            float[] m_p1 = m.getP1().getL();
            float[] m_p2 = m.getP2().getW();

            xo1 += m_p1[0];
            yo1 += m_p1[1];
            xo2 += m_p2[0];
            yo2 += m_p2[1];
        }
        xo1 /= length;
        yo1 /= length;
        xo2 /= length;
        yo2 /= length;

        float dx = xo1 - xo2; // reversed, because the second will be moved relative to the first
        float dy = yo1 - yo2;
        float sum1 = 0, sum2 = 0;
        float x1, y1, x2, y2;
        for (PointMatch m : matches) {
            float[] m_p1 = m.getP1().getL();
            float[] m_p2 = m.getP2().getW();

            // make points local to the center of mass of the first landmark set
            x1 = m_p1[0] - xo1; // x1
            y1 = m_p1[1] - yo1; // x2
            x2 = m_p2[0] - xo2 + dx; // y1
            y2 = m_p2[1] - yo2 + dy; // y2
            sum1 += x1 * y2 - y1 * x2; //   x1 * y2 - x2 * y1 // assuming p1 is x1,x2, and p2 is y1,y2
            sum2 += x1 * x2 + y1 * y2; //   x1 * y1 + x2 * y2
        }
        float angle = (float) Math.atan2(-sum1, sum2);

        affine.setToIdentity();
        affine.rotate(-angle, xo2, yo2);
        affine.translate(-dx, -dy);
    }

    /**
     * change the model a bit
     * <p/>
     * estimates the necessary amount of shaking for each single dimensional
     * distance in the set of matches
     *
     * @param matches point matches
     * @param scale   gives a multiplicative factor to each dimensional distance (increases the amount of shaking)
     * @param center  local pivot point
     */
    final public void shake(
            Collection<PointMatch> matches,
            float scale,
            float[] center) {
        double xd = 0.0;
        double yd = 0.0;
        double rd = 0.0;

        int num_matches = matches.size();
        if (num_matches > 0) {
            for (PointMatch m : matches) {
                float[] m_p1 = m.getP1().getW();
                float[] m_p2 = m.getP2().getW();

                xd += Math.abs(m_p1[0] - m_p2[0]);
                ;
                yd += Math.abs(m_p1[1] - m_p2[1]);
                ;

                // shift relative to the center
                float x1 = m_p1[0] - center[0];
                float y1 = m_p1[1] - center[1];
                float x2 = m_p2[0] - center[0];
                float y2 = m_p2[1] - center[1];

                float l1 = (float) Math.sqrt(x1 * x1 + y1 * y1);
                float l2 = (float) Math.sqrt(x2 * x2 + y2 * y2);

                x1 /= l1;
                x2 /= l2;
                y1 /= l1;
                y2 /= l2;

                //! unrotate (x1,y1)^T to (x2,y2)^T = (1,0)^T getting the sinus and cosinus of the rotation angle
                float cos = x1 * x2 + y1 * y2;
                float sin = y1 * x2 - x1 * y2;

                rd += Math.abs(Math.atan2(sin, cos));
            }
            xd /= matches.size();
            yd /= matches.size();
            rd /= matches.size();

            //System.out.println( rd );
        }

        affine.rotate(rnd.nextGaussian() * (float) rd * scale, center[0], center[1]);
    }

    /**
     * estimate the transformation model for a set of feature correspondences
     * containing a high number of outliers using RANSAC
     *
     * @param candidates       set of correspondence candidates
     * @param inliers          set ot correspondences that fit the finally estimated model if any
     * @param iterations       number of iterations
     * @param epsilon          maximally allowed displacement
     * @param min_inlier_ratio minimal amount of inliers
     * @return TRModel2D or null
     *         <p/>
     *         <p/>
     *         Bibtex reference:
     *         <pre>
     * @article{FischlerB81, author            = {Martin A. Fischler and Robert C. Bolles},
     * title			= {Random sample consensus: a paradigm for model fitting with applications to image analysis and automated cartography},
     * journal			= {Communications of the ACM},
     * volume			= {24},
     * number			= {6},
     * year			= {1981},
     * pages			= {381--395},
     * publisher		= {ACM Press},
     * address			= {New York, NY, USA},
     * issn			= {0001-0782},
     * doi				= {http://doi.acm.org/10.1145/358669.358692},
     * }
     * </pre>
     */
    static public TRModel2D estimateModel(
            List<PointMatch> candidates,
            Collection<PointMatch> inliers,
            int iterations,
            float epsilon,
            float min_inlier_ratio) {
        inliers.clear();

        if (candidates.size() < MIN_SET_SIZE) {
            System.err.println(candidates.size() + " correspondence candidates are not enough to estimate a model, at least " + TRModel2D.MIN_SET_SIZE + " required.");
            return null;
        }

        TRModel2D model = new TRModel2D();        //!< the final model to be estimated

        int i = 0;
        while (i < iterations) {
            // choose T::MIN_SET_SIZE disjunctive matches randomly
            PointMatch[] min_matches = new PointMatch[MIN_SET_SIZE];
            int[] keys = new int[MIN_SET_SIZE];

            for (int j = 0; j < MIN_SET_SIZE; ++j) {
                int key;
                boolean in_set = false;
                do {
                    key = (int) (rnd.nextDouble() * candidates.size());
                    in_set = false;

                    // check if this key exists already
                    for (int k = 0; k < j; ++k) {
                        if (key == keys[k]) {
                            in_set = true;
                            break;
                        }
                    }
                }
                while (in_set);
                keys[j] = key;
                min_matches[j] = candidates.get(key);
            }

            TRModel2D m = new TRModel2D();
            final ArrayList<PointMatch> temp_inliers = new ArrayList<PointMatch>();
            m.fit(min_matches);
            int num_inliers = 0;
            boolean is_good = m.test(candidates, temp_inliers, epsilon, min_inlier_ratio);
            while (is_good && num_inliers < temp_inliers.size()) {
                num_inliers = temp_inliers.size();
                m.minimize(temp_inliers);
                is_good = m.test(candidates, temp_inliers, epsilon, min_inlier_ratio);
            }
            if (
                    is_good &&
                            m.betterThan(model) &&
                            temp_inliers.size() >= 3 * MIN_SET_SIZE) // now at least 6 matches required
            {
                model = m.clone();
                inliers.clear();
                inliers.addAll(temp_inliers);
            }
            ++i;
        }
        if (inliers.size() == 0)
            return null;

        return model;
    }

    /**
     * estimate the transformation model for a set of feature correspondences
     * containing a high number of outliers using RANSAC
     * <p/>
     * increase the error as long as not more inliers occur
     */
    static public TRModel2D estimateBestModel(
            List<PointMatch> candidates,
            Collection<PointMatch> inliers,
            float min_epsilon,
            float max_epsilon,
            float min_inlier_ratio) {
        inliers.clear();
        TRModel2D model = null;
        float epsilon = 0.0f;
        if (candidates.size() > MIN_SET_SIZE) {
            int highest_num_inliers = 0;
            int convergence_count = 0;
            TRModel2D m = null;
            do {
                final ArrayList<PointMatch> temp_inliers = new ArrayList<PointMatch>();
                epsilon += min_epsilon;
                // 1000 iterations lead to a probability of < 0.01% that only bad data values were found
                m = estimateModel(
                        candidates,                    //!< point correspondence candidates
                        temp_inliers,
                        1000,                        //!< iterations
                        epsilon,                    //!< maximal alignment error for a good point pair when fitting the model
                        min_inlier_ratio);                //!< minimal partition (of 1.0) of inliers

                if (m != null) {
                    int num_inliers = temp_inliers.size();
                    if (num_inliers <= highest_num_inliers) {
                        ++convergence_count;
                    } else {
                        model = m.clone();
                        inliers.clear();
                        inliers.addAll(temp_inliers);
                        convergence_count = 0;
                        highest_num_inliers = num_inliers;
                    }
                }
            }
            while ((m == null || convergence_count < 4) && epsilon < max_epsilon);
        }
        if (model == null) {
            // IJ.log( "No model found." );
        } else {
            // IJ.log( "Model with epsilon <= " + epsilon + " for " + inliers.size() + " inliers found." );
            // IJ.log( "  Affine transform: " + model.getAffine().toString() );
        }

        return model;
    }


    public TRModel2D clone() {
        TRModel2D trm = new TRModel2D();
        trm.affine.setTransform(affine);
        trm.error = error;
        return trm;
    }

    public void preConcatenate(TRModel2D model) {
        this.affine.preConcatenate(model.affine);
    }

    public void concatenate(TRModel2D model) {
        this.affine.concatenate(model.affine);
    }
}
