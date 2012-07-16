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
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.*;

/**
 * User: mlux
 * Date: 14.05.2009
 * Time: 15:07:43
 */
public class MetricSpacesTest extends TestCase {
    String indexPath = "./test-index-cedd-flickr";
    // String imagePath = "";

    public void testIndexing() throws IOException {
        MetricSpacesInvertedListIndexing ms = MetricSpacesInvertedListIndexing.getDefaultInstance();
        MetricSpacesInvertedListIndexing.numReferenceObjectsUsed = 10;
        MetricSpacesInvertedListIndexing.numReferenceObjects = 50;
        ms.setProgress(new ProgressIndicator() {
            @Override
            public void setCurrentState(MetricSpacesInvertedListIndexing.State currentState) {
                super.setCurrentState(currentState);
                System.out.println("currentState = " + currentState);
            }

            @Override
            public void setNumDocsProcessed(int numDocsProcessed) {
                super.setNumDocsProcessed(numDocsProcessed);
                if (numDocsProcessed % 100 == 0) System.out.println("numDocsProcessed = " + numDocsProcessed);
            }
        });
        ms.createIndex(indexPath);
    }

    public void testSearch() throws IOException {
        int docNumber = 1;
        MetricSpacesInvertedListIndexing ms = MetricSpacesInvertedListIndexing.getDefaultInstance();
        MetricSpacesInvertedListIndexing.numReferenceObjectsUsed = 10;
        MetricSpacesInvertedListIndexing.numReferenceObjects = 50;
        IndexReader reader = ms.getIndexReader(indexPath);
        TopDocs docs = ms.search(reader.document(docNumber), indexPath);

        // print the results
        BufferedWriter bw = new BufferedWriter(new FileWriter("out.html"));
        bw.write("<html><body>");
        for (int i = 0; i < docs.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = docs.scoreDocs[i];
            bw.write("<img title=\"ID: " + scoreDoc.doc + ", " +
                    "Score: " + scoreDoc.score + "\" src=\"file:///" + reader.document(scoreDoc.doc).getValues("descriptorImageIdentifier")[0] + "\"> ");
        }
        bw.write("</body></html>");
        bw.close();
        showUrl("out.html");

    }

    public void testPerformance() throws IOException {
        MetricSpacesInvertedListIndexing mes = MetricSpacesInvertedListIndexing.getDefaultInstance();
        int numSearches = 10;
        IndexReader reader = mes.getIndexReader(indexPath);
        System.out.println(reader.maxDoc() + " documents");
        TopDocs docs;

        long ms = System.currentTimeMillis();
        for (int i = 0; i < numSearches; i++) {
            docs = mes.search(reader.document(i), indexPath);
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("ms = " + ms);


        ImageSearcher ceddSearcher = ImageSearcherFactory.createCEDDImageSearcher(100);
        ms = System.currentTimeMillis();
        for (int i = 0; i < numSearches; i++) {
            ceddSearcher.search(reader.document(i), reader);
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("ms = " + ms);
    }

    private void showUrl(String url) {
        String osName = System.getProperty("os.name");
        // take linux settings
        String browserCmd = "firefox {url}";
        // or windows in case of windows :)
        if (osName.toLowerCase().indexOf("windows") > -1) {
            browserCmd = "cmd.exe /c start \"\" \"{url}\"";
        }
        browserCmd = browserCmd.replace("{url}", new File(url).getAbsolutePath());
        try {
            System.out.println("browserCmd = " + browserCmd);
            Runtime.getRuntime().exec(browserCmd);
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }

    /**
     * Test routine for creation and update.
     *
     * @throws IOException
     */
    public void testIndexSmall() throws IOException {
        String smallIdx = "wang-cedd";
        MetricSpacesInvertedListIndexing ms = MetricSpacesInvertedListIndexing.getDefaultInstance();
        MetricSpacesInvertedListIndexing.numReferenceObjectsUsed = 10;
        MetricSpacesInvertedListIndexing.numReferenceObjects = 50;
        ms.setProgress(new ProgressIndicator() {
            @Override
            public void setCurrentState(MetricSpacesInvertedListIndexing.State currentState) {
                super.setCurrentState(currentState);
                System.out.println("currentState = " + currentState);
            }

            @Override
            public void setNumDocsProcessed(int numDocsProcessed) {
                super.setNumDocsProcessed(numDocsProcessed);
                if (numDocsProcessed % 100 == 0) System.out.println("numDocsProcessed = " + numDocsProcessed);
            }
        });
        ms.createIndex(smallIdx);
    }

    public void testMetrics() throws IOException {
        String smallIdx = "wang-cedd";
        MetricSpacesInvertedListIndexing ms = MetricSpacesInvertedListIndexing.getDefaultInstance();
        MetricSpacesInvertedListIndexing.numReferenceObjectsUsed = 10;
        MetricSpacesInvertedListIndexing.numReferenceObjects = 50;

        TopDocs docs = ms.search(ImageIO.read(new FileInputStream("wang-data-1000/10.jpg")), smallIdx);
        IndexReader ir = IndexReader.open(FSDirectory.open(new File(smallIdx)));
        for (int i = 0; i < docs.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = docs.scoreDocs[i];
            String identifier = ir.document(scoreDoc.doc).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(scoreDoc.score + ": " + identifier + " \t(" + scoreDoc.doc + ")");

        }
    }
}
