package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.bovw.SiftFeatureHistogramBuilder;
import net.semanticmetadata.lire.imageanalysis.bovw.SurfFeatureHistogramBuilder;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.SiftDocumentBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
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
    private DocumentBuilder surfBuilder, siftBuilder;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        indexPath = new File("index-bow");
        surfBuilder = new SurfDocumentBuilder();
        siftBuilder = new SiftDocumentBuilder();
    }

    public void testIndexingAndSearchSurf() throws IOException {
        // Creating an Lucene IndexWriter
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40, new WhitespaceAnalyzer(Version.LUCENE_40));
        IndexWriter iw = new IndexWriter(FSDirectory.open(indexPath), conf);
        long ms = System.currentTimeMillis();
        int count = 0;
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("testdata/flickr-10000"), true);
        for (Iterator<File> i = files.iterator(); i.hasNext(); ) {
            File imgFile = i.next();
            iw.addDocument(surfBuilder.createDocument(
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

    public void testSearchInIndexSurf() throws IOException {
        int[] docIDs = new int[]{7886, 1600, 4611, 4833, 4260, 2044, 7658};
        for (int i : docIDs) {
            IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
            SurfFeatureHistogramBuilder sfh = new SurfFeatureHistogramBuilder(ir);
            VisualWordsImageSearcher vis = new VisualWordsImageSearcher(10, DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
            Document doc = sfh.getVisualWords(surfBuilder.createDocument(ImageIO.read(new File(queryImage)), queryImage));
            ImageSearchHits hits = vis.search(ir.document(i), ir);
            FileUtils.saveImageResultsToPng("results_bow_no_tf_" + i, hits, queryImage);
        }
    }

    public void testSearchExternalImageSurf() throws IOException {
        IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
        SurfFeatureHistogramBuilder sfh = new SurfFeatureHistogramBuilder(ir);
        VisualWordsImageSearcher vis = new VisualWordsImageSearcher(10, DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
        Document doc = sfh.getVisualWords(surfBuilder.createDocument(ImageIO.read(new File(queryImage)), queryImage));
        ImageSearchHits hits = vis.search(doc, ir);
        FileUtils.saveImageResultsToPng("results_bow_surf", hits, queryImage);
    }

    // -------------< SIFT >--------------------
    public void testIndexingAndSearchSift() throws IOException {
        // Creating an Lucene IndexWriter
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40, new WhitespaceAnalyzer(Version.LUCENE_40));
        IndexWriter iw = new IndexWriter(FSDirectory.open(indexPath), conf);
        long ms = System.currentTimeMillis();
        int count = 0;
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("testdata\\ferrari"), true);
        for (Iterator<File> i = files.iterator(); i.hasNext(); ) {
            File imgFile = i.next();
            iw.addDocument(siftBuilder.createDocument(
                    ImageIO.read(imgFile), imgFile.getPath()));
            count++;
            if (count > 100 && count % 500 == 0) {
                System.out.println(count + " files indexed. " + (System.currentTimeMillis() - ms) / (count) + " ms per file");
            }

        }
        iw.close();
        IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
        SiftFeatureHistogramBuilder sfh = new SiftFeatureHistogramBuilder(ir, 1000, 500);
        sfh.index();
    }

    public void testSearchInIndexSift() throws IOException {
        int[] docIDs = new int[]{0, 10, 23, 35, 56, 77};
        for (int i : docIDs) {
            System.out.println("i = " + i);
            IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
            VisualWordsImageSearcher vis = new VisualWordsImageSearcher(10, DocumentBuilder.FIELD_NAME_SIFT_VISUAL_WORDS);
            ImageSearchHits hits = vis.search(ir.document(i), ir);
            FileUtils.saveImageResultsToPng("results_bow_no_tf_sift_" + i, hits, queryImage);
        }
    }

    public void testSearchExternalImageSift() throws IOException {
        IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
        SurfFeatureHistogramBuilder sfh = new SurfFeatureHistogramBuilder(ir);
        VisualWordsImageSearcher vis = new VisualWordsImageSearcher(10, DocumentBuilder.FIELD_NAME_SIFT_VISUAL_WORDS);
        Document doc = sfh.getVisualWords(siftBuilder.createDocument(ImageIO.read(new File(queryImage)), queryImage));
        ImageSearchHits hits = vis.search(doc, ir);
        FileUtils.saveImageResultsToPng("results_bow_sift", hits, queryImage);
    }

    // from the developer wiki
    public void testWikiCreateIndex() throws IOException {
        String indexPath = "./bovw-test";

        // create the initial local features:
        ChainedDocumentBuilder builder = new ChainedDocumentBuilder();
        builder.addBuilder(new SurfDocumentBuilder());
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true);
        ArrayList<String> images = FileUtils.getAllImages(new File("C:\\Temp\\dataset-sample\\images"), true);
        System.out.println("Indexing " + images.size() + " images.");
        int count = 0;
        for (String identifier : images) {
            Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
            count++;
            if (count%100 ==0) System.out.println(count);
        }
        iw.close();
        System.out.println("Creating vocabulary.");
        // create the visual words.
        IndexReader ir = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        // create a BoVW indexer, "-1" means that half of the images in the index are
        // employed for creating the vocabulary. "100" is the number of visual words to be created.
        SurfFeatureHistogramBuilder sh = new SurfFeatureHistogramBuilder(ir, -1, 100);
        // progress monitoring is optional and opens a window showing you the progress.
        sh.setProgressMonitor(new ProgressMonitor(null, "", "", 0, 100));
        sh.index();
    }

    public void testWikiSearchIndex() throws IOException {
        String indexPath = "./bovw-test";
        VisualWordsImageSearcher searcher = new VisualWordsImageSearcher(10,
                DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        // let's take the first document for a query:
        Document query = reader.document(2);
        ImageSearchHits hits = searcher.search(query, reader);
        // show or analyze your results ....
        FileUtils.saveImageResultsToPng("bovw", hits, query.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
    }


}
