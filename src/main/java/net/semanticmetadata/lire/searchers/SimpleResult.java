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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 02.06.13 11:03
 */

package net.semanticmetadata.lire.searchers;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 03.02.2006
 * <br>Time: 00:02:27
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimpleResult implements Comparable<SimpleResult> {
    //    private Document document;
    private double distance = -1d;
    private int indexNumber = -1;

    /**
     * Constructor for a result. The indexNumer is needed for sorting issues. Problem is that the TreeMap used for
     * collecting the results considers equality of objects based on the compareTo function. So if an image is in
     * the index twice, it's only found one time, the second instance -- with the same distance, but a
     * different Lucene document -- is not added to the TreeMap at runtime as their distance between each other
     * would be 0. This is tweaked with the running number of the document from the index, so duplicate documents that
     * are in the index twice, appear in the result list in the order they are found in the index. See also compareTo(...)
     * method.
     * @param distance the actual distance to the query
    //     * @param document the document instance form the Lucene index
     * @param indexNumber the running number from the IndexReader. Needed for sorting issues in the result TreeMap.
     */
    public SimpleResult(double distance, int indexNumber) {
        this.distance = distance;
        this.indexNumber = indexNumber;
    }

    public double getDistance() {
        assert (distance >= 0);
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }

    public int getIndexNumber() {
        return indexNumber;
    }


//    public Document getDocument() {
//        return document;
//    }

//    public void setDocument(Document document) {
//        this.document = document;
//    }

    /**
     * Compare the distance values to allow sorting in a tree map. If the distance value is the same, but the document
     * is different, the index number within the index is used to distinguishing the results. Otherwise the TreeMap
     * implementation wouldn't add the result.
     * @param o the SimpleResult to compare the current one to.
     * @return -1, 0, or 1
     */
    public int compareTo(SimpleResult o) {
        int compareValue = (int) Math.signum(distance - ((SimpleResult) o).distance);
        if (compareValue==0 && indexNumber != o.indexNumber) {
            return (int) Math.signum(indexNumber-o.indexNumber);
        }
        return compareValue;
    }

    @Override
    public boolean equals(Object obj) {
        // it's not the same if it's not the same class.
        if (! (obj instanceof SimpleResult)) return false;
            // it's the same if the document is the same, regardless of the distance.
//        else return (document.equals(((SimpleResult)obj).document) && indexNumber == ((SimpleResult)obj).indexNumber);
        else return (indexNumber == ((SimpleResult)obj).indexNumber);
    }
}