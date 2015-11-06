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
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.indexers.parallel.ExtractorItem;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.*;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * This class is used by the LocalDocumentBuilder and the SimpleDocumentBuilder to create
 * Lucene Documents for Local Features.
 * Created by Nektarios on 03/06/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public abstract class AbstractLocalDocumentBuilder implements DocumentBuilder {
//    enum HashingMode {BitSampling, LSH}
//    protected HashingMode hashingMode = HashingMode.BitSampling;
//    private boolean hashingEnabled = false;

    protected HashMap<ExtractorItem, LinkedList<Cluster[]>> extractorItems = new HashMap<ExtractorItem, LinkedList<Cluster[]>>(10);
    protected HashMap<ExtractorItem, HashMap<Integer, String[]>> fieldNamesDictionary = new HashMap<ExtractorItem, HashMap<Integer, String[]>>(10);
    protected AbstractAggregator aggregator = new BOVW();
    protected boolean docsCreated = false;


    /**
     * Images are resized so as not to exceed the {@link DocumentBuilder#MAX_IMAGE_DIMENSION}, after that
     * features are extracted using the given localFeatureExtractor.
     * @param image is the image
     * @param localFeatureExtractor selected local feature extractor
     * @return the input localFeatureExtractor
     */
    public LocalFeatureExtractor extractLocalFeatures(BufferedImage image, LocalFeatureExtractor localFeatureExtractor) {
        assert (image != null);
        // Scaling image is especially with the correlogram features very important!
        // All images are scaled to guarantee a certain upper limit for indexing.
        if (Math.max(image.getHeight(), image.getWidth()) > DocumentBuilder.MAX_IMAGE_DIMENSION) {
            image = ImageUtils.scaleImage(image, DocumentBuilder.MAX_IMAGE_DIMENSION);
        }

        localFeatureExtractor.extract(image);
        return localFeatureExtractor;
    }

    /**
     * Extracts the features and returns the Lucene Fields with the vector representation of the selected image.
     * @param image is the selected image.
     * @param extractorItem is the selected extractor.
     * @param listOfCodebooks is the list which can contain one or more codebooks to be used for the aggregation of the local features.
     * @return Lucene Fields with the vector representation of the selected image.
     */
    private Field[] getLocalDescriptorFields(BufferedImage image, ExtractorItem extractorItem, LinkedList<Cluster[]> listOfCodebooks) {
        LocalFeatureExtractor localFeatureExtractor = extractLocalFeatures(image, (LocalFeatureExtractor) extractorItem.getExtractorInstance());

        return createLocalDescriptorFields(localFeatureExtractor.getFeatures(), extractorItem, listOfCodebooks);
    }

    /**
     * Creates the Lucene Fiels with the vector representation of list of local features.
     * @param listOfLocalFeatures is the list of local features.
     * @param extractorItem is the extractor that was used to extract the features.
     * @param listOfCodebooks is the list which can contain one or more codebooks to be used for the aggregation of the local features.
     * @return Lucene Fields with the vector representation of the list of local features.
     */
    public Field[] createLocalDescriptorFields(List<? extends LocalFeature> listOfLocalFeatures, ExtractorItem extractorItem, LinkedList<Cluster[]> listOfCodebooks){
        Field[] result = new Field[listOfCodebooks.size() * 2];
        int count = 0;
        for (Cluster[] codebook : listOfCodebooks) {
            aggregator.createVectorRepresentation(listOfLocalFeatures, codebook);
            result[count] = new StoredField(fieldNamesDictionary.get(extractorItem).get(codebook.length)[0], aggregator.getByteVectorRepresentation());
            result[count + 1] = new TextField(fieldNamesDictionary.get(extractorItem).get(codebook.length)[1], aggregator.getStringVectorRepresentation(), Field.Store.YES);
            count += 2;
        }

        return result;
    }

    /**
     * @param image the image to analyze.
     * @return Lucene Fields with the vector representation of the selected image.
     */
    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        docsCreated = true;
        LinkedList<Field> resultList = new LinkedList<Field>();
        Field[] fields;
        if (extractorItems.size() > 0) {
            for (Map.Entry<ExtractorItem, LinkedList<Cluster[]>> extractorItemEntry : extractorItems.entrySet()) {
                fields = getLocalDescriptorFields(image, extractorItemEntry.getKey(), extractorItemEntry.getValue());

                Collections.addAll(resultList, fields);
            }
        }

        return resultList.toArray(new Field[resultList.size()]);
    }

    /**
     * @param image the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or a URL. Can be NULL.
     * @return a Lucene Document.
     */
    @Override
    public Document createDocument(BufferedImage image, String identifier) {
        Document doc = new Document();

        if (identifier != null) {
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
        }

        Field[] fields = createDescriptorFields(image);
        for (Field field : fields) {
            doc.add(field);
        }

        return doc;
    }
}
