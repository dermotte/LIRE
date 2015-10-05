package net.semanticmetadata.lire.imageanalysis.features.local.shapecontext;

/**
 * Created by Lukas Knoch on 16.09.15.
 *
 * This class represents a ShapeContext feature. The comparison for this local feature
 * is not implemented as it is only used for creating the Shapeme descriptor.
 */
public class ShapeContext implements net.semanticmetadata.lire.imageanalysis.features.LocalFeature {
    public static final String SHAPE_CONTEXT_FIELD = "ShapeContext";
    private double[] histogram;
    private double x;
    private double y;

    /**
     * needed for instantiation via getInstance()
     */
    public ShapeContext(){
    }

    public ShapeContext(double[] histogram, double x, double y) {
        this.histogram = histogram;
        this.x = x;
        this.y = y;
    }

    /**
     * not preserved over byte array representation
     * @return
     */
    @Override
    public double getX() {
        return x;
    }

    /**
     * not preserved over byte array representation
     * @return
     */
    @Override
    public double getY() {
        return y;
    }

    /**
     * not used
     * @return
     */
    @Override
    public double getSize() {
        throw new RuntimeException("not implemented");
    }

    @Override
    public Class<?> getClassOfExtractor() {
        return ShapeContextExtractor.class;
    }

    @Override
    public String getFeatureName() {
        return ShapeContext.class.getSimpleName();
    }

    @Override
    public String getFieldName() { //
        return SHAPE_CONTEXT_FIELD;
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        return net.semanticmetadata.lire.utils.SerializationUtils.toByteArray(histogram);
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setByteArrayRepresentation(byte[] bytes, int i, int i1) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double getDistance(net.semanticmetadata.lire.imageanalysis.features.LireFeature lireFeature) {
        throw new UnsupportedOperationException();
    }

    @Override
    public double[] getFeatureVector() {
        return histogram;
    }
}
