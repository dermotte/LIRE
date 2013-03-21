package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.imageanalysis.OpponentHistogram;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.MMapDirectory;
import org.apache.lucene.store.RAMDirectory;

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
            feat = new OpponentHistogram();
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
