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

import java.util.HashMap;

/**
 * Boundary Pixel for boundary following algorithm.
 * <p/>
 * User: Shotty
 * Date: 01.05.11
 * Time: 11:04
 */
public class BoundaryPixel8Edge extends BoundaryPixel {
    public static final int BOTTOM_RIGHT_EDGE = 1;
    public static final int BOTTOM_LEFT_EDGE = 3;
    public static final int TOP_LEFT_EDGE = 5;
    public static final int TOP_RIGHT_EDGE = 7;
    public static final int MAX_EDGES = 8;

    private boolean clockwise;
    private int currentEdge;
    private HashMap<String, ImagePoint> imagePoints;

    /**
     * Constructs a Pixel which is abel to get the neighbouring pixels taking account of the boundaries.
     *
     * @param point       the inspected point
     * @param imageWidth  width of the image
     * @param imageHeight height of the image
     * @param clockwise   clockwise or counterclockwise inspection of the neighbours
     */
    public BoundaryPixel8Edge(ImagePoint point, int imageWidth, int imageHeight, boolean clockwise,
                              HashMap<String, ImagePoint> imagePoints) {
        super(point, imageWidth, imageHeight);
        this.clockwise = clockwise;
        this.nextEdge = LEFT_EDGE;
        this.currentEdge = NO_EDGE;
        this.imagePoints = imagePoints;
    }

    /**
     * Set the next Edge
     *
     * @param edge the EDGE to start from
     */
    public void setCurrentEdge(int edge) {
        this.currentEdge = edge;
        if (clockwise) {
            nextEdge = (currentEdge + 1) % MAX_EDGES;
        } else {
            nextEdge = (currentEdge > 0) ? (currentEdge - 1) : MAX_EDGES - 1;

        }
    }

    protected ImagePoint getTopLeftNeighbor() {
        if (getY() == 0 || getX() == 0) {
            return null;
        } else {
            return new ImagePoint(getIndex() - imageWidth - 1, imageWidth);
        }
    }

    protected ImagePoint getTopRightNeighbor() {
        if (getY() == 0 || getX() == imageWidth - 1) {
            return null;
        } else {
            return new ImagePoint(getIndex() - imageWidth + 1, imageWidth);
        }
    }

    protected ImagePoint getBottomLeftNeighbor() {
        if (getY() == imageHeight - 1 || getX() == 0) {
            return null;
        } else {
            return new ImagePoint(getIndex() + imageWidth - 1, imageWidth);
        }
    }

    protected ImagePoint getBottomRightNeighbor() {
        if (getY() == imageHeight - 1 || getX() == imageWidth - 1) {
            return null;
        } else {
            return new ImagePoint(getIndex() + imageWidth + 1, imageWidth);
        }
    }

    protected ImagePoint setNextEdge(ImagePoint nextEdgePoint) {
        currentEdge = nextEdge;
        if (clockwise) {
            nextEdge = (nextEdge + 1) % MAX_EDGES;
        } else {
            nextEdge = (nextEdge > 0) ? (nextEdge - 1) : MAX_EDGES - 1;

        }
        if (nextEdgePoint != null && imagePoints.containsKey(nextEdgePoint.getX() + "_" + nextEdgePoint.getY()))
        // in boundary && part of the shape
        {
            return nextEdgePoint;
        } else  // not in boundary, calc the next edge
        {
            return calcNextEdge();
        }
    }

    @Override
    public ImagePoint calcNextEdge() {
        ImagePoint nextEdgePoint;
        switch (nextEdge) {
            case RIGHT_EDGE:
                // try the get the right edge of the current pixel
                nextEdgePoint = getRightNeighbor();
                return setNextEdge(nextEdgePoint);
            case BOTTOM_RIGHT_EDGE:
                // try to get the right bottom edge of the current pixel
                nextEdgePoint = getBottomRightNeighbor();
                return setNextEdge(nextEdgePoint);
            case BOTTOM_EDGE:
                // try the get the bottom edge of the current pixel
                nextEdgePoint = getBottomNeighbor();
                return setNextEdge(nextEdgePoint);
            case BOTTOM_LEFT_EDGE:
                // try to get the left bottom edge of the current pixel
                nextEdgePoint = getBottomLeftNeighbor();
                return setNextEdge(nextEdgePoint);
            case LEFT_EDGE:
                // try to get the left edge of the current pixel
                nextEdgePoint = getLeftNeighbor();
                return setNextEdge(nextEdgePoint);
            case TOP_LEFT_EDGE:
                // try to get the top left edge of the current pixel
                nextEdgePoint = getTopLeftNeighbor();
                return setNextEdge(nextEdgePoint);
            case TOP_EDGE:
                // try to get the top edge of the current pixel
                nextEdgePoint = getTopNeighbor();
                return setNextEdge(nextEdgePoint);
            case TOP_RIGHT_EDGE:
                // try to get the top left edge of the current pixel
                nextEdgePoint = getTopRightNeighbor();
                return setNextEdge(nextEdgePoint);
            default:
                nextEdge = NO_EDGE;
                return null; // all edges done
        }
    }

    public int getNeighbourEdge() {
        // which edge is this from the neighbours view
        // always the diagonal opposite of the current edge;
        switch (currentEdge) {
            case TOP_EDGE:
                return BOTTOM_EDGE;
            case TOP_RIGHT_EDGE:
                return BOTTOM_LEFT_EDGE;
            case RIGHT_EDGE:
                return LEFT_EDGE;
            case BOTTOM_RIGHT_EDGE:
                return TOP_LEFT_EDGE;
            case BOTTOM_EDGE:
                return TOP_EDGE;
            case BOTTOM_LEFT_EDGE:
                return TOP_RIGHT_EDGE;
            case LEFT_EDGE:
                return RIGHT_EDGE;
            case TOP_LEFT_EDGE:
                return BOTTOM_RIGHT_EDGE;
        }
        return NO_EDGE;
    }

    /**
     * Explore the next Edge of the Pixel.
     * Returns null if all edges have been explored for this pixel
     *
     * @return the BoundaryPixel of the next Neighbour
     */
    public BoundaryPixel8Edge getNextBoundary() {
        ImagePoint nextEdge = calcNextEdge();
        if (nextEdge != null) {
            return new BoundaryPixel8Edge(nextEdge, imageWidth, imageHeight, clockwise, imagePoints);
        } else {
            return null;
        }
    }
}
