/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * ~~~~~~~~~~~~~~~~~~~~
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package liredemo.indexing;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifReader;
import net.semanticmetadata.lire.DocumentBuilder;
import org.apache.lucene.document.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ...
 * Date: 10.06.2008
 * Time: 17:24:32
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ParallelIndexer implements Runnable {
    // Vectors are already synchronized, so that's the cheap solution.
    Vector<String> imageFiles;
    private int NUMBER_OF_SYNC_THREADS = 4;
    Hashtable<String, Boolean> indexThreads = new Hashtable<String, Boolean>(3);
    DocumentBuilder builder;
    Vector<Document> finished = new Vector<Document>();
    private boolean started = false;
    private final ExecutorService pool;

    private int countImagesOut = 0;
    private int countImagesProcesses = 0;


    public ParallelIndexer(List<String> imageFiles, DocumentBuilder b) {
        this.imageFiles = new Vector<String>();
        assert (imageFiles != null);
        this.imageFiles.addAll(imageFiles);
        builder = b;
        pool = Executors.newFixedThreadPool(NUMBER_OF_SYNC_THREADS);
    }

    public void run() {
        for (int i = 1; i < NUMBER_OF_SYNC_THREADS; i++) {
            PhotoIndexer runnable = new PhotoIndexer(this);
//            Thread t = new Thread(runnable);
//            t.start();
//            indexThreads.put(t.getName(), false);
            pool.submit(runnable);
        }
        started = true;
//        while (started) {
//            try {
//                pool.awaitTermination(15, TimeUnit.MINUTES);
//                started = false;
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//        }
    }

    public void addDoc(Document doc, String photofile) {
        if (doc != null) finished.add(doc);
        Thread.yield();
    }

    public Document getNext() {
        if (imageFiles.size() < 1) {
            boolean fb = true;
            for (String t : indexThreads.keySet()) {
                fb = fb && indexThreads.get(t);
            }
            if (started && fb) {
                return null;
            }
        }
        while (finished.size() < 1) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return finished.remove(0);
    }

    private String getNextImage() {
        if (imageFiles.size() > 0) {
            countImagesOut++;
            return imageFiles.remove(0);
        } else {
            System.out.println("countImagesOut = " + countImagesOut);
            return null;
        }
    }

    class PhotoIndexer implements Runnable {
        String photo;
        ParallelIndexer parent;
        private boolean hasFinished = false;

        PhotoIndexer(ParallelIndexer parent) {
            this.parent = parent;

        }

        public void run() {
            parent.indexThreads.put(Thread.currentThread().getName(), false);
            while ((photo = parent.getNextImage()) != null) {
                try {
                    BufferedImage image = readFile(photo);
                    if (image != null) {
                        Document doc = parent.builder.createDocument(image, photo);
                        parent.addDoc(doc, photo);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    parent.addDoc(null, photo);
                }
            }
            parent.indexThreads.put(Thread.currentThread().getName(), true);
        }

        private BufferedImage readFile(String path) throws IOException {
            BufferedImage image = null;
            if (path.toLowerCase().endsWith(".jpg")) {
                FileInputStream jpegFile = new FileInputStream(path);
                Metadata metadata = new Metadata();
                try {
                    new ExifReader(jpegFile).extract(metadata);
//                    byte[] thumb = ((ExifDirectory) metadata.getDirectory(ExifDirectory.class)).getThumbnailData();
//                    if (thumb != null) image = ImageIO.read(new ByteArrayInputStream(thumb));
//                    System.out.print("Read from thumbnail data ... ");
//                    System.out.println(image.getWidth() + " x " + image.getHeight());
                } catch (JpegProcessingException e) {
                    System.err.println("Could not extract EXIF data for " + path);
                    System.err.println("\t" + e.getMessage());
                } catch (Exception e) {
                    System.err.println("Could not extract EXIF data for " + path);
                    System.err.println("\t" + e.getMessage());
                }
                jpegFile.close();    // patch by Simon Micollier
            }
            // Fallback & PNGs:
            if (image == null)
                try {
                    image = ImageIO.read(new File(path));
                } catch (Exception e) {
                    System.err.println("Error reading file " + path + "\n\t" + e.getMessage());
                    e.printStackTrace();
                }
            return image;
        }
    }


}
