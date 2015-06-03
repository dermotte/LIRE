package net.semanticmetadata.lire.imageanalysis.bovw;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.impl.SimpleBuilder;
import org.apache.lucene.index.IndexReader;

/**
 * Created by Nektarios on 04/12/2014.
 */
public class SimpleFeatureVLADBuilder extends VLADBuilder{
    private SimpleBuilder.KeypointDetector detector;

    public SimpleFeatureVLADBuilder(IndexReader reader) {
        super(reader);
    }

    public SimpleFeatureVLADBuilder(IndexReader reader, int numDocsForVocabulary) {
        super(reader, numDocsForVocabulary);
    }

    public SimpleFeatureVLADBuilder(IndexReader reader, int numDocsForVocabulary, int numClusters) {
        super(reader, numDocsForVocabulary, numClusters);
    }

    public SimpleFeatureVLADBuilder(IndexReader reader, LireFeature lireFeature, SimpleBuilder.KeypointDetector detector) {
        super(reader, lireFeature);
        this.detector = detector;
    }

    public SimpleFeatureVLADBuilder(IndexReader reader, LireFeature lireFeature, SimpleBuilder.KeypointDetector detector, int numDocsForVocabulary) {
        super(reader, lireFeature, numDocsForVocabulary);
        this.detector = detector;
    }

    public SimpleFeatureVLADBuilder(IndexReader reader, LireFeature lireFeature, SimpleBuilder.KeypointDetector detector, int numDocsForVocabulary, int numClusters) {
        super(reader, lireFeature, numDocsForVocabulary, numClusters);
        this.detector = detector;
    }

    @Override
    protected void init() {
        String fname = (new SimpleBuilder()).getFieldName(detector, getFeatureInstance());
        localFeatureFieldName = fname;
        vladFieldName = fname + DocumentBuilder.FIELD_NAME_VLAD;
        vladHistFieldName = fname + DocumentBuilder.FIELD_NAME_VLAD_VECTOR;
        clusterFile = "./clusters-simpleVlad" + lireFeature.getFeatureName() + (new SimpleBuilder()).getDetector(detector).replace("det","") + ".dat";
    }
}
