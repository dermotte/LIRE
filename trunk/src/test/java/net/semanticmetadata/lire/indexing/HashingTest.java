package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
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
        HashingUtils.readHashFunctions();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File("indexor-1mh")));
        IndexSearcher searcher = new IndexSearcher(reader);
        CEDD feat = null;
        try {
            feat = new CEDD();
        } catch (Exception e) {
            System.err.println("there is a problem with creating the right feature instance.");
            e.printStackTrace();
        }

        if (feat != null) {
            feat.extract(ImageIO.read(new File(queryFile)));
            int[] ints = HashingUtils.generateHashes(feat.getDoubleHistogram());
            StringBuilder queryStringBuilder = new StringBuilder(10*10);
            for (int i = 0; i < ints.length; i++) {
                queryStringBuilder.append(ints[i]);
                queryStringBuilder.append(' ');
            }
            try {
                BooleanQuery query = new BooleanQuery();
                for (int i = 0; i < ints.length; i++) {
                    query.add(new BooleanClause(new TermQuery(new Term("Hashes", ints[i]+"")), BooleanClause.Occur.SHOULD));
                }
                TopDocs topDocs = searcher.search(query, 10);
                topDocs = rerank(topDocs, feat, reader);
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
        for (int i = 0; i< docs.scoreDocs.length; i++) {
            tmp.setByteArrayRepresentation(reader.document(docs.scoreDocs[i].doc).getBinaryValue(DocumentBuilder.FIELD_NAME_CEDD).bytes);
            maxScore = Math.max(1/tmp.getDistance(feature), maxScore);
            res.add(new ScoreDoc(docs.scoreDocs[i].doc, 1/tmp.getDistance(feature)));
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
        bw.write("<h3>results</h3>\n");
        for (int i = 0; i < Math.min(topDocs.scoreDocs.length, 50); i++) {
            String s = reader.document(topDocs.scoreDocs[i].doc).get("descriptorImageIdentifier");
            s = new File(s).getAbsolutePath();
            bw.write("<a href=\"" + s + "\"><img src=\"" + s + "\"></a><p>\n");
        }
        bw.write("</body>\n" +
                "</html>");
        bw.close();
        return new File(fileName).getPath();
    }

}
