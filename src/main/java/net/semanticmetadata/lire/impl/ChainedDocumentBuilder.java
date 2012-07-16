/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.Fieldable;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * User: mlux
 * Date: 20.02.2007
 * Time: 15:11:59
 */
public class ChainedDocumentBuilder extends AbstractDocumentBuilder {
    private LinkedList<DocumentBuilder> builders;
    private boolean docsCreated = false;

    public ChainedDocumentBuilder() {
        builders = new LinkedList<DocumentBuilder>();
    }

    public void addBuilder(DocumentBuilder builder) {
        if (docsCreated)
            throw new UnsupportedOperationException("Cannot modify chained builder after documents have been created!");
        builders.add(builder);
    }

    public Document createDocument(BufferedImage image, String identifier) throws FileNotFoundException {
        docsCreated = true;
        Document doc = new Document();
        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
        // this is unfortunately rather slow, but however it works :)
        if (builders.size() >= 1) {
            for (DocumentBuilder builder : builders) {
                Document d = builder.createDocument(image, identifier);
                for (Iterator<Fieldable> iterator = d.getFields().iterator(); iterator.hasNext(); ) {
                    Field f = (Field) iterator.next();
                    if (!f.name().equals(DocumentBuilder.FIELD_NAME_IDENTIFIER)) {
                        doc.add(f);
                    }
                }
            }
        }
        return doc;
    }
}
