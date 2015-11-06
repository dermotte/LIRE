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

package net.semanticmetadata.lire.indexers;

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

import java.io.IOException;
import java.util.LinkedList;

/**
 * Created by Nektarios on 28/5/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class TestIndexing extends TestCase {
    Class<? extends GlobalFeature> globalFeatureClass = CEDD.class;
    Class<? extends LocalFeatureExtractor> localFeatureClass = CvSurfExtractor.class;
    SimpleExtractor.KeypointDetector keypointDetector = SimpleExtractor.KeypointDetector.CVSURF;
    Class<? extends AbstractAggregator> aggregatorClass = BOVW.class;

    private final String indexPath = "test-index";
    private final String indexPathSeparate = "test-separate";
    private final String testExtensive = "testdata/UCID_png";
    //    private final String testExtensive = "testdata/ferrari";
    private final String testExtensiveBlack = "testdata/ferrari/black";
    private final String testExtensiveRed = "testdata/ferrari/red";
    private final String testExtensiveWhite = "testdata/ferrari/white";
    private final String testExtensiveYellow = "testdata/ferrari/yellow";

    String codebookPath = "./src/test/resources/codebooks/";

    private int numOfDocsForVocabulary = 500;
//    private int numOfClusters = 512;
    private int[] numsOfClusters = new int[] {32, 128};


    //Create new index
    public void testCreateNewIndex() throws IOException {
//        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive);
//        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numOfClusters, numOfDocsForVocabulary);
//        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numOfClusters, numOfDocsForVocabulary, aggregatorClass);
//
//        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numsOfClusters, numOfDocsForVocabulary);
//        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numsOfClusters, numOfDocsForVocabulary, aggregatorClass);

        Cluster[] codebook32 = Cluster.readClusters(codebookPath + "CvSURF32");
        Cluster[] codebook128 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD128");
        Cluster[] codebook512 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD512");

        LinkedList<Cluster[]> myList = new LinkedList<Cluster[]>();
        myList.add(codebook128);
        myList.add(codebook512);

        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numsOfClusters, numOfDocsForVocabulary, aggregatorClass);
        parallelIndexer.addExtractor(globalFeatureClass);
        parallelIndexer.addExtractor(localFeatureClass);
//        parallelIndexer.addExtractor(localFeatureClass, codebook32);
        parallelIndexer.addExtractor(globalFeatureClass, keypointDetector, myList);
        parallelIndexer.run();
    }

    //APPEND
    public void testAppendExistingIndex() throws IOException {
        //Create an index
        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numsOfClusters, numOfDocsForVocabulary, aggregatorClass);
        parallelIndexer.addExtractor(globalFeatureClass);
        parallelIndexer.addExtractor(localFeatureClass);
        parallelIndexer.addExtractor(globalFeatureClass, keypointDetector);
        parallelIndexer.run();

        //Append new images in that index
        ParallelIndexer parallelIndexerAppend = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensiveYellow, false);
        parallelIndexerAppend.run();
    }

    //USE EXISTING SETUP
    public void testIndexUsingExistingSetup(){
        //Create an index
        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numsOfClusters, numOfDocsForVocabulary, aggregatorClass);
        parallelIndexer.addExtractor(globalFeatureClass);
        parallelIndexer.addExtractor(localFeatureClass);
        parallelIndexer.addExtractor(globalFeatureClass, keypointDetector);
        parallelIndexer.run();

        //Create index using another indexe's setup
        ParallelIndexer parallelIndexerSeparate = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPathSeparate, testExtensiveRed, indexPath);
        parallelIndexerSeparate.run();
    }

}
