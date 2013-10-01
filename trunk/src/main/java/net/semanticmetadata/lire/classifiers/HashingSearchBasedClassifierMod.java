package net.semanticmetadata.lire.classifiers;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.impl.BitSamplingImageSearcher;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.indexing.tools.Extractor;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;

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

/**
 * Created with IntelliJ IDEA.
 * User: Michael
 * Date: 8/27/13
 * Time: 5:51 PM
 * To change this template use File | Settings | File Templates.
 */
public class HashingSearchBasedClassifierMod {
    ///////////////////////////////////////

    //  package net.semanticmetadata.lire;


    /**
     * Created with IntelliJ IDEA.
     * User: mlux, mriegler
     * Date: 01.08.2013
     * Time: 13:53
     */

    //MAIN CLASS
    //Starts the classification process
    public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException, ClassNotFoundException {

      //  String[] pathes = {"D:\\Datasets\\Fashion10000RunFashion\\", "D:\\Datasets\\Fashion10000RunFashionImproved\\", "D:\\Datasets\\Fashion10000RunItem\\", "D:\\Datasets\\Fashion10000RunItemImproved\\"};
        String[] pathes = {"D:\\Datasets\\Fashion10000RunItemImproved\\"};
        for (int r = 0; r < pathes.length; r++) {
            //config

            double informationGainThreshold = 0.22;    //threshold for information gain
            int numberOfCombinations = 12;      //number of max combinations
            int numberOfNeighbours = 16;        //number of neighbours for search result
            double precisionThreshold = 1.0;

            String pathName = pathes[r];
            boolean useIndexSearch = true; //use a with HashigIndexorMulti class generated index of the test set for search
            String locationSaveResultsFile = pathName + System.currentTimeMillis() + "_IG" + informationGainThreshold + "_NC" + numberOfCombinations+ "_NN" + numberOfNeighbours + "_" + "run.txt"; //where should the outcame be saved
            String locationOfIndex = pathName + "idx\\index";  //location of the lire index
            String testIndexLocation =  "D:\\Datasets\\FashionTestItemDataSet\\" + "idx\\index";
            String testImageLocation = "D:\\Datasets\\FashionTestItemDataSet\\";
           // String locationOfImages = pathName;    //location of the images
            String locationOfImages = pathName;    //location of the images
            String locationOfTrainSet = pathName + "train.txt"; //location of the trainingsset
            String locationExtracorFile = pathName + "indexall.data";    //location of the extractor file
            String locationOfTestset = pathName + "test.txt";          //Testset location


            //All possible classes and fields
            //   String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura"};
            //   String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura"};

         //   String[] fieldsArray = {"CEDD", "EdgeHistogram","ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "ScalableColor"};
         //   String[] classArray = {"CEDD", "EdgeHistogram","ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "ScalableColor"};

            String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
            String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};

            //int[] featureSpace = {144, 80, 192, 33, 630, 168, 60, 192, 18, 64, 64, 64};    //size of feature vector per feature
            //int[] featureSpace = {144, 60, 64, 64, 168, 64, 630, 192, 192, 18, 192, 80};
            int[] featureSpace = new int[classArray.length];
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
                    //System.out.println(f.getDoubleHistogram().length+f.getClass().getSimpleName());
                    for (int z = 0; z < classArray.length; z++) {
                        if (f.getClass().getSimpleName().equals(classArray[z]))
                            featureSpace[z] = f.getDoubleHistogram().length;

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
            //   featureInformationGainHashMap = getFeaturesInformationGainScore(locationOfImages, locationOfTrainSet, locationExtracorFile, classArray, featureSpace, featuresInformationGain, featureSpaceHashMap, featureInformationGainHashMap);

            ArrayList<String> reducedFieldsArray = new ArrayList<String>();
            ArrayList<String> reducedClassArray = new ArrayList<String>();


     /*   for (int i = 0; i < featuresInformationGain.length; i++) {
            if (featuresInformationGain[i] >= informationGainThreshold) {
                reducedFieldsArray.add(fieldsArray[i]);
                reducedClassArray.add(classArray[i]);
            }
        }*/
            double maxInfoGain = 0.0;
            double minInfoGain = 1000.0;
            for (int i = 0; i < classArray.length; i++) {
                if (featureInformationGainHashMap.get(classArray[i])>maxInfoGain)
                    maxInfoGain = featureInformationGainHashMap.get(classArray[i]);
                if (featureInformationGainHashMap.get(classArray[i])<minInfoGain)
                    minInfoGain = featureInformationGainHashMap.get(classArray[i]);
            }

            double infoGainAvg = 0.0;
            for (int i = 0; i < classArray.length; i++) {
                infoGainAvg = infoGainAvg + (featureInformationGainHashMap.get(classArray[i])-minInfoGain)/(maxInfoGain-minInfoGain);
            }
            infoGainAvg = (infoGainAvg/classArray.length)/2;

            for (int i = 0; i < classArray.length; i++) {
                //  if ((featureInformationGainHashMap.get(classArray[i])-minInfoGain)/(maxInfoGain-minInfoGain) >= infoGainAvg) {
                reducedFieldsArray.add(fieldsArray[i]);
                reducedClassArray.add(classArray[i]);
                //    }
            }

            //If the number of combinations is bigger than the max size of features
            if (numberOfCombinations > reducedClassArray.size())
                numberOfCombinations = reducedClassArray.size();

            System.out.println("Features reduced! Starting with classification. Reduced Feature Set: " + reducedClassArray.toString());


            //Starts the classification
            try {
                if (useIndexSearch)
                    testClassifyNCombinedFeaturesMulti(0, 220, locationSaveResultsFile, numberOfNeighbours, locationOfIndex, testIndexLocation, locationOfImages, locationOfTestset, 0, reducedFieldsArray, reducedClassArray, numberOfCombinations, class1, class2, informationGainThreshold, "TestSet", precisionThreshold, classArray,testImageLocation);
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
    }

    //use index for classification, its faster
    public static boolean testClassifyNCombinedFeaturesMulti(int start, int end, String storeToFile, int numberOfNeighbours, String indexLocation, String testIndexLocation,  String photosLocation, String testSetFile, int searchedClass, ArrayList<String> fieldsArray, ArrayList<String> classArray, int combineNfeatures, String class1, String class2, double informationGainThreshold, String useIndex, double precisionThreshold, String[]allClasses, String testImageLocation) throws IOException, NoSuchFieldException, IllegalAccessException, ClassNotFoundException, InstantiationException {

        //numer of features and how much should be combined
        int feats = fieldsArray.size();
        int combs = combineNfeatures;

        PrintWriter print_line = new PrintWriter(new BufferedWriter(new FileWriter(storeToFile)));

        //all the combinations computed and stored here
        ArrayList combinations = print_nCr(feats, combs);

        //  String[] fieldsArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoeffs", "Tamura", "Luminance_Layout", "Opponent_Histogram", "ScalableColor"};
        //  String[] classArray = {"CEDD", "EdgeHistogram", "FCTH", "ColorLayout", "PHOG", "JCD", "Gabor", "JpegCoefficientHistogram", "Tamura", "LuminanceLayout", "OpponentHistogram", "ScalableColor"};

        //get the features for the column names
        String sCombinedFeatures = "";
        for (int i = 0; i < allClasses.length; i++) {
            sCombinedFeatures = sCombinedFeatures + "Feature" + i + 1 + ";";
        }
        print_line.print(sCombinedFeatures + "K=;IGTH;PRTH;Weight Rank=;Class;Precision;Recall;True Negative Rate;Accuracy;False Positive Rate;F-Measure;MCC;WFM;Count Test Images;Count Correct;ms per test;TP;FP;TN;FN");
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

        //Iterate over the whole combinations list combination wise
        for (int i = 0; i < combinations.size(); i += combs) {

            // System.out.println(i);

            ArrayList<String> featureNameList = new ArrayList<String>();
ArrayList<LireFeature> lireFeatureList = new ArrayList<LireFeature>();
            ArrayList indexLocationList = new ArrayList();
            ArrayList testIndexLocationList = new ArrayList();

            ArrayList<IndexReader> indexReaderList = new ArrayList<IndexReader>();
            ArrayList<IndexReader> indexReaderTestList = new ArrayList<IndexReader>();
            ArrayList<BitSamplingImageSearcher> bisSearcherList = new ArrayList<BitSamplingImageSearcher>();
            ArrayList<ImageSearchHits> imageSearchHitsList = new ArrayList<ImageSearchHits>();

            ArrayList<HashMap<String, Integer>> tag2countList = new ArrayList<HashMap<String, Integer>>();
            ArrayList<HashMap<String, Double>> tag2weightList = new ArrayList<HashMap<String, Double>>();

            //iterate over all fields lists and fill it in a array, for each combination bundle
            for (int j = 0; j < combs; j++) {
                //   System.out.print(combinations.get(i + j).toString() + " ");
                featureNameList.add((String) DocumentBuilder.class.getField("FIELD_NAME_" + fields1List.get(i + j).toUpperCase()).get(null));
                lireFeatureList.add((LireFeature) Class.forName("net.semanticmetadata.lire.imageanalysis." + class1List.get(i + j)).newInstance());
                indexLocationList.add(indexLocation + class1List.get(i + j));
                testIndexLocationList.add(testIndexLocation + class1List.get(i + j));
            }

            boolean weightByRank = true;
            boolean createHTML = true;
            //  String[] classes = {"yes", "no"};
            String[] classes = {class1, class2};
            int k = numberOfNeighbours;

            HashMap<String, Integer> tag2count = new HashMap<String, Integer>(k);
            HashMap<String, Double> tag2weight = new HashMap<String, Double>(k);
            int c = 0;   // used for just one class ...
            //        for (int c = 0; c < 10; c++) {
            c = searchedClass;

            String classIdentifier = classes[c];

            // For the html creation
            ArrayList<String> classesHTML = new ArrayList<String>();
            ArrayList<String> filesHTML = new ArrayList<String>();

            //Multiclasses not used
            // int[] confusion = new int[2];
            // Arrays.fill(confusion, 0);
            // HashMap<String, Integer> class2id = new HashMap<String, Integer>(2);
            // for (int d = 0; d < classes.length; d++)
            //     class2id.put(classes[d], d);

            //Create the index reader
            for (int j = 0; j < combs; j++) {
                //   System.out.print(combinations.get(i + j).toString() + " ");
                indexReaderList.add(DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(j)))));
             //   indexReaderTestList.add(DirectoryReader.open(MMapDirectory.open(new File((String) indexLocationList.get(j) + "TestSet"))));
                indexReaderTestList.add(DirectoryReader.open(MMapDirectory.open(new File((String) testIndexLocationList.get(j) + "TestSet"))));
            }

            //Create the bsisearcher
            for (int j = 0; j < combs; j++) {
                bisSearcherList.add(new BitSamplingImageSearcher(k, (String) featureNameList.get(j), (String) featureNameList.get(j) + "_hash", (LireFeature) lireFeatureList.get(j), 1000));
            }


            int count = 0, countCorrect = 0;
            double countTp = 0, countFp = 0, countTn = 0, countFn = 0;      //F1 Metric
            long ms = System.currentTimeMillis();


            for (int x = 0 /*TODO: FIXME */; x < indexReaderTestList.get(0).numDocs(); x++) {

                //       System.out.println(x + " " + indexReaderTestList.get(0).numDocs());

                tag2count.clear();
                tag2weight.clear();
                tag2weightList.clear();
                tag2countList.clear();
                tag2count.put(class1, 1);
                tag2count.put(class2, 1);
                tag2weight.put(class1, 1.0);
                tag2weight.put(class2, 1.0);

                //Create the hits
                for (int j = 0; j < combs; j++) {
                    imageSearchHitsList.add(bisSearcherList.get(j).search(indexReaderTestList.get(j).document(x), indexReaderList.get(j)));
                }

                //The tag count arraylis
                for (int j = 0; j < combs; j++) {
                    tag2countList.add(new HashMap<String, Integer>(k));
                    tag2countList.get(j).put(class1,1);
                    tag2countList.get(j).put(class2,1);
                    tag2weightList.add(new HashMap<String, Double>(k));
                    tag2weightList.get(j).put(class1,1.0);
                    tag2weightList.get(j).put(class2,1.0);
                }


                // set tag weights and counts.
                for (int l = 0; l < k; l++) {

                    //  String tag = getTag(hits1.doc(l), photosLocation);
                    try {

                        for (int j = 0; j < combs; j++) {
                            tag2count.put(getTag(imageSearchHitsList.get(j).doc(l), photosLocation), tag2count.get(getTag(imageSearchHitsList.get(j).doc(l), photosLocation)) + 1);
                            tag2countList.get(j).put(getTag(imageSearchHitsList.get(j).doc(l), photosLocation), tag2countList.get(j).get(getTag(imageSearchHitsList.get(j).doc(l), photosLocation)) + 1);
                        }


                    } catch (IndexOutOfBoundsException e) {

                    }


                    try {
                        if (weightByRank) {

                            for (int j = 0; j < combs; j++) {
                                tag2weight.put(getTag(imageSearchHitsList.get(j).doc(l), photosLocation), tag2weight.get(getTag(imageSearchHitsList.get(j).doc(l), photosLocation)) + 1 / ((double) l + 1));
                                tag2weightList.get(j).put(getTag(imageSearchHitsList.get(j).doc(l), photosLocation), tag2weightList.get(j).get(getTag(imageSearchHitsList.get(j).doc(l), photosLocation)) + 1 / ((double) l + 1));
                            }
                        }
                    } catch (IndexOutOfBoundsException e) {

                    }
                    //  System.out.println(System.currentTimeMillis()-ms);
                    //  ms=System.currentTimeMillis();
                }
                imageSearchHitsList.clear();
                // find class, iterate over the tags (classes):
                int maxCount = 0, maxima = 0;
                int countClass1 = 0, countClass2 = 0;
                double weightClass1 = 0.0, weightClass2 = 0.0;
                String classifiedAs = null;

                for (Iterator<String> tagIterator = tag2countList.get(0).keySet().iterator(); tagIterator.hasNext(); ) {
                    String tag = tagIterator.next();
                    for (int j = 0; j < combs;j++){
                        if(tag.equals(class1))  {
                            countClass1 = countClass1 + tag2countList.get(j).get(tag);
                            weightClass1 = weightClass1 + tag2weightList.get(j).get(tag);
                        }
                        if(tag.equals(class2)){
                            countClass2 = countClass2 + tag2countList.get(j).get(tag);
                            weightClass2 = weightClass2 + tag2weightList.get(j).get(tag);
                        }
                    }
                }

                if ((countClass1*weightClass1)*precisionThreshold>countClass2*weightClass2)
                    classifiedAs = class1;
                else classifiedAs = class2;


                //for binary threshold only
                   /* if((((countClass1-1)-(countClass2-1))/k)>(0+precisionThreshold))
                    classifiedAs = class1;
                    else if((((countClass1-1)-(countClass2-1))/k)<0)
                    classifiedAs=class2;
*/

                // if there are two or more classes with the same number of results, then we take a look at the weights.
                // else the class is alread given in classifiedAs.



                count++;
                //SHOW THE CLASSIFICATION
                //     System.out.println(classifiedAs+";"+line);
                classesHTML.add(classifiedAs);


                filesHTML.add(indexReaderTestList.get(0).document(x).getField("descriptorImageIdentifier").stringValue());

                //F1 Metric
                //     if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes")) {
                if (classifiedAs.equals(getTag(indexReaderTestList.get(0).document(x), testImageLocation)) && classifiedAs.equals(class1)) {
                    countCorrect++;
                    countTp++;
                    //    } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("yes"))
                } else if (!classifiedAs.equals(getTag(indexReaderTestList.get(0).document(x), testImageLocation)) && classifiedAs.equals(class1))
                    countFp++;

                //    if (classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no")) {
                if (classifiedAs.equals(getTag(indexReaderTestList.get(0).document(x), testImageLocation)) && classifiedAs.equals(class2)) {
                    countCorrect++;
                    countTn++;
                    //     } else if (!classifiedAs.equals(getTagLine(line, photosLocation)) && classifiedAs.equals("no"))
                } else if (!classifiedAs.equals(getTag(indexReaderTestList.get(0).document(x), testImageLocation)) && classifiedAs.equals(class2))
                    countFn++;

                print_line.println(classifiedAs);
            }
            print_line.flush();

            double precisicon = getPrecision(countTp, countFp);
            double recall = getRecall(countTp, countFn);
            double trueNegativeRate = getTrueNegativeRate(countTn, countFp);
            double accuracy = getAccuracy(countTp, countFp, countTn, countFn);
            double fMeasure = getFmeasure(precisicon, recall);
            double falsePositiveRate = getFalsePositiveRate(countFp, countTn);
            double mccMeasure = getMccMeasure(countTp, countFp, countTn, countFn);
            double wFM = getWFM(countTp, countFp, countTn, countFn, fMeasure, count);

            String classesLongName = "";

            for (int j = 0; j < combs; j++) {
                //   System.out.print(combinations.get(i + j).toString() + " ");
                classesLongName = classesLongName + fields1List.get(i+j) + ";";
            }

            for (int j = 0; j < (allClasses.length-combs); j++){
                classesLongName = classesLongName + "no;";
            }

            //   print_line.printf("%s,%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", classesLongName, k, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            System.out.printf("%s%s;%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", classesLongName, k, informationGainThreshold, precisionThreshold, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, mccMeasure, wFM, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
            print_line.printf("%s%s;%s;%s;%s;%s;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%4.5f;%10d;%10d;%4d;%4.5f;%4.5f;%4.5f;%4.5f\n", classesLongName, k, informationGainThreshold, precisionThreshold, weightByRank, classIdentifier, precisicon, recall, trueNegativeRate, accuracy, falsePositiveRate, fMeasure, mccMeasure, wFM, count, countCorrect, (System.currentTimeMillis() - ms) / count, countTp, countFp, countTn, countFn);
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
    private static HashMap<String, Double> getFeaturesInformationGainScore(String photosLocation, String locationOfTrainSet, String locationExtracorFile, String[] classArray, int[] featureSpace, double[] featureInformationGain, HashMap<String, Integer> featureSpaceHashMap, HashMap<String, Double> featureInformationGainHashMap) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {

        //Configurations
        String storeToFile = System.currentTimeMillis() + "wekaTemp.arff";
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
                LireFeature f = (LireFeature) Class.forName(Extractor.features[tmpFeaturef]).newInstance();
                // byte[] length ...
                inf.read(tempIntf, 0, 4);
                tmpf = SerializationUtils.toInt(tempIntf);
                inf.read(tempf, 0, tmpf);
                f.setByteArrayRepresentation(tempf, 0, tmpf);
                //System.out.println(f.getDoubleHistogram().length+f.getClass().getSimpleName());
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
                //System.out.println(filename + Arrays.toString(f.getDoubleHistogram()));
                //System.out.println(f.getDoubleHistogram().length+f.getFieldName());
                double[] tempDouble = f.getDoubleHistogram();
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
    private static HashMap<String, Double> calculateInformationGain(String wekaFileLocation, double[] featureInformationGain, int featureSpace[], HashMap<String, Integer> featureSpaceHashMap, ArrayList<String> featureOrder, HashMap<String, Double> featureInformationGainHashMap) {

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
            featureInformationGainHashMap.put(featureOrder.get(currentFeature), featureInformationGainHashMap.get(featureOrder.get(currentFeature)) + rankedAttribuesArray[i][1]);
        }

        //Caalculate the mean of the information gain (better comparable)
        // for (int i = 0; i < featureInformationGain.length; i++) {
        //     featureInformationGain[i] = (featureInformationGain[i] / featureSpace[i]) * 100;
        // }

        //Calculate the mean of the information gain (better comparable)
        for (int i = 0; i < featureOrder.size(); i++) {
            //  featureInformationGainHashMap.put(featureOrder.get(i), (featureInformationGainHashMap.get(featureOrder.get(i)) / featureSpaceHashMap.get(featureOrder.get(i))) * 100);
            featureInformationGainHashMap.put(featureOrder.get(i), (featureInformationGainHashMap.get(featureOrder.get(i))));
        }

        // for(int i=0;i<0;i++){
        //     System.out.println(data.attribute(indices[i]).toString());
        // }
        System.out.println("Scoring finished, starting with classification! Scores: ");
        for (int i = 0; i < featureOrder.size(); i++) {
            System.out.println(featureOrder.get(i) + " " + featureInformationGainHashMap.get(featureOrder.get(i)));
            // featureInformationGainHashMap.put(featureOrder.get(i),(featureInformationGainHashMap.get(featureOrder.get(i))/featureSpaceHashMap.get(featureOrder.get(i)))*100);
        }
        // return featureInformationGain;
        File deleteFile = new File(wekaFileLocation);
        deleteFile.delete();
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

    ///////////////////////////////////////
}
