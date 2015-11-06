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

package net.semanticmetadata.lire.builders;

import junit.framework.TestCase;
import net.semanticmetadata.lire.aggregators.AbstractAggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.utils.FileUtils;
import org.apache.lucene.document.Document;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nektarios on 28/5/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class TestBuilders extends TestCase {
    Class<? extends GlobalFeature> globalFeatureClass = CEDD.class;
    Class<? extends LocalFeatureExtractor> localFeatureClass = CvSurfExtractor.class;
    SimpleExtractor.KeypointDetector keypointDetector = SimpleExtractor.KeypointDetector.CVSURF;
    Class<? extends AbstractAggregator> aggregatorClass = BOVW.class;

    String imagePath = "./src/test/resources/images/";
    String codebookPath = "./src/test/resources/codebooks/";

    public void testAllBuilders() throws IllegalAccessException, IOException, InstantiationException {
        ArrayList<String> images = FileUtils.readFileLines(new File(imagePath), true);
        System.out.println();
        testGlobal(images);
        testLocal(images);
        testSimple(images);
    }

    public void testGlobal() throws IOException, IllegalAccessException, InstantiationException {
        ArrayList<String> images = FileUtils.readFileLines(new File(imagePath), true);
        testGlobal(images);
    }

    public void testGlobal(ArrayList<String> images) throws IOException, IllegalAccessException, InstantiationException {
        GlobalFeature feature = globalFeatureClass.newInstance();
        GlobalFeature cachedInstance1 = globalFeatureClass.newInstance();
        GlobalFeature cachedInstance2 = globalFeatureClass.newInstance();
        GlobalFeature cachedInstance3 = globalFeatureClass.newInstance();
        GlobalFeature cachedInstance4 = globalFeatureClass.newInstance();
        GlobalFeature cachedInstance5 = globalFeatureClass.newInstance();
        String fieldName = feature.getFieldName();

        //Ways to create GlobalDocumentBuilder
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder();

        GlobalDocumentBuilder globalDocumentBuilder1 = new GlobalDocumentBuilder();
        globalDocumentBuilder1.addExtractor(globalFeatureClass);

        GlobalDocumentBuilder globalDocumentBuilder2 = new GlobalDocumentBuilder(globalFeatureClass);

        GlobalDocumentBuilder globalDocumentBuilder3 = new GlobalDocumentBuilder(globalFeatureClass, false);

        GlobalDocumentBuilder globalDocumentBuilder4 = new GlobalDocumentBuilder(true); //Using hashing
        globalDocumentBuilder4.addExtractor(globalFeatureClass);

        GlobalDocumentBuilder globalDocumentBuilder5 = new GlobalDocumentBuilder(globalFeatureClass, true); //Using hashing


        BufferedImage image;
        double[] featureVector, featureVector1, featureVector2, featureVector3, featureVector4, featureVector5;
        Document document1, document2, document3, document4, document5;
        double distance1, distance2, distance3, distance4;
        boolean bool, bool1, bool2, bool3, bool4, bool5;

        for (String path : images) {
            image = ImageIO.read(new FileInputStream(path));

            globalDocumentBuilder.extractGlobalFeature(image, feature);
            featureVector = feature.getFeatureVector();

            document1 = globalDocumentBuilder1.createDocument(image, imagePath);
            cachedInstance1.setByteArrayRepresentation(document1.getField(fieldName).binaryValue().bytes, document1.getField(fieldName).binaryValue().offset, document1.getField(fieldName).binaryValue().length);
            featureVector1 = cachedInstance1.getFeatureVector();

            document2 = globalDocumentBuilder2.createDocument(image, imagePath);
            cachedInstance2.setByteArrayRepresentation(document2.getField(fieldName).binaryValue().bytes, document2.getField(fieldName).binaryValue().offset, document2.getField(fieldName).binaryValue().length);
            featureVector2 = cachedInstance2.getFeatureVector();

            document3 = globalDocumentBuilder3.createDocument(image, imagePath);
            cachedInstance3.setByteArrayRepresentation(document3.getField(fieldName).binaryValue().bytes, document3.getField(fieldName).binaryValue().offset, document3.getField(fieldName).binaryValue().length);
            featureVector3 = cachedInstance3.getFeatureVector();

            document4 = globalDocumentBuilder4.createDocument(image, imagePath);
            cachedInstance4.setByteArrayRepresentation(document4.getField(fieldName).binaryValue().bytes, document4.getField(fieldName).binaryValue().offset, document4.getField(fieldName).binaryValue().length);
            featureVector4 = cachedInstance4.getFeatureVector();

            document5 = globalDocumentBuilder5.createDocument(image, imagePath);
            cachedInstance5.setByteArrayRepresentation(document5.getField(fieldName).binaryValue().bytes, document5.getField(fieldName).binaryValue().offset, document5.getField(fieldName).binaryValue().length);
            featureVector5 = cachedInstance5.getFeatureVector();

            distance1 = cachedInstance1.getDistance(cachedInstance2);
            distance2 = cachedInstance2.getDistance(cachedInstance3);
            distance3 = cachedInstance3.getDistance(cachedInstance4);
            distance4 = cachedInstance4.getDistance(cachedInstance5);

            bool = (distance1 == 0.0) && (distance1 == distance2) && (distance2 == distance3) && (distance3 == distance4);
            bool1 = Arrays.equals(featureVector, featureVector1);
            bool2 = Arrays.equals(featureVector1, featureVector2);
            bool3 = Arrays.equals(featureVector2, featureVector3);
            bool4 = Arrays.equals(featureVector3, featureVector4);
            bool5 = Arrays.equals(featureVector4, featureVector5);


            if (!(bool && bool1 && bool2 && bool3 && bool4 && bool5)) System.err.println("ERROR using " + path);
        }
        System.out.println("Global done...");
    }

    public void testLocal() throws IOException, IllegalAccessException, InstantiationException {
        ArrayList<String> images = FileUtils.readFileLines(new File(imagePath), true);
        testLocal(images);
    }

    public void testLocal(ArrayList<String> images) throws IOException, IllegalAccessException, InstantiationException {
        AbstractAggregator aggregator = aggregatorClass.newInstance();

        Cluster[] codebook32 = Cluster.readClusters(codebookPath + "CvSURF32");
        Cluster[] codebook128 = Cluster.readClusters(codebookPath + "CvSURF128");

        LinkedList<Cluster[]> listOfCodebooks = new LinkedList<Cluster[]>();
        listOfCodebooks.add(codebook32);
        listOfCodebooks.add(codebook128);

        LocalFeatureExtractor localFeatureExtractor = localFeatureClass.newInstance();

        LocalFeature feature = localFeatureClass.newInstance().getClassOfFeatures().newInstance();
        LocalFeature cachedInstance1 = localFeatureClass.newInstance().getClassOfFeatures().newInstance();
        LocalFeature cachedInstance2 = localFeatureClass.newInstance().getClassOfFeatures().newInstance();
        LocalFeature cachedInstance3 = localFeatureClass.newInstance().getClassOfFeatures().newInstance();
        LocalFeature cachedInstance4 = localFeatureClass.newInstance().getClassOfFeatures().newInstance();
        LocalFeature cachedInstance5 = localFeatureClass.newInstance().getClassOfFeatures().newInstance();
        LocalFeature cachedInstance6 = localFeatureClass.newInstance().getClassOfFeatures().newInstance();
        String fieldName = feature.getFieldName() + aggregator.getFieldName() + "32";

        //Ways to create LocalDocumentBuilder
        LocalDocumentBuilder localDocumentBuilder = new LocalDocumentBuilder();
        localDocumentBuilder.addExtractor(localFeatureClass, codebook32);

        LocalDocumentBuilder localDocumentBuilder1 = new LocalDocumentBuilder();
        localDocumentBuilder1.addExtractor(localFeatureClass, listOfCodebooks);

        LocalDocumentBuilder localDocumentBuilder2 = new LocalDocumentBuilder(localFeatureClass, codebook32);

        LocalDocumentBuilder localDocumentBuilder3 = new LocalDocumentBuilder(localFeatureClass, listOfCodebooks);

        LocalDocumentBuilder localDocumentBuilder4 = new LocalDocumentBuilder(aggregatorClass);
        localDocumentBuilder4.addExtractor(localFeatureClass, listOfCodebooks);

        LocalDocumentBuilder localDocumentBuilder5 = new LocalDocumentBuilder(localFeatureClass, codebook32, aggregatorClass);

        LocalDocumentBuilder localDocumentBuilder6 = new LocalDocumentBuilder(localFeatureClass, listOfCodebooks, aggregatorClass);

        BufferedImage image;
        double[] featureVector, featureVector1, featureVector2, featureVector3, featureVector4, featureVector5, featureVector6;
        Document document1, document2, document3, document4, document5, document6;
        double distance1, distance2, distance3, distance4, distance5;
        boolean bool, bool1, bool2, bool3, bool4, bool5, bool6;

        List<? extends LocalFeature> listOfLocalFeatures;

        for (String path : images) {
            image = ImageIO.read(new FileInputStream(path));

            localDocumentBuilder.extractLocalFeatures(image, localFeatureExtractor);
            listOfLocalFeatures = localFeatureExtractor.getFeatures();
            aggregator.createVectorRepresentation(listOfLocalFeatures, codebook32);
            featureVector = aggregator.getVectorRepresentation();

            document1 = localDocumentBuilder1.createDocument(image, imagePath);
            cachedInstance1.setByteArrayRepresentation(document1.getField(fieldName).binaryValue().bytes, document1.getField(fieldName).binaryValue().offset, document1.getField(fieldName).binaryValue().length);
            featureVector1 = cachedInstance1.getFeatureVector();

            document2 = localDocumentBuilder2.createDocument(image, imagePath);
            cachedInstance2.setByteArrayRepresentation(document2.getField(fieldName).binaryValue().bytes, document2.getField(fieldName).binaryValue().offset, document2.getField(fieldName).binaryValue().length);
            featureVector2 = cachedInstance2.getFeatureVector();

            document3 = localDocumentBuilder3.createDocument(image, imagePath);
            cachedInstance3.setByteArrayRepresentation(document3.getField(fieldName).binaryValue().bytes, document3.getField(fieldName).binaryValue().offset, document3.getField(fieldName).binaryValue().length);
            featureVector3 = cachedInstance3.getFeatureVector();

            document4 = localDocumentBuilder4.createDocument(image, imagePath);
            cachedInstance4.setByteArrayRepresentation(document4.getField(fieldName).binaryValue().bytes, document4.getField(fieldName).binaryValue().offset, document4.getField(fieldName).binaryValue().length);
            featureVector4 = cachedInstance4.getFeatureVector();

            document5 = localDocumentBuilder5.createDocument(image, imagePath);
            cachedInstance5.setByteArrayRepresentation(document5.getField(fieldName).binaryValue().bytes, document5.getField(fieldName).binaryValue().offset, document5.getField(fieldName).binaryValue().length);
            featureVector5 = cachedInstance5.getFeatureVector();

            document6 = localDocumentBuilder6.createDocument(image, imagePath);
            cachedInstance6.setByteArrayRepresentation(document6.getField(fieldName).binaryValue().bytes, document6.getField(fieldName).binaryValue().offset, document6.getField(fieldName).binaryValue().length);
            featureVector6 = cachedInstance6.getFeatureVector();

            distance1 = cachedInstance1.getDistance(cachedInstance2);
            distance2 = cachedInstance2.getDistance(cachedInstance3);
            distance3 = cachedInstance3.getDistance(cachedInstance4);
            distance4 = cachedInstance4.getDistance(cachedInstance5);
            distance5 = cachedInstance5.getDistance(cachedInstance6);

            bool = (distance1 == 0.0) && (distance1 == distance2) && (distance2 == distance3) && (distance3 == distance4) && (distance4 == distance5);
            bool1 = Arrays.equals(featureVector, featureVector1);
            bool2 = Arrays.equals(featureVector1, featureVector2);
            bool3 = Arrays.equals(featureVector2, featureVector3);
            bool4 = Arrays.equals(featureVector3, featureVector4);
            bool5 = Arrays.equals(featureVector4, featureVector5);
            bool6 = Arrays.equals(featureVector5, featureVector6);


            if (!(bool && bool1 && bool2 && bool3 && bool4 && bool5 && bool6)) System.err.println("ERROR using " + path);
        }
        System.out.println("Local done...");
    }

    public void testSimple() throws IOException, IllegalAccessException, InstantiationException {
        ArrayList<String> images = FileUtils.readFileLines(new File(imagePath), true);
        testSimple(images);
    }

    public void testSimple(ArrayList<String> images) throws IOException, IllegalAccessException, InstantiationException {
        AbstractAggregator aggregator = aggregatorClass.newInstance();

        Cluster[] codebook32 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD32");
        Cluster[] codebook128 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD128");

        LinkedList<Cluster[]> listOfCodebooks = new LinkedList<Cluster[]>();
        listOfCodebooks.add(codebook32);
        listOfCodebooks.add(codebook128);

        SimpleExtractor simpleExtractor = new SimpleExtractor(globalFeatureClass.newInstance(), keypointDetector);

        LocalFeature cachedInstance1 = simpleExtractor.getClassOfFeatures().newInstance();
        LocalFeature cachedInstance2 = simpleExtractor.getClassOfFeatures().newInstance();
        LocalFeature cachedInstance3 = simpleExtractor.getClassOfFeatures().newInstance();
        LocalFeature cachedInstance4 = simpleExtractor.getClassOfFeatures().newInstance();
        LocalFeature cachedInstance5 = simpleExtractor.getClassOfFeatures().newInstance();
        LocalFeature cachedInstance6 = simpleExtractor.getClassOfFeatures().newInstance();
        String fieldName = simpleExtractor.getFieldName() + aggregator.getFieldName() + "32";

        //Ways to create SimpleDocumentBuilder
        SimpleDocumentBuilder simpleDocumentBuilder = new SimpleDocumentBuilder();
        simpleDocumentBuilder.addExtractor(globalFeatureClass, keypointDetector, codebook32);

        SimpleDocumentBuilder simpleDocumentBuilder1 = new SimpleDocumentBuilder();
        simpleDocumentBuilder1.addExtractor(globalFeatureClass, keypointDetector, listOfCodebooks);

        SimpleDocumentBuilder simpleDocumentBuilder2 = new SimpleDocumentBuilder(globalFeatureClass, keypointDetector, codebook32);

        SimpleDocumentBuilder simpleDocumentBuilder3 = new SimpleDocumentBuilder(globalFeatureClass, keypointDetector, listOfCodebooks);

        SimpleDocumentBuilder simpleDocumentBuilder4 = new SimpleDocumentBuilder(aggregatorClass);
        simpleDocumentBuilder4.addExtractor(globalFeatureClass, keypointDetector, listOfCodebooks);

        SimpleDocumentBuilder simpleDocumentBuilder5 = new SimpleDocumentBuilder(globalFeatureClass, keypointDetector, codebook32, aggregatorClass);

        SimpleDocumentBuilder simpleDocumentBuilder6 = new SimpleDocumentBuilder(globalFeatureClass, keypointDetector, listOfCodebooks, aggregatorClass);

        BufferedImage image;
        double[] featureVector, featureVector1, featureVector2, featureVector3, featureVector4, featureVector5, featureVector6;
        Document document1, document2, document3, document4, document5, document6;
        double distance1, distance2, distance3, distance4, distance5;
        boolean bool, bool1, bool2, bool3, bool4, bool5, bool6;

        List<? extends LocalFeature> listOfLocalFeatures;

        for (String path : images) {
            image = ImageIO.read(new FileInputStream(path));

            simpleDocumentBuilder.extractLocalFeatures(image, simpleExtractor);
            listOfLocalFeatures = simpleExtractor.getFeatures();
            aggregator.createVectorRepresentation(listOfLocalFeatures, codebook32);
            featureVector = aggregator.getVectorRepresentation();

            document1 = simpleDocumentBuilder1.createDocument(image, imagePath);
            cachedInstance1.setByteArrayRepresentation(document1.getField(fieldName).binaryValue().bytes, document1.getField(fieldName).binaryValue().offset, document1.getField(fieldName).binaryValue().length);
            featureVector1 = cachedInstance1.getFeatureVector();

            document2 = simpleDocumentBuilder2.createDocument(image, imagePath);
            cachedInstance2.setByteArrayRepresentation(document2.getField(fieldName).binaryValue().bytes, document2.getField(fieldName).binaryValue().offset, document2.getField(fieldName).binaryValue().length);
            featureVector2 = cachedInstance2.getFeatureVector();

            document3 = simpleDocumentBuilder3.createDocument(image, imagePath);
            cachedInstance3.setByteArrayRepresentation(document3.getField(fieldName).binaryValue().bytes, document3.getField(fieldName).binaryValue().offset, document3.getField(fieldName).binaryValue().length);
            featureVector3 = cachedInstance3.getFeatureVector();

            document4 = simpleDocumentBuilder4.createDocument(image, imagePath);
            cachedInstance4.setByteArrayRepresentation(document4.getField(fieldName).binaryValue().bytes, document4.getField(fieldName).binaryValue().offset, document4.getField(fieldName).binaryValue().length);
            featureVector4 = cachedInstance4.getFeatureVector();

            document5 = simpleDocumentBuilder5.createDocument(image, imagePath);
            cachedInstance5.setByteArrayRepresentation(document5.getField(fieldName).binaryValue().bytes, document5.getField(fieldName).binaryValue().offset, document5.getField(fieldName).binaryValue().length);
            featureVector5 = cachedInstance5.getFeatureVector();

            document6 = simpleDocumentBuilder6.createDocument(image, imagePath);
            cachedInstance6.setByteArrayRepresentation(document6.getField(fieldName).binaryValue().bytes, document6.getField(fieldName).binaryValue().offset, document6.getField(fieldName).binaryValue().length);
            featureVector6 = cachedInstance6.getFeatureVector();

            distance1 = cachedInstance1.getDistance(cachedInstance2);
            distance2 = cachedInstance2.getDistance(cachedInstance3);
            distance3 = cachedInstance3.getDistance(cachedInstance4);
            distance4 = cachedInstance4.getDistance(cachedInstance5);
            distance5 = cachedInstance5.getDistance(cachedInstance6);

            bool = (distance1 == 0.0) && (distance1 == distance2) && (distance2 == distance3) && (distance3 == distance4) && (distance4 == distance5);
            bool1 = Arrays.equals(featureVector, featureVector1);
            bool2 = Arrays.equals(featureVector1, featureVector2);
            bool3 = Arrays.equals(featureVector2, featureVector3);
            bool4 = Arrays.equals(featureVector3, featureVector4);
            bool5 = Arrays.equals(featureVector4, featureVector5);
            bool6 = Arrays.equals(featureVector5, featureVector6);


            if (!(bool && bool1 && bool2 && bool3 && bool4 && bool5 && bool6)) System.err.println("ERROR using " + path);
        }
        System.out.println("Simple done...");
    }







}
