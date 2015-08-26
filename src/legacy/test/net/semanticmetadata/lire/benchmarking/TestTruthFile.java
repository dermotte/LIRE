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

package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.imageanalysis.joint.JointHistogram;
import net.semanticmetadata.lire.imageanalysis.spatialpyramid.SPACC;
import net.semanticmetadata.lire.imageanalysis.spatialpyramid.SPCEDD;
import net.semanticmetadata.lire.imageanalysis.spatialpyramid.SPLBP;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.MMapDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * User: mlux
 * Date: 04.10.13
 * Time: 11:20
 */
public class TestTruthFile extends TestCase {
    public String indexPath = "test-idx";
    private String path = "D:\\MyDropBox\\Dropbox\\topics\\sorted\\";
    private File truthFile = new File(path + "truth.txt");
    private File dataSet = new File("D:\\DataSets\\Labels\\dir.txt");
    private File answerSet = new File(path + "results.txt");
    private Class[] testFeatures = new Class[]{
            AutoColorCorrelogram.class,
            BinaryPatternsPyramid.class,
            CEDD.class,
            ColorLayout.class,
            EdgeHistogram.class,
            FCTH.class,
            FuzzyColorHistogram.class,
            Gabor.class,
            JCD.class,
            JointHistogram.class,
            JpegCoefficientHistogram.class,
            LocalBinaryPatterns.class,
            LuminanceLayout.class,
            OpponentHistogram.class,
            PHOG.class,
            RotationInvariantLocalBinaryPatterns.class,
            SPACC.class,
            SPCEDD.class,
            SPLBP.class,
            ScalableColor.class,
            SimpleColorHistogram.class,
            Tamura.class,
    };

    public void testAll() throws Exception {
        testIndexing();
        testPerformance();
    }


    public void testIndexing() throws Exception {
        // index data
        ParallelIndexer pin = new ParallelIndexer(16, indexPath, dataSet, true) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                for (int i = 0; i < testFeatures.length; i++) {
                    builder.addBuilder(new GenericDocumentBuilder(testFeatures[i], false));
                }
            }
        };
        pin.run();
        // index answers
        pin = new ParallelIndexer(16, indexPath, answerSet, false) {
                    @Override
                    public void addBuilders(ChainedDocumentBuilder builder) {
                        for (int i = 0; i < testFeatures.length; i++) {
                            builder.addBuilder(new GenericDocumentBuilder(testFeatures[i], false));
                        }
                    }
                };
        pin.run();
    }

    public void testPerformance() throws IOException {
        System.out.printf("%s\t%s\t%s\t%s\t%s\t%s\n", "Test", "recall@5", "recall@10", "recall@20", "recall@30", "recall@50");
        IndexReader reader = DirectoryReader.open(MMapDirectory.open(new File(indexPath)));
        for (int i = 0; i < testFeatures.length; i++) {
            getRecall(testFeatures[i].getName().substring(testFeatures[i].getName().lastIndexOf('.')+1), new GenericFastImageSearcher(250, testFeatures[i], true, reader), reader);
        }

    }

    private void getRecall(String prefix, ImageSearcher is, IndexReader reader) throws IOException {
        double ap5 = 0, ap10 = 0, ap20 = 0, ap30 = 0, ap50 = 0;
        HashMap<String, String[]> topics = new HashMap<String, String[]>(20);
        BufferedReader br = new BufferedReader(new FileReader(truthFile));
        String line;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (!line.startsWith("#") && line.length() > 3) {
                String[] split = line.split(":");
                String query = split[0];
                String[] result = split[1].split(" ");
                topics.put(query, result);
            }
        }
        br.close();

        StringBuilder sb = new StringBuilder(128);
        double count = 0, sum;
        double p5 = 0, p10 = 0, p20 = 0, p30 = 0, p50 = 0;
        Set<String> queries = topics.keySet();
        // iterate queries ...
        for (Iterator<String> iterator = queries.iterator(); iterator.hasNext(); ) {
            String q = iterator.next();
            sum = 0;
            BufferedImage bimg = ImageIO.read(new File(path + q));
            HashSet<String> topicResults = new HashSet<String>();
            String[] strings = topics.get(q);
            for (int i = 0; i < strings.length; i++) {
                topicResults.add(strings[i].trim());
            }
            ImageSearchHits hits = is.search(bimg, reader);
            // LSA with PHOG ...
//            LsaFilter filter = new LsaFilter(PHOG.class, new PHOG().getFieldName());
//            GenericDocumentBuilder b = new GenericDocumentBuilder(PHOG.class);
//            filter.filter(hits, b.createDocument(bimg, "--"));

            for (int i = 0; i < Math.min(50, hits.length()); i++) {
                Document d = hits.doc(i);
                String fileName = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                if (topicResults.contains(fileName.substring(fileName.lastIndexOf('\\')+1))) {
                    sum += 1;
                    sb.append('*');
                } else sb.append('.');

                if (i + 1 == 5) p5 += sum / (double) topicResults.size();
                if (i + 1 == 10) p10 += sum / (double) topicResults.size();
                if (i + 1 == 20) p20 += sum / (double) topicResults.size();
                if (i + 1 == 30) p30 += sum / (double) topicResults.size();
                if (i + 1 == 50) p50 += sum / (double) topicResults.size();
            }
//            System.out.println("sb.toString() = " + sb.toString());
            sb.delete(0, sb.length());
            count++;

            ap5 += p5 / count;
            ap10 += p10 / count;
            ap20 += p20 / count;
            ap30 += p30 / count;
            ap50 += p50 / count;
        }


        System.out.printf("%s\t%3.2f\t%3.2f\t%3.2f\t%3.2f\t%3.2f\n", prefix, ap5 / (double) topics.size(), ap10 / (double) topics.size(),
                ap20 / (double) topics.size(), ap30 / (double) topics.size(), ap50 / (double) topics.size());

    }
}
