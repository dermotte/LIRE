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
 * Updated: 01.07.13 15:48
 */

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.mser.MSER;
import net.semanticmetadata.lire.imageanalysis.mser.MSERFeature;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.MSERDocumentBuilder;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
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

    public void testParallelIndexMSER() throws IOException {
        ParallelIndexer pin = new ParallelIndexer(1, "mser-idx", "D:\\DataSets\\WIPO\\CA\\sample") {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(new MSERDocumentBuilder());
            }
        };
        pin.run();
    }

    public void testExtendedIndexMSER() throws IOException {
        MSERDocumentBuilder builder = new MSERDocumentBuilder();
        IndexWriterConfig conf = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION,
                new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
        IndexWriter iw = new IndexWriter(FSDirectory.open(indexPath), conf);
        long ms = System.currentTimeMillis();
        int count = 0;
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("D:\\DataSets\\WIPO\\CA\\sample"), true);
        for (Iterator<File> i = files.iterator(); i.hasNext(); ) {
            File imgFile = i.next();
            BufferedImage img = ImageIO.read(imgFile);
            if (Math.max(img.getWidth(), img.getHeight()) < 800) {
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
        BufferedImage image1 = ImageUtils.getGrayscaleImage(image);
        // extract features from image:
        MSER extractor = new MSER();
        List<MSERFeature> features = extractor.computeMSERFeatures(image1);
        ImageUtils.invertImage(image1);
        // invert grey
        features.addAll(extractor.computeMSERFeatures(image1));
        System.out.println(features.size());
    }
}
