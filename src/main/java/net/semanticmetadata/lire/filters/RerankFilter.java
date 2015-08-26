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

package net.semanticmetadata.lire.filters;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.SearchHitsFilter;
import net.semanticmetadata.lire.searchers.SimpleImageSearchHits;
import net.semanticmetadata.lire.searchers.SimpleResult;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
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

    public ImageSearchHits filter(ImageSearchHits results, IndexReader reader, Document query) {
        GlobalFeature queryFeature = null;
        GlobalFeature tempFeature = null;
        double distance = 0, maxDistance = 0;
        TreeSet<SimpleResult> resultSet = new TreeSet<SimpleResult>();

        // create our feature classes
        try {
            queryFeature = (GlobalFeature) featureClass.newInstance();
            tempFeature = (GlobalFeature) featureClass.newInstance();
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
            Document d = null;
            try {
                d = reader.document(results.documentID(x));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (d.getField(fieldName) != null) {
                tempFeature.setByteArrayRepresentation(d.getField(fieldName).binaryValue().bytes,
                        d.getField(fieldName).binaryValue().offset,
                        d.getField(fieldName).binaryValue().length);
                distance = queryFeature.getDistance(tempFeature);
                maxDistance = Math.max(maxDistance, distance);
                resultSet.add(new SimpleResult(distance, results.documentID(x)));
            } else {
                logger.info("Could not instantiate class " + featureClass.getName() + " from the given result set.");
            }
        }
        return new SimpleImageSearchHits(resultSet, maxDistance);
    }

    @Override
    public ImageSearchHits filter(TopDocs results, IndexReader reader, Document query) throws IOException {
        GlobalFeature queryFeature = null;
        GlobalFeature tempFeature = null;
        double distance = 0, maxDistance = 0;
        TreeSet<SimpleResult> resultSet = new TreeSet<SimpleResult>();

        // create our feature classes
        try {
            queryFeature = (GlobalFeature) featureClass.newInstance();
            tempFeature = (GlobalFeature) featureClass.newInstance();
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
        ScoreDoc[] scoreDocs = results.scoreDocs;
        Document tmp = null;
        for (int x = 0; x < scoreDocs.length; x++) {
            tmp =reader.document(scoreDocs[x].doc);
            if (tmp.getField(fieldName) != null) {
                tempFeature.setByteArrayRepresentation(tmp.getField(fieldName).binaryValue().bytes,
                        tmp.getField(fieldName).binaryValue().offset,
                        tmp.getField(fieldName).binaryValue().length);
                distance = queryFeature.getDistance(tempFeature);
                maxDistance = Math.max(maxDistance, distance);
                resultSet.add(new SimpleResult(distance, scoreDocs[x].doc));
            } else {
                logger.info("Could not instantiate class " + featureClass.getName() + " from the given result set.");
            }
        }
        return new SimpleImageSearchHits(resultSet, maxDistance);
    }
}
