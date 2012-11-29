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

package net.semanticmetadata.lire.filter;

import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.SearchHitsFilter;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.impl.SimpleImageSearchHits;
import net.semanticmetadata.lire.impl.SimpleResult;
import org.apache.lucene.document.Document;

import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * A result list is re-ranked based on the given query. Example usage is: you first do a search with
 * feature X and then re-rank the results with feature Y.
 * Created 03.08.11, 10:28 <br/>
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class RerankFilter implements SearchHitsFilter {
    private Logger logger = Logger.getLogger(getClass().getName());
    private Class featureClass;
    private String fieldName;

    public RerankFilter(Class featureClass, String fieldName) {
        this.featureClass = featureClass;
        this.fieldName = fieldName;
    }

    public ImageSearchHits filter(ImageSearchHits results, Document query) {
        LireFeature queryFeature = null;
        LireFeature tempFeature = null;
        float distance = 0, maxDistance = 0;
        TreeSet<SimpleResult> resultSet = new TreeSet<SimpleResult>();

        // create our feature classes
        try {
            queryFeature = (LireFeature) featureClass.newInstance();
            tempFeature = (LireFeature) featureClass.newInstance();
        } catch (Exception e) {
            logger.severe("Could not instantiate class " + featureClass.getName() + " in " + getClass().getName() + " (" + e.getMessage() + ").");
            return null;
        }

        // check if features are there and compatible.
        if (query.getField(fieldName) != null) {
            queryFeature.setByteArrayRepresentation(query.getField(fieldName).binaryValue().bytes,
                    query.getField(fieldName).binaryValue().offset,
                    query.getField(fieldName).binaryValue().length);
        } else {
            logger.severe("Given feature class " + featureClass.getName() + " is not available in the query document (" + getClass().getName() + ").");
            return null;
        }

        for (int x = 0; x < results.length(); x++) {
            if (results.doc(x).getField(fieldName) != null) {
                tempFeature.setByteArrayRepresentation(results.doc(x).getField(fieldName).binaryValue().bytes,
                        results.doc(x).getField(fieldName).binaryValue().offset,
                        results.doc(x).getField(fieldName).binaryValue().length);
                distance = queryFeature.getDistance(tempFeature);
                maxDistance = Math.max(maxDistance, distance);
                resultSet.add(new SimpleResult(distance, results.doc(x)));
            } else {
                logger.info("Could not instantiate class " + featureClass.getName() + " from the given result set.");
            }
        }
        return new SimpleImageSearchHits(resultSet, maxDistance);
    }
}
