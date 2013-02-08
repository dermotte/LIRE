package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.mser.MSER;
import net.semanticmetadata.lire.imageanalysis.mser.MSERFeature;
import net.semanticmetadata.lire.impl.MSERDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: mlux
 * Date: 07.02.13
 * Time: 13:19
 */
public class MserTest extends TestCase {
    private String[] testFiles = new String[]{
            "img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG",
            "img06.JPG", "img07.JPG", "img08.JPG", "error.jpg"};
    private String testFilesPath = "./src/test/resources/images/";
    private File indexPath = new File("mserIndex");

    public void testSimpleIndexMSER() throws IOException {
        MSERDocumentBuilder builder = new MSERDocumentBuilder();
        for (String file : testFiles) {
            builder.createDocument(new FileInputStream(testFilesPath + file), testFilesPath + file);
        }
    }

    public void testExtendedIndexMSER() throws IOException {
        MSERDocumentBuilder builder = new MSERDocumentBuilder();
        IndexWriterConfig conf = new IndexWriterConfig(Version.LUCENE_40, new WhitespaceAnalyzer(Version.LUCENE_40));
        IndexWriter iw = new IndexWriter(FSDirectory.open(indexPath), conf);
        long ms = System.currentTimeMillis();
        int count = 0;
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("C:\\Temp\\testImagelogos"), true);
        for (Iterator<File> i = files.iterator(); i.hasNext(); ) {
            File imgFile = i.next();
            BufferedImage img = ImageIO.read(imgFile);
            if (Math.max(img.getWidth(), img.getHeight())<800) {
                // scale image ...
                img = ImageUtils.scaleImage(img, 800);
            }
            iw.addDocument(builder.createDocument(img, imgFile.getPath()));
            count++;
            if (count > 2 && count % 25 == 0) {
                System.out.println(count + " files indexed. " + (System.currentTimeMillis() - ms) / (count) + " ms per file");
            }

        }
        iw.close();
    }

    public void testFaulty() throws IOException {
        MSERDocumentBuilder builder = new MSERDocumentBuilder();
//        String file = "C:\\Temp\\testImagelogos\\xml\\00\\00\\72330000.gif";
        String file = "C:\\Temp\\test.png";
        BufferedImage image = ImageUtils.createWorkingCopy(ImageIO.read(new FileInputStream(file)));
        BufferedImage image1 = ImageUtils.convertImageToGrey(image);
        // extract features from image:
        MSER extractor = new MSER();
        List<MSERFeature> features = extractor.computeMSERFeatures(image1);
        ImageUtils.invertImage(image1);
        // invert grey
        features.addAll(extractor.computeMSERFeatures(image1));
        System.out.println(features.size());
    }
}
