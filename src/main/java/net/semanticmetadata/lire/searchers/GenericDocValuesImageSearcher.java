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
 * Updated: 18.01.15 07:31
 */
package net.semanticmetadata.lire.searchers;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.indexers.parallel.ExtractorItem;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.util.Bits;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * A generic image searcher for global features that uses DocValues instead of Lucene text fields. Please make sure you
 * created the index using th useDocValues option in
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class GenericDocValuesImageSearcher extends AbstractImageSearcher {
    protected Logger logger = Logger.getLogger(getClass().getName());
    protected String fieldName;
    protected LireFeature cachedInstance = null;
    protected ExtractorItem extractorItem;

    protected IndexReader reader = null;

    protected int maxHits = 50;
    protected TreeSet<SimpleResult> docs = new TreeSet<SimpleResult>();
    protected double maxDistance;
    protected boolean useSimilarityScore = false;

    private BinaryDocValues docValues = null;


    public GenericDocValuesImageSearcher(int maxHits, Class<? extends GlobalFeature> globalFeature, IndexReader reader) {
        this.maxHits = maxHits;
        this.extractorItem = new ExtractorItem(globalFeature);
        this.fieldName = extractorItem.getFieldName();
        try {
            this.cachedInstance = (GlobalFeature) extractorItem.getExtractorInstance().getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.reader = reader;
        init();
    }

    protected void init() {
        // put all respective features into an in-memory cache ...
        if (reader != null) {
            try {
                docValues = MultiDocValues.getBinaryValues(reader, cachedInstance.getFieldName());
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }


    /**
     * @param lireFeature
     * @return the maximum distance found for normalizing.
     * @throws IOException
     */
    protected double findSimilar(LireFeature lireFeature) throws IOException {
        maxDistance = -1d;

        // clear result set ...
        docs.clear();
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        Document d;
        double tmpDistance;
        int docs = reader.numDocs();
        // we read each and every document from the index and then we compare it to the query.
        for (int i = 0; i < docs; i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.

            cachedInstance.setByteArrayRepresentation(docValues.get(i).bytes, docValues.get(i).offset, docValues.get(i).length);

            tmpDistance = cachedInstance.getDistance(lireFeature);
            assert (tmpDistance >= 0);
            // if the array is not full yet:
            if (this.docs.size() < maxHits) {
                this.docs.add(new SimpleResult(tmpDistance, i));
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


    // This is an approach based on DocValues. It's extremely fast, even without caching, but I don't know if it's in
    // RAM or not, ie. if I can fill up RAM with all documents at once.
    public ImageSearchHits search(int doc) throws IOException {
        SimpleImageSearchHits searchHits = null;
        LireFeature lireFeature = extractorItem.getFeatureInstance();
//        BinaryDocValues binaryValues = MultiDocValues.getBinaryValues(reader, lireFeature.getFieldName());
        lireFeature.setByteArrayRepresentation(docValues.get(doc).bytes, docValues.get(doc).offset, docValues.get(doc).length);
        double maxDistance = findSimilar(lireFeature);

        if (!useSimilarityScore) {
            searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
        } else {
            searchHits = new SimpleImageSearchHits(this.docs, maxDistance, useSimilarityScore);
        }
        return searchHits;
    }


    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        IndexSearcher is = new IndexSearcher(reader);
        TermQuery tq = new TermQuery(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, doc.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]));
        TopDocs topDocs = is.search(tq, 1);
        if (topDocs.totalHits > 0) {
            return search(topDocs.scoreDocs[0].doc);
        } else return null;
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        logger.finer("Starting extraction.");
        SimpleImageSearchHits searchHits = null;

        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder();
        GlobalFeature globalFeature = globalDocumentBuilder.extractGlobalFeature(image, (GlobalFeature) extractorItem.getExtractorInstance());

        double maxDistance = findSimilar(globalFeature);
        if (!useSimilarityScore) {
            searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
        } else {
            searchHits = new SimpleImageSearchHits(this.docs, maxDistance, useSimilarityScore);
        }

        return searchHits;

    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("not implemented yet.");

    }

    public String toString() {
        return "GenericDocValuesImageSearcher using " + extractorItem.getExtractorClass().getName();
    }

}
