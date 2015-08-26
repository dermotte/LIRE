package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.GenericByteLireFeature;
import net.semanticmetadata.lire.imageanalysis.bovw.VLADBuilder;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.MMapDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by mlux on 07.05.2014.
 */
public class VladTest extends TestCase {

    private ParallelIndexer parallelIndexer;

    private String indexPath = "wang-index";
    // if you don't have the images you can get them here: http://wang.ist.psu.edu/docs/related.shtml
    private String testExtensive = "./testdata/wang-1000";

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parallelIndexer = new ParallelIndexer(8, indexPath, testExtensive, true) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {

                builder.addBuilder(new SurfDocumentBuilder());
//               builder.addBuilder(new MSERDocumentBuilder());
//               builder.addBuilder(new SiftDocumentBuilder());

            }
        };
    }

    public void testIndexing() throws IOException {
        // indexing
        System.out.println("-< Getting files to index >--------------");
        parallelIndexer.run();
        VLADBuilder vladBuilder = new VLADBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))));
        vladBuilder.index();
    }

    public void testSearch() throws IOException {
        IndexReader reader = DirectoryReader.open(MMapDirectory.open(new File(indexPath)));
        VLADBuilder vladBuilder = new VLADBuilder(reader);
        GenericFastImageSearcher searcher = new GenericFastImageSearcher(1000, GenericByteLireFeature.class, DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader);
        DocumentBuilder db = new SurfDocumentBuilder();
        Document d = db.createDocument(new FileInputStream(new File("./testdata/wang-1000/99.jpg")), "./testdata/wang-1000/99.jpg");
        d = vladBuilder.getVisualWords(d);
        ImageSearchHits hits = searcher.search(d, reader);
        for (int i=0; i< hits.length(); i++) {
            String file = hits.doc(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": " + file);
        }

    }
}
