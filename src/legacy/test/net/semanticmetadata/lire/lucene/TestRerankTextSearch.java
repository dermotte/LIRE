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

package net.semanticmetadata.lire.lucene;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.filter.RerankFilter;
import net.semanticmetadata.lire.imageanalysis.FCTH;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A test class for image based re-ranking of results from a text search with Lucene.
 * It's based on Junit (it's either included in your IDE or just google for the jar)
 * The testdata employed is in the LIRE SVN at
 * https://code.google.com/p/lire/source/checkout
 *
 * @author Mathias Lux mathias@juggle.at
 *
 */
public class TestRerankTextSearch extends TestCase {
    // that's where we put the index for testing:
    private File testIndex = new File("textindextest");

    public void testIndexing() throws IOException, ParserConfigurationException, SAXException {
        IndexWriterConfig iwConf = new IndexWriterConfig(Version.LUCENE_42, new SimpleAnalyzer(Version.LUCENE_42));
        IndexWriter iw = new IndexWriter(FSDirectory.open(testIndex), iwConf);
        // if you want to append the index to a pre-existing one use the next line.
        // iwConf.setOpenMode(IndexWriterConfig.OpenMode.APPEND);
        // create a LIRE DocumentBuilder for extracting FCTH (just an example, every other feature will do).
        DocumentBuilder builder = DocumentBuilderFactory.getFCTHDocumentBuilder();
        ArrayList<File> files = FileUtils.getAllImageFiles(new File("testdata/ferrari"), true);
        // for handling the XML of the test data set
        SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        SAXParser saxParser = spf.newSAXParser();
        XMLReader xmlReader = saxParser.getXMLReader();
        for (Iterator<File> iterator = files.iterator(); iterator.hasNext(); ) {
            File img = iterator.next();
            String path = img.getCanonicalPath();
            // create the document with the LIRE DocumentBuilder, this adds the image features to the document.
            Document d = builder.createDocument(new FileInputStream(img), path);
            // handling the XML of the test data set
            path = path.substring(0,path.lastIndexOf('.')) + ".xml";
            TagHandler handler = new TagHandler();
            xmlReader.setContentHandler(handler);
            xmlReader.parse(new InputSource(new File(path).toURI().toString()));
            // add the text to the document ...
            d.add(new TextField("tags", handler.getTags(), Field.Store.YES));
            // don't forget to add the document to the index.
            iw.addDocument(d);
        }
        iw.close();
    }

    public void testSearch() throws IOException, ParseException {
        // create a Lucene IndexReader and the according IndexSearcher:
        IndexReader reader = DirectoryReader.open(FSDirectory.open(testIndex));
        IndexSearcher searcher = new IndexSearcher(reader);
        // The QueryParser takes a String and creates a query out of it. Make sure you use the same field
        // as for indexing, in this case "tags"
        QueryParser q = new QueryParser(Version.LUCENE_42, "tags", new SimpleAnalyzer(Version.LUCENE_42));
        // let's just take the tags of the first document in the index:
        Query query = q.parse(reader.document(1).getValues("tags")[0]);
        // now that's the actual search:
        // NOTE: The number of results here is critical. The less documents are returned here, the
        // less the image re-ranking can mess up. However, the recall (the absolute number of relevant
        // documents returned) is also influenced by this. Best to try several values like 10, 100, 200, 500, ...
        TopDocs results = searcher.search(query, 10);
        // here we print the results of the text search, just for the win.
        System.out.println("-----------> SEARCH RESULTS ...");
        for (int i = 0; i < results.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = results.scoreDocs[i];
            System.out.print(scoreDoc.score + "\t: ");
            // reader.document(scoreDoc.doc).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0] gets you the actual image file path.
            // LIRE manages all needed filed names as static Strings in DocumentBuilder ...
            System.out.print(reader.document(scoreDoc.doc).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0] + " -> ");
            System.out.println(reader.document(scoreDoc.doc).getValues("tags")[0]);
        }
        // just for a visual example ... this will pop up a browser window
        FileUtils.browseUri(FileUtils.saveImageResultsToHtml("text", results, reader, reader.document(1).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]));

        // and now for the re-ranking:
        // make sure to use a low level feature that has been indexed -- check the DocumentBuilder in above method.
        RerankFilter rerank = new RerankFilter(FCTH.class, DocumentBuilder.FIELD_NAME_FCTH);
        // note that you need the document here, it contains the low level feature ...
        // if you don't have it but just the image you need to create a new one with the
        // appropriate DocumentBuilder -- check the DocumentBuilder in above method.
        ImageSearchHits hitsReranked = rerank.filter(results, reader, reader.document(1));
        // and here we print the re-ranked hits:
        System.out.println("-----------> RERANKED ...");
        for (int i = 0; i < hitsReranked.length(); i++) {
            System.out.print(hitsReranked.score(i) + "\t: ");
            System.out.print(hitsReranked.doc(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0] + " -> ");
            System.out.println(hitsReranked.doc(i).getValues("tags")[0]);
        }
        // just for a visual example ... this will pop up a browser window.
        FileUtils.browseUri(FileUtils.saveImageResultsToHtml("reranked", hitsReranked, reader.document(1).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]));
    }

    // handling the XML of the test data set, we just want the tags.
    // no need for that if you get the text elsewhere.
    class TagHandler extends DefaultHandler {
        StringBuilder sb = new StringBuilder(1024);
        boolean inTag = false;

        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            if (localName.startsWith("tag")) inTag = true;
            super.startElement(uri, localName, qName, attributes);
        }

        public void endElement(String uri, String localName, String qName) throws SAXException {
            if (localName.startsWith("tag")) {
                inTag = false;
                sb.append(' ');
            }
            super.endElement(uri, localName, qName);
        }

        public void characters(char[] ch, int start, int length) throws SAXException {
            if (inTag) {
                sb.append(ch, start, length);
            }
        }

        public String getTags() {
            return sb.toString().trim();
        }
    }
}
