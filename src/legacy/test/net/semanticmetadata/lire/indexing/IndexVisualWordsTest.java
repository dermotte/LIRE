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
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.SurfFeature;
import net.semanticmetadata.lire.imageanalysis.bovw.BOVWBuilder;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * User: mlux
 * Date: 03.05.2011
 * Time: 10:28:16
 */
public class IndexVisualWordsTest extends TestCase {
    String directory = "./imageCLEF2011"; // Where are the photos?
    String index = "./imageClefIndex"; // Where is the index?
    int clusters = 2000; // number of visual words
    int numSamples = 2000; // number of samples used for visual words vocabulary building

    public void testIndexSurfHistogram() throws IOException {
        // index all files
        System.out.println("-< Getting files to index >--------------");
        List<String> images = FileUtils.getAllImages(new File(directory), true);
        System.out.println("-< Indexing " + images.size() + " files >--------------");
        indexFiles(images, index);

        // create histograms
//        System.out.println("-< Creating SIFT based histograms >--------------");
//        SiftFeatureHistogramBuilder siftFeatureHistogramBuilder = new SiftFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(index))), numSamples, clusters);
//        siftFeatureHistogramBuilder.index();
        System.out.println("-< Creating SURF based histograms >--------------");
        BOVWBuilder surfFeatureHistogramBuilder = new BOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(index))), new SurfFeature(), numSamples, clusters);
        surfFeatureHistogramBuilder.index();
//        System.out.println("-< Creating MSER based histograms >--------------");
//        MSERFeatureHistogramBuilder mserFeatureHistogramBuilder = new MSERFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File(index))), numSamples, clusters);
//        mserFeatureHistogramBuilder.index();
//        System.out.println("-< Finished >--------------");

    }

    private void indexFiles(List<String> images, String index) throws IOException {
        ChainedDocumentBuilder documentBuilder = new ChainedDocumentBuilder();
//        documentBuilder.addBuilder(new CEDDDocumentBuilder());
        documentBuilder.addBuilder(new SurfDocumentBuilder());
//        documentBuilder.addBuilder(new MSERDocumentBuilder());
//        documentBuilder.addBuilder(new SiftDocumentBuilder());

//        documentBuilder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//        documentBuilder.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
//        documentBuilder.addBuilder(new SimpleDocumentBuilder(false, false, true));
//        documentBuilder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
//        documentBuilder.addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());

        IndexWriter iw = LuceneUtils.createIndexWriter(index, true);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            Document doc = documentBuilder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
            count++;
            if (count % 500 == 0) System.out.println(count + " files indexed.");
            if (count == 500) break;
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.close();

    }

    public void testIndexMissingFiles() throws IOException {
        // first delete some of the existing ones ...
        System.out.println("Deleting visual words from docs ...");
        IndexReader ir = DirectoryReader.open(FSDirectory.open(new File(index)));
        IndexWriter iw = LuceneUtils.createIndexWriter(index, false);
        int maxDocs = ir.maxDoc();
        for (int i = 0; i < maxDocs / 10; i++) {
            Document d = ir.document(i);
//            d.removeFields(DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW);
            d.removeFields(DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW);
//            d.removeFields(DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM);
            d.removeFields(DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW_VECTOR);
//            d.removeFields(DocumentBuilder.FIELD_NAME_SURF);
            iw.updateDocument(new Term(DocumentBuilder.FIELD_NAME_IDENTIFIER, d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]), d);
        }
        System.out.println("# of deleted docs:  " + maxDocs / 10);
        System.out.println("Optimizing and closing ...");
        iw.close();
        ir.close();
        System.out.println("Creating new visual words ...");
        BOVWBuilder surfFeatureHistogramBuilder = new BOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(index))), new SurfFeature(), numSamples, clusters);
//        surfFeatureHistogramBuilder.indexMissing();
//        System.out.println("Finished.");
    }

    public void testStrings() {
        ProgressMonitor pm = new ProgressMonitor(null, "", "", 0, 100);
        System.out.println(String.format("%02d:%02d", 3, 5));
    }
}
