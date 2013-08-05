package liredemo.flickr;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ...
 * Date: 10.06.2008
 * Time: 11:06:44
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class FlickrDownloadThread implements Runnable {
    public final int NUMBER_OF_SYNC_DOWNLOADS = 5;
    private List<FlickrPhoto> images;
    private LinkedList<Document> finished;
    private int currentIndex = 0;
    DocumentBuilder builder;

    public FlickrDownloadThread(List<FlickrPhoto> images, DocumentBuilder builder) {
        this.images = images;
        finished = new LinkedList<Document>();
        this.builder = builder;
    }


    public void run() {
        ExecutorService pool = Executors.newFixedThreadPool(NUMBER_OF_SYNC_DOWNLOADS);
        for (FlickrPhoto photo : images) {
            // System.out.println("photo.title = " + photo.title);
            pool.execute(new SinglePhotoThread(photo, this));
        }
        pool.shutdown();
        while (!pool.isTerminated()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
        }
        if (currentIndex < images.size()) {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("WARNING: " + (images.size() - currentIndex) + " images left");
            currentIndex = images.size();
        }
    }


    public synchronized Document getCurrentDoc() {
        if (currentIndex < (images.size() - 1)) {
            currentIndex++;
            while (finished.size() < 1) {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return finished.removeFirst();
        } else return null;
    }

    public DocumentBuilder getDocumentBuilder() {
        return builder;
    }

    public void addDocumentToFinished(Document doc) {
        if (doc == null) currentIndex++;
        else finished.add(doc);
    }
}

class SinglePhotoThread implements Runnable {
    FlickrPhoto photo;
    FlickrDownloadThread fdt;

    SinglePhotoThread(FlickrPhoto photo, FlickrDownloadThread fdt) {
        this.photo = photo;
        this.fdt = fdt;
    }

    public void run() {
        try {
            BufferedImage image = ImageIO.read(new URL(photo.photourl));
            image = ImageUtils.scaleImage(image, 320);
            File cachedImage = new File(FlickrIndexingThread.cacheDirectory + photo.photourl.substring(photo.photourl.lastIndexOf("/") + 1, photo.photourl.length()));
            ImageIO.write(image, "jpg", cachedImage);
            Document doc = fdt.getDocumentBuilder().createDocument(image, cachedImage.getAbsolutePath());
            doc.add(new Field("FlickrURL", photo.url, Field.Store.YES, Field.Index.NOT_ANALYZED));
            doc.add(new Field("FlickrTitle", photo.title, Field.Store.YES, Field.Index.NOT_ANALYZED));
            StringBuilder sb = new StringBuilder(256);
            for (String tag : photo.tags) {
                sb.append(tag);
                sb.append(' ');
            }
            doc.add(new Field("tags", sb.toString(), Field.Store.YES, Field.Index.ANALYZED));
            fdt.addDocumentToFinished(doc);
        } catch (IOException e) {
            System.out.println("Warning: Exception reading & indexing image " + photo.photourl + ": " + e.getMessage());
            fdt.addDocumentToFinished(null);
        }

    }
}
