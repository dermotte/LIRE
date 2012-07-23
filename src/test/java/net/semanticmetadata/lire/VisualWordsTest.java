package net.semanticmetadata.lire;

import net.semanticmetadata.lire.imageanalysis.bovw.SurfFeatureHistogramBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * User: mlux
 * Date: 23.07.12
 * Time: 14:49
 */
public class VisualWordsTest {
    public static void main(String[] args) throws IOException {
        DocumentBuilder builder = new SurfDocumentBuilder();
        // Creating an Lucene IndexWriter
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_36,
                new WhitespaceAnalyzer(Version.LUCENE_36));
        IndexWriter iw =
                    new IndexWriter(FSDirectory.open(new File("index")), conf);

        ArrayList<File> files =
                    FileUtils.getAllImageFiles(new File("testdata"), true);
        for (Iterator<File> i = files.iterator(); i.hasNext(); ) {
            File imgFile = i.next();
            iw.addDocument(builder.createDocument(
                        ImageIO.read(imgFile), imgFile.getPath()));
        }
        iw.close();
        IndexReader ir = IndexReader.open(FSDirectory.open(new File("index")));
        SurfFeatureHistogramBuilder sfh =
                    new SurfFeatureHistogramBuilder(ir, 1000, 500);
        sfh.index();

        VisualWordsImageSearcher vis = new VisualWordsImageSearcher(100, 
                    DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
        Document queryDoc = builder.createDocument(ImageIO.read(new File("query.jpg")), "query");
        queryDoc = sfh.getVisualWords(queryDoc);
        ImageSearchHits hits = vis.search(queryDoc, ir);

    }
}
