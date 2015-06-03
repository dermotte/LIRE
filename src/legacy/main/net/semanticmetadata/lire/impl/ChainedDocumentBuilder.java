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
 * Updated: 26.04.13 13:43
 */

package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.LinkedList;

/**
 * Chaining of DocumentBuilder. If you need several different feature, create a ChainedDocumentBuilder and add
 * different DocumentBuilder instances with {@link ChainedDocumentBuilder#addBuilder(net.semanticmetadata.lire.DocumentBuilder)}
 * @author Mathias Lux, mathias@juggle.at, 20.02.2007
 */
public class ChainedDocumentBuilder extends AbstractDocumentBuilder {
    private LinkedList<DocumentBuilder> builders;
    private boolean docsCreated = false;

    /**
     * Creates a new, empty ChainedDocumentBuilder.
     */
    public ChainedDocumentBuilder() {
        builders = new LinkedList<DocumentBuilder>();
    }

    /**
     * Adds DocumentBuilder instances to the ChainedDocumentBuilder. Note that after Document instances have been
     * created with {@link ChainedDocumentBuilder#createDocument(java.awt.image.BufferedImage, String)} no new
     * DocumentBuilder instances can be added.
     * @param builder the DocumentBuilder instance to add.
     */
    public void addBuilder(DocumentBuilder builder) {
        if (docsCreated)
            throw new UnsupportedOperationException("Cannot modify chained builder after documents have been created!");
        builders.add(builder);
    }

    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        docsCreated = true;
        LinkedList<Field> resultList = new LinkedList<Field>();
        if (builders.size() >= 1) {
            for (DocumentBuilder builder : builders) {
                Field[] fields = builder.createDescriptorFields(image);
                for (int i = 0; i < fields.length; i++) {
                    resultList.add(fields[i]);
                }
            }
        }
        return resultList.toArray(new Field[resultList.size()]);
    }

    public Document createDocument(BufferedImage image, String identifier) throws FileNotFoundException {
        docsCreated = true;
        Document doc = new Document();
        if (identifier != null)
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES)); // changed to StringField based on the suggestions of Berthold Daum <berthold.daum@bdaum.de>
        if (builders.size() >= 1) {
            for (DocumentBuilder builder : builders) {
                Field[] fields = builder.createDescriptorFields(image);
                for (int i = 0; i < fields.length; i++) {
                    Field field = fields[i];
                    doc.add(field);
                }
            }
        }
        return doc;
    }
}
