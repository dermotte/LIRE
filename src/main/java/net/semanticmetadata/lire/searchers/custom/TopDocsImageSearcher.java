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
package net.semanticmetadata.lire.searchers.custom;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.SimpleImageSearchHits;
import net.semanticmetadata.lire.searchers.SimpleResult;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Bits;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 01.02.2006
 * <br>Time: 00:17:02
 *
 * TODO: revisit for performance, feature caching, etc.
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TopDocsImageSearcher {
    protected Logger logger = Logger.getLogger(getClass().getName());
    Class<?> descriptorClass;
    String fieldName;

    private int maxHits = 10;
    protected TreeSet<SimpleResult> docs;

    public TopDocsImageSearcher(int maxHits, Class<?> descriptorClass, String fieldName) {
        this.maxHits = maxHits;
        docs = new TreeSet<SimpleResult>();
        this.descriptorClass = descriptorClass;
        this.fieldName = fieldName;
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader, TopDocs results) throws IOException {
        logger.finer("Starting extraction.");
        GlobalFeature globalFeature = null;
        SimpleImageSearchHits searchHits = null;
        try {
            globalFeature = (GlobalFeature) descriptorClass.newInstance();
            // Scaling image is especially with the correlogram features very important!
            BufferedImage bimg = image;
            if (Math.max(image.getHeight(), image.getWidth()) > DocumentBuilder.MAX_IMAGE_DIMENSION) {
                bimg = ImageUtils.scaleImage(image, DocumentBuilder.MAX_IMAGE_DIMENSION);
            }
            globalFeature.extract(bimg);
            logger.fine("Extraction from image finished");

            double maxDistance = findSimilar(results, reader, globalFeature);
            searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        }
        return searchHits;
    }

    /**
     * @param results
     * @param reader
     * @param globalFeature
     * @return the maximum distance found for normalizing.
     * @throws java.io.IOException
     */
    protected double findSimilar(TopDocs results, IndexReader reader, GlobalFeature globalFeature) throws IOException {
        double maxDistance = -1d, overallMaxDistance = -1d;
        boolean hasDeletions = reader.hasDeletions();

        // clear result set ...
        docs.clear();
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);

        int docs = results.totalHits;
        for (int i = 0; i < docs; i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.

            Document d = reader.document(results.scoreDocs[i].doc);
            double distance = getDistance(d, globalFeature);
            assert (distance >= 0);
            // calculate the overall max distance to normalize score afterwards
            if (overallMaxDistance < distance) {
                overallMaxDistance = distance;
            }
            // if it is the first document:
            if (maxDistance < 0) {
                maxDistance = distance;
            }
            // if the array is not full yet:
            if (this.docs.size() < maxHits) {
                this.docs.add(new SimpleResult(distance, results.scoreDocs[i].doc));
                if (distance > maxDistance) maxDistance = distance;
            } else if (distance < maxDistance) {
                // if it is nearer to the sample than at least on of the current set:
                // remove the last one ...
                this.docs.remove(this.docs.last());
                // add the new one ...
                this.docs.add(new SimpleResult(distance, results.scoreDocs[i].doc));
                // and set our new distance border ...
                maxDistance = this.docs.last().getDistance();
            }
        }
        return maxDistance;
    }

    protected double getDistance(Document d, GlobalFeature globalFeature) {
        double distance = 0d;
        GlobalFeature lf;
        try {
            lf = (GlobalFeature) descriptorClass.newInstance();
            lf.setByteArrayRepresentation(d.getField(fieldName).binaryValue().bytes, d.getField(fieldName).binaryValue().offset, d.getField(fieldName).binaryValue().length);
            distance = globalFeature.getDistance(lf);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        }

        return distance;
    }

    public ImageSearchHits search(TopDocs results, Document d, IndexReader reader) throws IOException {
        SimpleImageSearchHits searchHits = null;
        try {
            GlobalFeature lf;// = (GlobalFeature) descriptorClass.newInstance();

            lf = (GlobalFeature) descriptorClass.newInstance();
            lf.setByteArrayRepresentation(d.getField(fieldName).binaryValue().bytes, d.getField(fieldName).binaryValue().offset, d.getField(fieldName).binaryValue().length);
            double maxDistance = findSimilar(results, reader, lf);

            searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
        } catch (InstantiationException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        } catch (IllegalAccessException e) {
            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
        }
        return searchHits;
    }

    public String toString() {
        return "TopDocsImageSearcher using " + descriptorClass.getName();
    }

}
