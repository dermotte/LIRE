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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 04.05.13 11:18
 */

package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.imageanalysis.PHOG;
import net.semanticmetadata.lire.impl.BitSamplingImageSearcher;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;

import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

/**
 * User: mlux
 * Date: 14.03.13
 * Time: 08:41
 */
public class HashingTest extends TestCase {
    String queryFile = "E:\\Temp\\images1\\1\\im1.jpg";

    public void testSearch() throws IOException {
        BitSampling.readHashFunctions();
        // Putting the index into RAM ... with MMapDirectory instead of FSDirectory:
        IndexReader reader = DirectoryReader.open(MMapDirectory.open(new File("indexor-1.4mh")));
        IndexSearcher searcher = new IndexSearcher(reader);
        searcher.setSimilarity(new DefaultSimilarity() {
            @Override
            public float tf(float freq) {
                return 1;
            }

            @Override
            public float idf(long docFreq, long numDocs) {
                return 1;
            }

            @Override
            public float coord(int overlap, int maxOverlap) {
                return 1;
            }

            @Override
            public float queryNorm(float sumOfSquaredWeights) {
                return 1;
            }

            @Override
            public float sloppyFreq(int distance) {
                return 1;
            }

            @Override
            public float lengthNorm(FieldInvertState state) {
                return 1;
            }
        });
        LireFeature feat = null;
        try {
            feat = new CEDD();
        } catch (Exception e) {
            System.err.println("there is a problem with creating the right feature instance.");
            e.printStackTrace();
        }

        if (feat != null) {
            feat.extract(ImageIO.read(new File(queryFile)));
            int[] ints = BitSampling.generateHashes(feat.getDoubleHistogram());
            System.out.println(Arrays.toString(ints));
            StringBuilder queryStringBuilder = new StringBuilder(10 * 10);
            for (int i = 0; i < ints.length; i++) {
                queryStringBuilder.append(ints[i]);
                queryStringBuilder.append(' ');
            }
            try {
                BooleanQuery query = new BooleanQuery();
                for (int i = 0; i < ints.length; i++) {
                    // be aware that the hashFunctionsFileName of the field must match the one you put the hashes in before.
                    query.add(new BooleanClause(new TermQuery(new Term("Hashes", ints[i] + "")), BooleanClause.Occur.SHOULD));
                }
                long ms = System.currentTimeMillis();
                TopDocs topDocs = null;
                for (int i = 0; i < 3; i++) {
                    topDocs = searcher.search(query, 5000);
                }
                System.out.println((System.currentTimeMillis() - ms)/3);
                ms = System.currentTimeMillis();
                for (int i = 0; i < 3; i++) topDocs = rerank(topDocs, feat, reader);
                System.out.println((System.currentTimeMillis() - ms)/3);
                String file = printToHtml(topDocs, reader);
                FileUtils.browseUri(file);
            } catch (Exception e) {
                System.err.println("Exception searching the index.");
                e.printStackTrace();
            }
        }


    }

    private TopDocs rerank(TopDocs docs, LireFeature feature, IndexReader reader) throws IOException, IllegalAccessException, InstantiationException {
        LireFeature tmp = feature.getClass().newInstance();
        ArrayList<ScoreDoc> res = new ArrayList<ScoreDoc>(docs.scoreDocs.length);
        float maxScore = 0f;
        for (int i = 0; i < docs.scoreDocs.length; i++) {
            tmp.setByteArrayRepresentation(reader.document(docs.scoreDocs[i].doc).getBinaryValue(DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM).bytes);
            maxScore = Math.max(1 / tmp.getDistance(feature), maxScore);
            res.add(new ScoreDoc(docs.scoreDocs[i].doc, 1 / tmp.getDistance(feature)));
        }
        // sorting res ...
        Collections.sort(res, new Comparator<ScoreDoc>() {
            @Override
            public int compare(ScoreDoc o1, ScoreDoc o2) {
                return (int) Math.signum(o2.score - o1.score);
            }
        });
        return new TopDocs(50, (ScoreDoc[]) res.toArray(new ScoreDoc[res.size()]), maxScore);
    }

    public void testImageSearcher() throws IOException {
        BitSamplingImageSearcher is = new BitSamplingImageSearcher(60, DocumentBuilder.FIELD_NAME_PHOG,
                DocumentBuilder.FIELD_NAME_PHOG + "_hash", new PHOG(), 500);
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("E:\\wipo1m-idx")));
        Document queryDoc = reader.document(1);
        ImageSearchHits search = is.search(queryDoc, reader);;
        long ms = System.currentTimeMillis();
        int runs = 50;
        for (int i = 0; i<runs; i++)
            search = is.search(queryDoc, reader);
        ms = System.currentTimeMillis() -ms;
//        String file = FileUtils.saveImageResultsToHtml("wipo", search, queryDoc.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
//        FileUtils.browseUri(file);
        System.out.println(((double) ms) / ((double) runs) + " ms per search.");
    }

    public void testLoadHashFunctions() throws IOException {
        BitSampling.readHashFunctions();
    }


    private String printToHtml(TopDocs topDocs, IndexReader reader) throws IOException {
        String fileName = "results-" + System.currentTimeMillis() / 1000 + ".html";
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        bw.write("<html>\n" +
                "<head><title>Search Results</title></head>\n" +
                "<body bgcolor=\"#FFFFFF\">\n");
        bw.write("<h3>query</h3>\n");
        bw.write("<a href=\"" + queryFile + "\"><img src=\"" + queryFile + "\"></a><p>\n");
        bw.write("<h3>results</h3>\n<table>");
        int elems = Math.min(topDocs.scoreDocs.length, 50);
        for (int i = 0; i < elems; i++) {
            if (i % 3 == 0) bw.write("<tr>");
            String s = reader.document(topDocs.scoreDocs[i].doc).get("descriptorImageIdentifier");
            s = new File(s).getAbsolutePath();
            bw.write("<td><a href=\"" + s + "\"><img style=\"max-width:220px\"src=\"" + s + "\"></a></td>\n");
            if (i % 3 == 2) bw.write("</tr>");
        }
        if (elems % 3 != 0) {
            if (elems % 3 == 2) {
                bw.write("<td>-</td>\n");
                bw.write("<td>-</td>\n");
            } else if (elems % 3 == 2) {
                bw.write("<td>-</td>\n");
            }
            bw.write("</tr>");
        }
        bw.write("</table></body>\n" +
                "</html>");
        bw.close();
        return new File(fileName).getPath();
    }

}
