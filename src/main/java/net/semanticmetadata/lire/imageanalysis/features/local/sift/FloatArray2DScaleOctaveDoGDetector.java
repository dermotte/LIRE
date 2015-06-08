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
 * Difference Of Gaussian detector on top of a scale space octave as described
 * by David Lowe \citep{Loew04}.
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
 * @author Stephan Saalfeld <saalfeld@mpi-cbg.de>
 * @version 0.1b
 */

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

import java.util.Vector;


public class FloatArray2DScaleOctaveDoGDetector {
    /**
     * minimal contrast of a candidate
     */
    //private static final float MIN_CONTRAST = 0.03f * 255.0f;
    //private static final float MIN_CONTRAST = 0.025f * 255.0f;
    private static final float MIN_CONTRAST = 0.025f;

    /**
     * maximal curvature ratio, higher values allow more edge-like responses
     */
    private static final float MAX_CURVATURE = 10;
    private static final float MAX_CURVATURE_RATIO = (MAX_CURVATURE + 1) * (MAX_CURVATURE + 1) / MAX_CURVATURE;

    private FloatArray2DScaleOctave octave;

    /**
     * detected candidates as float triples 0=>x, 1=>y, 2=>scale index
     */
    private Vector<float[]> candidates;

    public Vector<float[]> getCandidates() {
        return candidates;
    }

    /**
     * Constructor
     */
    public FloatArray2DScaleOctaveDoGDetector() {
        octave = null;
        candidates = null;
    }

    public void run(FloatArray2DScaleOctave o) {
        octave = o;
        candidates = new Vector<float[]>();
        detectCandidates();
    }

    private void detectCandidates() {
        FloatArray2D[] d = octave.getD();

        for (int i = d.length - 2; i >= 1; --i) {
            int ia = i - 1;
            int ib = i + 1;
            for (int y = d[i].height - 2; y >= 1; --y) {
                int r = y * d[i].width;
                int ra = r - d[i].width;
                int rb = r + d[i].width;

                X:
                for (int x = d[i].width - 2; x >= 1; --x) {
                    int ic = i;
                    int iac = ia;
                    int ibc = ib;
                    int yc = y;
                    int rc = r;
                    int rac = ra;
                    int rbc = rb;
                    int xc = x;
                    int xa = xc - 1;
                    int xb = xc + 1;
                    float e111 = d[ic].data[r + xc];

                    // check if d(x, y, i) is an extremum
                    // do it pipeline-friendly ;)

                    float e000 = d[iac].data[rac + xa];
                    boolean isMax = e000 < e111;
                    boolean isMin = e000 > e111;
                    if (!(isMax || isMin)) continue;
                    float e100 = d[iac].data[rac + xc];
                    isMax &= e100 < e111;
                    isMin &= e100 > e111;
                    if (!(isMax || isMin)) continue;
                    float e200 = d[iac].data[rac + xb];
                    isMax &= e200 < e111;
                    isMin &= e200 > e111;
                    if (!(isMax || isMin)) continue;

                    float e010 = d[iac].data[rc + xa];
                    isMax &= e010 < e111;
                    isMin &= e010 > e111;
                    if (!(isMax || isMin)) continue;
                    float e110 = d[iac].data[rc + xc];
                    isMax &= e110 < e111;
                    isMin &= e110 > e111;
                    if (!(isMax || isMin)) continue;
                    float e210 = d[iac].data[rc + xb];
                    isMax &= e210 < e111;
                    isMin &= e210 > e111;
                    if (!(isMax || isMin)) continue;

                    float e020 = d[iac].data[rbc + xa];
                    isMax &= e020 < e111;
                    isMin &= e020 > e111;
                    if (!(isMax || isMin)) continue;
                    float e120 = d[iac].data[rbc + xc];
                    isMax &= e120 < e111;
                    isMin &= e120 > e111;
                    if (!(isMax || isMin)) continue;
                    float e220 = d[iac].data[rbc + xb];
                    isMax &= e220 < e111;
                    isMin &= e220 > e111;
                    if (!(isMax || isMin)) continue;


                    float e001 = d[ic].data[rac + xa];
                    isMax &= e001 < e111;
                    isMin &= e001 > e111;
                    if (!(isMax || isMin)) continue;
                    float e101 = d[ic].data[rac + xc];
                    isMax &= e101 < e111;
                    isMin &= e101 > e111;
                    if (!(isMax || isMin)) continue;
                    float e201 = d[ic].data[rac + xb];
                    isMax &= e201 < e111;
                    isMin &= e201 > e111;
                    if (!(isMax || isMin)) continue;

                    float e011 = d[ic].data[rc + xa];
                    isMax &= e011 < e111;
                    isMin &= e011 > e111;
                    if (!(isMax || isMin)) continue;
                    float e211 = d[ic].data[rc + xb];
                    isMax &= e211 < e111;
                    isMin &= e211 > e111;
                    if (!(isMax || isMin)) continue;

                    float e021 = d[ic].data[rbc + xa];
                    isMax &= e021 < e111;
                    isMin &= e021 > e111;
                    if (!(isMax || isMin)) continue;
                    float e121 = d[ic].data[rbc + xc];
                    isMax &= e121 < e111;
                    isMin &= e121 > e111;
                    if (!(isMax || isMin)) continue;
                    float e221 = d[ic].data[rbc + xb];
                    isMax &= e221 < e111;
                    isMin &= e221 > e111;
                    if (!(isMax || isMin)) continue;


                    float e002 = d[ibc].data[rac + xa];
                    isMax &= e002 < e111;
                    isMin &= e002 > e111;
                    if (!(isMax || isMin)) continue;
                    float e102 = d[ibc].data[rac + xc];
                    isMax &= e102 < e111;
                    isMin &= e102 > e111;
                    if (!(isMax || isMin)) continue;
                    float e202 = d[ibc].data[rac + xb];
                    isMax &= e202 < e111;
                    isMin &= e202 > e111;
                    if (!(isMax || isMin)) continue;

                    float e012 = d[ibc].data[rc + xa];
                    isMax &= e012 < e111;
                    isMin &= e012 > e111;
                    if (!(isMax || isMin)) continue;
                    float e112 = d[ibc].data[rc + xc];
                    isMax &= e112 < e111;
                    isMin &= e112 > e111;
                    if (!(isMax || isMin)) continue;
                    float e212 = d[ibc].data[rc + xb];
                    isMax &= e212 < e111;
                    isMin &= e212 > e111;
                    if (!(isMax || isMin)) continue;

                    float e022 = d[ibc].data[rbc + xa];
                    isMax &= e022 < e111;
                    isMin &= e022 > e111;
                    if (!(isMax || isMin)) continue;
                    float e122 = d[ibc].data[rbc + xc];
                    isMax &= e122 < e111;
                    isMin &= e122 > e111;
                    if (!(isMax || isMin)) continue;
                    float e222 = d[ibc].data[rbc + xb];
                    isMax &= e222 < e111;
                    isMin &= e222 > e111;
                    if (!(isMax || isMin)) continue;

                    // so it is an extremum, try to localize it with subpixel
                    // accuracy, if it has to be moved for more than 0.5 in at
                    // least one direction, try it again there but maximally 5
                    // times

                    boolean isLocalized = false;
                    boolean isLocalizable = true;

                    float dx;
                    float dy;
                    float di;

                    float dxx;
                    float dyy;
                    float dii;

                    float dxy;
                    float dxi;
                    float dyi;

                    float ox;
                    float oy;
                    float oi;

                    float od = Float.MAX_VALUE;      // offset square distance

                    float fx = 0;
                    float fy = 0;
                    float fi = 0;

                    int t = 5; // maximal number of re-localizations
                    do {
                        --t;

                        // derive at (x, y, i) by center of difference
                        dx = (e211 - e011) / 2.0f;
                        dy = (e121 - e101) / 2.0f;
                        di = (e112 - e110) / 2.0f;

                        // create hessian at (x, y, i) by laplace
                        float e111_2 = 2.0f * e111;
                        dxx = e011 - e111_2 + e211;
                        dyy = e101 - e111_2 + e121;
                        dii = e110 - e111_2 + e112;

                        dxy = (e221 - e021 - e201 + e001) / 4.0f;
                        dxi = (e212 - e012 - e210 + e010) / 4.0f;
                        dyi = (e122 - e102 - e120 + e100) / 4.0f;

                        // invert hessian
                        Array2DRowRealMatrix H = new Array2DRowRealMatrix(new double[][]{
                                {(double) dxx, (double) dxy, (double) dxi},
                                {(double) dxy, (double) dyy, (double) dyi},
                                {(double) dxi, (double) dyi, (double) dii}});
                        RealMatrix H_inv;
                        try {
                            H_inv = new LUDecomposition(H).getSolver().getInverse();
                        } catch (RuntimeException e) {
                            continue X;
                        }
                        double[][] h_inv = H_inv.getData();

                        // estimate the location of zero crossing being the offset of the extremum

                        ox = -(float) h_inv[0][0] * dx - (float) h_inv[0][1] * dy - (float) h_inv[0][0] * di;
                        oy = -(float) h_inv[1][0] * dx - (float) h_inv[1][1] * dy - (float) h_inv[1][0] * di;
                        oi = -(float) h_inv[2][0] * dx - (float) h_inv[2][1] * dy - (float) h_inv[2][0] * di;

                        float odc = ox * ox + oy * oy + oi * oi;

                        if (odc < 2.0f) {
                            if ((Math.abs(ox) > 0.5 || Math.abs(oy) > 0.5 || Math.abs(oi) > 0.5) && odc < od) {
                                od = odc;

                                xc = (int) Math.round((float) xc + ox);
                                yc = (int) Math.round((float) yc + oy);
                                ic = (int) Math.round((float) ic + oi);

                                if (xc < 1 || yc < 1 || ic < 1 || xc > d[0].width - 2 || yc > d[0].height - 2 || ic > d.length - 2)
                                    isLocalizable = false;
                                else {
                                    xa = xc - 1;
                                    xb = xc + 1;
                                    rc = yc * d[ic].width;
                                    rac = rc - d[ic].width;
                                    rbc = rc + d[ic].width;
                                    iac = ic - 1;
                                    ibc = ic + 1;

                                    e000 = d[iac].data[rac + xa];
                                    e100 = d[iac].data[rac + xc];
                                    e200 = d[iac].data[rac + xb];

                                    e010 = d[iac].data[rc + xa];
                                    e110 = d[iac].data[rc + xc];
                                    e210 = d[iac].data[rc + xb];

                                    e020 = d[iac].data[rbc + xa];
                                    e120 = d[iac].data[rbc + xc];
                                    e220 = d[iac].data[rbc + xb];


                                    e001 = d[ic].data[rac + xa];
                                    e101 = d[ic].data[rac + xc];
                                    e201 = d[ic].data[rac + xb];

                                    e011 = d[ic].data[rc + xa];
                                    e111 = d[ic].data[rc + xc];
                                    e211 = d[ic].data[rc + xb];

                                    e021 = d[ic].data[rbc + xa];
                                    e121 = d[ic].data[rbc + xc];
                                    e221 = d[ic].data[rbc + xb];


                                    e002 = d[ibc].data[rac + xa];
                                    e102 = d[ibc].data[rac + xc];
                                    e202 = d[ibc].data[rac + xb];

                                    e012 = d[ibc].data[rc + xa];
                                    e112 = d[ibc].data[rc + xc];
                                    e212 = d[ibc].data[rc + xb];

                                    e022 = d[ibc].data[rbc + xa];
                                    e122 = d[ibc].data[rbc + xc];
                                    e222 = d[ibc].data[rbc + xb];
                                }
                            } else {
                                fx = (float) xc + ox;
                                fy = (float) yc + oy;
                                fi = (float) ic + oi;

                                if (fx < 0 || fy < 0 || fi < 0 || fx > d[0].width - 1 || fy > d[0].height - 1 || fi > d.length - 1)
                                    isLocalizable = false;
                                else
                                    isLocalized = true;
                            }
                        } else isLocalizable = false;
                    }
                    while (!isLocalized && isLocalizable && t >= 0);
                    // reject detections that could not be localized properly

                    if (!isLocalized) {
//						System.err.println( "Localization failed (x: " + xc + ", y: " + yc + ", i: " + ic + ") => (ox: " + ox + ", oy: " + oy + ", oi: " + oi + ")" );
//						if ( ic < 1 || ic > d.length - 2 )
//							System.err.println( "  Detection outside octave." );						
                        continue;
                    }

                    // reject detections with very low contrast

                    if (Math.abs(e111 + 0.5f * (dx * ox + dy * oy + di * oi)) < MIN_CONTRAST) continue;

                    // reject edge responses

                    float det = dxx * dyy - dxy * dxy;
                    float trace = dxx + dyy;
                    if (trace * trace / det > MAX_CURVATURE_RATIO) continue;

                    candidates.addElement(new float[]{fx, fy, fi});
                    //candidates.addElement( new float[]{ x, y, i } );
                }
            }
        }
    }
}