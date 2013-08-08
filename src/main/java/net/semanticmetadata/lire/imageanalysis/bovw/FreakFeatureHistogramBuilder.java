package net.semanticmetadata.lire.imageanalysis.bovw;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.FreakFeature;
import net.semanticmetadata.lire.imageanalysis.LireFeature;

import org.apache.lucene.index.IndexReader;

/**
 * LocalFeatureHistogramBuilder for FREAK features.
 * 
 * @author Mario Taschwer
 * @version $Id$
 */
public class FreakFeatureHistogramBuilder extends LocalFeatureHistogramBuilder 
{
    public FreakFeatureHistogramBuilder(IndexReader reader) {
        super(reader);
        init();
    }

    public FreakFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary) {
        super(reader, numDocsForVocabulary);
        init();
    }

    public FreakFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary, int numClusters) {
        super(reader, numDocsForVocabulary, numClusters);
        init();
    }

    @Override
    protected LireFeature getFeatureInstance() {
        return new FreakFeature();
    }

    private void init() {
        localFeatureFieldName = DocumentBuilder.FIELD_NAME_FREAK;
        visualWordsFieldName = DocumentBuilder.FIELD_NAME_FREAK_VISUAL_WORDS;
        localFeatureHistFieldName = DocumentBuilder.FIELD_NAME_FREAK_LOCAL_FEATURE_HISTOGRAM;
        clusterFile = "./clusters-freak.dat";
    }
}
