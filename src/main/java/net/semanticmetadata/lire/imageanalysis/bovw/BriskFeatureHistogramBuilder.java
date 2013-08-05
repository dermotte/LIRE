package net.semanticmetadata.lire.imageanalysis.bovw;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.BriskFeature;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.imageanalysis.bovw.LocalFeatureHistogramBuilder;

import org.apache.lucene.index.IndexReader;

/**
 * LocalFeatureHistogramBuilder for BRISK features.
 * 
 * @author Mario Taschwer
 * @version $Id$
 */
public class BriskFeatureHistogramBuilder extends LocalFeatureHistogramBuilder 
{
    public BriskFeatureHistogramBuilder(IndexReader reader) {
        super(reader);
        init();
    }

    public BriskFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary) {
        super(reader, numDocsForVocabulary);
        init();
    }

    public BriskFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary, int numClusters) {
        super(reader, numDocsForVocabulary, numClusters);
        init();
    }

    @Override
    protected LireFeature getFeatureInstance() {
        return new BriskFeature();
    }

    private void init() {
        localFeatureFieldName = DocumentBuilder.FIELD_NAME_BRISK;
        visualWordsFieldName = DocumentBuilder.FIELD_NAME_BRISK_VISUAL_WORDS;
        localFeatureHistFieldName = DocumentBuilder.FIELD_NAME_BRISK_LOCAL_FEATURE_HISTOGRAM;
        clusterFile = "./clusters-brisk.dat";
    }
}
