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

package net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures;

import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nektarios on 1/10/2014.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class CvSurfExtractor implements LocalFeatureExtractor {
    //Default: double  hessianThreshold, int nOctaves=4, int nOctaveLayers=2, bool extended=true, bool upright=false
    private double hessianThreshold=500.0;
    private int nOctaves=4;
    private int nOctaveLayers=2;
    private int extended=1;
    private int upright=0;

    LinkedList<CvSurfFeature> features;

    FeatureDetector detector;
    DescriptorExtractor extractor;

//    private boolean passingParams = false;

    public CvSurfExtractor(){
        init();
    }

    public CvSurfExtractor(double hessianThreshold, int nOctaves, int nOctaveLayers, int extended, int upright){
        this.hessianThreshold = hessianThreshold;
        this.nOctaves = nOctaves;
        this.nOctaveLayers = nOctaveLayers;
        this.extended = extended;
        this.upright = upright;
//        this.passingParams = true;
        init();
    }

    private void init(){
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        detector = FeatureDetector.create(FeatureDetector.SURF);
        extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
//        if (passingParams) {
        try {
            File temp = File.createTempFile("tempFile", ".tmp");
            //String settings = "%YAML:1.0\nhessianThreshold: 500.0\nnOctaves: 4\nnOctaveLayers: 2\nextended: true\nupright: false";
            String settings = "%YAML:1.0\nhessianThreshold: " + hessianThreshold + "\nnOctaves: " + nOctaves + "\nnOctaveLayers: " + nOctaveLayers + "\nextended: " + extended + "\nupright: " + upright;
            FileWriter writer = new FileWriter(temp, false);
            writer.write(settings);
            writer.close();
            extractor.read(temp.getPath());
            detector.read(temp.getPath());
            temp.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        }
    }

    @Override
    public LinkedList<CvSurfFeature> getFeatures() {
        return features;
    }

    @Override
    public Class<? extends LocalFeature> getClassOfFeatures() {
        return CvSurfFeature.class;
    }

    @Override
    public void extract(BufferedImage img) {
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        List<KeyPoint> myKeys;
//        Mat img_object = Highgui.imread(image, 0); //0 = CV_LOAD_IMAGE_GRAYSCALE
//        detector.detect(img_object, keypoints);
        byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        Mat matRGB = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
        matRGB.put(0, 0, data);
        Mat matGray = new Mat(img.getHeight(),img.getWidth(),CvType.CV_8UC1);
        Imgproc.cvtColor(matRGB, matGray, Imgproc.COLOR_BGR2GRAY);              //TODO: RGB or BGR?
        byte[] dataGray = new byte[matGray.rows()*matGray.cols()*(int)(matGray.elemSize())];
        matGray.get(0, 0, dataGray);

        detector.detect(matGray, keypoints);
        extractor.compute(matGray, keypoints, descriptors);
        myKeys = keypoints.toList();

        features = new LinkedList<CvSurfFeature>();
        KeyPoint key;
        CvSurfFeature feat;
        double[] desc;
        int cols, rows = myKeys.size();
        for (int i=0; i<rows; i++) {
            cols = (descriptors.row(i)).cols();
            desc = new double[cols];
            key = myKeys.get(i);
            for(int j=0; j < cols; j++)
            {
                desc[j]=descriptors.get(i, j)[0];
            }
            feat = new CvSurfFeature(key.pt.x, key.pt.y, key.size, desc);
            features.add(feat);
        }
    }


    public LinkedList<CvSurfFeature> computeSurfKeypoints(BufferedImage img) {
        MatOfKeyPoint keypoints = new MatOfKeyPoint();
        List<KeyPoint> myKeys;
//        Mat img_object = Highgui.imread(image, 0); //0 = CV_LOAD_IMAGE_GRAYSCALE
//        detector.detect(img_object, keypoints);
        byte[] data = ((DataBufferByte) img.getRaster().getDataBuffer()).getData();
        Mat matRGB = new Mat(img.getHeight(), img.getWidth(), CvType.CV_8UC3);
        matRGB.put(0, 0, data);
        Mat matGray = new Mat(img.getHeight(),img.getWidth(),CvType.CV_8UC1);
        Imgproc.cvtColor(matRGB, matGray, Imgproc.COLOR_BGR2GRAY);              //TODO: RGB or BGR?
        byte[] dataGray = new byte[matGray.rows()*matGray.cols()*(int)(matGray.elemSize())];
        matGray.get(0, 0, dataGray);

        detector.detect(matGray, keypoints);
        myKeys = keypoints.toList();

        LinkedList<CvSurfFeature> myKeypoints = new LinkedList<CvSurfFeature>();
        KeyPoint key;
        CvSurfFeature feat;
        for (Iterator<KeyPoint> iterator = myKeys.iterator(); iterator.hasNext(); ) {
            key = iterator.next();
            feat = new CvSurfFeature(key.pt.x, key.pt.y, key.size, null);
            myKeypoints.add(feat);
        }

        return myKeypoints;
    }

    public String getParameters()
    {
        return "hessianThreshold: "+hessianThreshold+" nOctaves: "+nOctaves+" nOctaveLayers: "+nOctaveLayers+" extended: "+extended+" upright: "+upright;
    }

}
