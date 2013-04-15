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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 15.04.13 15:18
 */

package net.semanticmetadata.lire.indexing.parallel;

import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

/**
 * This class allows for creating indexes in a parallel manner. The class
 * at hand reads files from the disk and acts as producer, while several consumer
 * threads extract the features from the given files.
 *
 * @author Mathias Lux, mathias@juggle.at, 15.04.13
 */

public class ParallelIndexer implements Runnable {
    private int numberOfThreads = 10;
    private String pathname = "para-idx";
    private String imageDirectory = "testdata/wang-1000";
    Stack<WorkItem> images = new Stack<WorkItem>();
    IndexWriter writer;
    boolean ended = false;
    private ArrayList<File> files;
    int overallCount = 0;

    public static void main(String[] args) {
        ParallelIndexer p = new ParallelIndexer(10, "para-index", "testdata/wang-1000") {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
                builder.addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
                builder.addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
            }
        };

        p.run();
    }

    public ParallelIndexer(int numberOfThreads, String pathname, String imageDirectory) {
        this.numberOfThreads = numberOfThreads;
        this.pathname = pathname;
        this.imageDirectory = imageDirectory;
    }

    /**
     * Overwrite this method to define the builders to be used within the Indexer.
     * @param builder
     */
    public void addBuilders(ChainedDocumentBuilder builder) {
        builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
//            builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//            builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
//            builder.addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());
    }

    public void run() {
        IndexWriterConfig config = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION, new StandardAnalyzer(LuceneUtils.LUCENE_VERSION));
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE);
        try {
            writer = new IndexWriter(FSDirectory.open(new File(pathname)), config);
            files = FileUtils.getAllImageFiles(new File(imageDirectory), true);
            Thread p = new Thread(new Producer());
            p.start();
            LinkedList<Thread> threads = new LinkedList<Thread>();
            long l = System.currentTimeMillis();
            for (int i = 0; i < numberOfThreads; i++) {
                Thread c = new Thread(new Consumer());
                c.start();
                threads.add(c);
            }
            for (Iterator<Thread> iterator = threads.iterator(); iterator.hasNext(); ) {
                iterator.next().join();
            }
            long l1 = System.currentTimeMillis() - l;
            System.out.println("Analyzed " + overallCount + " images in " + l1/1000 + " seconds, " + l1/overallCount + " ms each.");
            writer.commit();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class Producer implements Runnable {
        public void run() {
            BufferedImage tmpImage;
            for (Iterator<File> iterator = files.iterator(); iterator.hasNext(); ) {
                File next = iterator.next();
                try {
                    tmpImage = ImageIO.read(next);
                    int tmpSize = 1;
                    synchronized (images) {
                        images.add(new WorkItem(next.getCanonicalPath(), tmpImage));
                        tmpSize =  images.size();
                        images.notifyAll();
                    }
                    try {
                        if (tmpSize > 20) Thread.sleep(100);
                        else Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            synchronized (images) {
                ended = true;
                images.notifyAll();
            }
        }
    }

    class Consumer implements Runnable {
        WorkItem tmp = null;
        ChainedDocumentBuilder builder = new ChainedDocumentBuilder();
        int count = 0;
        boolean locallyEnded = false;

        Consumer() {
            addBuilders(builder);
        }

        public void run() {
            while (!locallyEnded) {
                synchronized (images) {
                    // we wait for the stack to be either filled or empty & not being filled any more.
                    while (images.empty() && !ended) {
                        try {
                            images.wait(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    // make sure the thread locally knows that the end has come (outer loop)
                    if (images.empty() && ended)
                        locallyEnded = true;
                    // well the last thing we want is an exception in the very last round.
                    if (!images.empty()) {
                        tmp = images.pop();
                        count++;
                        overallCount++;
                    }
                }
                try {
                    Document d = builder.createDocument(tmp.getImage(), tmp.getFileName());
                    writer.addDocument(d);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
//            System.out.println("Images analyzed: " + count);
        }
    }
}
