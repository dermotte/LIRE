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
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 16.04.13 18:32
 */

package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;

/**
 * This class allows for searching based on {@link net.semanticmetadata.lire.indexing.hashing.BitSampling}
 * Hashing. First a number of candidates is retrieved from the index, then the candidates are re-ranked.
 * The number of candidates can be tuned with the numHashedResults parameter in the constructor. The higher
 * this parameter, the better the results, but the slower the search.
 * @author Mathias Lux, mathias@juggle.at, 2013-04-12
 */

public class BitSamplingImageSearcher extends AbstractImageSearcher {
    private int maxResultsHashBased = 1000;
    private int maximumHits = 100;
    private String featureFieldName = DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM;
    private LireFeature feature;
    private String hashesFieldName = "Hashes";

    /**
     * Creates a new searcher for BitSampling based hashes.
     * @param maximumHits how many hits the searcher shall return.
     * @param featureFieldName the field name of the feature.
     * @param hashesFieldName the field name of the hashes.
     * @param feature an instance of the feature.
     */
    public BitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, LireFeature feature) {
        this.maximumHits = maximumHits;
        this.featureFieldName = featureFieldName;
        this.hashesFieldName = hashesFieldName;
        this.feature = feature;
        try {
            BitSampling.readHashFunctions();
        } catch (IOException e) {
            System.err.println("Error reading hash functions from default location.");
            e.printStackTrace();
        }
    }

    public BitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, LireFeature feature, int numHashedResults) {
        this.maximumHits = maximumHits;
        this.featureFieldName = featureFieldName;
        this.hashesFieldName = hashesFieldName;
        this.feature = feature;
        maxResultsHashBased = numHashedResults;
        try {
            BitSampling.readHashFunctions();
        } catch (IOException e) {
            System.err.println("Error reading hash functions from default location.");
            e.printStackTrace();
        }
    }

    public BitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, LireFeature feature, InputStream hashes) {
        this.maximumHits = maximumHits;
        this.featureFieldName = featureFieldName;
        this.hashesFieldName = hashesFieldName;
        this.feature = feature;
        try {
            BitSampling.readHashFunctions(hashes);
            hashes.close();
        } catch (IOException e) {
            System.err.println("Error reading has functions from given input stream.");
            e.printStackTrace();
        }
    }

    public BitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, LireFeature feature, InputStream hashes, int numHashedResults) {
        this.maximumHits = maximumHits;
        this.featureFieldName = featureFieldName;
        this.hashesFieldName = hashesFieldName;
        this.feature = feature;
        maxResultsHashBased = numHashedResults;
        try {
            BitSampling.readHashFunctions(hashes);
            hashes.close();
        } catch (IOException e) {
            System.err.println("Error reading has functions from given input stream.");
            e.printStackTrace();
        }
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        try {
            LireFeature queryFeature = feature.getClass().newInstance();
            queryFeature.extract(image);
            int[] ints = BitSampling.generateHashes(queryFeature.getDoubleHistogram());
            String[] hashes = new String[ints.length];
            for (int i = 0; i < ints.length; i++) {
                hashes[i] = Integer.toString(ints[i]);
            }
            return search(hashes, queryFeature, reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        try {
            LireFeature queryFeature = feature.getClass().newInstance();
            queryFeature.setByteArrayRepresentation(doc.getBinaryValue(featureFieldName).bytes,
                    doc.getBinaryValue(featureFieldName).offset,
                    doc.getBinaryValue(featureFieldName).length);
            return search(doc.getValues(hashesFieldName)[0].split(" "), queryFeature, reader);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ImageSearchHits search(String[] hashes, LireFeature queryFeature, IndexReader reader) throws IOException {
        // first search by text:
        IndexSearcher searcher = new IndexSearcher(reader);
        BooleanQuery query = new BooleanQuery();
        for (int i = 0; i < hashes.length; i++) {
            // be aware that the name of the field must match the one you put the hashes in before.
            query.add(new BooleanClause(new TermQuery(new Term(hashesFieldName, hashes[i] + "")), BooleanClause.Occur.SHOULD));
        }
        TopDocs docs = searcher.search(query, maxResultsHashBased);
        // then re-rank
        TreeSet<SimpleResult> resultScoreDocs = new TreeSet<SimpleResult>();
        float maxDistance = 0f;
        float tmpScore = 0f;
        for (int i = 0; i < docs.scoreDocs.length; i++) {
            feature.setByteArrayRepresentation(reader.document(docs.scoreDocs[i].doc).getBinaryValue(featureFieldName).bytes,
                    reader.document(docs.scoreDocs[i].doc).getBinaryValue(featureFieldName).offset,
                    reader.document(docs.scoreDocs[i].doc).getBinaryValue(featureFieldName).length);
            tmpScore = queryFeature.getDistance(feature);
            if (resultScoreDocs.size() < maximumHits) {
                resultScoreDocs.add(new SimpleResult(tmpScore, reader.document(docs.scoreDocs[i].doc), docs.scoreDocs[i].doc));
                maxDistance = Math.max(maxDistance, tmpScore);
            } else if (tmpScore < maxDistance) {
                resultScoreDocs.add(new SimpleResult(tmpScore, reader.document(docs.scoreDocs[i].doc), docs.scoreDocs[i].doc));
            }
            while (resultScoreDocs.size() > maximumHits) {
                resultScoreDocs.remove(resultScoreDocs.last());
                maxDistance = resultScoreDocs.last().getDistance();
            }
//            resultScoreDocs.add(new SimpleResult(tmpScore, reader.document(docs.scoreDocs[i].doc), docs.scoreDocs[i].doc));
        }
        return new SimpleImageSearchHits(resultScoreDocs, maxDistance);
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("not implemented.");
    }
}
