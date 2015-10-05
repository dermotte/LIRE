package net.semanticmetadata.lire.aggregators;

import net.semanticmetadata.lire.imageanalysis.features.local.shapecontext.ShapeContext;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Lukas Knoch on 16.09.15.
 * Shapeme feature implemented according to
 * http://www.cs.berkeley.edu/~malik/papers/mori-belongie-malik-pami05.pdf
 * Shape Context implemented according to
 *
 * S. Belongie, J. Malik, and J. Puzicha, “Shape Matching and Object
 * Recognition Using Shape Contexts,” IEEE Trans. Pattern Analysis and
 * Machine Intelligence, vol. 24, no. 4, pp. 509-522, Apr. 2002
 * https://www.cs.berkeley.edu/~malik/papers/BMP-shape.pdf
 *
 *
 */
public class ShapemeAggregator extends net.semanticmetadata.lire.aggregators.AbstractAggregator {
    private int[] histogram;


    public void createVisualWords(List<? extends net.semanticmetadata.lire.imageanalysis.features.LocalFeature> list, net.semanticmetadata.lire.classifiers.Cluster[] clusters) {
        histogram = new int[clusters.length];
        Arrays.fill(histogram, 0);
        for (net.semanticmetadata.lire.imageanalysis.features.LocalFeature listOfLocalFeature : list) {
            histogram[clusterForFeature(listOfLocalFeature.getFeatureVector(), clusters)]++;
        }
    }

    @Override
    public void createVectorRepresentation(List<? extends net.semanticmetadata.lire.imageanalysis.features.LocalFeature> list, net.semanticmetadata.lire.classifiers.Cluster[] clusters) {
        createVisualWords(list,clusters);
    }

    @Override
    public byte[] getByteVectorRepresentation() {
         return net.semanticmetadata.lire.utils.SerializationUtils.toByteArray(histogram);
    }

    @Override
    public String getStringVectorRepresentation() {
        return arrayToVisualWordString(histogram);
    }

    @Override
    public double[] getVectorRepresentation() {
        double[] histTmp = new double[histogram.length];
        for (int i : histogram) {
            histTmp[i] = histogram[i];
        }
        return histTmp;
    }

    @Override
    public String getFieldName() {
        return ShapeContext.SHAPE_CONTEXT_FIELD;
    }

    /**
     * Returns the vector representation in string format.
     * @return the vector representation as string.
     */
    public String toString() {
        throw new UnsupportedOperationException();
    }

    private String arrayToVisualWordString(int[] data) {
        StringBuilder sb = new StringBuilder(1024);
        int visualWordIndex;
        for (int i = 0; i < data.length; i++) {
            visualWordIndex = data[i];
            for (int j = 0; j < visualWordIndex; j++) {
                sb.append(Integer.toHexString(i));
                sb.append(' ');
            }
        }
        return sb.toString();
    }
}
