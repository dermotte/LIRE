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

package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * Main test class for testing algorithms on the PASCAL VOC 2007 data set,
 * which is available at http://pascallin.ecs.soton.ac.uk/challenges/VOC/voc2007/index.html#testdata
 * Created: 11.05.2011 09:04:25
 */
public class TestPascalVOC2007 extends TestCase {
    private ChainedDocumentBuilder builder;
    private String testExtensive = "VOC2007/JPEGImages";
    private String indexPath = "./pascal-map-test";

    public void setUp() {
        // Setting up DocumentBuilder:
        builder = new ChainedDocumentBuilder();
        builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
        builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
//        builder.addBuilder(new GenericDocumentBuilder(FuzzyColorHistogram.class, "FIELD_FUZZYCOLORHIST"));
//        builder.addBuilder(new GenericDocumentBuilder(JpegCoefficientHistogram.class, "FIELD_JPEGCOEFFHIST"));
//        builder.addBuilder(new SimpleDocumentBuilder(true, true, true));
//        builder.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());

        // ----- local features ------------------
//        builder.addBuilder(new SiftDocumentBuilder());
//        builder.addBuilder(new SurfDocumentBuilder());
//        builder.addBuilder(new MSERDocumentBuilder());
    }

    public void testIndex() throws IOException {
        // indexing
        System.out.println("-< Getting files to index >--------------");
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        System.out.println("-< Indexing " + images.size() + " files >--------------");
        indexFiles(images, builder, indexPath);

//        in case of "bag of visual words" ...
//        SiftFeatureHistogramBuilder sh1 = new SiftFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 8000);
//        sh1.index();
//        SurfFeatureHistogramBuilder sh = new SurfFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 8000);
//        sh.index();
//        MSERFeatureHistogramBuilder sh = new MSERFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(indexPath))), 200, 8000);
//        sh.index();

        System.out.println("-< Indexing finished >--------------");

    }

    private void indexFiles(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        System.out.println(">> Indexing " + images.size() + " files.");
        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
            count++;
            if (count % 100 == 0) System.out.println(count + " files indexed.");
//            if (count == 200) break;
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.close();
    }

    public void testMAP() {
        computeMAP(ImageSearcherFactory.createColorHistogramImageSearcher(4952), "Color Histogram - JSD");
//        computeMAP(new SiftVisualWordsImageSearcher(4952), "Sift BoVW");

    }

    private void computeMAP(ImageSearcher imageSearcher, String prefix) {
        //To change body of created methods use File | Settings | File Templates.
    }

}
