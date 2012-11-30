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

package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.Similarity;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;

/**
 * Provides a general searcher for visual words implementation. Can be used for SIFT, SURF and MSER.
 * Date: 28.09.2010
 * Time: 13:58:33
 * Mathias Lux, mathias@juggle.at
 */
public class VisualWordsImageSearcher extends AbstractImageSearcher {
    private int numMaxHits;
    private String fieldName;
    //        private Similarity similarity = new DefaultSimilarity();
//        private Similarity similarity = new MySimilarity();
    private Similarity similarity = new BM25Similarity();
    QueryParser qp;


    public VisualWordsImageSearcher(int numMaxHits, Similarity similarity, String fieldName) {
        this.similarity = similarity;
        this.numMaxHits = numMaxHits;
        this.fieldName = fieldName;
        qp = new QueryParser(LuceneUtils.LUCENE_VERSION, fieldName, new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
        BooleanQuery.setMaxClauseCount(10000);
    }

    public VisualWordsImageSearcher(int numMaxHits, String fieldName) {
        this.numMaxHits = numMaxHits;
        this.fieldName = fieldName;
        qp = new QueryParser(LuceneUtils.LUCENE_VERSION, fieldName, new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
        BooleanQuery.setMaxClauseCount(10000);
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        SimpleImageSearchHits sh = null;
        IndexSearcher isearcher = new IndexSearcher(reader);
        isearcher.setSimilarity(similarity);
        String queryString = doc.getValues(fieldName)[0];
        Query tq = null;
        try {
            tq = qp.parse(queryString);
            TopDocs docs = isearcher.search(tq, numMaxHits);
            LinkedList<SimpleResult> res = new LinkedList<SimpleResult>();
            float maxDistance = 0;
            for (int i = 0; i < docs.scoreDocs.length; i++) {
                float d = 1f / docs.scoreDocs[i].score;
                maxDistance = Math.max(d, maxDistance);
                SimpleResult sr = new SimpleResult(d, reader.document(docs.scoreDocs[i].doc));
                res.add(sr);
            }
            sh = new SimpleImageSearchHits(res, maxDistance);
        } catch (ParseException e) {
            System.err.println(queryString);
            e.printStackTrace();
        }
        return sh;
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        throw new UnsupportedOperationException("Not implemented!");
    }

    /**
     * This implementation has shown formidable results with the Nister UKBench data set.
     */
    private static class MySimilarity extends DefaultSimilarity {
        public float tf(float freq) {
//            return (float) Math.log(freq);
            return 1.0f;
        }

        public float idf(int docfreq, int numdocs) {
            return 1f;
        }

        public float queryNorm(float sumOfSquaredWeights) {
            return 1;
        }

        public float computeNorm(String field, FieldInvertState state) {
            return 1;
        }
    }
}
