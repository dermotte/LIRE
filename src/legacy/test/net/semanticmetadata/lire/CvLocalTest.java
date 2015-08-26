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
 * Updated: 19.02.15 21:00
 */

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.GenericDoubleLireFeature;
import net.semanticmetadata.lire.imageanalysis.bovw.BOVWBuilder;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSurfFeature;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.CvSurfDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.impl.custom.SingleNddCeddImageSearcher;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.IOException;

/**
 * Created by mlux on 13.02.2015.
 */
public class CvLocalTest extends TestCase {

    String indexPath = "cv_test_idx";
        private String imageDirectory = "testdata/wang-1000";
//    private String imageDirectory = "D:\\Temp\\imagew\\source_images";
    private DocumentBuilder simpleBuilder;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        simpleBuilder = new CvSurfDocumentBuilder();
    }

    public void testIndexing() throws IOException {
        ParallelIndexer indexer = new ParallelIndexer(4, indexPath, imageDirectory, true) {
            // use this to add you preferred builders.
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(simpleBuilder);
            }
        };
//        indexer.run();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        BOVWBuilder sh = new BOVWBuilder(reader, new CvSurfFeature(), 1000, 512);
        sh.index();
    }

    public void testSearch() throws IOException {
        // need to add that before the first search to make sure the index gets small again!!!!!
//        IndexWriter iw = LuceneUtils.createIndexWriter(FSDirectory.open(new File(indexPath)), false, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
//        iw.forceMerge(1);
//        iw.commit();
//        iw.close();
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        // both of these work:
        ImageSearcher is = new GenericFastImageSearcher(10, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_CVSURF + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader);
        is = new SingleNddCeddImageSearcher(reader, false, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_CVSURF + DocumentBuilder.FIELD_NAME_BOVW_VECTOR);

        String queryFile = "testdata/wang-1000/99.jpg";
//        String queryFile = "D:\\Temp\\imagew\\source_images\\04560283.jpg";
        BOVWBuilder sh = new BOVWBuilder(reader, new CvSurfFeature(), 1000, 512);
        int numSearches = 10;
        long ms = System.currentTimeMillis();
        for (int j = 0; j < numSearches; j++) {
            Document queryDocument = simpleBuilder.createDocument(ImageIO.read(new File(queryFile)), queryFile);
            queryDocument = sh.getVisualWords(queryDocument);
            ImageSearchHits hits = is.search(queryDocument, reader);
            for (int i = 0; i < hits.length(); i++) {
                System.out.println(hits.score(i) + ": " + hits.doc(i).get(DocumentBuilder.FIELD_NAME_IDENTIFIER));
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.printf("It took %.2f ms per search\n", (double) ms / (double) numSearches);
    }
}
