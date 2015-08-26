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

package net.semanticmetadata.lire.benchmarking.custom;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.MMapDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by mlux on 10.07.14.   A test done with Jakub Lokoc, lokoc@ksi.ms.mff.cuni.cz
 */
public class ProfimediaTest extends TestCase {
    ParallelIndexer parallelIndexer;
    private String indexPath = "profimedia-index";
    private String testExtensive = "./testdata/Profimedia/png";
    private ChainedDocumentBuilder builder;
    String[] queries = new String[]{
            "1", "292", "575", "872", "1133", "1441", "1566", "1747", "2141", "2282", "2562", "2882", "3237", "3489", "3717", "3935", "4023", "4140", "4349", "4659", "4932", "5136", "5301", "5586", "5661", "5848", "6045", "6218", "6447", "6612", "6720", "6911", "7047", "7322", "7675", "7913", "8092", "8305", "8611", "8914", "9183", "9443", "9737", "9922", "10088", "10279", "10403", "10609", "10824", "11071", "11186", "11291", "11394", "11613", "11931", "12097", "12347", "12647", "12821", "13184", "13359", "13467", "13735", "13854", "14098", "14364", "14949", "15138", "15344", "15578", "15839", "15964", "16169", "16443", "16549", "16709", "17109", "17432", "17606", "17945", "18129", "18526", "18704", "18870", "19038", "19329", "19561", "19819", "20021", "20151", "20403", "20566", "20713", "20933", "21182", "21352", "21575", "21629", "21874", "21992"
    };

    //    private final Class feature = JCD.class;
    private Class[] features = new Class[]{
            JCD.class, CEDD.class, AutoColorCorrelogram.class, PHOG.class, OpponentHistogram.class,
            EdgeHistogram.class, ScalableColor.class, ColorLayout.class, FCTH.class, FuzzyOpponentHistogram.class, JpegCoefficientHistogram.class

    };

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        parallelIndexer = new ParallelIndexer(8, indexPath, testExtensive, true) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                for (int i = 0; i < features.length; i++) {
                    builder.addBuilder(new GenericDocumentBuilder(features[i]));
                }

            }
        };
    }

    public void testIndex() {
    }

    public void testSearch() throws IOException {
//        parallelIndexer.run();
        for (int f = 0; f < features.length; f++) {
            System.out.println(features[f].getName());
            GenericFastImageSearcher s = new GenericFastImageSearcher(21993, features[f]);
            IndexReader reader = DirectoryReader.open(MMapDirectory.open(new File(indexPath)));
            File out = new File(features[f].getName() + "_profimedia.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(out));
            for (int i = 0; i < queries.length; i++) {
                if (i%50==0) System.out.print(".");
                String query = queries[i];
                BufferedImage queryImage = ImageIO.read(new File(testExtensive + "/" + query + ".png"));
                ImageSearchHits hits = s.search(queryImage, reader);
                bw.write(query + ", ");
                for (int j = 0; j < hits.length(); j++) {
                    bw.write(hits.doc(j).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0].replace("C:\\Java\\Projects\\LireSVN\\testdata\\Profimedia\\png\\", "").replace(".png", ""));
                    bw.write(",");
                }
                bw.write("\n");
            }
            bw.close();
            System.out.println();
        }
    }
}
