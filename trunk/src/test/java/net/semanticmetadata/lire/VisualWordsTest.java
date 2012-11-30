package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.bovw.SurfFeatureHistogramBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
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
public class VisualWordsTest extends TestCase {
    private static File indexPath;
    String queryImage = "testdata/flickr-10000/3544790945_38c07af051_o.jpg";
    private DocumentBuilder builder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        indexPath = new File("index-bow");
        builder = new SurfDocumentBuilder();
    }

    public void testIndexingAndSearch() throws IOException {
        // Creating an Lucene IndexWriter
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40, new WhitespaceAnalyzer(Version.LUCENE_40));
        IndexWriter iw = new IndexWriter(FSDirectory.open(indexPath), conf);
        long ms = System.currentTimeMillis();
        int count = 0;
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("testdata/flickr-10000"), true);
        for (Iterator<File> i = files.iterator(); i.hasNext(); ) {
            File imgFile = i.next();
            iw.addDocument(builder.createDocument(
                    ImageIO.read(imgFile), imgFile.getPath()));
            count++;
            if (count > 100 && count % 500 == 0) {
                System.out.println(count + " files indexed. " + (System.currentTimeMillis() - ms) / (count) + " ms per file");
            }

        }
        iw.close();
        IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
        SurfFeatureHistogramBuilder sfh = new SurfFeatureHistogramBuilder(ir, 1000, 500);
        sfh.index();
    }

    public void testSearchInIndex() throws IOException {
        int[] docIDs = new int[]{7886, 1600, 4611, 4833, 4260, 2044, 7658};
        for (int i : docIDs) {
            IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
            SurfFeatureHistogramBuilder sfh = new SurfFeatureHistogramBuilder(ir);
            VisualWordsImageSearcher vis = new VisualWordsImageSearcher(10, DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
            Document doc = sfh.getVisualWords(builder.createDocument(ImageIO.read(new File(queryImage)), queryImage));
            ImageSearchHits hits = vis.search(ir.document(i), ir);
            FileUtils.saveImageResultsToPng("results_bow_no_tf_" + i, hits, queryImage);
        }
    }

    public void testSearchExternalImage() throws IOException {
        IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
        SurfFeatureHistogramBuilder sfh = new SurfFeatureHistogramBuilder(ir);
        VisualWordsImageSearcher vis = new VisualWordsImageSearcher(10, DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
        Document doc = sfh.getVisualWords(builder.createDocument(ImageIO.read(new File(queryImage)), queryImage));
        ImageSearchHits hits = vis.search(doc, ir);
        FileUtils.saveImageResultsToPng("results_bow_", hits, queryImage);
    }
}
