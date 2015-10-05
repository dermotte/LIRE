package net.semanticmetadata.lire.imageanalysis.features.local.shapecontext;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.DefaultRealMatrixChangingVisitor;
import org.apache.commons.math3.linear.RealMatrix;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by Lukas Knoch on 16.09.15.
 * Shape Context implemented according to:
 * S. Belongie, J. Malik, and J. Puzicha, “Shape Matching and Object
 * Recognition Using Shape Contexts,” IEEE Trans. Pattern Analysis and
 * Machine Intelligence, vol. 24, no. 4, pp. 509-522, Apr. 2002
 * https://www.cs.berkeley.edu/~malik/papers/BMP-shape.pdf
 *
 *
 */


public class ShapeContextExtractor implements net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor {

    public static final int SAMPLE_POINTS = 500;
    LinkedList<ShapeContext> listOfFeatures;

    public static LinkedList<ShapeContext> createHistogram(Point[] points, final int angularBins, int radialBins, float innerRadius, float outerRadius) {
        RealMatrix anglesMatrix = calculateAngleMatrix(points, angularBins);
        RealMatrix radiusMatrix = calculateRadiusMatrix(points, radialBins, innerRadius, outerRadius);
        return constructFeatureList(anglesMatrix, radiusMatrix, angularBins, radialBins, points);
    }

    /**
     * Return a matrix which has all angles from each point to all the other
     * @param points
     * @return
     */
    private static RealMatrix calculateAngleMatrix(Point[] points, final int angularBins) {
        double xLength, yLength;
        int numOfSamples = points.length;
        double twoPI = 2 * Math.PI;
        RealMatrix anglesMatrix = new Array2DRowRealMatrix(numOfSamples, numOfSamples);

        for (int i = 0; i < numOfSamples; i++) {
            for (int j = 0; j < numOfSamples; j++) {
                xLength = points[i].x - points[j].x;
                yLength = points[i].y - points[j].y;
                anglesMatrix.setEntry(i, j, Math.atan2(xLength, yLength));
                anglesMatrix.setEntry(i, j, ((anglesMatrix.getEntry(i, j) % twoPI) + twoPI) % twoPI);
            }
        }
        anglesMatrix.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
            public double visit(int row, int column, double value) {
                return Math.floor(value / (2 * Math.PI / angularBins));
            }
        });
        return anglesMatrix;
    }

    /**
     * Return a matrix which has all radius from each point to all the other
     * @param points
     * @param radialBins
     * @param innerRadius
     *@param outerRadius @return
     */
    private static RealMatrix calculateRadiusMatrix(Point[] points, int radialBins, float innerRadius, float outerRadius) {
        double sumDist = 0;
        RealMatrix radiusMatrix = new Array2DRowRealMatrix(points.length, points.length);
        for (int i = 0; i < points.length; i++) {
            for (int j = i+1; j < points.length; j++) {
                double euclidean = Utils.euclidDistance(points[i], points[j]);
                radiusMatrix.setEntry(i, j, euclidean);
                radiusMatrix.setEntry(j, i, euclidean);
                sumDist += (euclidean * 2);
            }
        }
        double meanDistance = sumDist / Math.pow(points.length, 2);
        if (meanDistance != 0) {
            final double finalMeanDistance = meanDistance;
            radiusMatrix.walkInOptimizedOrder(new DefaultRealMatrixChangingVisitor() {
                public double visit(int row, int column, double value) {
                    return value / finalMeanDistance;
                }
            });
        }
        double val;
        double[] LogSpace = Utils.logSpace(innerRadius, outerRadius, radialBins);
        double upperLogSpaceValue = LogSpace[radialBins - 1];
        for (int i = 0; i < points.length; i++) {
            for (int j = 0; j < points.length; j++) {
                val = radiusMatrix.getEntry(i, j);
                radiusMatrix.setEntry(i, j, Utils.NAN);
                if (val > upperLogSpaceValue) {
                    continue;
                }
                for (int k = 0; k < radialBins; ++k) { // find the right radius
                    if (val < LogSpace[k]) {
                        radiusMatrix.setEntry(i, j, k);
                        break;
                    }
                }
            }
        }
        return radiusMatrix;
    }

    /**
     *
     * @param angleMatrix
     * @param radiusMatrix
     * @param radialBins
     * @param angularBins
     * @param points
     * @return
     */
    private static LinkedList<ShapeContext> constructFeatureList(RealMatrix angleMatrix, RealMatrix radiusMatrix, int radialBins, int angularBins, Point[] points) {
        int rowLen = angleMatrix.getRowDimension();
        int columnLen = angleMatrix.getColumnDimension();
        if (rowLen != radiusMatrix.getRowDimension() || columnLen != radiusMatrix.getColumnDimension()) { // dimension are not the same
            return null;
        }
        LinkedList<ShapeContext> shapeContexts = new LinkedList<>();
        int radialBin, angularBin;
        for (int i = 0; i < rowLen; i++) {
            RealMatrix histogram = new Array2DRowRealMatrix(radialBins, angularBins);
            for (int j = i; j < columnLen; j++) {
                radialBin = (int) radiusMatrix.getEntry(i, j);
                angularBin = (int) angleMatrix.getEntry(i, j);
                if (radialBin != Utils.NAN && angularBin != Utils.NAN) {
                    histogram.setEntry(angularBin, radialBin, histogram.getEntry(angularBin, radialBin) + 1);
                }
            }
            shapeContexts.add(new ShapeContext(elements(histogram),points[i].x,points[i].y));
        }
        return shapeContexts;
    }

    @Override
    public List<? extends net.semanticmetadata.lire.imageanalysis.features.LocalFeature> getFeatures() {
        return listOfFeatures;
    }

    @Override
    public Class<? extends net.semanticmetadata.lire.imageanalysis.features.LocalFeature> getClassOfFeatures() {
        return ShapemeHistogram.class;
    }

    /**
     *
     * @param image
     */
    @Override
    public void extract(BufferedImage image) {
        Point[] pointsArray;
        try {
            List<Point> pts = getEdgePoints(image, SAMPLE_POINTS);
            pointsArray = pts.toArray(new Point[pts.size()]);
        }catch (Exception e){
            e.printStackTrace();
            pointsArray= new Point[0];
        }
        listOfFeatures = ShapeContextExtractor.createHistogram(pointsArray, 12, 5, 0.2f, 2f);
    }

    /**
     * selects edge points from image.
     * @param image
     * @return
     */
    public static List<Point> getEdgePoints(BufferedImage image, int samplePoints) {
        net.semanticmetadata.lire.imageanalysis.filters.CannyEdgeDetector cannyEdgeDetector = new net.semanticmetadata.lire.imageanalysis.filters.CannyEdgeDetector(image);
        image = cannyEdgeDetector.filter();
        List<Point> points = new ArrayList<>();
        int[] tmp = new int[1];
        for (int i = 0; i < image.getHeight(); i++) {
            for (int j = 0; j < image.getWidth(); j++) {
                if (image.getRaster().getPixel(j, i, tmp)[0] == 0) {
                    points.add(new Point(j, i));
                }
            }
        }
        return distributedSample(points, samplePoints);
    }

    /**
     * Method for debug use only, generates an image of the sampled points
     * @param image
     * @return
     */
    public static BufferedImage getSampledPointsImage(BufferedImage image){
        int[] pixel = {255};
        List<Point> points = getEdgePoints(image, SAMPLE_POINTS);
        BufferedImage imgNew = new BufferedImage(image.getWidth(),image.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
        for (Point point : points) {
            imgNew.getRaster().setPixel(point.x,point.y,pixel);
        }
        return imgNew;
    }

    /**
     * transforms RealMatrix to double[]. Bit stupid as the internal representation is a double[] anyway...
     * @param re
     * @return
     */
    private static double[] elements(RealMatrix re) {
        int k = 0;
        double[] res = new double[re.getRowDimension()*re.getColumnDimension()];
        for (int i = 0; i < re.getRowDimension(); i++) {
            for (int j = 0; j < re.getColumnDimension(); j++) {
                res[k++] = re.getEntry(i, j);
            }
        }
        return res;
    }

    /**
     * get a random sample from the given points
     * @param items
     * @param m
     * @param <T>
     * @return
     */
    public static <T> Set<T> randomSample(List<T> items, int m){
        if(items.size()<=m){
            return new HashSet<>(items);
        }
        Random rnd = new Random(100);
        HashSet<T> res = new HashSet<>(m);
        int n = items.size();
        for(int i=n-m;i<n;i++){
            int pos = rnd.nextInt(i+1);
            T item = items.get(pos);
            if (res.contains(item))
                res.add(items.get(i));
            else
                res.add(item);
        }
        return res;
    }

    /**
     * Get every nth point, where n = items.size()/m
     * @param items
     * @param m
     * @param <T>
     * @return
     */
    public static <T> List<T> distributedSample(List<T> items, int m){
        if(items.size()<=m){
            return new ArrayList<>(items);
        }
        List<T> res = new ArrayList<>(m);
        int n = items.size();
        int step = n/m;
        for (int i = 0; i < n; i+=step) {
            res.add(items.get(i));
        }
        return res;
    }

    public static class Point implements Comparable<Point> {
        public int x;
        public int y;

        public Point(int new_x, int new_y) {
            x = new_x;
            y = new_y;
        }

        public String toString(){
            return "x: "+x+" y: "+y;
        }

        @Override
        public int compareTo(Point point) {
            int v1  =  Integer.compare(x, point.x);
            if(v1 != 0){
                return v1;
            }
            return Integer.compare(y, point.y);
        }
    }

    public static class Utils {
        public static int NAN = -1;

        public static double euclidDistance(Point point1, Point point2) {
            double xSqr = Math.pow(point1.x - point2.x, 2);
            double ySqr = Math.pow(point1.y - point2.y, 2);
            return Math.sqrt(xSqr + ySqr);
        }

        /**
         * create an array with numSlizes log space distributed.
         * @param lowBoundary
         * @param highBoundary
         * @param numSlices
         * @return
         */
        public static double[] logSpace(double lowBoundary, double highBoundary, int numSlices) {
            lowBoundary = Math.log10(lowBoundary);
            highBoundary = Math.log10(highBoundary);
            double[] logSpace = new double[numSlices];
            double distance = highBoundary - lowBoundary;
            int numOfSlices = numSlices - 1;
            double delta = distance / numOfSlices;
            for (short i = 0; i < numOfSlices; i++) {
                logSpace[i] = Math.pow(10, lowBoundary + (i * delta));
            }
            logSpace[numOfSlices] = Math.pow(10, highBoundary);
            return logSpace;
        }
    }
}

