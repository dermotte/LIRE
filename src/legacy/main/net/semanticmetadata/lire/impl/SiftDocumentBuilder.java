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
 * Updated: 21.04.13 09:06
 */
package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.sift.Extractor;
import net.semanticmetadata.lire.imageanalysis.sift.Feature;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;

/**
 * ...
 * Date: 23.09.2008
 * Time: 12:05:08
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SiftDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(getClass().getName());
    private Extractor extractor;

    public SiftDocumentBuilder() {
        extractor = new Extractor();
    }

    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        Field[] result = null;
        try {
            // extract features from image:
            List<Feature> features = extractor.computeSiftFeatures(image);
            result = new Field[features.size()];
            int count = 0;
            // create new document:
            for (Iterator<Feature> fit = features.iterator(); fit.hasNext(); ) {
                Feature f = fit.next();
                result[count] = new StoredField(DocumentBuilder.FIELD_NAME_SIFT, f.getByteArrayRepresentation());
                count++;
            }
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return result;
    }

    public Document createDocument(BufferedImage image, String identifier) {
        Document doc = null;
        try {
            // extract features from image:
            List<Feature> features = extractor.computeSiftFeatures(image);
            // create new document:
            doc = new Document();
            for (Iterator<Feature> fit = features.iterator(); fit.hasNext(); ) {
                Feature f = fit.next();
                // add each feature to the document:
                doc.add(new StoredField(DocumentBuilder.FIELD_NAME_SIFT, f.getByteArrayRepresentation()));
            }
            if (identifier != null)
                doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
        } catch (IOException e) {
            logger.severe(e.getMessage());
        }
        return doc;
    }
}
