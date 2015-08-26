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
 * Updated: 26.08.14 12:56
 */

package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.impl.BitSamplingImageSearcher;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.impl.SimpleImageSearchHits;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.indexing.hashing.LocalitySensitiveHashing;
import net.semanticmetadata.lire.indexing.tools.ParallelExtractor;
import net.semanticmetadata.lire.indexing.tools.ProximityHashingIndexor;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Benchmarking class for hashing based indexes.
 *
 * @author Mathias Lux, mathias@juggle.at, 01.06.13
 */

public class TestHashingIndex extends TestCase {
    private String dataSetImageList = "D:\\Java\\Projects\\Lire\\testdata\\flickrphotos.lst";
//    private String dataSetImageList = "D:\\DataSets\\Flickrphotos\\imageList.txt";
    private String dataSetDataOut = "D:\\Java\\Projects\\Lire\\testdata\\flickrphotos.out";
//    private String dataSetDataOut = "D:\\DataSets\\Flickrphotos\\imageList.out";
    private String testIndex = "C:/Temp/idx-test-hashing";

    public void testExtractFeatures() {
        ParallelExtractor pe = new ParallelExtractor();
        pe.setFileList(new File(dataSetImageList));
        pe.setOutFile(new File(dataSetDataOut));
        ParallelExtractor.setNumberOfThreads(6);

        pe.addFeature(new PHOG());
        pe.addFeature(new CEDD());
//        pe.addFeature(new JCD());
//        pe.addFeature(new FCTH());
        pe.addFeature(new AutoColorCorrelogram());
        pe.addFeature(new OpponentHistogram());
        pe.addFeature(new SimpleColorHistogram());
        pe.addFeature(new ColorLayout());
        pe.addFeature(new EdgeHistogram());
//        pe.addFeature(new SPCEDD());

        pe.run();
    }

    public void testHashing() throws IOException, IllegalAccessException, InstantiationException {
        BitSampling.setBits(12);
        BitSampling.setNumFunctionBundles(150);
//        testHashing(JCD.class, DocumentBuilder.FIELD_NAME_JCD);
        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        testHashing(FCTH.class, DocumentBuilder.FIELD_NAME_FCTH);
        testHashing(OpponentHistogram.class, DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM);
        testHashing(EdgeHistogram.class, DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
        testHashing(PHOG.class, DocumentBuilder.FIELD_NAME_PHOG);
        testHashing(AutoColorCorrelogram.class, DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
        testHashing(SimpleColorHistogram.class, DocumentBuilder.FIELD_NAME_COLORHISTOGRAM);
        testHashing(ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT);
//        testHashing(SPCEDD.class, "spcedd");

//        BitSampling.bits = 12;
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        BitSampling.bits = 16;
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        BitSampling.bits = 24;
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        BitSampling.bits = 28;
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//
//        BitSampling.setNumFunctionBundles(50);
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        BitSampling.setNumFunctionBundles(150);
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        BitSampling.setNumFunctionBundles(200);
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        BitSampling.setNumFunctionBundles(250);
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//
//        BitSampling.bits = 12;
//        BitSampling.setNumFunctionBundles(150);
//        BitSampling.setW(0.01d);
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        testHashing(AutoColorCorrelogram.class, DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
//        BitSampling.setW(0.1d);
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        testHashing(AutoColorCorrelogram.class, DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
//        BitSampling.setW(0.5d);
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        testHashing(AutoColorCorrelogram.class, DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
//        BitSampling.setW(4d);
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        BitSampling.setW(5d);
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        BitSampling.setW(10d);
//        testHashing(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);

    }

    private void testHashing(Class featureClass, String fieldName) throws IOException, InstantiationException, IllegalAccessException {
        String hashesFile = "hashes.obj";
        String hashesFileL = "l_hashes.obj";
        int numResults = 50;
        int maxQueries = 20;
        int queryOffset = 100;

        File file = new File(hashesFile);
        if (file.exists()) file.delete();
        file = new File(hashesFileL);
        if (file.exists()) file.delete();
        BitSampling.generateHashFunctions(hashesFile);
        LocalitySensitiveHashing.generateHashFunctions(hashesFileL);
//        HashingIndexor hi = new HashingIndexor();
        ProximityHashingIndexor hi = new ProximityHashingIndexor();
        BitSampling.readHashFunctions(new FileInputStream(hashesFile));
        LocalitySensitiveHashing.readHashFunctions(new FileInputStream(hashesFileL));
        hi.setFeatureClass(featureClass);
        hi.addInputFile(new File(dataSetDataOut));
        hi.setIndexPath(testIndex);
        hi.run();
        System.out.println();

        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(new File(testIndex)), IOContext.READONCE));
        // generating ground truth for all queries ...
        ImageSearcher groundTruth = new GenericFastImageSearcher(numResults, featureClass, fieldName);
        ArrayList<ImageSearchHits> trueHitsList = new ArrayList<ImageSearchHits>(maxQueries);
        long time = System.currentTimeMillis();
        for (int q = 0; q < maxQueries; q++) {
            trueHitsList.add(q, groundTruth.search(reader.document(q + queryOffset), reader));
        }
        time = System.currentTimeMillis() - time;
        // header
        System.out.println(featureClass.getName().substring(featureClass.getName().lastIndexOf('.') + 1));
        System.out.println("Number of queries: " + maxQueries);
        System.out.println("Time taken for linear search: " + (time / maxQueries));
        System.out.printf("numFunctionBundles: %d, numBits: %d, w: %2.2f, dimensions: %d\n", BitSampling.getNumFunctionBundles(), BitSampling.getBits(), BitSampling.getW(), BitSampling.dimensions);
        System.out.println("#hashedResults\ttrue pos.\t#results\tms per search\tprecision");

        for (int j = 100; j <= 3000; j += 100) {
            ImageSearcher hashed = new BitSamplingImageSearcher(numResults, fieldName, fieldName + "_hash", (LireFeature) featureClass.newInstance(), new FileInputStream(hashesFile), j);
            long ms = 0;
            long msSum = 0;
            int posSum = 0;
            for (int q = 0; q < maxQueries; q++) {
                ms = System.currentTimeMillis();
                ImageSearchHits hashedHits = hashed.search(reader.document(q + queryOffset), reader);
                assert(hashedHits.length()<=numResults);
                msSum += System.currentTimeMillis() - ms;
                HashSet<Integer> t = new HashSet<Integer>(hashedHits.length());
                HashSet<Integer> h = new HashSet<Integer>(hashedHits.length());
                for (int i = 0; i < trueHitsList.get(q).length(); i++) {
                    t.add(((SimpleImageSearchHits) trueHitsList.get(q)).readerID(i));
                    h.add(((SimpleImageSearchHits) hashedHits).readerID(i));
                }
                assert (t.size() == h.size());
                int intersect = 0;
                for (Iterator<Integer> iterator = h.iterator(); iterator.hasNext(); ) {
                    if (t.contains(iterator.next())) {
                        intersect++;
                    }
                }
                posSum += intersect;
            }
            if (j > 1400) j += 100;
            double truePositives = ((double) posSum) / ((double) maxQueries);
            System.out.printf("%4d\t%4.1f\t%4d\t%6.1f\t%1.3f\n", j, truePositives, numResults, ((double) msSum) / ((double) maxQueries), truePositives / (double) numResults);
            if (posSum / maxQueries == numResults) break;
        }
    }
}
