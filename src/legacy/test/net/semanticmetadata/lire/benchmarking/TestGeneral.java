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

package net.semanticmetadata.lire.benchmarking;

//import Jama.Matrix;
//import Jama.SingularValueDecomposition;

import junit.framework.TestCase;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.SimpleImageSearchHits;
import net.semanticmetadata.lire.impl.SimpleResult;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

/**
 * User: mlux
 * Date: 25.11.2009
 * Time: 11:54:49
 */
public class TestGeneral extends TestCase {
    // check if this directory exists!!
    public String testIndex = "./temp/generaltestindex";
    //    public String testFiles = "C:\\Temp\\...\\images\\indexsrc\\default\\original";
    public String testFiles = "C:\\Temp\\...\\images\\indexsrc\\...";

    public HashMap<String, String> testcases = new HashMap<String, String>(12);

    private ChainedDocumentBuilder builder;
    private String queryImage;

    private static boolean cutImages = false;
    private double[][] mdata = null;
    private ArrayList<Document> index;

    protected void setUp() {
        builder = new ChainedDocumentBuilder();
//        builder.addBuilder(new GenericDocumentBuilder(FuzzyColorHistogram.class, "FIELD_FUZZYCOLORHIST"));
//        builder.addBuilder(new GenericDocumentBuilder(JpegCoefficientHistogram.class, "FIELD_JPEGCOEFFHIST"));
//        builder.addBuilder(new GenericDocumentBuilder(HSVColorHistogram.class, "FIELD_HSVCOLORHIST"));
//        builder.addBuilder(new GenericDocumentBuilder(GeneralColorLayout.class, "FIELD_GENCL"));
//        builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
//        builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//        builder.addBuilder(new GenericDocumentBuilder(SimpleColorHistogram.class, "FIELD_CH"));
//        builder.addBuilder(new GenericDocumentBuilder(AutoColorCorrelogram.class, "FIELD_ACC"));
        builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
        builder.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());

        // creating test cases ...
        String testcasesDir = "C:\\Temp\\RGA\\cameraShots\\";
        testcases.put(testcasesDir + "bocajr.jpg", "sb_153.jpg");
        testcases.put(testcasesDir + "deskshot_01.jpg", "sb_196.jpg");
        testcases.put(testcasesDir + "deskshot_02.jpg", "sb_213.jpg");
        testcases.put(testcasesDir + "future.jpg", "sb_270.jpg");
        testcases.put(testcasesDir + "icecrystals.jpg", "sb_219.jpg");
        testcases.put(testcasesDir + "jedis.jpg", "sb_214.jpg");
//        testcases.put(testcasesDir + "midnightfogs.jpg", "");
//        testcases.put(testcasesDir + "myfile.jpg", "");
//        testcases.put(testcasesDir + "new_01.jpg", "");
        testcases.put(testcasesDir + "stussey.jpg", "sb_136.jpg");
        testcases.put(testcasesDir + "supremelow.jpg", "sb_136.jpg");
        testcases.put(testcasesDir + "tiffany.jpg", "sb_130.jpg");
//        testcases.put(testcasesDir + "unluckys.jpg", "");
    }

    public void testIndex() throws IOException {
        System.out.println("-< Getting files to index >--------------");
        ArrayList<String> images = FileUtils.getAllImages(new File(testFiles), true);
        System.out.println("-< Indexing " + images.size() + " files >--------------");

        indexFiles(images, builder, testIndex);
        System.out.println("-< Indexing finished >--------------");

    }

    private void indexFiles(ArrayList<String> images, DocumentBuilder builder, String indexPath) throws IOException {
        // eventually check if the directory is there or not ...
        IndexWriter iw = LuceneUtils.createIndexWriter(testIndex, false);
        int count = 0;
        long time = System.currentTimeMillis();
        for (String identifier : images) {
            // TODO: cut toes from the image ... -> doesn't work out very well. Stable at first, decreasing then.
            // TODO: Joint Histogram ...
            // TODO: LSA / PCA on the vectors ...-> this looks like a job for me :-D
            // TODO: local features ...
            Document doc = null;
            if (cutImages) {
                BufferedImage bimg = ImageUtils.cropImage(ImageIO.read(new FileInputStream(identifier)), 0, 0, 200, 69);
                doc = builder.createDocument(bimg, identifier);
            } else doc = builder.createDocument(new FileInputStream(identifier), identifier);
            iw.addDocument(doc);
            count++;
            if (count % 100 == 0) {
                int percent = (int) Math.floor(((double) count * 100.0) / (double) images.size());
                double timeTemp = (double) (System.currentTimeMillis() - time) / 1000d;
                int secsLeft = (int) Math.round(((timeTemp / (double) count) * (double) images.size()) - timeTemp);
                System.out.println(percent + "% finished (" + count + " files), " + secsLeft + " s left");
            }
        }
        long timeTaken = (System.currentTimeMillis() - time);
        float sec = ((float) timeTaken) / 1000f;

        System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
        iw.commit();
        iw.close();
    }

    public void testPrecision() throws IOException, IllegalAccessException, InstantiationException {
        int maxHits = 200;
//        SimpleColorHistogram.DEFAULT_DISTANCE_FUNCTION = SimpleColorHistogram.DistanceFunction.JSD;
//        computeErrorRate(ImageSearcherFactory.createColorHistogramImageSearcher(10), "Color Histogram-L2");
//        System.out.println("> CEDD");
//        computeErrorRate(ImageSearcherFactory.createCEDDImageSearcher(maxHits), "CEDD");
//        System.out.println("> FCTH");
//        computeErrorRate(ImageSearcherFactory.createFCTHImageSearcher(maxHits), "FCTH");
//        System.out.println("> JCD");
//        computeErrorRate(new GenericImageSearcher(maxHits, JCD.class, "FIELD_JCD"), "CEDD");
        System.out.println("> FuzzyColorHist");
//        computeErrorRate(new GenericImageSearcher(maxHits, FuzzyColorHistogram.class, "FIELD_FUZZYCOLORHIST"), "FuzzyColorHistogram");
//        System.out.println("> JpegCoeffHist");
//        computeErrorRate(new GenericImageSearcher(maxHits, JpegCoefficientHistogram.class, "FIELD_JPEGCOEFFHIST"), "JpegCoefficientHistogram");
//        System.out.println("> HSVColorHistogram");
//        computeErrorRate(new GenericImageSearcher(maxHits, HSVColorHistogram.class, "FIELD_HSVCOLORHIST"), "HSVColorHistogram");
//        System.out.println("> SimpleColorHistogram");
//        computeErrorRate(new GenericImageSearcher(maxHits, SimpleColorHistogram.class, "FIELD_CH"), "SimpleColorHistogram");
//        System.out.println("> AutoColorCorrelogram");
//        computeErrorRate(new GenericImageSearcher(maxHits, AutoColorCorrelogram.class, "FIELD_ACC"), "AutoColorCorrelogram");
//        System.out.println("> Tamura");
//        computeErrorRate(new GenericImageSearcher(maxHits, Tamura.class, "FIELD_TAMURA"), "Tamura");
//        System.out.println("> GeneralColorLayout");
//        computeErrorRate(new GenericImageSearcher(maxHits, GeneralColorLayout.class, "FIELD_GENCL"), "GeneralColorLayout");
//        System.out.println("> ScalableColor");
//        computeErrorRate(new SimpleImageSearcher(maxHits, 1.0f, 0f, 0f), "ScalableColor");
        System.out.println("> ColorLayout");
        computeErrorRate(ImageSearcherFactory.createColorLayoutImageSearcher(maxHits), "ColorLayout");
        System.out.println("> ColorLayout (LSA)");
//        computeErrorRateLsa(new SimpleImageSearcher(maxHits, 0f, 1f, 0f), "ColorLayout-Lsa");
//        System.out.println("> Edgehist");
//        computeErrorRate(new SimpleImageSearcher(maxHits, 0f, 0f, 1f), "EdgeHist");
    }

    public void computeErrorRate(ImageSearcher searcher, String prefix) throws IOException, InstantiationException, IllegalAccessException {
//        int maxHits = 10;
        IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(testIndex)));
        for (Iterator<String> testIterator = testcases.keySet().iterator(); testIterator.hasNext(); ) {
            queryImage = testIterator.next();
            Document query;
            if (cutImages) {
                BufferedImage bimg = ImageUtils.cropImage(ImageIO.read(new FileInputStream(queryImage)), 0, 0, 200, 69);
                query = builder.createDocument(new FileInputStream(queryImage), queryImage);
            } else
                query = builder.createDocument(new FileInputStream(queryImage), queryImage);
            ImageSearchHits hits = searcher.search(query, reader);
            // hits = rerank(hits, query, ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT);
            for (int i = 0; i < hits.length(); i++) {
                if (hits.doc(i).get("descriptorImageIdentifier").toLowerCase().endsWith(testcases.get(queryImage))) {
                    System.out.println(queryImage.substring(queryImage.lastIndexOf('\\') + 1) + "-" + prefix + " -> Found at rank " + i + " (" + hits.length() + ")");
                }
            }
            // saveToHtml(queryImage.substring(queryImage.lastIndexOf('\\') + 1) + "-" + prefix, hits, queryImage);
        }
    }
    /*
  public void computeErrorRateLsa(ImageSearcher searcher, String prefix) throws IOException, InstantiationException, IllegalAccessException {
//        int maxHits = 10;
      IndexReader reader = IndexReader.open(SimpleFSDirectory.open(new File(testIndex)), true);
      for (Iterator<String> testIterator = testcases.keySet().iterator(); testIterator.hasNext();) {
          queryImage = testIterator.next();
          BufferedImage bimg = ImageIO.read(new FileInputStream(queryImage));
          if (cutImages)
              bimg = ImageUtils.cropImage(bimg, 0, 0, 200, 69);
          ColorLayout cl = new ColorLayout();
          cl.extract(bimg);
          ImageSearchHits hits = lsa(reader, cl, 100);
          for (int i = 0; i < hits.length(); i++) {
              if (hits.doc(i).get("descriptorImageIdentifier").toLowerCase().endsWith(testcases.get(queryImage))) {
                  System.out.println(queryImage.substring(queryImage.lastIndexOf('\\') + 1) + "-" + prefix + " -> Found at rank " + i);
              }
          }
//            saveToHtml(queryImage.substring(queryImage.lastIndexOf('\\') + 1) + "-" + prefix, hits, queryImage);
      }
  }  */

    private ImageSearchHits rerank(ImageSearchHits hits, Document query, Class descriptorClass, String fieldName) throws IllegalAccessException, InstantiationException {
        ArrayList<SimpleResult> results = new ArrayList<SimpleResult>(hits.length());
        LireFeature qf = getFeature(descriptorClass, query.getValues(fieldName)[0]);
        float maxDistance = 0f;
        for (int i = 0; i < hits.length(); i++) {
            LireFeature lf = getFeature(descriptorClass, hits.doc(i).getValues(fieldName)[0]);
            float distance = lf.getDistance(qf);
            SimpleResult sr = new SimpleResult(distance, hits.doc(i), i);
            results.add(sr);
            maxDistance = Math.max(maxDistance, distance);
        }
        Collections.sort(results);
        return new SimpleImageSearchHits(results, maxDistance);
    }

    private LireFeature getFeature(Class descriptorClass, String data) throws IllegalAccessException, InstantiationException {
        LireFeature lf = (LireFeature) descriptorClass.newInstance();
        if (data != null && data.length() > 0) {
            lf.setStringRepresentation(data);
        }
        return lf;
    }


    private void saveToHtml(String prefix, ImageSearchHits hits, String queryImage) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter("results - " + prefix + ".html"));
        bw.write("<html>\n" +
                "<head><title>Search Results</title></head>\n" +
                "<body bgcolor=\"#FFFFFF\">\n");
        bw.write("<h3>query</h3>\n");
        bw.write("<a href=\"file://" + queryImage + "\"><img src=\"file://" + queryImage + "\"></a><p>\n");
        bw.write("<h3>results</h3>\n");
        for (int i = 0; i < hits.length(); i++) {
            bw.write("<a href=\"file://" + hits.doc(i).get("descriptorImageIdentifier") + "\"><img src=\"file://" + hits.doc(i).get("descriptorImageIdentifier") + "\"></a><p>\n");
        }
        bw.write("</body>\n" +
                "</html>");
        bw.close();
    }


    /*
    private ImageSearchHits lsa(IndexReader reader, ColorLayout query, int numDims) {
        ArrayList<double[]> list = new ArrayList<double[]>(reader.numDocs());
        index = new ArrayList<Document>(reader.numDocs());
        int docs = reader.numDocs();
        for (int i = 0; i < docs; i++) {
            // bugfix by Roman Kern
            if (reader.hasDeletions() && reader.isDeleted(i)) {
                continue;
            }

            try {
                Document d = reader.document(i);
                String cl = d.getValues(DocumentBuilder.FIELD_NAME_COLORLAYOUT)[0];
                ColorLayout cli = new ColorLayout();
                cli.setStringRepresentation(cl);
                double[] hist = new double[cli.getNumberOfCCoeff() * 2 + cli.getNumberOfYCoeff()];
                int pos = 0;
//                for (int j = 0; j < 15; j++) {
                for (int j = 0; j < cli.getNumberOfYCoeff(); j++) {
                    hist[pos] = (double) cli.getYCoeff()[j];
                    pos++;
                }
//                for (int j = 0; j < 10; j++) {
                for (int j = 0; j < cli.getNumberOfCCoeff(); j++) {
                    hist[pos] = (double) cli.getCbCoeff()[j];
                    pos++;
                }
//                for (int j = 0; j < 10; j++) {
                for (int j = 0; j < cli.getNumberOfCCoeff(); j++) {
                    hist[pos] = (double) cli.getCrCoeff()[j];
                    pos++;
                }
                list.add(hist);
                index.add(d);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        double[] hist = new double[query.getNumberOfCCoeff() * 2 + query.getNumberOfYCoeff()];
        int pos = 0;
//        for (int j = 0; j < 15; j++) {
        for (int j = 0; j < query.getNumberOfYCoeff(); j++) {
            hist[pos] = (double) query.getYCoeff()[j];
            pos++;
        }
//        for (int j = 0; j < 10; j++) {
        for (int j = 0; j < query.getNumberOfCCoeff(); j++) {
            hist[pos] = (double) query.getCbCoeff()[j];
            pos++;
        }
//        for (int j = 0; j < 10; j++) {
        for (int j = 0; j < query.getNumberOfCCoeff(); j++) {
            hist[pos] = (double) query.getCrCoeff()[j];
            pos++;
        }
        list.add(hist);
        index.add(new Document());
//        System.out.println("list.size() = " + list.size());
        // create matrix:
        mdata = new double[list.size()][hist.length];
        for (int i = 0; i < mdata.length; i++) {
            mdata[i] = list.get(i);
        }
        Matrix m = new Matrix(mdata);
        long ms = System.currentTimeMillis();
        SingularValueDecomposition svd = m.svd();
        ms = System.currentTimeMillis() - ms;
        System.out.println("ms/1000 = " + ms / 1000);
        double[] singularValues = svd.getSingularValues();
        for (int i = numDims; i < singularValues.length; i++) {
            singularValues[i] = 0d;
        }
        Matrix mNew = svd.getU().times(svd.getS()).times(svd.getV().transpose());
        double[][] data = mNew.getArray();

        // results: 
        TreeSet<SimpleResult> results = new TreeSet<SimpleResult>();
        double maxDistance = 0;
        double[] queryData = data[data.length - 1];
        int max = data.length - 1;
//        max = Math.min(max, 500);
        for (int i = 0; i < max; i++) {
            double[] doubles = data[i];
            double distance = MetricsUtils.distL2(doubles, queryData);
            results.add(new SimpleResult((float) distance, index.get(i)));
            maxDistance = Math.max(maxDistance, distance);
        }
        ImageSearchHits hits = new SimpleImageSearchHits(results, (float) maxDistance);
        return hits;
    }
          */

}
