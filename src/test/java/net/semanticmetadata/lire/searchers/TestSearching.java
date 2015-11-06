/*
 * This file is part of the LIRE project: http://lire-project.net
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
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval -
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
 */

package net.semanticmetadata.lire.searchers;

import junit.framework.TestCase;
import net.semanticmetadata.lire.aggregators.AbstractAggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
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
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;

/**
 * Created by Nektarios on 28/5/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class TestSearching extends TestCase {
    Class<? extends GlobalFeature> globalFeatureClass = CEDD.class;
    Class<? extends LocalFeatureExtractor> localFeatureClass = CvSurfExtractor.class;
    SimpleExtractor.KeypointDetector keypointDetector = SimpleExtractor.KeypointDetector.CVSURF;
    Class<? extends AbstractAggregator> aggregatorClass = BOVW.class;

    private final String indexPath = "test-index";
    private final String indexPathSeparate = "test-separate";
    private final String testExtensive = "testdata/ferrari";
    private final String testExtensiveBlack = "testdata/ferrari/black";
    private final String testExtensiveRed = "testdata/ferrari/red";
    private final String testExtensiveWhite = "testdata/ferrari/white";
    private final String testExtensiveYellow = "testdata/ferrari/yellow";

    String codebookPath = "./src/test/resources/codebooks/";

    String imageToSearch = "testdata/ferrari/red/3862801353_58634506b4_b.jpg";

    private int numOfDocsForVocabulary = 500;
    private int numOfClusters = 512;

    public void testSearch() throws IOException, IllegalAccessException, InstantiationException {
        Cluster[] cvsurf512 = Cluster.readClusters(codebookPath + "CvSURF512");
        Cluster[] simpleceddcvsurf512 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD512");

        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numOfClusters, numOfDocsForVocabulary, aggregatorClass);
        parallelIndexer.addExtractor(globalFeatureClass);
        parallelIndexer.addExtractor(localFeatureClass, cvsurf512);
        parallelIndexer.addExtractor(globalFeatureClass, keypointDetector, simpleceddcvsurf512);
        parallelIndexer.run();

        BufferedImage image = ImageIO.read(new FileInputStream(imageToSearch));

        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(Paths.get(indexPath)), IOContext.READONCE));
        System.out.println("Documents in the reader: " + reader.maxDoc());

        GenericFastImageSearcher ceddSearcher = new GenericFastImageSearcher(10, globalFeatureClass, true, reader);
        ImageSearchHits ceddhits = ceddSearcher.search(image, reader);
        String hitFile;
        for (int y = 0; y < ceddhits.length(); y++) {
            hitFile = reader.document(ceddhits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
            System.out.println(y + ". " + hitFile + " " + ceddhits.score(y));
        }
        System.out.println();

        GenericFastImageSearcher cvsurfsearcher = new GenericFastImageSearcher(10, localFeatureClass, aggregatorClass.newInstance(), 512, true, reader, indexPath + ".config");
        ImageSearchHits cvsurfhits = cvsurfsearcher.search(image, reader);
        for (int y = 0; y < cvsurfhits.length(); y++) {
            hitFile = reader.document(cvsurfhits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
            System.out.println(y + ". " + hitFile + " " + cvsurfhits.score(y));
        }
        System.out.println();

        GenericFastImageSearcher simpleceddcvsurfsearcher = new GenericFastImageSearcher(10, globalFeatureClass, keypointDetector, aggregatorClass.newInstance(), 512, true, reader, indexPath + ".config");
        ImageSearchHits simpleceddcvsurfhits = simpleceddcvsurfsearcher.search(image, reader);
        for (int y = 0; y < simpleceddcvsurfhits.length(); y++) {
            hitFile = reader.document(simpleceddcvsurfhits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
            System.out.println(y + ". " + hitFile + " " + simpleceddcvsurfhits.score(y));
        }
        System.out.println();
    }

    public void testSearchMulImages() throws IOException, IllegalAccessException, InstantiationException {
        Cluster[] cvsurf512 = Cluster.readClusters(codebookPath + "CvSURF512");
        Cluster[] simpleceddcvsurf512 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD512");

        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numOfClusters, numOfDocsForVocabulary, aggregatorClass);
        parallelIndexer.addExtractor(globalFeatureClass);
        parallelIndexer.addExtractor(localFeatureClass, cvsurf512);
        parallelIndexer.addExtractor(globalFeatureClass, keypointDetector, simpleceddcvsurf512);
        parallelIndexer.run();

        ArrayList<String> images = FileUtils.readFileLines(new File(testExtensive), true);

        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(Paths.get(indexPath)), IOContext.READONCE));
        System.out.println("Documents in the reader: " + reader.maxDoc());

        GenericFastImageSearcher ceddSearcher = new GenericFastImageSearcher(1, globalFeatureClass, true, reader);
        GenericFastImageSearcher cvsurfsearcher = new GenericFastImageSearcher(1, localFeatureClass, aggregatorClass.newInstance(), 512, true, reader, indexPath + ".config");
        GenericFastImageSearcher simpleceddcvsurfsearcher = new GenericFastImageSearcher(1, globalFeatureClass, keypointDetector, aggregatorClass.newInstance(), 512, true, reader, indexPath + ".config");


        BufferedImage image;
        ImageSearchHits ceddhits, cvsurfhits, simpleceddcvsurfhits;
        String hitFile;
        int counter = 0;
        for(String next : images){
            image = ImageIO.read(new FileInputStream(next));
            next = next.substring(next.lastIndexOf('\\') + 1);
            System.out.println(counter + " ~ " + next);

            ceddhits = ceddSearcher.search(image, reader);
            hitFile = reader.document(ceddhits.documentID(0)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
            if (next.equals(hitFile))
                System.out.println(0 + ". " + hitFile + " " + ceddhits.score(0));
            else
                System.err.println("ERROR " + hitFile + " " + ceddhits.score(0) + " ERROR");

            cvsurfhits = cvsurfsearcher.search(image, reader);
            hitFile = reader.document(cvsurfhits.documentID(0)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
            if (next.equals(hitFile))
                System.out.println(0 + ". " + hitFile + " " + cvsurfhits.score(0));
            else
                System.err.println("ERROR " + hitFile + " " + cvsurfhits.score(0)+ " ERROR");

            simpleceddcvsurfhits = simpleceddcvsurfsearcher.search(image, reader);
            hitFile = reader.document(simpleceddcvsurfhits.documentID(0)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
            if (next.equals(hitFile))
                System.out.println(0 + ". " + hitFile + " " + simpleceddcvsurfhits.score(0));
            else
                System.err.println("ERROR " + hitFile + " " + simpleceddcvsurfhits.score(0)+ " ERROR");

            counter++;
            System.out.println();
        }
    }

    public void testSeparateIndex() throws IOException, IllegalAccessException, InstantiationException {
        Cluster[] cvsurf512 = Cluster.readClusters(codebookPath + "CvSURF512");
        Cluster[] simpleceddcvsurf512 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD512");

        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensiveRed, numOfClusters, numOfDocsForVocabulary, aggregatorClass);
        parallelIndexer.addExtractor(globalFeatureClass);
        parallelIndexer.addExtractor(localFeatureClass, cvsurf512);
        parallelIndexer.addExtractor(globalFeatureClass, keypointDetector, simpleceddcvsurf512);
        parallelIndexer.run();

        ParallelIndexer parallelIndexerSeparate = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPathSeparate, testExtensiveBlack, indexPath);
        parallelIndexerSeparate.run();

        IndexReader readerIndex = DirectoryReader.open(new RAMDirectory(FSDirectory.open(Paths.get(indexPath)), IOContext.READONCE));
        System.out.println("Documents in the reader: " + readerIndex.maxDoc());

        IndexReader readerQueries = DirectoryReader.open(new RAMDirectory(FSDirectory.open(Paths.get(indexPathSeparate)), IOContext.READONCE));
        System.out.println("Documents in the reader: " + readerQueries.maxDoc());

        GenericFastImageSearcher ceddSearcher = new GenericFastImageSearcher(5, globalFeatureClass, true, readerIndex);
        GenericFastImageSearcher cvsurfsearcher = new GenericFastImageSearcher(5, localFeatureClass, aggregatorClass.newInstance(), 512, true, readerIndex, indexPath + ".config");
        GenericFastImageSearcher simpleceddcvsurfsearcher = new GenericFastImageSearcher(5, globalFeatureClass, keypointDetector, aggregatorClass.newInstance(), 512, true, readerIndex, indexPath + ".config");

        Bits liveDocs = MultiFields.getLiveDocs(readerQueries);

        ImageSearchHits ceddhits, cvsurfhits, simpleceddcvsurfhits;
        Document queryDoc;
        String queryfile, hitFile;
        int counter = 0;
        for (int i = 0; i < readerQueries.maxDoc(); i++) {
            if (readerQueries.hasDeletions() && !liveDocs.get(i)) continue;

            queryDoc = readerQueries.document(i);
            queryfile = queryDoc.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            queryfile = queryfile.substring(queryfile.lastIndexOf('\\') + 1);
            System.out.println(counter + ". Query image: " + queryfile);
            ceddhits = ceddSearcher.search(queryDoc, readerIndex);
            cvsurfhits = cvsurfsearcher.search(queryDoc, readerIndex);
            simpleceddcvsurfhits = simpleceddcvsurfsearcher.search(queryDoc, readerIndex);

            System.out.println("Global:");
            for (int y = 0; y < ceddhits.length(); y++) {
                hitFile = readerIndex.document(ceddhits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
                System.out.println(y + ". " + hitFile + " " + ceddhits.score(y));
            }

            System.out.println("Local:");
            for (int y = 0; y < cvsurfhits.length(); y++) {
                hitFile = readerIndex.document(cvsurfhits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
                System.out.println(y + ". " + hitFile + " " + cvsurfhits.score(y));
            }

            System.out.println("Simple:");
            for (int y = 0; y < simpleceddcvsurfhits.length(); y++) {
                hitFile = readerIndex.document(simpleceddcvsurfhits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                hitFile = hitFile.substring(hitFile.lastIndexOf('\\') + 1);
                System.out.println(y + ". " + hitFile + " " + simpleceddcvsurfhits.score(y));
            }
            System.out.println();
            counter++;
        }
    }
}
