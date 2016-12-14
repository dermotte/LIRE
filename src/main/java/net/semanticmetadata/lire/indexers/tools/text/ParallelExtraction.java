package net.semanticmetadata.lire.indexers.tools.text;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.parallel.WorkItem;
import net.semanticmetadata.lire.utils.StatsUtils;
import org.apache.commons.io.IOUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Simple command line tool for extraction of image
 * feature from image files given by a list.
 *
 * @autor Mathias Lux
 */
public class ParallelExtraction implements Runnable {
    private File imageList;
    private File outFile;
    private ArrayList<GlobalFeature> listOfFeatures = new ArrayList<>();

    private int numOfThreads = 8;
    private int overallCount = 0;
    private LinkedBlockingQueue<WorkItem> queue = new LinkedBlockingQueue<>(5000);
    private long monitoringInterval = 10;
    private OutputStream dos;

    public static void main(String[] args) {
        ParallelExtraction e = new ParallelExtraction();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-i")) {
                // infile ...
                if ((i + 1) < args.length)
                    e.setImageList(new File(args[i + 1]));
                else {
                    System.err.println("Please give a input file after the -i option.");
                    printHelp();
                }
            } else if (arg.startsWith("-o")) {
                // out file
                if ((i + 1) < args.length)
                    e.setOutFile(new File(args[i + 1]));
                else {
                    System.err.println("Please name an outfile after the -o option.");
                    printHelp();
                }
            } else if (arg.startsWith("-f")) {
                // features
                if ((i + 1) < args.length) {
                    String[] split = args[i + 1].split(",");
                    for (int j = 0; j < split.length; j++) {
                        String className = split[j];
                        if (!className.contains(".")) {
                            className = "net.semanticmetadata.lire.imageanalysis.features.global." + className;
                        }
                        try {
                            e.addFeature((GlobalFeature) (Class.forName(className).newInstance()));
                        } catch (Exception erwin) {
                            erwin.printStackTrace();
                        }
                    }
                } else {
                    System.err.println("Please name an outfile after the -o option.");
                    printHelp();
                }
            }
        }
        if (!e.check()) {
            printHelp();
            System.exit(1);
        }
        e.run();
    }

    private boolean check() {
        boolean result = true;
        result = listOfFeatures.size() > 0 && imageList != null && outFile != null;
        return result;
    }

    private static void printHelp() {
        System.out.println("Use with -i <infile> -o <outfile> -c CEDD,FCTH,PHOG");
    }

    public void setImageList(File imageList) {
        this.imageList = imageList;
    }

    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    public void addFeature(GlobalFeature feature) {
        listOfFeatures.add(feature);
    }

    @Override
    public void run() {
        System.out.printf("Starting to index files defined in %s.\n", imageList.getPath());
        System.out.printf("Writing output to %s using the following features: \n", outFile.getPath());
        listOfFeatures.forEach(globalFeature -> System.out.println(" - " + globalFeature.getFeatureName()));
        System.out.println("------------------------------------------------------------");
        try {
            dos = new BufferedOutputStream(new FileOutputStream(outFile), 1024 * 1024 * 100);
            StringBuilder sb = new StringBuilder("file;");
            for (Iterator<GlobalFeature> iterator = listOfFeatures.iterator(); iterator.hasNext(); ) {
                sb.append(iterator.next().getClass().getName() + ";");
            }
            sb.append("\n");
            dos.write(sb.toString().getBytes());
            dos.flush();
            Producer p = new Producer(IOUtils.lineIterator(new FileReader(imageList)));
            Thread pThread = new Thread(p);
            pThread.start();
            Monitoring m = new Monitoring();
            Thread mThread = new Thread(m);
            mThread.start();

            LinkedList<Thread> consumerThreads = new LinkedList<>();
            for (int i = 0; i < numOfThreads; i++) {
                Consumer c = new Consumer();
                Thread cThread = new Thread(c);
                consumerThreads.add(cThread);
                cThread.start();
            }
            pThread.join();
            for (Iterator<Thread> iterator = consumerThreads.iterator(); iterator.hasNext(); ) {
                iterator.next().join();
            }
            System.out.printf("Analyzed %d files\n", overallCount);
            m.stopMonitoring();
            dos.flush();
            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void addFeatures(List features) {
        for (Iterator<GlobalFeature> iterator = listOfFeatures.iterator(); iterator.hasNext(); ) {
            GlobalFeature next = iterator.next();
            try {
                features.add(next.getClass().newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    class Producer implements Runnable {
        private Iterator<String> imageFiles;

        public Producer(Iterator<String> imageFiles) {
            this.imageFiles = imageFiles;
            overallCount = 0;
            queue.clear();
        }

        public void run() {
            File next;
            String path;
            while (imageFiles.hasNext() & overallCount < 10000) {
                path = imageFiles.next();
                next = new File(path);
                try {
                    byte[] buffer = IOUtils.readFully(new FileInputStream(next), (int) next.length());
                    queue.put(new WorkItem(path, buffer));
                } catch (Exception e) {
                    System.err.println("Could not open " + path + ". " + e.getMessage());
                }
            }
            path = null;
            byte[] buffer = null;
            for (int i = 0; i < numOfThreads * 3; i++) {
                try {
                    queue.put(new WorkItem(path, buffer));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class Monitoring implements Runnable {
        private boolean isRunning;

        public Monitoring() {
            this.isRunning = true;
        }

        public void run() {
            long end, gap = 1000 * monitoringInterval;
            long start = System.currentTimeMillis();
            try {
                Thread.sleep(gap); // wait xx seconds
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (isRunning) {
                try {
                    // print the current status:
                    end = System.currentTimeMillis() - start;
                    System.out.printf("Analyzed %d images in %s ~ %3.2f ms each. (queue size is %d)\n", overallCount, StatsUtils.convertTime(end), ((overallCount > 0) ? ((float) end / (float) overallCount) : -1f), queue.size());
                    Thread.sleep(gap); // wait xx seconds
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

        public void stopMonitoring() {
            this.isRunning = false;
        }
    }

    class Consumer implements Runnable {
        WorkItem tmp = null;
        LinkedList<GlobalFeature> features = new LinkedList<GlobalFeature>();
        int count = 0;
        boolean locallyEnded = false;

        Consumer() {
            addFeatures(features);
        }

        public void run() {
            while (!locallyEnded) {
                try {
                    if (queue.size() == 0) Thread.sleep(75);
                    tmp = queue.take();
                    if (tmp.getBuffer() == null) {
                        locallyEnded = true;
                    } else {
                        try {
                            StringBuilder sb = new StringBuilder(256);
                            BufferedImage img = ImageIO.read(new ByteArrayInputStream(tmp.getBuffer()));
                            sb.append(tmp.getFileName() + ";");
                            for (Iterator<GlobalFeature> iterator = features.iterator(); iterator.hasNext(); ) {
                                GlobalFeature next = iterator.next();
                                next.extract(img);
                                sb.append(org.apache.commons.codec.binary.Base64.encodeBase64String(next.getByteArrayRepresentation()) + ";");
                            }
                            sb.append("\n");
                            synchronized (dos) {
                                dos.write(sb.toString().getBytes());
                                overallCount++;
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error processing file " + tmp.getFileName());
                    e.printStackTrace();
                }
            }
        }
    }
}