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
 * ~~~~~~~~~~~~~~~~~~~~
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */
package net.semanticmetadata.lire.imageanalysis.mpeg7;

import net.semanticmetadata.lire.imageanalysis.LireFeature;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * ScalableColor
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ScalableColorImpl {
    protected Logger logger = Logger.getLogger(getClass().getName());

    protected BufferedImage img;
    protected int NumberOfCoefficients = 256;
    protected int NumberOfBitplanesDiscarded = 0;
    protected int _ySize, _xSize;
    protected int _h_value, _s_value, _v_value;
    protected double _quant_h, _quant_s, _quant_v;
    protected int _xNumOfBlocks, _yNumOfBlocks;
    protected int[][][] _wholeHist = null;
    protected int[] haarTransformedHistogram;
    protected static int[][] scalableColorQuantValues =
            {
                    {217, 9, 255}, {-71, 9, 255}, {-27, 8, 127}, {-54, 9, 255}, {-8, 7, 63}, {-14, 7, 63}, {-22, 7, 63}, {-29, 8, 127},
                    {-6, 6, 31}, {-13, 7, 63}, {-11, 6, 31}, {-22, 7, 63}, {-9, 7, 63}, {-14, 7, 63}, {-19, 7, 63}, {-22, 7, 63},
                    {0, 4, 7}, {-1, 5, 15}, {0, 3, 3}, {-2, 6, 31}, {1, 5, 15}, {-5, 6, 31}, {0, 5, 15}, {0, 7, 63},
                    {2, 5, 15}, {-2, 6, 31}, {-2, 5, 15}, {0, 7, 63}, {3, 5, 15}, {-5, 6, 31}, {-1, 6, 31}, {4, 7, 63},
                    {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {-1, 5, 15}, {0, 3, 3}, {0, 3, 3}, {-1, 5, 15}, {-2, 5, 15},
                    {-1, 5, 15}, {-1, 4, 7}, {-1, 5, 15}, {-3, 5, 15}, {-1, 5, 15}, {-2, 5, 15}, {-4, 5, 15}, {-5, 5, 15},
                    {-1, 5, 15}, {0, 3, 3}, {-2, 5, 15}, {-2, 5, 15}, {-2, 5, 15}, {-3, 5, 15}, {-3, 5, 15}, {0, 5, 15},
                    {0, 5, 15}, {0, 5, 15}, {0, 5, 15}, {2, 5, 15}, {-1, 5, 15}, {0, 5, 15}, {3, 6, 31}, {3, 5, 15},
                    {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 4, 7}, {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {-1, 4, 7},
                    {-1, 4, 7}, {-1, 4, 7}, {-2, 5, 15}, {-1, 5, 15}, {-2, 5, 15}, {-2, 5, 15}, {-2, 5, 15}, {-1, 5, 15},
                    {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {-1, 4, 7}, {0, 2, 1}, {0, 3, 3}, {-1, 4, 7}, {-1, 5, 15},
                    {-2, 5, 15}, {-1, 4, 7}, {-2, 5, 15}, {-1, 5, 15}, {-3, 5, 15}, {-3, 5, 15}, {-2, 5, 15}, {0, 5, 15},
                    {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {-1, 4, 7}, {0, 3, 3}, {0, 3, 3}, {-2, 5, 15}, {-2, 5, 15},
                    {-2, 5, 15}, {-2, 4, 7}, {-2, 5, 15}, {-1, 5, 15}, {-3, 5, 15}, {-3, 5, 15}, {-1, 5, 15}, {0, 5, 15},
                    {1, 4, 7}, {0, 3, 3}, {0, 4, 7}, {-1, 4, 7}, {0, 3, 3}, {0, 4, 7}, {-1, 4, 7}, {0, 4, 7},
                    {-1, 4, 7}, {-1, 3, 3}, {-1, 4, 7}, {0, 4, 7}, {-1, 5, 15}, {0, 5, 15}, {1, 5, 15}, {-1, 5, 15},
                    {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3}, {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3},
                    {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 4, 7}, {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3},
                    {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {1, 4, 7}, {0, 2, 1}, {0, 3, 3}, {-1, 4, 7}, {1, 4, 7},
                    {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 4, 7}, {0, 3, 3}, {0, 3, 3}, {-1, 4, 7}, {0, 4, 7},
                    {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3}, {0, 2, 1}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3},
                    {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {1, 4, 7}, {0, 2, 1}, {0, 3, 3}, {0, 4, 7}, {1, 4, 7},
                    {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {1, 5, 15}, {0, 3, 3}, {0, 3, 3}, {-1, 5, 15}, {2, 5, 15},
                    {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 4, 7}, {0, 3, 3}, {0, 3, 3}, {-1, 4, 7}, {1, 5, 15},
                    {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {0, 4, 7}, {0, 4, 7},
                    {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {1, 4, 7}, {0, 3, 3}, {0, 3, 3}, {-1, 5, 15}, {1, 5, 15},
                    {0, 3, 3}, {0, 2, 1}, {-1, 3, 3}, {1, 5, 15}, {0, 3, 3}, {-1, 4, 7}, {-1, 5, 15}, {2, 5, 15},
                    {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 4, 7}, {0, 3, 3}, {-1, 3, 3}, {0, 4, 7}, {1, 4, 7},
                    {1, 3, 3}, {0, 2, 1}, {-1, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {1, 4, 7},
                    {0, 3, 3}, {0, 2, 1}, {-1, 3, 3}, {0, 4, 7}, {0, 3, 3}, {0, 3, 3}, {0, 4, 7}, {1, 4, 7},
                    {0, 3, 3}, {0, 2, 1}, {0, 3, 3}, {0, 4, 7}, {0, 3, 3}, {-1, 3, 3}, {0, 4, 7}, {1, 4, 7},
                    {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {0, 3, 3}, {-1, 3, 3}, {0, 3, 3}, {-1, 4, 7}
            };

    protected static final int[][] tabelle = new int[][]{
            {0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10,
                    12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6,
                    8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2,
                    4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14,
                    0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10,
                    12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6,
                    8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2,
                    4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14,
                    0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10,
                    12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6,
                    8, 10, 12, 14, 0, 4, 8, 12, 0, 4, 8, 12, 0, 4, 8, 12, 0, 4, 8, 12, 0, 4,
                    8, 12, 0, 4, 8, 12, 0, 4, 8, 12, 0, 8, 0},

            {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2,
                    2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5,
                    5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8,
                    8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10,
                    11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13, 13,
                    13, 13, 14, 14, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 15, 15, 0, 0, 0, 0,
                    0, 0, 0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 4, 4, 4, 4, 4, 4, 4, 4, 6, 6,
                    6, 6, 6, 6, 6, 6, 8, 8, 8, 8, 8, 8, 8, 8, 10, 10, 10, 10, 10, 10, 10, 10,
                    12, 12, 12, 12, 12, 12, 12, 12, 14, 14, 14, 14, 14, 14, 14, 14, 0, 0, 0, 0, 0, 0,
                    0, 0, 2, 2, 2, 2, 2, 2, 2, 2, 8, 8, 8, 8, 8, 8, 8, 8, 10, 10, 10, 10,
                    10, 10, 10, 10, 0, 0, 0, 0, 2, 2, 2, 2, 8, 8, 8, 8, 10, 10, 10, 10, 0, 0,
                    0, 0, 8, 8, 8, 8, 0, 0, 0, 0, 0, 0, 0},

            {1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11,
                    13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7,
                    9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3,
                    5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15,
                    1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11,
                    13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 1, 3, 5, 7, 9, 11, 13, 15, 0, 2, 4, 6,
                    8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2,
                    4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14,
                    0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10,
                    12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6, 8, 10, 12, 14, 0, 2, 4, 6,
                    8, 10, 12, 14, 2, 6, 10, 14, 2, 6, 10, 14, 2, 6, 10, 14, 2, 6, 10, 14, 0, 4,
                    8, 12, 0, 4, 8, 12, 0, 4, 8, 12, 4, 12, 8},

            {0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2,
                    2, 2, 3, 3, 3, 3, 3, 3, 3, 3, 4, 4, 4, 4, 4, 4, 4, 4, 5, 5, 5, 5,
                    5, 5, 5, 5, 6, 6, 6, 6, 6, 6, 6, 6, 7, 7, 7, 7, 7, 7, 7, 7, 8, 8,
                    8, 8, 8, 8, 8, 8, 9, 9, 9, 9, 9, 9, 9, 9, 10, 10, 10, 10, 10, 10, 10, 10,
                    11, 11, 11, 11, 11, 11, 11, 11, 12, 12, 12, 12, 12, 12, 12, 12, 13, 13, 13, 13, 13, 13,
                    13, 13, 14, 14, 14, 14, 14, 14, 14, 14, 15, 15, 15, 15, 15, 15, 15, 15, 1, 1, 1, 1,
                    1, 1, 1, 1, 3, 3, 3, 3, 3, 3, 3, 3, 5, 5, 5, 5, 5, 5, 5, 5, 7, 7,
                    7, 7, 7, 7, 7, 7, 9, 9, 9, 9, 9, 9, 9, 9, 11, 11, 11, 11, 11, 11, 11, 11,
                    13, 13, 13, 13, 13, 13, 13, 13, 15, 15, 15, 15, 15, 15, 15, 15, 4, 4, 4, 4, 4, 4,
                    4, 4, 6, 6, 6, 6, 6, 6, 6, 6, 12, 12, 12, 12, 12, 12, 12, 12, 14, 14, 14, 14,
                    14, 14, 14, 14, 0, 0, 0, 0, 2, 2, 2, 2, 8, 8, 8, 8, 10, 10, 10, 10, 2, 2,
                    2, 2, 10, 10, 10, 10, 8, 8, 8, 8, 0, 0, 0},

            {128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 128, 128, 128, 128,
                    128, 128, 128, 128, 128, 128, 128, 64, 64, 64, 64,
                    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
                    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64,
                    64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 64, 32, 32, 32, 32, 32, 32,
                    32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
                    32, 32, 32, 32, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 16, 8, 8,
                    8, 8, 8, 8, 8, 8, 4, 4, 4, 4, 2, 2, 1}
    };
    protected static final int[] sorttab = new int[]{
            0, 4, 8, 12, 32, 36, 40, 44, 128, 132, 136, 140, 160, 164, 168, 172,
            2, 6, 10, 14, 34, 38, 42, 46, 130, 134, 138, 142, 162, 166, 170, 174,
            64, 66, 68, 70, 72, 74, 76, 78, 96, 98, 100, 102, 104, 106, 108, 110, 192, 194, 196, 198, 200, 202, 204, 206, 224, 226, 228, 230, 232, 234, 236, 238,
            16, 18, 20, 22, 24, 26, 28, 30, 48, 50, 52, 54, 56, 58, 60, 62, 80, 82, 84, 86, 88, 90, 92, 94, 112, 114, 116, 118, 120, 122, 124, 126, 144, 146, 148, 150, 152, 154, 156, 158, 176, 178, 180, 182, 184, 186, 188, 190, 208, 210, 212, 214, 216, 218, 220, 222, 240, 242, 244, 246, 248, 250, 252, 254,
            1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35, 37, 39, 41, 43, 45, 47, 49, 51, 53, 55, 57, 59, 61, 63, 65, 67, 69, 71, 73, 75, 77, 79, 81, 83, 85, 87, 89, 91, 93, 95, 97, 99, 101, 103, 105, 107, 109, 111, 113, 115, 117, 119, 121, 123, 125, 127, 129, 131, 133, 135, 137, 139, 141, 143, 145, 147, 149, 151, 153, 155, 157, 159, 161, 163, 165, 167, 169, 171, 173, 175, 177, 179, 181, 183, 185, 187, 189, 191, 193, 195, 197, 199, 201, 203, 205, 207, 209, 211, 213, 215, 217, 219, 221, 223, 225, 227, 229, 231, 233, 235, 237, 239, 241, 243, 245, 247, 249, 251, 253, 255
    };
    private int[] pixels = null;

    public ScalableColorImpl() {

    }

    public ScalableColorImpl(BufferedImage image, int NumberOfCoefficients, int NumberOfBitplanesDiscarded) {
        this.img = image;
        int numC = NumberOfCoefficients;
        if (numC < 31)
            numC = 16;
        else if (numC < 64)
            numC = 32;
        else if (numC < 128)
            numC = 64;
        else if (numC < 256)
            numC = 128;
        else if (numC >= 256) numC = 256;
        int numB = NumberOfBitplanesDiscarded;
        if (numB < 1) numB = 0;
        if (numB > 8) numB = 8;
        this.NumberOfBitplanesDiscarded = numB;
        this.NumberOfCoefficients = numC;
        _xSize = img.getWidth();
        _ySize = img.getHeight();
        init();
        extract();
    }

    public ScalableColorImpl(BufferedImage image) {
        this.img = image;
        this.NumberOfBitplanesDiscarded = 0;
        this.NumberOfCoefficients = 64;
        _xSize = img.getWidth();
        _ySize = img.getHeight();
        init();
        extract();
    }

    public void extract(BufferedImage image) {
        this.img = image;
        this.NumberOfBitplanesDiscarded = 0;
        this.NumberOfCoefficients = 64;
        _xSize = img.getWidth();
        _ySize = img.getHeight();
        init();
        extract();
    }

    /**
     * Use this constructor if the pixel data was taken from
     * a non rectangular region. Please note that the neighbourhood
     * of pixels cannot be taken into account (No reliable spatial
     * coherency can be created). The pixels array goes like:
     * {pixel1[0], pixel1[1], pixel1[2], pixel2[0], pixel2[1], ...}<br>
     * please note that althought
     *
     * @param pixels gives the pixels one int after another.
     */
    public ScalableColorImpl(int[] pixels) {
        this.img = null;
        this.NumberOfBitplanesDiscarded = 0;
        this.NumberOfCoefficients = 64;
        _xSize = 1;
        _ySize = pixels.length / 3;
        this.pixels = pixels;
        init();
        extract();
    }

    public ScalableColorImpl(String descriptor) {
        _xSize = 0;
        _ySize = 0;
        setStringRepresentation(descriptor);
        init();
    }

    protected void init() {
        _xNumOfBlocks = 1;
        _yNumOfBlocks = 1;
        _h_value = 16;
        _s_value = 4;
        _v_value = 4;

    }

    protected void extract() {
        int imageColSize = _xSize * _ySize * 3;
        //contains HSV- values of the Image
        int[] hsvImageBuffer;

        hsvImageBuffer = createHsvImageBuffer(imageColSize);

        if (_xNumOfBlocks > _xSize)
            _xNumOfBlocks = _xSize;
        if (_yNumOfBlocks > _ySize)
            _yNumOfBlocks = _ySize;

        //width and height of the blocks
        int width = _xSize / _xNumOfBlocks;
        int height = _ySize / _yNumOfBlocks;

        // nur bei verwendung von bloecken ...
//        _desHistograms = new int[_xNumOfBlocks][_yNumOfBlocks][_h_value][_s_value][_v_value];
//        for (int i = 0; i < _xNumOfBlocks; i++) {
//            for (int j = 0; j < _yNumOfBlocks; j++) {
//                for (int k = 0; k < _h_value; k++) {
//                    for (int l = 0; l < _s_value; l++) {
//                        for (int m = 0; m < _v_value; m++)
//                            _desHistograms[i][j][k][l][m] = 0;
//                    }
//                }
//            }
//        }

        _wholeHist = new int[_h_value][_s_value][_v_value];
        for (int k = 0; k < _h_value; k++) {
            for (int l = 0; l < _s_value; l++) {
                for (int m = 0; m < _v_value; m++)
                    _wholeHist[k][l][m] = 0;
            }
        }

        //Quantisation and histogram-calculation
        int x, y;
        x = 0;
        y = 0;

        for (int m = 0; m < _xNumOfBlocks; m++) {
            for (int n = 0; n < _yNumOfBlocks; n++) {
                int xy;
                for (int j = y; j < (y + height); j++) {
                    xy = (x * 3) + (j * _xSize * 3);
                    for (int i = xy; i < (xy + (width * 3)); i += 3) {
                        _Quant(hsvImageBuffer[i], hsvImageBuffer[i + 1], hsvImageBuffer[i + 2], m, n);
                    }
                }
                y += height;
            }
            x += width;
            y = 0;
        }
        int sumPixels = 0;
        int[] tmpHist = new int[_h_value * _v_value * _s_value];
        int count = 0;
        for (int k = 0; k < _v_value; k++) {
            for (int l = 0; l < _s_value; l++) {
                for (int m = 0; m < _h_value; m++) {
                    tmpHist[count] = _wholeHist[m][l][k];
                    sumPixels += tmpHist[count];
                    count++;
                }
            }
        }
        QuantizeHistogram(tmpHist, sumPixels);
        haarTransformedHistogram = HaarTransform(tmpHist);
    }

    private int[] createHsvImageBuffer(int imageColSize) {
        int[] hsvImageBuffer = null;
        hsvImageBuffer = new int[imageColSize];
        if (img != null) {
            //convertRgbToHsv
            int[] hsv = new int[3];
            WritableRaster raster = img.getRaster();
            int[] pixel = new int[3];

            for (int i = 0; i < imageColSize; i += 3) {
                raster.getPixel((i / 3) % _xSize, (i / 3) / _xSize, pixel);
                convertRgbToHsv(pixel[0], pixel[1], pixel[2], hsv);
                hsvImageBuffer[i] = hsv[0];
                hsvImageBuffer[i + 1] = hsv[1];
                hsvImageBuffer[i + 2] = hsv[2];
            }
        } else if (pixels != null) {
            int[] hsv = new int[3];
            for (int i = 0; i < pixels.length; i += 3) {
                convertRgbToHsv(pixels[i], pixels[i + 1], pixels[i + 2], hsv);
                hsvImageBuffer[i] = hsv[0];
                hsvImageBuffer[i + 1] = hsv[1];
                hsvImageBuffer[i + 2] = hsv[2];
            }
        }
        return hsvImageBuffer;
    }

    public void recalc() {
        if (img != null) {
            int[] tmpHist = new int[_h_value * _v_value * _s_value];
            int count = 0;
            int sumPixels = 0;
            for (int k = 0; k < _v_value; k++) {
                for (int l = 0; l < _s_value; l++) {
                    for (int m = 0; m < _h_value; m++) {
                        tmpHist[count] = _wholeHist[m][l][k];
                        sumPixels += tmpHist[count];
                        count++;
                    }
                }
            }
            QuantizeHistogram(tmpHist, sumPixels);
            haarTransformedHistogram = HaarTransform(tmpHist);
        }
    }

    protected static void convertRgbToHsv(int R, int G, int B, int[] hsv) {
/*
        // clssical approach
        int max, min;
        float hue = 0f;

        max = Math.max(R, G);     //calculation of max(R,G,B)
        max = Math.max(max, B);

        min = Math.min(R, G);     //calculation of min(R,G,B)
        min = Math.min(min, B);

        if (max == 0)
            hsv[1] = 0;
        else
            hsv[1] = (int) (((max - min) / (float) max) * 255f);      //Saturation range: (0..255)

        if (max == min) {
            hue = 0;     // (max - min) = 0
        } else {
            float maxMinusMin = (float) (max - min);
            if (R == max)
                hue = ((G - B) / maxMinusMin);

            else if (G == max)
                hue = (2 + (B - R) / maxMinusMin);

            else if (B == max)
                hue = (4 + (R - G) / maxMinusMin);

            hue *= 60f;

            if (hue < 0f)
                hue += 360f;
        }

        hsv[0] = (int) (hue);                        // Hue  range: (0..359)
        hsv[2] = max;                            // Value range:(0..255)

*/

        // taken from XM ...
        int maxrgb, minrgb;
        char order;
        double floath;

        if (G > B) {
            if (R > G) {
                maxrgb = R;
                minrgb = B;
                order = 0;
            } else {
                if (B > R) {
                    maxrgb = G;
                    minrgb = R;
                    order = 1;
                } else {
                    maxrgb = G;
                    minrgb = B;
                    order = 2;
                }
            }
        } else {
            if (R > B) {
                maxrgb = R;
                minrgb = G;
                order = 3;
            } else {
                if (G > R) {
                    maxrgb = B;
                    minrgb = R;
                    order = 4;
                } else {
                    maxrgb = B;
                    minrgb = G;
                    order = 5;
                }
            }
        }

        if (maxrgb == 0) {
            hsv[0] = 0;
            hsv[1] = 0;
            hsv[2] = 0;
            return;
        }

        hsv[2] = maxrgb;
        hsv[1] = ((maxrgb - minrgb) * 255) / maxrgb;

        if (maxrgb == minrgb) {
            hsv[0] = 0;
            return;
        }

        switch (order) {
            case 0:
                floath = 1.0 - (double) (R - G) / (double) (R - B);
                break;
            case 1:
                floath = 3.0 - (double) (G - B) / (double) (G - R);
                break;
            case 2:
                floath = 1.0 + (double) (G - R) / (double) (G - B);
                break;
            case 3:
                floath = 5.0 + (double) (R - B) / (double) (R - G);
                break;
            case 4:
                floath = 3.0 + (double) (B - G) / (double) (B - R);
                break;
            case 5:
                floath = 5.0 - (double) (B - R) / (double) (B - G);
                break;
            default:
                System.err.println("Error: Internal error in RGB to HSV transformation");
                floath = 0.0;
        }

        hsv[0] = (int) (floath / 6 * 255);
    }

    protected void _Quant(int H, int S, int V, int m, int n) {
        int i, j, k;
        i = (int) ((H * _h_value) / 256f);            //H in _quant_h	 levels
        j = (int) ((S * _s_value) / 256f);            //S in _quant_s  levels
        k = (int) ((V * _v_value) / 256f);            //V in _quant_v  levels

//        _desHistograms[m][n][i][j][k]++;
        _wholeHist[i][j][k]++;
    }

    public int getSimilarity(ScalableColorImpl secHist) {
        int diffsum = 0;
        int diff = 0;
        // similarity nur wenn #BitPlanes und #Coeff gleich ist:
        if (secHist.NumberOfBitplanesDiscarded == NumberOfBitplanesDiscarded &&
                secHist.NumberOfCoefficients == NumberOfCoefficients &&
                secHist.haarTransformedHistogram != null &&
                haarTransformedHistogram != null) {
            int[] secHaarHist = secHist.haarTransformedHistogram;
            for (int l = 0; l < NumberOfCoefficients; l++) {
                diff = Math.abs(secHaarHist[l] - haarTransformedHistogram[l]);
                diffsum += diff;
            }
        } else {
            // there is a problem, we cannot compare the descriptors.
            if (haarTransformedHistogram != null && secHist.haarTransformedHistogram != null)
                logger.info("NumberOfBitplanesDiscarded and/or NumberOfCoefficients not matching");
            else
                logger.info("One of the Descriptor histograms is NULL");
            return -1;
        }
        // sum of differences, L1
        return diffsum;
    }

    // von XM ... :)
    static void histo_3d_hirarch_5(int[][] tabelle, int tablae, int[] histogram,
                                   int h_size, int s_size, int v_size, int hist_nr) {
        int sum, dif, x1, y1, x2, y2;
        int[][] matrix = new int[16][16];

        for (int i = 0; i < h_size * s_size * v_size; ++i)
            matrix[i % (h_size)][i / (h_size)] = histogram[i];

        for (int i = 0; i < tablae; ++i) {
            y1 = tabelle[0][i];
            x1 = tabelle[1][i];
            y2 = tabelle[2][i];
            x2 = tabelle[3][i];
            sum = matrix[y1][x1] + matrix[y2][x2];
            dif = matrix[y2][x2] - matrix[y1][x1];

            matrix[y1][x1] = sum;
            matrix[y2][x2] = dif;
        }

        for (int i = 0; i < h_size * s_size * v_size; ++i)
            histogram[i] = matrix[i % (h_size)][i / (h_size)];
    }

    // von XM ... :)
    private static void histo_3d_hirarch_16_5(int[][] tabelle, int tablae, int[] histogram,
                                              int h_size, int s_size, int v_size, int hist_nr) {
        int i, j, sum, dif, x1, y1, x2, y2;
        int[][] matrix = new int[16][16];
        int iprint = 0;

        for (i = 0; i < h_size * s_size * v_size; ++i)
            matrix[i % (h_size)][i / (h_size)] = histogram[i];

        for (i = 0; i < tablae; ++i) {
            if (tabelle[4][i] <= 8) continue;
            y1 = tabelle[0][i];
            x1 = tabelle[1][i];
            y2 = tabelle[2][i];
            x2 = tabelle[3][i];
            sum = matrix[y1][x1] + matrix[y2][x2];
            dif = matrix[y2][x2] - matrix[y1][x1];

            if (iprint == 1) {

                matrix[y1][x1] = sum;
            }
            matrix[y2][x2] = dif;
        }

        for (i = 0; i < h_size * s_size * v_size; ++i)
            histogram[i] = matrix[i % (h_size)][i / (h_size)];
    }

    int[] QuantizeHistogram(int[] aHist, int sumPixels) {

        // ** from XM ...
        int factor = 0, ibinwert;
        //	  unsigned long NoOfBitsProBin=11;
        double binwert;

        //	  factor=0;
        //	  for (i=0; i<NoOfBitsProBin;i++)
        //	    factor=2*factor+1;

        factor = 0x7ff; //NoBitsProBin=11

        //quantisierung der bins
        for (int i = 0; i < NumberOfCoefficients; i++) {
            binwert = (double) (factor) * (double) (aHist[i] / (double) sumPixels);


            ibinwert = (int) (binwert + 0.49999);
            if (ibinwert > factor) ibinwert = factor;//obsolete

            aHist[i] = ibinwert;
        }

        factor = 15;
        int iwert = 0;
        double wert, potenz = 0.4;
        double arg, maxwert;


        maxwert = (double) 40 * (double) 2047 / (double) 100;

        for (int i = 0; i < NumberOfCoefficients; i++) {
            wert = (double) (aHist[i]);

            if (wert > maxwert) iwert = factor;

            if (wert <= maxwert) {
                arg = wert / maxwert;
                wert = (float) factor * Math.pow(arg, potenz);
                iwert = (int) (wert + 0.5);
            }

            if (iwert > factor) iwert = factor;

            aHist[i] = iwert;
        }
        return aHist;
    }

    // kopiert von XM ... :)
    private int[] HaarTransform(int[] aHist) {
        int index, tablae = 255, iwert, hist_nr;
        int[] histogram_in, histogram_out;
        int RecHistogram = 0;
        int h_size, s_size, v_size, max_color = 256;

        h_size = 16;
        s_size = 4;
        v_size = 4;

        hist_nr = 256;

        RecHistogram = 0;

        histogram_in = new int[max_color];
        histogram_out = new int[max_color];

        for (int i = 0; i < NumberOfCoefficients; i++) {
            histogram_in[i] = (int) aHist[i];
        }

        if (RecHistogram == 2) {
            histo_3d_hirarch_16_5(tabelle, tablae, histogram_in, h_size, s_size, v_size, hist_nr);
            hsv_hir_quant_lin_5(histogram_in);
        }

        if (RecHistogram != 2) {
            histo_3d_hirarch_5(tabelle, tablae, histogram_in,
                    h_size, s_size, v_size, hist_nr);

            for (int j = 0; j < 256; ++j) {
                index = sorttab[j];
                histogram_out[j] = histogram_in[index];
            }

            hsv_hir_quant_lin_5(histogram_out);
            red_bits_pro_bin_5(histogram_out, NumberOfBitplanesDiscarded, 0);

        }
        int[] returnHist = new int[hist_nr];
        System.arraycopy(histogram_out, 0, returnHist, 0, hist_nr);

        return returnHist;

    }

    // XM Kauderwelsch :)
    static void red_bits_pro_bin_5(int[] histogram,
                                   int NumberOfBitplanesDiscarded,
                                   int ivert) {
        int wert, wert1, bits_pro_bin, bits_pro_bild;
        int max_bits_pro_bin, anzkof;
        if (NumberOfBitplanesDiscarded == 0) return;

        bits_pro_bild = 0;
        max_bits_pro_bin = 0;
        anzkof = 0;
        if (NumberOfBitplanesDiscarded > 0) {
            for (int i = 0; i < 256; ++i) {
                bits_pro_bin = scalableColorQuantValues[i][1] - NumberOfBitplanesDiscarded;
                if (bits_pro_bin < 2) {
                    wert = histogram[i];
                    if (wert >= 0) histogram[i] = 1;
                    if (wert < 0) histogram[i] = 0;
                    bits_pro_bild = bits_pro_bild + 1;
                }
                if (bits_pro_bin >= 2) {
                    wert = histogram[i];
                    wert1 = wert;
                    if (wert < 0) wert = -wert;
                    bits_pro_bild = bits_pro_bild + bits_pro_bin;
                    if (bits_pro_bin > max_bits_pro_bin) max_bits_pro_bin = bits_pro_bin;
                    anzkof = anzkof + 1;

                    for (int j = 0; j < NumberOfBitplanesDiscarded; ++j)
                        wert = wert >> 1;

//                    if ((wert == 0) && (wert1 >= 0)) histogram[i] = 0;
//                    if ((wert == 0) && (wert1 < 0)) histogram[i] = 1;
                    if (wert1 < 0) wert = -wert;
                    histogram[i] = wert;
                }
            }
        }
    }

    // XM Kauderwelsch :)
    private static void hsv_hir_quant_lin_5(int[] histogram) {
        int i, wert, maxwert;
        for (i = 0; i < 256; ++i) {
            maxwert = scalableColorQuantValues[i][2];
            wert = histogram[i] - scalableColorQuantValues[i][0];
            if (wert > maxwert) wert = maxwert;
            if (wert < -maxwert) wert = -maxwert;
            histogram[i] = wert;
        }
    }

    public int getNumberOfCoefficients() {
        return NumberOfCoefficients;
    }

    public void setNumberOfCoefficients(int numberOfCoefficients) {
        if (img != null) {
            NumberOfCoefficients = numberOfCoefficients;
        }
    }

    public int getNumberOfBitplanesDiscarded() {
        return NumberOfBitplanesDiscarded;
    }

    public void setNumberOfBitplanesDiscarded(int numberOfBitplanesDiscarded) {
        if (img != null) {
            NumberOfBitplanesDiscarded = numberOfBitplanesDiscarded;
        }
    }

    public int[] getHaarTransformedHistogram() {
        return haarTransformedHistogram;
    }

    public boolean isRecalcable() {
        if (_wholeHist != null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Compares one descriptor to another.
     *
     * @param descriptor
     * @return the distance from [0,infinite) or -1 if descriptor type does not match
     */
    public float getDistance(LireFeature descriptor) {
        if (!(descriptor instanceof ScalableColorImpl)) return -1f;
        ScalableColorImpl sc = (ScalableColorImpl) descriptor;
        return (float) sc.getSimilarity(this);
    }

    public String getStringRepresentation() {
        StringBuilder builder = new StringBuilder(NumberOfCoefficients * 3 + 32);
        builder.append("scalablecolor;");
        builder.append(NumberOfBitplanesDiscarded);
        builder.append(';');
        builder.append(NumberOfCoefficients);
        builder.append(';');
        for (int i = 0; i < NumberOfCoefficients; i++) {
            builder.append(haarTransformedHistogram[i]);
            if ((i + 1) < NumberOfCoefficients) builder.append(' ');
        }
        return builder.toString();
    }

    public void setStringRepresentation(String descriptor) {
        String[] parts = descriptor.split(";");
        if (!parts[0].equals("scalablecolor")) {
            throw new UnsupportedOperationException("This is no valid representation of a ScalableColor descriptor!");
        }
        NumberOfBitplanesDiscarded = Integer.parseInt(parts[1]);
        NumberOfCoefficients = Integer.parseInt(parts[2]);
        int[] hist = new int[NumberOfCoefficients];
        StringTokenizer st = new StringTokenizer(parts[3], " ");
        int count = 0;
        while (st.hasMoreElements()) {
            hist[count] = Integer.parseInt(st.nextToken());
            count++;
        }
        haarTransformedHistogram = hist;
    }
}
