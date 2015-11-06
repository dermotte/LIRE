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
import net.semanticmetadata.lire.aggregators.AbstractAggregator;
import net.semanticmetadata.lire.aggregators.Aggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearcherUsingWSs;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Bits;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Nektarios on 15/11/2014.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class TestZuBuD extends TestCase {

    //ZuBuD
    private String db = "ZuBuD";
    private String indexPath = "zubud-index";
    private String indexPathQueries = "zubud-queries";
    private String testExtensive = "testdata/png-ZuBuD";
    private String testExtensiveQueries = "testdata/qimage";
    private final String groundTruth = "testdata/queries/ZuBuDQueries.txt";


    private int numOfDocsForVocabulary = 500;
    private Class<? extends AbstractAggregator> aggregator = BOVW.class;
    private int[] numOfClusters = new int[] {32, 128, 512, 2048};

//    private Class<? extends AbstractAggregator> aggregator = VLAD.class;
//    private int[] numOfClusters = new int[] {16, 64};

    private HashMap<String, List<String>> queries;
    private HashMap<String, Integer> query2id;

    protected void setUp() throws Exception {
        super.setUp();
        long time = System.currentTimeMillis() % (1000 * 60 * 60 * 24 * 7);
        indexPath += "-" + time;
        indexPathQueries += "-" + time;

        // Getting the queries:
        BufferedReader br = new BufferedReader(new FileReader(groundTruth));
        String line;
        queries = new HashMap<String, List<String>>(260);
        query2id = new HashMap<String, Integer>(260);
        int qID = 1;
        String currentQuery = null;
        LinkedList<String> results = null;
        while ((line = br.readLine()) != null) {
            line = line.trim();
            if (line.startsWith("#") || line.length() < 4)
                continue;
            else {
                if (line.endsWith(":")) {
                    if (currentQuery != null) {
                        queries.put(currentQuery, results);
                        query2id.put(currentQuery, qID);
                        qID++;
                    }
                    currentQuery = line.replace(':', ' ').trim();
                    results = new LinkedList<String>();
                } else {
                    results.add(line);
                }
            }
        }
        queries.put(currentQuery, results);
        query2id.put(currentQuery, qID);
    }

    public void testMAP() throws IOException {
        // INDEXING ...
        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, numOfClusters, numOfDocsForVocabulary, aggregator);

        //GLOBALS
//        parallelIndexer.addExtractor(ACCID.class);
        parallelIndexer.addExtractor(CEDD.class);
//        parallelIndexer.addExtractor(FCTH.class);
//        parallelIndexer.addExtractor(JCD.class);
//        parallelIndexer.addExtractor(AutoColorCorrelogram.class);
//        parallelIndexer.addExtractor(BinaryPatternsPyramid.class);
//        parallelIndexer.addExtractor(FuzzyColorHistogram.class);
//        parallelIndexer.addExtractor(FuzzyOpponentHistogram.class);
//        parallelIndexer.addExtractor(Gabor.class);
//        parallelIndexer.addExtractor(JpegCoefficientHistogram.class);
//        parallelIndexer.addExtractor(LocalBinaryPatterns.class);
//        parallelIndexer.addExtractor(LuminanceLayout.class);
//        parallelIndexer.addExtractor(OpponentHistogram.class);
//        parallelIndexer.addExtractor(PHOG.class);
//        parallelIndexer.addExtractor(RotationInvariantLocalBinaryPatterns.class);
//        parallelIndexer.addExtractor(SimpleColorHistogram.class);
//        parallelIndexer.addExtractor(Tamura.class);
//        parallelIndexer.addExtractor(JointHistogram.class);
//        parallelIndexer.addExtractor(LocalBinaryPatternsAndOpponent.class);
//        parallelIndexer.addExtractor(RankAndOpponent.class);
//        parallelIndexer.addExtractor(ColorLayout.class);
//        parallelIndexer.addExtractor(EdgeHistogram.class);
//        parallelIndexer.addExtractor(ScalableColor.class);
//        parallelIndexer.addExtractor(SPCEDD.class);
//        parallelIndexer.addExtractor(SPJCD.class);
//        parallelIndexer.addExtractor(SPFCTH.class);
//        parallelIndexer.addExtractor(SPACC.class);
//        parallelIndexer.addExtractor(SPLBP.class);

        //SIMPLE
//        parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSURF);
//        parallelIndexer.addExtractor(FCTH.class, SimpleExtractor.KeypointDetector.CVSURF);
        parallelIndexer.addExtractor(JCD.class, SimpleExtractor.KeypointDetector.CVSURF);
//        parallelIndexer.addExtractor(AutoColorCorrelogram.class, SimpleExtractor.KeypointDetector.CVSURF);
//        parallelIndexer.addExtractor(OpponentHistogram.class, SimpleExtractor.KeypointDetector.CVSURF);
//        parallelIndexer.addExtractor(ColorLayout.class, SimpleExtractor.KeypointDetector.CVSURF);
//        parallelIndexer.addExtractor(EdgeHistogram.class, SimpleExtractor.KeypointDetector.CVSURF);
//        parallelIndexer.addExtractor(ScalableColor.class, SimpleExtractor.KeypointDetector.CVSURF);


        //LOCAL
        parallelIndexer.addExtractor(CvSurfExtractor.class);
//        parallelIndexer.addExtractor(CvSiftExtractor.class);
//        parallelIndexer.addExtractor(SurfExtractor.class);
//        parallelIndexer.addExtractor(SiftExtractor.class);
//        parallelIndexer.addExtractor(SelfSimilaritiesExtractor.class);

        parallelIndexer.run();

        ParallelIndexer parallelIndexerSeparate = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPathQueries, testExtensiveQueries, indexPath);
        parallelIndexerSeparate.run();


        // SEARCHING
        IndexReader readerIndex = DirectoryReader.open(new RAMDirectory(FSDirectory.open(Paths.get(indexPath)), IOContext.READONCE));
        System.out.println("Documents in the reader: " + readerIndex.maxDoc());

        IndexReader readerQueries = DirectoryReader.open(new RAMDirectory(FSDirectory.open(Paths.get(indexPathQueries)), IOContext.READONCE));
        System.out.println("Documents in the reader: " + readerQueries.maxDoc());


        System.out.println("Feature\tMAP\tp@10\tER");

        long start = System.currentTimeMillis();
//
//        computeMAP(new GenericFastImageSearcher(1000, ACCID.class, true, readerIndex), "ACCID", readerIndex, readerQueries);
        computeMAP(new GenericFastImageSearcher(1000, CEDD.class, true, readerIndex), "CEDD", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, FCTH.class, true, readerIndex), "FCTH", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, JCD.class, true, readerIndex), "JCD", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, AutoColorCorrelogram.class, true, readerIndex), "AutoColorCorrelogram", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, BinaryPatternsPyramid.class, true, readerIndex), "BinaryPatternsPyramid", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, FuzzyColorHistogram.class, true, readerIndex), "FuzzyColorHistogram", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, FuzzyOpponentHistogram.class, true, readerIndex), "FuzzyOpponentHistogram", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, Gabor.class, true, readerIndex), "Gabor", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, JpegCoefficientHistogram.class, true, readerIndex), "JpegCoefficientHistogram", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, LocalBinaryPatterns.class, true, readerIndex), "LocalBinaryPatterns", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, LuminanceLayout.class, true, readerIndex), "LuminanceLayout", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, OpponentHistogram.class, true, readerIndex), "OpponentHistogram", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, PHOG.class, true, readerIndex), "PHOG", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, RotationInvariantLocalBinaryPatterns.class, true, readerIndex), "RotationInvariantLocalBinaryPatterns", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, SimpleColorHistogram.class, true, readerIndex), "SimpleColorHistogram", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, Tamura.class, true, readerIndex), "Tamura", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, JointHistogram.class, true, readerIndex), "JointHistogram", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, LocalBinaryPatternsAndOpponent.class, true, readerIndex), "LocalBinaryPatternsAndOpponent", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, RankAndOpponent.class, true, readerIndex), "RankAndOpponent", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, ColorLayout.class, true, readerIndex), "ColorLayout", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, EdgeHistogram.class, true, readerIndex), "EdgeHistogram", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, ScalableColor.class, true, readerIndex), "ScalableColor", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, SPCEDD.class, true, readerIndex), "SPCEDD", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, SPJCD.class, true, readerIndex), "SPJCD", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, SPFCTH.class, true, readerIndex), "SPFCTH", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, SPACC.class, true, readerIndex), "SPACC", readerIndex, readerQueries);
//        computeMAP(new GenericFastImageSearcher(1000, SPLBP.class, true, readerIndex), "SPLBP", readerIndex, readerQueries);


        //BOVW
        for (int i = 0; i < numOfClusters.length; i++) {
//            computeMAP(new GenericFastImageSearcher(1000, CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple BOVW CEDD CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, FCTH.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple BOVW FCTH CVSURF", readerIndex, numOfClusters[i], readerQueries);
            computeMAP(new GenericFastImageSearcher(1000, JCD.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple BOVW JCD CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, AutoColorCorrelogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple BOVW AutoColorCorrelogram CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, OpponentHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple BOVW OpponentHistogram CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, ColorLayout.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple BOVW ColorLayout CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, EdgeHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple BOVW EdgeHistogram CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, ScalableColor.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple BOVW ScalableColor CVSURF", readerIndex, numOfClusters[i], readerQueries);


//            performWSs(CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "Simple BOVW CEDD CVSURF", readerQueries);
//            performWSs(FCTH.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "Simple BOVW FCTH CVSURF", readerQueries);
            performWSs(JCD.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "Simple BOVW JCD CVSURF", readerQueries);
//            performWSs(AutoColorCorrelogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "Simple BOVW AutoColorCorrelogram CVSURF", readerQueries);
//            performWSs(OpponentHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "Simple BOVW OpponentHistogram CVSURF", readerQueries);
//            performWSs(ColorLayout.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "Simple BOVW ColorLayout CVSURF", readerQueries);
//            performWSs(EdgeHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "Simple BOVW EdgeHistogram CVSURF", readerQueries);
//            performWSs(ScalableColor.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "Simple BOVW ScalableColor CVSURF", readerQueries);


            computeMAP(new GenericFastImageSearcher(1000, CvSurfExtractor.class, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "CVSURF BOVW", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, CvSiftExtractor.class, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "CVSIFT BOVW", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, SurfExtractor.class, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "SURF BOVW", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, SiftExtractor.class, new BOVW(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "SIFT BOVW", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, SelfSimilaritiesExtractor.class, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "SelfSimilarities BOVW", reader, numOfClusters[i], readerQueries);

            performWSs(CvSurfExtractor.class, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "CVSURF BOVW", readerQueries);
//            performWSs(CvSiftExtractor.class, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "CVSIFT BOVW", readerQueries);
//            performWSs(SurfExtractor.class, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "SURF BOVW", readerQueries);
//            performWSs(SiftExtractor.class, new BOVW(), numOfClusters[i], readerIndex, indexPath + ".config", "SIFT BOVW", readerQueries);
//            performWSs(SelfSimilaritiesExtractor.class, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "SelfSimilarities BOVW", readerQueries);
        }


        //VLAD
//        for (int i = 0; i < numOfClusters.length; i++) {
//            computeMAP(new GenericFastImageSearcher(1000, CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple VLAD CEDD CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, FCTH.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple VLAD FCTH CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, JCD.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple VLAD JCD CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, AutoColorCorrelogram.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple VLAD AutoColorCorrelogram CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, OpponentHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple VLAD OpponentHistogram CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, ColorLayout.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple VLAD ColorLayout CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, EdgeHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple VLAD EdgeHistogram CVSURF", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, ScalableColor.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "Simple VLAD ScalableColor CVSURF", readerIndex, numOfClusters[i], readerQueries);
////
//            computeMAP(new GenericFastImageSearcher(1000, CvSurfExtractor.class, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "CVSURF VLAD", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, CvSiftExtractor.class, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "CVSIFT VLAD", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, SurfExtractor.class, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "SURF VLAD", readerIndex, numOfClusters[i], readerQueries);
//            computeMAP(new GenericFastImageSearcher(1000, SiftExtractor.class, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "SIFT VLAD", readerIndex, numOfClusters[i], readerQueries);
////            computeMAP(new GenericFastImageSearcher(1000, SelfSimilaritiesExtractor.class, new VLAD(), numOfClusters[i], true, readerIndex, indexPath + ".config"), "SelfSimilarities VLAD", readerIndex, numOfClusters[i], readerQueries);
//        }

        double h = (System.currentTimeMillis() - start) / 3600000.0;
        double m = (h - Math.floor(h)) * 60.0;
        double s = (m - Math.floor(m)) * 60;
        System.out.printf("Total time of searching: %s.\n", String.format("%s%02d:%02d", (((int)h > 0)? String.format("%02d:", (int) h) : ""), (int)m, (int)s));
    }

    public void performWSs (Class<? extends GlobalFeature> globalFeature, SimpleExtractor.KeypointDetector detector, Aggregator aggregator, int codebookSize, IndexReader reader, String codebooksDir, String prefix, IndexReader readerQueries) throws IOException
    {
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, false, false, false), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, false, false, true), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, false, true, false), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, false, true, true), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, true, false, false), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, true, false, true), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, true, true, false), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, true, true, true), prefix, reader, codebookSize, readerQueries);
    }

    public void performWSs (Class<? extends LocalFeatureExtractor> localFeatureExtractor, Aggregator aggregator, int codebookSize, IndexReader reader, String codebooksDir, String prefix, IndexReader readerQueries) throws IOException
    {
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, false, false, false), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, false, false, true), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, false, true, false), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, false, true, true), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, true, false, false), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, true, false, true), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, true, true, false), prefix, reader, codebookSize, readerQueries);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, true, true, true), prefix, reader, codebookSize, readerQueries);
    }

    private void computeMAP(GenericFastImageSearcher genericFastImageSearcher, String prefix, IndexReader reader, IndexReader readerQueries) throws IOException {
        computeMAP(genericFastImageSearcher, prefix, reader, 0, readerQueries);
    }

    private void computeMAP(ImageSearcher searcher, String prefix, IndexReader reader, int clusters, IndexReader readerQueries) throws IOException {
        long start = System.currentTimeMillis();

        double queryCount = 0d;
        double errorRate = 0;
        double map = 0;
        double p10 = 0;
        int errorCount=0;
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(readerQueries);
        PrintWriter fw;
        if (searcher.toString().contains("ImageSearcherUsingWSs")) {
            (new File("eval/" + db + "/" + prefix.replace(' ', '_') + "/" + clusters + "/")).mkdirs();
            fw = new PrintWriter(new File("eval/" + db + "/" + prefix.replace(' ', '_') + "/" + clusters + "/" + prefix.replace(' ', '_') + "-" + db + clusters + searcher.toString().split("\\s+")[searcher.toString().split("\\s+").length - 1] + ".txt"));
        }else {
//            (new File("eval/#WithMirFlickr/" + db + "/")).mkdirs();
            (new File("eval/" + db + "/")).mkdirs();
            if (clusters>0)
                fw = new PrintWriter(new File("eval/" + db + "/" + prefix.replace(' ', '_') + "-" + db + clusters +".txt"));
            else
//                fw = new PrintWriter(new File("eval/#WithMirFlickr/" + db + "/" + prefix.replace(' ', '_') + "-" + db + "Global.txt")); //forGlobal
                fw = new PrintWriter(new File("eval/" + db + "/" + prefix.replace(' ', '_') + "-" + db + "Global.txt")); //forGlobal
        }
        Hashtable<Integer, String> evalText = new Hashtable<Integer, String>(260);
        for (int i = 0; i < readerQueries.maxDoc(); i++) {
            if (readerQueries.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.
            String fileName = getIDfromFileName(readerQueries.document(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
            if (queries.keySet().contains(fileName)) {
                String tmpEval = "";
                queryCount += 1d;
                // ok, we've got a query here for a document ...
                Document queryDoc = readerQueries.document(i);
                ImageSearchHits hits = searcher.search(queryDoc, reader);
                double rank = 0;
                double avgPrecision = 0;
                double found = 0;
                double tmpP10 = 0;
                Locale.setDefault(Locale.US);
                for (int y = 0; y < hits.length(); y++) {
                    String hitFile = getIDfromFileName(reader.document(hits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
                    // TODO: Sort by query ID!
                    tmpEval += String.format(Locale.US, "%d 1 %s %d %.2f test\n", query2id.get(fileName), hitFile.substring(0, hitFile.lastIndexOf('.')), (int) rank + 1, hits.score(y));
                    // if (!hitFile.equals(fileName)) {
                    rank++;
//                    if ((queries.get(fileName).contains(hitFile) || hitFile.equals(fileName))&&(!fileName.equals(hitFile))) { // it's a hit.
                    if (queries.get(fileName).contains(hitFile) || hitFile.equals(fileName)) { // it's a hit.
                        found++;
                        // TODO: Compute error rate, etc. here.
                        avgPrecision += found / rank;// * (1d/queries.get(fileName).size());
//                        avgPrecision += found / (rank-1);// * (1d/queries.get(fileName).size());
//                            if (rank<=60) System.out.print('X');
                        if (rank <= 10) tmpP10++;
                    } else {     // nothing has been found.
                        if (rank == 1) errorRate += 1d;
//                            if (rank<=60) System.out.print('-');
                    }
                }
                // }
//                System.out.println();
//                avgPrecision /= (double) (1d + queries.get(fileName).size()); // TODO: check!!
                avgPrecision /= (double) (queries.get(fileName).size());

                if (!(found - queries.get(fileName).size() == 0)){
                    // some of the results have not been found. We have to deal with it ...
                    errorCount++;
                }

                // assertTrue(found - queries.get(fileName).size() == 0);
                map += avgPrecision;
                p10 += tmpP10;
                evalText.put(query2id.get(fileName), tmpEval);
            }
        }

        for (int i = 0; i < query2id.size(); i++) {
            fw.write(evalText.get(i + 1));
        }

        fw.close();
        errorRate = errorRate / queryCount;
        map = map / queryCount;
        p10 = p10 / (queryCount * 10d);

        double h = (System.currentTimeMillis() - start) / 3600000.0;
        double m = (h - Math.floor(h)) * 60.0;
        double s = (m - Math.floor(m)) * 60;
        String str = String.format("%s%02d:%02d", (((int)h > 0)? String.format("%02d:", (int) h) : ""), (int)m, (int)s) + " ~ ";

        if (searcher.toString().contains("ImageSearcherUsingWSs"))
            str += String.format("%s%s\t%.4f\t%.4f\t%.4f\t(%s)", prefix, ((clusters>0)? ("\t"+clusters):"") , map, p10, errorRate, searcher.toString().split("\\s+")[searcher.toString().split("\\s+").length-1]);
        else
            str += String.format("%s%s\t%.4f\t%.4f\t%.4f", prefix, ((clusters>0)? ("\t"+clusters):""), map, p10, errorRate);
        if (errorCount>0) {
            // some of the results have not been found. We have to deal with it ...
            str += "\t~~\tDid not find result ;(\t(" + errorCount + ")";
        }
        System.out.println(str);
    }

    private String getIDfromFileName(String path) {
        // That's the one for Windows. Change for Linux ...
        return path.substring(path.lastIndexOf('\\') + 1);
    }

}
