package net.semanticmetadata.lire.imageanalysis.bovw;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSiftFeature;
import org.apache.lucene.index.IndexReader;
/**
 * Created by Nektarios on 3/10/2014.
 */
public class CvSiftFeatureHistogramBuilder extends LocalFeatureHistogramBuilder {
    public CvSiftFeatureHistogramBuilder(IndexReader reader) {
        super(reader);
        init();
    }

    public CvSiftFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary) {
        super(reader, numDocsForVocabulary);
        init();
    }

    public CvSiftFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary, int numClusters) {
        super(reader, numDocsForVocabulary, numClusters);
        init();
    }

    private void init() {
        localFeatureFieldName = DocumentBuilder.FIELD_NAME_CVSIFT;
        visualWordsFieldName = DocumentBuilder.FIELD_NAME_CVSIFT_VISUAL_WORDS;
        localFeatureHistFieldName = DocumentBuilder.FIELD_NAME_CVSIFT_LOCAL_FEATURE_HISTOGRAM;
        clusterFile = "./clusters-cvsift.dat";
    }

    @Override
    protected LireFeature getFeatureInstance() {
        return new CvSiftFeature();
    }
}
