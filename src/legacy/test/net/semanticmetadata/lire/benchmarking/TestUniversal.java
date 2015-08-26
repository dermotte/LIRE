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
import net.semanticmetadata.lire.imageanalysis.bovw.*;
import net.semanticmetadata.lire.imageanalysis.spatialpyramid.*;
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
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: nek.anag
 * Date: 30.10.14
 * Time: 14:05
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
//    private String testExtensive = "testdata/Holidays.small";
//    private final String groundTruth = "testdata/queries/HolidaysQueries.txt";


    private int sample = 500;
    private int clusters = 512; //Set 0 if Global!!

    private ChainedDocumentBuilder builder;
    private HashMap<String, List<String>> queries;
    private HashMap<String, Integer> query2id;

    ParallelIndexer parallelIndexer;

    protected void setUp() throws Exception {
        super.setUp();
        indexPath += "-" + System.currentTimeMillis() % (1000 * 60 * 60 * 24 * 7);
        // Setting up DocumentBuilder:
        parallelIndexer = new ParallelIndexer(16, indexPath, testExtensive, true) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
//                builder.addBuilder(new GenericDocumentBuilder(CEDD.class));
//                builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getLuminanceLayoutDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder());
//                builder.addBuilder(new GenericDocumentBuilder(RankAndOpponent.class, "jop"));
//                builder.addBuilder(new GenericFastDocumentBuilder(FuzzyOpponentHistogram.class, "opHist"));

//                builder.addBuilder(new SurfDocumentBuilder());
//                builder.addBuilder(new SiftDocumentBuilder());
//                builder.addBuilder(new CvSurfDocumentBuilder());
//                builder.addBuilder(new CvSiftDocumentBuilder());
//                builder.addBuilder(new MSERDocumentBuilder());
//                builder.addBuilder(new GenericDocumentBuilder(LocalBinaryPatterns.class, "lbp"));
//                builder.addBuilder(new GenericDocumentBuilder(LocalBinaryPatternsAndOpponent.class, "jhl"));
//                builder.addBuilder(new GenericDocumentBuilder(RotationInvariantLocalBinaryPatterns.class, "rlbp"));


                //GLOBAL Tests
//                builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
//                builder.addBuilder(new GenericDocumentBuilder(LocalBinaryPatterns.class, "lbp"));
//                builder.addBuilder(new GenericDocumentBuilder(RotationInvariantLocalBinaryPatterns.class, "rlbp"));
//                builder.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());

//                builder.addBuilder(new GenericDocumentBuilder(SPCEDD.class));
//                builder.addBuilder(new GenericDocumentBuilder(SPJCD.class));
//                builder.addBuilder(new GenericDocumentBuilder(SPFCTH.class));
//                builder.addBuilder(new GenericDocumentBuilder(SPACC.class));
//                builder.addBuilder(new GenericDocumentBuilder(SPLBP.class));


                //Tests for SIMPLE
                builder.addBuilder(new SimpleBuilder(new CEDD(), SimpleBuilder.KeypointDetector.CVSURF));
//                builder.addBuilder(new SimpleBuilder(new CEDD(), SimpleBuilder.KeypointDetector.CVSIFT));
//                builder.addBuilder(new SimpleBuilder(new CEDD(), SimpleBuilder.KeypointDetector.Random));
//                builder.addBuilder(new SimpleBuilder(new CEDD(), SimpleBuilder.KeypointDetector.GaussRandom));
//
//                builder.addBuilder(new SimpleBuilder(new FCTH(), SimpleBuilder.KeypointDetector.CVSURF));
//                builder.addBuilder(new SimpleBuilder(new JCD(), SimpleBuilder.KeypointDetector.CVSURF));
//                builder.addBuilder(new SimpleBuilder(new AutoColorCorrelogram(), SimpleBuilder.KeypointDetector.CVSURF));
//                builder.addBuilder(new SimpleBuilder(new OpponentHistogram(), SimpleBuilder.KeypointDetector.CVSURF));
//                builder.addBuilder(new SimpleBuilder(new LocalBinaryPatterns(), SimpleBuilder.KeypointDetector.CVSURF));
//                builder.addBuilder(new SimpleBuilder(new RotationInvariantLocalBinaryPatterns(), SimpleBuilder.KeypointDetector.CVSURF));
//                builder.addBuilder(new SimpleBuilder(new ScalableColor(), SimpleBuilder.KeypointDetector.CVSURF));
//                builder.addBuilder(new SimpleBuilder(new ColorLayout(), SimpleBuilder.KeypointDetector.CVSURF));
//                builder.addBuilder(new SimpleBuilder(new EdgeHistogram(), SimpleBuilder.KeypointDetector.CVSURF));
//
//                builder.addBuilder(new CvSurfDocumentBuilder());
//                builder.addBuilder(new CvSiftDocumentBuilder());
//                builder.addBuilder(new SurfDocumentBuilder());
//                builder.addBuilder(new SiftDocumentBuilder());
            }
        };

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
        parallelIndexer.run();

//        System.out.println("** Aggregate features");
//        AggregateFeatures aggregateFeatures = new AggregateFeatures(DirectoryReader.open(FSDirectory.open(new File(indexPath))));
//        aggregateFeatures.aggregate();

//        System.out.println("** CVSURF BoVW");
//        BOVWBuilder bovwBuilderSURF = new BOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new CvSurfFeature(), sample, clusters);
//        bovwBuilderSURF.index();
//        System.out.println("** CVSIFT BoVW");
//        BOVWBuilder bovwBuilder = new BOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new CvSiftFeature(), sample, clusters);
//        bovwBuilder.index();

//        System.out.println("** CVSIFT VLAD");
//        VLADBuilder vladBuilder = new VLADBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new CvSiftFeature(), sample, clusters);
//        vladBuilder.index();

        System.out.println("** SIMPLE BoVW using CEDD and CVSURF");
        SimpleFeatureBOVWBuilder simpleBovwBuilder = new SimpleFeatureBOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new CEDD(), SimpleBuilder.KeypointDetector.CVSURF, sample, clusters);
        simpleBovwBuilder.index();

//        System.out.println("** SIMPLE VLAD using CEDD and CVSURF");
//        SimpleFeatureVLADBuilder simpleVladBuilder = new SimpleFeatureVLADBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new CEDD(), SimpleBuilder.KeypointDetector.CVSURF, sample, clusters);
//        simpleVladBuilder.index();

        // SEARCHING
        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(new File(indexPath)), IOContext.READONCE));

        System.out.println("Feature\tMAP\tp@10\tER");
//        computeMAP(new GenericFastImageSearcher(1000, CEDD.class, true, reader), "CEDD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, FCTH.class, true, reader), "FCTH", reader);
//        computeMAP(new GenericFastImageSearcher(1000, JCD.class, true, reader), "JCD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, AutoColorCorrelogram.class, true, reader), "Color Correlation", reader);
//        computeMAP(new GenericFastImageSearcher(1000, OpponentHistogram.class, true, reader), "Opponent Histogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, LocalBinaryPatterns.class, "lbp", true, reader), "LBP ", reader);
//        computeMAP(new GenericFastImageSearcher(1000, RotationInvariantLocalBinaryPatterns.class, "rlbp"), "RILBP ", reader);
//        computeMAP(new GenericFastImageSearcher(1000, ScalableColor.class, true, reader), "Scalable Color", reader);
//        computeMAP(new GenericFastImageSearcher(1000, ColorLayout.class, true, reader), "Color Layout", reader);
//        computeMAP(new GenericFastImageSearcher(1000, EdgeHistogram.class, true, reader), "Edge Histogram", reader);

//        computeMAP(new GenericFastImageSearcher(1000, SPCEDD.class, true, reader), "SPCEDD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SPJCD.class, true, reader), "SPJCD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SPFCTH.class, true, reader), "SPFCTH", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SPACC.class, true, reader), "SPACC ", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SPLBP.class, true, reader), "SPLBP ", reader);

//        computeMAP(new GenericFastImageSearcher(1000, PHOG.class, true, reader), "PHOG", reader);
//        computeMAP(new GenericFastImageSearcher(1000, JointHistogram.class, true, reader), "Joint Histogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, SimpleColorHistogram.class, true, reader), "RGB Color Histogram", reader);
//        computeMAP(new GenericFastImageSearcher(1000, LocalBinaryPatternsAndOpponent.class, "jhl", true, reader), "JHL ", reader);
//        computeMAP(ImageSearcherFactory.createTamuraImageSearcher(1000), "Tamura", reader);
//        computeMAP(ImageSearcherFactory.createTamuraImageSearcher(1000), "Tamura", reader);

//        computeMAP(new VisualWordsImageSearcher(1000, DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW), "Surf BoVW Lucene", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM, true, reader), "Surf BoVW L2", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM, true, reader), "Sift BoVW L2", reader);
//        computeMAP(new VisualWordsImageSearcher(1000, (new ScalableColor()).getFieldName() + "LoDe"), "LoDe SC Lucene", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new CEDD()).getFieldName() + "LoDe_Hist", true, reader), "LoDe SC L2", reader);
//        computeMAP(new VisualWordsImageSearcher(1000, (new CEDD()).getFieldName() + "LoDe"), "LoDe CEDD Lucene", reader);

        //NEK TESTS for SIMPLE//
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new CEDD()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Simple BOVW CEDD CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new FCTH()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Simple BOVW FCTH CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new JCD()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Simple BOVW JCD CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new AutoColorCorrelogram()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Simple BOVW AutoColCorrel CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new OpponentHistogram()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Simple BOVW OppHist CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new LocalBinaryPatterns()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Simple BOVW LBP CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new RotationInvariantLocalBinaryPatterns()) + SimpleBuilder.Detector_CVSURF + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Simple BOVW RILBP CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new ScalableColor()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Simple BOVW SC CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new ColorLayout()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Simple BOVW CL CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new EdgeHistogram()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Simple BOVW EH CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_CVSURF + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "CVSURF BoVW", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_CVSIFT + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "CVSIFT BoVW", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Surf BoVW", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SIFT + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Sift BoVW", reader);
//
        //perform weighting schemes for SIMPLE
        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new CEDD()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Simple BOVW CEDD CVSURF", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new FCTH()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Simple BOVW FCTH CVSURF", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new JCD()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Simple BOVW JCD CVSURF", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new AutoColorCorrelogram()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Simple BOVW AutoColCorrel CVSURF", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new OpponentHistogram()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Simple BOVW OppHist CVSURF", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new LocalBinaryPatterns()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Simple BOVW LBP CVSURF", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new RotationInvariantLocalBinaryPatterns()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Simple BOVW RILBP CVSURF", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new ScalableColor()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Simple BOVW SC CVSURF", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new ColorLayout()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Simple BOVW CL CVSURF", reader);
//        performWSs((new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new EdgeHistogram()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Simple BOVW EH CVSURF", reader);
//        performWSs(DocumentBuilder.FIELD_NAME_CVSURF + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "CVSURF BoVW", reader);
//        performWSs(DocumentBuilder.FIELD_NAME_CVSIFT + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "CVSIFT BoVW", reader);
//        performWSs(DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Surf BoVW", reader);
//        performWSs(DocumentBuilder.FIELD_NAME_SIFT + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, "Sift BoVW", reader);


        //VLAD
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new CEDD()) + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Simple VLAD CEDD CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new FCTH()) + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Simple VLAD FCTH CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new JCD()) + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Simple VLAD JCD CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new AutoColorCorrelogram()) + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Simple VLAD AutoColCorrel CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new OpponentHistogram()) + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Simple VLAD OppHist CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new LocalBinaryPatterns()) + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Simple VLAD LBP CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new RotationInvariantLocalBinaryPatterns()) + SimpleBuilder.Detector_CVSURF + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Simple VLAD RILBP CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new ScalableColor()) + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Simple VLAD SC CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new ColorLayout()) + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Simple VLAD CL CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new EdgeHistogram()) + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Simple VLAD EH CVSURF", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_CVSURF + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "CVSURF VLAD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_CVSIFT + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "CVSIFT VLAD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Surf VLAD", reader);
//        computeMAP(new GenericFastImageSearcher(1000, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SIFT + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader), "Sift VLAD", reader);
    }

    public void performWSs (String fieldName, String prefix, IndexReader reader) throws IOException
    {
        computeMAP(new ImageSearcherUsingWSs(1000, GenericDoubleLireFeature.class, fieldName, true, reader, false, false, false), prefix, reader);
        computeMAP(new ImageSearcherUsingWSs(1000, GenericDoubleLireFeature.class, fieldName, true, reader, false, false, true), prefix, reader);
        computeMAP(new ImageSearcherUsingWSs(1000, GenericDoubleLireFeature.class, fieldName, true, reader, false, true, false), prefix, reader);
        computeMAP(new ImageSearcherUsingWSs(1000, GenericDoubleLireFeature.class, fieldName, true, reader, false, true, true), prefix, reader);
        computeMAP(new ImageSearcherUsingWSs(1000, GenericDoubleLireFeature.class, fieldName, true, reader, true, false, false), prefix, reader);
        computeMAP(new ImageSearcherUsingWSs(1000, GenericDoubleLireFeature.class, fieldName, true, reader, true, false, true), prefix, reader);
        computeMAP(new ImageSearcherUsingWSs(1000, GenericDoubleLireFeature.class, fieldName, true, reader, true, true, false), prefix, reader);
        computeMAP(new ImageSearcherUsingWSs(1000, GenericDoubleLireFeature.class, fieldName, true, reader, true, true, true), prefix, reader);
    }

    private void computeMAP(ImageSearcher searcher, String prefix, IndexReader reader) throws IOException {
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
            (new File("eval/" + db + "/")).mkdirs();
            if (clusters>0)
                fw = new PrintWriter(new File("eval/" + db + "/" + prefix.replace(' ', '_') + "-" + db + clusters +".txt"));
            else
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
                ImageSearchHits hits = searcher.search(queryDoc, reader);
                double rank = 0;
                double avgPrecision = 0;
                double found = 0;
                double tmpP10 = 0;
                Locale.setDefault(Locale.US);
                for (int y = 0; y < hits.length(); y++) {
                    String hitFile = getIDfromFileName(hits.doc(y).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
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
//        System.out.print(prefix);
        String s;
        if (searcher.toString().contains("ImageSearcherUsingWSs"))
            s = String.format("%s\t%.4f\t%.4f\t%.4f\t(%s)", prefix, map, p10, errorRate, searcher.toString().split("\\s+")[searcher.toString().split("\\s+").length-1]);
        else
            s = String.format("%s\t%.4f\t%.4f\t%.4f", prefix, map, p10, errorRate);
        if (errorCount>0) {
            // some of the results have not been found. We have to deal with it ...
            //System.err.println("Did not find result ;(  (" + errorCount + ")");
            s += "\t~~\tDid not find result ;(\t(" + errorCount + ")";
        }
        System.out.println(s);
    }

    private String getIDfromFileName(String path) {
        // That's the one for Windows. Change for Linux ...
//        return path.substring(path.lastIndexOf('\\') + 1).replace(".jpg", ".tif");
        return path.substring(path.lastIndexOf('\\') + 1);
    }

    public void testIndexingSpeed() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), false);
//        testFeatureSpeed(images, new AutoColorCorrelogram());
//        testFeatureSpeed(images, new CEDD());
//        testFeatureSpeed(images, new FCTH());
//        testFeatureSpeed(images, new JCD());
        testFeatureSpeed(images, new SPACC());
        testFeatureSpeed(images, new SPCEDD());
        testFeatureSpeed(images, new SPFCTH());
        testFeatureSpeed(images, new SPJCD());
    }

    public void testSearchSpeed() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), false);
        testSearchSpeed(images, AutoColorCorrelogram.class);
        testSearchSpeed(images, CEDD.class);
        testSearchSpeed(images, FCTH.class);
        testSearchSpeed(images, JCD.class);
        testSearchSpeed(images, SPACC.class);
        testSearchSpeed(images, SPCEDD.class);
        testSearchSpeed(images, SPFCTH.class);
        testSearchSpeed(images, SPJCD.class);
    }

    private void testSearchSpeed(ArrayList<String> images, final Class featureClass) throws IOException {
        parallelIndexer = new ParallelIndexer(8, indexPath, testExtensive, true) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(new GenericDocumentBuilder(featureClass, "feature"));
            }
        };
        parallelIndexer.run();
        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(new File(indexPath)), IOContext.READONCE));
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        double queryCount = 0d;
        ImageSearcher searcher = new GenericFastImageSearcher(100, featureClass, "feature");
        long ms = System.currentTimeMillis();
        for (int i = 0; i < reader.maxDoc(); i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.
            String fileName = getIDfromFileName(reader.document(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
            if (queries.keySet().contains(fileName)) {
                queryCount += 1d;
                // ok, we've got a query here for a document ...
                Document queryDoc = reader.document(i);
                ImageSearchHits hits = searcher.search(queryDoc, reader);
            }
        }
        ms = System.currentTimeMillis() - ms;
        System.out.printf("%s \t %3.1f \n", featureClass.getName().substring(featureClass.getName().lastIndexOf('.') + 1), (double) ms / queryCount);
    }

    private void testFeatureSpeed(ArrayList<String> images, LireFeature feature) throws IOException {
        long ms = System.currentTimeMillis();
        for (Iterator<String> iterator = images.iterator(); iterator.hasNext(); ) {
            String s = iterator.next();
            feature.extract(ImageIO.read(new File(s)));
        }
        ms = System.currentTimeMillis() - ms;
        System.out.printf("%s \t %3.1f \n", feature.getClass().getName().substring(feature.getClass().getName().lastIndexOf('.') + 1), (double) ms / (double) images.size());
    }


}
