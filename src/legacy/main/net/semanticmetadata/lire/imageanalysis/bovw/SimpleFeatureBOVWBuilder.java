/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.imageanalysis.bovw;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.impl.SimpleBuilder;
import org.apache.lucene.index.IndexReader;

/**
 * Created by mlux_2 on 13.06.2014.
 */
//public class SimpleFeatureBOVWBuilder extends LocalFeatureHistogramBuilderFromCodeBook {
public class SimpleFeatureBOVWBuilder extends BOVWBuilder {
    private SimpleBuilder.KeypointDetector detector;

    public SimpleFeatureBOVWBuilder(IndexReader reader) {
        super(reader);
    }

    public SimpleFeatureBOVWBuilder(IndexReader reader, int numDocsForVocabulary) {
        super(reader, numDocsForVocabulary);
    }

    public SimpleFeatureBOVWBuilder(IndexReader reader, int numDocsForVocabulary, int numClusters) {
        super(reader, numDocsForVocabulary, numClusters);
    }

    public SimpleFeatureBOVWBuilder(IndexReader reader, LireFeature lireFeature, SimpleBuilder.KeypointDetector detector) {
        super(reader, lireFeature);
        this.detector = detector;
    }

    public SimpleFeatureBOVWBuilder(IndexReader reader, LireFeature lireFeature, SimpleBuilder.KeypointDetector detector, int numDocsForVocabulary) {
        super(reader, lireFeature, numDocsForVocabulary);
        this.detector = detector;
    }

    public SimpleFeatureBOVWBuilder(IndexReader reader, LireFeature lireFeature, SimpleBuilder.KeypointDetector detector, int numDocsForVocabulary, int numClusters) {
        super(reader, lireFeature, numDocsForVocabulary, numClusters);
        this.detector = detector;
    }



    @Override
    protected void init() {
        String fname = (new SimpleBuilder()).getFieldName(detector, getFeatureInstance());
        localFeatureFieldName = fname;
        visualWordsFieldName = fname + DocumentBuilder.FIELD_NAME_BOVW;
        localFeatureHistFieldName = fname + DocumentBuilder.FIELD_NAME_BOVW_VECTOR;
        clusterFile = "./clusters-simpleBovw" + lireFeature.getFeatureName() + (new SimpleBuilder()).getDetector(detector).replace("det","") + ".dat";
    }
}
