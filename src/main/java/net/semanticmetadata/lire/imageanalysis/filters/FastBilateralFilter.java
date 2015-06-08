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

package net.semanticmetadata.lire.imageanalysis.filters;


//  See http://en.wikipedia.org/wiki/Bilateral_filter.
//  Implementation of the O(1) bilateral filtering method presented in the following reference:
//  [Qingxiong Yang, Kar-Han Tan and Narendra Ahuja, Real-time O(1) Bilateral Filtering,
//  IEEE Conference on Computer Vision and Pattern Recognition (CVPR) 2009]
//
//  The algorithm provided here is an approximation of the bilateral filter based
//  on interpolation of spatial filters at different pixel intensity levels. It
//  runs in constant time.
//  The pixel intensity levels are quantized and only the filters for the quantized
//  values are calculated. The implementation also includes (optional) spatial
//  sub-sampling to increase the overall speed.
//  This implementation is initially based on the C code available at http://www.cs.cityu.edu.hk/~qiyang/
//
public final class FastBilateralFilter{

    /*
        // Use this class like this ...

        BufferedImage b = new BufferedImage(img.getWidth(), img.getHeight(), BufferedImage.TYPE_INT_RGB);
        b.getGraphics().drawImage(img, 0, 0, null);
        IndexedIntArray src = new IndexedIntArray(new int[img.getWidth()*img.getHeight()], 0);
        IndexedIntArray dst = new IndexedIntArray(new int[img.getWidth()*img.getHeight()], 0);
        b.getRaster().getDataElements(0, 0, img.getWidth(), img.getHeight(), src.array);
        FastBilateralFilter fbf = new FastBilateralFilter(img.getWidth(), img.getHeight(), img.getWidth(), 30f, 0.3f);
        fbf.apply(src, dst);
        b.getRaster().setDataElements(0, 0, img.getWidth(), img.getHeight(), dst.array);
    */

    private final int width;
    private final int height;
    private final int stride;
    private final int downSampling;
    private final int radius;
    private final float[] box;
    private final float[][] jk;
    private final float[] wk;
    private final float[] grayscale;
    private final int[] colors;
    private final int[] buffer1;
    private final int[] buffer2;
    private final int channels;


    // sigmaR = sigma Range (for pixel intensities)
    // sigmaD = sigma Distance (for pixel locations)
    public FastBilateralFilter(int width, int height, float sigmaR, float sigmaD) {
        this(width, height, width, sigmaR, sigmaD, 4, 3, 3);
    }


    // sigmaR = sigma Range (for pixel intensities)
    // sigmaD = sigma Distance (for pixel locations)
    public FastBilateralFilter(int width, int height, int stride,
                               float sigmaR, float sigmaD) {
        this(width, height, stride, sigmaR, sigmaD, 4, 3, 3);
    }


    // sigmaR = sigma Range (for pixel intensities)
    // sigmaD = sigma Distance (for pixel locations)
    // range sampling: 4 is enough to guarantee an accurate approximation
    public FastBilateralFilter(int width, int height, int stride,
                               float sigmaR, float sigmaD, int rangeSampling, int downSampling, int channels) {
        if (height < 8)
            throw new IllegalArgumentException("The height must be at least 8");

        if (width < 8)
            throw new IllegalArgumentException("The width must be at least 8");

        if (stride < 8)
            throw new IllegalArgumentException("The stride must be at least 8");

        if ((downSampling < 0) || (downSampling > 3))
            throw new IllegalArgumentException("The down sampling factor must be in [0..3]");

        if ((rangeSampling < 1) || (downSampling > 5))
            throw new IllegalArgumentException("The range sampling factor must be in [1..5]");

        if ((sigmaR < 1) && (sigmaR > 32))
            throw new IllegalArgumentException("The range sigma must be in [1..32]");

        if ((sigmaD < 1) && (sigmaD > 32))
            throw new IllegalArgumentException("The distance sigma must be in [1..32]");

        if ((channels < 1) || (channels > 3))
            throw new IllegalArgumentException("The number of image channels must be in [1..3]");

        this.height = height;
        this.width = width;
        this.stride = stride;
        final int adjust = (1 << downSampling) - 1;
        int scaledH = (height + adjust) >> downSampling;
        int scaledW = (width + adjust) >> downSampling;
        this.box = new float[scaledW * scaledH];
        this.jk = new float[][]{new float[scaledW * scaledH], new float[scaledW * scaledH]};
        this.wk = new float[scaledW * scaledH];
        this.channels = channels;
        this.downSampling = downSampling;
        this.grayscale = new float[rangeSampling];
        this.radius = (int) ((2 * sigmaD * Math.min(scaledW, scaledH) + 1) / 2);
        this.buffer1 = new int[scaledH * scaledW];
        this.buffer2 = new int[scaledH * scaledW];
        this.colors = new int[256];

        for (int i = 0; i < this.colors.length; i++)
            this.colors[i] = (int) (256f * Math.exp(-(float) (i * i) / (2 * sigmaR * sigmaR)));
    }

    private static void gaussianRecursiveX(float[] od, float[] id, int w, int h,
                                           float a0, float a1, float a2, float a3, float b1, float b2,
                                           float coefp, float coefn) {
        int offs = 0;

        for (int y = 0; y < h; y++) {
            // forward pass
            float xp = id[offs];
            float yb = coefp * xp;
            float yp = yb;

            for (int x = 0; x < w; x++) {
                float xc = id[offs + x];
                float yc = a0 * xc + a1 * xp - b1 * yp - b2 * yb;
                od[offs + x] = yc;
                xp = xc;
                yb = yp;
                yp = yc;
            }

            // reverse pass: ensure response is symmetrical
            float xn = id[offs + w - 1];
            float xa = xn;
            float yn = coefn * xn;
            float ya = yn;

            for (int x = w - 1; x >= 0; x--) {
                float xc = id[offs + x];
                float yc = a2 * xn + a3 * xa - b1 * yn - b2 * ya;
                od[offs + x] += yc;
                xa = xn;
                xn = xc;
                ya = yn;
                yn = yc;
            }

            offs += w;
        }
    }

    private static void gaussianRecursiveY(float[] od, float[] id, int w, int h,
                                           float a0, float a1, float a2, float a3, float b1, float b2,
                                           float coefp, float coefn) {
        for (int x = 0; x < w; x++) {
            // forward pass
            int offs = 0;
            float xp = id[x];
            float yb = coefp * xp;
            float yp = yb;

            for (int y = 0; y < h; y++) {
                float xc = id[offs + x];
                float yc = a0 * xc + a1 * xp - b1 * yp - b2 * yb;
                od[offs + x] = yc;
                xp = xc;
                yb = yp;
                yp = yc;
                offs += w;
            }

            // reverse pass: ensure response is symmetrical
            offs = (h - 1) * w;
            float xn = id[offs + x];
            float xa = xn;
            float yn = coefn * xn;
            float ya = yn;

            for (int y = h - 1; y >= 0; y--) {
                float xc = id[offs + x];
                float yc = a2 * xn + a3 * xa - b1 * yn - b2 * ya;
                od[offs + x] += yc;
                xa = xn;
                xn = xc;
                ya = yn;
                yn = yc;
                offs -= w;
            }
        }
    }

    private static void gaussianRecursive(float[] image, float[] temp, int w, int h, float sigma) {
        final float nsigma = (sigma < 0.1f) ? 0.1f : sigma;
        final float alpha = 1.695f / nsigma;
        final float ema = (float) Math.exp(-alpha);
        final float ema2 = (float) Math.exp(-2 * alpha);
        final float b1 = -2 * ema;
        final float b2 = ema2;
        final float k = (1 - ema) * (1 - ema) / (1 + 2 * alpha * ema - ema2);
        final float a0 = k;
        final float a1 = k * (alpha - 1) * ema;
        final float a2 = k * (alpha + 1) * ema;
        final float a3 = -k * ema2;
        final float coefp = (a0 + a1) / (1 + b1 + b2);
        final float coefn = (a2 + a3) / (1 + b1 + b2);
        gaussianRecursiveX(temp, image, w, h, a0, a1, a2, a3, b1, b2, coefp, coefn);
        gaussianRecursiveY(image, temp, w, h, a0, a1, a2, a3, b1, b2, coefp, coefn);
    }

    private static float interpolateLinearXY(float[] image, float x, float y, int w) {
        final int x0 = (int) x;
        final int xt = x0 + 1;
        final int y0 = (int) y;
        final float dx = x - x0;
        final float dy = y - y0;
        final float dx1 = 1 - dx;
        final float dy1 = 1 - dy;
        final float d00 = dx1 * dy1;
        final float d0t = dx * dy1;
        final float dt0 = dx1 * dy;
        final float dtt = dx * dy;
        final int offs0 = y0 * w;
        final int offst = offs0 + w;
        return ((d00 * image[offs0 + x0]) + (d0t * image[offs0 + xt]) +
                (dt0 * image[offst + x0]) + (dtt * image[offst + xt]));
    }

    private static float interpolateLinearXY2(float[] image1, float[] image2, float alpha, float x, float y, int w) {
        final int x0 = (int) x;
        final int xt = x0 + 1;
        final int y0 = (int) y;
        final float dx = x - x0;
        final float dy = y - y0;
        final float dx1 = 1 - dx;
        final float dy1 = 1 - dy;
        final float d00 = dx1 * dy1;
        final float d0t = dx * dy1;
        final float dt0 = dx1 * dy;
        final float dtt = dx * dy;
        final int offs0 = y0 * w;
        final int offst = offs0 + w;
        float res1 = ((d00 * image1[offs0 + x0]) + (d0t * image1[offs0 + xt]) +
                (dt0 * image1[offst + x0]) + (dtt * image1[offst + xt]));
        float res2 = ((d00 * image2[offs0 + x0]) + (d0t * image2[offs0 + xt]) +
                (dt0 * image2[offst + x0]) + (dtt * image2[offst + xt]));
        return alpha * res1 + (1.0f - alpha) * res2;
    }

    public boolean apply(IndexedIntArray source, IndexedIntArray destination) {
        // Aliasing
        final int[] src = source.array;
        final int[] dst = destination.array;
        final int srcIdx = source.index;
        final int dstIdx = destination.index;
        final int[] buf1 = this.buffer1;
        final float[] wk_ = this.wk;
        final int ds = this.downSampling;
        final int scaledH = this.height >> ds;
        final int scaledW = this.width >> ds;
        final int len = src.length;
        int[] buf2 = src;

        if (ds > 0) {
            buf2 = this.buffer2;
            final int xx = srcIdx % this.stride;
            final int yy = srcIdx / this.stride;
            final int ww = (this.width + xx < this.stride) ? this.width : this.stride - xx;
            final int hh = ((this.height + yy) * this.stride <= len) ? this.height : this.height - yy;
            DecimateDownSampler sampler = new DecimateDownSampler(ww, hh,
                    this.stride, srcIdx, 1 << ds);
            sampler.subSample(src, buf2);
        } else if ((srcIdx != 0) || (this.stride != this.width)) {
            buf2 = this.buffer2;
            int iOffs = srcIdx;
            int oOffs = dstIdx;

            for (int y = this.height; y > 0; y--) {
                System.arraycopy(src, iOffs, buf2, oOffs, this.width);
                iOffs += this.stride;
                oOffs += this.width;
            }
        }

        for (int channel = 0; channel < this.channels; channel++) {
            final int shift = channel << 3;
            int min = 255;
            int max = 0;

            // Extract channel and min,max for this channel
            for (int i = 0; i < buf1.length; i++) {
                final int val = (buf2[i] >> shift) & 0xFF;
                max = max - (((max - val) >> 31) & (max - val));
                min = min + (((val - min) >> 31) & (val - min));
                buf1[i] = val;
            }

            final float fmin = (float) min;
            final float fmax = (float) max;
            final int maxGrayIdx = this.grayscale.length - 1;
            this.grayscale[0] = fmin;
            this.grayscale[maxGrayIdx] = fmax;
            final float delta = fmax - fmin + 0.01f; // make sure it is not 0

            // Create scale of gray tones
            for (int i = 1; i < maxGrayIdx; i++)
                this.grayscale[i] = (float) min + (i * (delta / maxGrayIdx));

            int jk_idx0 = 0;
            int jk_idx1 = 1;
            float[] jk_ = this.jk[0];
            final float shift_inv = 1.0f / (1 << ds);
            final float delta_scale = (float) maxGrayIdx / delta;

            // For each gray level
            for (int grayRangeIdx = 0; grayRangeIdx <= maxGrayIdx; grayRangeIdx++) {
                int offs = 0;
                final float gray = this.grayscale[grayRangeIdx];

                // Compute Principle Bilateral Filtered Image Component Jk (and Wk)
                for (int y = 0; y < scaledH; y++) {
                    final int end = offs + scaledW;

                    for (int x = offs; x < end; x++) {
                        final int val = buf1[x] & 0xFF;
                        final int colorIdx = (int) (Math.abs(gray - val) + 0.5f);
                        final int color = this.colors[colorIdx];
                        jk_[x] = color * val;
                        wk_[x] = color;
                    }

                    offs += scaledW;
                }

                gaussianRecursive(jk_, this.box, scaledW, scaledH, this.radius);
                gaussianRecursive(wk_, this.box, scaledW, scaledH, this.radius);
                final int scaledSize = scaledW * scaledH;
                final float maxW = (float) (scaledW - 2);
                final float maxH = (float) (scaledH - 2);
                final int w = this.width;

                for (int n = 0; n < scaledSize; n++)
                    jk_[n] /= wk_[n];

                if (grayRangeIdx != 0) {
                    int iOffs = srcIdx;
                    int oOffs = dstIdx;
                    final float[] jk0 = this.jk[jk_idx0];
                    final float[] jk1 = this.jk[jk_idx1];

                    // Calculate the bilateral filtered pixel value by linear interpolation of Jk and Jk+1
                    for (int y = 0; y < this.height; y++) {
                        float ys = Math.min(((float) y) * shift_inv, maxH);
                        final int endX = (iOffs + w < len) ? w : len - iOffs;

                        for (int x = 0; x < endX; x++) {
                            final int val = (src[iOffs + x] >> shift) & 0xFF;
                            final float kf = ((float) (val - min) * delta_scale);
                            final int k = (int) kf;

                            if (k == (grayRangeIdx - 1)) {
                                final float alpha = (float) (k + 1) - kf;
                                final float xs = Math.min(((float) x) * shift_inv, maxW);
                                final int val2 = (int) interpolateLinearXY2(jk0, jk1, alpha, xs, ys, scaledW);
                                dst[oOffs + x] &= ~(0xFF << shift); //src can be the same buffer as dst
                                dst[oOffs + x] |= ((val2 & 0xFF) << shift);
                            } else if ((grayRangeIdx == maxGrayIdx) && (k == grayRangeIdx)) {
                                final float xs = Math.min(((float) x) * shift_inv, maxW);
                                final int val2 = (int) (interpolateLinearXY(jk1, xs, ys, scaledW) + 0.5f);
                                dst[oOffs + x] &= ~(0xFF << shift); //src can be the same buffer as dst
                                dst[oOffs + x] |= ((val2 & 0xFF) << shift);
                            }
                        }

                        iOffs += this.stride;
                        oOffs += this.stride;

                        if (iOffs >= len)
                            break;
                    }

                    jk_idx1 = jk_idx0;
                    jk_idx0 = 1 - jk_idx1;
                }

                jk_ = this.jk[jk_idx1];
            }
        }

        return true;
    }
}