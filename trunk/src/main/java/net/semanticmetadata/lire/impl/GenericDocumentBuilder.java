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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 23.06.13 18:57
 */
package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.indexing.hashing.LocalitySensitiveHashing;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.*;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.logging.Logger;

/**
 * This class allows to create a DocumentBuilder based on a class implementing LireFeature.
 * Date: 28.05.2008
 * Time: 14:32:15
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author sangupta, sandy.pec@gmail.com (fixed null pointer exception)
 */
public class GenericDocumentBuilder extends AbstractDocumentBuilder {
    enum HashingMode {BitSampling, LSH}
    private boolean hashingEnabled = false;
    private Logger logger = Logger.getLogger(getClass().getName());
    public static final int MAX_IMAGE_DIMENSION = 1024;
    Class<? extends LireFeature> descriptorClass;
    String fieldName;
    final static Mode DEFAULT_MODE = Mode.Fast;
    Mode currentMode = DEFAULT_MODE;
    // private LireFeature lireFeature;
    protected static HashingMode hashingMode = HashingMode.BitSampling;

    static {
        // Let's try to read the hash functions right here and we don't have to care about it right now.
        try {
            BitSampling.readHashFunctions();
//            LocalitySensitiveHashing.readHashFunctions();
        } catch (IOException e) {
            System.err.println("Could not read hashes from file when first creating a GenericDocumentBuilder instance.");
            e.printStackTrace();
        }
    }

    // Decide between byte array version (fast) or string version (slow)
    public enum Mode {
        Fast, Slow
    }

    /**
     * Creating a new DocumentBuilder based on a class based on the interface {@link net.semanticmetadata.lire.imageanalysis.LireFeature}
     *
     * @param descriptorClass has to implement {@link net.semanticmetadata.lire.imageanalysis.LireFeature}
     * @param fieldName       the field hashFunctionsFileName in the index.
     */
    public GenericDocumentBuilder(Class<? extends LireFeature> descriptorClass, String fieldName) {
        this.descriptorClass = descriptorClass;
        this.fieldName = fieldName;
    }

    /**
     * Creating a new DocumentBuilder based on a class based on the interface {@link net.semanticmetadata.lire.imageanalysis.LireFeature}
     *
     * @param descriptorClass has to implement {@link net.semanticmetadata.lire.imageanalysis.LireFeature}
     * @param fieldName       The hashFunctionsFileName of the field, where the feature vector is stored.
     * @param hashing         set to true is you want to create an additional field for hashes based on BitSampling.
     */
    public GenericDocumentBuilder(Class<? extends LireFeature> descriptorClass, String fieldName, boolean hashing) {
        this.descriptorClass = descriptorClass;
        this.fieldName = fieldName;
        hashingEnabled = hashing;
    }

    /**
     * Creating a new DocumentBuilder based on a class based on the interface {@link net.semanticmetadata.lire.imageanalysis.LireFeature}
     *
     * @param descriptorClass has to implement {@link net.semanticmetadata.lire.imageanalysis.LireFeature}
     * @param fieldName       the field hashFunctionsFileName in the index.
     * @param mode            the mode the GenericDocumentBuilder should work in, byte[] (== Mode.Fast) or string (==Mode.Slow) storage in Lucene.
     */
    public GenericDocumentBuilder(Class<? extends LireFeature> descriptorClass, String fieldName, Mode mode) {
        this.descriptorClass = descriptorClass;
        this.fieldName = fieldName;
        this.currentMode = mode;
    }

    public Field[] createDescriptorFields(BufferedImage image) {
        Field[] result;
        if (hashingEnabled) result = new Field[2];
        else result = new Field[1];
        String featureString = "";
        assert (image != null);
        BufferedImage bimg = image;
        // Scaling image is especially with the correlogram features very important!
        // All images are scaled to guarantee a certain upper limit for indexing.
        if (Math.max(image.getHeight(), image.getWidth()) > MAX_IMAGE_DIMENSION) {
            bimg = ImageUtils.scaleImage(image, MAX_IMAGE_DIMENSION);
        }
        Document doc = null;
        try {
            logger.finer("Starting extraction from image [" + descriptorClass.getName() + "].");
            LireFeature lireFeature = null;

            lireFeature = descriptorClass.newInstance();

            lireFeature.extract(bimg);
//            featureString = vd.getStringRepresentation();
            logger.fine("Extraction finished [" + descriptorClass.getName() + "].");

            // TODO: Stored field is compressed and upon search decompression takes a lot of time (> 50% with a small index with 50k images). Find something else ...
            result[0] = new StoredField(fieldName, lireFeature.getByteArrayRepresentation());

            // if BitSampling is an issue we add a field with the given hashFunctionsFileName and the suffix "hash":
            if (hashingEnabled) {
                // TODO: check eventually if there is a more compressed string version of the integers. i.e. the hex string
                if (lireFeature.getDoubleHistogram().length <= 3100) {
                    int[] hashes;
                    if (hashingMode == HashingMode.BitSampling) {
                        hashes = BitSampling.generateHashes(lireFeature.getDoubleHistogram());
                    } else {
                        hashes = LocalitySensitiveHashing.generateHashes(lireFeature.getDoubleHistogram());
                    }
                    result[1] = new TextField(fieldName + "_hash", SerializationUtils.arrayToString(hashes), Field.Store.YES);
                } else
                    System.err.println("Could not create hashes, feature vector too long: " + lireFeature.getDoubleHistogram().length + " (" + lireFeature.getClass().getName() + ")");
            }
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * Creates a fully fledged Document to be added to a Lucene index.
     * @param image      the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or an URL. Can be NULL.
     * @return
     */
    public Document createDocument(BufferedImage image, String identifier) {
        assert (image != null);
        
        // sangupta: create a new document else code below
        // will throw a NPE
        Document doc = new Document();
        
        if (identifier != null) {
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
        }
        
        Field[] fields = createDescriptorFields(image);
        for (int i = 0; i < fields.length; i++) {
            doc.add(fields[i]);
        }
        
        return doc;
    }
}
