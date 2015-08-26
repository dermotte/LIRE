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
 * Updated: 03.08.13 09:07
 */

package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.imageanalysis.bovw.SimpleFeatureBOVWBuilder;
import net.semanticmetadata.lire.impl.*;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;

import javax.imageio.ImageIO;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: nek.anag
 * Date: 25.02.15
 * Time: 15:30
 */
public class TestSimple extends TestCase {
    //myDatabase
    private String indexPath = "myDatabase-index";
//    private final String indexPath = "myDatabase-index-000000000";
    private final String collectionPath = "C:\\myDatabase";
    private final String queriesOutsideCollectionPath = "C:\\qimages";
    private final String queriesFile = "queriesFile.txt";

    private int sample = 2000;  //Sample to create codebook
    private int clusters = 512; //Number of clusters for codebook

    private HashSet<String> allQueries;
    private LinkedList<Document> outsideQueries;

    ParallelIndexer parallelIndexer;

    protected void setUp() throws Exception {
        super.setUp();
        indexPath += "-" + System.currentTimeMillis() % (1000 * 60 * 60 * 24 * 7);
        // Setting up DocumentBuilder:
        parallelIndexer = new ParallelIndexer(16, indexPath, collectionPath, true) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                //Tests for SIMPLE
                builder.addBuilder(new SimpleBuilder(new CEDD(), SimpleBuilder.KeypointDetector.Random));
//                builder.addBuilder(new SimpleBuilder(new FCTH(), SimpleBuilder.KeypointDetector.Random));
//                builder.addBuilder(new SimpleBuilder(new JCD(), SimpleBuilder.KeypointDetector.Random));
            }
        };

        // Getting the queries:
        BufferedReader br = new BufferedReader(new FileReader(queriesFile));
        String line;
        allQueries = new HashSet<String>(100);
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#") || line.length() < 4) continue;
            else {
                allQueries.add(line.trim());
            }
        }
    }

    public void testMAP() throws IOException {
///*

        // INDEXING ...
        parallelIndexer.run();

        System.out.println("** SIMPLE BoVW using CEDD and Rand");
        SimpleFeatureBOVWBuilder simpleBovwBuilderCEDD = new SimpleFeatureBOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new CEDD(), SimpleBuilder.KeypointDetector.Random, sample, clusters);
//        simpleBovwBuilderCEDD.setDeleteLocalFeatures(false);
        simpleBovwBuilderCEDD.index();

//        System.out.println("** SIMPLE BoVW using FCTH and Rand");
//        SimpleFeatureBOVWBuilder simpleBovwBuilderFCTH = new SimpleFeatureBOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new FCTH(), SimpleBuilder.KeypointDetector.Random, sample, clusters);
////        simpleBovwBuilderFCTH.setDeleteLocalFeatures(false);
//        simpleBovwBuilderFCTH.index();
//
//        System.out.println("** SIMPLE BoVW using JCD and Rand");
//        SimpleFeatureBOVWBuilder simpleBovwBuilderJCD = new SimpleFeatureBOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new JCD(), SimpleBuilder.KeypointDetector.Random, sample, clusters);
//        simpleBovwBuilderJCD.index();

//*/


        // Read queries tha are not included in the collection
        outsideQueries = new LinkedList<Document>();
        outsideQueries.clear();
        if (queriesOutsideCollectionPath != null)
        {
            SimpleFeatureBOVWBuilder sCEDDBuilderforQueries = new SimpleFeatureBOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new CEDD(), SimpleBuilder.KeypointDetector.Random);
//            SimpleFeatureBOVWBuilder sFCTHBuilderforQueries = new SimpleFeatureBOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new FCTH(), SimpleBuilder.KeypointDetector.Random);
//            SimpleFeatureBOVWBuilder sJCDBuilderforQueries = new SimpleFeatureBOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new JCD(), SimpleBuilder.KeypointDetector.Random);
            ChainedDocumentBuilder documentBuilder = new ChainedDocumentBuilder();
            parallelIndexer.addBuilders(documentBuilder);
            System.out.println("Getting all queries in " + queriesOutsideCollectionPath + ".");
            List<String> files = files = FileUtils.getAllImages(new File(queriesOutsideCollectionPath), true);
            Document query;
            String path;
            for (Iterator<String> iterator = files.iterator(); iterator.hasNext(); ) {
                path = iterator.next();
                query = documentBuilder.createDocument(ImageIO.read(new File(path)), path);
                query = sCEDDBuilderforQueries.getVisualWords(query);
//                query = sFCTHBuilderforQueries.getVisualWords(query);
//                query = sJCDBuilderforQueries.getVisualWords(query);
                outsideQueries.add(query);
            }
        }

        // SEARCHING
        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(new File(indexPath)), IOContext.READONCE));

        System.out.println("Searching...");

        doSearch(new GenericFastImageSearcher(30, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.Random, new CEDD()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "SimpleCEDDRand", reader);
//        doSearch(new GenericFastImageSearcher(30, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.Random, new FCTH()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "SimpleFCTHRand", reader);
//        doSearch(new GenericFastImageSearcher(30, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.Random, new JCD()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "SimpleJCDRand", reader);
//
        //perform weighting schemes
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.Random, new CEDD()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "SimpleCEDDRand", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.Random, new FCTH()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "SimpleFCTHRand", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.Random, new JCD()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "SimpleJCDRand", reader);

    }

    public void performWSs (String fieldName, String prefix, IndexReader reader) throws IOException
    {
//        doSearch(new ImageSearcherUsingWSs(30, GenericDoubleLireFeature.class, fieldName, true, reader, false, false, false), prefix, reader);
//        doSearch(new ImageSearcherUsingWSs(30, GenericDoubleLireFeature.class, fieldName, true, reader, false, false, true), prefix, reader);
//        doSearch(new ImageSearcherUsingWSs(30, GenericDoubleLireFeature.class, fieldName, true, reader, false, true, false), prefix, reader);
//        doSearch(new ImageSearcherUsingWSs(30, GenericDoubleLireFeature.class, fieldName, true, reader, false, true, true), prefix, reader);
//        doSearch(new ImageSearcherUsingWSs(30, GenericDoubleLireFeature.class, fieldName, true, reader, true, false, false), prefix, reader);
        doSearch(new ImageSearcherUsingWSs(30, GenericDoubleLireFeature.class, fieldName, true, reader, true, false, true), prefix, reader);
//        doSearch(new ImageSearcherUsingWSs(30, GenericDoubleLireFeature.class, fieldName, true, reader, true, true, false), prefix, reader);
//        doSearch(new ImageSearcherUsingWSs(30, GenericDoubleLireFeature.class, fieldName, true, reader, true, true, true), prefix, reader);
    }

    private void doSearch(ImageSearcher searcher, String prefix, IndexReader reader) throws IOException {
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        String fileName, fullFileName;
        Document queryDoc;
        ImageSearchHits hits;
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.
            fullFileName = reader.document(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            fileName = getIDfromFileName(fullFileName);
            if (allQueries.contains(fileName)) {
                // ok, we've got a query here for a document ...
                queryDoc = reader.document(i);
                hits = searcher.search(queryDoc, reader);
                FileUtils.browseUri(FileUtils.saveImageResultsToHtml(prefix + "-" + fileName, hits, fullFileName));
            }
        }
        for (int i = 0; i < outsideQueries.size(); i++) {
            fullFileName = outsideQueries.get(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            fileName = getIDfromFileName(fullFileName);
            if (allQueries.contains(fileName)) {
                // ok, we've got a query here for a document ...
                queryDoc = outsideQueries.get(i);
                hits = searcher.search(queryDoc, reader);
                FileUtils.browseUri(FileUtils.saveImageResultsToHtml(prefix + "-" + fileName, hits, fullFileName));
            }
        }
    }

    private String getIDfromFileName(String path) {
        // That's the one for Windows. Change for Linux ...
        return path.substring(path.lastIndexOf('\\') + 1);
    }
}
