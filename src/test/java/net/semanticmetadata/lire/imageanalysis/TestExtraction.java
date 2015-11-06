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

package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;
import net.semanticmetadata.lire.aggregators.AbstractAggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.builders.LocalDocumentBuilder;
import net.semanticmetadata.lire.builders.SimpleDocumentBuilder;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.utils.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

/**
 * Created by Nektarios on 28/5/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class TestExtraction extends TestCase{
    Class<? extends GlobalFeature> globalFeatureClass = CEDD.class;
    Class<? extends LocalFeatureExtractor> localFeatureClass = CvSurfExtractor.class;
    SimpleExtractor.KeypointDetector keypointDetector = SimpleExtractor.KeypointDetector.CVSURF;
    Class<? extends AbstractAggregator> aggregatorClass = BOVW.class;

    String imagePath = "./src/test/resources/images/";
    String codebookPath = "./src/test/resources/codebooks/";

    public void testExtraction() throws IllegalAccessException, IOException, InstantiationException {
        ArrayList<String> images = FileUtils.readFileLines(new File(imagePath), true);
        System.out.println();
        testGlobalExtract(images);
        testLocalExtract(images);
        testSimpleExtract(images);
    }

    public void testGlobalExtract() throws IOException, IllegalAccessException, InstantiationException {
        ArrayList<String> images = FileUtils.readFileLines(new File(imagePath), true);
        testGlobalExtract(images);
    }

    public void testGlobalExtract(ArrayList<String> images) throws IOException, IllegalAccessException, InstantiationException {
        GlobalFeature globalFeature = globalFeatureClass.newInstance();
        GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder();

        BufferedImage image;
        double[] featureVector;
        long ms, totalTime = 0;
        for (String path : images) {
            image = ImageIO.read(new FileInputStream(path));
            ms = System.currentTimeMillis();
            globalDocumentBuilder.extractGlobalFeature(image, globalFeature);
            ms = System.currentTimeMillis() - ms;
            totalTime += ms;
            featureVector = globalFeature.getFeatureVector();

            System.out.println(String.format("%.2f",  (double) ms ) + " ms. ~ " + path.substring(path.lastIndexOf('\\') + 1) + " ~ " + Arrays.toString(featureVector));
        }
        System.out.println(globalFeature.getFeatureName() + " " + String.format("%.2f",  totalTime / (double) images.size()) + " ms each.");
        System.out.println();
    }

    public void testLocalExtract() throws IOException, IllegalAccessException, InstantiationException {
        ArrayList<String> images = FileUtils.readFileLines(new File(imagePath), true);
        testLocalExtract(images);
    }

    public void testLocalExtract(ArrayList<String> images) throws IOException, IllegalAccessException, InstantiationException {
        LocalFeatureExtractor localFeatureExtractor = localFeatureClass.newInstance();
        LocalDocumentBuilder localDocumentBuilder = new LocalDocumentBuilder();
        AbstractAggregator aggregator = aggregatorClass.newInstance();

        Cluster[] codebook32 = Cluster.readClusters(codebookPath + "CvSURF32");
        Cluster[] codebook128 = Cluster.readClusters(codebookPath + "CvSURF128");

        BufferedImage image;
        double[] featureVector;
        long ms, totalTime = 0;
        for (String path : images) {
            image = ImageIO.read(new FileInputStream(path));
            ms = System.currentTimeMillis();
            localDocumentBuilder.extractLocalFeatures(image, localFeatureExtractor);
            aggregator.createVectorRepresentation(localFeatureExtractor.getFeatures(), codebook32);
            ms = System.currentTimeMillis() - ms;
            totalTime += ms;
            featureVector = aggregator.getVectorRepresentation();

            System.out.println(String.format("%.2f",  (double) ms ) + " ms. ~ " + path.substring(path.lastIndexOf('\\') + 1) + " ~ " + Arrays.toString(featureVector));
        }
        System.out.println(localFeatureExtractor.getClassOfFeatures().newInstance().getFeatureName() + " " + String.format("%.2f",  totalTime / (double) images.size()) + " ms each.");
        System.out.println();
    }

    public void testSimpleExtract() throws IOException, IllegalAccessException, InstantiationException {
        ArrayList<String> images = FileUtils.readFileLines(new File(imagePath), true);
        testSimpleExtract(images);
    }

    public void testSimpleExtract(ArrayList<String> images) throws IOException, IllegalAccessException, InstantiationException {
        SimpleExtractor simpleExtractor = new SimpleExtractor(globalFeatureClass.newInstance(), keypointDetector);
        SimpleDocumentBuilder simpleDocumentBuilder = new SimpleDocumentBuilder();
        AbstractAggregator aggregator = aggregatorClass.newInstance();

        Cluster[] codebook32 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD32");
        Cluster[] codebook128 = Cluster.readClusters(codebookPath + "SIMPLEdetCVSURFCEDD128");

        BufferedImage image;
        double[] featureVector;
        long ms, totalTime = 0;
        for (String path : images) {
            image = ImageIO.read(new FileInputStream(path));
            ms = System.currentTimeMillis();
            simpleDocumentBuilder.extractLocalFeatures(image, simpleExtractor);
            aggregator.createVectorRepresentation(simpleExtractor.getFeatures(), codebook32);
            ms = System.currentTimeMillis() - ms;
            totalTime += ms;
            featureVector = aggregator.getVectorRepresentation();

            System.out.println(String.format("%.2f",  (double) ms ) + " ms. ~ " + path.substring(path.lastIndexOf('\\') + 1) + " ~ " + Arrays.toString(featureVector));
        }
        System.out.println(simpleExtractor.getFeatureName() + " " + String.format("%.2f",  totalTime / (double) images.size()) + " ms each.");
        System.out.println();
    }
}
