/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
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

package net.semanticmetadata.lire.imageanalysis.mser;

/**
 * Created by IntelliJ IDEA.
 * <p/>
 * User: Shotty
 * Date: 28.06.2010
 * Time: 23:53:21
 */
public class BoundaryPixel {
    public static final int RIGHT_EDGE = 0;
    public static final int BOTTOM_EDGE = 2;
    public static final int LEFT_EDGE = 4;
    public static final int TOP_EDGE = 6;
    public static final int NO_EDGE = 8;

    protected int imageWidth;
    protected int imageHeight;
    protected ImagePoint point;
    protected int nextEdge;

    public BoundaryPixel(ImagePoint point, int imageWidth, int imageHeight) {
        this.imageWidth = imageWidth;
        this.imageHeight = imageHeight;
        this.point = point;
        this.nextEdge = RIGHT_EDGE;
    }

    public ImagePoint getPoint() {
        return point;
    }

    public int getIndex() {
        return point.getIndex();
    }

    public int getX() {
        return point.getX();
    }

    public int getY() {
        return point.getY();
    }

    /**
     * Calculate the next Edge from the current pixel.
     * Starts from RIGHT --> BOTTOM --> LEFT --> TOP Neighbour Pixel
     * returns NULL if all edges where explored-
     *
     * @return an ImagePoint object or null if there is no edge left
     */
    public ImagePoint calcNextEdge() {
        ImagePoint nextEdgePoint;
        switch (nextEdge) {
            case RIGHT_EDGE:
                // try the get the right edge of the current pixel
                nextEdgePoint = getRightNeighbor();
                if (nextEdgePoint != null) // in boundary
                {
                    nextEdge = BOTTOM_EDGE;
                    return nextEdgePoint;
                }
            case BOTTOM_EDGE:
                // try the get the bottom edge of the current pixel
                nextEdgePoint = getBottomNeighbor();
                if (nextEdgePoint != null) // in boundary
                {
                    nextEdge = LEFT_EDGE;
                    return nextEdgePoint;
                }
            case LEFT_EDGE:
                // try to get the left edge of the current pixel
                nextEdgePoint = getLeftNeighbor();
                if (nextEdgePoint != null) // in boundary
                {
                    nextEdge = TOP_EDGE;
                    return nextEdgePoint;
                }
            case TOP_EDGE:
                // try to get the top edge of the current pixel
                nextEdgePoint = getTopNeighbor();
                if (nextEdgePoint != null) // in boundary
                {
                    nextEdge = NO_EDGE;
                    return nextEdgePoint;
                }
            default:
                nextEdge = NO_EDGE;
                return null; // all edges done
        }
    }

    /**
     * Explore the next Edge of the Pixel.
     * Returns null if all edges have been explored for this pixel
     *
     * @return the BoundaryPixel of the next Neighbour
     */
    public BoundaryPixel getNextBoundary() {
        ImagePoint nextEdge = calcNextEdge();
        if (nextEdge != null) {
            return new BoundaryPixel(nextEdge, imageWidth, imageHeight);
        } else {
            return null;
        }
    }

    protected ImagePoint getTopNeighbor() {
        if (getY() == 0) {
            return null;
        } else {
            return new ImagePoint(getIndex() - imageWidth, imageWidth);
        }
    }

    protected ImagePoint getLeftNeighbor() {
        if (getX() == 0) {
            return null;
        } else {
            return new ImagePoint(getIndex() - 1, imageWidth);
        }
    }

    protected ImagePoint getBottomNeighbor() {
        if (getY() == imageHeight - 1) {
            return null;
        } else {
            return new ImagePoint(getIndex() + imageWidth, imageWidth);
        }
    }

    protected ImagePoint getRightNeighbor() {
        if (getX() == imageWidth - 1) {
            return null;
        } else {
            return new ImagePoint(getIndex() + 1, imageWidth);
        }
    }
}
