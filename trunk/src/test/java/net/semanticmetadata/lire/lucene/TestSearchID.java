package net.semanticmetadata.lire.lucene;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;

/**
 * User: mlux
 * Date: 06.03.13
 * Time: 16:27
 */
public class TestSearchID extends TestCase {
    private String indexPath = "test-index";

    public void testSearch() throws IOException {
        // use GeneralTest to create the index ...
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath + "-small")));

        IndexSearcher is = new IndexSearcher(reader);
        TopDocs docs = is.search(new TermQuery(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, "img01.JPG")), 10);
        Document result = reader.document(docs.scoreDocs[0].doc);
        System.out.println(result.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
    }
}
