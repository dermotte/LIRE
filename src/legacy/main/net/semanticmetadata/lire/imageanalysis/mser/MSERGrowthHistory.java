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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * The GrowthHistory holds the information for ONE Extremal Region!!
 * <p/>
 * User: Shotty
 * Date: 28.06.2010
 * Time: 11:04:17
 */
public class MSERGrowthHistory implements Comparable {
    int index;
    int size;
    int maxGreyValue;
    LinkedImagePoint head;
    ImagePoint[] points = null;
    ImagePoint[] borderPoints = null;

    MSERGrowthHistory parent;


    public MSERGrowthHistory(int size, int value, LinkedImagePoint head) {
        this.size = size;
        this.maxGreyValue = value;

        this.head = head;
        this.parent = this;
    }

    public int getSize() {
        return size;
    }

    public ImagePoint[] getPoints() {
        if (points == null) {
            points = new ImagePoint[size];
            LinkedImagePoint temp = head;
            for (int i = 0; i < size; i++) {
                points[i] = temp.getPoint();
                temp = temp.getNext();

            }
        }
        return points;
    }

    /**
     * Calculate the border from all the points in the shape
     *
     * @return only the points on the border of the shape
     */
    public ImagePoint[] getBorderPoints(int width, int height) {
        if (borderPoints == null) {
            // point with the smallest index is topLeft
            ImagePoint topLeft = null;

            HashMap<String, ImagePoint> imagePoints = new HashMap<String, ImagePoint>();

            // fill the hash map
            for (ImagePoint p : getPoints()) {
                if (topLeft == null || topLeft.getIndex() > p.getIndex()) {
                    topLeft = p;
                }
                imagePoints.put(p.getX() + "_" + p.getY(), p);
            }

            List<ImagePoint> boundary = new ArrayList<ImagePoint>();

            // begin with topLeft, which is obviously part of the boundary
            boundary.add(topLeft);

            //examine neighbours in a counterclockwise direction
            BoundaryPixel8Edge currentPoint = new BoundaryPixel8Edge(topLeft, width, height, false, imagePoints);

            BoundaryPixel8Edge neighbor = currentPoint.getNextBoundary();

            // set the stop conditions
            ImagePoint stopConditionCurrentPoint = currentPoint.getPoint();
            ImagePoint stopConditionNeighborPoint = neighbor.getPoint();

            // add the neighbor to the boundary pixels
            boundary.add(imagePoints.get(neighbor.getX() + "_" + neighbor.getY()));
            // where did the edge come from
            neighbor.setCurrentEdge(currentPoint.getNeighbourEdge());
            // neighbor is now current point
            currentPoint = neighbor;
            // get new neighbor
            neighbor = currentPoint.getNextBoundary();

            while (stopConditionCurrentPoint.getIndex() != currentPoint.getIndex() ||
                    stopConditionNeighborPoint.getIndex() != neighbor.getIndex()) {
                // add the neighbor to the boundary pixels
                boundary.add(imagePoints.get(neighbor.getX() + "_" + neighbor.getY()));

                // where did the edge come from
                neighbor.setCurrentEdge(currentPoint.getNeighbourEdge());
                // neighbor is now current point
                currentPoint = neighbor;
                // get new neighbor
                neighbor = currentPoint.getNextBoundary();
            }

            // -1 because start == last
            borderPoints = new ImagePoint[boundary.size()];
            boundary.toArray(borderPoints);
        }


        return borderPoints;

    }

    public MSERGrowthHistory getParent() {
        return parent;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

    public int compareTo(Object o) {
        if (maxGreyValue < ((MSERGrowthHistory) o).maxGreyValue) {
            return -1;
        } else if (maxGreyValue > ((MSERGrowthHistory) o).maxGreyValue) {
            return 1;
        }
        return 0;
    }


    public static void main(String[] args) {
        int[] points = new int[]{12,
                21, 22, 23, 25,
                30, 31, 32, 33, 34, 35, 36, 38, 39,
                40, 41, 42, 43, 44, 45, 48,
                50, 51, 53, 54, 55, 56, 58, 59,
                63, 64, 65, 66, 67, 68,
                71, 72, 73, 74, 75, 76,
                80, 81, 82, 83, 84,
                91, 92, 93
        };

        int width = 10;

        LinkedImagePoint head = new LinkedImagePoint(new ImagePoint(points[0], width));
        LinkedImagePoint last = head;
        LinkedImagePoint current;

        for (int i = 1; i < points.length; i++) {
            current = new LinkedImagePoint(new ImagePoint(points[i], width));
            last.setNext(current);
            current.setPrev(last);
            last = current;
        }
        MSERGrowthHistory test = new MSERGrowthHistory(49, 36, head);

        ImagePoint[] border = test.getBorderPoints(10, 10);

        String borderPoints = "";
        for (ImagePoint p : border) {
            borderPoints += " " + p.getIndex();
        }

        System.out.println(borderPoints);

        // expected border:
        String expectedBorderPoints = " 12 21 30 40 50 51 42 53 63 72 71 80 91 92 93 84 75 76 67 68 59 48 39 38 48 58 67 56 45 36 25 34 23 12";

        System.out.println(expectedBorderPoints);

        System.out.println("SAME =" + ((borderPoints.equals(expectedBorderPoints) ? "true" : "false")));

        points = new int[]
                {23, 26, 27, 31, 32, 33, 34, 35, 36, 37, 44, 45, 46, 47, 55, 56, 57, 65, 66};

        width = 10;

        head = new LinkedImagePoint(new ImagePoint(points[0], width));
        last = head;

        for (int i = 1; i < points.length; i++) {
            current = new LinkedImagePoint(new ImagePoint(points[i], width));
            last.setNext(current);
            current.setPrev(last);
            last = current;
        }

        test = new MSERGrowthHistory(19, 36, head);

        border = test.getBorderPoints(width, 10);

        borderPoints = "";
        for (ImagePoint p : border) {
            borderPoints += " " + p.getIndex();
        }

        System.out.println(borderPoints);

        // expected border:
        expectedBorderPoints = " 23 32 31 32 33 44 55 65 66 57 47 37 27 26 35 34 23";

        System.out.println(expectedBorderPoints);

        System.out.println("SAME =" + ((borderPoints.equals(expectedBorderPoints) ? "true" : "false")));
    }
}
