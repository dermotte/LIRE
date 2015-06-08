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
 * Updated: 23.06.13 18:16
 */
package net.semanticmetadata.lire.searchers;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.OpponentHistogram;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.util.Bits;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 01.02.2006
 * <br>Time: 00:17:02
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FastOpponentImageSearcher extends AbstractImageSearcher {
    protected Logger logger = Logger.getLogger(getClass().getName());
    private OpponentHistogram cachedInstance = null;

    private int maxHits = 10;
    protected TreeSet<SimpleResult> docs;
    private byte[] tempBinaryValue;
    private double maxDistance;
    private float overallMaxDistance;

    public FastOpponentImageSearcher(int maxHits) {
        this.maxHits = maxHits;
        docs = new TreeSet<SimpleResult>();
        this.cachedInstance = new OpponentHistogram();
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        logger.finer("Starting extraction.");
        OpponentHistogram globalFeature = null;
        SimpleImageSearchHits searchHits = null;
        globalFeature = new OpponentHistogram();
        // Scaling image is especially with the correlogram features very important!
        BufferedImage bimg = image;
        if (Math.max(image.getHeight(), image.getWidth()) > DocumentBuilder.MAX_IMAGE_DIMENSION) {
            bimg = ImageUtils.scaleImage(image, DocumentBuilder.MAX_IMAGE_DIMENSION);
        }
        globalFeature.extract(bimg);
        logger.fine("Extraction from image finished");

        double maxDistance = findSimilar(reader, globalFeature);
        searchHits = new SimpleImageSearchHits(this.docs, (float) maxDistance);
        return searchHits;
    }

    /**
     * @param reader
     * @param globalFeature
     * @return the maximum distance found for normalizing.
     * @throws java.io.IOException
     */
    protected double findSimilar(IndexReader reader, GlobalFeature globalFeature) throws IOException {
        maxDistance = -1f;
        // clear result set ...
        docs.clear();
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        Document d;
        double tmpDistance;
        int docs = reader.numDocs();
        byte[] histogram = globalFeature.getByteArrayRepresentation();
        for (int i = 0; i < docs; i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.

            d = reader.document(i);
            tmpDistance = getDistance(d, histogram);
            assert (tmpDistance >= 0);
            // calculate the overall max distance to normalize score afterwards
//            if (overallMaxDistance < tmpDistance) {
//                overallMaxDistance = tmpDistance;
//            }
            // if it is the first document:
            if (maxDistance < 0) {
                maxDistance = tmpDistance;
            }
            // if the array is not full yet:
            if (this.docs.size() < maxHits) {
                this.docs.add(new SimpleResult( tmpDistance, i));
                if (tmpDistance > maxDistance) maxDistance = tmpDistance;
            } else if (tmpDistance < maxDistance) {
                // if it is nearer to the sample than at least on of the current set:
                // remove the last one ...
                this.docs.remove(this.docs.last());
                // add the new one ...
                this.docs.add(new SimpleResult(tmpDistance, i));
                // and set our new distance border ...
                maxDistance = this.docs.last().getDistance();
            }
        }
        return maxDistance;
    }

    /**
     * Main similarity method called for each and every document in the index.
     *
     * @param document
     * @param histogram
     * @return the distance between the given feature and the feature stored in the document.
     */
    protected double getDistance(Document document, byte[] histogram) {
        if (document.getField(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).binaryValue() != null && document.getField(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).binaryValue().length > 0) {
            return cachedInstance.getDistance(histogram, 0, histogram.length,
                    document.getField(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).binaryValue().bytes,
                    document.getField(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).binaryValue().offset,
                    document.getField(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).binaryValue().length);
        } else {
            logger.warning("No feature stored in this document!");
        }
        return 0d;
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        SimpleImageSearchHits searchHits = null;
        OpponentHistogram globalFeature = new OpponentHistogram();

        if (doc.getField(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).binaryValue() != null && doc.getField(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).binaryValue().length > 0)
            globalFeature.setByteArrayRepresentation(doc.getField(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).binaryValue().bytes,
                    doc.getField(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).binaryValue().offset,
                    doc.getField(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).binaryValue().length);
        double maxDistance = findSimilar(reader, globalFeature);

        searchHits = new SimpleImageSearchHits(this.docs, (float) maxDistance);
        return searchHits;
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("not implemented");
    }

    public String toString() {
        return getClass().getName();
    }

}
