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
 * Updated: 26.04.13 10:22
 */

package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.*;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * This file is part of LIRE, a Java library for content based image retrieval.
 *
 * @author Mathias Lux, mathias@juggle.at, 24.04.13
 */

public class TestGroundTruth extends TestCase {
    public String indexPath = "test-idx-large";
    private File fileList;
    private File truth = new File("C:\\Temp\\Eval-WIPO\\trimmed.txt");

    public void testAll() throws IOException {
        testIndexing();
        testSearchBenchmark();
    }

    public void testIndexing() {
        fileList = new File("D:\\DataSets\\WIPO\\CA\\dir.txt");
//        fileList = new File("C:\\Temp\\Eval-WIPO\\all_converted.txt");
        ParallelIndexer pin = new ParallelIndexer(8, indexPath, fileList) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
                builder.addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
                builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
                builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
                builder.addBuilder(DocumentBuilderFactory.getLuminanceLayoutDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
            }
        };
        pin.run();
        pin = new ParallelIndexer(4, indexPath, truth) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
                builder.addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
                builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
                builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
                builder.addBuilder(DocumentBuilderFactory.getLuminanceLayoutDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
            }
        };
        pin.run();
    }

    public void testSearchBenchmark() throws IOException {
        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(new File(indexPath)), IOContext.READONCE));
        System.out.println("Precision @ 20:");
//        System.out.println("CEDD        " + getPrecision(ImageSearcherFactory.createCEDDImageSearcher(30), reader));
        System.out.println("ColorLayout " + getPrecision(ImageSearcherFactory.createColorLayoutImageSearcher(30), reader));
        System.out.println("PHOG        " + getPrecision(ImageSearcherFactory.createPHOGImageSearcher(30), reader));
//        System.out.println("FCTH        " + getPrecision(ImageSearcherFactory.createFCTHImageSearcher(30), reader));
        System.out.println("JCD         " + getPrecision(ImageSearcherFactory.createJCDImageSearcher(30), reader));
        System.out.println("EdgeHist    " + getPrecision(ImageSearcherFactory.createEdgeHistogramImageSearcher(30), reader));
//        System.out.println("JointHist   " + getPrecision(ImageSearcherFactory.createJointHistogramImageSearcher(30), reader));
        System.out.println("Lum.Lay.    " + getPrecision(ImageSearcherFactory.createLuminanceLayoutImageSearcher(30), reader));
//        System.out.println("OpponentHis " + getPrecision(ImageSearcherFactory.createOpponentHistogramSearcher(30), reader));
//        System.out.println("ColorHist   " + getPrecision(ImageSearcherFactory.createColorHistogramImageSearcher(30), reader));
    }

    /**
     * Precision @ 20 ...
     *
     * @param is
     * @param reader
     * @return
     * @throws IOException
     */
    private double getPrecision(ImageSearcher is, IndexReader reader) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(truth));
        String line = null;
        StringBuilder sb = new StringBuilder(128);
        double count = 0, sum = 0;
        while ((line = br.readLine()) != null) {
            BufferedImage bimg = ImageIO.read(new File(line));
            ImageSearchHits hits = is.search(bimg, reader);
            for (int i = 0; i < 20; i++) {
                Document d = hits.doc(i);
                String fileName = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                if (fileName.contains("swoosh")) {
                    sum += 1;
                    sb.append('*');
                } else sb.append('.');
            }
            sb.append(" - " + line);
            System.out.println(sb);
            sb.delete(0, sb.length());
            count++;
        }
        return sum / (20d * count);
    }
}
