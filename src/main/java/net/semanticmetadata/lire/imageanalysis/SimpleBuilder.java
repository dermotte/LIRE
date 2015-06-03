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
 * Updated: 13.02.15 18:52
 */

package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.ScalableColor;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSiftExtractor;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSiftFeature;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSurfFeature;
import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * Implementation based on the paper Searching Images with MPEG-7 (& MPEG-7-like) Powered Localized
 * dEscriptors: The SIMPLE answer to effective Content Based Image Retrieval (Savvas Chatzichristofis
 * sent me the code :))
 * Created by mlux on 13.06.2014.
 */
public class SimpleBuilder extends AbstractDocumentBuilder {
    public static final int MAX_IMAGE_DIMENSION = 1024;
    public static final String Detector_CVSURF = "detCVSURF";
    public static final String Detector_CVSIFT = "detCVSIFT";
    public static final String Detector_RANDOM = "detRandom";
    public static final String Detector_GAUSSRANDOM = "detGaussRandom";

    private LireFeature lireFeature = new CEDD();
    private KeypointDetector kpdetect = KeypointDetector.Random;
    public enum KeypointDetector {CVSURF, CVSIFT, Random, GaussRandom};
    private int samples = 600;

    final int[] sizeLookUp = new int[] {40, (int) (40 * 1.6), (int) (40 * 2.3), 40 * 3};

    public SimpleBuilder() {
    }

    /**
     * Set the Local Descriptor feature to the one given.
     * @param lireFeature
     */
    public SimpleBuilder(LireFeature lireFeature) {
        this.lireFeature = lireFeature;
    }

    public SimpleBuilder(LireFeature lireFeature, KeypointDetector detector) {
        this.kpdetect = detector;
        this.lireFeature = lireFeature;
    }

    public SimpleBuilder(LireFeature lireFeature, KeypointDetector detector, int numOfSamplesInCaseOfRandom) {
        this.kpdetect = detector;
        this.lireFeature = lireFeature;
        this.samples = numOfSamplesInCaseOfRandom;
    }


    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        BufferedImage bimg = image;
        // Scaling image is especially with the correlogram features very important!
        // All images are scaled to guarantee a certain upper limit for indexing.
        if (Math.max(image.getHeight(), image.getWidth()) > MAX_IMAGE_DIMENSION) {
            bimg = ImageUtils.scaleImage(image, MAX_IMAGE_DIMENSION);
        }
        if (kpdetect == KeypointDetector.CVSURF) {
            return useCVSURF(bimg);
        }
        else if (kpdetect == KeypointDetector.CVSIFT){
            return useCVSIFT(bimg);
        }
        else if (kpdetect == KeypointDetector.Random){
            return useRandom(bimg);
        }
        else if (kpdetect == KeypointDetector.GaussRandom){
            return useGaussRandom(bimg);
        }
        else
            throw new UnsupportedOperationException("Something was wrong in setting the desired detector");
    }

    private Field[] useCVSURF(BufferedImage image) {
        ArrayList<Field> fields = new ArrayList<Field>();
        CvSurfExtractor extractor = new CvSurfExtractor();
        LinkedList<CvSurfFeature> descriptors = extractor.computeSurfKeypoints(image);
        CvSurfFeature next;
        for (Iterator<CvSurfFeature> iterator = descriptors.iterator(); iterator.hasNext(); ) {
            next = iterator.next();
            lireFeature.extract(ImageUtils.cropImage(image, (int) (next.point[0] - (int) next.size / 2), (int) (next.point[1] - (int) next.size / 2), (int) next.size, (int) next.size));
            fields.add(new StoredField(DocumentBuilder.FIELD_NAME_SIMPLE + lireFeature.getFieldName() + Detector_CVSURF, lireFeature.getByteArrayRepresentation()));
        }
        return fields.toArray(new Field[fields.size()]);
    }

    private Field[] useCVSIFT(BufferedImage image) {
        ArrayList<Field> fields = new ArrayList<Field>();
        CvSiftExtractor extractor = new CvSiftExtractor();
        LinkedList<CvSiftFeature> descriptors = extractor.computeSiftKeypoints(image);
        CvSiftFeature next;
        for (Iterator<CvSiftFeature> iterator = descriptors.iterator(); iterator.hasNext(); ) {
            next = iterator.next();
            lireFeature.extract(ImageUtils.cropImage(image, (int) (next.point[0] - (int) next.size / 2), (int) (next.point[1] - (int) next.size / 2), (int) next.size, (int) next.size));
            fields.add(new StoredField(DocumentBuilder.FIELD_NAME_SIMPLE + lireFeature.getFieldName() + Detector_CVSIFT, lireFeature.getByteArrayRepresentation()));
        }
        return fields.toArray(new Field[fields.size()]);
    }

    private Field[] useRandom(BufferedImage image) {
        ArrayList<Field> fields = new ArrayList<Field>();
//        LinkedList<keypoint> keypointsList = createRndPts(image.getWidth(), image.getHeight(), samples);
//        keypoint next;
        // opting in for more performance: no "new" for creating key points, no String concat in the loop. (ml)
        int[] myKeypoint = new int[3];
        Random r = new Random();
        String fieldName = DocumentBuilder.FIELD_NAME_SIMPLE + lireFeature.getFieldName() + Detector_RANDOM;
        for (int i = 0; i < samples; i++) {
            createNextRandomPoint(myKeypoint, image.getWidth(), image.getHeight(), r);
            lireFeature.extract(ImageUtils.cropImage(image, myKeypoint[0], myKeypoint[1],  myKeypoint[2], myKeypoint[2]));
            fields.add(new StoredField(fieldName, lireFeature.getByteArrayRepresentation()));
            
        }
//        for (Iterator<keypoint> iterator = keypointsList.iterator(); iterator.hasNext(); ) {
//            next = iterator.next();
//            lireFeature.extract(ImageUtils.cropImage(image, (int) (next.X), (int) (next.Y), (int) next.Size, (int) next.Size));
//            fields.add(new StoredField(DocumentBuilder.FIELD_NAME_SIMPLE + lireFeature.getFieldName() + Detector_RANDOM, lireFeature.getByteArrayRepresentation()));
//        }
        return fields.toArray(new Field[fields.size()]);
    }

    private Field[] useGaussRandom(BufferedImage image) {
        ArrayList<Field> fields = new ArrayList<Field>();
        LinkedList<keypoint> keypointsList = createGaussRndPts(image.getWidth(), image.getHeight(), samples);
        keypoint next;
        String fieldName = DocumentBuilder.FIELD_NAME_SIMPLE + lireFeature.getFieldName() + Detector_GAUSSRANDOM;
        for (Iterator<keypoint> iterator = keypointsList.iterator(); iterator.hasNext(); ) {
            next = iterator.next();
            lireFeature.extract(ImageUtils.cropImage(image, (int) (next.X - (next.Size / 2)), (int) (next.Y - (next.Size / 2)), (int) next.Size, (int) next.Size));
            fields.add(new StoredField(fieldName, lireFeature.getByteArrayRepresentation()));
        }
        return fields.toArray(new Field[fields.size()]);
    }

    private LinkedList<keypoint> createRndPts(int width, int height, int samples){
        Random ran = new Random();
        int size = -1;
        keypoint key;
        LinkedList<keypoint> keypointsList = new LinkedList<keypoint>();
        for (int i = 0; i < samples; i++) {
//            sizeTemp = ran.nextInt(4);
//            if (sizeTemp == 0) {
//                size = 40;
//            } else if (sizeTemp == 1) {
//                size = (int) (40 * 1.6);
//            } else if (sizeTemp == 2) {
//                size = (int) (40 * 2.3);
//            } else if (sizeTemp == 3) {
//                size = (int) (40 * 3);
//            }
            size = sizeLookUp[ran.nextInt(4)];
            key = new keypoint(ran.nextInt(width - size), ran.nextInt(height - size), size);
//            System.out.println("(" + (key.X) + "," + (key.Y) + ")\t" + key.Size);
            keypointsList.add(key);
        }

        return keypointsList;
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

        int size = -1;
        int sizeTemp, sizeLimit, widthLimit, heightLimit;
        int x, y;
        keypoint key;
        LinkedList<keypoint> keypointsList = new LinkedList<keypoint>();
        for (int i = 0; i < samples; i++) {
//            sizeTemp = ran.nextInt(4);
//            if (sizeTemp == 0) {
//                size = 40;
//            } else if (sizeTemp == 1) {
//                size = (int) (40 * 1.6);
//            } else if (sizeTemp == 2) {
//                size = (int) (40 * 2.3);
//            } else if (sizeTemp == 3) {
//                size = (int) (40 * 3);
//            }
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

    public String getDetector(KeypointDetector detector)
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

    public String getFieldName(KeypointDetector detector, LireFeature feature)
    {
        if (detector == KeypointDetector.CVSURF) {
            return DocumentBuilder.FIELD_NAME_SIMPLE + feature.getFieldName() + getDetector(detector);
        }
        else if (detector == KeypointDetector.CVSIFT){
            return DocumentBuilder.FIELD_NAME_SIMPLE + feature.getFieldName() + getDetector(detector);
        }
        else if (detector == KeypointDetector.Random){
            return DocumentBuilder.FIELD_NAME_SIMPLE + feature.getFieldName() + getDetector(detector);
        }
        else if (detector == KeypointDetector.GaussRandom){
            return DocumentBuilder.FIELD_NAME_SIMPLE + feature.getFieldName() + getDetector(detector);
        }
        else
            throw new UnsupportedOperationException("Something was wrong in returning the used detector");
    }

    private class keypoint
    {
        public int X;
        public int Y;
        public int Size;

        public keypoint(int xx, int yy, int s)
        {
            X = xx;
            Y = yy;
            Size = s;
        }
    }
}
