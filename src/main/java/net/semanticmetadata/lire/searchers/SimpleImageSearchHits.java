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
 * Updated: 02.06.13 11:27
 */

package net.semanticmetadata.lire.searchers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This file is part of the LIRE project: http://www.SemanticMetadata.net
 * <br>Date: 02.02.2006
 * <br>Time: 23:56:15
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimpleImageSearchHits implements ImageSearchHits {
    ArrayList<SimpleResult> results;

    public SimpleImageSearchHits(Collection<SimpleResult> results, double maxDistance) {
        this.results = new ArrayList<SimpleResult>(results.size());
        this.results.addAll(results);
        // this step normalizes and inverts the distance ...
        // although its now a score or similarity like measure its further called distance
//        for (Iterator<SimpleResult> iterator = this.results.iterator(); iterator.hasNext(); ) {
//            SimpleResult result = iterator.next();
//            // result.setDistance(1f - result.getDistance() / maxDistance);
//        }
    }

    /**
     * Basic constructor to create results.
     *
     * @param results
     * @param maxDistance
     * @param useSimilarityScore set to true is you want similarity scores, otherwise distances will be used. Note that using distance is faster in terms of runtime.
     */
    public SimpleImageSearchHits(Collection<SimpleResult> results, double maxDistance, boolean useSimilarityScore) {
        this.results = new ArrayList<SimpleResult>(results.size());
        this.results.addAll(results);
        // this step normalizes and inverts the distance ...
        // although its now a score or similarity like measure its further called distance
        for (Iterator<SimpleResult> iterator = this.results.iterator(); iterator.hasNext(); ) {
            SimpleResult result = iterator.next();
            if (useSimilarityScore) result.setDistance(1d - result.getDistance() / maxDistance);
        }
    }

    /**
     * Returns the size of the result list.
     *
     * @return the size of the result list.
     */
    public int length() {
        return results.size();
    }

    /**
     * Returns the score of the document at given position.
     * Please note that the score in this case is a distance,
     * which means a score of 0 denotes the best possible hit.
     * The result list starts with position 0 as everything
     * in computer science does.
     *
     * @param position defines the position
     * @return the score of the document at given position. The lower the better (its a distance measure).
     */
    public double score(int position) {
        return results.get(position).getDistance();
    }

//    /**
//     * Returns the document at given position
//     *
//     * @param position defines the position.
//     * @return the document at given position.
//     */
//    public Document doc(int position) {
//        return results.get(position).getDocument();
//    }

    /**
     * Returns the id of the document within the respective Lucene IndexReader
     *
     * @param position position in the result list
     * @return the id in the IndexReader.
     */
    public int documentID(int position) {
        return results.get(position).getIndexNumber();
    }

//    public String path(int position) {
//        return results.get(position).getPath();
//    }

    @SuppressWarnings("unused")
    private double sigmoid(float f) {
        double result = 0d;
        result = -1d + 2d / (1d + Math.exp(-2d * f / 0.6));
        return (1d - result);
    }
}
