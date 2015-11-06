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

package net.semanticmetadata.lire.builders;

import net.semanticmetadata.lire.aggregators.AbstractAggregator;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.indexers.parallel.ExtractorItem;

import java.util.HashMap;
import java.util.LinkedList;

/**
 * This class can be used as the LocalDocumentBuilder class, in order to create Lucene Documents
 * according to the SIMPLE model (see features.local.simple package)
 * Created by Nektarios on 03/06/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class SimpleDocumentBuilder extends AbstractLocalDocumentBuilder {

    public SimpleDocumentBuilder() {
    }

    public SimpleDocumentBuilder(Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector keypointDetector, Cluster[] codebook) {
        addExtractor(globalFeatureClass, keypointDetector, codebook);
    }

    public SimpleDocumentBuilder(Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector keypointDetector, LinkedList<Cluster[]> listOfCodebooks) {
        addExtractor(globalFeatureClass,keypointDetector, listOfCodebooks);
    }

    public SimpleDocumentBuilder(ExtractorItem extractorItem, Cluster[] codebook) {
        addExtractor(extractorItem, codebook);
    }

    public SimpleDocumentBuilder(ExtractorItem extractorItem, LinkedList<Cluster[]> listOfCodebooks) {
        addExtractor(extractorItem, listOfCodebooks);
    }

    public SimpleDocumentBuilder(Class<? extends AbstractAggregator> aggregatorClass) {
        try {
            this.aggregator = aggregatorClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    public SimpleDocumentBuilder(Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector keypointDetector, Cluster[] codebook, Class<? extends AbstractAggregator> aggregatorClass) {
        try {
            this.aggregator = aggregatorClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        addExtractor(globalFeatureClass, keypointDetector, codebook);
    }

    public SimpleDocumentBuilder(Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector keypointDetector, LinkedList<Cluster[]> listOfCodebooks, Class<? extends AbstractAggregator> aggregatorClass) {
        try {
            this.aggregator = aggregatorClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        addExtractor(globalFeatureClass, keypointDetector, listOfCodebooks);
    }

    public SimpleDocumentBuilder(ExtractorItem extractorItem, Cluster[] codebook, Class<? extends AbstractAggregator> aggregatorClass) {
        try {
            this.aggregator = aggregatorClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        addExtractor(extractorItem, codebook);
    }

    public SimpleDocumentBuilder(ExtractorItem extractorItem, LinkedList<Cluster[]> listOfCodebooks, Class<? extends AbstractAggregator> aggregatorClass) {
        try {
            this.aggregator = aggregatorClass.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        addExtractor(extractorItem, listOfCodebooks);
    }

    /**
     * Can be used to add a global extractor with a {@link SimpleExtractor.KeypointDetector}.
     * @param globalFeatureClass
     * @param keypointDetector
     * @param codebook
     */
    public void addExtractor(Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector keypointDetector, Cluster[] codebook) {
        if ((!(codebook.length>0))||(codebook == null)) throw new UnsupportedOperationException("Codebook cannot be empty or null!!");
        LinkedList<Cluster[]> listOfCodebooks = new LinkedList<Cluster[]>();
        listOfCodebooks.add(codebook);
        addExtractor(new ExtractorItem(globalFeatureClass, keypointDetector), listOfCodebooks);
    }

    /**
     * Can be used to add a global extractor with a {@link SimpleExtractor.KeypointDetector}.
     * @param extractorItem
     * @param codebook
     */
    public void addExtractor(ExtractorItem extractorItem, Cluster[] codebook) {
        if ((!(codebook.length>0))||(codebook == null)) throw new UnsupportedOperationException("Codebook cannot be empty or null!!");
        LinkedList<Cluster[]> listOfCodebooks = new LinkedList<Cluster[]>();
        listOfCodebooks.add(codebook);
        addExtractor(extractorItem, listOfCodebooks);
    }

    /**
     * Can be used to add a global extractor with a {@link SimpleExtractor.KeypointDetector}.
     * @param globalFeatureClass
     * @param keypointDetector
     * @param listOfCodebooks
     */
    public void addExtractor(Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector keypointDetector, LinkedList<Cluster[]> listOfCodebooks) {
        if ((!(listOfCodebooks.size()>0))||(listOfCodebooks == null)) throw new UnsupportedOperationException("List of codebooks cannot be empty or null!!");
        addExtractor(new ExtractorItem(globalFeatureClass, keypointDetector), listOfCodebooks);
    }

    /**
     * Can be used to add a global extractor with a {@link SimpleExtractor.KeypointDetector}.
     * @param extractorItem
     * @param listOfCodebooks
     */
    public void addExtractor(ExtractorItem extractorItem, LinkedList<Cluster[]> listOfCodebooks) {
        if (docsCreated) throw new UnsupportedOperationException("Cannot modify builder after documents have been created!");
        if (!extractorItem.isSimple()) throw new UnsupportedOperationException("ExtractorItem must be SIMPLE");
        if ((!(listOfCodebooks.size()>0))||(listOfCodebooks == null)) throw new UnsupportedOperationException("List of codebooks cannot be empty or null!!");

        HashMap<Integer, String[]> mapOfFieldNames = new HashMap<Integer, String[]>(listOfCodebooks.size());
        String fieldName = extractorItem.getFieldName() + aggregator.getFieldName();

        String[] strArr;
        for (Cluster[] codebook : listOfCodebooks) {
            strArr = new String[2];
            strArr[0] = fieldName + codebook.length;
            strArr[1] = fieldName + codebook.length + "Str";
            mapOfFieldNames.put(codebook.length, strArr);
        }

        extractorItems.put(extractorItem, listOfCodebooks);
        fieldNamesDictionary.put(extractorItem, mapOfFieldNames);
    }

}
