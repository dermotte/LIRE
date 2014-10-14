package net.semanticmetadata.lire.imageanalysis.bovw;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSurfFeature;
import org.apache.lucene.index.IndexReader;

/**
 * Created by Nektarios on 3/10/2014.
 */
public class CvSurfFeatureHistogramBuilder extends LocalFeatureHistogramBuilder {
    public CvSurfFeatureHistogramBuilder(IndexReader reader) {
        super(reader);
        init();
    }

    public CvSurfFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary) {
        super(reader, numDocsForVocabulary);
        init();
    }

    public CvSurfFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary, int numClusters) {
        super(reader, numDocsForVocabulary, numClusters);
        init();
    }

    @Override
    protected LireFeature getFeatureInstance() {
        return new CvSurfFeature();
    }

    private void init() {
        localFeatureFieldName = DocumentBuilder.FIELD_NAME_CVSURF;
        visualWordsFieldName = DocumentBuilder.FIELD_NAME_CVSURF_VISUAL_WORDS;
        localFeatureHistFieldName = DocumentBuilder.FIELD_NAME_CVSURF_LOCAL_FEATURE_HISTOGRAM;
        clusterFile = "./clusters-cvsurf.dat";
    }
}
