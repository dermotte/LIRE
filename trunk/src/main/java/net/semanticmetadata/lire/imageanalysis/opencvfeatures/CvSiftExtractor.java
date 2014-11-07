package net.semanticmetadata.lire.imageanalysis.opencvfeatures;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.DescriptorExtractor;
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
 */
public class CvSiftExtractor {
    //Default: int nfeatures=0, int nOctaveLayers=3, double contrastThreshold=0.04, double edgeThreshold=10, double sigma=1.6
    private int nfeatures=0;
    private int nOctaveLayers=3;
    private double contrastThreshold=0.04;
    private double edgeThreshold=10;
    private double sigma=1.6;

    public CvSiftExtractor(){
    }

    public CvSiftExtractor(int features, int OctaveLayers, double contrastThres, double edgeThres, double sgm){
        nfeatures=features;
        nOctaveLayers=OctaveLayers;
        contrastThreshold=contrastThres;
        edgeThreshold=edgeThres;
        sigma=sgm;
    }

    public LinkedList<CvSiftFeature> computeSiftFeatures(BufferedImage img) {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SIFT);
        try {
            File temp = File.createTempFile("tempFile", ".tmp");
            //int nfeatures=0, int nOctaveLayers=3, double contrastThreshold=0.04, double edgeThreshold=10, double sigma=1.6
            //String settings = "%YAML:1.0\nnfeatures: 0\nnOctaveLayers: 3\ncontrastThreshold: 0.04\nedgeThreshold: 10\nsigma: 1.6";
            String settings = "%YAML:1.0\nnfeatures: "+nfeatures+"\nnOctaveLayers: "+nOctaveLayers+"\ncontrastThreshold: "+contrastThreshold+"\nedgeThreshold: "+edgeThreshold+"\nsigma: "+sigma;
            FileWriter writer = new FileWriter(temp, false);
            writer.write(settings);
            writer.close();
            extractor.read(temp.getPath());
            detector.read(temp.getPath());
            temp.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        LinkedList<CvSiftFeature> features = new LinkedList<CvSiftFeature>();
        KeyPoint key;
        CvSiftFeature feat;
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
            feat = new CvSiftFeature(key.angle, key.class_id, key.octave, new double[] {key.pt.x, key.pt.y}, key.response, key.size, desc);
            features.add(feat);
        }

        return features;
    }

    public LinkedList<CvSiftFeature> computeSiftKeypoints(BufferedImage img) {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SIFT);
        try {
            File temp = File.createTempFile("tempFile", ".tmp");
            //int nfeatures=0, int nOctaveLayers=3, double contrastThreshold=0.04, double edgeThreshold=10, double sigma=1.6
            //String settings = "%YAML:1.0\nnfeatures: 0\nnOctaveLayers: 3\ncontrastThreshold: 0.04\nedgeThreshold: 10\nsigma: 1.6";
            String settings = "%YAML:1.0\nnfeatures: "+nfeatures+"\nnOctaveLayers: "+nOctaveLayers+"\ncontrastThreshold: "+contrastThreshold+"\nedgeThreshold: "+edgeThreshold+"\nsigma: "+sigma;
            FileWriter writer = new FileWriter(temp, false);
            writer.write(settings);
            writer.close();
            detector.read(temp.getPath());
            temp.deleteOnExit();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

        LinkedList<CvSiftFeature> features = new LinkedList<CvSiftFeature>();
        KeyPoint next;
        CvSiftFeature feat;
        for (Iterator<KeyPoint> iterator = myKeys.iterator(); iterator.hasNext(); ) {
            next = iterator.next();
            feat = new CvSiftFeature(next.angle, next.class_id, next.octave, new double[] {next.pt.x, next.pt.y}, next.response, next.size, null);
            features.add(feat);
        }

        return features;
    }

    public String getParameters()
    {
        return "nfeatures: "+nfeatures+" nOctaveLayers: "+nOctaveLayers+" contrastThreshold: "+contrastThreshold+" edgeThreshold: "+edgeThreshold+" sigma: "+sigma;
    }

}
