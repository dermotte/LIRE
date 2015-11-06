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
import net.semanticmetadata.lire.imageanalysis.features.Extractor;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.FCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.JCD;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPACC;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPCEDD;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPFCTH;
import net.semanticmetadata.lire.imageanalysis.features.global.spatialpyramid.SPJCD;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearcherUsingWSs;
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
import java.io.*;
import java.nio.file.Paths;
import java.util.*;

/**
 * Created by Nektarios on 30/10/2014.
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class TestUniversal extends TestCase {

    //UCID
    private String db = "UCID";
    private String indexPath = "ucid-index";
    private String testExtensive = "testdata/UCID_png";
    private final String groundTruth = "testdata/queries/ucid.v2.groundtruth.txt";

    //UKBench
//    private String db = "UKB";
//    private String indexPath = "ukbench-index";
//    private String testExtensive = "testdata/ukbench";
//    private final String groundTruth = "testdata/queries/NisterQueries.txt";

    //Wang
//    private String db = "Wang";
//    private String indexPath = "wang-index";
//    private String testExtensive = "testdata/wang";
//    private final String groundTruth = "testdata/queries/WangQueries.txt";

    //Holidays
//    private String db = "Hol";
//    private String indexPath = "holidays-index";
//    private String testExtensive = "testdata/holidays";
//    private final String groundTruth = "testdata/queries/HolidaysQueries.txt";


    private int numOfDocsForVocabulary = 500;
    private Class<? extends AbstractAggregator> aggregator = BOVW.class;
    private int[] numOfClusters = new int[] {32, 128, 512, 2048};

//    private Class<? extends AbstractAggregator> aggregator = VLAD.class;
//    private int[] numOfClusters = new int[] {16, 64};

    private HashMap<String, List<String>> queries;
    private HashMap<String, Integer> query2id;

    protected void setUp() throws Exception {
        super.setUp();
        indexPath += "-" + System.currentTimeMillis() % (1000 * 60 * 60 * 24 * 7);

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
//        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, false);
//        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive);

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


        // SEARCHING
        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(Paths.get(indexPath)), IOContext.READONCE));
//        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
        System.out.println("Documents in the reader: " + reader.maxDoc());
//
        System.out.println("Feature\tMAP\tp@10\tER");
//
        long start = System.currentTimeMillis();
//
//        computeMAP(new GenericFastImageSearcher(1000, ACCID.class, true, reader), "ACCID", reader);
        computeMAP(new GenericFastImageSearcher(1000, CEDD.class, true, reader), "CEDD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, FCTH.class, true, reader), "FCTH", reader);
//        computeMAP(new GenericFastImageSearcher(1000, JCD.class, true, reader), "JCD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, AutoColorCorrelogram.class, true, reader), "AutoColorCorrelogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, BinaryPatternsPyramid.class, true, reader), "BinaryPatternsPyramid", reader);
//        computeMAP(new GenericFastImageSearcher(1000, FuzzyColorHistogram.class, true, reader), "FuzzyColorHistogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, FuzzyOpponentHistogram.class, true, reader), "FuzzyOpponentHistogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, Gabor.class, true, reader), "Gabor", reader);
//        computeMAP(new GenericFastImageSearcher(1000, JpegCoefficientHistogram.class, true, reader), "JpegCoefficientHistogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, LocalBinaryPatterns.class, true, reader), "LocalBinaryPatterns", reader);
//        computeMAP(new GenericFastImageSearcher(1000, LuminanceLayout.class, true, reader), "LuminanceLayout", reader);
//        computeMAP(new GenericFastImageSearcher(1000, OpponentHistogram.class, true, reader), "OpponentHistogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, PHOG.class, true, reader), "PHOG", reader);
//        computeMAP(new GenericFastImageSearcher(1000, RotationInvariantLocalBinaryPatterns.class, true, reader), "RotationInvariantLocalBinaryPatterns", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SimpleColorHistogram.class, true, reader), "SimpleColorHistogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, Tamura.class, true, reader), "Tamura", reader);
//        computeMAP(new GenericFastImageSearcher(1000, JointHistogram.class, true, reader), "JointHistogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, LocalBinaryPatternsAndOpponent.class, true, reader), "LocalBinaryPatternsAndOpponent", reader);
//        computeMAP(new GenericFastImageSearcher(1000, RankAndOpponent.class, true, reader), "RankAndOpponent", reader);
//        computeMAP(new GenericFastImageSearcher(1000, ColorLayout.class, true, reader), "ColorLayout", reader);
//        computeMAP(new GenericFastImageSearcher(1000, EdgeHistogram.class, true, reader), "EdgeHistogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, ScalableColor.class, true, reader), "ScalableColor", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SPCEDD.class, true, reader), "SPCEDD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SPJCD.class, true, reader), "SPJCD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SPFCTH.class, true, reader), "SPFCTH", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SPACC.class, true, reader), "SPACC", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SPLBP.class, true, reader), "SPLBP", reader);


        //BOVW
        for (int i = 0; i < numOfClusters.length; i++) {
//            computeMAP(new GenericFastImageSearcher(1000, CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple BOVW CEDD CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, FCTH.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple BOVW FCTH CVSURF", reader, numOfClusters[i]);
            computeMAP(new GenericFastImageSearcher(1000, JCD.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple BOVW JCD CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, AutoColorCorrelogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple BOVW AutoColorCorrelogram CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, OpponentHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple BOVW OpponentHistogram CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, ColorLayout.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple BOVW ColorLayout CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, EdgeHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple BOVW EdgeHistogram CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, ScalableColor.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple BOVW ScalableColor CVSURF", reader, numOfClusters[i]);


//            performWSs(CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "Simple BOVW CEDD CVSURF");
//            performWSs(FCTH.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "Simple BOVW FCTH CVSURF");
            performWSs(JCD.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "Simple BOVW JCD CVSURF");
//            performWSs(AutoColorCorrelogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "Simple BOVW AutoColorCorrelogram CVSURF");
//            performWSs(OpponentHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "Simple BOVW OpponentHistogram CVSURF");
//            performWSs(ColorLayout.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "Simple BOVW ColorLayout CVSURF");
//            performWSs(EdgeHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "Simple BOVW EdgeHistogram CVSURF");
//            performWSs(ScalableColor.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "Simple BOVW ScalableColor CVSURF");


            computeMAP(new GenericFastImageSearcher(1000, CvSurfExtractor.class, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "CVSURF BOVW", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, CvSiftExtractor.class, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "CVSIFT BOVW", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, SurfExtractor.class, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "SURF BOVW", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, SiftExtractor.class, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "SIFT BOVW", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, SelfSimilaritiesExtractor.class, new BOVW(), numOfClusters[i], true, reader, indexPath + ".config"), "SelfSimilarities BOVW", reader, numOfClusters[i]);

            performWSs(CvSurfExtractor.class, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "CVSURF BOVW");
//            performWSs(CvSiftExtractor.class, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "CVSIFT BOVW");
//            performWSs(SurfExtractor.class, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "SURF BOVW");
//            performWSs(SiftExtractor.class, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "SIFT BOVW");
//            performWSs(SelfSimilaritiesExtractor.class, new BOVW(), numOfClusters[i], reader, indexPath + ".config", "SelfSimilarities BOVW");
        }


        //VLAD
//        for (int i = 0; i < numOfClusters.length; i++) {
//            computeMAP(new GenericFastImageSearcher(1000, CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple VLAD CEDD CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, FCTH.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple VLAD FCTH CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, JCD.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple VLAD JCD CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, AutoColorCorrelogram.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple VLAD AutoColorCorrelogram CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, OpponentHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple VLAD OpponentHistogram CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, ColorLayout.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple VLAD ColorLayout CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, EdgeHistogram.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple VLAD EdgeHistogram CVSURF", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, ScalableColor.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "Simple VLAD ScalableColor CVSURF", reader, numOfClusters[i]);
////
//            computeMAP(new GenericFastImageSearcher(1000, CvSurfExtractor.class, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "CVSURF VLAD", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, CvSiftExtractor.class, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "CVSIFT VLAD", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, SurfExtractor.class, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "SURF VLAD", reader, numOfClusters[i]);
//            computeMAP(new GenericFastImageSearcher(1000, SiftExtractor.class, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "SIFT VLAD", reader, numOfClusters[i]);
////            computeMAP(new GenericFastImageSearcher(1000, SelfSimilaritiesExtractor.class, new VLAD(), numOfClusters[i], true, reader, indexPath + ".config"), "SelfSimilarities VLAD", reader, numOfClusters[i]);
//        }

        double h = (System.currentTimeMillis() - start) / 3600000.0;
        double m = (h - Math.floor(h)) * 60.0;
        double s = (m - Math.floor(m)) * 60;
        System.out.printf("Total time of searching: %s.\n", String.format("%s%02d:%02d", (((int)h > 0)? String.format("%02d:", (int) h) : ""), (int)m, (int)s));
    }

    public void performWSs (Class<? extends GlobalFeature> globalFeature, SimpleExtractor.KeypointDetector detector, Aggregator aggregator, int codebookSize, IndexReader reader, String codebooksDir, String prefix) throws IOException
    {
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, false, false, false), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, false, false, true), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, false, true, false), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, false, true, true), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, true, false, false), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, true, false, true), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, true, true, false), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, globalFeature, detector, aggregator, codebookSize, reader, codebooksDir, true, true, true), prefix, reader, codebookSize);
    }

    public void performWSs (Class<? extends LocalFeatureExtractor> localFeatureExtractor, Aggregator aggregator, int codebookSize, IndexReader reader, String codebooksDir, String prefix) throws IOException
    {
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, false, false, false), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, false, false, true), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, false, true, false), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, false, true, true), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, true, false, false), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, true, false, true), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, true, true, false), prefix, reader, codebookSize);
        computeMAP(new ImageSearcherUsingWSs(1000, localFeatureExtractor, aggregator, codebookSize, reader, codebooksDir, true, true, true), prefix, reader, codebookSize);
    }

    private void computeMAP(GenericFastImageSearcher genericFastImageSearcher, String prefix, IndexReader reader) throws IOException {
        computeMAP(genericFastImageSearcher, prefix, reader, 0);
    }

    private void computeMAP(ImageSearcher searcher, String prefix, IndexReader reader, int clusters) throws IOException {
        long start = System.currentTimeMillis();
        long timeOfSearch = 0, ms;

        double queryCount = 0d;
        double errorRate = 0;
        double map = 0;
        double p10 = 0;
        int errorCount=0;
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);
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
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.
            String fileName = getIDfromFileName(reader.document(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
            if (queries.keySet().contains(fileName)) {
                String tmpEval = "";
                queryCount += 1d;
                // ok, we've got a query here for a document ...
                Document queryDoc = reader.document(i);
                ms = System.currentTimeMillis();
                ImageSearchHits hits = searcher.search(queryDoc, reader);
                timeOfSearch += System.currentTimeMillis() - ms;
                double rank = 0;
                double avgPrecision = 0;
                double found = 0;
                double tmpP10 = 0;
                Locale.setDefault(Locale.US);
                for (int y = 0; y < hits.length(); y++) {
//                    String hitFile = getIDfromFileName(hits.doc(y).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
                    String hitFile = getIDfromFileName(reader.document(hits.documentID(y)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
//                    String hitFile = getIDfromFileName(hits.path(y));
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
                avgPrecision /= (double) (1d + queries.get(fileName).size());
//                avgPrecision /= (double) (queries.get(fileName).size());

                if (!(found - queries.get(fileName).size() == 1)){
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
        h = timeOfSearch / 3600000.0;
        m = (h - Math.floor(h)) * 60.0;
        s = (m - Math.floor(m)) * 60;
        str += " ~ TimeOfsearch: " + String.format("%s%02d:%02d", (((int)h > 0)? String.format("%02d:", (int) h) : ""), (int)m, (int)s);

        System.out.println(str);
    }

    private String getIDfromFileName(String path) {
        // That's the one for Windows. Change for Linux ...
        return path.substring(path.lastIndexOf('\\') + 1);
    }

    public void testIndexingSpeed() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), false);
        testFeatureSpeed(images, AutoColorCorrelogram.class);
        testFeatureSpeed(images, CEDD.class);
        testFeatureSpeed(images, FCTH.class);
        testFeatureSpeed(images, JCD.class);
        testFeatureSpeed(images, SPACC.class);
        testFeatureSpeed(images, SPCEDD.class);
        testFeatureSpeed(images, SPFCTH.class);
        testFeatureSpeed(images, SPJCD.class);
    }

    public void testSearchSpeed() throws IOException {
        testSearchSpeed(AutoColorCorrelogram.class);
        testSearchSpeed(CEDD.class);
        testSearchSpeed(FCTH.class);
        testSearchSpeed(JCD.class);
        testSearchSpeed(SPACC.class);
        testSearchSpeed(SPCEDD.class);
        testSearchSpeed(SPFCTH.class);
        testSearchSpeed(SPJCD.class);
    }

    private void testSearchSpeed(Class<? extends GlobalFeature> featureClass) throws IOException {
        ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, indexPath, testExtensive, true);
        parallelIndexer.addExtractor(featureClass);
        parallelIndexer.run();
        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(Paths.get(indexPath)), IOContext.READONCE));
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        double queryCount = 0d;
        ImageSearcher searcher = new GenericFastImageSearcher(100, featureClass);
        long ms = System.currentTimeMillis();
        String fileName;
        Document queryDoc;
        ImageSearchHits hits;
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.
            fileName = getIDfromFileName(reader.document(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
            if (queries.keySet().contains(fileName)) {
                queryCount += 1d;
                // ok, we've got a query here for a document ...
                queryDoc = reader.document(i);
                hits = searcher.search(queryDoc, reader);
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.printf("%s \t %3.1f \n", featureClass.getName().substring(featureClass.getName().lastIndexOf('.') + 1), (double) ms / queryCount);
    }

    private void testFeatureSpeed(ArrayList<String> images, Class<? extends Extractor> extractorClass) throws IOException {
        Extractor extractor;
        long ms;
        try {
            extractor = extractorClass.newInstance();
            ms = System.currentTimeMillis();
            for (String s : images) {
                extractor.extract(ImageIO.read(new File(s)));
            }
            ms = System.currentTimeMillis() - ms;
            System.out.printf("%s \t %3.1f \n", extractor.getClass().getName().substring(extractor.getClass().getName().lastIndexOf('.') + 1), (double) ms / (double) images.size());
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


}
