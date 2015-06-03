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
 * Updated: 21.04.13 09:01
 */
package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.mser.MSER;
import net.semanticmetadata.lire.imageanalysis.mser.MSERFeature;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

/**
 * Creates a Document out of the given image with MSER Features
 * Date: 27.03.2011
 * Time: 10:00:08
 *
 * @author Christine Keim, christine.keim@inode.at
 */
public class MSERDocumentBuilder extends AbstractDocumentBuilder {
    private MSER extractor;

    public MSERDocumentBuilder() {
        extractor = new MSER();
    }

    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        throw new UnsupportedOperationException("createDescriptorFields(BufferedImage image) not implemented for MSER!");
    }

    public Document createDocument(BufferedImage image, String identifier) {
        Document doc = null;
        try {
            // convert to grey ...
            BufferedImage image1 = convertImageToGrey(image);
            // extract features from image:
            List<MSERFeature> features = extractor.computeMSERFeatures(image1);

            ImageUtils.invertImage(image1);
            // invert grey
            features.addAll(extractor.computeMSERFeatures(image1));


            // create new document:
            doc = new Document();
            if (features.size() < 1) {
                System.err.println("No MSER features found for " + identifier);
//            } else {
//                System.out.println("features.size() = " + features.size());
            }
            for (Iterator<MSERFeature> fit = features.iterator(); fit.hasNext(); ) {
                MSERFeature f = fit.next();
                boolean skip = false;
                // add each feature to the document:
                // check first if NaN!!
                for (int j = 0; j < f.getDoubleHistogram().length; j++) {
                    if (Double.isNaN(f.getDoubleHistogram()[j])) skip = true;
                    break;
                }

                if (!skip)
                    doc.add(new StoredField(DocumentBuilder.FIELD_NAME_MSER, f.getByteArrayRepresentation()));
                else {
//                    System.err.println("Found NaN in features in file " + identifier + ". ");
                }
            }
            if (identifier != null) {
                doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return doc;
    }

    public BufferedImage convertImageToGrey(BufferedImage image) {
        BufferedImage result = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_BYTE_GRAY);
        result.getGraphics().drawImage(image, 0, 0, null);
        return result;
    }
}
