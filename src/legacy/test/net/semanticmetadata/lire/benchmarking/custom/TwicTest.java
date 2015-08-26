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
public class TwicTest extends TestCase {
    ParallelIndexer parallelIndexer;
    private String indexPath = "twic-index";
    private String testExtensive = "./testdata/TWIC/png";
    private ChainedDocumentBuilder builder;
    String[] queries = new String[]{
            "60", "142", "217", "278", "309", "423", "471", "520", "596", "664", "701", "806", "855", "942", "1007", "1052", "1105", "1133", "1180", "1248", "1301", "1369", "1412", "1461", "1497", "1563", "1668", "1732", "1802", "1870", "1879", "1927", "1987", "2034", "2135", "2179", "2211", "2342", "2345", "2418", "2506", "2608", "2657", "2755", "2825", "2844", "2952", "2999", "3113", "3147", "3192", "3292", "3366", "3435", "3443", "3559", "3588", "3666", "3740", "3782", "3825", "3916", "3983", "4004", "4087", "4139", "4200", "4240", "4305", "4380", "4425", "4444", "4525", "4572", "4666", "4702", "4792", "4833", "4893", "4949", "5017", "5067", "5084", "5197", "5234", "5348", "5401", "5448", "5498", "5551", "5580", "5622", "5701", "5723", "5796", "5829", "5905", "6011", "6072", "6124", "6134", "6221", "6257", "6315", "6338", "6451", "6498", "6520", "6567", "6645", "6712", "6795", "6859", "6886", "6947", "6994", "7039", "7097", "7143", "7197", "7232", "7283", "7361", "7384", "7439", "7485", "7549", "7572", "7638", "7699", "7753", "7811", "7871", "7924", "7951", "8007", "8053", "8123", "8152", "8211", "8288", "8345", "8365", "8444", "8491", "8549", "8586", "8656", "8671", "8767", "8782", "8865", "8905", "8958", "9001", "9056", "9123", "9151", "9203", "9264", "9301", "9378", "9415", "9520", "9579", "9620", "9661", "9699", "9750", "9821", "9892", "9926", "9991", "10020", "10114", "10164", "10200", "10245", "10282", "10341", "10414", "10455", "10499", "10543", "10580", "10632", "10731", "10827", "10886", "10950", "11001", "11033", "11084", "11152", "11176", "11247", "11307", "11378", "11411", "11498"
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
            GenericFastImageSearcher s = new GenericFastImageSearcher(11555, features[f]);
            IndexReader reader = DirectoryReader.open(MMapDirectory.open(new File(indexPath)));
            File out = new File(features[f].getName() + "_twic.csv");
            BufferedWriter bw = new BufferedWriter(new FileWriter(out));
            for (int i = 0; i < queries.length; i++) {
                if (i%50==0) System.out.print(".");
                String query = queries[i];
                BufferedImage queryImage = ImageIO.read(new File(testExtensive + "/" + query + ".png"));
                ImageSearchHits hits = s.search(queryImage, reader);
                bw.write(query + ", ");
                for (int j = 0; j < hits.length(); j++) {
                    bw.write(hits.doc(j).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0].replace("C:\\Java\\Projects\\LireSVN\\testdata\\TWIC\\png\\", "").replace(".png", ""));
                    bw.write(",");
                }
                bw.write("\n");
            }
            bw.close();
            System.out.println();
        }
    }
}
