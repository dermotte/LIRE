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

// Trivial sub sampler that keeps 1 in "n" pixels
public class DecimateDownSampler
{
    private final int width;
    private final int height;
    private final int stride;
    private final int offset;
    private final int factor;


    public DecimateDownSampler(int width, int height)
    {
        this(width, height, width, 0, 2);
    }


    public DecimateDownSampler(int width, int height, int factor)
    {
        this(width, height, width, 0, factor);
    }


    public DecimateDownSampler(int width, int height, int stride, int offset, int factor)
    {
        if (height < 8)
            throw new IllegalArgumentException("The height must be at least 8");

        if (width < 8)
            throw new IllegalArgumentException("The width must be at least 8");

        if (offset < 0)
            throw new IllegalArgumentException("The offset must be at least 0");

        if (stride < width)
            throw new IllegalArgumentException("The stride must be at least as big as the width");

        if (factor < 2)
            throw new IllegalArgumentException("This implementation only supports "+
                    "a scaling factor greater than or equal to 2");

        this.height = height;
        this.width = width;
        this.stride = stride;
        this.offset = offset;
        this.factor = factor;
    }


    public void subSampleHorizontal(int[] input, int[] output)
    {
        final int w = this.width;
        final int inc = this.factor;
        final int st = this.stride;
        int iOffs = this.offset;
        int oOffs = 0;

        for (int j=this.height; j>0; j--)
        {
           final int end = iOffs + w;

           for (int i=iOffs; i<end; i+=inc)
              output[oOffs++] = input[i];

           iOffs += st;
        }
    }


    public void subSampleVertical(int[] input, int[] output)
    {
        final int w = this.width;
        final int inc = this.factor;
        final int stn = this.stride * inc;
        int iOffs = this.offset;
        int oOffs = 0;

        for (int j=this.height; j>0; j-=inc)
        {
           System.arraycopy(input, iOffs, output, oOffs, w);
           iOffs += stn;
        }
    }


    public void subSample(int[] input, int[] output)
    {
        final int w = this.width;
        final int inc = this.factor;
        final int stn = this.stride * inc;
        int iOffs = this.offset;
        int oOffs = 0;

        for (int j=this.height; j>0; j-=inc)
        {
           final int end = iOffs + w;

           for (int i=iOffs; i<end; i+=inc)
              output[oOffs++] = input[i];

           iOffs += stn;
        }
    }


    public boolean supportsScalingFactor(int factor)
    {
        return (factor >= 2) ? true : false;
    }
}
