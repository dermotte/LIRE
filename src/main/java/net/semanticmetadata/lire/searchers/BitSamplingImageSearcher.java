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
 * Updated: 26.08.14 13:20
 */

package net.semanticmetadata.lire.searchers;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.TreeSet;

/**
 * This class allows for searching based on {@link net.semanticmetadata.lire.indexers.hashing.BitSampling}
 * HashingMode. First a number of candidates is retrieved from the index, then the candidates are re-ranked.
 * The number of candidates can be tuned with the numHashedResults parameter in the constructor. The higher
 * this parameter, the better the results, but the slower the search.
 *
 * @author Mathias Lux, mathias@juggle.at, 2013-04-12
 */

public class BitSamplingImageSearcher extends AbstractImageSearcher {
    private int maxResultsHashBased = 1000;
    private int maximumHits = 100;
    private String featureFieldName = null;
    private GlobalFeature feature = null;
    private String hashesFieldName = null;
    private boolean partialHashes = false;

    /**
     * Creates a new searcher for BitSampling based hashes.
     *
     * @param maximumHits      how many hits the searcher shall return.
     * @param featureFieldName the field hashFunctionsFileName of the feature.
     * @param hashesFieldName  the field hashFunctionsFileName of the hashes.
     * @param feature          an instance of the feature.
     */
    public BitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, GlobalFeature feature) {
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

    /**
     * Creates a new searcher for BitSampling based hashes. The field names are inferred from the entries in //TODO {@ link GenericDocumentBuilder}
     *
     * @param maximumHits how many hits the searcher shall return.
     * @param feature     an instance of the feature.
     */
    public BitSamplingImageSearcher(int maximumHits, GlobalFeature feature) {
        this.maximumHits = maximumHits;
        this.featureFieldName = feature.getFieldName();
        this.hashesFieldName = featureFieldName + DocumentBuilder.HASH_FIELD_SUFFIX;
        this.feature = feature;
        try {
            BitSampling.readHashFunctions();
        } catch (IOException e) {
            System.err.println("Error reading hash functions from default location.");
            e.printStackTrace();
        }
    }

    /**
     * Creates a new searcher for BitSampling based hashes. The field names are inferred from the entries in //TODO {@ link GenericDocumentBuilder}
     *
     * @param maximumHits how many hits the searcher shall return.
     * @param feature     an instance of the feature.
     * @param useFastSearch if true it only uses a random sample of hashes for the query and speeds up the search significantly.
     */
    public BitSamplingImageSearcher(int maximumHits, GlobalFeature feature, boolean useFastSearch) {
        this.maximumHits = maximumHits;
        featureFieldName = feature.getFieldName();
        hashesFieldName = featureFieldName + DocumentBuilder.HASH_FIELD_SUFFIX;
        this.feature = feature;
        partialHashes = useFastSearch;
        try {
            BitSampling.readHashFunctions();
        } catch (IOException e) {
            System.err.println("Error reading hash functions from default location.");
            e.printStackTrace();
        }
    }

    /**
     * Creates a new searcher for BitSampling based hashes. The field names are inferred from the entries in //TODO {@ link GenericDocumentBuilder}
     *
     * @param maximumHits      how many hits the searcher shall return.
     * @param feature          an instance of the feature.
     * @param numHashedResults the number of candidate results retrieved from the index before re-ranking.
     */
    public BitSamplingImageSearcher(int maximumHits, GlobalFeature feature, int numHashedResults) {
        this.maximumHits = maximumHits;
        this.featureFieldName = feature.getFieldName();
        this.hashesFieldName = featureFieldName + DocumentBuilder.HASH_FIELD_SUFFIX;
        this.feature = feature;
        maxResultsHashBased = numHashedResults;
        try {
            BitSampling.readHashFunctions();
        } catch (IOException e) {
            System.err.println("Error reading hash functions from default location.");
            e.printStackTrace();
        }
    }

    public BitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, GlobalFeature feature, int numHashedResults) {
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

    public BitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, GlobalFeature feature, InputStream hashes) {
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

    public BitSamplingImageSearcher(int maximumHits, String featureFieldName, String hashesFieldName, GlobalFeature feature, InputStream hashes, int numHashedResults) {
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
            GlobalFeature queryFeature = feature.getClass().newInstance();
            queryFeature.extract(image);
            int[] ints = BitSampling.generateHashes(queryFeature.getFeatureVector());
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
            GlobalFeature queryFeature = feature.getClass().newInstance();
            queryFeature.setByteArrayRepresentation(doc.getBinaryValue(featureFieldName).bytes,
                    doc.getBinaryValue(featureFieldName).offset,
                    doc.getBinaryValue(featureFieldName).length);
            return search(doc.getValues(hashesFieldName)[0].split(" "), queryFeature, reader);
//            return search(doc.getValues(hashesFieldName + "_q")[0].split(" "), queryFeature, reader);  // just for debug if a query feature is stored in the index.
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private ImageSearchHits search(String[] hashes, GlobalFeature queryFeature, IndexReader reader) throws IOException {
        // first search by text:
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new BaseSimilarity());
        BooleanQuery query = new BooleanQuery();
        for (int i = 0; i < hashes.length; i++) {
            // be aware that the hashFunctionsFileName of the field must match the one you put the hashes in before.
            if (partialHashes) {
                if (Math.random() < 0.5)
                    query.add(new BooleanClause(new TermQuery(new Term(hashesFieldName, hashes[i] + "")), BooleanClause.Occur.SHOULD));
            } else
                query.add(new BooleanClause(new TermQuery(new Term(hashesFieldName, hashes[i] + "")), BooleanClause.Occur.SHOULD));
        }
        TopDocs docs = searcher.search(query, maxResultsHashBased);
//        System.out.println(docs.totalHits);
        // then re-rank
        TreeSet<SimpleResult> resultScoreDocs = new TreeSet<SimpleResult>();
        double maxDistance = -1d;
        double tmpScore;
        for (int i = 0; i < docs.scoreDocs.length; i++) {
            feature.setByteArrayRepresentation(reader.document(docs.scoreDocs[i].doc).getBinaryValue(featureFieldName).bytes,
                    reader.document(docs.scoreDocs[i].doc).getBinaryValue(featureFieldName).offset,
                    reader.document(docs.scoreDocs[i].doc).getBinaryValue(featureFieldName).length);
            tmpScore = queryFeature.getDistance(feature);
            assert (tmpScore >= 0);
            if (resultScoreDocs.size() < maximumHits) {
                resultScoreDocs.add(new SimpleResult(tmpScore, docs.scoreDocs[i].doc));
                maxDistance = Math.max(maxDistance, tmpScore);
            } else if (tmpScore < maxDistance) {
                // if it is nearer to the sample than at least one of the current set:
                // remove the last one ...
                resultScoreDocs.remove(resultScoreDocs.last());
                // add the new one ...
                resultScoreDocs.add(new SimpleResult(tmpScore, docs.scoreDocs[i].doc));
                // and set our new distance border ...
                maxDistance = resultScoreDocs.last().getDistance();
            }
        }
        assert (resultScoreDocs.size() <= maximumHits);
        return new SimpleImageSearchHits(resultScoreDocs, maxDistance);
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("not implemented.");
    }

    class BaseSimilarity extends DefaultSimilarity {
        public float tf(float freq) {
            return freq;
        }

        public float idf(long docFreq, long numDocs) {
            return 1;
        }

        public float coord(int overlap, int maxOverlap) {
            return 1;
        }

        public float queryNorm(float sumOfSquaredWeights) {
            return 1;
        }

        public float sloppyFreq(int distance) {
            return 1;
        }

        public float lengthNorm(FieldInvertState state) {
            return 1;
        }
    }
}
