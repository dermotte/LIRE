package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.*;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.*;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

/**
 * ...
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Created: 04.06.12, 14:19
 */
public class LocalitySensitiveHashingTest extends TestCase {
    String testExtensive = "./wang-1000";
    private String indexPath = "index-hashed";
    private int numImagesEval = 50;

    public double testIndexing() throws IOException, IllegalAccessException, InstantiationException {
        LocalitySensitiveHashing.generateHashFunctions();
        LocalitySensitiveHashing.readHashFunctions();
        DocumentBuilder builder = new ChainedDocumentBuilder();
        ((ChainedDocumentBuilder) builder).addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());

//        System.out.println("-< Getting files to index >--------------");
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
//        System.out.println("-< Indexing " + images.size() + " files >--------------");

        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            CEDD cedd = new CEDD();
            cedd.extract(ImageIO.read(new FileInputStream(identifier)));
            Document doc = new Document();
            doc.add(new Field(DocumentBuilder.FIELD_NAME_CEDD, cedd.getByteArrayRepresentation()));
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
            int[] hashes = LocalitySensitiveHashing.generateHashes(cedd.getDoubleHistogram());
            StringBuilder hash = new StringBuilder(512);
            for (int i = 0; i < hashes.length; i++) {
                hash.append(hashes[i]);
                hash.append(' ');
            }
//            System.out.println("hash = " + hash);
            doc.add(new Field("hash", hash.toString(), Field.Store.YES, Field.Index.ANALYZED));
            iw.addDocument(doc);
            count++;
//            if (count % 100 == 0) System.out.println(count + " files indexed.");
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

//        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.optimize();
        iw.close();

        return testSearch();

    }

    public void testSearchSingle() throws IOException, InstantiationException, IllegalAccessException {
        System.out.println(testSearch());
    }

    public double testSearch() throws IOException, IllegalAccessException, InstantiationException {
        double sum = 0;
        int numDocs = 1000;
        for (int i = 0; i < numDocs; i++) {
            sum += (singleSearch(i));
        }
        return (sum / (double) numDocs);
    }

    public void testParameters() throws IOException, InstantiationException, IllegalAccessException {
        for (int i = 100; i < 300; i += 10) {
            LocalitySensitiveHashing.binLength = (double) i / 100d;
            System.out.println("binLength = " + LocalitySensitiveHashing.binLength + "\t " + testIndexing());
        }
//        for (int i=100; i<300; i+=10) {
//            LocalitySensitiveHashing.numFunctionBundles = i;
//            System.out.println("numFunctionBundles = " + LocalitySensitiveHashing.numFunctionBundles + "\t " + testIndexing());
//        }
    }

    public double singleSearch(int docNum) throws IOException, InstantiationException, IllegalAccessException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));

        // -----------

        String query = reader.document(docNum).getValues("hash")[0];
        CEDD ceddQuery = new CEDD();
        ceddQuery.setByteArrayRepresentation(reader.document(docNum).getBinaryValue(DocumentBuilder.FIELD_NAME_CEDD));

        // -----------

        HashSet<String> gold = new HashSet<String>(numImagesEval);
        ImageSearcher cis = ImageSearcherFactory.createCEDDImageSearcher(100);
        ImageSearchHits hits = cis.search(reader.document(docNum), reader);
        for (int i = 0; i < 10; i++) {
            gold.add(hits.doc(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
        }

        // ------------

        IndexSearcher searcher = new IndexSearcher(reader);
        TopDocs topDocs = searcher.search(createQuery(query), numImagesEval);
//        topDocs = rerank(topDocs, ceddQuery, reader);
//        System.out.println("topDocs.scoreDocs.length = " + topDocs.scoreDocs.length);
        double numMatches = 0;
        for (int i = 0; i < topDocs.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = topDocs.scoreDocs[i];
//            System.out.print(scoreDoc.score + ": ");
            String file = reader.document(scoreDoc.doc).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
//            System.out.println(file.substring(file.lastIndexOf('/') + 1) + (gold.contains(file)?" x":" o"));
            if (gold.contains(file)) numMatches++;
        }
        return numMatches;
    }

    private TopDocs rerank(TopDocs docs, LireFeature feature, IndexReader reader) throws IOException, IllegalAccessException, InstantiationException {
        LireFeature tmp = new CEDD();
        ArrayList<ScoreDoc> res = new ArrayList<ScoreDoc>(docs.scoreDocs.length);
        float maxScore = 0f;
        for (int i = 0; i < docs.scoreDocs.length; i++) {
            tmp.setByteArrayRepresentation(reader.document(docs.scoreDocs[i].doc).getBinaryValue(DocumentBuilder.FIELD_NAME_CEDD));
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
        return new TopDocs(numImagesEval, (ScoreDoc[]) res.toArray(new ScoreDoc[res.size()]), maxScore);
    }

    private BooleanQuery createQuery(String hashes) {
        StringTokenizer tok = new StringTokenizer(hashes);
        BooleanQuery q = new BooleanQuery();
        while (tok.hasMoreTokens())
            q.add(new BooleanClause(new TermQuery(new Term("hash", tok.nextToken())), BooleanClause.Occur.SHOULD));
        return q;
    }
}
