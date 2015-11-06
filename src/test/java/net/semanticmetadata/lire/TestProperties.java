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

package net.semanticmetadata.lire;

import junit.framework.TestCase;
import net.semanticmetadata.lire.aggregators.AbstractAggregator;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.Extractor;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.indexers.parallel.ExtractorItem;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * Created by Nektarios on 28/5/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class TestProperties extends TestCase {

    private Class<? extends AbstractAggregator> aggregator;

    private boolean sample = false, docsCreated = false;
    private int[] numOfClusters;
    private int numOfDocsForCodebooks = 500;

    private HashSet<ExtractorItem> GlobalExtractors = new HashSet<ExtractorItem>(10); // default size (16)
    private HashMap<ExtractorItem, LinkedList<Cluster[]>> LocalExtractorsAndCodebooks = new HashMap<ExtractorItem, LinkedList<Cluster[]>>(10); // default size (16)
    private HashMap<ExtractorItem, LinkedList<Cluster[]>> SimpleExtractorsAndCodebooks = new HashMap<ExtractorItem, LinkedList<Cluster[]>>(10); // default size (16)

    public void testWriteLoadProperties(){
        writePropertiesFileTest();
        loadPropertiesFile("testxml.xml");
        testPrintSetUp();
    }

    public void writePropertiesFileTest(){
        try{
            Properties props = new Properties();

            props.setProperty("0", "info");
            props.setProperty("0.info.0", "net.semanticmetadata.lire.aggregators.BOVW");
            props.setProperty("0.info.1", "32");
            props.setProperty("0.info.2", "128");

            props.setProperty("1", "global");
            props.setProperty("1.extractor", "net.semanticmetadata.lire.imageanalysis.features.global.CEDD");

            props.setProperty("2", "global");
            props.setProperty("2.extractor", "net.semanticmetadata.lire.imageanalysis.features.global.FCTH");

            props.setProperty("3", "local");
            props.setProperty("3.extractor", "net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor");
            props.setProperty("3.codebook.1", "./src/test/resources/codebooks/CvSURF32");
            props.setProperty("3.codebook.2", "./src/test/resources/codebooks/CvSURF128");


            props.setProperty("4", "simple");
            props.setProperty("4.extractor", "net.semanticmetadata.lire.imageanalysis.features.global.CEDD");
            props.setProperty("4.detector", "detCVSURF");
            props.setProperty("4.codebook.1", "./src/test/resources/codebooks/SIMPLEdetCVSURFCEDD32");
            props.setProperty("4.codebook.2", "./src/test/resources/codebooks/SIMPLEdetCVSURFCEDD128");

            FileOutputStream fos = new FileOutputStream("testxml.xml");
            props.storeToXML(fos, "AllExtractors");
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writePropertiesFile(){
        try{
            Properties props = new Properties();

            props.setProperty("0", "info");
            props.setProperty("0.info.0", aggregator.getCanonicalName());
            int counter = 1;
            for (int i = 0; i < numOfClusters.length; i++) {
                props.setProperty("0.info." + String.valueOf(counter), String.valueOf(numOfClusters[i]));
                counter++;
            }

            counter = 1;
            Map.Entry<ExtractorItem, LinkedList<Cluster[]>> nextEntry;
            for (Iterator<Map.Entry<ExtractorItem, LinkedList<Cluster[]>>> iterator = LocalExtractorsAndCodebooks.entrySet().iterator(); iterator.hasNext(); ) {
                nextEntry = iterator.next();
                props.setProperty(String.valueOf(counter), "local");
                props.setProperty(String.valueOf(counter) + ".extractor", nextEntry.getKey().getExtractorClass().getCanonicalName());
                for (int i = 0; i < nextEntry.getValue().size(); i++) {
                    props.setProperty(String.valueOf(counter) + ".codebook." + String.valueOf(i + 1), nextEntry.getKey().getFeatureInstance().getFieldName() + nextEntry.getValue().get(i).length);
                }
                counter++;
            }
            for (Iterator<Map.Entry<ExtractorItem, LinkedList<Cluster[]>>> iterator = SimpleExtractorsAndCodebooks.entrySet().iterator(); iterator.hasNext(); ) {
                nextEntry = iterator.next();
                props.setProperty(String.valueOf(counter), "simple");
                props.setProperty(String.valueOf(counter) + ".extractor", nextEntry.getKey().getExtractorClass().getCanonicalName());
                props.setProperty(String.valueOf(counter) + ".detector", SimpleExtractor.getDetector(nextEntry.getKey().getKeypointDetector()));
                for (int i = 0; i < nextEntry.getValue().size(); i++) {
                    props.setProperty(String.valueOf(counter) + ".codebook." + String.valueOf(i + 1), nextEntry.getKey().getFieldName() + nextEntry.getValue().get(i).length);
                }
                counter++;
            }
            for (Iterator<ExtractorItem> iterator = GlobalExtractors.iterator(); iterator.hasNext(); ) {
                props.setProperty(String.valueOf(counter), "global");
                props.setProperty(String.valueOf(counter) + ".extractor", iterator.next().getExtractorClass().getCanonicalName());
                counter++;
            }

            FileOutputStream fos = new FileOutputStream("testxml.xml");
            props.storeToXML(fos, "AllExtractors");
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadPropertiesFile(String path){
        try{
            Properties prop = new Properties();
            FileInputStream fis = new FileInputStream(path);
            prop.loadFromXML(fis);

            int codebookCounter, counter = 0;
            Class<? extends GlobalFeature> tmpGlobalClass;
            Class<? extends LocalFeatureExtractor> tmpLocalClass;
            SimpleExtractor.KeypointDetector detector;
            LinkedList<Cluster[]> tmpListOfCodebooks;
            String extractorType;
            while(prop.getProperty(String.valueOf(counter)) != null){
                extractorType = prop.getProperty(String.valueOf(counter));
                if (extractorType.equals("global")){
                    tmpGlobalClass = (Class<? extends GlobalFeature>)Class.forName(prop.getProperty(String.valueOf(counter) + ".extractor"));
                    addExtractor(tmpGlobalClass);
                } else if (extractorType.equals("local")){
                    codebookCounter = 1;
                    tmpListOfCodebooks = new LinkedList<Cluster[]>();
                    while(prop.getProperty(String.valueOf(counter) + ".codebook." + String.valueOf(codebookCounter)) != null){
                        tmpListOfCodebooks.add(Cluster.readClusters(prop.getProperty(String.valueOf(counter) + ".codebook." + String.valueOf(codebookCounter))));
                        codebookCounter++;
                    }
                    tmpLocalClass = (Class<? extends LocalFeatureExtractor>)Class.forName(prop.getProperty(String.valueOf(counter) + ".extractor"));
                    addExtractor(tmpLocalClass, tmpListOfCodebooks);
                } else if (extractorType.equals("simple")){
                    codebookCounter = 1;
                    tmpListOfCodebooks = new LinkedList<Cluster[]>();
                    while(prop.getProperty(String.valueOf(counter) + ".codebook." + String.valueOf(codebookCounter)) != null){
                        tmpListOfCodebooks.add(Cluster.readClusters(prop.getProperty(String.valueOf(counter) + ".codebook." + String.valueOf(codebookCounter))));
                        codebookCounter++;
                    }
                    tmpGlobalClass = (Class<? extends GlobalFeature>)Class.forName(prop.getProperty(String.valueOf(counter) + ".extractor"));
                    detector = SimpleExtractor.getDetector(prop.getProperty(String.valueOf(counter) + ".detector"));
                    addExtractor(tmpGlobalClass, detector, tmpListOfCodebooks);
                } else if (extractorType.equals("info")){
                    this.aggregator = (Class<? extends AbstractAggregator>)Class.forName(prop.getProperty(String.valueOf(counter) + ".info.0"));
                    codebookCounter = 1;
                    LinkedList<Integer> tmpListOfNumOfClusters = new LinkedList<Integer>();
                    while(prop.getProperty(String.valueOf(counter) + ".info." + String.valueOf(codebookCounter)) != null){
                        tmpListOfNumOfClusters.add(Integer.valueOf(prop.getProperty(String.valueOf(counter) + ".info." + String.valueOf(codebookCounter))));
                        codebookCounter++;
                    }
                    codebookCounter = 0;
                    numOfClusters = new int[tmpListOfNumOfClusters.size()];
                    for (Integer e : tmpListOfNumOfClusters)
                        numOfClusters[codebookCounter++] = e.intValue();
                } else {
                    throw new UnsupportedOperationException("loadPropertiesFile");
                }
                counter++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void addExtractor(Class<? extends Extractor> extractorClass) {
        if (docsCreated) throw new UnsupportedOperationException("Cannot add extractors after documents have been created!");
        ExtractorItem extractorItem = new ExtractorItem(extractorClass);
        boolean flag = true;
        if (extractorItem.isGlobal()){
            for (Iterator<ExtractorItem> iterator = GlobalExtractors.iterator(); iterator.hasNext(); ) {
                ExtractorItem next = iterator.next();
                if (next.getExtractorClass().equals(extractorClass)){
                    flag = false;
                }
            }
            if (flag) {
                this.GlobalExtractors.add(extractorItem);
            } else {
                throw new UnsupportedOperationException(extractorClass.getSimpleName() + " already exists!!");
            }
        }
        else if (extractorItem.isLocal()) {
            for (Iterator<Map.Entry<ExtractorItem, LinkedList<Cluster[]>>> iterator = LocalExtractorsAndCodebooks.entrySet().iterator(); iterator.hasNext(); ) {
                Map.Entry<ExtractorItem, LinkedList<Cluster[]>> next = iterator.next();
                if (next.getKey().getExtractorClass().equals(extractorClass)){
                    flag = false;
                }
            }
            if (flag) {
                this.LocalExtractorsAndCodebooks.put(extractorItem, new LinkedList<Cluster[]>());
                this.sample = true;
            } else {
                throw new UnsupportedOperationException(extractorClass.getSimpleName() + " already exists!!");
            }
        } else throw new UnsupportedOperationException("Error");
    }

    public void addExtractor(Class<? extends LocalFeatureExtractor> localFeatureExtractor, LinkedList<Cluster[]> codebooks) {
        if (docsCreated) throw new UnsupportedOperationException("Cannot add extractors after documents have been created!");
        ExtractorItem extractorItem = new ExtractorItem(localFeatureExtractor);
        boolean found, flag = true;

        for (Iterator<Map.Entry<ExtractorItem, LinkedList<Cluster[]>>> iterator = LocalExtractorsAndCodebooks.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<ExtractorItem, LinkedList<Cluster[]>> next = iterator.next();
            if (next.getKey().getExtractorClass().equals(localFeatureExtractor)){
                flag = false;
            }
        }
        if (flag) {
            this.LocalExtractorsAndCodebooks.put(extractorItem, codebooks);

            for (int i = 0; i < numOfClusters.length; i++) {
                found = false;
                for (int j = 0; j < codebooks.size(); j++) {
                    if (codebooks.get(j).length == numOfClusters[i])
                    {
                        found = true;
                    }
                }
                if (!found) this.sample = true;
            }

        } else {
            throw new UnsupportedOperationException(localFeatureExtractor.getSimpleName() + " already exists!!");
        }
    }

    public void addExtractor(Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector detector, LinkedList<Cluster[]> codebooks) {
        if (docsCreated) throw new UnsupportedOperationException("Cannot add extractors after documents have been created!");
        boolean found, flag = true;
        for (Iterator<Map.Entry<ExtractorItem, LinkedList<Cluster[]>>> iterator = SimpleExtractorsAndCodebooks.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<ExtractorItem, LinkedList<Cluster[]>> next = iterator.next();
            if ((next.getKey().getExtractorClass().equals(globalFeatureClass))&&(next.getKey().getKeypointDetector()==detector)){
                flag = false;
            }
        }
        if (flag) {
            this.SimpleExtractorsAndCodebooks.put(new ExtractorItem(globalFeatureClass, detector), codebooks);

            for (int i = 0; i < numOfClusters.length; i++) {
                found = false;
                for (int j = 0; j < codebooks.size(); j++) {
                    if (codebooks.get(j).length == numOfClusters[i])
                    {
                        found = true;
                    }
                }
                if (!found) this.sample = true;
            }

        } else {
            throw new UnsupportedOperationException(globalFeatureClass.getSimpleName() + " with " + detector.name() + " already exists!!");
        }
    }

    public void testPrintSetUp(){
        System.out.println("===================================================================================");
        System.out.println("SetUp:");
        if (((LocalExtractorsAndCodebooks.size() > 0)||(SimpleExtractorsAndCodebooks.size() > 0))&&(numOfClusters.length > 0)){
            System.out.println("numOfDocsForCodebooks: " + numOfDocsForCodebooks);
            System.out.print("Set of codebooks: " + numOfClusters[0]);
            for (int i = 1; i < numOfClusters.length; i++) {
                System.out.print(", " + numOfClusters[i]);
            }
            System.out.println();
        }

        if (GlobalExtractors.size() > 0) {
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("Set of GlobalFeatures: ");
            for (ExtractorItem GlobalExtractor : GlobalExtractors) {
                System.out.println(GlobalExtractor.getExtractorClass().getSimpleName());
            }
        }

        Iterator<Cluster[]> iteratorList;
        if (LocalExtractorsAndCodebooks.size() > 0) {
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("Set of LocalFeaturesExtractors: ");
            for (Map.Entry<ExtractorItem, LinkedList<Cluster[]>> extractorItemLinkedListEntry : LocalExtractorsAndCodebooks.entrySet()) {
                System.out.println(extractorItemLinkedListEntry.getKey().getExtractorClass().getSimpleName());
                if (extractorItemLinkedListEntry.getValue().size() > 0) {
                    System.out.print(" ~ Existing codebooks: ");
                    iteratorList = extractorItemLinkedListEntry.getValue().iterator();
                    System.out.print(iteratorList.next().length);
                    for (int i = 1; i < extractorItemLinkedListEntry.getValue().size(); i++) {
                        System.out.print(", " + iteratorList.next().length);
                    }
                    System.out.println();
                }
            }
        }

        if (SimpleExtractorsAndCodebooks.size() > 0) {
            System.out.println("~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
            System.out.println("Set of SIMPLE Features: ");
            for (Map.Entry<ExtractorItem, LinkedList<Cluster[]>> extractorItemLinkedListEntry : SimpleExtractorsAndCodebooks.entrySet()) {
                System.out.println(extractorItemLinkedListEntry.getKey().getExtractorClass().getSimpleName() + " ~ " + SimpleExtractor.getDetector(extractorItemLinkedListEntry.getKey().getKeypointDetector()));
                if (extractorItemLinkedListEntry.getValue().size() > 0) {
                    System.out.print(" ~ Existing codebooks: ");
                    iteratorList = extractorItemLinkedListEntry.getValue().iterator();
                    System.out.print(iteratorList.next().length);
                    for (int i = 1; i < extractorItemLinkedListEntry.getValue().size(); i++) {
                        System.out.print(", " + iteratorList.next().length);
                    }
                    System.out.println();
                }
            }
        }
        System.out.println("===================================================================================");
    }
}
