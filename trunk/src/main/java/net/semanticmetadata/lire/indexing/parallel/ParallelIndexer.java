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
 * Updated: 26.04.13 13:50
 */

package net.semanticmetadata.lire.indexing.parallel;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
    private String indexPath;
    private String imageDirectory;
    Stack<WorkItem> images = new Stack<WorkItem>();
    IndexWriter writer;
    File imageList = null;
    boolean ended = false;
    boolean threadFinished = false;
    private List<String> files;
    int overallCount = 0, numImages = -1;
    private IndexWriterConfig.OpenMode openMode = IndexWriterConfig.OpenMode.CREATE_OR_APPEND;
    // all xx seconds a status message will be displayed
    private int monitoringInterval = 30;

    public static void main(String[] args) {
        String indexPath = null;
        String imageDirectory = null;
        File imageList = null;
        int numThreads = 10;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-i")) {  // index
                if ((i + 1) < args.length) {
                    indexPath = args[i + 1];
                }
            } else if (arg.startsWith("-n")) { // number of Threads
                if ((i + 1) < args.length) {
                    try {
                        numThreads = Integer.parseInt(args[i + 1]);
                    } catch (NumberFormatException e) {
                        System.err.println("Could not read number of threads: " + args[i + 1] + "\nUsing default value " + numThreads);
                    }
                }
            } else if (arg.startsWith("-l")) { // list of images in a file ...
                imageDirectory = null;
                if ((i + 1) < args.length) {
                    imageList = new File(args[i + 1]);
                    if (!imageList.exists()) {
                        System.err.println(args[i + 1] + " does not exits!");
                        printHelp();
                        System.exit(-1);
                    }
                }
            } else if (arg.startsWith("-d")) { // image directory
                if ((i + 1) < args.length) {
                    imageDirectory = args[i + 1];
                }
            }
        }

        if (indexPath == null) {
            printHelp();
            System.exit(-1);
        } else if (imageList == null && (imageDirectory == null || !new File(imageDirectory).exists())) {
            printHelp();
            System.exit(-1);
        }
        ParallelIndexer p;
        if (imageList != null) {
            p = new ParallelIndexer(numThreads, indexPath, imageList) {
                @Override
                public void addBuilders(ChainedDocumentBuilder builder) {
                    builder.addBuilder(new GenericDocumentBuilder(PHOG.class, DocumentBuilder.FIELD_NAME_PHOG, true));
                    builder.addBuilder(new GenericDocumentBuilder(JCD.class, DocumentBuilder.FIELD_NAME_JCD, true));
                    builder.addBuilder(new GenericDocumentBuilder(OpponentHistogram.class, DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM, true));
                    builder.addBuilder(new GenericDocumentBuilder(JointHistogram.class, DocumentBuilder.FIELD_NAME_JOINT_HISTOGRAM, true));
                    builder.addBuilder(new GenericDocumentBuilder(ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT, true));
                    builder.addBuilder(new GenericDocumentBuilder(EdgeHistogram.class, DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM, true));
                    builder.addBuilder(new GenericDocumentBuilder(SimpleColorHistogram.class, DocumentBuilder.FIELD_NAME_COLORHISTOGRAM, true));
                }
            };

        } else {
            p = new ParallelIndexer(numThreads, indexPath, imageDirectory) {
                @Override
                public void addBuilders(ChainedDocumentBuilder builder) {
                    builder.addBuilder(new GenericDocumentBuilder(PHOG.class, DocumentBuilder.FIELD_NAME_PHOG, true));
                    builder.addBuilder(new GenericDocumentBuilder(JCD.class, DocumentBuilder.FIELD_NAME_JCD, true));
                    builder.addBuilder(new GenericDocumentBuilder(OpponentHistogram.class, DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM, true));
                    builder.addBuilder(new GenericDocumentBuilder(JointHistogram.class, DocumentBuilder.FIELD_NAME_JOINT_HISTOGRAM, true));
                    builder.addBuilder(new GenericDocumentBuilder(ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT, true));
                    builder.addBuilder(new GenericDocumentBuilder(EdgeHistogram.class, DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM, true));
                    builder.addBuilder(new GenericDocumentBuilder(SimpleColorHistogram.class, DocumentBuilder.FIELD_NAME_COLORHISTOGRAM, true));
                }
            };
        }
        p.run();
    }

    /**
     * Prints help text in case the thing is not configured correctly.
     */
    private static void printHelp() {
        System.out.println("Usage:\n" +
                "\n" +
                "$> ParallelIndexer -i <index> <-d <image-directory> | -l <image-list>> [-n <number of threads>]\n" +
                "\n" +
                "index             ... The directory of the index. Will be appended or created if not existing.\n" +
                "images-directory  ... The directory the images are found in. It's traversed recursively.\n" +
                "image-list        ... A list of images in a file, one per line. Use instead of images-directory.\n" +
                "number of threads ... The number of threads used for extracting features, e.g. # of CPU cores.");
    }

    /**
     * @param numberOfThreads
     * @param indexPath
     * @param imageDirectory  a directory containing all the images somewhere in the child hierarchy.
     */
    public ParallelIndexer(int numberOfThreads, String indexPath, String imageDirectory) {
        this.numberOfThreads = numberOfThreads;
        this.indexPath = indexPath;
        this.imageDirectory = imageDirectory;
    }

    /**
     *
     * @param numberOfThreads
     * @param indexPath
     * @param imageDirectory
     * @param overWrite overwrite (instead of append) the index.
     */
    public ParallelIndexer(int numberOfThreads, String indexPath, String imageDirectory, boolean overWrite) {
        this.numberOfThreads = numberOfThreads;
        this.indexPath = indexPath;
        this.imageDirectory = imageDirectory;
        if (overWrite) openMode = IndexWriterConfig.OpenMode.CREATE;
    }

    /**
     * @param numberOfThreads
     * @param indexPath
     * @param imageList       a file containing a list of images, one per line
     */
    public ParallelIndexer(int numberOfThreads, String indexPath, File imageList) {
        this.numberOfThreads = numberOfThreads;
        this.indexPath = indexPath;
        this.imageList = imageList;
    }

    /**
     * Overwrite this method to define the builders to be used within the Indexer.
     *
     * @param builder
     */
    public void addBuilders(ChainedDocumentBuilder builder) {
        builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getLuminanceLayoutDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
    }

    public void run() {
        IndexWriterConfig config = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION, new StandardAnalyzer(LuceneUtils.LUCENE_VERSION));
        config.setOpenMode(openMode);
        try {
            if (imageDirectory != null) System.out.println("Getting all images in " + imageDirectory + ".");
            writer = new IndexWriter(FSDirectory.open(new File(indexPath)), config);
            if (imageList == null) {
                files = FileUtils.getAllImages(new File(imageDirectory), true);
            } else {
                files = new LinkedList<String>();
                BufferedReader br = new BufferedReader(new FileReader(imageList));
                String line = null;
                while ((line = br.readLine()) != null) {
                    if (line.trim().length() > 3) files.add(line.trim());
                }
            }
            numImages = files.size();
            System.out.println("Indexing " + files.size() + " images.");
            Thread p = new Thread(new Producer());
            p.start();
            LinkedList<Thread> threads = new LinkedList<Thread>();
            long l = System.currentTimeMillis();
            for (int i = 0; i < numberOfThreads; i++) {
                Thread c = new Thread(new Consumer());
                c.start();
                threads.add(c);
            }
            Thread m = new Thread(new Monitoring());
            m.start();
            for (Iterator<Thread> iterator = threads.iterator(); iterator.hasNext(); ) {
                iterator.next().join();
            }
            long l1 = System.currentTimeMillis() - l;
            System.out.println("Analyzed " + overallCount + " images in " + l1 / 1000 + " seconds, ~" + l1 / overallCount + " ms each.");
            writer.commit();
            writer.close();
            threadFinished = true;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Check is this thread is still running.
     *
     * @return
     */
    public boolean hasEnded() {
        return threadFinished;
    }

    /**
     * Returns how many of the images have been processed already.
     *
     * @return
     */
    public double getPercentageDone() {
        return (double) overallCount / (double) numImages;
    }

    class Monitoring implements Runnable {
        public void run() {
            long ms = System.currentTimeMillis();
            try {
                Thread.sleep(1000 * monitoringInterval); // wait xx seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!ended) {
                try {
                    // print the current status:
                    long time = System.currentTimeMillis() - ms;
                    System.out.println("Analyzed " + overallCount + " images in " + time / 1000 + " seconds, " + time / overallCount + " ms each.");
                    Thread.sleep(1000 * monitoringInterval); // wait xx seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Producer implements Runnable {
        public void run() {
            BufferedImage tmpImage;
            int tmpSize = 0;
            for (Iterator<String> iterator = files.iterator(); iterator.hasNext(); ) {
                String path = iterator.next();
                File next = new File(path);
                try {
                    tmpImage = ImageIO.read(next);
                    tmpSize = 1;
                    synchronized (images) {
                        path = next.getCanonicalPath();
                        // TODO: add re-write rule for path here!
//                        path = path.replace("E:\\WIPO-conv\\convert", "");
//                        path = path.replace("D:\\Temp\\WIPO-US\\jpg_", "");
                        images.add(new WorkItem(path, tmpImage));
                        tmpSize = images.size();
                        images.notifyAll();
                    }
                    try {
                        if (tmpSize > 500) Thread.sleep(100);
                        // else Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                } catch (Exception e) {
                    System.err.println("Could not open " + path + ". " + e.getMessage());
//                    e.printStackTrace();
                }
            }
            synchronized (images) {
                ended = true;
                images.notifyAll();
            }
        }
    }

    /**
     * Consumers take the images prepared from the Producer and extract all the image features.
     */
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
