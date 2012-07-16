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

package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.ImageSearchHits;
import org.apache.lucene.document.Document;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 02.02.2006
 * <br>Time: 23:56:15
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SimpleImageSearchHits implements ImageSearchHits {
    ArrayList<SimpleResult> results;

    public SimpleImageSearchHits(Collection<SimpleResult> results, float maxDistance) {
        this.results = new ArrayList<SimpleResult>(results.size());
        this.results.addAll(results);
        // this step normalizes and inverts the distance ...
        // although its now a score or similarity like measure its further called distance
        for (Iterator<SimpleResult> iterator = this.results.iterator(); iterator.hasNext(); ) {
            SimpleResult result = iterator.next();
            result.setDistance(1f - result.getDistance() / maxDistance);
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
    public float score(int position) {
        return results.get(position).getDistance();
    }

    /**
     * Returns the document at given position
     *
     * @param position defines the position.
     * @return the document at given position.
     */
    public Document doc(int position) {
        return results.get(position).getDocument();
    }

    private float sigmoid(float f) {
        double result = 0f;
        result = -1d + 2d / (1d + Math.exp(-2d * f / 0.6));
        return (float) (1d - result);
    }
}
