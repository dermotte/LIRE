package net.semanticmetadata.lire.imageanalysis.features.local.shapecontext;

import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Created by Lukas Knoch on 23.09.15.
 * This class contains the histogram representation of the ShapeContext feature. It is actually a global feature aggregated
 * by many local features, but needs to be a local feature as that is required by the current indexer and searcher configuration.
 */
public class ShapemeHistogram implements net.semanticmetadata.lire.imageanalysis.features.LocalFeature {

    int[] histogram;
    @Override
    public String getFeatureName() {
        return ShapeContext.class.getSimpleName();
    }

    @Override
    public String getFieldName() {
        return ShapeContext.SHAPE_CONTEXT_FIELD;
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        return net.semanticmetadata.lire.utils.SerializationUtils.toByteArray(histogram);
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        histogram = net.semanticmetadata.lire.utils.SerializationUtils.toIntArray(bytes);
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes, int i, int i1) {
        histogram = net.semanticmetadata.lire.utils.SerializationUtils.toIntArray(bytes);
    }

    @Override
    public double getDistance(net.semanticmetadata.lire.imageanalysis.features.LireFeature lireFeature) {
        if(!(lireFeature instanceof  ShapemeHistogram)){
            throw new RuntimeException("not implemented");
        }
        return net.semanticmetadata.lire.utils.MetricsUtils.distL1(((ShapemeHistogram) lireFeature).histogram, histogram);
    }

    @Override
    public double[] getFeatureVector() {
        double[] tmpHist = new double[histogram.length];
        for (int i = 0; i < histogram.length; i++) {
            tmpHist[i] = histogram[i];
        }
        return tmpHist;
    }

    @Override
    public double getX() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getY() {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getSize() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Class<?> getClassOfExtractor() {
        return ShapeContextExtractor.class;
    }
}
