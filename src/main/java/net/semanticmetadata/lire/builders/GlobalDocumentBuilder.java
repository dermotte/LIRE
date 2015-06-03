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
 */

package net.semanticmetadata.lire.builders;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import net.semanticmetadata.lire.indexers.hashing.LocalitySensitiveHashing;
import net.semanticmetadata.lire.indexers.parallel.ExtractorItem;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.util.BytesRef;

import java.awt.image.BufferedImage;
import java.util.*;

/**
 * Created by Nektarios on 03/06/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class GlobalDocumentBuilder implements DocumentBuilder {
    public enum HashingMode {BitSampling, LSH}
    private HashingMode hashingMode = HashingMode.BitSampling;
    private boolean hashingEnabled = false;

    private HashMap<ExtractorItem, String[]> extractorItems = new HashMap<ExtractorItem, String[]>(10);
    private boolean docsCreated = false;

    public GlobalDocumentBuilder() {
    }

    public GlobalDocumentBuilder(boolean hashing) {
        this.hashingEnabled = hashing;
        if (hashingEnabled) testHashes();
    }

    public GlobalDocumentBuilder(Class<? extends GlobalFeature> globalFeatureClass) {
        addExtractor(globalFeatureClass);
    }
    public GlobalDocumentBuilder(Class<? extends GlobalFeature> globalFeatureClass, boolean hashing) {
        addExtractor(globalFeatureClass);
        this.hashingEnabled = hashing;
        if (hashingEnabled) testHashes();
    }

    public GlobalDocumentBuilder(ExtractorItem extractorItem) {
        addExtractor(extractorItem);
    }


    public void addExtractor(Class<? extends GlobalFeature> globalFeatureClass) {
        addExtractor(new ExtractorItem(globalFeatureClass));
    }

    public void addExtractor(ExtractorItem extractorItem) {
        if (docsCreated) throw new UnsupportedOperationException("Cannot modify builder after documents have been created!");
        if (!extractorItem.isGlobal()) throw new UnsupportedOperationException("ExtractorItem must contain GlobalFeature");

        String fieldName = extractorItem.getFieldName();
        extractorItems.put(extractorItem, new String[]{fieldName, fieldName + DocumentBuilder.HASH_FIELD_SUFFIX});
    }

    private static void testHashes(){
//        Let's try to read the hash functions right here and we don't have to care about it right now.
        try {
            BitSampling.readHashFunctions();
//            LocalitySensitiveHashing.readHashFunctions();
        } catch (Exception e) {
            System.err.println("Could not read hashes from file when first creating a GenericDocumentBuilder instance.");
            e.printStackTrace();
        }
    }

    public GlobalFeature extractGlobalFeature(BufferedImage image, GlobalFeature globalFeature) {
        assert (image != null);
        // Scaling image is especially with the correlogram features very important!
        // All images are scaled to guarantee a certain upper limit for indexing.
        if (Math.max(image.getHeight(), image.getWidth()) > DocumentBuilder.MAX_IMAGE_DIMENSION) {
            image = ImageUtils.scaleImage(image, DocumentBuilder.MAX_IMAGE_DIMENSION);
        }

        globalFeature.extract(image);
        return globalFeature;
    }

    private Field[] getGlobalDescriptorFields(BufferedImage image, ExtractorItem extractorItem) {
        Field[] result;
        if (hashingEnabled) result = new Field[2];
        else result = new Field[1];

        GlobalFeature globalFeature = extractGlobalFeature(image, (GlobalFeature) extractorItem.getExtractorInstance());

        // TODO: Stored field is compressed and upon search decompression takes a lot of time (> 50% with a small index with 50k images). Find something else ...
        result[0] = new StoredField(extractorItems.get(extractorItem)[0], new BytesRef(globalFeature.getByteArrayRepresentation()));

        // if BitSampling is an issue we add a field with the given hashFunctionsFileName and the suffix "hash":
        if (hashingEnabled) {
            // TODO: check eventually if there is a more compressed string version of the integers. i.e. the hex string
            if (globalFeature.getFeatureVector().length <= 3100) {
                int[] hashes;
                if (hashingMode == HashingMode.BitSampling) {
                    hashes = BitSampling.generateHashes(globalFeature.getFeatureVector());
                } else {
                    hashes = LocalitySensitiveHashing.generateHashes(globalFeature.getFeatureVector());
                }
                result[1] = new TextField(extractorItems.get(extractorItem)[1], SerializationUtils.arrayToString(hashes), Field.Store.YES);
            } else
                System.err.println("Could not create hashes, feature vector too long: " + globalFeature.getFeatureVector().length + " (" + globalFeature.getClass().getName() + ")");
        }
        return result;
    }



    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        docsCreated = true;
        LinkedList<Field> resultList = new LinkedList<Field>();
        Field[] fields;
        if (extractorItems.size() > 0) {
            for (Map.Entry<ExtractorItem, String[]> extractorItemEntry : extractorItems.entrySet()) {
                fields = getGlobalDescriptorFields(image, extractorItemEntry.getKey());

                Collections.addAll(resultList, fields);
            }
        }

        return resultList.toArray(new Field[resultList.size()]);
    }

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
