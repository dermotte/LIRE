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

import java.util.Random;

public class Filter {
    /**
     * return a integer that is flipped in the range [0 ... mod - 1]
     *
     * @param a     the value to be flipped
     * @param range the size of the range
     * @return a flipped in range like a ping pong ball
     */
    public static final int flipInRange(int a, int mod) {
        int p = 2 * mod;
        if (a < 0) a = p + a % p;
        if (a >= p) a = a % p;
        if (a >= mod) a = mod - a % mod - 1;
        return a;
    }

    /**
     * fast floor ld of unsigned v
     */
    public static final int ldu(int v) {
        int c = 0;
        do {
            v >>= 1;
            c++;
        }
        while (v > 1);
        return c;
    }

    /**
     * Create a 1d-Gaussian kernel of appropriate size
     *
     * @param sigma     Standard deviation of the Gaussian kernel
     * @param normalize Normalize integral of the Gaussian kernel to 1 or not...
     * @return float[] Gaussian kernel of appropriate size
     */
    public static float[] createGaussianKernel1D(float sigma, boolean normalize) {
        int size = 3;
        float[] gaussianKernel;

        if (sigma <= 0) {
            gaussianKernel = new float[3];
            gaussianKernel[1] = 1;
        } else {
            size = Math.max(3, (int) (2 * (int) (3 * sigma + 0.5) + 1));

            float two_sq_sigma = 2 * sigma * sigma;
            gaussianKernel = new float[size];

            for (int x = size / 2; x >= 0; --x) {
                float val = (float) Math.exp(-(float) (x * x) / two_sq_sigma);

                gaussianKernel[size / 2 - x] = val;
                gaussianKernel[size / 2 + x] = val;
            }
        }

        if (normalize) {
            float sum = 0;
            for (float value : gaussianKernel)
                sum += value;

            for (int i = 0; i < gaussianKernel.length; i++)
                gaussianKernel[i] /= sum;
        }


        return gaussianKernel;
    }


    public static FloatArray2D createGaussianKernel2D(float sigma, boolean normalize) {
        int size = 3;
        FloatArray2D gaussianKernel;

        if (sigma <= 0) {
            gaussianKernel = new FloatArray2D(3, 3);
            gaussianKernel.data[4] = 1;
        } else {
            size = Math.max(3, (int) (2 * (int) (3 * sigma + 0.5) + 1));

            float two_sq_sigma = 2 * sigma * sigma;
            gaussianKernel = new FloatArray2D(size, size);

            for (int y = size / 2; y >= 0; --y) {
                for (int x = size / 2; x >= 0; --x) {
                    float val = (float) Math.exp(-(float) (y * y + x * x) / two_sq_sigma);

                    gaussianKernel.set(val, size / 2 - x, size / 2 - y);
                    gaussianKernel.set(val, size / 2 - x, size / 2 + y);
                    gaussianKernel.set(val, size / 2 + x, size / 2 - y);
                    gaussianKernel.set(val, size / 2 + x, size / 2 + y);
                }
            }
        }

        if (normalize) {
            float sum = 0;
            for (float value : gaussianKernel.data)
                sum += value;

            for (int i = 0; i < gaussianKernel.data.length; i++)
                gaussianKernel.data[i] /= sum;
        }


        return gaussianKernel;
    }

    /*
    ** create a normalized gaussian impulse with appropriate size and offset center
    */
    static public FloatArray2D create_gaussian_kernel_2D_offset(
            float sigma,
            float offset_x,
            float offset_y,
            boolean normalize) {
        int size = 3;
        FloatArray2D gaussian_kernel;
        if (sigma == 0) {
            gaussian_kernel = new FloatArray2D(3, 3);
            gaussian_kernel.data[4] = 1;
        } else {
            size = Math.max(3, (int) (2 * Math.round(3 * sigma) + 1));
            float two_sq_sigma = 2 * sigma * sigma;
            // float normalization_factor = 1.0/(float)M_PI/two_sq_sigma;
            gaussian_kernel = new FloatArray2D(size, size);
            for (int x = size - 1; x >= 0; --x) {
                float fx = (float) (x - size / 2);
                for (int y = size - 1; y >= 0; --y) {
                    float fy = (float) (y - size / 2);
                    float val = (float) (Math.exp(-(Math.pow(fx - offset_x, 2) + Math.pow(fy - offset_y, 2)) / two_sq_sigma));
                    gaussian_kernel.set(val, x, y);
                }
            }
        }
        if (normalize) {
            float sum = 0;
            for (float value : gaussian_kernel.data)
                sum += value;

            for (int i = 0; i < gaussian_kernel.data.length; i++)
                gaussian_kernel.data[i] /= sum;
        }
        return gaussian_kernel;
    }

    public static FloatArray2D computeIncreasingGaussianX(FloatArray2D input, float stDevStart, float stDevEnd) {
        FloatArray2D output = new FloatArray2D(input.width, input.height);

        int width = input.width;
        float changeFilterSize = (float) (stDevEnd - stDevStart) / (float) width;
        float sigma;
        int filterSize;

        float avg;

        for (int x = 0; x < input.width; x++) {
            sigma = stDevStart + changeFilterSize * (float) x;
            FloatArray2D kernel = createGaussianKernel2D(sigma, true);
            filterSize = kernel.width;

            for (int y = 0; y < input.height; y++) {
                avg = 0;

                for (int fx = -filterSize / 2; fx <= filterSize / 2; fx++)
                    for (int fy = -filterSize / 2; fy <= filterSize / 2; fy++) {
                        try {
                            avg += input.get(x + fx, y + fy) * kernel.get(fx + filterSize / 2, fy + filterSize / 2);
                        } catch (Exception e) {
                        }
                        ;

                    }

                output.set(avg, x, y);
            }
        }
        return output;
    }

    public static FloatArray2D computeGaussian(FloatArray2D input, float sigma) {
        FloatArray2D output = new FloatArray2D(input.width, input.height);

        float avg, kernelsum;

        FloatArray2D kernel = createGaussianKernel2D(sigma, true);
        int filterSize = kernel.width;

        for (int x = 0; x < input.width; x++) {
            for (int y = 0; y < input.height; y++) {
                avg = 0;
                kernelsum = 0;

                for (int fx = -filterSize / 2; fx <= filterSize / 2; fx++)
                    for (int fy = -filterSize / 2; fy <= filterSize / 2; fy++) {
                        try {
                            avg += input.get(x + fx, y + fy) * kernel.get(fx + filterSize / 2, fy + filterSize / 2);
                            kernelsum += kernel.get(fx + filterSize / 2, fy + filterSize / 2);
                        } catch (Exception e) {
                        }
                        ;

                    }

                output.set(avg / kernelsum, x, y);
            }
        }
        return output;
    }


    public static FloatArray2D computeGaussianFastMirror(FloatArray2D input, float sigma) {
        FloatArray2D output = new FloatArray2D(input.width, input.height);

        float avg, kernelsum = 0;
        float[] kernel = createGaussianKernel1D(sigma, true);
        int filterSize = kernel.length;

        // get kernel sum
        for (double value : kernel)
            kernelsum += value;

        // fold in x
        for (int x = 0; x < input.width; x++)
            for (int y = 0; y < input.height; y++) {
                avg = 0;

                if (x - filterSize / 2 >= 0 && x + filterSize / 2 < input.width)
                    for (int f = -filterSize / 2; f <= filterSize / 2; f++)
                        avg += input.get(x + f, y) * kernel[f + filterSize / 2];
                else
                    for (int f = -filterSize / 2; f <= filterSize / 2; f++)
                        avg += input.getMirror(x + f, y) * kernel[f + filterSize / 2];

                output.set(avg / kernelsum, x, y);

            }

        // fold in y
        for (int x = 0; x < input.width; x++) {
            float[] temp = new float[input.height];

            for (int y = 0; y < input.height; y++) {
                avg = 0;

                if (y - filterSize / 2 >= 0 && y + filterSize / 2 < input.height)
                    for (int f = -filterSize / 2; f <= filterSize / 2; f++)
                        avg += output.get(x, y + f) * kernel[f + filterSize / 2];
                else
                    for (int f = -filterSize / 2; f <= filterSize / 2; f++)
                        avg += output.getMirror(x, y + f) * kernel[f + filterSize / 2];

                temp[y] = avg / kernelsum;
            }

            for (int y = 0; y < input.height; y++)
                output.set(temp[y], x, y);
        }

        return output;
    }

    public static FloatArray2D distortSamplingX(FloatArray2D input) {
        FloatArray2D output = new FloatArray2D(input.width, input.height);

        int filterSize = 3;
        float avg;

        Random rnd = new Random(353245632);

        for (int x = 0; x < input.width; x++) {
            FloatArray2D kernel = new FloatArray2D(3, 1);

            float random = (rnd.nextFloat() - 0.5f) * 2;
            float val1, val2, val3;

            if (random < 0) {
                val1 = -random;
                val2 = 1 + random;
                val3 = 0;
            } else {
                val3 = random;
                val2 = 1 - random;
                val1 = 0;
            }

            kernel.set(val1, 0, 0);
            kernel.set(val2, 1, 0);
            kernel.set(val3, 2, 0);

            for (int y = 0; y < input.height; y++) {
                avg = 0;

                for (int fx = -filterSize / 2; fx <= filterSize / 2; fx++) {
                    try {
                        avg += input.get(x + fx, y) * kernel.get(fx + filterSize / 2, 0);
                    } catch (Exception e) {
                    }
                    ;
                }

                output.set(avg, x, y);
            }
        }
        return output;
    }

    public static FloatArray2D distortSamplingY(FloatArray2D input) {
        FloatArray2D output = new FloatArray2D(input.width, input.height);

        int filterSize = 3;
        float avg;

        Random rnd = new Random(7893469);

        for (int y = 0; y < input.height; y++) {
            FloatArray2D kernel = new FloatArray2D(1, 3);

            float random = (rnd.nextFloat() - 0.5f) * 2;
            float val1, val2, val3;

            if (random < 0) {
                val1 = -random;
                val2 = 1 + random;
                val3 = 0;
            } else {
                val3 = random;
                val2 = 1 - random;
                val1 = 0;
            }

            kernel.set(val1, 0, 0);
            kernel.set(val2, 0, 1);
            kernel.set(val3, 0, 2);

            for (int x = 0; x < input.width; x++) {
                avg = 0;

                for (int fy = -filterSize / 2; fy <= filterSize / 2; fy++) {
                    try {
                        avg += input.get(x, y + fy) * kernel.get(0, fy + filterSize / 2);
                    } catch (Exception e) {
                    }
                    ;
                }

                output.set(avg, x, y);
            }
        }
        return output;
    }

    public static FloatArray2D computeLaPlaceFilter3(FloatArray2D input) {
        FloatArray2D output = new FloatArray2D(input.width, input.height);

        float derivX, derivY;
        float x1, x2, x3;
        float y1, y2, y3;

        for (int y = 1; y < input.height - 1; y++)
            for (int x = 1; x < input.width - 1; x++) {
                x1 = input.get(x - 1, y);
                x2 = input.get(x, y);
                x3 = input.get(x + 1, y);

                derivX = x1 - 2 * x2 + x3;

                y1 = input.get(x, y - 1);
                y2 = input.get(x, y);
                y3 = input.get(x, y + 1);

                derivY = y1 - 2 * y2 + y3;

                output.set((float) Math.sqrt(Math.pow(derivX, 2) + Math.pow(derivY, 2)), x, y);
            }

        return output;
    }

    public static FloatArray2D computeLaPlaceFilter5(FloatArray2D input) {
        FloatArray2D output = new FloatArray2D(input.width, input.height);

        float derivX, derivY;
        float x1, x3, x5;
        float y1, y3, y5;

        for (int y = 2; y < input.height - 2; y++)
            for (int x = 2; x < input.width - 2; x++) {
                x1 = input.get(x - 2, y);
                x3 = input.get(x, y);
                x5 = input.get(x + 2, y);

                derivX = x1 - 2 * x3 + x5;

                y1 = input.get(x, y - 2);
                y3 = input.get(x, y);
                y5 = input.get(x, y + 2);

                derivY = y1 - 2 * y3 + y5;

                output.set((float) Math.sqrt(Math.pow(derivX, 2) + Math.pow(derivY, 2)), x, y);
            }

        return output;
    }

    public static FloatArray2D[] createGradients(FloatArray2D array) {
        FloatArray2D[] gradients = new FloatArray2D[2];
        gradients[0] = new FloatArray2D(array.width, array.height);
        gradients[1] = new FloatArray2D(array.width, array.height);

        for (int y = 0; y < array.height; ++y) {
            int[] ro = new int[3];
            ro[0] = array.width * Math.max(0, y - 1);
            ro[1] = array.width * y;
            ro[2] = array.width * Math.min(y + 1, array.height - 1);
            for (int x = 0; x < array.width; ++x) {
                // L(x+1, y) - L(x-1, y)
                float der_x = (
                        array.data[ro[1] + Math.min(x + 1, array.width - 1)] -
                                array.data[ro[1] + Math.max(0, x - 1)]) / 2;

                // L(x, y+1) - L(x, y-1)
                float der_y = (
                        array.data[ro[2] + x] -
                                array.data[ro[0] + x]) / 2;

                //! amplitude
                gradients[0].data[ro[1] + x] = (float) Math.sqrt(Math.pow(der_x, 2) + Math.pow(der_y, 2));
                //! orientation
                gradients[1].data[ro[1] + x] = (float) Math.atan2(der_y, der_x);
            }
        }
        //ImageArrayConverter.FloatArrayToImagePlus( gradients[ 1 ], "gradients", 0, 0 ).show();
        return gradients;
    }

    /**
     * in place enhance all values of a FloatArray to fill the given range
     */
    public static final void enhance(FloatArray2D src, float scale) {
        float min = Float.MAX_VALUE;
        float max = Float.MIN_VALUE;
        for (int i = 0; i < src.data.length; ++i) {
            if (src.data[i] < min) min = src.data[i];
            else if (src.data[i] > max) max = src.data[i];
        }
        scale /= (max - min);
        for (int i = 0; i < src.data.length; ++i)
            src.data[i] = scale * (src.data[i] - min);
    }

    /**
     * convolve an image with a horizontal and a vertical kernel
     * simple straightforward, not optimized---replace this with a trusted better version soon
     *
     * @param input the input image
     * @param h     horizontal kernel
     * @param v     vertical kernel
     * @return convolved image
     */
    public static FloatArray2D convolveSeparable(FloatArray2D input, float[] h, float[] v) {
        FloatArray2D output = new FloatArray2D(input.width, input.height);
        FloatArray2D temp = new FloatArray2D(input.width, input.height);

        int hl = h.length / 2;
        int vl = v.length / 2;

        int xl = input.width - h.length + 1;
        int yl = input.height - v.length + 1;

        // create lookup tables for coordinates outside the image range
        int[] xb = new int[h.length + hl - 1];
        int[] xa = new int[h.length + hl - 1];
        for (int i = 0; i < xb.length; ++i) {
            xb[i] = flipInRange(i - hl, input.width);
            xa[i] = flipInRange(i + xl, input.width);
        }

        int[] yb = new int[v.length + vl - 1];
        int[] ya = new int[v.length + vl - 1];
        for (int i = 0; i < yb.length; ++i) {
            yb[i] = input.width * flipInRange(i - vl, input.height);
            ya[i] = input.width * flipInRange(i + yl, input.height);
        }

//		String xa_str = "xa: ";
//		String xb_str = "xb: ";
//		String ya_str = "ya: ";
//		String yb_str = "yb: ";
//		for ( int i = 0; i < xa.length; ++i )
//		{
//			xa_str = xa_str + xa[ i ] + ", ";
//			xb_str = xb_str + xb[ i ] + ", ";
//			ya_str = ya_str + ( ya[ i ] / input.width ) + ", ";
//			yb_str = yb_str + ( yb[ i ] / input.width ) + ", ";
//		}
//		
//		System.out.println( xb_str );
//		System.out.println( xa_str );
//		System.out.println( yb_str );
//		System.out.println( ya_str );


        xl += hl;
        yl += vl;
        // horizontal convolution per row
        int rl = input.height * input.width;
        for (int r = 0; r < rl; r += input.width) {
            for (int x = hl; x < xl; ++x) {
                int c = x - hl;
                float val = 0;
                for (int xk = 0; xk < h.length; ++xk) {
                    val += h[xk] * input.data[r + c + xk];
                }
                temp.data[r + x] = val;
            }
            for (int x = 0; x < hl; ++x) {
                float valb = 0;
                float vala = 0;
                for (int xk = 0; xk < h.length; ++xk) {
                    valb += h[xk] * input.data[r + xb[x + xk]];
                    vala += h[xk] * input.data[r + xa[x + xk]];
                }
                temp.data[r + x] = valb;
                temp.data[r + x + xl] = vala;
            }
        }

        // vertical convolution per column
        rl = yl * temp.width;
        int vlc = vl * temp.width;
        for (int x = 0; x < temp.width; ++x) {
            for (int r = vlc; r < rl; r += temp.width) {
                float val = 0;
                int c = r - vlc;
                int rk = 0;
                for (int yk = 0; yk < v.length; ++yk) {
                    val += v[yk] * temp.data[c + rk + x];
                    rk += temp.width;
                }
                output.data[r + x] = val;
            }
            for (int y = 0; y < vl; ++y) {
                int r = y * temp.width;
                float valb = 0;
                float vala = 0;
                for (int yk = 0; yk < v.length; ++yk) {
                    valb += h[yk] * temp.data[yb[y + yk] + x];
                    vala += h[yk] * temp.data[ya[y + yk] + x];
                }
                output.data[r + x] = valb;
                output.data[r + rl + x] = vala;
            }
        }

        return output;
    }

}
