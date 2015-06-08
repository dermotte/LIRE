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

/**
 * Scale Invariant Feature Transform as described by David Lowe \citep{Loew04}.
 *
 * BibTeX:
 * <pre>
 * &#64;article{Lowe04,
 *   author  = {David G. Lowe},
 *   title   = {Distinctive Image Features from Scale-Invariant Keypoints},
 *   journal = {International Journal of Computer Vision},
 *   year    = {2004},
 *   volume  = {60},
 *   number  = {2},
 *   pages   = {91--110},
 * }
 * </pre>
 *
 * License: GPL
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License 2
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
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

import java.util.List;
import java.util.Vector;

public class FloatArray2DSIFT {

    /**
     * number of orientation histograms per axis of the feature descriptor
     * square
     */
    private int FEATURE_DESCRIPTOR_SIZE;
    private int FEATURE_DESCRIPTOR_WIDTH;

    /**
     * number of bins per orientation histogram of the feature descriptor
     */
    private int FEATURE_DESCRIPTOR_ORIENTATION_BINS = 0;
    private float FEATURE_DESCRIPTOR_ORIENTATION_BIN_SIZE = 0;

    /**
     * evaluation mask for the feature descriptor square
     */
    private float[][] descriptorMask;


    /**
     * Returns the size in bytes of a Feature object.
     */
    public long getFeatureObjectSize() {
        return FloatArray2DSIFT.getFeatureObjectSize(FEATURE_DESCRIPTOR_SIZE, FEATURE_DESCRIPTOR_ORIENTATION_BINS);
    }

    static public long getFeatureObjectSize(final int fdsize, final int fdbins) {
        return 32 + 4 + 4 + 8 + fdsize * fdsize * fdbins * 4 + 32 + 32;
    }


    /**
     * octaved scale space
     */
    private FloatArray2DScaleOctave[] octaves;

    public FloatArray2DScaleOctave[] getOctaves() {
        return octaves;
    }

    public FloatArray2DScaleOctave getOctave(int i) {
        return octaves[i];
    }

    /**
     * Difference of Gaussian detector
     */
    private FloatArray2DScaleOctaveDoGDetector dog;

    /**
     * Constructor
     *
     * @param feature_descriptor_size
     * @param feature_descriptor_size
     */
    public FloatArray2DSIFT(
            int feature_descriptor_size,
            int feature_descriptor_orientation_bins) {
        octaves = null;
        dog = new FloatArray2DScaleOctaveDoGDetector();

        FEATURE_DESCRIPTOR_SIZE = feature_descriptor_size;
        FEATURE_DESCRIPTOR_WIDTH = 4 * FEATURE_DESCRIPTOR_SIZE;
        FEATURE_DESCRIPTOR_ORIENTATION_BINS = feature_descriptor_orientation_bins;
        FEATURE_DESCRIPTOR_ORIENTATION_BIN_SIZE = 2.0f * (float) Math.PI / (float) FEATURE_DESCRIPTOR_ORIENTATION_BINS;

        descriptorMask = new float[FEATURE_DESCRIPTOR_SIZE * 4][FEATURE_DESCRIPTOR_SIZE * 4];

        float two_sq_sigma = FEATURE_DESCRIPTOR_SIZE * FEATURE_DESCRIPTOR_SIZE * 8;
        for (int y = FEATURE_DESCRIPTOR_SIZE * 2 - 1; y >= 0; --y) {
            float fy = (float) y + 0.5f;
            for (int x = FEATURE_DESCRIPTOR_SIZE * 2 - 1; x >= 0; --x) {
                float fx = (float) x + 0.5f;
                float val = (float) Math.exp(-(fy * fy + fx * fx) / two_sq_sigma);
                descriptorMask[2 * FEATURE_DESCRIPTOR_SIZE - 1 - y][2 * FEATURE_DESCRIPTOR_SIZE - 1 - x] = val;
                descriptorMask[2 * FEATURE_DESCRIPTOR_SIZE + y][2 * FEATURE_DESCRIPTOR_SIZE - 1 - x] = val;
                descriptorMask[2 * FEATURE_DESCRIPTOR_SIZE - 1 - y][2 * FEATURE_DESCRIPTOR_SIZE + x] = val;
                descriptorMask[2 * FEATURE_DESCRIPTOR_SIZE + y][2 * FEATURE_DESCRIPTOR_SIZE + x] = val;
            }
        }
    }

    /**
     * initialize the scale space as a scale pyramid having octave stubs only
     *
     * @param src           image having a generating gaussian kernel of initial_sigma
     *                      img must be a 2d-array of float values in range [0.0f, ..., 1.0f]
     * @param steps         gaussian smooth steps steps per scale octave
     * @param initial_sigma sigma of the generating gaussian kernel of img
     * @param min_size      minimal size of a scale octave in pixel
     * @param max_size      maximal size of an octave to be taken into account
     *                      Use this to save memory and procesing time, if processing higher
     *                      resolutions is not necessary.
     */
    public void init(
            FloatArray2D src,
            int steps,
            float initial_sigma,
            int min_size,
            int max_size) {
        // estimate the number of octaves needed using a simple while loop instead of ld
        int o = 0;
        float w = (float) src.width;
        float h = (float) src.height;
        while (w > (float) min_size && h > (float) min_size) {
            w /= 2.0f;
            h /= 2.0f;
            ++o;
        }
        octaves = new FloatArray2DScaleOctave[o];

        float[] sigma = new float[steps + 3];
        sigma[0] = initial_sigma;
        float[] sigma_diff = new float[steps + 3];
        sigma_diff[0] = 0.0f;
        float[][] kernel_diff = new float[steps + 3][];

        //System.out.println( "sigma[0] = " + sigma[ 0 ] + "; sigma_diff[0] = " + sigma_diff[ 0 ] );

        for (int i = 1; i < steps + 3; ++i) {
            sigma[i] = initial_sigma * (float) Math.pow(2.0f, (float) i / (float) steps);
            sigma_diff[i] = (float) Math.sqrt(sigma[i] * sigma[i] - initial_sigma * initial_sigma);

            //System.out.println( "sigma[" + i + "] = " + sigma[ i ] + "; sigma_diff[" + i + "] = " + sigma_diff[ i ] );

            kernel_diff[i] = Filter.createGaussianKernel1D(sigma_diff[i], true);
        }

        FloatArray2D next;

        for (int i = 0; i < octaves.length; ++i) {
            octaves[i] = new FloatArray2DScaleOctave(
                    src,
                    sigma,
                    sigma_diff,
                    kernel_diff);
            octaves[i].buildStub();
            next = new FloatArray2D(
                    src.width / 2 + src.width % 2,
                    src.height / 2 + src.height % 2);
            FloatArray2DScaleOctave.downsample(octaves[i].getL(1), next);
            if (src.width > max_size || src.height > max_size)
                octaves[i].clear();
            src = next;
        }
    }

    // TODO this is for test
    //---------------------------------------------------------------------
    //public FloatArray2D pattern;


    /**
     * sample the scaled and rotated gradients in a region around the
     * features location, the regions size is defined by
     * ( FEATURE_DESCRIPTOR_SIZE * 4 )^2 ( 4x4 subregions )
     *
     * @param c            candidate 0=>x, 1=>y, 2=>scale index
     * @param o            octave index
     * @param octave_sigma sigma of the corresponding gaussian kernel with
     *                     respect to the scale octave
     * @param orientation  orientation [-&pi; ... &pi;]
     */
    private float[] createDescriptor(
            float[] c,
            int o,
            float octave_sigma,
            float orientation) {
        FloatArray2DScaleOctave octave = octaves[o];
        FloatArray2D[] gradients = octave.getL1(Math.round(c[2]));
        FloatArray2D[] region = new FloatArray2D[2];

        region[0] = new FloatArray2D(
                FEATURE_DESCRIPTOR_WIDTH,
                FEATURE_DESCRIPTOR_WIDTH);
        region[1] = new FloatArray2D(
                FEATURE_DESCRIPTOR_WIDTH,
                FEATURE_DESCRIPTOR_WIDTH);
        float cos_o = (float) Math.cos(orientation);
        float sin_o = (float) Math.sin(orientation);

        // TODO this is for test
        //---------------------------------------------------------------------
        //FloatArray2D image = octave.getL( Math.round( c[ 2 ] ) );
        //pattern = new FloatArray2D( FEATURE_DESCRIPTOR_WIDTH, FEATURE_DESCRIPTOR_WIDTH );

        //! sample the region arround the keypoint location
        for (int y = FEATURE_DESCRIPTOR_WIDTH - 1; y >= 0; --y) {
            float ys =
                    ((float) y - 2.0f * (float) FEATURE_DESCRIPTOR_SIZE + 0.5f) * octave_sigma; //!< scale y around 0,0
            for (int x = FEATURE_DESCRIPTOR_WIDTH - 1; x >= 0; --x) {
                float xs =
                        ((float) x - 2.0f * (float) FEATURE_DESCRIPTOR_SIZE + 0.5f) * octave_sigma; //!< scale x around 0,0
                float yr = cos_o * ys + sin_o * xs; //!< rotate y around 0,0
                float xr = cos_o * xs - sin_o * ys; //!< rotate x around 0,0

                // flip_range at borders
                // TODO for now, the gradients orientations do not flip outside
                // the image even though they should do it. But would this
                // improve the result?

                // translate ys to sample y position in the gradient image
                int yg = Filter.flipInRange(
                        (int) (Math.round(yr + c[1])),
                        gradients[0].height);

                // translate xs to sample x position in the gradient image
                int xg = Filter.flipInRange(
                        (int) (Math.round(xr + c[0])),
                        gradients[0].width);

                // get the samples
                int region_p = FEATURE_DESCRIPTOR_WIDTH * y + x;
                int gradient_p = gradients[0].width * yg + xg;

                // weigh the gradients
                region[0].data[region_p] = gradients[0].data[gradient_p] * descriptorMask[y][x];

                // rotate the gradients orientation it with respect to the features orientation
                region[1].data[region_p] = gradients[1].data[gradient_p] - orientation;

                // TODO this is for test
                //---------------------------------------------------------------------
                //pattern.data[ region_p ] = image.data[ gradient_p ];
            }
        }


        float[][][] hist = new float[FEATURE_DESCRIPTOR_SIZE][FEATURE_DESCRIPTOR_SIZE][FEATURE_DESCRIPTOR_ORIENTATION_BINS];

        // build the orientation histograms of 4x4 subregions
        for (int y = FEATURE_DESCRIPTOR_SIZE - 1; y >= 0; --y) {
            int yp = FEATURE_DESCRIPTOR_SIZE * 16 * y;
            for (int x = FEATURE_DESCRIPTOR_SIZE - 1; x >= 0; --x) {
                int xp = 4 * x;
                for (int ysr = 3; ysr >= 0; --ysr) {
                    int ysrp = 4 * FEATURE_DESCRIPTOR_SIZE * ysr;
                    for (int xsr = 3; xsr >= 0; --xsr) {
                        float bin_location = (region[1].data[yp + xp + ysrp + xsr] + (float) Math.PI) / (float) FEATURE_DESCRIPTOR_ORIENTATION_BIN_SIZE;

                        int bin_b = (int) (bin_location);
                        int bin_t = bin_b + 1;
                        float d = bin_location - (float) bin_b;

                        bin_b = (bin_b + 2 * FEATURE_DESCRIPTOR_ORIENTATION_BINS) % FEATURE_DESCRIPTOR_ORIENTATION_BINS;
                        bin_t = (bin_t + 2 * FEATURE_DESCRIPTOR_ORIENTATION_BINS) % FEATURE_DESCRIPTOR_ORIENTATION_BINS;

                        float t = region[0].data[yp + xp + ysrp + xsr];

                        hist[y][x][bin_b] += t * (1 - d);
                        hist[y][x][bin_t] += t * d;
                    }
                }
            }
        }

        float[] desc = new float[FEATURE_DESCRIPTOR_SIZE * FEATURE_DESCRIPTOR_SIZE * FEATURE_DESCRIPTOR_ORIENTATION_BINS];

        // normalize, cut above 0.2 and renormalize
        float max_bin_val = 0;
        int i = 0;
        for (int y = FEATURE_DESCRIPTOR_SIZE - 1; y >= 0; --y) {
            for (int x = FEATURE_DESCRIPTOR_SIZE - 1; x >= 0; --x) {
                for (int b = FEATURE_DESCRIPTOR_ORIENTATION_BINS - 1; b >= 0; --b) {
                    desc[i] = hist[y][x][b];
                    if (desc[i] > max_bin_val) max_bin_val = desc[i];
                    ++i;
                }
            }
        }
        max_bin_val /= 0.2;
        for (i = 0; i < desc.length; ++i) {
            desc[i] = (float) Math.min(1.0, desc[i] / max_bin_val);
        }

        return desc;
    }

    /**
     * assign orientation to the given candidate, if more than one orientations
     * found, duplicate the feature for each orientation
     * <p/>
     * estimate the feature descriptor for each of those candidates
     *
     * @param c        candidate 0=>x, 1=>y, 2=>scale index
     * @param o        octave index
     * @param features finally contains all processed candidates
     */
    void processCandidate(
            float[] c,
            int o,
            Vector<SiftFeature> features) {
        final int ORIENTATION_BINS = 36;
        final float ORIENTATION_BIN_SIZE = 2.0f * (float) Math.PI / (float) ORIENTATION_BINS;
        float[] histogram_bins = new float[ORIENTATION_BINS];

        int scale = (int) Math.pow(2, o);

        FloatArray2DScaleOctave octave = octaves[o];

        float octave_sigma = octave.SIGMA[0] * (float) Math.pow(2.0f, c[2] / (float) octave.STEPS);

        // create a circular gaussian window with sigma 1.5 times that of the feature
        FloatArray2D gaussianMask =
                Filter.create_gaussian_kernel_2D_offset(
                        octave_sigma * 1.5f,
                        c[0] - (float) Math.floor(c[0]),
                        c[1] - (float) Math.floor(c[1]),
                        false);
        //FloatArrayToImagePlus( gaussianMask, "gaussianMask", 0, 0 ).show();

        // get the gradients in a region arround the keypoints location
        FloatArray2D[] src = octave.getL1(Math.round(c[2]));
        FloatArray2D[] gradientROI = new FloatArray2D[2];
        gradientROI[0] = new FloatArray2D(gaussianMask.width, gaussianMask.width);
        gradientROI[1] = new FloatArray2D(gaussianMask.width, gaussianMask.width);

        int half_size = gaussianMask.width / 2;
        int p = gaussianMask.width * gaussianMask.width - 1;
        for (int yi = gaussianMask.width - 1; yi >= 0; --yi) {
            int ra_y = src[0].width * Math.max(0, Math.min(src[0].height - 1, (int) c[1] + yi - half_size));
            int ra_x = ra_y + Math.min((int) c[0], src[0].width - 1);

            for (int xi = gaussianMask.width - 1; xi >= 0; --xi) {
                int pt = Math.max(ra_y, Math.min(ra_y + src[0].width - 2, ra_x + xi - half_size));
                gradientROI[0].data[p] = src[0].data[pt];
                gradientROI[1].data[p] = src[1].data[pt];
                --p;
            }
        }

        // and mask this region with the precalculated gaussion window
        for (int i = 0; i < gradientROI[0].data.length; ++i) {
            gradientROI[0].data[i] *= gaussianMask.data[i];
        }

        // TODO this is for test
        //---------------------------------------------------------------------
        //ImageArrayConverter.FloatArrayToImagePlus( gradientROI[ 0 ], "gaussianMaskedGradientROI", 0, 0 ).show();
        //ImageArrayConverter.FloatArrayToImagePlus( gradientROI[ 1 ], "gaussianMaskedGradientROI", 0, 0 ).show();

        // build an orientation histogram of the region
        for (int i = 0; i < gradientROI[0].data.length; ++i) {
            int bin = Math.max(0, (int) ((gradientROI[1].data[i] + Math.PI) / ORIENTATION_BIN_SIZE));
            histogram_bins[bin] += gradientROI[0].data[i];
        }

        // find the dominant orientation and interpolate it with respect to its two neighbours
        int max_i = 0;
        for (int i = 0; i < ORIENTATION_BINS; ++i) {
            if (histogram_bins[i] > histogram_bins[max_i]) max_i = i;
        }

        /**
         * interpolate orientation estimate the offset from center of the
         * parabolic extremum of the taylor series through env[1], derivatives
         * via central difference and laplace
         */
        float e0 = histogram_bins[(max_i + ORIENTATION_BINS - 1) % ORIENTATION_BINS];
        float e1 = histogram_bins[max_i];
        float e2 = histogram_bins[(max_i + 1) % ORIENTATION_BINS];
        float offset = (e0 - e2) / 2.0f / (e0 - 2.0f * e1 + e2);
        float orientation = ((float) max_i + offset) * ORIENTATION_BIN_SIZE - (float) Math.PI;

        // assign descriptor and add the Feature instance to the collection
        features.addElement(
                new SiftFeature(
                        octave_sigma * scale,
                        orientation,
                        new float[]{c[0] * scale, c[1] * scale},
                        //new float[]{ ( c[ 0 ] + 0.5f ) * scale - 0.5f, ( c[ 1 ] + 0.5f ) * scale - 0.5f },
                        createDescriptor(c, o, octave_sigma, orientation)));

        // TODO this is for test
        //---------------------------------------------------------------------
        //ImageArrayConverter.FloatArrayToImagePlus( pattern, "test", 0f, 1.0f ).show();

        /**
         * check if there is another significant orientation ( > 80% max )
         * if there is one, duplicate the feature and
         */
        for (int i = 0; i < ORIENTATION_BINS; ++i) {
            if (
                    i != max_i &&
                            (max_i + 1) % ORIENTATION_BINS != i &&
                            (max_i - 1 + ORIENTATION_BINS) % ORIENTATION_BINS != i &&
                            histogram_bins[i] > 0.8 * histogram_bins[max_i]) {
                /**
                 * interpolate orientation estimate the offset from center of
                 * the parabolic extremum of the taylor series through env[1],
                 * derivatives via central difference and laplace
                 */
                e0 = histogram_bins[(i + ORIENTATION_BINS - 1) % ORIENTATION_BINS];
                e1 = histogram_bins[i];
                e2 = histogram_bins[(i + 1) % ORIENTATION_BINS];

                if (e0 < e1 && e2 < e1) {
                    offset = (e0 - e2) / 2.0f / (e0 - 2.0f * e1 + e2);
                    orientation = ((float) i + 0.5f + offset) * ORIENTATION_BIN_SIZE - (float) Math.PI;

                    features.addElement(
                            new SiftFeature(
                                    octave_sigma * scale,
                                    orientation,
                                    new float[]{c[0] * scale, c[1] * scale},
                                    //new float[]{ ( c[ 0 ] + 0.5f ) * scale - 0.5f, ( c[ 1 ] + 0.5f ) * scale - 0.5f },
                                    createDescriptor(c, o, octave_sigma, orientation)));

                    // TODO this is for test
                    //---------------------------------------------------------------------
                    //ImageArrayConverter.FloatArrayToImagePlus( pattern, "test", 0f, 1.0f ).show();
                }
            }
        }
        return;
    }


    /**
     * detect features in the specified scale octave
     *
     * @param o octave index
     * @return detected features
     */
    public Vector<SiftFeature> runOctave(int o) {
        Vector<SiftFeature> features = new Vector<SiftFeature>();
        FloatArray2DScaleOctave octave = octaves[o];
        octave.build();
        dog.run(octave);
        Vector<float[]> candidates = dog.getCandidates();
        for (float[] c : candidates) {
            this.processCandidate(c, o, features);
        }
        //System.out.println( features.size() + " candidates processed in octave " + o );

        return features;
    }

    /**
     * detect features in all scale octaves
     *
     * @return detected features
     */
    public Vector<SiftFeature> run() {
        Vector<SiftFeature> features = new Vector<SiftFeature>();
        for (int o = 0; o < octaves.length; ++o) {
            if (octaves[o].state == FloatArray2DScaleOctave.State.EMPTY) continue;
            Vector<SiftFeature> more = runOctave(o);
            features.addAll(more);
        }
        return features;
    }

    /**
     * detect features in all scale octaves
     *
     * @return detected features
     */
    public Vector<SiftFeature> run(int max_size) {
        Vector<SiftFeature> features = new Vector<SiftFeature>();
        for (int o = 0; o < octaves.length; ++o) {
            if (octaves[o].width <= max_size && octaves[o].height <= max_size) {
                Vector<SiftFeature> more = runOctave(o);
                features.addAll(more);
            }
        }

        //System.out.println( features.size() + " candidates processed in all octaves" );
        return features;
    }


    /**
     * identify corresponding features using spatial constraints
     *
     * @param fs1    feature collection from set 1 sorted by decreasing size
     * @param fs2    feature collection from set 2 sorted by decreasing size
     * @param max_sd maximal difference in size (ratio max/min)
     * @param model  transformation model to be applied to fs2
     * @param max_id maximal distance in image space ($\sqrt{x^2+y^2}$)
     * @return matches
     *         <p/>
     *         TODO implement the spatial constraints
     */
    public static Vector<PointMatch> createMatches(
            List<SiftFeature> fs1,
            List<SiftFeature> fs2,
            float max_sd,
            Model model,
            float max_id) {
        Vector<PointMatch> matches = new Vector<PointMatch>();
        float min_sd = 1.0f / max_sd;

        int size = fs2.size();
        int size_1 = size - 1;

        for (SiftFeature f1 : fs1) {
            SiftFeature best = null;
            float best_d = Float.MAX_VALUE;
            float second_best_d = Float.MAX_VALUE;

            int first = 0;
            int last = size_1;
            int s = size / 2 + size % 2;
            if (max_sd < Float.MAX_VALUE) {
                while (s > 1) {
                    SiftFeature f2 = fs2.get(last);
                    if (f2.scale / f1.scale < min_sd) last = Math.max(0, last - s);
                    else last = Math.min(size_1, last + s);
                    f2 = fs2.get(first);
                    if (f2.scale / f1.scale < max_sd) first = Math.max(0, first - s);
                    else first = Math.min(size_1, first + s);
                    s = s / 2 + s % 2;
                }
                //System.out.println( "first = " + first + ", last = " + last + ", first.scale = " + fs2.get( first ).scale + ", last.scale = " + fs2.get( last ).scale + ", this.scale = " + f1.scale );
            }

            //for ( Feature f2 : fs2 )

            for (int i = first; i <= last; ++i) {
                SiftFeature f2 = fs2.get(i);
                float d = f1.descriptorDistance(f2);
                if (d < best_d) {
                    second_best_d = best_d;
                    best_d = d;
                    best = f2;
                } else if (d < second_best_d)
                    second_best_d = d;
            }
            if (best != null && second_best_d < Float.MAX_VALUE && best_d / second_best_d < 0.92)
                // not weighted
//				matches.addElement(
//						new PointMatch(
//								new Point(
//										new float[] { f1.location[ 0 ], f1.location[ 1 ] } ),
//								new Point(
//										new float[] { best.location[ 0 ], best.location[ 1 ] } ) ) );
                // weighted with the features scale
                matches.addElement(
                        new PointMatch(
                                new Point(
                                        new float[]{f1.location[0], f1.location[1]}),
                                new Point(
                                        new float[]{best.location[0], best.location[1]}),
                                (f1.scale + best.scale) / 2.0f));
        }
        // now remove ambiguous matches
        for (int i = 0; i < matches.size(); ) {
            boolean amb = false;
            PointMatch m = matches.get(i);
            float[] m_p2 = m.getP2().getL();
            for (int j = i + 1; j < matches.size(); ) {
                PointMatch n = matches.get(j);
                float[] n_p2 = n.getP2().getL();
                if (m_p2[0] == n_p2[0] && m_p2[1] == n_p2[1]) {
                    amb = true;
                    //System.out.println( "removing ambiguous match at " + j );
                    matches.removeElementAt(j);
                } else ++j;
            }
            if (amb) {
                //System.out.println( "removing ambiguous match at " + i );
                matches.removeElementAt(i);
            } else ++i;
        }
        return matches;
    }

    /**
     * get a histogram of feature sizes
     */
    public static float[] featureSizeHistogram(
            Vector<SiftFeature> features,
            float min,
            float max,
            int bins) {
        System.out.print("estimating feature size histogram ...");
        int num_features = features.size();
        float h[] = new float[bins];
        int hb[] = new int[bins];

        for (SiftFeature f : features) {
            int bin = (int) Math.max(0, Math.min(bins - 1, (int) (Math.log(f.scale) / Math.log(2.0) * 28.0f)));
            ++hb[bin];
        }
        for (int i = 0; i < bins; ++i) {
            h[i] = (float) hb[i] / (float) num_features;
        }
        System.out.println(" done");
        return h;
    }
}
