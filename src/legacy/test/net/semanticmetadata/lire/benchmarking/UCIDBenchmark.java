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
 * Updated: 27.06.14 13:06
 */

package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.imageanalysis.bovw.BOVWBuilder;
import net.semanticmetadata.lire.imageanalysis.bovw.SimpleFeatureBOVWBuilder;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import net.semanticmetadata.lire.imageanalysis.spatialpyramid.SPACC;
import net.semanticmetadata.lire.imageanalysis.spatialpyramid.SPCEDD;
import net.semanticmetadata.lire.imageanalysis.spatialpyramid.SPFCTH;
import net.semanticmetadata.lire.imageanalysis.spatialpyramid.SPJCD;
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
 * User: mlux
 * Date: 14.05.13
 * Time: 10:56
 */
public class UCIDBenchmark extends TestCase {
    private String indexPath = "ucid-index";
    // if you don't have the images you can get them here: http://homepages.lboro.ac.uk/~cogs/datasets/ucid/ucid.html
    // I converted all images to PNG (lossless) to save time, space & troubles with Java.
    private String testExtensive = "testdata/UCID";
    private final String groundTruth = "testdata/ucid.v2.groundtruth.txt";

    //    private String testExtensive = "testdata/UCID.small";
//    private final String groundTruth = "testdata/ucid.v2.groundtruth.small.txt";
//
    private ChainedDocumentBuilder builder;
    private HashMap<String, List<String>> queries;
    private HashMap<String, Integer> query2id;

    ParallelIndexer parallelIndexer;

    /* configure test of global features by adding them here */

    LireFeature[] globalFeaturesToTest = new LireFeature[]{
//            new CEDD(),
//            new PHOG(),
//            new SPACC(),
//            new SPCEDD(),
//            new OpponentHistogram()
    };

    LireFeature[] simpleFeaturesToTest = new LireFeature[]{
            new CEDD()
//            new ScalableColor()
//            new OpponentHistogram()
    };

    boolean testSift = false;
    boolean testSurf = false;


    protected void setUp() throws Exception {

        super.setUp();
//        indexPath = "ucid-index-573374558";
        indexPath += "-" + System.currentTimeMillis() % (1000 * 60 * 60 * 24 * 7);
        // Setting up DocumentBuilder:
        parallelIndexer = new ParallelIndexer(16, indexPath, testExtensive, true) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                for (int i = 0; i < globalFeaturesToTest.length; i++) {
                    LireFeature lireFeature = globalFeaturesToTest[i];
                    builder.addBuilder(new GenericDocumentBuilder(lireFeature.getClass()));
                }

                for (int i = 0; i < simpleFeaturesToTest.length; i++) {
                    LireFeature lireFeature = simpleFeaturesToTest[i];
                    // need to create a feature instance for each thread.
                    try {
                        builder.addBuilder(new SimpleBuilder(lireFeature.getClass().newInstance()));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                if (testSift) builder.addBuilder(new SiftDocumentBuilder());
                if (testSurf) builder.addBuilder(new SurfDocumentBuilder());
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
        // ************************* INDEXING *************************
        parallelIndexer.run();

        if (testSurf) {
            System.out.println("** SURF BoVW");
            BOVWBuilder sh = new BOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new SurfFeature(), 500, 128);
            sh.index();
        }

        if (testSift) {
            System.out.println("** SIFT BoVW");
            BOVWBuilder sh = new BOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new Feature(), 500, 128);
            sh.index();
        }

        for (int i = 0; i < simpleFeaturesToTest.length; i++) {
            LireFeature lireFeature = simpleFeaturesToTest[i];
            System.out.println("** SIMPLE BoVW with " + lireFeature.getFeatureName());
            SimpleFeatureBOVWBuilder ldb = new SimpleFeatureBOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), lireFeature, SimpleBuilder.KeypointDetector.CVSURF, 500, 128);
            ldb.index();
        }


//        System.out.println("** SIMPLE BoVW / LoDe CEDD");
//        ldb = new SimpleFeatureBOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), 500, 128, new CEDD());
//        ldb.index();

        // VLAD
//        VLADBuilder vladBuilder = new VLADBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))));
//        vladBuilder.index();

        // ************************* SEARCHING *************************
        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(new File(indexPath)), IOContext.READONCE));

        System.out.println("Feature\tMAP\tp@10\tER");

        for (int i = 0; i < globalFeaturesToTest.length; i++) {
            LireFeature lireFeature = globalFeaturesToTest[i];
            computeMAP(new GenericFastImageSearcher(1400, lireFeature.getClass(), true, reader), lireFeature.getFeatureName(), reader);
        }


//        computeMAP(new VisualWordsImageSearcher(1400, DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW), "Surf BoVW Lucene", reader);
        if (testSurf)
//            computeMAP(new GenericFastImageSearcher(1400, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM, true, reader), "Surf BoVW L2", reader);
            computeMAP(new GenericFastImageSearcher(1400, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SURF + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Surf BoVW L2", reader);
        if (testSift)
//            computeMAP(new GenericFastImageSearcher(1400, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM, true, reader), "Sift BoVW L2", reader);
            computeMAP(new GenericFastImageSearcher(1400, GenericDoubleLireFeature.class, DocumentBuilder.FIELD_NAME_SIFT + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader), "Sift BoVW L2", reader);

        for (int i = 0; i < simpleFeaturesToTest.length; i++) {
            LireFeature lireFeature = simpleFeaturesToTest[i];
            computeMAP(new GenericFastImageSearcher(1400, GenericDoubleLireFeature.class, lireFeature.getFieldName() + "LoDe_Hist", true, reader), "LoDe L2 " + lireFeature.getFeatureName(), reader);
        }

//        computeMAP(new VisualWordsImageSearcher(1400, (new ScalableColor()).getFieldName() + "LoDe"), "LoDe SC Lucene", reader);
//        computeMAP(new GenericFastImageSearcher(1400, GenericDoubleLireFeature.class, (new ScalableColor()).getFieldName() + "LoDe_Hist", true, reader), "LoDe SC L2", reader);
//        computeMAP(new VisualWordsImageSearcher(1400, (new CEDD()).getFieldName() + "LoDe"), "LoDe CEDD Lucene", reader);
//        computeMAP(new GenericFastImageSearcher(1400, GenericDoubleLireFeature.class, (new CEDD()).getFieldName() + "LoDe_Hist", true, reader), "LoDe CEDD L2", reader);

//        computeMAP(new GenericFastImageSearcher(1400, GenericByteLireFeature.class, DocumentBuilder.FIELD_NAME_SURF_VLAD, true, reader), "VLAD-SURF", reader);

    }

    private void computeMAP(ImageSearcher searcher, String prefix, IndexReader reader) throws IOException {
        double queryCount = 0d;
        double errorRate = 0;
        double map = 0;
        double p10 = 0;
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        PrintWriter fw = new PrintWriter(new File("eval/" + prefix.replace(' ', '_') + "-eval.txt"));
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
                    tmpEval += String.format(Locale.US, "%d 1 %s %d %.2f test\n", query2id.get(fileName), hitFile.substring(0, hitFile.lastIndexOf('.')), (int) rank + 1, 100 - hits.score(y));
                    // if (!hitFile.equals(fileName)) {
                    rank++;
                    if (queries.get(fileName).contains(hitFile) || hitFile.equals(fileName)) { // it's a hit.
                        found++;
                        // TODO: Compute error rate, etc. here.
                        avgPrecision += found / rank;// * (1d/queries.get(fileName).size());
//                            if (rank<=60) System.out.print('X');
                        if (rank <= 10) tmpP10++;
                    } else {     // nothing has been found.
                        if (rank == 1) errorRate += 1d;
//                            if (rank<=60) System.out.print('-');
                    }
                }
                // }
//                System.out.println();
                if (found - queries.get(fileName).size() == 1)
                    avgPrecision /= (double) (1d + queries.get(fileName).size());
                else {
                    // some of the results have not been found. We have to deal with it ...
                    System.err.println("Did not find result ;(");
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
        System.out.print(prefix);
        System.out.format("\t%.5f\t%.5f\t%.5f\n", map, p10, errorRate);

    }

    private String getIDfromFileName(String path) {
        // That's the one for Windows. Change for Linux ...
        return path.substring(path.lastIndexOf('\\') + 1).replace(".jpg", ".tif");
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
