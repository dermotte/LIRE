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
 * Updated: 13.02.15 18:52
 */

package net.semanticmetadata.lire.imageanalysis.features.local.simple;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSiftExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSiftFeature;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfFeature;
import net.semanticmetadata.lire.utils.ImageUtils;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Implementation based on the paper "Searching Images with MPEG-7 (& MPEG-7-like) Powered Localized
 * dEscriptors: The SIMPLE answer to effective Content Based Image Retrieval"
 * Created by mlux on 13/06/2014.
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class SimpleExtractor implements LocalFeatureExtractor{
    public enum KeypointDetector {CVSURF, CVSIFT, Random, GaussRandom};

    private static final String Detector_CVSURF = "detCVSURF";
    private static final String Detector_CVSIFT = "detCVSIFT";
    private static final String Detector_RANDOM = "detRnd";
    private static final String Detector_GAUSSRANDOM = "detGRnd";

    private final int[] sizeLookUp = new int[] {40, (int) (40 * 1.6), (int) (40 * 2.3), 40 * 3};
    private String fieldName, featureName;

    private Class<? extends GlobalFeature> globalFeatureClass;
    private GlobalFeature globalFeature;
    private KeypointDetector kpdetector;
    private int samplePoints = 600;

    private CvSurfExtractor cvSurfExtractor;
    private CvSiftExtractor cvSiftExtractor;

    LinkedList<SimpleFeature> listOfFeatures;


    public SimpleExtractor(GlobalFeature globalFeature, KeypointDetector detector) {
        this.globalFeature = globalFeature;
        this.globalFeatureClass = globalFeature.getClass();
        this.kpdetector = detector;

        this.fieldName = DocumentBuilder.FIELD_NAME_SIMPLE + getDetector(this.kpdetector) + this.globalFeature.getFieldName();
        if (kpdetector == KeypointDetector.CVSURF) {
            cvSurfExtractor = new CvSurfExtractor();
            featureName = DocumentBuilder.FIELD_NAME_SIMPLE + " using " + globalFeature.getFeatureName() + " and the CVSURF Detector";
        } else if (kpdetector == KeypointDetector.CVSIFT){
            cvSiftExtractor = new CvSiftExtractor();
            featureName = DocumentBuilder.FIELD_NAME_SIMPLE + " using " + globalFeature.getFeatureName() + " and the CVSIFT Detector";
        } else if (kpdetector == KeypointDetector.Random){
            featureName = DocumentBuilder.FIELD_NAME_SIMPLE + " using " + globalFeature.getFeatureName() + " and a Random keypoint Detector";
        } else if (kpdetector == KeypointDetector.GaussRandom){
            featureName = DocumentBuilder.FIELD_NAME_SIMPLE + " using " + globalFeature.getFeatureName() + " and a GaussRandom keypoint Detector";
        } else
            throw new UnsupportedOperationException("Something was wrong in setting the desired detector");
    }

    @Override
    public List<? extends LocalFeature> getFeatures() {
        return listOfFeatures;
    }

    @Override
    public Class<? extends LocalFeature> getClassOfFeatures() {
        return SimpleFeature.class;
    }

    public Class<? extends GlobalFeature> getGlobalFeatureClass(){
        return globalFeatureClass;
    }

    public KeypointDetector getKpdetector () {
        return kpdetector;
    }


    @Override
    public void extract(BufferedImage image) {
        if (kpdetector == KeypointDetector.CVSURF) {
            useCVSURF(image);
        }
        else if (kpdetector == KeypointDetector.CVSIFT){
            useCVSIFT(image);
        }
        else if (kpdetector == KeypointDetector.Random){
            useRandom(image);
        }
        else if (kpdetector == KeypointDetector.GaussRandom){
            useGaussRandom(image);
        }
        else
            throw new UnsupportedOperationException("Something was wrong in setting the desired detector");
    }

    private void useCVSURF(BufferedImage image) {
        listOfFeatures = new LinkedList<SimpleFeature>();
        LinkedList<CvSurfFeature> surfKeypoints = cvSurfExtractor.computeSurfKeypoints(image);
        for (CvSurfFeature keypoint : surfKeypoints) {
            globalFeature.extract(ImageUtils.cropImage(image, (int) (keypoint.getX() - (int) keypoint.getSize() / 2), (int) (keypoint.getY() - (int) keypoint.getSize() / 2), (int) keypoint.getSize(), (int) keypoint.getSize()));
            listOfFeatures.add(new SimpleFeature(globalFeature.getFeatureVector(), keypoint.getX(), keypoint.getY(), keypoint.getSize(), fieldName, featureName, globalFeatureClass));
        }
    }

    private void useCVSIFT(BufferedImage image) {
        listOfFeatures = new LinkedList<SimpleFeature>();
        LinkedList<CvSiftFeature> cvSiftFeatures = cvSiftExtractor.computeSiftKeypoints(image);
        for (CvSiftFeature cvSiftFeature : cvSiftFeatures) {
            globalFeature.extract(ImageUtils.cropImage(image, (int) (cvSiftFeature.getX() - (int) cvSiftFeature.getSize() / 2), (int) (cvSiftFeature.getY() - (int) cvSiftFeature.getSize() / 2), (int) cvSiftFeature.getSize(), (int) cvSiftFeature.getSize()));
            listOfFeatures.add(new SimpleFeature(globalFeature.getFeatureVector(), cvSiftFeature.getX(), cvSiftFeature.getY(), cvSiftFeature.getSize(), fieldName, featureName, globalFeatureClass));
        }
    }

    private void useRandom(BufferedImage image) {
        listOfFeatures = new LinkedList<SimpleFeature>();
        int[] myKeypoint = new int[3];
        Random r = new Random();
        for (int i = 0; i < samplePoints; i++) {
            createNextRandomPoint(myKeypoint, image.getWidth(), image.getHeight(), r);
            globalFeature.extract(ImageUtils.cropImage(image, myKeypoint[0], myKeypoint[1], myKeypoint[2], myKeypoint[2]));
            listOfFeatures.add(new SimpleFeature(globalFeature.getFeatureVector(), myKeypoint[0], myKeypoint[1], myKeypoint[2], fieldName, featureName, globalFeatureClass));
        }
    }

    private void useGaussRandom(BufferedImage image) {
        listOfFeatures = new LinkedList<SimpleFeature>();
        LinkedList<keypoint> keypointsList = createGaussRndPts(image.getWidth(), image.getHeight(), samplePoints);
        for (keypoint kpoint : keypointsList) {
            globalFeature.extract(ImageUtils.cropImage(image, (int) (kpoint.X - (kpoint.Size / 2)), (int) (kpoint.Y - (kpoint.Size / 2)), (int) kpoint.Size, (int) kpoint.Size));
            listOfFeatures.add(new SimpleFeature(globalFeature.getFeatureVector(), (int) (kpoint.getX() - (kpoint.getSize() / 2)), (int) (kpoint.getY() - (kpoint.getSize() / 2)), (int) kpoint.getSize(), fieldName, featureName, globalFeatureClass));
        }
    }

    private void createNextRandomPoint(int[] myKeypoint, int width, int height, Random random) {
        myKeypoint[2] = sizeLookUp[random.nextInt(4)];
        myKeypoint[0] = random.nextInt(width-myKeypoint[2]);
        myKeypoint[1] = random.nextInt(height-myKeypoint[2]);
    }

    private LinkedList<keypoint> createGaussRndPts(int width, int height, int samples){
        Random ran = new Random();

        double seedWidth = (width / 4) - 10;
        double seedHeight = (height / 4) - 10;
        double meanWidth = width / 2;
        double meanHeight = height / 2;

        int size, x, y, sizeLimit, widthLimit, heightLimit;
        keypoint key;
        LinkedList<keypoint> keypointsList = new LinkedList<keypoint>();
        for (int i = 0; i < samples; i++) {
            size = sizeLookUp[ran.nextInt(4)];
            sizeLimit = size / 2;
            widthLimit = width - sizeLimit;
            heightLimit = height - sizeLimit;

            //TODO: change do...while
            do
            {
                x = (int) (ran.nextGaussian() * seedWidth + meanWidth);
            } while (!((x > sizeLimit) && (x < widthLimit)));
            do
            {
                y = (int) (ran.nextGaussian() * seedHeight + meanHeight);
            } while (!((y > sizeLimit) && (y < heightLimit)));

            key = new keypoint(x, y, size);
//            System.out.println("(" + (key.X) + "," + (key.Y) + ")\t" + key.Size);
            keypointsList.add(key);
        }

        return keypointsList;
    }

    public static String getDetector(KeypointDetector detector)
    {
        if (detector == KeypointDetector.CVSURF) {
            return Detector_CVSURF;
        }
        else if (detector == KeypointDetector.CVSIFT){
            return Detector_CVSIFT;
        }
        else if (detector == KeypointDetector.Random){
            return Detector_RANDOM;
        }
        else if (detector == KeypointDetector.GaussRandom){
            return Detector_GAUSSRANDOM;
        }
        else
            throw new UnsupportedOperationException("Something was wrong in returning the used detector");
    }

    public static KeypointDetector getDetector(String detector)
    {
        if (detector.equals(Detector_CVSURF)) {
            return KeypointDetector.CVSURF;
        }
        else if (detector.equals(Detector_CVSIFT)){
            return KeypointDetector.CVSIFT;
        }
        else if (detector.equals(Detector_RANDOM)){
            return KeypointDetector.Random;
        }
        else if (detector.equals(Detector_GAUSSRANDOM)){
            return KeypointDetector.GaussRandom;
        }
        else
            throw new UnsupportedOperationException("Something was wrong in returning the used detector");
    }

    public String getFieldName() {
        return fieldName;
    }

    public String getFeatureName(){
        return featureName;
    }

    private class keypoint
    {
        private int X;
        private int Y;
        private int Size;

        public keypoint(int x, int y, int size)
        {
            this.X = x;
            this.Y = y;
            this.Size = size;
        }

        public int getSize() {
            return Size;
        }

        public int getX() {
            return X;
        }

        public int getY() {
            return Y;
        }
    }
}
