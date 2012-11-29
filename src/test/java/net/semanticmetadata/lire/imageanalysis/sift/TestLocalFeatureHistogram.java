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
 * ~~~~~~~~~~~~~~~~~~~~
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */
package net.semanticmetadata.lire.imageanalysis.sift;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.clustering.Cluster;
import net.semanticmetadata.lire.clustering.KMeans;
import net.semanticmetadata.lire.imageanalysis.bovw.SiftFeatureHistogramBuilder;
import net.semanticmetadata.lire.imageanalysis.bovw.SurfFeatureHistogramBuilder;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.SiftDocumentBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

//import net.semanticmetadata.lire.clustering.Image;

/**
 * ...
 * Date: 23.09.2008
 * Time: 17:26:00
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class TestLocalFeatureHistogram extends TestCase {
    private Extractor extractor;
    private String indexPath = "wang-index";
    private String testExtensive = "C:\\Temp\\flickrphotos\\flickrphotos";
    //    private String testExtensive = "../liredemo/flickrphotos";
//    private String testExtensive = "./wang-1000";
    private int[] sampleQueries = {284, 77, 108, 416, 144, 534, 898, 104, 67, 10, 607, 165, 343, 973, 591, 659, 812, 231, 261, 224, 227, 914, 427, 810, 979, 716, 253, 708, 751, 269, 531, 699, 835, 370, 642, 504, 297, 970, 929, 20, 669, 434, 201, 9, 575, 631, 730, 7, 546, 816, 431, 235, 289, 111, 862, 184, 857, 624, 323, 393, 465, 905, 581, 626, 212, 459, 722, 322, 584, 540, 194, 704, 410, 267, 349, 371, 909, 403, 724, 573, 539, 812, 831, 600, 667, 672, 454, 873, 452, 48, 322, 424, 952, 277, 565, 388, 149, 966, 524, 36, 528, 75, 337, 655, 836, 698, 230, 259, 897, 652, 590, 757, 673, 937, 676, 650, 297, 434, 358, 789, 484, 975, 318, 12, 506, 38, 979, 732, 957, 904, 852, 635, 620, 28, 59, 732, 84, 788, 562, 913, 173, 508, 32, 16, 882, 847, 320, 185, 268, 230, 259, 931, 653, 968, 838, 906, 596, 140, 880, 847, 297, 77, 983, 536, 494, 530, 870, 922, 467, 186, 254, 727, 439, 241, 12, 947, 561, 160, 740, 705, 619, 571, 745, 774, 845, 507, 156, 936, 473, 830, 88, 66, 204, 737, 770, 445, 358, 707, 95, 349};


    protected void setUp() throws Exception {
        super.setUp();
        extractor = new Extractor();
    }

    public void testKMeans() throws IOException {
//        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
//        KMeans k = new KMeans();
//        int count = 0;
//        for (int i = 0; i < sampleQueries.length; i++) {
//            int id = sampleQueries[i];
////            System.out.print("id = " + id + ": ");
//            String s = testExtensive + "/" + id + ".jpg";
//            System.out.println("s = " + s);
//            List<Feature> features = extractor.computeSiftFeatures(ImageIO.read(new File(s)));
//            List<Histogram> tmpList = new LinkedList<Histogram>();
//            for (Iterator<Feature> histogramIterator = features.iterator(); histogramIterator.hasNext(); ) {
//                Feature feature = histogramIterator.next();
//                tmpList.add(feature);
//            }
//            k.addImage(s, tmpList);
////            if (count > 20) break;
//            count++;
//        }
//        System.out.println("Init clustering");
//        k.init();
//        System.out.println("First step");
//        double laststress = k.clusteringStep();
//        System.out.println("2nd step");
//        double newstress = k.clusteringStep();
//        while (newstress > laststress) {
//            System.out.println("newstress-laststress = " + (newstress - laststress));
//            laststress = newstress;
//            newstress = k.clusteringStep();
//            System.out.print(".");
//        }
//        System.out.println("\nfinished");
//        printClusters(k);
//
//        // create histograms ...
//        List<Image> imgs = k.getImages();
//        Cluster[] clusters = k.getClusters();
//        for (Iterator<Image> imageIterator = imgs.iterator(); imageIterator.hasNext(); ) {
//            Image image = imageIterator.next();
//            image.initHistogram(k.getNumClusters());
//            for (Iterator<Histogram> iterator = image.features.iterator(); iterator.hasNext(); ) {
//                Histogram feat = iterator.next();
//                image.getLocalFeatureHistogram()[k.getClusterOfFeature(feat)]++;
////                image.normalizeFeatureHistogram();
//            }
//        }
//
//        for (Image i : imgs) {
//            i.printHistogram();
//        }
    }

    private void printClusters(KMeans k) {
        Cluster[] clusters = k.getClusters();
        for (int i = 0; i < clusters.length; i++) {
            Cluster cluster = clusters[i];
            System.out.println(i + ": " + cluster.toString());
        }
    }

    public void testSiftIndexing() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        ChainedDocumentBuilder db = new ChainedDocumentBuilder();
        db.addBuilder(new SiftDocumentBuilder());
        db.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
        IndexWriter iw = LuceneUtils.createIndexWriter("sift-idx", true);

        for (int i = 0; i < images.size(); i++) {
//            int sampleQuery = sampleQueries[i];
//            String s = testExtensive + "/" + sampleQuery + ".jpg";
            iw.addDocument(db.createDocument(new FileInputStream(images.get(i)), images.get(i)));
            if (i % 100 == 99) System.out.print(".");
            if (i % 1000 == 999) System.out.print(" ~ " + i + " files indexed\n");
            if (i > 1000) break;
        }
        System.out.println("");
        iw.close();
    }

    public void testSurfIndexing() throws IOException {
        ArrayList<String> images = FileUtils.getAllImages(new File(testExtensive), true);
        ChainedDocumentBuilder db = new ChainedDocumentBuilder();
        db.addBuilder(new SurfDocumentBuilder());
        IndexWriter iw = LuceneUtils.createIndexWriter("sift-idx", true);
        for (int i = 0; i < images.size(); i++) {
//            int sampleQuery = sampleQueries[i];
//            String s = testExtensive + "/" + sampleQuery + ".jpg";
            iw.addDocument(db.createDocument(new FileInputStream(images.get(i)), images.get(i)));
            if (i % 100 == 99) System.out.print(".");
            if (i % 1000 == 999) System.out.print(" ~ " + i + " files indexed\n");
            if (i > 1000) break;
        }
        System.out.println("");
        iw.close();
    }

    public void testCreateLocalFeatureHistogram() throws IOException {
//        testSiftIndexing();

        SiftFeatureHistogramBuilder sh = new SiftFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File("sift-idx"))), 2000);
        sh.index();
        testFindimages();
    }

    public void testCreateSurfFeatureHistogram() throws IOException {
//        testSiftIndexing();

        SurfFeatureHistogramBuilder sh = new SurfFeatureHistogramBuilder(IndexReader.open(FSDirectory.open(new File("surf-idx"))), 500, 1000);
        sh.index();
//        testFindimages();
    }

    public void testFindimages() throws IOException {
//        IndexReader reader = IndexReader.open(FSDirectory.open(new File("sift-idx")));
//        int docID = 700;
//        // test with plain L1:
//        SiftLocalFeatureHistogramImageSearcher searcher = new SiftLocalFeatureHistogramImageSearcher(11);
//        ImageSearchHits searchHits = searcher.search(reader.document(docID), reader);
//        for (int i = 0; i < searchHits.length(); i++) {
//            Document document = searchHits.doc(i);
//            String file = document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
//            System.out.println(searchHits.score(i) + ": " + file);
//        }
//        System.out.println("----");
//        // test based on the Lucene scoring function:
//        String query = reader.document(docID).getValues(DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS)[0];
//        System.out.println("query = " + query);
//        QueryParser qp = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS, new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
//        IndexSearcher isearcher = new IndexSearcher(reader);
//        isearcher.setSimilarity(new Similarity() {
//            @Override
//            public float computeNorm(String s, FieldInvertState fieldInvertState) {
//                return 1f;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public float queryNorm(float v) {
//                return 1;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public float sloppyFreq(int i) {
//                return 0;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public float tf(float v) {
//                return v;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public float idf(int docfreq, int numdocs) {
////                return 1f;  //To change body of implemented methods use File | Settings | File Templates.
//                return (float) (Math.log((double) numdocs / (double) docfreq));  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public float coord(int i, int i1) {
//                return 1;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//        });
//        StringBuilder sb = new StringBuilder();
//        try {
//            TopDocs docs = isearcher.search(qp.parse(query), 10);
//            sb.append("<html>\n" +
//                    "<body bgcolor=\"#FFFFFF\">\n" +
//                    "<table>");
//            for (int i = 0; i < docs.scoreDocs.length; i++) {
//                sb.append("    <tr>\n" +
//                        "        <td>" + docs.scoreDocs[i].score + "</td>\n" +
//                        "        <td><img src=\"" + reader.document(docs.scoreDocs[i].doc).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0] + "\"/></td>\n" +
//                        "    </tr>");
//                System.out.println(docs.scoreDocs[i].score + ": " + reader.document(docs.scoreDocs[i].doc).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
//            }
//            sb.append("</table>\n" +
//                    "</body>\n" +
//                    "</html>");
//            writeToFile(sb.toString(), "result.html");
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public void testSurfSearch() throws IOException {
//        IndexReader reader = IndexReader.open(FSDirectory.open(new File("surf-idx")));
//        int docID = 414;
//
//        String query = reader.document(docID).getValues(DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS)[0];
//        System.out.println("query = " + query);
//        QueryParser qp = new QueryParser(Version.LUCENE_30, DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS, new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
//        IndexSearcher isearcher = new IndexSearcher(reader);
//        isearcher.setSimilarity(new Similarity() {
//            @Override
//            public float computeNorm(String s, FieldInvertState fieldInvertState) {
//                return 1f;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public float queryNorm(float v) {
//                return 1;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public float sloppyFreq(int i) {
//                return 0;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public float tf(float v) {
//                return (float) Math.sqrt(v);  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public float idf(int docfreq, int numdocs) {
//                return 1f;  //To change body of implemented methods use File | Settings | File Templates.
////                return (float) (Math.log((double) numdocs/(double) docfreq));  //To change body of implemented methods use File | Settings | File Templates.
//            }
//
//            @Override
//            public float coord(int i, int i1) {
//                return 1;  //To change body of implemented methods use File | Settings | File Templates.
//            }
//        });
//        StringBuilder sb = new StringBuilder();
//        try {
//            TopDocs docs = isearcher.search(qp.parse(query), 10);
//            sb.append("<html>\n" +
//                    "<body bgcolor=\"#FFFFFF\">\n" +
//                    "<table>");
//            for (int i = 0; i < docs.scoreDocs.length; i++) {
//                sb.append("    <tr>\n" +
//                        "        <td>" + docs.scoreDocs[i].score + ", doc: " + docs.scoreDocs[i].doc + "</td>\n" +
//                        "        <td><img src=\"" + reader.document(docs.scoreDocs[i].doc).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0] + "\"/></td>\n" +
//                        "    </tr>");
//                System.out.println(docs.scoreDocs[i].score + ": " + reader.document(docs.scoreDocs[i].doc).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
//            }
//            sb.append("</table>\n" +
//                    "</body>\n" +
//                    "</html>");
//            writeToFile(sb.toString(), "result.html");
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public void testSiftSerialization() throws IOException {
//        String file = new String("./wang-1000/1.jpg");
//        Extractor e = new Extractor();
//        List<Feature> fs = e.computeSiftFeatures(ImageIO.read(new File(file)));
//        for (Iterator<Feature> featureIterator = fs.iterator(); featureIterator.hasNext(); ) {
//            Feature feature = featureIterator.next();
//            byte[] bytes = feature.getByteArrayRepresentation();
//            Feature cp = new Feature();
//            cp.setByteArrayRepresentation(bytes);
//            System.out.println(feature.getStringRepresentation().equals(cp.getStringRepresentation()));
//        }
//    }
//
//    private void writeToFile(String s, String filename) {
//        try {
//            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filename)));
//            bw.write(s);
//            bw.close();
//        } catch (IOException e) {
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
//        }
    }
}
