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
public class CvSurfExtractor {
    //Default: double  hessianThreshold, int nOctaves=4, int nOctaveLayers=2, bool extended=true, bool upright=false
    private double hessianThreshold=500.0;
    private int nOctaves=4;
    private int nOctaveLayers=2;
    private Boolean extended=true;
    private Boolean upright=false;

    public CvSurfExtractor(){

    }

    public CvSurfExtractor(double hessianThres, int Octaves, int OctaveLayers, Boolean extend, Boolean upr){
        hessianThreshold = hessianThres;
        nOctaves = Octaves;
        nOctaveLayers = OctaveLayers;
        extended = extend;
        upright = upr;
    }

    public LinkedList<CvSurfFeature> computeSurfFeatures(BufferedImage img) {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
        try {
            File temp = File.createTempFile("tempFile", ".tmp");
            //String settings = "%YAML:1.0\nhessianThreshold: 500.0\nnOctaves: 4\nnOctaveLayers: 2\nextended: true\nupright: false";
            String settings = "%YAML:1.0\nhessianThreshold: "+hessianThreshold+"\nnOctaves: "+nOctaves+"\nnOctaveLayers: "+nOctaveLayers+"\nextended: "+extended+"\nupright: "+upright;
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

        LinkedList<CvSurfFeature> features = new LinkedList<CvSurfFeature>();
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
            feat = new CvSurfFeature(key.angle, key.class_id, key.octave, new double[] {key.pt.x, key.pt.y}, key.response, key.size, desc);
            features.add(feat);
        }

        return features;

    }

    public LinkedList<CvSurfFeature> computeSurfKeypoints(BufferedImage img) {
        System.loadLibrary( Core.NATIVE_LIBRARY_NAME );

        FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
        try {
            File temp = File.createTempFile("tempFile", ".tmp");
            //String settings = "%YAML:1.0\nhessianThreshold: 500.0\nnOctaves: 4\nnOctaveLayers: 2\nextended: true\nupright: false";
            String settings = "%YAML:1.0\nhessianThreshold: "+hessianThreshold+"\nnOctaves: "+nOctaves+"\nnOctaveLayers: "+nOctaveLayers+"\nextended: "+extended+"\nupright: "+upright;
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

        LinkedList<CvSurfFeature> features = new LinkedList<CvSurfFeature>();
        KeyPoint next;
        CvSurfFeature feat;
        for (Iterator<KeyPoint> iterator = myKeys.iterator(); iterator.hasNext(); ) {
            next = iterator.next();
            feat = new CvSurfFeature(next.angle, next.class_id, next.octave, new double[] {next.pt.x, next.pt.y}, next.response, next.size, null);
            features.add(feat);
        }

        return features;
    }

    public String getParameters()
    {
        return "hessianThreshold: "+hessianThreshold+" nOctaves: "+nOctaves+" nOctaveLayers: "+nOctaveLayers+" extended: "+extended+" upright: "+upright;
    }
}
