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
 * Updated: 01.07.13 16:15
 */

package net.semanticmetadata.lire.indexing.tools;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.indexing.parallel.WorkItem;
import net.semanticmetadata.lire.utils.SerializationUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.*;

/**
 * The Extractor is a configurable class that extracts multiple features from multiple images
 * and puts them into a data file. Main purpose is run multiple extractors at multiple machines
 * and put the data files into one single index. Images are references relatively to the data file,
 * so it should work fine for network file systems.
 * <p/>
 * File format is specified as: (12(345)+)+ with 1-5 being ...
 * <p/>
 * 1. Length of the file hashFunctionsFileName [4 bytes], an int n giving the number of bytes for the file hashFunctionsFileName
 * 2. File hashFunctionsFileName, relative to the outfile [n bytes, see above]
 * 3. Feature index [1 byte], see static members
 * 4. Feature value length [4 bytes], an int k giving the number of bytes encoding the value
 * 5. Feature value [k bytes, see above]
 * <p/>
 * The file is sent through an GZIPOutputStream, so it's compressed in addition.
 * <p/>
 * Note that the outfile has to be in a folder parent to all images!
 *
 * @author Mathias Lux, mathias@juggle.at, 08.03.13
 */
public class ParallelExtractor implements Runnable {
    Stack<WorkItem> images = new Stack<WorkItem>();
    boolean ended = false;
    int overallCount = 0;
    OutputStream dos = null;

    public static final String[] features = new String[]{
            "net.semanticmetadata.lire.imageanalysis.CEDD",                  // 0
            "net.semanticmetadata.lire.imageanalysis.FCTH",                  // 1
            "net.semanticmetadata.lire.imageanalysis.OpponentHistogram",     // 2
            "net.semanticmetadata.lire.imageanalysis.joint.JointHistogram",        // 3
            "net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram",  // 4
            "net.semanticmetadata.lire.imageanalysis.ColorLayout",           // 5
            "net.semanticmetadata.lire.imageanalysis.EdgeHistogram",         // 6
            "net.semanticmetadata.lire.imageanalysis.Gabor",                 // 7
            "net.semanticmetadata.lire.imageanalysis.JCD",                   // 8
            "net.semanticmetadata.lire.imageanalysis.JpegCoefficientHistogram",
            "net.semanticmetadata.lire.imageanalysis.ScalableColor",         // 10
            "net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram",  // 11
            "net.semanticmetadata.lire.imageanalysis.Tamura",                // 12
            "net.semanticmetadata.lire.imageanalysis.LuminanceLayout",       // 13
            "net.semanticmetadata.lire.imageanalysis.PHOG",                   // 14
    };

    public static final String[] featureFieldNames = new String[]{
            DocumentBuilder.FIELD_NAME_CEDD,                 // 0
            DocumentBuilder.FIELD_NAME_FCTH,                 // 1
            DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM,   // 2
            DocumentBuilder.FIELD_NAME_JOINT_HISTOGRAM,      // 3
            DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM, // 4
            DocumentBuilder.FIELD_NAME_COLORLAYOUT,          // 5
            DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM,        // 6
            DocumentBuilder.FIELD_NAME_GABOR,                // 7
            DocumentBuilder.FIELD_NAME_JCD,                  // 8
            DocumentBuilder.FIELD_NAME_JPEGCOEFFS,
            DocumentBuilder.FIELD_NAME_SCALABLECOLOR,
            DocumentBuilder.FIELD_NAME_COLORHISTOGRAM,
            DocumentBuilder.FIELD_NAME_TAMURA,               // 12
            DocumentBuilder.FIELD_NAME_LUMINANCE_LAYOUT,     // 13
            DocumentBuilder.FIELD_NAME_PHOG,                 // 14
    };

    static HashMap<String, Integer> feature2index;

    static {
        feature2index = new HashMap<String, Integer>(features.length);
        for (int i = 0; i < features.length; i++) {
            feature2index.put(features[i], i);
        }
    }

    LinkedList<LireFeature> listOfFeatures;
    File fileList = null;
    File outFile = null;
    private static int numberOfThreads = 4;
    private int monitoringInterval = 10;

    /**
     * Sets the number of consumer threads that are employed for extraction
     * @param numberOfThreads
     */
    public static void setNumberOfThreads(int numberOfThreads) {
        ParallelExtractor.numberOfThreads = numberOfThreads;
    }

    public ParallelExtractor() {
        // default constructor.
        listOfFeatures = new LinkedList<LireFeature>();
    }

    /**
     * Adds a feature to the extractor chain. All those features are extracted from images.
     *
     * @param feature
     */
    public void addFeature(LireFeature feature) {
        listOfFeatures.add(feature);
    }

    /**
     * Sets the file list for processing. One image file per line is fine.
     *
     * @param fileList
     */
    public void setFileList(File fileList) {
        this.fileList = fileList;
    }

    /**
     * Sets the outfile. The outfile has to be in a folder parent to all input images.
     *
     * @param outFile
     */
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    public static void main(String[] args) throws IOException {
        ParallelExtractor e = new ParallelExtractor();

        // parse programs args ...
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-i")) {
                // infile ...
                if ((i + 1) < args.length)
                    e.setFileList(new File(args[i + 1]));
                else printHelp();
            } else if (arg.startsWith("-o")) {
                // out file
                if ((i + 1) < args.length)
                    e.setOutFile(new File(args[i + 1]));
                else printHelp();
            } else if (arg.startsWith("-h")) {
                // help
                printHelp();
            } else if (arg.startsWith("-n")) {
                if ((i + 1) < args.length)
                    try {
                        ParallelExtractor.numberOfThreads = Integer.parseInt(args[i+1]);
                    } catch (Exception e1) {
                        System.err.println("Could not set number of threads to \""+args[i+1]+"\".");
                        e1.printStackTrace();
                    }
                else printHelp();
            } else if (arg.startsWith("-c")) {
                // config file ...
                Properties p = new Properties();
                p.load(new FileInputStream(new File(args[i + 1])));
                Enumeration<?> enumeration = p.propertyNames();
                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    if (key.toLowerCase().startsWith("feature.")) {
                        try {
                            e.addFeature((LireFeature) Class.forName(p.getProperty(key)).newInstance());
                        } catch (Exception e1) {
                            System.err.println("Could not add feature named " + p.getProperty(key));
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        // check if there is an infile, an outfile and some features to extract.
        if (!e.isConfigured()) {
            printHelp();
        } else {
            e.run();
        }
    }

    private boolean isConfigured() {
        boolean configured = true;
        if (fileList == null || !fileList.exists()) configured = false;
        else if (outFile == null) {
            // create an outfile ...
            try {
                outFile = new File(fileList.getCanonicalPath() + ".data");
                System.out.println("Setting out file to " + outFile.getCanonicalFile());
            } catch (IOException e) {
                configured = false;
            }
        } else if (outFile.exists()) {
            System.err.println(outFile.getName() + " already exists. Please delete or choose another outfile.");
//            configured = false;
        }
        if (listOfFeatures.size() < 1) configured = false;
        return configured;
    }

    private static void printHelp() {
        System.out.println("Help for the Extractor class.\n" +
                "=============================\n" +
                "This help text is shown if you start the Extractor with the '-h' option.\n" +
                "\n" +
                "1. Usage\n" +
                "========\n" +
                "$> Extractor -i <infile> [-o <outfile>] -c <configfile> [-n <threads>]\n" +
                "\n" +
                "Note: if you don't specify an outfile just \".data\" is appended to the infile for output.\n" +
                "\n" +
                "2. Config File\n" +
                "==============\n" +
                "The config file is a simple java Properties file. It basically gives the \n" +
                "employed features as a list of properties, just like:\n" +
                "\n" +
                "feature.1=net.semanticmetadata.lire.imageanalysis.CEDD\n" +
                "feature.2=net.semanticmetadata.lire.imageanalysis.FCTH\n" +
                "\n" +
                "... and so on. ");
    }


    @Override
    public void run() {
        // check:
        if (fileList == null || !fileList.exists()) {
            System.err.println("No text file with a list of images given.");
            return;
        }
        if (listOfFeatures.size() == 0) {
            System.err.println("No features to extract given.");
            return;
        }
        try {
            dos = new BufferedOutputStream(new FileOutputStream(outFile));
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
            dos.close();
//            writer.commit();
//            writer.close();
//            threadFinished = true;

        } catch (Exception e) {
            e.printStackTrace();
        }

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
                    System.out.println("Analyzed " + overallCount + " images in " + time / 1000 + " seconds, " + ((overallCount>0)?(time / overallCount):"n.a.") + " ms each ("+images.size()+" images currently in queue).");
                    Thread.sleep(1000 * monitoringInterval); // wait xx seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    class Producer implements Runnable {
        public void run() {
            int tmpSize = 0;
            try {
                BufferedReader br = new BufferedReader(new FileReader(fileList));
                String file = null;
                File next = null;
                while ((file = br.readLine()) != null) {
                    next = new File(file);
                    BufferedImage img = null;
                    try {
                        img = ImageIO.read(next);
                        String path = next.getCanonicalPath();
                        synchronized (images) {
                            images.add(new WorkItem(path, img));
                            tmpSize = images.size();
                            // if the cache is too crowded, then wait.
                            if (tmpSize>500) images.wait(500);
                            // if the cache is too small, dont' notify.
//                            if (tmpSize>50) images.notifyAll();
                        }
                    } catch (Exception e) {
                        System.err.println("Could not read image " + file + ": " + e.getMessage());
                    }
                    try {
                        if (tmpSize > 500) Thread.sleep(1000);
                        else Thread.sleep(2);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            synchronized (images) {
                ended = true;
                images.notifyAll();
            }
        }
    }

    class Consumer implements Runnable {
        WorkItem tmp = null;
        LinkedList<LireFeature> features = new LinkedList<LireFeature>();
        int count = 0;
        boolean locallyEnded = false;

        Consumer() {
            addFeatures(features);
        }

        public void run() {
            byte[] myBuffer = new byte[1024 * 1024 * 10];
            int bufferCount = 0;

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
                    if (!images.empty() && !locallyEnded) {
                        tmp = images.pop();
                        count++;
                        overallCount++;
                    }
                }
                try {
                    bufferCount = 0;
                    if (!locallyEnded) {
                        BufferedImage img = tmp.getImage();
                        byte[] tmpBytes = tmp.getFileName().getBytes();
                        // everything is written to a buffer and only if no exception is thrown, the image goes to index.
                        System.arraycopy(SerializationUtils.toBytes(tmpBytes.length), 0, myBuffer, 0, 4);
                        bufferCount += 4;
                        // dos.write(SerializationUtils.toBytes(tmpBytes.length));
                        System.arraycopy(tmpBytes, 0, myBuffer, bufferCount, tmpBytes.length);
                        bufferCount += tmpBytes.length;
                        // dos.write(tmpBytes);
                        for (LireFeature feature : features) {
                            feature.extract(img);
                            myBuffer[bufferCount] = (byte) feature2index.get(feature.getClass().getName()).intValue();
                            bufferCount++;
                            // dos.write(feature2index.get(feature.getClass().getName()));
                            tmpBytes = feature.getByteArrayRepresentation();
                            System.arraycopy(SerializationUtils.toBytes(tmpBytes.length), 0, myBuffer, bufferCount, 4);
                            bufferCount += 4;
                            // dos.write(SerializationUtils.toBytes(tmpBytes.length));
                            System.arraycopy(tmpBytes, 0, myBuffer, bufferCount, tmpBytes.length);
                            bufferCount += tmpBytes.length;
                            // dos.write(tmpBytes);
                        }
                        // finally write everything to the stream - in case no exception was thrown..
                        synchronized (dos) {
                            dos.write(myBuffer, 0, bufferCount);
                            dos.write(-1);
                            dos.flush();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing file " + tmp.getFileName());
                    e.printStackTrace();
                }
            }
        }
    }

    private void addFeatures(List features) {
        for (Iterator<LireFeature> iterator = listOfFeatures.iterator(); iterator.hasNext(); ) {
            LireFeature next = iterator.next();
            try {
                features.add(next.getClass().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
