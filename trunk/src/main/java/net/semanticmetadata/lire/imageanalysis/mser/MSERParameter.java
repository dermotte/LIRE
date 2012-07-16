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

package net.semanticmetadata.lire.imageanalysis.mser;

/**
 * Created by IntelliJ IDEA.
 * User: Shotty
 * Date: 30.06.2010
 * Time: 15:11:46
 */
public class MSERParameter {
    // delta, in the code, it compares (size_{i}-size_{i-delta})/size_{i-delta}
    int delta;
    // prune the area which bigger/smaller than maxArea/minArea
    double maxArea;
    double minArea;
    // prune the area have similar size to its children
    double maxVariation;
    // trace back to cut off mser with diversity < min_diversity
    double minDiversity;

    /* the next few params for MSER of color image */
    // for color image, the evolution steps
    int maxEvolution;
    // the area threshold to cause re-initialize
    double areaThreshold;
    // ignore too small margin
    double minMargin;
    // the aperture size for edge blur
    int edgeBlurSize;

    /**
     * Constructor with default values
     */
    public MSERParameter() {
        // original of paper: 3, 0.5, > 25 pixel, 1, 0.5
        this.delta = 5;
        this.minArea = 0.001;
        this.maxArea = 0.5;
        this.maxVariation = 1;
        this.minDiversity = 0.75F;
//        this.delta = 5;
//        this.minArea = 0.001;
//        this.maxArea = 0.5;
//        this.maxVariation = 1;
//        this.minDiversity = 0.75F;

/*
        this.minArea = 60;
        this.maxArea = 14400;

        this.maxEvolution = 200;
        this.areaThreshold = 1.01;
        this.minMargin = 0.003;
        this.edgeBlurSize = 5;
*/
    }

    public MSERParameter(int delta,
                         double minArea, double maxArea,
                         double maxVariation, double minDiversity,
                         int maxEvolution, double areaThreshold,
                         double minMargin, int edgeBlurSize) {
        this.delta = delta;
        this.minArea = minArea;
        this.maxArea = maxArea;
        this.maxVariation = maxVariation;
        this.minDiversity = minDiversity;

/*
        this.maxEvolution = maxEvolution;
        this.areaThreshold = areaThreshold;
        this.minMargin = minMargin;
        this.edgeBlurSize = edgeBlurSize;
*/
    }
}
