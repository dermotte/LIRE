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
 * Updated: 21.01.15 08:29
 */

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * This file is part of LIRe
 * Date: 31.01.2006
 * Time: 23:59:45
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class CreateIndexTest extends TestCase {
    private String[] testFiles = new String[]{"img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG",
            "img06.JPG", "img07.JPG", "img08.JPG", "img08a.JPG", "error.jpg"};
    private String testFilesPath = "./src/test/resources/images/";
    private String indexPath = "test-index";
    private String testExtensive = "./wang-1000";

    private DocumentBuilder getDocumentBuilder() {
        ChainedDocumentBuilder result = new ChainedDocumentBuilder();
        result.addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
        result.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        result.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
        result.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
        result.addBuilder(DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder());
        return result;
    }

    /**
     * Creates an index with an extensive list of global features.
     *
     * @throws IOException
     */
    public void testCreateIndex() throws IOException {
        ChainedDocumentBuilder builder = (ChainedDocumentBuilder) getDocumentBuilder();

        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath + "-small", true);
        for (String identifier : testFiles) {
            System.out.println("Indexing file " + identifier);
            Document doc = builder.createDocument(new FileInputStream(testFilesPath + identifier), identifier);
            iw.addDocument(doc);
        }
        iw.close();
    }

    public void testCreateCorrelogramIndex() throws IOException {
        String[] testFiles = new String[]{"img01.jpg", "img02.jpg", "img03.jpg", "img04.jpg", "img05.jpg", "img06.jpg", "img07.jpg", "img08.jpg", "img09.jpg", "img10.jpg"};
        String testFilesPath = "./src/test/resources/small/";

        DocumentBuilder builder = DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder();
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath + "-small", true);
        long ms = System.currentTimeMillis();
        for (String identifier : testFiles) {
            Document doc = builder.createDocument(new FileInputStream(testFilesPath + identifier), identifier);
            iw.addDocument(doc);
        }
        System.out.println("Time taken: " + ((System.currentTimeMillis() - ms) / testFiles.length) + " ms");
        iw.close();
    }

    public void testCreateCEDDIndex() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        DocumentBuilder builder = DocumentBuilderFactory.getCEDDDocumentBuilder();
        IndexWriter iw = LuceneUtils.createIndexWriter("wang-cedd", true);
        for (String identifier : images) {
            Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
        }
        iw.close();
    }

    public void testCreateBigIndex() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        indexFiles(images, getDocumentBuilder(), indexPath + "-big-index");
    }
    
    public void testParallelIndexing() {
        
        
    }

    private void indexFiles(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        System.out.println(">> Indexing " + images.size() + " files.");

        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            try {
                Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
                iw.addDocument(doc);
            } catch (Exception e) {
                System.err.println("Error indexing file: " + identifier + "(" + e.getMessage() + ")");
                // e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            count++;
            if (count % 100 == 0) {
                System.out.print(count + " files indexed. ");
                float pct = (float) count / (float) images.size();
                float tmp = (float) (System.currentTimeMillis() - time) / 1000;
                float remain = (tmp / pct) * (1f - pct);
                System.out.println("Remaining: <" + ((int) (remain / 60) + 1) + " minutes of <" + ((int) ((tmp / pct) / 60) + 1) + " minutes");
            }
            // if (count == 200) break;
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.close();
    }

    @SuppressWarnings("unused")
	private void indexFilesMultithreaded(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        System.out.println(">> Indexing " + images.size() + " files.");
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true);
        SynchronizedWriter sw = new SynchronizedWriter(iw);
        ExecutorService pool = Executors.newFixedThreadPool(4);

        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            pool.execute(new IndexingThread(identifier, sw));
            count++;
            if (count % 1000 == 0) System.out.println(count + " files added.");
            // if (count == 200) break;
        }
        while (!pool.isTerminated()) {
            try {
                pool.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("indexed: " + iw.maxDoc());
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.close();
    }

    class SynchronizedWriter {
        IndexWriter iw;

        SynchronizedWriter(IndexWriter iw) {
            this.iw = iw;
        }

        public synchronized void addDocument(Document d) throws IOException {
            iw.addDocument(d);
        }
    }

    class IndexingThread implements Runnable {
        DocumentBuilder builder = DocumentBuilderFactory.getCEDDDocumentBuilder();
        String file = null;
        private SynchronizedWriter synchronizedWriter;

        IndexingThread(String img, SynchronizedWriter sw) {
            this.file = img;
            synchronizedWriter = sw;
        }

        public void run() {
            try {
                Document doc = builder.createDocument(new FileInputStream(file), file);
                synchronizedWriter.addDocument(doc);
            } catch (Exception e) {
                System.err.println("Error indexing file: " + file + "(" + e.getMessage() + ")");
            }
        }
    }
}
