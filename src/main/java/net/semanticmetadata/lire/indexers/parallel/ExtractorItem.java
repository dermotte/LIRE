/*
 * This file is part of the LIRE project: http://lire-project.net
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
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval -
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
 */

package net.semanticmetadata.lire.indexers.parallel;

import net.semanticmetadata.lire.imageanalysis.features.Extractor;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;

/**
 * This class is a container for all features.
 * It contains the class of the extractor, an instance of the extractor, an instance of the feature, the field name of the feature
 * and for the simple model, the keypoint detector. (In case of global features the extractor and the feature instances, are
 * the same object).
 * Created by Nektarios on 6/5/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class ExtractorItem {
    private Class<? extends Extractor> extractorClass;
    private Extractor extractorInstance;
    private LireFeature featureInstance;
    private SimpleExtractor.KeypointDetector keypointDetector;
    private String fieldName;

    private boolean global = false;
    private boolean local = false;
    private boolean simple = false;

    public ExtractorItem(Class<? extends Extractor> extractorClass){
        if (extractorClass == null) throw new UnsupportedOperationException("extractorClass cannot be null");

        this.extractorClass = extractorClass;
        try {
            this.extractorInstance = extractorClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        if ((GlobalFeature.class).isAssignableFrom(extractorClass)){
            this.global = true;
            this.fieldName = ((GlobalFeature) extractorInstance).getFieldName();
            this.featureInstance = (GlobalFeature) extractorInstance;
        }
        else if ((LocalFeatureExtractor.class).isAssignableFrom(extractorClass)){
            this.local = true;
            try {
                this.featureInstance = ((LocalFeatureExtractor) extractorInstance).getClassOfFeatures().newInstance();
                this.fieldName = featureInstance.getFieldName();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        else throw new UnsupportedOperationException("Error");

        this.keypointDetector = null;
    }

    public ExtractorItem(Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector keypointDetector){
        if ((globalFeatureClass == null)||(keypointDetector == null)) throw new UnsupportedOperationException("globalFeature or detector cannot be null");

        this.extractorClass = globalFeatureClass;   //GlobalFeature!!
        try {
            this.extractorInstance = new SimpleExtractor(globalFeatureClass.newInstance(), keypointDetector);
            this.featureInstance = ((SimpleExtractor) extractorInstance).getClassOfFeatures().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.fieldName = ((SimpleExtractor) extractorInstance).getFieldName();
        this.keypointDetector = keypointDetector;
        this.simple = true;
    }

    public Class<? extends Extractor> getExtractorClass(){
        return extractorClass;
    }

    public Extractor getExtractorInstance(){
        return extractorInstance;
    }

    public LireFeature getFeatureInstance(){
        return featureInstance;
    }

    public SimpleExtractor.KeypointDetector getKeypointDetector(){
        return keypointDetector;
    }

    public String getFieldName() { return fieldName; }

    public boolean isGlobal(){ return global; }

    public boolean isLocal(){ return local; }

    public boolean isSimple(){ return simple; }

    public ExtractorItem clone(){
        ExtractorItem clone;

        if (simple)
            clone = new ExtractorItem((Class<? extends GlobalFeature>) extractorClass, keypointDetector);
        else
            clone = new ExtractorItem(extractorClass);

        return clone;
    }
}
