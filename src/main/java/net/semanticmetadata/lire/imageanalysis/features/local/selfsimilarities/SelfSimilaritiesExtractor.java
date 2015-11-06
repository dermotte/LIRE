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
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval -
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
 */

package net.semanticmetadata.lire.imageanalysis.features.local.selfsimilarities;

import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;

import java.awt.image.BufferedImage;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation based on the paper:
 * Efficient Retrieval of Deformable Shape Classes using Local Self-Similarities
 *
 * Created by Nektarios on 12/1/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class SelfSimilaritiesExtractor implements LocalFeatureExtractor {

    private int patch_size = 5;
    private int desc_rad = 40;
    private int cor_size = (desc_rad*2 + patch_size);
    private int nrad = 3;
    private int nang = 12;
    private int var_noise = 300000;
    private double saliency_thresh = 0.7;
    private double homogeneity_thresh = 0.7;
    private double snn_thresh = 0.85;

    private double [] ssdescs;
    double ssd_min_bound1, ssd_min_bound2;

    LinkedList<SelfSimilaritiesFeature> features = null;

    public SelfSimilaritiesExtractor(){
    }

    public void extract(BufferedImage img) {
        int image_width=img.getWidth(); //width
        int image_height=img.getHeight(); //height
        int image_channels=img.getColorModel().getNumColorComponents();

        int pixel;
        int[][] ImageGridRed = new int[image_width][image_height];
        int[][] ImageGridGreen = new int[image_width][image_height];
        int[][] ImageGridBlue = new int[image_width][image_height];
        for (int x = 0; x < image_width; x++) {
            for (int y = 0; y < image_height; y++) {
                pixel = img.getRGB(x, y);
                ImageGridRed[x][y] = (pixel >> 16) & 0xff;
                ImageGridGreen[x][y] = (pixel >> 8) & 0xff;
                ImageGridBlue[x][y] = (pixel) & 0xff;
            }
        }

        int xfrom = 0;
        int xto = image_width - cor_size + 1;
        int yfrom = 0;
        int yto = image_height - cor_size + 1;
        int ssd_sz = (cor_size - patch_size + 1);
        int dim = (nrad * nang);
        int ssd_sz_pow = ssd_sz*ssd_sz;

        ssdescs = new double[yto*xto*dim];

        int[] imask = createImask(ssd_sz);

        int corPatchRad = cor_size/2 - patch_size/2;
        int[] ssd;
        for (int y = yfrom; y < yto; y++){
            ssd = ssd_compute(ImageGridRed, ImageGridGreen, ImageGridBlue, xfrom, xfrom + ssd_sz, y, y + ssd_sz, xfrom + corPatchRad, y + corPatchRad, ssd_sz, patch_size);
            ssdesc_descriptor(ssd, imask, ssd_sz_pow, var_noise, (y*xto + xfrom)*(dim));

            for (int x = xfrom + 1; x < xto; x++){
                ssd_compute_irow(ImageGridRed, ImageGridGreen, ImageGridBlue, x, x + ssd_sz, y, y + ssd_sz, x + corPatchRad, y + corPatchRad, patch_size, ssd);
                ssdesc_descriptor(ssd, imask, ssd_sz_pow, var_noise, (y*xto + x)*(dim));
            }
        }

        prune_normalise_ssdescs(xfrom, xto, yfrom, yto, dim);
    }

    private int[] createImask(int ssd_sz) {
        int[] imask = new int[ssd_sz*ssd_sz];
        int center = (ssd_sz-1)/2;
        double lpbase = Math.pow(10, Math.log10((double) nrad) / nrad);
        double[] radiiQuants = new double[nrad];

        for (int i = 0; i < nrad-1; i++){
            radiiQuants[i] = (Math.pow(lpbase, i + 1)-1)/(nrad - 1) * center;
        }
        radiiQuants[nrad-1] = desc_rad;

        double r, ang;
        int rind, xCord, yCord;
        for (int x = 0; x < ssd_sz; x++){
            xCord = center - x;
            for (int y = 0; y < ssd_sz; y++){
                yCord = center - y;
                r = Math.sqrt((double) ((xCord * xCord) + (yCord * yCord)));
                ang = Math.atan2((double) (xCord), (double) (yCord)) + Math.PI;
//                rind=0;
                if (r > radiiQuants[nrad-1]){
                    imask[y*ssd_sz + x] = -1;
                } else {
                    for (rind = 0; rind < nrad; rind++){
                        if (r <= radiiQuants[rind]) break;
                    }
                    rind = (nrad-1) - rind;
                    imask[y*ssd_sz + x] = ((int)((ang*nang)/(2*Math.PI)) % nang)*nrad + rind;
                }
            }
        }
        imask[center*ssd_sz + center] = -1;
        return imask;
    }

    private int[] ssd_compute(int[][] ImageGridRed, int[][] ImageGridGreen, int[][] ImageGridBlue, int xl, int xr, int yl, int yr, int xp, int yp, int ssd_sz, int sz){
        int[] ssd = new int[ssd_sz*ssd_sz];
        int x1, x2, y1, y2, counter = 0;
        double diff;
        for (int yy = yl; yy < yr; yy++){
            for (int xx = xl; xx < xr; xx++){
                ssd[counter] = 0;
                // xc and yc give offset within the inner patch
                for (int xc = 0; xc < sz; xc++){
                    x1 = xx  + xc;
                    x2 = xp + xc;
                    for (int yc = 0; yc < sz; yc++){
                        y1 = yy  + yc;
                        y2 = yp + yc;

                        diff = ImageGridRed[x1][y1] - ImageGridRed[x2][y2];
                        ssd[counter] += diff*diff;
                        diff = ImageGridGreen[x1][y1] - ImageGridGreen[x2][y2];
                        ssd[counter] += diff*diff;
                        diff = ImageGridBlue[x1][y1] - ImageGridBlue[x2][y2];
                        ssd[counter] += diff*diff;
                    }
                }
                counter++;
            }
        }
        return ssd;
    }

    private void ssdesc_descriptor(int[] ssd, int[] imask, int ssd_sz_to, int var_noise, int offset){
        double autoQ = 0.0;
//            for(int j=0; j<numAutoVarianceIndices; j++)
//                autoQ = (ssdTraveller[autoVarianceIndices.get(j)]>autoQ)?ssdTraveller[autoVarianceIndices.get(j)]:autoQ;
        double val, divisor = (autoQ>var_noise)?autoQ:var_noise;
        int ptr;
        for (int i = 0; i < ssd_sz_to; i++){
            if (imask[i]!=-1) {
                val = Math.exp(-1*((double)ssd[i])/divisor);
                ptr = imask[i] + offset;
                ssdescs[ptr] = (ssdescs[ptr] > val) ? ssdescs[ptr] : val;
            }
        }
    }

    private void ssd_compute_irow(int[][] ImageGridRed, int[][] ImageGridGreen, int[][] ImageGridBlue, int xl, int xr, int yl, int yr, int xp, int yp, int sz, int[] ssd){ //TODO: ssd returned??
        int x1, y1, x2, y2, x3, x4, diff, counter = 0;
        x2 = xp - 1;
        x4 = xp + sz - 1;
        for (int yy = yl; yy < yr; yy++){
            for (int xx = xl; xx < xr; xx++){
                x1 = xx  - 1;
                x3 = xx  + sz - 1;
                // yc gives vertical offset within the inner patch
                for (int yc = 0; yc < sz; yc++){
                    y1 = yy  + yc;
                    y2 = yp + yc;

                    diff = ImageGridRed[x1][y1] - ImageGridRed[x2][y2];
                    ssd[counter] -= diff*diff;
                    diff = ImageGridRed[x3][y1] - ImageGridRed[x4][y2];
                    ssd[counter] += diff*diff;
                    diff = ImageGridGreen[x1][y1] - ImageGridGreen[x2][y2];
                    ssd[counter] -= diff*diff;
                    diff = ImageGridGreen[x3][y1] - ImageGridGreen[x4][y2];
                    ssd[counter] += diff*diff;
                    diff = ImageGridBlue[x1][y1] - ImageGridBlue[x2][y2];
                    ssd[counter] -= diff*diff;
                    diff = ImageGridBlue[x3][y1] - ImageGridBlue[x4][y2];
                    ssd[counter] += diff*diff;
                }
                counter++;
            }
        }
    }

    private double calc_ssd_ssdesc_min2(int ssdesc1Ptr, int ssdesc2Ptr, int ssdesc_size, double boundMultiplier){
        double bm;

        if (boundMultiplier == -1){
            bm = Math.sqrt((double) ssdesc_size);
        } else {
            bm = boundMultiplier;
        }

        double diff, one_norm = 0;
        for (int i = 0; i < ssdesc_size; i++){
            one_norm += Math.abs(ssdescs[ssdesc1Ptr + i] - ssdescs[ssdesc2Ptr + i]);
        }
        // only calculate the 2-norm if the 1-norm is such it will be within the minimum 2
        if (one_norm <= ssd_min_bound2){
            double two_norm = 0;
            for (int i = 0; i < ssdesc_size; i++){
                diff = ssdescs[ssdesc1Ptr + i] - ssdescs[ssdesc2Ptr + i];
                two_norm += diff*diff;
            }
            two_norm = Math.sqrt(two_norm);
            //calculate the bound for the calculated 2-norm
            double two_norm_bound = two_norm*bm;
            //update the minimum two bounds
            if ((two_norm_bound < ssd_min_bound2) && (two_norm_bound >= ssd_min_bound1)){
                ssd_min_bound2 = two_norm_bound;
            }
            if (two_norm_bound < ssd_min_bound1){
                ssd_min_bound2 = ssd_min_bound1;
                ssd_min_bound1 = two_norm_bound;
            }

            //NOTE: will return even if the norm is EQUAL to the second minimum value
            return two_norm;
        } else {
            return -1;
        }
    }

    private void prune_normalise_ssdescs(int xfrom, int xto, int yfrom, int yto, int dim){
//        LinkedList<SelfSimilaritiesFeature> features = new LinkedList<SelfSimilaritiesFeature>();
//        SelfSimilaritiesFeature feat;
//        double[] resp;

//        LinkedList<int[]> salient_coords = new LinkedList<int[]>();
//        LinkedList<int[]> homogeneous_coords = new LinkedList<int[]>();
//        LinkedList<int[]> snn_coords = new LinkedList<int[]>();
        LinkedList<int[]> draw_coords = new LinkedList<int[]>();

        double boundMultiplier = Math.sqrt((double) dim);
        double min_ssd, max_ssd, ssd, diff;
        int ssdesc1Ptr = -dim, ssdesc2Ptr;
        int corRad = (cor_size-1)/2;

//        boolean salientOrHomogeneous;
        LinkedList<Double> desc_sims = new LinkedList<Double>();
        for (int y = yfrom; y < yto; y++) {
            for (int x = xfrom; x < xto; x++) {
//                xc = x + corRad;
//                yc = y + corRad;
//                ssdesc1Ptr = (y*(xto) + x)*(dim);
                ssdesc1Ptr += dim;

                // find min/max for purposes of salient/homogeneous patch detection
                min_ssd = ssdescs[ssdesc1Ptr];
                max_ssd = min_ssd;
                diff = max_ssd - min_ssd;
                for (int i = 1; i < dim; i++){
                    min_ssd = (ssdescs[ssdesc1Ptr+i] < min_ssd) ? ssdescs[ssdesc1Ptr+i] : min_ssd;
                    max_ssd = (ssdescs[ssdesc1Ptr+i] > max_ssd) ? ssdescs[ssdesc1Ptr+i] : max_ssd;
                }

                /* Perform salient/homogeneous descriptor preening */
//                salientOrHomogeneous = false;
//                if (max_ssd < (1 - saliency_thresh)){
//                    salientOrHomogeneous = true;
//                    salient_coords.add((new int[] {xc, yc}));
//                }
//                if (min_ssd > homogeneity_thresh){
//                    salientOrHomogeneous = true;
//                    homogeneous_coords.add((new int[] {xc, yc}));
//                }

                /* Only continue if the descriptor hasn't already been categorised */
//                if (salientOrHomogeneous == false){
                if (!((max_ssd < (1 - saliency_thresh))||(min_ssd > homogeneity_thresh))){
				/* Perform second nearest neighbour preening if necessary, otherwise categorise as valid descriptor immediately */
                    if (snn_thresh < 1) {
//                        desc_sims = new LinkedList<Double>();
                        desc_sims.clear();
                        // min_ssds is used to store the minimum 2 ssd bounds (minssd/sqrt(2))
                        // during the current iteration, so that the ssd is computed
                        // the minimum number of times
                        ssd_min_bound1 = Double.MAX_VALUE; //todo: ssd_min_bound1 & ssd_min_bound2 not global
                        ssd_min_bound2 = ssd_min_bound1;

                        // 1. Iterate through all other descriptors, calculating and storing similarity
                        ssdesc2Ptr = -dim;
                        for (int y2 = 0; y2 < yto; y2++){
                            for (int x2 = 0; x2 < xto; x2++){
//                                ssdesc2Ptr = (y2*(xto) + x2)*(dim);
                                ssdesc2Ptr += dim;
                                // skip comparison to self
//                                if ((y==y2)&&(x==x2)) continue;
                                if (ssdesc1Ptr==ssdesc2Ptr) continue;

                                // calculate the ssd if within the range of the two smallest ssds calculated so far
                                ssd = calc_ssd_ssdesc_min2(ssdesc1Ptr, ssdesc2Ptr, dim, boundMultiplier);

                                // only store the ssd if within that range
                                if (ssd != -1){
                                    desc_sims.add(ssd);
                                }
                            }
                        }
                        // 2. Sort the similarities in ascending order
                        Collections.sort(desc_sims);
                        // 3. Calculate SNN ratio for current descriptor
//                        snn = desc_sims.get(0)/desc_sims.get(1);

//                        if ((desc_sims.get(0)/desc_sims.get(1)) > snn_thresh){
//                            snn_coords.add((new int[] {xc, yc}));
//                        } else {
                        if (!((desc_sims.get(0)/desc_sims.get(1)) > snn_thresh)) {
                            draw_coords.add((new int[] {x + corRad, y + corRad}));
//                            resp = new double[dim];
//                            for (int binOffset = 0; binOffset < dim; binOffset++) {
//                                resp[binOffset]= (ssdescs[ssdesc1Ptr + binOffset] - min_ssd)/(diff);
//                            }
//                            features.add(new SelfSimilaritiesFeature(resp, (new int[] {x + corRad, y + corRad})));
                        }
                    }else {
                        draw_coords.add((new int[] {x + corRad, y + corRad}));
//                        resp = new double[dim];
//                        for (int binOffset = 0; binOffset < dim; binOffset++) {
//                            resp[binOffset]= (ssdescs[ssdesc1Ptr + binOffset] - min_ssd)/(diff);
//                        }
//                        features.add(new SelfSimilaritiesFeature(resp, (new int[] {x + corRad, y + corRad})));
                    }
                }
            }
        }

        double[] resp;
        int[] coords;
        features = new LinkedList<SelfSimilaritiesFeature>();
        SelfSimilaritiesFeature feat;
        for (int i = 0; i < draw_coords.size(); i++) {
            coords = draw_coords.get(i);
            ssdesc1Ptr = ((coords[1]-corRad)*(xto) + (coords[0]-corRad))*(dim);

            // find min/max for purposes of salient/homogeneous patch detection
            min_ssd = ssdescs[ssdesc1Ptr];
            max_ssd = min_ssd;
            for (int ii = 1; ii < dim; ii++){
                min_ssd = (ssdescs[ssdesc1Ptr+ii] < min_ssd) ? ssdescs[ssdesc1Ptr+ii] : min_ssd;
                max_ssd = (ssdescs[ssdesc1Ptr+ii] > max_ssd) ? ssdescs[ssdesc1Ptr+ii] : max_ssd;
            }

            resp = new double[dim];
            for (int binOffset = 0; binOffset < dim; binOffset++) {
                resp[binOffset]= (ssdescs[ssdesc1Ptr + binOffset] - min_ssd)/(max_ssd - min_ssd);
            }

            feat = new SelfSimilaritiesFeature(resp, coords[0], coords[1], cor_size);
            features.add(feat);
        }
    }

    @Override
    public List<? extends LocalFeature> getFeatures() {
        return features;
    }

    @Override
    public Class<? extends LocalFeature> getClassOfFeatures() {
        return SelfSimilaritiesFeature.class;
    }
}