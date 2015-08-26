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
 * Updated: 13.02.15 17:34
 */

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.SurfFeature;
import net.semanticmetadata.lire.imageanalysis.bovw.BOVWBuilder;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSurfFeature;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.impl.*;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
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
    String pathName;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        pathName = "index-bow";
        indexPath = new File(pathName);
        surfBuilder = new SurfDocumentBuilder();
        siftBuilder = new SiftDocumentBuilder();
    }

    public void testIndexingAndSearchSurf() throws IOException {
        ParallelIndexer pin = new ParallelIndexer(8, pathName, "wang-1000") {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(new SurfDocumentBuilder());
            }
        };
        pin.run();
        IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
        BOVWBuilder sfh = new BOVWBuilder(ir, new SurfFeature(), 1000, 50);
        sfh.index();
    }

    public void testSearchInIndexSurf() throws IOException {
        int[] docIDs = new int[]{7886, 1600, 4611, 4833, 4260, 2044, 7658};
        for (int i : docIDs) {
            IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
            BOVWBuilder sfh = new BOVWBuilder(ir,new SurfFeature());
            VisualWordsImageSearcher vis = new VisualWordsImageSearcher(10, DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW);
//            Document doc = sfh.getVisualWords(surfBuilder.createDocument(ImageIO.read(new File(queryImage)), queryImage));
            ImageSearchHits hits = vis.search(ir.document(i), ir);
            FileUtils.saveImageResultsToPng("results_bow_no_tf_" + i, hits, queryImage);
        }
    }

    public void testSearchExternalImageSurf() throws IOException {
        IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
        BOVWBuilder sfh = new BOVWBuilder(ir, new SurfFeature());
        VisualWordsImageSearcher vis = new VisualWordsImageSearcher(10, DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW);
//        Document doc = sfh.getVisualWords(surfBuilder.createDocument(ImageIO.read(new File(queryImage)), queryImage));
//        ImageSearchHits hits = vis.search(doc, ir);
//        FileUtils.saveImageResultsToPng("results_bow_surf", hits, queryImage);
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
        BOVWBuilder sfh = new BOVWBuilder(ir, new Feature(), 1000, 500);
        sfh.index();
    }

    public void testSearchInIndexSift() throws IOException {
        int[] docIDs = new int[]{0, 10, 23, 35, 56, 77};
        for (int i : docIDs) {
            System.out.println("i = " + i);
            IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
            VisualWordsImageSearcher vis = new VisualWordsImageSearcher(10, DocumentBuilder.FIELD_NAME_SIFT + DocumentBuilder.FIELD_NAME_BOVW);
            ImageSearchHits hits = vis.search(ir.document(i), ir);
            FileUtils.saveImageResultsToPng("results_bow_no_tf_sift_" + i, hits, queryImage);
        }
    }

    public void testSearchExternalImageSift() throws IOException {
        IndexReader ir = DirectoryReader.open(FSDirectory.open(indexPath));
        BOVWBuilder sfh = new BOVWBuilder(ir, new SurfFeature());
        VisualWordsImageSearcher vis = new VisualWordsImageSearcher(10, DocumentBuilder.FIELD_NAME_SIFT + DocumentBuilder.FIELD_NAME_BOVW);
//        Document doc = sfh.getVisualWords(siftBuilder.createDocument(ImageIO.read(new File(queryImage)), queryImage));
//        ImageSearchHits hits = vis.search(doc, ir);
//        FileUtils.saveImageResultsToPng("results_bow_sift", hits, queryImage);
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
        BOVWBuilder sh = new BOVWBuilder(ir, new SurfFeature(), -1, 100);
        // progress monitoring is optional and opens a window showing you the progress.
//        sh.setProgressMonitor(new ProgressMonitor(null, "", "", 0, 100));
        sh.index();
    }

    public void testWikiSearchIndex() throws IOException {
        String indexPath = "./bovw-test";
        VisualWordsImageSearcher searcher = new VisualWordsImageSearcher(10,
                DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW);
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        // let's take the first document for a query:
        Document query = reader.document(2);
        ImageSearchHits hits = searcher.search(query, reader);
        // show or analyze your results ....
        FileUtils.saveImageResultsToPng("bovw", hits, query.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
    }
    
    public void testCvSurfBuilder() throws IOException {
        String imageDirectory = "D:\\Temp\\imagew\\source_images";
        ParallelIndexer indexer = new ParallelIndexer(4, indexPath.getName(), imageDirectory) {
            // use this to add you preferred builders.
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(new CvSurfDocumentBuilder());
            }
        };
        indexer.run();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(indexPath));
        BOVWBuilder sh = new BOVWBuilder(reader, new CvSurfFeature(), 5000, 512);
        sh.index();
    }


}
