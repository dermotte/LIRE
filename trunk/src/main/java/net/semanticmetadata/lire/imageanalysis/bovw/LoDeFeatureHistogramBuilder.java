package net.semanticmetadata.lire.imageanalysis.bovw;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.imageanalysis.ScalableColor;
import org.apache.lucene.index.IndexReader;

/**
 * Created by mlux_2 on 13.06.2014.
 */
public class LoDeFeatureHistogramBuilder extends LocalFeatureHistogramBuilder {
    LireFeature lireFeature = new ScalableColor();

    public LoDeFeatureHistogramBuilder(IndexReader reader) {
        super(reader);
        init();
    }

    public LoDeFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary) {
        super(reader, numDocsForVocabulary);
        init();
    }

    public LoDeFeatureHistogramBuilder(IndexReader reader, int numDocsForVocabulary, int numClusters) {
        super(reader, numDocsForVocabulary, numClusters);
        init();
    }

    private void init() {
        localFeatureFieldName = lireFeature.getFieldName();
        visualWordsFieldName = lireFeature.getFieldName()+"LoDe";
        localFeatureHistFieldName = lireFeature.getFieldName()+"LoDe_Hist";
        clusterFile = "./clusters-lode.dat";
    }

    @Override
    protected LireFeature getFeatureInstance() {
        LireFeature result = null;
        try {
            result =  lireFeature.getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }
}
