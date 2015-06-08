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
 * Updated: 18.01.15 07:52
 */

package net.semanticmetadata.lire.classifiers;/*
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

import junit.framework.TestCase;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.searchers.BitSamplingImageSearcher;
import net.semanticmetadata.lire.deprecatedclasses.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.indexers.tools.Extractor;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.MMapDirectory;
import weka.attributeSelection.AttributeSelection;
import weka.attributeSelection.InfoGainAttributeEval;
import weka.attributeSelection.Ranker;
import weka.core.Instances;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: mlux, mriegler
 * Date: 01.08.2013
 * Time: 13:53
 */
public class ClassifierTest extends TestCase {

    //MAIN CLASS
    //Starts the classification process
    public void testClassifyNFeatures() throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {

        //config
        boolean useIndexSearch = true; //use a with HashigIndexorMulti class generated index of the test set for search
        String locationSaveResultsFile = "D:\\Fashion10000RunFashion3withTweakmoreNo.txt"; //where should the outcame be saved
        String locationOfIndex = "D:\\Datasets\\FashionTestFashionDataSet\\idx\\index";  //location of the lire index
        String locationOfImages = "D:\\Datasets\\FashionTest\\";     //location of the images
        String locationOfTrainSet = "D:\\Datasets\\FashionTestFashionDataSet\\train.txt"; //location of the trainingsset
        String locationExtracorFile = "D:\\Datasets\\FashionTestFashionDataSet\\indexall.data";    //location of the extractor file
        String locationOfTestset = "D:\\Datasets\\FashionTestFashionDataSet\\test.txt";          //Testset location
     /*   String locationOfIndex = "D:\\Datasets\\FashionTestFashionDataSet\\idx\\index";  //location of the lire index
        String locationOfImages = "D:\\Datasets\\FashionTest\\";     //location of the images
        String locationOfTrainSet = "D:\\Datasets\\FashionTestFashionDataSet\\train.txt"; //location of the trainingsset
        String locationExtracorFile = "D:\\Datasets\\FashionTestFashionDataSet\\indexall.data";    //location of the extractor file
        String locationOfTestset = "D:\\Datasets\\FashionTestFashionDataSet\\test.txt"; */         //Testset location
        double informationGainThreshold = 0.1;    //threshold for information gain
        int numberOfCombinations = 12;      //number of max combinations
        int numberOfNeighbours = 2;        //number of neighbours for search result
        //All possible classes and fields
        //    String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
        //    String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};
        String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
        String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};

        //int[] featureSpace = {144, 80, 192, 33, 630, 168, 60, 192, 18, 64, 64, 64};    //size of feature vector per feature
        //int[] featureSpace = {144, 60, 64, 64, 168, 64, 630, 192, 192, 18, 192, 80};
        int [] featureSpace = new int[classArray.length];
        double[] featuresInformationGain = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};       //for information gain. musst contain the same value of zeros as fiedls and class array have features

        //get the length of the feature spaces
        InputStream inf = new FileInputStream(locationExtracorFile);
        byte[] tempIntf = new byte[4];
        int tmpf, tmpFeaturef;
        byte[] tempf = new byte[100 * 1024];
        while ((tmpf = inf.read(tempIntf, 0, 4)) > 0) {
            tmpf = SerializationUtils.toInt(tempIntf);
            inf.read(tempf, 0, tmpf);
            while (inf.read(tempIntf, 0, 1) > 0) {
                if (tempIntf[0] == -1) break;
                tmpFeaturef = tempIntf[0];
                LireFeature f = (LireFeature) Class.forName(Extractor.features[tmpFeaturef]).newInstance();
                // byte[] length ...
                inf.read(tempIntf, 0, 4);
                tmpf = SerializationUtils.toInt(tempIntf);
                inf.read(tempf, 0, tmpf);
                f.setByteArrayRepresentation(tempf, 0, tmpf);
                //System.out.println(f.getFeatureVector().length+f.getClass().getSimpleName());
                for (int z=0;z<classArray.length;z++){
                    if(f.getClass().getSimpleName().equals(classArray[z]))
                        featureSpace[z] = f.getFeatureVector().length;

                }

            }
            break;
        }


        //Create Hasmap for the feature length
        HashMap<String, Integer> featureSpaceHashMap = new HashMap<String, Integer>(classArray.length);
        for (int d = 0; d < classArray.length; d++)
            featureSpaceHashMap.put(classArray[d], featureSpace[d]);

        //Create Hasmap for the feature information gain
        HashMap<String, Double> featureInformationGainHashMap = new HashMap<String, Double>(classArray.length);
        for (int d = 0; d < classArray.length; d++)
            featureInformationGainHashMap.put(classArray[d], 0.0);


        //classes for the search
        String class1 = "yes";
        String class2 = "no";


        //Calculate the information gain for each feature and reduce the features for combination
        featureInformationGainHashMap = getFeaturesInformationGainScore(locationOfImages, locationOfTrainSet, locationExtracorFile, classArray, featureSpace, featuresInformationGain, featureSpaceHashMap, featureInformationGainHashMap);

        ArrayList<String> reducedFieldsArray = new ArrayList<String>();
        ArrayList<String> reducedClassArray = new ArrayList<String>();


     /*   for (int i = 0; i < featuresInformationGain.length; i++) {
            if (featuresInformationGain[i] >= informationGainThreshold) {
                reducedFieldsArray.add(fieldsArray[i]);
                reducedClassArray.add(classArray[i]);
            }
        }*/
        for (int i = 0; i < classArray.length; i++) {
            if (featureInformationGainHashMap.get(classArray[i]) >= informationGainThreshold) {
                reducedFieldsArray.add(fieldsArray[i]);
                reducedClassArray.add(classArray[i]);
            }
        }

        //If the number of combinations is bigger than the max size of features
        if (numberOfCombinations > reducedClassArray.size())
            numberOfCombinations = reducedClassArray.size();

        System.out.println("Features reduced! Starting with classification. Reduced Feature Set: " + reducedClassArray.toString());


        //Starts the classification
        try {
            if (useIndexSearch)
                testClassifyNCombinedFeaturesMulti(0, 220, locationSaveResultsFile, numberOfNeighbours, locationOfIndex, locationOfImages, locationOfTestset, 0, reducedFieldsArray, reducedClassArray, numberOfCombinations, class1, class2, informationGainThreshold,"TestSet");
            else
                testClassifyNCombinedFeaturesMulti(0, 220, locationSaveResultsFile, numberOfNeighbours, locationOfIndex, locationOfImages, locationOfTestset, 0, reducedFieldsArray, reducedClassArray, numberOfCombinations, class1, class2, informationGainThreshold);

        } catch (NoSuchFieldException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


    //classfication for N combined features
    public static boolean testClassifyNCombinedFeaturesMulti(int start, int end, String storeToFile, int numberOfNeighbours, String indexLocation, String photosLocation, String testSetFile, int searchedClass, ArrayList<String> fieldsArray, ArrayList<String> classArray, int combineNfeatures, String class1, String class2, double informationGainThreshold) throws IOException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, InstantiationException {

        //numer of features and how much should be combined
        int feats = fieldsArray.size();
        int combs = combineNfeatures;

        PrintWriter print_line = new PrintWriter(new BufferedWriter(new FileWriter(storeToFile)));

        //all the combinations stored here
        ArrayList combinations = print_nCr(feats, combs);

        //  String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
        //  String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};

        //get the features for the column names
        String sCombinedFeatures = "";
        for (int i = 0; i < 12; i++) {
            sCombinedFeatures = sCombinedFeatures + "Feature" + i + 1 + ";";
        }
        print_line.print(sCombinedFeatures + "K=;IGTH;Weight Rank=;Class;Precision;Recall;True Negative Rate;Accuracy;False Positive Rate;F-Measure;Count Test Images;Count Correct;ms per test;TP;FP;TN;FN");
        print_line.println();
        print_line.flush();

        ArrayList<String> fields1List = new ArrayList<String>();
        ArrayList<String> class1List = new ArrayList<String>();


        for (int i = 0; i < combinations.size(); i += combs) {
            for (int j = 0; j < combs; j++) {
                //     System.out.print(combinations.get(i + j).toString() + " ");
                int x = (Integer) combinations.get(i + j) - 1;
                fields1List.add(fieldsArray.get(x));
                class1List.add(classArray.get(x));
            }
        }


        for (int i = 0; i < combinations.size(); i += combs) {

            // System.out.println(i);

            ArrayList featureNameList = new ArrayList();
            ArrayList lireFeatureList = new ArrayList();
            ArrayList indexLocationList = new ArrayList();


            //iterate over all fields lists and fill it in a array
            for (int j = 0; j < combs; j++) {
                //   System.out.print(combinations.get(i + j).toString() + " ");
                featureNameList.add((String) DocumentBuilder.class.getField("FIELD_NAME_" + fields1List.get(i + j).toUpperCase()).get(null));
                lireFeatureList.add((LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + class1List.get(i + j)).newInstance());
                indexLocationList.add(indexLocation + class1List.get(i + j));
            }

            boolean weightByRank = true;
            boolean createHTML = true;
            //  String[] classes = {"yes", "no"};
            String[] classes = {class1, class2};
            int k = numberOfNeighbours;


            //System.out.println("Tests for lf1 " + f1 + " with k=" + k + " combined with " + f2 + " - weighting by rank sum: " + weightByRank);
            //System.out.println("========================================");
            HashMap<String, Integer> tag2count = new HashMap<String, Integer>(k);
            HashMap<String, Double> tag2weight = new HashMap<String, Double>(k);
            int c = 0;   // used for just one class ...
            //        for (int c = 0; c < 10; c++) {
            c = searchedClass;

            String classIdentifier = classes[c];

            //"D:\\Datasets\\FashionTest\\fashion10000Test\\" + classIdentifier + ".txt";

            // INIT
            ArrayList<String> classesHTML = new ArrayList<String>();
            ArrayList<String> filesHTML = new ArrayList<String>();


            int[] confusion = new int[2];
            Arrays.fill(confusion, 0);
            HashMap<String, Integer> class2id = new HashMap<String, Integer>(2);
            for (int d = 0; d < classes.length; d++)
                class2id.put(classes[d], d);

            BufferedReader br = new BufferedReader(new FileReader(testSetFile));
            String line;

            IndexReader ir2 = null;
            ImageSearcher bis2 = null;
            IndexReader ir3 = null;
            ImageSearcher bis3 = null;
            IndexReader ir4 = null;
            ImageSearcher bis4 = null;
            IndexReader ir5 = null;
            ImageSearcher bis5 = null;
            IndexReader ir6 = null;
            ImageSearcher bis6 = null;
            IndexReader ir7 = null;
            ImageSearcher bis7 = null;
            IndexReader ir8 = null;
            ImageSearcher bis8 = null;
            IndexReader ir9 = null;
            ImageSearcher bis9 = null;
            IndexReader ir10 = null;
            ImageSearcher bis10 = null;
            IndexReader ir11 = null;
            ImageSearcher bis11 = null;
            IndexReader ir12 = null;
            ImageSearcher bis12 = null;


            IndexReader ir1 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(0))));
            ImageSearcher bis1 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(0).getClass(), (String) featureNameList.get(0), true, ir1);
            if (combs > 1) {
                ir2 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(1))));
                bis2 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(1).getClass(), (String) featureNameList.get(1), true, ir2);
            }
            if (combs > 2) {
                ir3 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(2))));
                bis3 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(2).getClass(), (String) featureNameList.get(2), true, ir3);
            }
            if (combs > 3) {
                ir4 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(3))));
                bis4 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(3).getClass(), (String) featureNameList.get(3), true, ir4);
            }
            if (combs > 4) {
                ir5 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(4))));
                bis5 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(4).getClass(), (String) featureNameList.get(4), true, ir5);
            }
            if (combs > 5) {
                ir6 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(5))));
                bis6 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(5).getClass(), (String) featureNameList.get(5), true, ir6);
            }
            if (combs > 6) {
                ir7 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(6))));
                bis7 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(6).getClass(), (String) featureNameList.get(6), true, ir7);
            }
            if (combs > 7) {
                ir8 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(7))));
                bis8 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(7).getClass(), (String) featureNameList.get(7), true, ir8);
            }
            if (combs > 8) {
                ir9 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(8))));
                bis9 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(8).getClass(), (String) featureNameList.get(8), true, ir9);
            }
            if (combs > 9) {
                ir10 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(9))));
                bis10 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(9).getClass(), (String) featureNameList.get(9), true, ir10);
            }
            if (combs > 10) {
                ir11 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(10))));
                bis11 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(10).getClass(), (String) featureNameList.get(10), true, ir11);
            }
            if (combs > 11) {
                ir12 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(11))));
                bis12 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(11).getClass(), (String) featureNameList.get(11), true, ir12);
            }

            ImageSearchHits hits1;
            ImageSearchHits hits2 = null;
            ImageSearchHits hits3 = null;
            ImageSearchHits hits4 = null;
            ImageSearchHits hits5 = null;
            ImageSearchHits hits6 = null;
            ImageSearchHits hits7 = null;
            ImageSearchHits hits8 = null;
            ImageSearchHits hits9 = null;
            ImageSearchHits hits10 = null;
            ImageSearchHits hits11 = null;
            ImageSearchHits hits12 = null;

            int count = 0, countCorrect = 0;
            double countTp = 0, countFp = 0, countTn = 0, countFn = 0;      //F1 Metric
            long ms = System.currentTimeMillis();
            while ((line = br.readLine()) != null) {

                //  System.out.println(count);

                tag2count.clear();
                tag2weight.clear();
                //  tag2count.put("yes", 1);
                //  tag2count.put("no", 1);
                //  tag2weight.put("yes", 1.0);
                //  tag2weight.put("no", 1.0);

                tag2count.put(class1, 1);
                tag2count.put(class2, 1);
                tag2weight.put(class1, 1.0);
                tag2weight.put(class2, 1.0);


                hits1 = bis1.search(ImageIO.read(new File(line)), ir1);
                if (combs > 1) {
                    hits2 = bis2.search(ImageIO.read(new File(line)), ir2);
                }
                if (combs > 2) {
                    hits3 = bis3.search(ImageIO.read(new File(line)), ir3);
                }
                if (combs > 3) {
                    hits4 = bis4.search(ImageIO.read(new File(line)), ir4);
                }
                if (combs > 4) {
                    hits5 = bis5.search(ImageIO.read(new File(line)), ir5);
                }
                if (combs > 5) {
                    hits6 = bis6.search(ImageIO.read(new File(line)), ir6);
                }
                if (combs > 6) {
                    hits7 = bis7.search(ImageIO.read(new File(line)), ir7);
                }
                if (combs > 7) {
                    hits8 = bis8.search(ImageIO.read(new File(line)), ir8);
                }
                if (combs > 8) {
                    hits9 = bis9.search(ImageIO.read(new File(line)), ir9);
                }
                if (combs > 9) {
                    hits10 = bis10.search(ImageIO.read(new File(line)), ir10);
                }
                if (combs > 10) {
                    hits11 = bis11.search(ImageIO.read(new File(line)), ir11);
                }
                if (combs > 11) {
                    hits12 = bis12.search(ImageIO.read(new File(line)), ir12);
                }

                // set tag weights and counts.
                for (int l = 0; l < k; l++) {

                    //  String tag = getTag(hits1.doc(l), photosLocation);

                    tag2count.put(getTag(hits1.doc(l), photosLocation), tag2count.get(getTag(hits1.doc(l), photosLocation)) + 1);
                    if (combs > 1)
                        tag2count.put(getTag(hits2.doc(l), photosLocation), tag2count.get(getTag(hits2.doc(l), photosLocation)) + 1);
                    if (combs > 2)
                        tag2count.put(getTag(hits3.doc(l), photosLocation), tag2count.get(getTag(hits3.doc(l), photosLocation)) + 1);
                    if (combs > 3)
                        tag2count.put(getTag(hits4.doc(l), photosLocation), tag2count.get(getTag(hits4.doc(l), photosLocation)) + 1);
                    if (combs > 4)
                        tag2count.put(getTag(hits5.doc(l), photosLocation), tag2count.get(getTag(hits5.doc(l), photosLocation)) + 1);
                    if (combs > 5)
                        tag2count.put(getTag(hits6.doc(l), photosLocation), tag2count.get(getTag(hits6.doc(l), photosLocation)) + 1);
                    if (combs > 6)
                        tag2count.put(getTag(hits7.doc(l), photosLocation), tag2count.get(getTag(hits7.doc(l), photosLocation)) + 1);
                    if (combs > 7)
                        tag2count.put(getTag(hits8.doc(l), photosLocation), tag2count.get(getTag(hits8.doc(l), photosLocation)) + 1);
                    if (combs > 8)
                        tag2count.put(getTag(hits9.doc(l), photosLocation), tag2count.get(getTag(hits9.doc(l), photosLocation)) + 1);
                    if (combs > 9)
                        tag2count.put(getTag(hits10.doc(l), photosLocation), tag2count.get(getTag(hits10.doc(l), photosLocation)) + 1);
                    if (combs > 10)
                        tag2count.put(getTag(hits11.doc(l), photosLocation), tag2count.get(getTag(hits11.doc(l), photosLocation)) + 1);
                    if (combs > 11)
                        tag2count.put(getTag(hits12.doc(l), photosLocation), tag2count.get(getTag(hits12.doc(l), photosLocation)) + 1);


                    if (weightByRank) {
                        tag2weight.put(getTag(hits1.doc(l), photosLocation), (double) l);
                        if (combs > 1)
                            tag2weight.put(getTag(hits2.doc(l), photosLocation), (double) l);
                        if (combs > 2)
                            tag2weight.put(getTag(hits3.doc(l), photosLocation), (double) l);
                        if (combs > 3)
                            tag2weight.put(getTag(hits4.doc(l), photosLocation), (double) l);
                        if (combs > 4)
                            tag2weight.put(getTag(hits5.doc(l), photosLocation), (double) l);
                        if (combs > 5)
                            tag2weight.put(getTag(hits6.doc(l), photosLocation), (double) l);
                        if (combs > 6)
                            tag2weight.put(getTag(hits7.doc(l), photosLocation), (double) l);
                        if (combs > 7)
                            tag2weight.put(getTag(hits8.doc(l), photosLocation), (double) l);
                        if (combs > 8)
                            tag2weight.put(getTag(hits9.doc(l), photosLocation), (double) l);
                        if (combs > 9)
                            tag2weight.put(getTag(hits10.doc(l), photosLocation), (double) l);
                        if (combs > 10)
                            tag2weight.put(getTag(hits11.doc(l), photosLocation), (double) l);
                        if (combs > 11)
                            tag2weight.put(getTag(hits12.doc(l), photosLocation), (double) l);
                    }

                }
                // find class, iterate over the tags (classes):
                int maxCount = 0, maxima = 0;
                String classifiedAs = null;
                for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                    String tag = tagIterator.next();
                    if (tag2count.get(tag) > maxCount) {
                        maxCount = tag2count.get(tag);
                        maxima = 1;
                        classifiedAs = tag;
                    } else if (tag2count.get(tag) == maxCount) {
                        maxima++;
                    }
                }
                // if there are two or more classes with the same number of results, then we take a look at the weights.
                // else the class is alread given in classifiedAs.
                if (maxima > 1) {
                    double minWeight = Double.MAX_VALUE;
                    for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                        String tag = tagIterator.next();
                        if (tag2weight.get(tag) < minWeight) {
                            minWeight = tag2weight.get(tag);
                            classifiedAs = tag;
                        }
                    }
                }

                count++;
                //SHOW THE CLASSIFICATION
                //     System.out.println(classifiedAs+";"+line);
                classesHTML.add(classifiedAs);
                filesHTML.add(line);

                //F1 Metric
                //     if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes")) {
                if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals(class1)) {
                    countCorrect++;
                    countTp++;
                    //    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes"))
                } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals(class1))
                    countFp++;

                //    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no")) {
                if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals(class2)) {
                    countCorrect++;
                    countTn++;
                    //     } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no"))
                } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals(class2))
                    countFn++;

                // confusion:
                //confusion[class2id.get(classifiedAs)]++;
//                    System.out.printf("%10s (%4.3f, %10d, %4d)\n", classifiedAs, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);

            }

            double precisicon = getPrecision(countTp, countFp);
            double recall = getRecall(countTp, countFn);
            double trueNegativeRate = getTrueNegativeRate(countTn, countFp);
            double accuracy = getAccuracy(countTp, countFp, countTn, countFn);
            double fMeasure = getFmeasure(precisicon, recall);
            double falsePositiveRate = getFalsePositiveRate(countFp, countTn);
            double mccMeasure = getMccMeasure(countTp, countFp, countTn, countFn);
            double wFM = getWFM(countTp, countFp, countTn, countFn, fMeasure, count);
            // System.out.println("Results for class " + classIdentifier);
            // System.out.printf("Class\tPrecision\tRecall\tTrue Negative Rate\tAccuracy\tF-Measure\tCount Test Images\tCount Corret\tms per test\n");
            // System.out.printf("%s\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%10d\t%10d\t%4d\n", classIdentifier, precisicon, recall, trueNegativeRate,accuracy, fMeasure,  count, countCorrect, (System.currentTimeMillis() - ms) / count);

            //   System.out.println(i + 1 + " of " + class1List.size() + " finished. " + (System.currentTimeMillis() - ms) / 1000 + " seconds per round. " + "Feature: " + " Current y: " + i);

            String classesLongName = "";

            for (int j = 0; j < combs; j++) {
                //   System.out.print(combinations.get(i + j).toString() + " ");
                classesLongName = classesLongName + fields1List.get(i + j) + ";";
            }

            //   print_line.printf("%s,%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", classesLongName, k, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            System.out.printf("%s%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", classesLongName, k, informationGainThreshold, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, mccMeasure, wFM, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            print_line.printf("%s%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", classesLongName, k, informationGainThreshold, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, mccMeasure, wFM, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            print_line.flush();

            //Create HTML
            if (createHTML == true) {

                String fileName = "classifieresults-" + System.currentTimeMillis() / 1000 + ".html";
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
                bw.write("<html>\n" +
                        "<head><title>Classification Results</title></head>\n" +
                        "<body bgcolor=\"#FFFFFF\">\n");
                bw.write("<table>");

                // int elems = Math.min(filesHTML.size(),50);
                int elems = filesHTML.size();

                for (int d = 0; i < elems; d++) {
                    if (d % 3 == 0) bw.write("<tr>");

                    String s = filesHTML.get(d);
                    String colorF = "rgb(0, 255, 0)";

                    if (classesHTML.get(d).equals("no"))
                        colorF = "rgb(255, 0, 0)";
                    //  String s = reader.document(topDocs.scoreDocs[i].doc).get("descriptorImageIdentifier");
                    //  System.out.println(reader.document(topDocs.scoreDocs[i].doc).get("featLumLay"));
                    //  s = new File(s).getAbsolutePath();
                    // System.out.println(s);
                    bw.write("<td><a href=\"" + s + "\"><img style=\"max-width:220px;border:medium solid " + colorF + ";\"src=\"" + s + "\" border=\"" + 5 + "\" style=\"border: 3px\n" +
                            "black solid;\"></a></td>\n");
                    if (d % 3 == 2) bw.write("</tr>");
                }
                if (elems % 3 != 0) {
                    if (elems % 3 == 2) {
                        bw.write("<td>-</td with exit code 0\nd>\n");
                        bw.write("<td>-</td>\n");
                    } else if (elems % 3 == 2) {
                        bw.write("<td>-</td>\n");
                    }
                    bw.write("</tr>");
                }

                bw.write("</table></body>\n" +
                        "</html>");
                bw.close();
            }
            //   } // kfor
//        }
        }
        print_line.close();
        return true;
    }

    //use index for classification, its faster
    public static boolean testClassifyNCombinedFeaturesMulti(int start, int end, String storeToFile, int numberOfNeighbours, String indexLocation, String photosLocation, String testSetFile, int searchedClass, ArrayList<String> fieldsArray, ArrayList<String> classArray, int combineNfeatures, String class1, String class2, double informationGainThreshold, String useIndex) throws IOException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, InstantiationException {

        //numer of features and how much should be combined
        int feats = fieldsArray.size();
        int combs = combineNfeatures;

        PrintWriter print_line = new PrintWriter(new BufferedWriter(new FileWriter(storeToFile)));

        //all the combinations stored here
        ArrayList combinations = print_nCr(feats, combs);

        //  String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
        //  String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};

        //get the features for the column names
        String sCombinedFeatures = "";
        for (int i = 0; i < 12; i++) {
            sCombinedFeatures = sCombinedFeatures + "Feature" + i + 1 + ";";
        }
        print_line.print(sCombinedFeatures + "K=;IGTH;Weight Rank=;Class;Precision;Recall;True Negative Rate;Accuracy;False Positive Rate;F-Measure;Count Test Images;Count Correct;ms per test;TP;FP;TN;FN");
        print_line.println();
        print_line.flush();

        ArrayList<String> fields1List = new ArrayList<String>();
        ArrayList<String> class1List = new ArrayList<String>();


        for (int i = 0; i < combinations.size(); i += combs) {
            for (int j = 0; j < combs; j++) {
                //     System.out.print(combinations.get(i + j).toString() + " ");
                int x = (Integer) combinations.get(i + j) - 1;
                fields1List.add(fieldsArray.get(x));
                class1List.add(classArray.get(x));
            }
        }


        for (int i = 0; i < combinations.size(); i += combs) {

            // System.out.println(i);

            ArrayList featureNameList = new ArrayList();
            ArrayList lireFeatureList = new ArrayList();
            ArrayList indexLocationList = new ArrayList();


            //iterate over all fields lists and fill it in a array
            for (int j = 0; j < combs; j++) {
                //   System.out.print(combinations.get(i + j).toString() + " ");
                featureNameList.add((String) DocumentBuilder.class.getField("FIELD_NAME_" + fields1List.get(i + j).toUpperCase()).get(null));
                lireFeatureList.add((LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + class1List.get(i + j)).newInstance());
                indexLocationList.add(indexLocation + class1List.get(i + j));
            }

            boolean weightByRank = true;
            boolean createHTML = true;
            //  String[] classes = {"yes", "no"};
            String[] classes = {class1, class2};
            int k = numberOfNeighbours;


            //System.out.println("Tests for lf1 " + f1 + " with k=" + k + " combined with " + f2 + " - weighting by rank sum: " + weightByRank);
            //System.out.println("========================================");
            HashMap<String, Integer> tag2count = new HashMap<String, Integer>(k);
            HashMap<String, Double> tag2weight = new HashMap<String, Double>(k);
            int c = 0;   // used for just one class ...
            //        for (int c = 0; c < 10; c++) {
            c = searchedClass;

            String classIdentifier = classes[c];

            //"D:\\Datasets\\FashionTest\\fashion10000Test\\" + classIdentifier + ".txt";

            // INIT
            ArrayList<String> classesHTML = new ArrayList<String>();
            ArrayList<String> filesHTML = new ArrayList<String>();


            int[] confusion = new int[2];
            Arrays.fill(confusion, 0);
            HashMap<String, Integer> class2id = new HashMap<String, Integer>(2);
            for (int d = 0; d < classes.length; d++)
                class2id.put(classes[d], d);

            //   BufferedReader br = new BufferedReader(new FileReader(testSetFile));
            //   String line;

            IndexReader irt1 = null;
            IndexReader irt2 = null;
            IndexReader irt3 = null;
            IndexReader irt4 = null;
            IndexReader irt5 = null;
            IndexReader irt6 = null;
            IndexReader irt7 = null;
            IndexReader irt8 = null;
            IndexReader irt9 = null;
            IndexReader irt10 = null;
            IndexReader irt11 = null;
            IndexReader irt12 = null;


            IndexReader ir2 = null;
            ImageSearcher bis2 = null;
            IndexReader ir3 = null;
            ImageSearcher bis3 = null;
            IndexReader ir4 = null;
            ImageSearcher bis4 = null;
            IndexReader ir5 = null;
            ImageSearcher bis5 = null;
            IndexReader ir6 = null;
            ImageSearcher bis6 = null;
            IndexReader ir7 = null;
            ImageSearcher bis7 = null;
            IndexReader ir8 = null;
            ImageSearcher bis8 = null;
            IndexReader ir9 = null;
            ImageSearcher bis9 = null;
            IndexReader ir10 = null;
            ImageSearcher bis10 = null;
            IndexReader ir11 = null;
            ImageSearcher bis11 = null;
            IndexReader ir12 = null;
            ImageSearcher bis12 = null;

    /*        IndexReader ir2 = null;
            BitSamplingImageSearcher bis2 = null;
            IndexReader ir3 = null;
            BitSamplingImageSearcher bis3 = null;
            IndexReader ir4 = null;
            BitSamplingImageSearcher bis4 = null;
            IndexReader ir5 = null;
            BitSamplingImageSearcher bis5 = null;
            IndexReader ir6 = null;
            BitSamplingImageSearcher bis6 = null;
            IndexReader ir7 = null;
            BitSamplingImageSearcher bis7 = null;
            IndexReader ir8 = null;
            BitSamplingImageSearcher bis8 = null;
            IndexReader ir9 = null;
            BitSamplingImageSearcher bis9 = null;
            IndexReader ir10 = null;
            BitSamplingImageSearcher bis10 = null;
            IndexReader ir11 = null;
            BitSamplingImageSearcher bis11 = null;
            IndexReader ir12 = null;
            BitSamplingImageSearcher bis12 = null;*/


            IndexReader ir1 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(0))));
            irt1 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(0)+"TestSet")));
            //  ImageSearcher bis1 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(0).getClass(), (String) featureNameList.get(0), true, ir1);
            GenericFastImageSearcher bis1 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(0).getClass(), (String) featureNameList.get(0), true, ir1);
            if (combs > 1) {
                ir2 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(1))));
                irt2 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(1)+"TestSet")));
                bis2 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(1).getClass(), (String) featureNameList.get(1), true, ir2);
            }
            if (combs > 2) {
                ir3 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(2))));
                irt3 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(2)+"TestSet")));
                bis3 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(2).getClass(), (String) featureNameList.get(2), true, ir3);
            }
            if (combs > 3) {
                ir4 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(3))));
                irt4 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(3)+"TestSet")));
                bis4 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(3).getClass(), (String) featureNameList.get(3), true, ir4);
            }
            if (combs > 4) {
                ir5 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(4))));
                irt5 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(4)+"TestSet")));
                bis5 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(4).getClass(), (String) featureNameList.get(4), true, ir5);
            }
            if (combs > 5) {
                ir6 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(5))));
                irt6 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(5)+"TestSet")));
                bis6 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(5).getClass(), (String) featureNameList.get(5), true, ir6);
            }
            if (combs > 6) {
                ir7 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(6))));
                irt7 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(6)+"TestSet")));
                bis7 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(6).getClass(), (String) featureNameList.get(6), true, ir7);
            }
            if (combs > 7) {
                ir8 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(7))));
                irt8 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(7)+"TestSet")));
                bis8 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(7).getClass(), (String) featureNameList.get(7), true, ir8);
            }
            if (combs > 8) {
                ir9 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(8))));
                irt9 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(8)+"TestSet")));
                bis9 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(8).getClass(), (String) featureNameList.get(8), true, ir9);
            }
            if (combs > 9) {
                ir10 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(9))));
                irt10 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(9)+"TestSet")));
                bis10 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(9).getClass(), (String) featureNameList.get(9), true, ir10);
            }
            if (combs > 10) {
                ir11 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(10))));
                irt11 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(10)+"TestSet")));
                bis11 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(10).getClass(), (String) featureNameList.get(10), true, ir11);
            }
            if (combs > 11) {
                ir12 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(11))));
                irt12 = DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(11)+"TestSet")));
                bis12 = new GenericFastImageSearcher(k, (Class<?>) lireFeatureList.get(11).getClass(), (String) featureNameList.get(11), true, ir12);
            }

            ImageSearchHits hits1;
            ImageSearchHits hits2 = null;
            ImageSearchHits hits3 = null;
            ImageSearchHits hits4 = null;
            ImageSearchHits hits5 = null;
            ImageSearchHits hits6 = null;
            ImageSearchHits hits7 = null;
            ImageSearchHits hits8 = null;
            ImageSearchHits hits9 = null;
            ImageSearchHits hits10 = null;
            ImageSearchHits hits11 = null;
            ImageSearchHits hits12 = null;

            int count = 0, countCorrect = 0;
            double countTp = 0, countFp = 0, countTn = 0, countFn = 0;      //F1 Metric
            long ms = System.currentTimeMillis();
            for (int x = 0; x <  irt1.numDocs();x++){

                //  while ((line = br.readLine()) != null) {

                // System.out.println(x);

                tag2count.clear();
                tag2weight.clear();
                //  tag2count.put("yes", 1);
                //  tag2count.put("no", 1);
                //  tag2weight.put("yes", 1.0);
                //  tag2weight.put("no", 1.0);


                tag2count.put(class1, 1);
                tag2count.put(class2, 1);
                tag2weight.put(class1, 1.0);
                tag2weight.put(class2, 1.0);


                hits1 = bis1.search(irt1.document(x), ir1);
                if (combs > 1) {
                    hits2 = bis2.search(irt2.document(x), ir2);
                }
                if (combs > 2) {
                    hits3 = bis3.search(irt3.document(x), ir3);
                }
                if (combs > 3) {
                    hits4 = bis4.search(irt4.document(x), ir4);
                }
                if (combs > 4) {
                    hits5 = bis5.search(irt5.document(x), ir5);
                }
                if (combs > 5) {
                    hits6 = bis6.search(irt6.document(x), ir6);
                }
                if (combs > 6) {
                    hits7 = bis7.search(irt7.document(x), ir7);
                }
                if (combs > 7) {
                    hits8 = bis8.search(irt8.document(x), ir8);
                }
                if (combs > 8) {
                    hits9 = bis9.search(irt9.document(x), ir9);
                }
                if (combs > 9) {
                    hits10 = bis10.search(irt10.document(x), ir10);
                }
                if (combs > 10) {
                    hits11 = bis11.search(irt11.document(x), ir11);
                }
                if (combs > 11) {
                    hits12 = bis12.search(irt12.document(x), ir12);
                }

                // set tag weights and counts.
                for (int l = 0; l < k; l++) {

                    //  String tag = getTag(hits1.doc(l), photosLocation);

                    tag2count.put(getTag(hits1.doc(l), photosLocation), tag2count.get(getTag(hits1.doc(l), photosLocation)) + 1);
                    if (combs > 1)
                        tag2count.put(getTag(hits2.doc(l), photosLocation), tag2count.get(getTag(hits2.doc(l), photosLocation)) + 1);
                    if (combs > 2)
                        tag2count.put(getTag(hits3.doc(l), photosLocation), tag2count.get(getTag(hits3.doc(l), photosLocation)) + 1);
                    if (combs > 3)
                        tag2count.put(getTag(hits4.doc(l), photosLocation), tag2count.get(getTag(hits4.doc(l), photosLocation)) + 1);
                    if (combs > 4)
                        tag2count.put(getTag(hits5.doc(l), photosLocation), tag2count.get(getTag(hits5.doc(l), photosLocation)) + 1);
                    if (combs > 5)
                        tag2count.put(getTag(hits6.doc(l), photosLocation), tag2count.get(getTag(hits6.doc(l), photosLocation)) + 1);
                    if (combs > 6)
                        tag2count.put(getTag(hits7.doc(l), photosLocation), tag2count.get(getTag(hits7.doc(l), photosLocation)) + 1);
                    if (combs > 7)
                        tag2count.put(getTag(hits8.doc(l), photosLocation), tag2count.get(getTag(hits8.doc(l), photosLocation)) + 1);
                    if (combs > 8)
                        tag2count.put(getTag(hits9.doc(l), photosLocation), tag2count.get(getTag(hits9.doc(l), photosLocation)) + 1);
                    if (combs > 9)
                        tag2count.put(getTag(hits10.doc(l), photosLocation), tag2count.get(getTag(hits10.doc(l), photosLocation)) + 1);
                    if (combs > 10)
                        tag2count.put(getTag(hits11.doc(l), photosLocation), tag2count.get(getTag(hits11.doc(l), photosLocation)) + 1);
                    if (combs > 11)
                        tag2count.put(getTag(hits12.doc(l), photosLocation), tag2count.get(getTag(hits12.doc(l), photosLocation)) + 1);


                    if (weightByRank) {
                        tag2weight.put(getTag(hits1.doc(l), photosLocation), (double) l);
                        if (combs > 1)
                            tag2weight.put(getTag(hits2.doc(l), photosLocation), (double) l);
                        if (combs > 2)
                            tag2weight.put(getTag(hits3.doc(l), photosLocation), (double) l);
                        if (combs > 3)
                            tag2weight.put(getTag(hits4.doc(l), photosLocation), (double) l);
                        if (combs > 4)
                            tag2weight.put(getTag(hits5.doc(l), photosLocation), (double) l);
                        if (combs > 5)
                            tag2weight.put(getTag(hits6.doc(l), photosLocation), (double) l);
                        if (combs > 6)
                            tag2weight.put(getTag(hits7.doc(l), photosLocation), (double) l);
                        if (combs > 7)
                            tag2weight.put(getTag(hits8.doc(l), photosLocation), (double) l);
                        if (combs > 8)
                            tag2weight.put(getTag(hits9.doc(l), photosLocation), (double) l);
                        if (combs > 9)
                            tag2weight.put(getTag(hits10.doc(l), photosLocation), (double) l);
                        if (combs > 10)
                            tag2weight.put(getTag(hits11.doc(l), photosLocation), (double) l);
                        if (combs > 11)
                            tag2weight.put(getTag(hits12.doc(l), photosLocation), (double) l);
                    }
                    //  System.out.println(System.currentTimeMillis()-ms);
                    //  ms=System.currentTimeMillis();
                }
                // find class, iterate over the tags (classes):
                int maxCount = 0, maxima = 0;
                String classifiedAs = null;
                for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                    String tag = tagIterator.next();
                    //  System.out.println(tag+tag2count.get(tag));
                    if (tag2count.get(tag) > maxCount) {
                        maxCount = tag2count.get(tag);
                        maxima = 1;
                        classifiedAs = tag;

                    } else if (tag2count.get(tag) == maxCount) {
                        maxima++;
                    }
                }
                // if there are two or more classes with the same number of results, then we take a look at the weights.
                // else the class is alread given in classifiedAs.
                if (maxima > 1) {
                    double minWeight = Double.MAX_VALUE;
                    for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                        String tag = tagIterator.next();
                        if (tag2weight.get(tag) < minWeight) {
                            minWeight = tag2weight.get(tag);
                            classifiedAs = tag;
                        }
                    }
                }

                count++;
                //SHOW THE CLASSIFICATION
                //     System.out.println(classifiedAs+";"+line);
                classesHTML.add(classifiedAs);
                filesHTML.add(irt1.document(x).getField("descriptorImageIdentifier").stringValue());

                //F1 Metric
                //     if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes")) {
                if (classifiedAs.equals(getTag(irt1.document(x), photosLocation)) && classifiedAs.equals(class1)) {
                    countCorrect++;
                    countTp++;
                    //    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes"))
                } else if (!classifiedAs.equals(getTag(irt1.document(x), photosLocation)) && classifiedAs.equals(class1))
                    countFp++;

                //    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no")) {
                if (classifiedAs.equals(getTag(irt1.document(x), photosLocation)) && classifiedAs.equals(class2)) {
                    countCorrect++;
                    countTn++;
                    //     } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no"))
                } else if (!classifiedAs.equals(getTag(irt1.document(x), photosLocation)) && classifiedAs.equals(class2))
                    countFn++;

                // confusion:
                //confusion[class2id.get(classifiedAs)]++;
//                    System.out.printf("%10s (%4.3f, %10d, %4d)\n", classifiedAs, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);

            }

            double precisicon = getPrecision(countTp, countFp);
            double recall = getRecall(countTp, countFn);
            double trueNegativeRate = getTrueNegativeRate(countTn, countFp);
            double accuracy = getAccuracy(countTp, countFp, countTn, countFn);
            double fMeasure = getFmeasure(precisicon, recall);
            double falsePositiveRate = getFalsePositiveRate(countFp, countTn);
            double mccMeasure = getMccMeasure(countTp, countFp, countTn, countFn);
            double wFM = getWFM(countTp, countFp, countTn, countFn, fMeasure, count);
            // System.out.println("Results for class " + classIdentifier);
            // System.out.printf("Class\tPrecision\tRecall\tTrue Negative Rate\tAccuracy\tF-Measure\tCount Test Images\tCount Corret\tms per test\n");
            // System.out.printf("%s\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%10d\t%10d\t%4d\n", classIdentifier, precisicon, recall, trueNegativeRate,accuracy, fMeasure,  count, countCorrect, (System.currentTimeMillis() - ms) / count);

            //   System.out.println(i + 1 + " of " + class1List.size() + " finished. " + (System.currentTimeMillis() - ms) / 1000 + " seconds per round. " + "Feature: " + " Current y: " + i);

            String classesLongName = "";

            for (int j = 0; j < combs; j++) {
                //   System.out.print(combinations.get(i + j).toString() + " ");
                classesLongName = classesLongName + fields1List.get(i + j) + ";";
            }

            //   print_line.printf("%s,%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", classesLongName, k, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            System.out.printf("%s%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", classesLongName, k, informationGainThreshold, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, mccMeasure, wFM, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            print_line.printf("%s%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", classesLongName, k, informationGainThreshold, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, mccMeasure, wFM, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            print_line.flush();

            //Create HTML
            if (createHTML == true) {

                String fileName = "classifieresults-" + System.currentTimeMillis() / 1000 + ".html";
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
                bw.write("<html>\n" +
                        "<head><title>Classification Results</title></head>\n" +
                        "<body bgcolor=\"#FFFFFF\">\n");
                bw.write("<table>");

                // int elems = Math.min(filesHTML.size(),50);
                int elems = filesHTML.size();

                for (int d = 0; d < elems; d++) {
                    if (d % 3 == 0) bw.write("<tr>");

                    String s = filesHTML.get(d);
                    String colorF = "rgb(0, 255, 0)";

                    if (classesHTML.get(d).equals("no"))
                        colorF = "rgb(255, 0, 0)";
                    // String s = ir1.document(topDocs.scoreDocs[i].doc).get("descriptorImageIdentifier");
                    // String s = filesHTML.get(d);
                    //  System.out.println(reader.document(topDocs.scoreDocs[i].doc).get("featLumLay"));
                    //  s = new File(s).getAbsolutePath();
                    // System.out.println(s);
                    bw.write("<td><a href=\"" + s + "\"><img style=\"max-width:220px;border:medium solid " + colorF + ";\"src=\"" + s + "\" border=\"" + 5 + "\" style=\"border: 3px\n" +
                            "black solid;\"></a></td>\n");
                    if (d % 3 == 2) bw.write("</tr>");
                }
                if (elems % 3 != 0) {
                    if (elems % 3 == 2) {
                        bw.write("<td>-</td with exit code 0\nd>\n");
                        bw.write("<td>-</td>\n");
                    } else if (elems % 3 == 2) {
                        bw.write("<td>-</td>\n");
                    }
                    bw.write("</tr>");
                }

                bw.write("</table></body>\n" +
                        "</html>");
                bw.close();
            }
            //   } // kfor
//        }
        }
        print_line.close();
        return true;
    }


    //ground class for classification
    public void testClassify() throws IOException {
        boolean weightByRank = true;
        String[] classes = {"2012", "beach", "food", "london", "music", "nature", "people", "sky", "travel", "wedding"};
        int k = 50;
        //String indexLocation = "D:\\Datasets\\FashionTestItemDataSet\\idx\\index";
        String photosLocation = "D:\\Datasets\\FashionTestItemDataSet\\";
        String fieldName = DocumentBuilder.FIELD_NAME_COLORLAYOUT;
        LireFeature feature = new ColorLayout();
        String indexPath = "E:\\acmgc-cl-idx";
        System.out.println("Tests for feature " + fieldName + " with k=" + k + " - weighting by rank sum: " + weightByRank);
        System.out.println("========================================");
        HashMap<String, Integer> tag2count = new HashMap<String, Integer>(k);
        HashMap<String, Double> tag2weight = new HashMap<String, Double>(k);
        int c = 9;   // used for just one class ...
//        for (int c = 0; c < 10; c++) {
        String classIdentifier = classes[c];
        String listFiles = "D:\\DataSets\\Yahoo-GC\\test\\" + classIdentifier + ".txt";

        // INIT
        int[] confusion = new int[10];
        Arrays.fill(confusion, 0);
        HashMap<String, Integer> class2id = new HashMap<String, Integer>(10);
        for (int i = 0; i < classes.length; i++)
            class2id.put(classes[i], i);

        BufferedReader br = new BufferedReader(new FileReader(listFiles));
        String line;
        IndexReader ir = DirectoryReader.open(MMapDirectory.open(new File(indexPath)));
        // in-memory linear search
//            ImageSearcher bis = new GenericFastImageSearcher(k, feature.getClass(), fieldName, true, ir);
        // hashing based searcher
        BitSamplingImageSearcher bis = new BitSamplingImageSearcher(k, fieldName, fieldName + "_hash", feature, 1000);
        ImageSearchHits hits;
        int count = 0, countCorrect = 0;
        long ms = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {
            try {
                tag2count.clear();
                tag2weight.clear();
                hits = bis.search(ImageIO.read(new File(line)), ir);
                // set tag weights and counts.
                for (int l = 0; l < k; l++) {
                    String tag = getTag(hits.doc(l), photosLocation);
                    if (tag2count.get(tag) == null) tag2count.put(tag, 1);
                    else tag2count.put(tag, tag2count.get(tag) + 1);
                    if (weightByRank) {
                        if (tag2weight.get(tag) == null) tag2weight.put(tag, (double) l);
                        else tag2weight.put(tag, (double) l + tag2weight.get(tag));
                    } else {
                        if (tag2weight.get(tag) == null) tag2weight.put(tag, Double.valueOf(hits.score(l)));
                        else tag2weight.put(tag, (double) l + hits.score(l));
                    }
                }
                // find class:
                int maxCount = 0, maxima = 0;
                String classifiedAs = null;
                for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                    String tag = tagIterator.next();
                    if (tag2count.get(tag) > maxCount) {
                        maxCount = tag2count.get(tag);
                        maxima = 1;
                        classifiedAs = tag;
                    } else if (tag2count.get(tag) == maxCount) {
                        maxima++;
                    }
                }
                // if there are two or more classes with the same number of results, then we take a look at the weights.
                // else the class is alread given in classifiedAs.
                if (maxima > 1) {
                    double minWeight = Double.MAX_VALUE;
                    for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                        String tag = tagIterator.next();
                        if (tag2weight.get(tag) < minWeight) {
                            minWeight = tag2weight.get(tag);
                            classifiedAs = tag;
                        }
                    }
                }
//                    if (tag2.equals(tag3)) tag1 = tag2;
                count++;
                if (classifiedAs.equals(classIdentifier)) countCorrect++;
                // confusion:
                confusion[class2id.get(classifiedAs)]++;
//                    System.out.printf("%10s (%4.3f, %10d, %4d)\n", classifiedAs, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);
            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
        }
//            System.out.println("Results for class " + classIdentifier);
        System.out.printf("Class\tAvg. Precision\tCount Test Images\tms per test\n");
        System.out.printf("%s\t%4.5f\t%10d\t%4d\n", classIdentifier, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);
        System.out.printf("Confusion\t");
//            for (int i = 0; i < classes.length; i++) {
//                System.out.printf("%s\t", classes[i]);
//            }
//            System.out.println();
        for (int i = 0; i < classes.length; i++) {
            System.out.printf("%d\t", confusion[i]);
        }
        System.out.println();
//        }
    }

    //Classifie Fashion(or every other frameing category) in yes or no.
    public void testClassifyFashion() throws IOException {

        PrintWriter print_line = new PrintWriter(new BufferedWriter(new FileWriter("D:\\resultsSingleFeatureItem1.txt")));

        String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
        String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};

        // System.out.printf("Feature1;Feature2;Feature3;K=;Weight Rank=;Class;Precision;Recall;True Negative Rate;Accuracy;F-Measure;Count Test Images;Count Corret;ms per test;");

        print_line.print("Feature1;Feature2;Feature3;K=;Weight Rank=;Class;Precision;Recall;True Negative Rate;Accuracy; False Positive Rate;F-Measure;Count Test Images;Count Correct;ms per test;TP;FP;TN;FN");
        print_line.println();
        print_line.flush();

        for (int y = 0; y < fieldsArray.length; y++) {

            String fieldName1String = "FIELD_NAME_" + fieldsArray[y].toUpperCase();
            boolean weightByRank = true;
            boolean createHTML = false;
            String[] classes = {"yes", "no"};
            int k = 1;
            String indexLocation = "D:\\Datasets\\FashionTestItemDataSet\\idx\\index";
            String photosLocation = "D:\\Datasets\\FashionTestItemDataSet\\";
            //  String listFiles = "D:\\Datasets\\FashionTestItemDataSet\\itemtest.txt";
            String listFiles = "D:\\Datasets\\FashionTestItemDataSet\\itemtest.txt";

            // CONFIG
            String fieldName = null;
            try {
                fieldName = (String) DocumentBuilder.class.getField(fieldName1String).get(null);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            LireFeature feature = null;
            try {
                feature = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[y]).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            String indexPath = "D:\\Datasets\\FashionTestItemDataSet\\idx\\index" + classArray[y];


            //  for (int ik = 0;ik<k;ik++)       {

            //   System.out.println("Tests for feature " + fieldName + " with k=" + k + " - weighting by rank sum: " + weightByRank);
            //   System.out.println("========================================");
            HashMap<String, Integer> tag2count = new HashMap<String, Integer>(k);
            HashMap<String, Double> tag2weight = new HashMap<String, Double>(k);
            int c = 0;   // used for just one class ...
//        for (int c = 0; c < 10; c++) {
            String classIdentifier = classes[c];

            //"D:\\Datasets\\FashionTest\\fashion10000Test\\" + classIdentifier + ".txt";

            // INIT
            ArrayList<String> classesHTML = new ArrayList<String>();
            ArrayList<String> filesHTML = new ArrayList<String>();

            int[] confusion = new int[2];
            Arrays.fill(confusion, 0);
            HashMap<String, Integer> class2id = new HashMap<String, Integer>(2);
            for (int i = 0; i < classes.length; i++)
                class2id.put(classes[i], i);

            BufferedReader br = new BufferedReader(new FileReader(listFiles));
            String line;
            IndexReader ir = DirectoryReader.open(MMapDirectory.open(new File(indexPath)));
            // in-memory linear search
            ImageSearcher bis = new GenericFastImageSearcher(k, feature.getClass(), fieldName, true, ir);
            // hashing based searcher
            //BitSamplingImageSearcher bis = new BitSamplingImageSearcher(k, fieldName, fieldName + "_hash", feature, 3000);
            ImageSearchHits hits;
            int count = 0, countCorrect = 0;
            double countTp = 0, countFp = 0, countTn = 0, countFn = 0;      //F1 Metric
            long ms = System.currentTimeMillis();
            while ((line = br.readLine()) != null) {
                try {
                    tag2count.clear();
                    tag2weight.clear();
                    hits = bis.search(ImageIO.read(new File(line)), ir);
                    // set tag weights and counts.
                    for (int l = 0; l < k; l++) {
                        String tag = getTag(hits.doc(l), photosLocation);
                        if (tag2count.get(tag) == null) tag2count.put(tag, 1);
                        else tag2count.put(tag, tag2count.get(tag) + 1);
                        if (weightByRank) {
                            if (tag2weight.get(tag) == null) tag2weight.put(tag, (double) l);
                            else tag2weight.put(tag, (double) l + tag2weight.get(tag));
                        } else {
                            if (tag2weight.get(tag) == null) tag2weight.put(tag, Double.valueOf(hits.score(l)));
                            else tag2weight.put(tag, (double) l + hits.score(l));
                        }
                    }
                    // find class:
                    int maxCount = 0, maxima = 0;
                    String classifiedAs = null;
                    for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                        String tag = tagIterator.next();
                        if (tag2count.get(tag) > maxCount) {
                            maxCount = tag2count.get(tag);
                            maxima = 1;
                            classifiedAs = tag;
                        } else if (tag2count.get(tag) == maxCount) {
                            maxima++;
                        }
                    }
                    // if there are two or more classes with the same number of results, then we take a look at the weights.
                    // else the class is alread given in classifiedAs.
                    if (maxima > 1) {
                        double minWeight = Double.MAX_VALUE;
                        for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                            String tag = tagIterator.next();
                            if (tag2weight.get(tag) < minWeight) {
                                minWeight = tag2weight.get(tag);
                                classifiedAs = tag;
                            }
                        }
                    }
//                    if (tag2.equals(tag3)) tag1 = tag2;
                    count++;
                    //SHOW THE CLASSIFICATION
                    //     System.out.println(classifiedAs+";"+line);
                    classesHTML.add(classifiedAs);
                    filesHTML.add(line);
                    //F1 Metric
                    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes")) {
                        countCorrect++;
                        countTp++;
                    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes"))
                        countFp++;

                    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no")) {
                        countCorrect++;
                        countTn++;
                    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no"))
                        countFn++;
                    //if (classifiedAs.equals(classIdentifier)) countCorrect++;
                    //if (classifiedAs.equals(classIdentifier)) countCorrect++;
                    // confusion:
                    confusion[class2id.get(classifiedAs)]++;
//                    System.out.printf("%10s (%4.3f, %10d, %4d)\n", classifiedAs, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                }
            }

            double precisicon = getPrecision(countTp, countFp);
            double recall = getRecall(countTp, countFn);
            double trueNegativeRate = getTrueNegativeRate(countTn, countFp);
            double accuracy = getAccuracy(countTp, countFp, countTn, countFn);
            double fMeasure = getFmeasure(precisicon, recall);
            double falsePositiveRate = getFalsePositiveRate(countFp, countTn);
            double mccMeasure = getMccMeasure(countTp, countFp, countTn, countFn);
//            System.out.println("Results for class " + classIdentifier);

            // System.out.printf("%s;%s;%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d\n",classArray[y],classArray[y],classArray[y],k,weightByRank, classIdentifier, precisicon, recall, trueNegativeRate,accuracy, fMeasure,  count, countCorrect, (System.currentTimeMillis() - ms) / count);
            System.out.println(y + 1 + " of " + classArray.length + " finished. " + (System.currentTimeMillis() - ms) / 1000 + " seconds per round. " + "Feature: " + classArray[y] + " Current y " + y);

            print_line.printf("%s;%s;%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", classArray[y], classArray[y], classArray[y], k, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            print_line.flush();

//        System.out.printf("Confusion\t");
//            for (int i = 0; i < classes.length; i++) {
//                System.out.printf("%s\t", classes[i]);
//            }
//            System.out.println();
//        for (int i = 0; i < classes.length; i++) {
            //           System.out.printf("%d\t", confusion[i]);
//        }
//        System.out.println();

            if (createHTML == true) {
                //Create HTML

                String fileName = "classifieresults-" + System.currentTimeMillis() / 1000 + ".html";
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
                bw.write("<html>\n" +
                        "<head><title>Classification Results</title></head>\n" +
                        "<body bgcolor=\"#FFFFFF\">\n");
                bw.write("<table>");

                // int elems = Math.min(filesHTML.size(),50);
                int elems = filesHTML.size();

                for (int i = 0; i < elems; i++) {
                    if (i % 3 == 0) bw.write("<tr>");

                    String s = filesHTML.get(i);
                    String colorF = "rgb(0, 255, 0)";

                    if (classesHTML.get(i).equals("no"))
                        colorF = "rgb(255, 0, 0)";
                    //  String s = reader.document(topDocs.scoreDocs[i].doc).get("descriptorImageIdentifier");
                    //  System.out.println(reader.document(topDocs.scoreDocs[i].doc).get("featLumLay"));
                    //  s = new File(s).getAbsolutePath();
                    // System.out.println(s);
                    bw.write("<td><a href=\"" + s + "\"><img style=\"max-width:220px;border:medium solid " + colorF + ";\"src=\"" + s + "\" border=\"" + 5 + "\" style=\"border: 3px\n" +
                            "black solid;\"></a></td>\n");
                    if (i % 3 == 2) bw.write("</tr>");
                }
                if (elems % 3 != 0) {
                    if (elems % 3 == 2) {
                        bw.write("<td>-</td with exit code 0\nd>\n");
                        bw.write("<td>-</td>\n");
                    } else if (elems % 3 == 2) {
                        bw.write("<td>-</td>\n");
                    }
                    bw.write("</tr>");
                }

                bw.write("</table></body>\n" +
                        "</html>");
                bw.close();
            }
            //   } // kfor
//        }
        }

        print_line.close();


    }

    //classification for two combined features
    public void testClassifyFashionCombinedFeatures() throws IOException {

        PrintWriter print_line = new PrintWriter(new BufferedWriter(new FileWriter("D:\\resultsitemDoubleFeatureK1.txt")));
        String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
        String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};

        print_line.print("Feature1;Feature2;Feature3;K=;Weight Rank=;Class;Precision;Recall;True Negative Rate;Accuracy;False Positive Rate;F-Measure;Count Test Images;Count Correct;ms per test;TP;FP;TN;FN");
        print_line.println();
        print_line.flush();

        ArrayList<String> fields1List = new ArrayList<String>();
        ArrayList<String> fields2List = new ArrayList<String>();
        ArrayList<String> class1List = new ArrayList<String>();
        ArrayList<String> class2List = new ArrayList<String>();


        for (int g = 0; g < fieldsArray.length; g++) {
            //   System.out.println(fieldsArray[g]);
            for (int h = g + 1; h < fieldsArray.length; h++) {
                fields1List.add(fieldsArray[g]);
                fields2List.add(fieldsArray[h]);
                class1List.add(classArray[g]);
                class2List.add(classArray[h]);
            }

        }

        for (int y = 0; y < fields1List.size(); y++) {

            String fS1 = class1List.get(y);
            String fS2 = class2List.get(y);

            String fN1 = "FIELD_NAME_" + fields1List.get(y).toUpperCase();
            String fN2 = "FIELD_NAME_" + fields2List.get(y).toUpperCase();

            boolean weightByRank = true;
            //create HTML file or not
            boolean createHTML = false;
            //binary classes
            String[] classes = {"yes", "no"};
            //number of neighbours
            int k = 1;
            //location of the index and the images, indexname has to be index+classname (indexPHOG)
            String indexLocation = "D:\\Datasets\\FashionTestItemDataSet\\idx\\index";
            String photosLocation = "D:\\Datasets\\FashionTestItemDataSet\\";
            //Testset
            String listFiles = "D:\\Datasets\\FashionTestItemDataSet\\itemtest.txt";

            String f1 = null;
            String f2 = null;
            try {
                f1 = (String) DocumentBuilder.class.getField(fN1).get(null);
                f2 = (String) DocumentBuilder.class.getField(fN2).get(null);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            LireFeature lf1 = null;
            LireFeature lf2 = null;
            try {
                lf1 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + fS1).newInstance();
                lf2 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + fS2).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            String i1 = indexLocation + fS1;
            String i2 = indexLocation + fS2;

            //  for (int ik = 0;ik<k;ik++)       {

            //  System.out.println("Tests for lf1 " + f1 + " with k=" + k + " combined with " + f2 + " - weighting by rank sum: " + weightByRank);
            //  System.out.println("========================================");
            HashMap<String, Integer> tag2count = new HashMap<String, Integer>(k);
            HashMap<String, Double> tag2weight = new HashMap<String, Double>(k);
            int c = 0;   // used for just one class ...
            //        for (int c = 0; c < 10; c++) {
            String classIdentifier = classes[c];

            //"D:\\Datasets\\FashionTest\\fashion10000Test\\" + classIdentifier + ".txt";

            // INIT
            ArrayList<String> classesHTML = new ArrayList<String>();
            ArrayList<String> filesHTML = new ArrayList<String>();

            // int[] confusion = new int[2];
            // Arrays.fill(confusion, 0);
            HashMap<String, Integer> class2id = new HashMap<String, Integer>(2);
            for (int i = 0; i < classes.length; i++)
                class2id.put(classes[i], i);

            BufferedReader br = new BufferedReader(new FileReader(listFiles));
            String line;

            IndexReader ir1 = DirectoryReader.open(MMapDirectory.open(new File(i1)));
            IndexReader ir2 = DirectoryReader.open(MMapDirectory.open(new File(i2)));
            // in-memory linear search
            ImageSearcher bis1 = new GenericFastImageSearcher(k, lf1.getClass(), f1, true, ir1);
            ImageSearcher bis2 = new GenericFastImageSearcher(k, lf2.getClass(), f2, true, ir2);
            // hashing based searcher
            //BitSamplingImageSearcher bis1 = new BitSamplingImageSearcher(k, f1, f1 + "_hash", lf1, 3000);
            ImageSearchHits hits1;
            ImageSearchHits hits2;

            int count = 0, countCorrect = 0;
            double countTp = 0, countFp = 0, countTn = 0, countFn = 0;      //F1 Metric
            long ms = System.currentTimeMillis();
            while ((line = br.readLine()) != null) {
                try {
                    tag2count.clear();
                    tag2weight.clear();
                    hits1 = bis1.search(ImageIO.read(new File(line)), ir1);
                    hits2 = bis2.search(ImageIO.read(new File(line)), ir2);
                    //Print the tag of both searches
                    //System.out.println(getTag(hits1.doc(0)) + "\n" + getTag(hits2.doc(0)));

                    // set tag weights and counts.
                    for (int l = 0; l < k; l++) {
                        String tag1 = getTag(hits1.doc(l), photosLocation);
                        String tag2 = getTag(hits2.doc(l), photosLocation);

                        //  if (tag2count.get(tag1) == null) tag2count.put(tag1, 1);
                        //  else tag2count.put(tag1, tag2count.get(tag1) + 1);

                        //Simple combination
                        if (tag2count.get(tag1) == null && tag2count.get(tag2) == null) {
                            tag2count.put(tag1, 1);
                            tag2count.put(tag2, 1);
                        } else if (tag2count.get(tag1) != null && tag2count.get(tag2) != null) {
                            tag2count.put(tag1, tag2count.get(tag1) + 1);
                            tag2count.put(tag2, tag2count.get(tag2) + 1);
                        } else if (tag2count.get(tag1) != null && tag2count.get(tag2) == null) {
                            tag2count.put(tag1, tag2count.get(tag1) + 1);
                            tag2count.put(tag2, 1);
                        } else if (tag2count.get(tag1) == null && tag2count.get(tag2) != null) {
                            tag2count.put(tag1, 1);
                            tag2count.put(tag2, tag2count.get(tag2) + 1);
                        }


                        if (weightByRank) {
                            //only if rank weight used
                            if (tag2weight.get(tag1) == null && tag2weight.get(tag2) == null) {
                                tag2weight.put(tag1, (double) l);
                                tag2weight.put(tag2, (double) l);

                            } else if (tag2weight.get(tag1) != null && tag2weight.get(tag2) != null) {
                                tag2weight.put(tag1, (double) l + tag2weight.get(tag1));
                                tag2weight.put(tag2, (double) l + tag2weight.get(tag2));
                            } else if (tag2weight.get(tag1) != null && tag2weight.get(tag2) == null) {
                                tag2weight.put(tag1, (double) l + tag2weight.get(tag1));
                                tag2weight.put(tag2, (double) l);
                            } else if (tag2count.get(tag1) == null && tag2count.get(tag2) != null) {
                                tag2weight.put(tag1, (double) l);
                                tag2weight.put(tag2, (double) l + tag2weight.get(tag2));
                            }
                            //  else {
                            //      tag2weight.put(tag1, (double) l + tag2weight.get(tag1));
                            //}
                        } else {
                            // System.out.println(hits1.score(l));
                            //  System.out.println(hits2.score(l));
                            if (tag2weight.get(tag1) == null) {
                                if (Double.valueOf(hits1.score(l)) > Double.valueOf(hits2.score(l)))
                                    tag2weight.put(tag1, Double.valueOf(hits1.score(l)));
                                else
                                    tag2weight.put(tag2, Double.valueOf(hits2.score(l)));
                            } else {

                                if (Double.valueOf(hits1.score(l)) > Double.valueOf(hits2.score(l)))
                                    tag2weight.put(tag1, (double) l + hits1.score(l));
                                else
                                    tag2weight.put(tag2, (double) l + hits2.score(l));

                            }
                        }
                    }
                    // find class, iterate over the tags (classes):
                    int maxCount = 0, maxima = 0;
                    String classifiedAs = null;
                    for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                        String tag = tagIterator.next();
                        if (tag2count.get(tag) > maxCount) {
                            maxCount = tag2count.get(tag);
                            maxima = 1;
                            classifiedAs = tag;
                        } else if (tag2count.get(tag) == maxCount) {
                            maxima++;
                        }
                    }
                    // if there are two or more classes with the same number of results, then we take a look at the weights.
                    // else the class is alread given in classifiedAs.
                    if (maxima > 1) {
                        double minWeight = Double.MAX_VALUE;
                        for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                            String tag = tagIterator.next();
                            if (tag2weight.get(tag) < minWeight) {
                                minWeight = tag2weight.get(tag);
                                classifiedAs = tag;
                            }
                        }
                    }
//                    if (tag2.equals(tag3)) tag1 = tag2;
                    count++;
                    //SHOW THE CLASSIFICATION
                    //     System.out.println(classifiedAs+";"+line);
                    classesHTML.add(classifiedAs);
                    filesHTML.add(line);

                    //F1 Metric calculation
                    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes")) {
                        countCorrect++;
                        countTp++;
                    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes"))
                        countFp++;

                    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no")) {
                        countCorrect++;
                        countTn++;
                    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no"))
                        countFn++;
                    //if (classifiedAs.equals(getTagLine(line)))countCorrect++;
                    //if (classifiedAs.equals(classIdentifier)) countCorrect++;
                    //if (classifiedAs.equals(classIdentifier)) countCorrect++;
                    // confusion:
                    //confusion[class2id.get(classifiedAs)]++;
//                    System.out.printf("%10s (%4.3f, %10d, %4d)\n", classifiedAs, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);
                } catch (Exception e) {
                    System.err.println(">>> ERR:" + e.getMessage() + e);
                    //   throw (NullPointerException) e;
                }
            }

            //get all the evaluation values
            double precisicon = getPrecision(countTp, countFp);
            double recall = getRecall(countTp, countFn);
            double trueNegativeRate = getTrueNegativeRate(countTn, countFp);
            double accuracy = getAccuracy(countTp, countFp, countTn, countFn);
            double fMeasure = getFmeasure(precisicon, recall);
            double falsePositiveRate = getFalsePositiveRate(countFp, countTn);
//            System.out.println("Results for class " + classIdentifier);
//        System.out.printf("Class\tPrecision\tRecall\tTrue Negative Rate\tAccuracy\tF-Measure\tCount Test Images\tCount Corret\tms per test\n");
//        System.out.printf("%s\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%10d\t%10d\t%4d\n", classIdentifier, precisicon, recall, trueNegativeRate,accuracy, fMeasure,  count, countCorrect, (System.currentTimeMillis() - ms) / count);


            System.out.println(y + 1 + " of " + class1List.size() + " finished. " + (System.currentTimeMillis() - ms) / 1000 + " seconds per round. " + "Feature: " + class1List.get(y) + " " + class2List.get(y) + " Current y: " + y);

            print_line.printf("%s;%s;%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", class1List.get(y), class2List.get(y), "no", k, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            print_line.flush();

//        System.out.printf("Confusion\t");
//            for (int i = 0; i < classes.length; i++) {
//                System.out.printf("%s\t", classes[i]);
//            }
//            System.out.println();
//        for (int i = 0; i < classes.length; i++) {
            //           System.out.printf("%d\t", confusion[i]);
//        }
            //  System.out.println();

            //Create HTML

            if (createHTML == true) {

                String fileName = "classifieresults-" + System.currentTimeMillis() / 1000 + ".html";
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
                bw.write("<html>\n" +
                        "<head><title>Classification Results</title></head>\n" +
                        "<body bgcolor=\"#FFFFFF\">\n");
                bw.write("<table>");

                // int elems = Math.min(filesHTML.size(),50);
                int elems = filesHTML.size();

                for (int i = 0; i < elems; i++) {
                    if (i % 3 == 0) bw.write("<tr>");

                    String s = filesHTML.get(i);
                    String colorF = "rgb(0, 255, 0)";

                    if (classesHTML.get(i).equals("no"))
                        colorF = "rgb(255, 0, 0)";
                    //  String s = reader.document(topDocs.scoreDocs[i].doc).get("descriptorImageIdentifier");
                    //  System.out.println(reader.document(topDocs.scoreDocs[i].doc).get("featLumLay"));
                    //  s = new File(s).getAbsolutePath();
                    // System.out.println(s);
                    bw.write("<td><a href=\"" + s + "\"><img style=\"max-width:220px;border:medium solid " + colorF + ";\"src=\"" + s + "\" border=\"" + 5 + "\" style=\"border: 3px\n" +
                            "black solid;\"></a></td>\n");
                    if (i % 3 == 2) bw.write("</tr>");
                }
                if (elems % 3 != 0) {
                    if (elems % 3 == 2) {
                        bw.write("<td>-</td with exit code 0\nd>\n");
                        bw.write("<td>-</td>\n");
                    } else if (elems % 3 == 2) {
                        bw.write("<td>-</td>\n");
                    }
                    bw.write("</tr>");
                }

                bw.write("</table></body>\n" +
                        "</html>");
                bw.close();
            }
            //   } // kfor
//        }
        }

        print_line.close();

    }

    //classification for three combined features
    public void testClassifyFashionThreeCombinedFeatures() throws IOException {

        PrintWriter print_line = new PrintWriter(new BufferedWriter(new FileWriter("D:\\resultsTripleFeature.txt")));

        String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
        String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};

        print_line.print("Feature1;Feature2;Feature3;K=;Weight Rank=;Class;Precision;Recall;True Negative Rate;Accuracy;False Positive Rate;F-Measure;Count Test Images;Count Correct;ms per test;TP;FP;TN;FN");
        print_line.println();
        print_line.flush();

        ArrayList<String> fields1List = new ArrayList<String>();
        ArrayList<String> fields2List = new ArrayList<String>();
        ArrayList<String> fields3List = new ArrayList<String>();
        ArrayList<String> class1List = new ArrayList<String>();
        ArrayList<String> class2List = new ArrayList<String>();
        ArrayList<String> class3List = new ArrayList<String>();


        for (int g = 0; g < fieldsArray.length; g++) {
            //   System.out.println(fieldsArray[g]);
            for (int h = g + 1; h < fieldsArray.length; h++) {
                for (int f = h + 1; f < fieldsArray.length; f++) {

                    fields1List.add(fieldsArray[g]);
                    fields2List.add(fieldsArray[h]);
                    fields3List.add(fieldsArray[f]);
                    class1List.add(classArray[g]);
                    class2List.add(classArray[h]);
                    class3List.add(classArray[f]);
                }
            }

        }

        for (int y = 0; y < fields1List.size(); y++) {

            String fS1 = class1List.get(y);
            String fS2 = class2List.get(y);
            String fS3 = class3List.get(y);

            String fN1 = "FIELD_NAME_" + fields1List.get(y).toUpperCase();
            String fN2 = "FIELD_NAME_" + fields2List.get(y).toUpperCase();
            String fN3 = "FIELD_NAME_" + fields3List.get(y).toUpperCase();

            boolean weightByRank = true;
            boolean createHTML = false;
            String[] classes = {"yes", "no"};
            int k = 50;
            String indexLocation = "D:\\Datasets\\FashionTestItemDataSet\\idx\\index";
            String photosLocation = "D:\\Datasets\\FashionTestItemDataSet\\";
            //Testset
            String testFiles = "D:\\Datasets\\FashionTestFashionDataSet\\test.txt";

            String f1 = null;
            String f2 = null;
            String f3 = null;
            try {
                f1 = (String) DocumentBuilder.class.getField(fN1).get(null);
                f2 = (String) DocumentBuilder.class.getField(fN2).get(null);
                f3 = (String) DocumentBuilder.class.getField(fN3).get(null);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            LireFeature feature = null;
            LireFeature featureSecond = null;
            LireFeature featureThird = null;
            try {
                feature = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + fS1).newInstance();
                featureSecond = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + fS2).newInstance();
                featureThird = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + fS3).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            String i1 = indexLocation + fS1;
            String i2 = indexLocation + fS2;
            String i3 = indexLocation + fS3;

            //boolean weightByRank = true;
            //String[] classes = {"yes", "no"};
            //int k = 70;
            // CONFIG
            //String f1 = DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM;
            //String f2 = DocumentBuilder.FIELD_NAME_CEDD;
            //String f3 = DocumentBuilder.FIELD_NAME_PHOG;
            //LireFeature feature = new EdgeHistogram();
            //LireFeature featureSecond = new CEDD();
            //LireFeature featureThird = new PHOG();
            //String i1 = "D:\\Datasets\\FashionTestFashionDataSet\\idx\\indexEdgeHistogram";
            //String i2 = "D:\\Datasets\\FashionTestFashionDataSet\\idx\\indexCEDD";
            //String i3 = "D:\\Datasets\\FashionTestFashionDataSet\\idx\\indexPHOG";

            //  for (int ik = 0;ik<k;ik++)       {

            //System.out.println("Tests for feature " + f1 + " with k=" + k + " combined with " + f2 + " - weighting by rank sum: " + weightByRank);
            //System.out.println("========================================");
            HashMap<String, Integer> tag2count = new HashMap<String, Integer>(k);
            HashMap<String, Double> tag2weight = new HashMap<String, Double>(k);
            int c = 0;   // used for just one class ...
            //        for (int c = 0; c < 10; c++) {
            String classIdentifier = classes[c];

            //"D:\\Datasets\\FashionTest\\fashion10000Test\\" + classIdentifier + ".txt";

            // INIT
            ArrayList<String> classesHTML = new ArrayList<String>();
            ArrayList<String> filesHTML = new ArrayList<String>();


            int[] confusion = new int[2];
            Arrays.fill(confusion, 0);
            HashMap<String, Integer> class2id = new HashMap<String, Integer>(2);
            for (int i = 0; i < classes.length; i++)
                class2id.put(classes[i], i);

            BufferedReader br = new BufferedReader(new FileReader(testFiles));
            String line;

            IndexReader ir1 = DirectoryReader.open(MMapDirectory.open(new File(i1)));
            IndexReader ir2 = DirectoryReader.open(MMapDirectory.open(new File(i2)));
            IndexReader ir3 = DirectoryReader.open(MMapDirectory.open(new File(i3)));
            // in-memory linear search
            ImageSearcher bis1 = new GenericFastImageSearcher(k, feature.getClass(), f1, true, ir1);
            ImageSearcher bis2 = new GenericFastImageSearcher(k, featureSecond.getClass(), f2, true, ir2);
            ImageSearcher bis3 = new GenericFastImageSearcher(k, featureThird.getClass(), f3, true, ir3);
            // hashing based searcher
            //BitSamplingImageSearcher bis1 = new BitSamplingImageSearcher(k, f1, f1 + "_hash", feature, 3000);
            ImageSearchHits hits1;
            ImageSearchHits hits2;
            ImageSearchHits hits3;

            int count = 0, countCorrect = 0;
            double countTp = 0, countFp = 0, countTn = 0, countFn = 0;      //F1 Metric
            long ms = System.currentTimeMillis();
            while ((line = br.readLine()) != null) {
                try {
                    tag2count.clear();
                    tag2weight.clear();
                    hits1 = bis1.search(ImageIO.read(new File(line)), ir1);
                    hits2 = bis2.search(ImageIO.read(new File(line)), ir2);
                    hits3 = bis3.search(ImageIO.read(new File(line)), ir3);
                    //Print the tag of both searches
                    //System.out.println(getTag(hits1.doc(0)) + "\n" + getTag(hits2.doc(0)));

                    // set tag weights and counts.
                    for (int l = 0; l < k; l++) {
                        String tag1 = getTag(hits1.doc(l), photosLocation);
                        String tag2 = getTag(hits2.doc(l), photosLocation);
                        String tag3 = getTag(hits3.doc(l), photosLocation);

                        //  if (tag2count.get(tag1) == null) tag2count.put(tag1, 1);
                        //  else tag2count.put(tag1, tag2count.get(tag1) + 1);

                        //Simple combination
                        if (tag2count.get(tag1) == null && tag2count.get(tag2) == null && tag2count.get(tag3) == null) {
                            tag2count.put(tag1, 1);
                            tag2count.put(tag2, 1);
                            tag2count.put(tag3, 1);
                        } else if (tag2count.get(tag1) != null && tag2count.get(tag2) != null && tag2count.get(tag3) != null) {
                            tag2count.put(tag1, tag2count.get(tag1) + 1);
                            tag2count.put(tag2, tag2count.get(tag2) + 1);
                            tag2count.put(tag3, tag2count.get(tag3) + 1);
                        } else if (tag2count.get(tag1) != null && tag2count.get(tag2) != null && tag2count.get(tag3) == null) {
                            tag2count.put(tag1, tag2count.get(tag1) + 1);
                            tag2count.put(tag2, tag2count.get(tag2) + 1);
                            tag2count.put(tag3, 1);
                        } else if (tag2count.get(tag1) != null && tag2count.get(tag2) == null && tag2count.get(tag3) != null) {
                            tag2count.put(tag1, tag2count.get(tag1) + 1);
                            tag2count.put(tag2, 1);
                            tag2count.put(tag3, tag2count.get(tag3) + 1);
                        } else if (tag2count.get(tag1) == null && tag2count.get(tag2) != null && tag2count.get(tag3) != null) {
                            tag2count.put(tag1, 1);
                            tag2count.put(tag2, tag2count.get(tag2) + 1);
                            tag2count.put(tag3, tag2count.get(tag3) + 1);
                        } else if (tag2count.get(tag1) == null && tag2count.get(tag2) == null && tag2count.get(tag3) != null) {
                            tag2count.put(tag1, 1);
                            tag2count.put(tag2, 1);
                            tag2count.put(tag3, tag2count.get(tag3) + 1);
                        } else if (tag2count.get(tag1) == null && tag2count.get(tag2) != null && tag2count.get(tag3) == null) {
                            tag2count.put(tag1, 1);
                            tag2count.put(tag2, tag2count.get(tag2) + 1);
                            tag2count.put(tag3, 1);
                        } else if (tag2count.get(tag1) != null && tag2count.get(tag2) == null && tag2count.get(tag3) == null) {
                            tag2count.put(tag1, tag2count.get(tag1) + 1);
                            tag2count.put(tag2, 1);
                            tag2count.put(tag3, 1);
                        }


                        if (weightByRank) {
                            //only if rank weight used
                            if (tag2weight.get(tag1) == null && tag2weight.get(tag2) == null && tag2weight.get(tag3) == null) {
                                tag2weight.put(tag1, (double) l);
                                tag2weight.put(tag2, (double) l);
                                tag2weight.put(tag3, (double) l);
                            } else if (tag2weight.get(tag1) != null && tag2weight.get(tag2) != null && tag2weight.get(tag3) != null) {
                                tag2weight.put(tag1, (double) l + tag2weight.get(tag1));
                                tag2weight.put(tag2, (double) l + tag2weight.get(tag2));
                                tag2weight.put(tag3, (double) l + tag2weight.get(tag3));
                            } else if (tag2weight.get(tag1) != null && tag2weight.get(tag2) != null && tag2weight.get(tag3) == null) {
                                tag2weight.put(tag1, (double) l + tag2weight.get(tag1));
                                tag2weight.put(tag2, (double) l + tag2weight.get(tag2));
                                tag2weight.put(tag3, (double) l);
                            } else if (tag2count.get(tag1) != null && tag2count.get(tag2) == null && tag2weight.get(tag3) != null) {
                                tag2weight.put(tag1, (double) l + tag2weight.get(tag1));
                                tag2weight.put(tag2, (double) l);
                                tag2weight.put(tag3, (double) l + tag2weight.get(tag3));
                            } else if (tag2count.get(tag1) == null && tag2count.get(tag2) != null && tag2weight.get(tag3) != null) {
                                tag2weight.put(tag1, (double) l);
                                tag2weight.put(tag2, (double) l + tag2weight.get(tag2));
                                tag2weight.put(tag3, (double) l + tag2weight.get(tag3));
                            }
                            //  else {
                            //      tag2weight.put(tag1, (double) l + tag2weight.get(tag1));
                            //}
                        } else {
                            // System.out.println(hits1.score(l));
                            //  System.out.println(hits2.score(l));
                            if (tag2weight.get(tag1) == null) {
                                if (Double.valueOf(hits1.score(l)) > Double.valueOf(hits2.score(l)) && Double.valueOf(hits1.score(l)) > Double.valueOf(hits3.score(l)))
                                    tag2weight.put(tag1, Double.valueOf(hits1.score(l)));
                                else if (Double.valueOf(hits2.score(l)) > Double.valueOf(hits1.score(l)) && Double.valueOf(hits2.score(l)) > Double.valueOf(hits3.score(l)))
                                    tag2weight.put(tag2, Double.valueOf(hits2.score(l)));
                                else if (Double.valueOf(hits3.score(l)) > Double.valueOf(hits1.score(l)) && Double.valueOf(hits3.score(l)) > Double.valueOf(hits2.score(l)))
                                    tag2weight.put(tag3, Double.valueOf(hits3.score(l)));
                            } else {

                                if (Double.valueOf(hits1.score(l)) > Double.valueOf(hits2.score(l)) && Double.valueOf(hits1.score(l)) > Double.valueOf(hits3.score(l)))
                                    tag2weight.put(tag1, (double) l + hits1.score(l));
                                else if (Double.valueOf(hits2.score(l)) > Double.valueOf(hits1.score(l)) && Double.valueOf(hits2.score(l)) > Double.valueOf(hits3.score(l)))
                                    tag2weight.put(tag2, (double) l + hits2.score(l));
                                else if (Double.valueOf(hits3.score(l)) > Double.valueOf(hits1.score(l)) && Double.valueOf(hits3.score(l)) > Double.valueOf(hits2.score(l)))
                                    tag2weight.put(tag3, Double.valueOf(hits3.score(l)));

                            }
                        }
                    }
                    // find class, iterate over the tags (classes):
                    int maxCount = 0, maxima = 0;
                    String classifiedAs = null;
                    for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                        String tag = tagIterator.next();
                        if (tag2count.get(tag) > maxCount) {
                            maxCount = tag2count.get(tag);
                            maxima = 1;
                            classifiedAs = tag;
                        } else if (tag2count.get(tag) == maxCount) {
                            maxima++;
                        }
                    }
                    // if there are two or more classes with the same number of results, then we take a look at the weights.
                    // else the class is alread given in classifiedAs.
                    if (maxima > 1) {
                        double minWeight = Double.MAX_VALUE;
                        for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                            String tag = tagIterator.next();
                            if (tag2weight.get(tag) < minWeight) {
                                minWeight = tag2weight.get(tag);
                                classifiedAs = tag;
                            }
                        }
                    }
//                    if (tag2.equals(tag3)) tag1 = tag2;
                    count++;
                    //SHOW THE CLASSIFICATION
                    //     System.out.println(classifiedAs+";"+line);
                    classesHTML.add(classifiedAs);
                    filesHTML.add(line);

                    //F1 Metric
                    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes")) {
                        countCorrect++;
                        countTp++;
                    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes"))
                        countFp++;

                    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no")) {
                        countCorrect++;
                        countTn++;
                    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no"))
                        countFn++;
                    //if (classifiedAs.equals(getTagLine(line)))countCorrect++;
                    //if (classifiedAs.equals(classIdentifier)) countCorrect++;
                    //if (classifiedAs.equals(classIdentifier)) countCorrect++;
                    // confusion:
                    confusion[class2id.get(classifiedAs)]++;
//                    System.out.printf("%10s (%4.3f, %10d, %4d)\n", classifiedAs, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);
                } catch (Exception e) {
                    System.err.println(">>> ERR:" + e.getMessage() + e);
                    //   throw (NullPointerException) e;
                }
            }

            double precisicon = getPrecision(countTp, countFp);
            double recall = getRecall(countTp, countFn);
            double trueNegativeRate = getTrueNegativeRate(countTn, countFp);
            double accuracy = getAccuracy(countTp, countFp, countTn, countFn);
            double fMeasure = getFmeasure(precisicon, recall);
            double falsePositiveRate = getFalsePositiveRate(countFp, countTn);
//            System.out.println("Results for class " + classIdentifier);
            // System.out.printf("Class\tPrecision\tRecall\tTrue Negative Rate\tAccuracy\tF-Measure\tCount Test Images\tCount Corret\tms per test\n");
            // System.out.printf("%s\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%10d\t%10d\t%4d\n", classIdentifier, precisicon, recall, trueNegativeRate,accuracy, fMeasure,  count, countCorrect, (System.currentTimeMillis() - ms) / count);

            System.out.println(y + 1 + " of " + class1List.size() + " finished. " + (System.currentTimeMillis() - ms) / 1000 + " seconds per round. " + "Feature: " + class1List.get(y) + " " + class2List.get(y) + " " + class3List.get(y) + " Current y: " + y);

            print_line.printf("%s;%s;%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", class1List.get(y), class2List.get(y), class3List.get(y), k, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            print_line.flush();

//        System.out.printf("Confusion\t");
//            for (int i = 0; i < classes.length; i++) {
//                System.out.printf("%s\t", classes[i]);
//            }
//            System.out.println();
//        for (int i = 0; i < classes.length; i++) {
            //           System.out.printf("%d\t", confusion[i]);
//        }
            //   System.out.println();

            //Create HTML
            if (createHTML == true) {

                String fileName = "classifieresults-" + System.currentTimeMillis() / 1000 + ".html";
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
                bw.write("<html>\n" +
                        "<head><title>Classification Results</title></head>\n" +
                        "<body bgcolor=\"#FFFFFF\">\n");
                bw.write("<table>");

                // int elems = Math.min(filesHTML.size(),50);
                int elems = filesHTML.size();

                for (int i = 0; i < elems; i++) {
                    if (i % 3 == 0) bw.write("<tr>");

                    String s = filesHTML.get(i);
                    String colorF = "rgb(0, 255, 0)";

                    if (classesHTML.get(i).equals("no"))
                        colorF = "rgb(255, 0, 0)";
                    //  String s = reader.document(topDocs.scoreDocs[i].doc).get("descriptorImageIdentifier");
                    //  System.out.println(reader.document(topDocs.scoreDocs[i].doc).get("featLumLay"));
                    //  s = new File(s).getAbsolutePath();
                    // System.out.println(s);
                    bw.write("<td><a href=\"" + s + "\"><img style=\"max-width:220px;border:medium solid " + colorF + ";\"src=\"" + s + "\" border=\"" + 5 + "\" style=\"border: 3px\n" +
                            "black solid;\"></a></td>\n");
                    if (i % 3 == 2) bw.write("</tr>");
                }
                if (elems % 3 != 0) {
                    if (elems % 3 == 2) {
                        bw.write("<td>-</td with exit code 0\nd>\n");
                        bw.write("<td>-</td>\n");
                    } else if (elems % 3 == 2) {
                        bw.write("<td>-</td>\n");
                    }
                    bw.write("</tr>");
                }

                bw.write("</table></body>\n" +
                        "</html>");
                bw.close();
            }
            //   } // kfor
//        }
        }
        print_line.close();
    }

    //classification for three combined features using theading
    public static boolean testClassifyFashionThreeCombinedFeaturesMulti(int start, int end, String storeToFile) throws IOException {

        PrintWriter print_line = new PrintWriter(new BufferedWriter(new FileWriter(storeToFile)));

        String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
        String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};

        print_line.print("Feature1;Feature2;Feature3;K=;Weight Rank=;Class;Precision;Recall;True Negative Rate;Accuracy;False Positive Rate;F-Measure;Count Test Images;Count Correct;ms per test;TP;FP;TN;FN");
        print_line.println();
        print_line.flush();

        ArrayList<String> fields1List = new ArrayList<String>();
        ArrayList<String> fields2List = new ArrayList<String>();
        ArrayList<String> fields3List = new ArrayList<String>();
        ArrayList<String> class1List = new ArrayList<String>();
        ArrayList<String> class2List = new ArrayList<String>();
        ArrayList<String> class3List = new ArrayList<String>();


        for (int g = 0; g < fieldsArray.length; g++) {
            //   System.out.println(fieldsArray[g]);
            for (int h = g + 1; h < fieldsArray.length; h++) {
                for (int f = h + 1; f < fieldsArray.length; f++) {

                    fields1List.add(fieldsArray[g]);
                    fields2List.add(fieldsArray[h]);
                    fields3List.add(fieldsArray[f]);
                    class1List.add(classArray[g]);
                    class2List.add(classArray[h]);
                    class3List.add(classArray[f]);
                }
            }

        }

        for (int y = start; y < end; y++) {

            String fS1 = class1List.get(y);
            String fS2 = class2List.get(y);
            String fS3 = class3List.get(y);

            String fN1 = "FIELD_NAME_" + fields1List.get(y).toUpperCase();
            String fN2 = "FIELD_NAME_" + fields2List.get(y).toUpperCase();
            String fN3 = "FIELD_NAME_" + fields3List.get(y).toUpperCase();

            boolean weightByRank = true;
            boolean createHTML = false;
            String[] classes = {"yes", "no"};
            int k = 3;
            String indexLocation = "D:\\Datasets\\FashionTestItemDataSet\\idx\\index";
            String photosLocation = "D:\\Datasets\\FashionTestItemDataSet\\";
            //Testset
            String listFiles = "D:\\Datasets\\FashionTestItemDataSet\\itemtest.txt";
            // CONFIG

            String f1 = null;
            String f2 = null;
            String f3 = null;
            try {
                f1 = (String) DocumentBuilder.class.getField(fN1).get(null);
                f2 = (String) DocumentBuilder.class.getField(fN2).get(null);
                f3 = (String) DocumentBuilder.class.getField(fN3).get(null);
            } catch (NoSuchFieldException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            LireFeature lf1 = null;
            LireFeature lf2 = null;
            LireFeature lf3 = null;
            try {
                lf1 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + fS1).newInstance();
                lf2 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + fS2).newInstance();
                lf3 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + fS3).newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (ClassNotFoundException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            String i1 = indexLocation + fS1;
            String i2 = indexLocation + fS2;
            String i3 = indexLocation + fS3;

            //boolean weightByRank = true;
            //String[] classes = {"yes", "no"};
            //int k = 70;
            // CONFIG
            //String f1 = DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM;
            //String f2 = DocumentBuilder.FIELD_NAME_CEDD;
            //String f3 = DocumentBuilder.FIELD_NAME_PHOG;
            //LireFeature lf1 = new EdgeHistogram();
            //LireFeature lf2 = new CEDD();
            //LireFeature lf3 = new PHOG();
            //String i1 = "D:\\Datasets\\FashionTestFashionDataSet\\idx\\indexEdgeHistogram";
            //String i2 = "D:\\Datasets\\FashionTestFashionDataSet\\idx\\indexCEDD";
            //String i3 = "D:\\Datasets\\FashionTestFashionDataSet\\idx\\indexPHOG";

            //  for (int ik = 0;ik<k;ik++)       {

            //System.out.println("Tests for lf1 " + f1 + " with k=" + k + " combined with " + f2 + " - weighting by rank sum: " + weightByRank);
            //System.out.println("========================================");
            HashMap<String, Integer> tag2count = new HashMap<String, Integer>(k);
            HashMap<String, Double> tag2weight = new HashMap<String, Double>(k);
            int c = 0;   // used for just one class ...
            //        for (int c = 0; c < 10; c++) {
            String classIdentifier = classes[c];

            //"D:\\Datasets\\FashionTest\\fashion10000Test\\" + classIdentifier + ".txt";

            // INIT
            ArrayList<String> classesHTML = new ArrayList<String>();
            ArrayList<String> filesHTML = new ArrayList<String>();


            int[] confusion = new int[2];
            Arrays.fill(confusion, 0);
            HashMap<String, Integer> class2id = new HashMap<String, Integer>(2);
            for (int i = 0; i < classes.length; i++)
                class2id.put(classes[i], i);

            BufferedReader br = new BufferedReader(new FileReader(listFiles));
            String line;

            IndexReader ir1 = DirectoryReader.open(MMapDirectory.open(new File(i1)));
            IndexReader ir2 = DirectoryReader.open(MMapDirectory.open(new File(i2)));
            IndexReader ir3 = DirectoryReader.open(MMapDirectory.open(new File(i3)));
            // in-memory linear search
            ImageSearcher bis1 = new GenericFastImageSearcher(k, lf1.getClass(), f1, true, ir1);
            ImageSearcher bis2 = new GenericFastImageSearcher(k, lf2.getClass(), f2, true, ir2);
            ImageSearcher bis3 = new GenericFastImageSearcher(k, lf3.getClass(), f3, true, ir3);
            // hashing based searcher
            //BitSamplingImageSearcher bis1 = new BitSamplingImageSearcher(k, f1, f1 + "_hash", lf1, 3000);
            ImageSearchHits hits1;
            ImageSearchHits hits2;
            ImageSearchHits hits3;

            int count = 0, countCorrect = 0;
            double countTp = 0, countFp = 0, countTn = 0, countFn = 0;      //F1 Metric
            long ms = System.currentTimeMillis();
            while ((line = br.readLine()) != null) {
                try {
                    tag2count.clear();
                    tag2weight.clear();
                    tag2count.put("yes", 1);
                    tag2count.put("no", 1);
                    tag2weight.put("yes", 1.0);
                    tag2weight.put("no", 1.0);

                    hits1 = bis1.search(ImageIO.read(new File(line)), ir1);
                    hits2 = bis2.search(ImageIO.read(new File(line)), ir2);
                    hits3 = bis3.search(ImageIO.read(new File(line)), ir3);
                    //Print the tag of both searches
                    //System.out.println(getTag(hits1.doc(0)) + "\n" + getTag(hits2.doc(0)));

                    // set tag weights and counts.
                    for (int l = 0; l < k; l++) {
                        String tag = getTag(hits1.doc(l), photosLocation);
                        String tagSecond = getTag(hits2.doc(l), photosLocation);
                        String tagThird = getTag(hits3.doc(l), photosLocation);

                        //Simple combination

                        tag2count.put(tag, tag2count.get(tag) + 1);
                        tag2count.put(tagSecond, tag2count.get(tagSecond) + 1);
                        tag2count.put(tagThird, tag2count.get(tagThird) + 1);

                        if (weightByRank) {
                            //only if rank weight used
                            if (tag2weight.get(tag) == null && tag2weight.get(tagSecond) == null && tag2weight.get(tagThird) == null) {
                                tag2weight.put(tag, (double) l);
                                tag2weight.put(tagSecond, (double) l);
                                tag2weight.put(tagThird, (double) l);
                            } else if (tag2weight.get(tag) != null && tag2weight.get(tagSecond) != null && tag2weight.get(tagThird) != null) {
                                tag2weight.put(tag, (double) l + tag2weight.get(tag));
                                tag2weight.put(tagSecond, (double) l + tag2weight.get(tagSecond));
                                tag2weight.put(tagThird, (double) l + tag2weight.get(tagThird));
                            } else if (tag2weight.get(tag) != null && tag2weight.get(tagSecond) != null && tag2weight.get(tagThird) == null) {
                                tag2weight.put(tag, (double) l + tag2weight.get(tag));
                                tag2weight.put(tagSecond, (double) l + tag2weight.get(tagSecond));
                                tag2weight.put(tagThird, (double) l);
                            } else if (tag2count.get(tag) != null && tag2count.get(tagSecond) == null && tag2weight.get(tagThird) != null) {
                                tag2weight.put(tag, (double) l + tag2weight.get(tag));
                                tag2weight.put(tagSecond, (double) l);
                                tag2weight.put(tagThird, (double) l + tag2weight.get(tagThird));
                            } else if (tag2count.get(tag) == null && tag2count.get(tagSecond) != null && tag2weight.get(tagThird) != null) {
                                tag2weight.put(tag, (double) l);
                                tag2weight.put(tagSecond, (double) l + tag2weight.get(tagSecond));
                                tag2weight.put(tagThird, (double) l + tag2weight.get(tagThird));
                            }
                            //  else {
                            //      tag2weight.put(tag, (double) l + tag2weight.get(tag));
                            //}
                        } else {
                            // System.out.println(hits1.score(l));
                            //  System.out.println(hits2.score(l));
                            if (tag2weight.get(tag) == null) {
                                if (Double.valueOf(hits1.score(l)) > Double.valueOf(hits2.score(l)) && Double.valueOf(hits1.score(l)) > Double.valueOf(hits3.score(l)))
                                    tag2weight.put(tag, Double.valueOf(hits1.score(l)));
                                else if (Double.valueOf(hits2.score(l)) > Double.valueOf(hits1.score(l)) && Double.valueOf(hits2.score(l)) > Double.valueOf(hits3.score(l)))
                                    tag2weight.put(tagSecond, Double.valueOf(hits2.score(l)));
                                else if (Double.valueOf(hits3.score(l)) > Double.valueOf(hits1.score(l)) && Double.valueOf(hits3.score(l)) > Double.valueOf(hits2.score(l)))
                                    tag2weight.put(tagThird, Double.valueOf(hits3.score(l)));
                            } else {

                                if (Double.valueOf(hits1.score(l)) > Double.valueOf(hits2.score(l)) && Double.valueOf(hits1.score(l)) > Double.valueOf(hits3.score(l)))
                                    tag2weight.put(tag, (double) l + hits1.score(l));
                                else if (Double.valueOf(hits2.score(l)) > Double.valueOf(hits1.score(l)) && Double.valueOf(hits2.score(l)) > Double.valueOf(hits3.score(l)))
                                    tag2weight.put(tagSecond, (double) l + hits2.score(l));
                                else if (Double.valueOf(hits3.score(l)) > Double.valueOf(hits1.score(l)) && Double.valueOf(hits3.score(l)) > Double.valueOf(hits2.score(l)))
                                    tag2weight.put(tagThird, Double.valueOf(hits3.score(l)));

                            }
                        }
                    }
                    // find class, iterate over the tags (classes):
                    int maxCount = 0, maxima = 0;
                    String classifiedAs = null;
                    for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                        String tag = tagIterator.next();
                        if (tag2count.get(tag) > maxCount) {
                            maxCount = tag2count.get(tag);
                            maxima = 1;
                            classifiedAs = tag;
                        } else if (tag2count.get(tag) == maxCount) {
                            maxima++;
                        }
                    }
                    // if there are two or more classes with the same number of results, then we take a look at the weights.
                    // else the class is alread given in classifiedAs.
                    if (maxima > 1) {
                        double minWeight = Double.MAX_VALUE;
                        for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                            String tag = tagIterator.next();
                            if (tag2weight.get(tag) < minWeight) {
                                minWeight = tag2weight.get(tag);
                                classifiedAs = tag;
                            }
                        }
                    }
//                    if (tag2.equals(tag3)) tag1 = tag2;
                    count++;
                    //SHOW THE CLASSIFICATION
                    //     System.out.println(classifiedAs+";"+line);
                    classesHTML.add(classifiedAs);
                    filesHTML.add(line);

                    //F1 Metric
                    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes")) {
                        countCorrect++;
                        countTp++;
                    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes"))
                        countFp++;

                    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no")) {
                        countCorrect++;
                        countTn++;
                    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no"))
                        countFn++;
                    //if (classifiedAs.equals(getTagLine(line)))countCorrect++;
                    //if (classifiedAs.equals(classIdentifier)) countCorrect++;
                    //if (classifiedAs.equals(classIdentifier)) countCorrect++;
                    // confusion:
                    confusion[class2id.get(classifiedAs)]++;
//                    System.out.printf("%10s (%4.3f, %10d, %4d)\n", classifiedAs, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);
                } catch (Exception e) {
                    System.err.println(">>> ERR:" + e.getMessage() + e);
                    //   throw (NullPointerException) e;
                }
            }

            double precisicon = getPrecision(countTp, countFp);
            double recall = getRecall(countTp, countFn);
            double trueNegativeRate = getTrueNegativeRate(countTn, countFp);
            double accuracy = getAccuracy(countTp, countFp, countTn, countFn);
            double fMeasure = getFmeasure(precisicon, recall);
            double falsePositiveRate = getFalsePositiveRate(countFp, countTn);
//            System.out.println("Results for class " + classIdentifier);
            // System.out.printf("Class\tPrecision\tRecall\tTrue Negative Rate\tAccuracy\tF-Measure\tCount Test Images\tCount Corret\tms per test\n");
            // System.out.printf("%s\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%10d\t%10d\t%4d\n", classIdentifier, precisicon, recall, trueNegativeRate,accuracy, fMeasure,  count, countCorrect, (System.currentTimeMillis() - ms) / count);

            System.out.println(y + 1 + " of " + class1List.size() + " finished. " + (System.currentTimeMillis() - ms) / 1000 + " seconds per round. " + "Feature: " + class1List.get(y) + " " + class2List.get(y) + " " + class3List.get(y) + " Current y: " + y);

            print_line.printf("%s;%s;%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", class1List.get(y), class2List.get(y), class3List.get(y), k, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            print_line.flush();

//        System.out.printf("Confusion\t");
//            for (int i = 0; i < classes.length; i++) {
//                System.out.printf("%s\t", classes[i]);
//            }
//            System.out.println();
//        for (int i = 0; i < classes.length; i++) {
            //           System.out.printf("%d\t", confusion[i]);
//        }
            //   System.out.println();

            //Create HTML
            if (createHTML == true) {

                String fileName = "classifieresults-" + System.currentTimeMillis() / 1000 + ".html";
                BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
                bw.write("<html>\n" +
                        "<head><title>Classification Results</title></head>\n" +
                        "<body bgcolor=\"#FFFFFF\">\n");
                bw.write("<table>");

                // int elems = Math.min(filesHTML.size(),50);
                int elems = filesHTML.size();

                for (int i = 0; i < elems; i++) {
                    if (i % 3 == 0) bw.write("<tr>");

                    String s = filesHTML.get(i);
                    String colorF = "rgb(0, 255, 0)";

                    if (classesHTML.get(i).equals("no"))
                        colorF = "rgb(255, 0, 0)";
                    //  String s = reader.document(topDocs.scoreDocs[i].doc).get("descriptorImageIdentifier");
                    //  System.out.println(reader.document(topDocs.scoreDocs[i].doc).get("featLumLay"));
                    //  s = new File(s).getAbsolutePath();
                    // System.out.println(s);
                    bw.write("<td><a href=\"" + s + "\"><img style=\"max-width:220px;border:medium solid " + colorF + ";\"src=\"" + s + "\" border=\"" + 5 + "\" style=\"border: 3px\n" +
                            "black solid;\"></a></td>\n");
                    if (i % 3 == 2) bw.write("</tr>");
                }
                if (elems % 3 != 0) {
                    if (elems % 3 == 2) {
                        bw.write("<td>-</td with exit code 0\nd>\n");
                        bw.write("<td>-</td>\n");
                    } else if (elems % 3 == 2) {
                        bw.write("<td>-</td>\n");
                    }
                    bw.write("</tr>");
                }

                bw.write("</table></body>\n" +
                        "</html>");
                bw.close();
            }
            //   } // kfor
//        }
        }
        print_line.close();
        return true;
    }

    //classification for all combined features
    public void testClassifyFashionAllCombinedFeatures() throws IOException {

        PrintWriter print_line = new PrintWriter(new BufferedWriter(new FileWriter("D:\\resultsallFeatureK31.txt")));

        String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
        String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};

        print_line.print("Feature1;Feature2;Feature3;K=;Weight Rank=;Class;Precision;Recall;True Negative Rate;Accuracy;False Positive Rate;F-Measure;Count Test Images;Count Correct;ms per test;TP;FP;TN;FN");
        print_line.println();
        print_line.flush();


        boolean weightByRank = true;
        boolean createHTML = false;
        String[] classes = {"yes", "no"};
        int k = 3;
        String indexLocation = "D:\\Datasets\\FashionTestItemDataSet\\idx\\index";
        String photosLocation = "D:\\Datasets\\FashionTestItemDataSet\\";
        //Testset
        String listFiles = "D:\\Datasets\\FashionTestItemDataSet\\itemtest.txt";
        //  String indexLocation = "D:\\Datasets\\FashionTestFashionDataSet\\idx\\index";
        // CONFIG


        String f1 = null;
        String f2 = null;
        String f3 = null;
        String f4 = null;
        String f5 = null;
        String f6 = null;
        String f7 = null;
        String f8 = null;
        String f9 = null;
        String f10 = null;
        String f11 = null;
        String f12 = null;

        try {
            f1 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[0].toUpperCase()).get(null);
            f2 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[1].toUpperCase()).get(null);
            f3 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[2].toUpperCase()).get(null);
            f4 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[3].toUpperCase()).get(null);
            f5 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[4].toUpperCase()).get(null);
            f6 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[5].toUpperCase()).get(null);
            f7 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[6].toUpperCase()).get(null);
            f8 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[7].toUpperCase()).get(null);
            f9 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[8].toUpperCase()).get(null);
            f10 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[9].toUpperCase()).get(null);
            f11 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[10].toUpperCase()).get(null);
            f12 = (String) DocumentBuilder.class.getField("FIELD_NAME_" + fieldsArray[11].toUpperCase()).get(null);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        LireFeature lf1 = null;
        LireFeature lf2 = null;
        LireFeature lf3 = null;
        LireFeature lf4 = null;
        LireFeature lf5 = null;
        LireFeature lf6 = null;
        LireFeature lf7 = null;
        LireFeature lf8 = null;
        LireFeature lf9 = null;
        LireFeature lf10 = null;
        LireFeature lf11 = null;
        LireFeature lf12 = null;
        try {
            lf1 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[0]).newInstance();
            lf2 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[1]).newInstance();
            lf3 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[2]).newInstance();
            lf4 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[3]).newInstance();
            lf5 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[4]).newInstance();
            lf6 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[5]).newInstance();
            lf7 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[6]).newInstance();
            lf8 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[7]).newInstance();
            lf9 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[8]).newInstance();
            lf10 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[9]).newInstance();
            lf11 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[10]).newInstance();
            lf12 = (LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + classArray[11]).newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (ClassNotFoundException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


        String i1 = indexLocation + classArray[0];
        String i2 = indexLocation + classArray[1];
        String i3 = indexLocation + classArray[2];
        String i4 = indexLocation + classArray[3];
        String i5 = indexLocation + classArray[4];
        String i6 = indexLocation + classArray[5];
        String i7 = indexLocation + classArray[6];
        String i8 = indexLocation + classArray[7];
        String i9 = indexLocation + classArray[8];
        String i10 = indexLocation + classArray[9];
        String i11 = indexLocation + classArray[10];
        String i12 = indexLocation + classArray[11];


        //  for (int ik = 0;ik<k;ik++)       {

        //System.out.println("Tests for feature " + fieldName + " with k=" + k + " combined with " + fieldNameSecond + " - weighting by rank sum: " + weightByRank);
        //System.out.println("========================================");
        HashMap<String, Integer> tag2count = new HashMap<String, Integer>(k);
        HashMap<String, Double> tag2weight = new HashMap<String, Double>(k);

        //   tag2count.put("yes",1);
        //   tag2count.put("no",1);

        int c = 0;   // used for just one class ...
        //        for (int c = 0; c < 10; c++) {
        String classIdentifier = classes[c];

        //"D:\\Datasets\\FashionTest\\fashion10000Test\\" + classIdentifier + ".txt";

        // INIT
        ArrayList<String> classesHTML = new ArrayList<String>();
        ArrayList<String> filesHTML = new ArrayList<String>();


        //   int[] confusion = new int[2];
        //   Arrays.fill(confusion, 0);
        HashMap<String, Integer> class2id = new HashMap<String, Integer>(2);
        for (int i = 0; i < classes.length; i++)
            class2id.put(classes[i], i);

        BufferedReader br = new BufferedReader(new FileReader(listFiles));
        String line;

        IndexReader ir1 = DirectoryReader.open(MMapDirectory.open(new File(i1)));
        IndexReader ir2 = DirectoryReader.open(MMapDirectory.open(new File(i2)));
        IndexReader ir3 = DirectoryReader.open(MMapDirectory.open(new File(i3)));
        IndexReader ir4 = DirectoryReader.open(MMapDirectory.open(new File(i4)));
        IndexReader ir5 = DirectoryReader.open(MMapDirectory.open(new File(i5)));
        IndexReader ir6 = DirectoryReader.open(MMapDirectory.open(new File(i6)));
        IndexReader ir7 = DirectoryReader.open(MMapDirectory.open(new File(i7)));
        IndexReader ir8 = DirectoryReader.open(MMapDirectory.open(new File(i8)));
        IndexReader ir9 = DirectoryReader.open(MMapDirectory.open(new File(i9)));
        IndexReader ir10 = DirectoryReader.open(MMapDirectory.open(new File(i10)));
        IndexReader ir11 = DirectoryReader.open(MMapDirectory.open(new File(i11)));
        IndexReader ir12 = DirectoryReader.open(MMapDirectory.open(new File(i12)));

        // in-memory linear search
        ImageSearcher bis1 = new GenericFastImageSearcher(k, lf1.getClass(), f1, true, ir1);
        ImageSearcher bis2 = new GenericFastImageSearcher(k, lf2.getClass(), f2, true, ir2);
        ImageSearcher bis3 = new GenericFastImageSearcher(k, lf3.getClass(), f3, true, ir3);
        ImageSearcher bis4 = new GenericFastImageSearcher(k, lf4.getClass(), f4, true, ir4);
        ImageSearcher bis5 = new GenericFastImageSearcher(k, lf5.getClass(), f5, true, ir5);
        ImageSearcher bis6 = new GenericFastImageSearcher(k, lf6.getClass(), f6, true, ir6);
        ImageSearcher bis7 = new GenericFastImageSearcher(k, lf7.getClass(), f7, true, ir7);
        ImageSearcher bis8 = new GenericFastImageSearcher(k, lf8.getClass(), f8, true, ir8);
        ImageSearcher bis9 = new GenericFastImageSearcher(k, lf9.getClass(), f9, true, ir9);
        ImageSearcher bis10 = new GenericFastImageSearcher(k, lf10.getClass(), f10, true, ir10);
        ImageSearcher bis11 = new GenericFastImageSearcher(k, lf11.getClass(), f11, true, ir11);
        ImageSearcher bis12 = new GenericFastImageSearcher(k, lf12.getClass(), f12, true, ir12);
        // hashing based searcher
        //BitSamplingImageSearcher bis = new BitSamplingImageSearcher(k, fieldName, fieldName + "_hash", feature, 3000);

        ImageSearchHits hits1;
        ImageSearchHits hits2;
        ImageSearchHits hits3;
        ImageSearchHits hits4;
        ImageSearchHits hits5;
        ImageSearchHits hits6;
        ImageSearchHits hits7;
        ImageSearchHits hits8;
        ImageSearchHits hits9;
        ImageSearchHits hits10;
        ImageSearchHits hits11;
        ImageSearchHits hits12;

        int count = 0, countCorrect = 0;
        double countTp = 0, countFp = 0, countTn = 0, countFn = 0;      //F1 Metric
        long ms = System.currentTimeMillis();
        while ((line = br.readLine()) != null) {

            System.out.println(line);
            System.out.println(count);

            try {
                tag2count.clear();
                tag2count.put("yes", 1);
                tag2count.put("no", 1);
                tag2weight.clear();
                tag2weight.put("yes", 1.0);
                tag2weight.put("no", 1.0);


                hits1 = bis1.search(ImageIO.read(new File(line)), ir1);
                hits2 = bis2.search(ImageIO.read(new File(line)), ir2);
                hits3 = bis3.search(ImageIO.read(new File(line)), ir3);
                hits4 = bis4.search(ImageIO.read(new File(line)), ir4);
                hits5 = bis5.search(ImageIO.read(new File(line)), ir5);
                hits6 = bis6.search(ImageIO.read(new File(line)), ir6);
                hits7 = bis7.search(ImageIO.read(new File(line)), ir7);
                hits8 = bis8.search(ImageIO.read(new File(line)), ir8);
                hits9 = bis9.search(ImageIO.read(new File(line)), ir9);
                hits10 = bis10.search(ImageIO.read(new File(line)), ir10);
                hits11 = bis11.search(ImageIO.read(new File(line)), ir11);
                hits12 = bis12.search(ImageIO.read(new File(line)), ir12);
                //Print the tag of both searches
                //System.out.println(getTag(hits.doc(0)) + "\n" + getTag(hitsSecond.doc(0)));

                // set tag weights and counts.
                for (int l = 0; l < k; l++) {
                    String tag1 = getTag(hits1.doc(l), photosLocation);
                    String tag2 = getTag(hits2.doc(l), photosLocation);
                    String tag3 = getTag(hits3.doc(l), photosLocation);
                    String tag4 = getTag(hits4.doc(l), photosLocation);
                    String tag5 = getTag(hits5.doc(l), photosLocation);
                    String tag6 = getTag(hits6.doc(l), photosLocation);
                    String tag7 = getTag(hits7.doc(l), photosLocation);
                    String tag8 = getTag(hits8.doc(l), photosLocation);
                    String tag9 = getTag(hits9.doc(l), photosLocation);
                    String tag10 = getTag(hits10.doc(l), photosLocation);
                    String tag11 = getTag(hits11.doc(l), photosLocation);
                    String tag12 = getTag(hits12.doc(l), photosLocation);

                    //  System.out.println(tag1);


                    //  if (tag2count.get(tag) == null) tag2count.put(tag, 1);
                    //  else tag2count.put(tag, tag2count.get(tag) + 1);

                    //Simple combination

                    tag2count.put(tag1, tag2count.get(tag1) + 1);
                    tag2count.put(tag2, tag2count.get(tag2) + 1);
                    tag2count.put(tag3, tag2count.get(tag3) + 1);
                    tag2count.put(tag4, tag2count.get(tag4) + 1);
                    tag2count.put(tag5, tag2count.get(tag5) + 1);
                    tag2count.put(tag6, tag2count.get(tag6) + 1);
                    tag2count.put(tag7, tag2count.get(tag7) + 1);
                    tag2count.put(tag8, tag2count.get(tag8) + 1);
                    tag2count.put(tag9, tag2count.get(tag9) + 1);
                    tag2count.put(tag10, tag2count.get(tag10) + 1);
                    tag2count.put(tag11, tag2count.get(tag11) + 1);
                    tag2count.put(tag12, tag2count.get(tag12) + 1);


                    if (weightByRank) {
                        //only if rank weight used
                        tag2weight.put(tag1, (double) l);
                        tag2weight.put(tag2, (double) l);
                        tag2weight.put(tag3, (double) l);
                        tag2weight.put(tag4, (double) l);
                        tag2weight.put(tag5, (double) l);
                        tag2weight.put(tag6, (double) l);
                        tag2weight.put(tag7, (double) l);
                        tag2weight.put(tag8, (double) l);
                        tag2weight.put(tag9, (double) l);
                        tag2weight.put(tag10, (double) l);
                        tag2weight.put(tag11, (double) l);
                        tag2weight.put(tag12, (double) l);

                    }
                    //  else {
                    //      tag2weight.put(tag, (double) l + tag2weight.get(tag));
                    //}
                    //SCORE
                    //     else {
                    // System.out.println(hits.score(l));
                    //  System.out.println(hitsSecond.score(l));
                    //         if (tag2weight.get(tag) == null) {
                    //           if (Double.valueOf(hits.score(l)) > Double.valueOf(hitsSecond.score(l)) && Double.valueOf(hits.score(l)) > Double.valueOf(hitsThird.score(l)))
                    //              tag2weight.put(tag, Double.valueOf(hits.score(l)));
                    //          else if (Double.valueOf(hitsSecond.score(l)) > Double.valueOf(hits.score(l)) && Double.valueOf(hitsSecond.score(l)) > Double.valueOf(hitsThird.score(l)))
                    //             tag2weight.put(tagSecond, Double.valueOf(hitsSecond.score(l)));
                    //              else if (Double.valueOf(hitsThird.score(l)) > Double.valueOf(hits.score(l)) && Double.valueOf(hitsThird.score(l)) > Double.valueOf(hitsSecond.score(l)))
                    //                   tag2weight.put(tagThird, Double.valueOf(hitsThird.score(l)));
                    //          } else {

                    //             if (Double.valueOf(hits.score(l)) > Double.valueOf(hitsSecond.score(l)) && Double.valueOf(hits.score(l)) > Double.valueOf(hitsThird.score(l)))
                    //                 tag2weight.put(tag, (double) l + hits.score(l));
                    //            else if (Double.valueOf(hitsSecond.score(l)) > Double.valueOf(hits.score(l)) && Double.valueOf(hitsSecond.score(l)) > Double.valueOf(hitsThird.score(l)))
                    //               tag2weight.put(tagSecond, (double) l + hitsSecond.score(l));
                    //          else if (Double.valueOf(hitsThird.score(l)) > Double.valueOf(hits.score(l)) && Double.valueOf(hitsThird.score(l)) > Double.valueOf(hitsSecond.score(l)))
                    //            tag2weight.put(tagThird, Double.valueOf(hitsThird.score(l)));

                    //      }
                    // }
                }
                // find class, iterate over the tags (classes):
                int maxCount = 0, maxima = 0;
                String classifiedAs = null;
                for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                    String tag = tagIterator.next();
                    if (tag2count.get(tag) > maxCount) {
                        maxCount = tag2count.get(tag);
                        maxima = 1;
                        classifiedAs = tag;
                    } else if (tag2count.get(tag) == maxCount) {
                        maxima++;
                    }
                }
                // if there are two or more classes with the same number of results, then we take a look at the weights.
                // else the class is alread given in classifiedAs.
                if (maxima > 1) {
                    double minWeight = Double.MAX_VALUE;
                    for (Iterator<String> tagIterator = tag2count.keySet().iterator(); tagIterator.hasNext(); ) {
                        String tag = tagIterator.next();
                        if (tag2weight.get(tag) < minWeight) {
                            minWeight = tag2weight.get(tag);
                            classifiedAs = tag;
                        }
                    }
                }
//                    if (tag2.equals(tag3)) tag1 = tag2;
                count++;
                //SHOW THE CLASSIFICATION
                //     System.out.println(classifiedAs+";"+line);
                classesHTML.add(classifiedAs);
                filesHTML.add(line);

                //F1 Metric
                if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes")) {
                    countCorrect++;
                    countTp++;
                } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes"))
                    countFp++;

                if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no")) {
                    countCorrect++;
                    countTn++;
                } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no"))
                    countFn++;
                //if (classifiedAs.equals(getTagLine(line)))countCorrect++;
                //if (classifiedAs.equals(classIdentifier)) countCorrect++;
                //if (classifiedAs.equals(classIdentifier)) countCorrect++;
                // confusion:
                //   confusion[class2id.get(classifiedAs)]++;
//                    System.out.printf("%10s (%4.3f, %10d, %4d)\n", classifiedAs, ((double) countCorrect / (double) count), count, (System.currentTimeMillis() - ms) / count);
            } catch (Exception e) {
                System.err.println(">>> ERR:" + e.getMessage() + e);
                // throw (NullPointerException) e;
            }
        }

        double precisicon = getPrecision(countTp, countFp);
        double recall = getRecall(countTp, countFn);
        double trueNegativeRate = getTrueNegativeRate(countTn, countFp);
        double accuracy = getAccuracy(countTp, countFp, countTn, countFn);
        double fMeasure = getFmeasure(precisicon, recall);
        double falsePositiveRate = getFalsePositiveRate(countFp, countTn);
//            System.out.println("Results for class " + classIdentifier);
        // System.out.printf("Class\tPrecision\tRecall\tTrue Negative Rate\tAccuracy\tF-Measure\tCount Test Images\tCount Corret\tms per test\n");
        // System.out.printf("%s\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%4.5f\t%10d\t%10d\t%4d\n", classIdentifier, precisicon, recall, trueNegativeRate,accuracy, fMeasure,  count, countCorrect, (System.currentTimeMillis() - ms) / count);

        //   System.out.println(y + 1 + " of " + class1List.size() + " finished. " + (System.currentTimeMillis() - ms) / 1000 + " seconds per round. " + "Feature: " + class1List.get(y) + " " + class2List.get(y) + " " + class3List.get(y) + " Current y: " + y);

        print_line.printf("%s;%s;%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", "all", "all", "all", k, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
        print_line.flush();

//        System.out.printf("Confusion\t");
//            for (int i = 0; i < classes.length; i++) {
//                System.out.printf("%s\t", classes[i]);
//            }
//            System.out.println();
//        for (int i = 0; i < classes.length; i++) {
        //           System.out.printf("%d\t", confusion[i]);
//        }
        //   System.out.println();

        //Create HTML
        if (createHTML == true) {

            String fileName = "classifieresults-" + System.currentTimeMillis() / 1000 + ".html";
            BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
            bw.write("<html>\n" +
                    "<head><title>Classification Results</title></head>\n" +
                    "<body bgcolor=\"#FFFFFF\">\n");
            bw.write("<table>");

            // int elems = Math.min(filesHTML.size(),50);
            int elems = filesHTML.size();

            for (int i = 0; i < elems; i++) {
                if (i % 3 == 0) bw.write("<tr>");

                String s = filesHTML.get(i);
                String colorF = "rgb(0, 255, 0)";

                if (classesHTML.get(i).equals("no"))
                    colorF = "rgb(255, 0, 0)";
                //  String s = reader.document(topDocs.scoreDocs[i].doc).get("descriptorImageIdentifier");
                //  System.out.println(reader.document(topDocs.scoreDocs[i].doc).get("featLumLay"));
                //  s = new File(s).getAbsolutePath();
                // System.out.println(s);
                bw.write("<td><a href=\"" + s + "\"><img style=\"max-width:220px;border:medium solid " + colorF + ";\"src=\"" + s + "\" border=\"" + 5 + "\" style=\"border: 3px\n" +
                        "black solid;\"></a></td>\n");
                if (i % 3 == 2) bw.write("</tr>");
            }
            if (elems % 3 != 0) {
                if (elems % 3 == 2) {
                    bw.write("<td>-</td with exit code 0\nd>\n");
                    bw.write("<td>-</td>\n");
                } else if (elems % 3 == 2) {
                    bw.write("<td>-</td>\n");
                }
                bw.write("</tr>");
            }

            bw.write("</table></body>\n" +
                    "</html>");
            bw.close();
        }
        //   } // kfor
//        }

        print_line.close();
    }

    //get the tag from a given document
    private static String getTag(Document d, String photosLocation) {
        StringBuilder ab = new StringBuilder(d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0].replace(photosLocation, ""));

        //  System.out.println(ab.substring(0, ab.indexOf("\\")).toString());
        return ab.substring(0, ab.indexOf("\\")).toString();
        //  return ab.toString();
        //return "yes";
    }

    //get the tag from a textfile line
    private static String getTagLine(String line, String photosLocation) {
        line = line.replace(photosLocation, "");
        //  System.out.println(line.substring(0, line.indexOf("\\")).toString());
        return line.substring(0, line.indexOf("\\")).toString();
        //return "yes";
    }

    //Mesures
    private static double getPrecision(double tp, double fp) {
        double precision;
        return precision = tp / (tp + fp);
    }

    private static double getRecall(double tp, double fn) {
        double recall;
        return recall = tp / (tp + fn);
    }

    private static double getTrueNegativeRate(double tn, double fp) {
        double negativeRate;
        return negativeRate = tn / (tn + fp);
    }

    private static double getAccuracy(double tp, double fp, double tn, double fn) {
        double accuracy;
        return accuracy = (tp + tn) / (tp + tn + fp + fn);
    }

    private static double getFalsePositiveRate(double fp, double tn) {
        double falsePositiveRate;
        return falsePositiveRate = fp / (fp + tn);
    }

    private static double getFmeasure(double precision, double recall) {
        double fMeasure;
        return fMeasure = 2 * ((precision * recall) / (precision + recall));
    }

    private static double getMccMeasure(double tp, double fp, double tn, double fn) {
        double mccMeasure;
        return mccMeasure = ((tp * tn) - (fp * fn)) / Math.sqrt((tp + fp) * (tp + fn) * (tn + fp) * (tn + fn));
    }

    private static double getWFM(double tp, double fp, double tn, double fn, double fMeasure, double allN) {
        double wFm;
        double nPrec = tn / (tn + fn);
        double nRec = tn / (tn + fp);
        double nF1 = 2 * (nPrec * nRec) / (nPrec + nRec);

        return wFm = (fMeasure * (tp + fn) + nF1 * (fp + tn)) / allN;
    }

    //Calculates the possible combinations of the selected features
    public static ArrayList print_nCr(final int n, final int r) {
        int[] res = new int[r];
        ArrayList combinations = new ArrayList();
        for (int i = 0; i < res.length; i++) {
            res[i] = i + 1;
        }
        boolean done = false;
        while (!done) {


            // System.out.println(Arrays.toString(res));
            for (int j = 0; j < res.length; j++) {
                combinations.add(res[j]);
            }

            done = getNext(res, n, r);
        }
        return combinations;
    }

    //Part of print_nCr
    public static boolean getNext(final int[] num, final int n, final int r) {
        int target = r - 1;
        num[target]++;
        if (num[target] > ((n - (r - target)) + 1)) {
            // Carry the One
            while (num[target] > ((n - (r - target)))) {
                target--;
                if (target < 0) {
                    break;
                }
            }
            if (target < 0) {
                return true;
            }
            num[target]++;
            for (int i = target + 1; i < num.length; i++) {
                num[i] = num[i - 1] + 1;
            }
        }
        return false;
    }

    // FEATURE SELECTION PART
    //Do the feature selection, returns a double array with the scores
    private static HashMap<String, Double> getFeaturesInformationGainScore(String photosLocation, String locationOfTrainSet, String locationExtracorFile, String[] classArray, int[] featureSpace, double[] featureInformationGain, HashMap<String, Integer> featureSpaceHashMap,HashMap<String, Double> featureInformationGainHashMap) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        //Configurations
        String storeToFile = "wekaTemp.arff";
        // String photosLocation = "D:\\Datasets\\FashionTest\\";
        // String locationOfTrainSet = "D:\\Datasets\\FashionTestFashionDataSet\\train.txt";
        // String locationExtracorFile =  "D:\\Datasets\\FashionTestFashionDataSet\\indexall.data";

        //Name of the features to extract
        //  String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};
        //  int[] featureSpace = {144, 80, 192, 33, 630, 168, 60, 192, 18, 64, 64, 64};
        //  double[] featureInformationGain = {0,0,0,0,0,0,0,0,0,0,0,0};

        PrintWriter print_line = new PrintWriter(new BufferedWriter(new FileWriter(storeToFile)));

        print_line.print("@relation vowel-train" + "\n" + "\n");
        print_line.flush();

        //get the feature order
        InputStream inf = new FileInputStream(locationExtracorFile);
        byte[] tempIntf = new byte[4];
        int tmpf, tmpFeaturef;

        ArrayList<String> featureOrder = new ArrayList<String>();

        byte[] tempf = new byte[100 * 1024];
        while ((tmpf = inf.read(tempIntf, 0, 4)) > 0) {
            tmpf = SerializationUtils.toInt(tempIntf);
            inf.read(tempf, 0, tmpf);
            while (inf.read(tempIntf, 0, 1) > 0) {
                if (tempIntf[0] == -1) break;
                tmpFeaturef = tempIntf[0];
                GlobalFeature f = (GlobalFeature) Class.forName(Extractor.features[tmpFeaturef]).newInstance();
                // byte[] length ...
                inf.read(tempIntf, 0, 4);
                tmpf = SerializationUtils.toInt(tempIntf);
                inf.read(tempf, 0, tmpf);
                f.setByteArrayRepresentation(tempf, 0, tmpf);
                //System.out.println(f.getFeatureVector().length+f.getClass().getSimpleName());
                featureOrder.add(f.getClass().getSimpleName());
            }
            break;
        }


        //   for (int i = 0; i < classArray.length; i++) {
        for (int i = 0; i < featureOrder.size(); i++) {
            // for (int j = 0; j < featureSpace[i]; j++) {
            for (int j = 0; j < featureSpaceHashMap.get(featureOrder.get(i)); j++) {
                print_line.print("@attribute " + i + "_" + featureOrder.get(i) + "_" + j + " " + "numeric" + "\n" + "\n");
            }
            print_line.flush();
        }

        //  print_line.print("@attribute FileName String" + "\n" + "\n");

        print_line.print("@attribute Class {yes,no}" + "\n" + "\n" + "@data" + "\n" + "\n");
        print_line.flush();


        BufferedReader br = new BufferedReader(new FileReader(locationOfTrainSet));
        String line;

        //   while ((line = br.readLine()) != null) {
        //       BufferedImage img = ImageIO.read(new File(line));
        //       String tag = getTagLine(line, photosLocation);

        InputStream in = new FileInputStream(locationExtracorFile);
        byte[] tempInt = new byte[4];
        int tmp, tmpFeature;

        byte[] temp = new byte[100 * 1024];
        while ((tmp = in.read(tempInt, 0, 4)) > 0) {
            tmp = SerializationUtils.toInt(tempInt);
            in.read(temp, 0, tmp);
            String filename = new String(temp, 0, tmp);
            String tag = getTagLine(filename, photosLocation);
            while (in.read(tempInt, 0, 1) > 0) {
                if (tempInt[0] == -1) break;
                tmpFeature = tempInt[0];
                LireFeature f = (LireFeature) Class.forName(Extractor.features[tmpFeature]).newInstance();
                // byte[] length ...
                in.read(tempInt, 0, 4);
                tmp = SerializationUtils.toInt(tempInt);
                in.read(temp, 0, tmp);
                f.setByteArrayRepresentation(temp, 0, tmp);
                //System.out.println(filename + Arrays.toString(f.getFeatureVector()));
                //System.out.println(f.getFeatureVector().length+f.getFieldName());
                double[] tempDouble = f.getFeatureVector();
                double tempMean = 0.0;
                for (int j = 0; j < tempDouble.length; j++) {
                    //      tempMean = tempMean + tempDouble[j];
                    print_line.print(tempDouble[j] + ",");
                }
                //  print_line.print(tempMean/tempDouble.length + ",");

            }
            //  print_line.print(filename+","+tag);
            print_line.print(tag + ",");

            print_line.print("\n");
            print_line.flush();
        }
        //   }

        print_line.close();
        return calculateInformationGain(storeToFile, featureInformationGain, featureSpace, featureSpaceHashMap, featureOrder, featureInformationGainHashMap);
        //  System.out.println(Arrays.toString(featureInformationGain));

    }

    //Does the information gain algorithm and return a list of total information gain scores
    private static HashMap<String, Double> calculateInformationGain(String wekaFileLocation, double[] featureInformationGain, int featureSpace[], HashMap<String, Integer> featureSpaceHashMap, ArrayList<String> featureOrder,HashMap<String, Double> featureInformationGainHashMap) {

        Instances data = null;
        try {
            data = new Instances(new BufferedReader(new FileReader(wekaFileLocation)));
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        AttributeSelection attsel = new AttributeSelection();  // package weka.attributeSelection!
        InfoGainAttributeEval eval = new InfoGainAttributeEval();
        Ranker search = new Ranker();
        search.setThreshold(-1.7976931348623157E308);
        search.setNumToSelect(-1);
        search.setGenerateRanking(true);
        attsel.setEvaluator(eval);
        attsel.setSearch(search);
        try {

            attsel.SelectAttributes(data);
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        // obtain the attribute indices that were selected
        int[] indices = new int[0];
        double[][] rankedAttribuesArray = new double[0][0];
        try {
            rankedAttribuesArray = attsel.rankedAttributes();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        try {
            indices = attsel.selectedAttributes();
        } catch (Exception e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        for (int i = 0; i < rankedAttribuesArray.length; i++) {

            int currentFeature = Integer.parseInt(data.attribute((int) rankedAttribuesArray[i][0]).name().substring(0, data.attribute((int) rankedAttribuesArray[i][0]).name().indexOf("_")));
            //System.out.println("DDDDDDDDDDDDDD"+currentFeature);
            // System.out.print(data.attribute((int) rankedAttribuesArray[i][0]).name() + "/" + rankedAttribuesArray[i][0] + "/");
            //     System.out.println(rankedAttribuesArray[i][1]);
            // data.attribute((int) rankedAttribuesArray[i][0]).name().substring(0,data.attribute((int) rankedAttribuesArray[i][0]).name().indexOf("_"));
            // featureInformationGain[currentFeature] = featureInformationGain[currentFeature] + rankedAttribuesArray[i][1];
            featureInformationGainHashMap.put(featureOrder.get(currentFeature),featureInformationGainHashMap.get(featureOrder.get(currentFeature))+rankedAttribuesArray[i][1]);
        }

        //Caalculate the mean of the information gain (better comparable)
        // for (int i = 0; i < featureInformationGain.length; i++) {
        //     featureInformationGain[i] = (featureInformationGain[i] / featureSpace[i]) * 100;
        // }

        //Calculate the mean of the information gain (better comparable)
        for (int i = 0; i < featureOrder.size(); i++) {
            featureInformationGainHashMap.put(featureOrder.get(i),(featureInformationGainHashMap.get(featureOrder.get(i))/featureSpaceHashMap.get(featureOrder.get(i)))*100);
        }

        // for(int i=0;i<0;i++){
        //     System.out.println(data.attribute(indices[i]).toString());
        // }
        System.out.println("Scoring finished, starting with classification! Scores: ");
        for (int i = 0; i < featureOrder.size(); i++) {
            System.out.println(featureOrder.get(i)+" "+ featureInformationGainHashMap.get(featureOrder.get(i)));
            // featureInformationGainHashMap.put(featureOrder.get(i),(featureInformationGainHashMap.get(featureOrder.get(i))/featureSpaceHashMap.get(featureOrder.get(i)))*100);
        }
        // return featureInformationGain;
        return featureInformationGainHashMap;
    }

    //SOME TEST CLASSES
    //classifie three combinend features main class uses threading
    public void testThreadClassifyThreeFeatures() throws IOException {
        Thread[] all = new Thread[4];
        all[0] = new Thread(new Classifie3Task(0, 56, "D:\\resultsTripleFeatureItemK31.txt"));
        all[1] = new Thread(new Classifie3Task(56, 111, "D:\\resultsTripleFeatureItemK32.txt"));
        all[2] = new Thread(new Classifie3Task(111, 166, "D:\\resultsTripleFeatureItemK33.txt"));
        all[3] = new Thread(new Classifie3Task(166, 220, "D:\\resultsTripleFeatureItemK34.txt"));

        all[0].start();
        all[1].start();
        all[2].start();
        all[3].start();

        try {
            all[0].join();
            all[1].join();
            all[2].join();
            all[3].join();
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }

    //Thread test 3 features
    public static class Classifie3Task implements Runnable {

        int start;
        int end;
        String storeToFile;

        public Classifie3Task(int start, int end, String storeToFile) {
            this.start = start;
            this.end = end;
            this.storeToFile = storeToFile;
        }

        public void run() {
            // for (int i=0; i < end; i++) {
            // do something
            try {
                testClassifyFashionThreeCombinedFeaturesMulti(start, end, storeToFile);
            } catch (IOException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

            //  }
        }
    }

    //Testclass for the NCR function
    public void testNCR() throws IOException {

        int feats = 12;
        int combs = 2;

        ArrayList combinations = print_nCr(feats, combs);

        for (int i = 0; i < combinations.size(); i += combs) {


            for (int j = 0; j < combs; j++) {
                System.out.print(combinations.get(i + j).toString() + " ");
            }
            System.out.println();


        }


/////////


    }




}
