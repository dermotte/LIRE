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

import com.stromberglabs.jopensurf.SURFInterestPoint;
import com.stromberglabs.jopensurf.Surf;
import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.SurfFeature;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.List;

/**
 * User: mathias@juggle.at
 * Date: 29.09.2010
 * Time: 15:41:28
 */
public class SurfDocumentBuilder extends AbstractDocumentBuilder {
    public Document createDocument(BufferedImage image, String identifier) {
        Document doc = null;
        Surf s = new Surf(image);
        List<SURFInterestPoint> interestPoints = s.getFreeOrientedInterestPoints();
        doc = new Document();
        for (Iterator<SURFInterestPoint> sipi = interestPoints.iterator(); sipi.hasNext(); ) {
            SURFInterestPoint sip = sipi.next();
            SurfFeature sf = new SurfFeature(sip);
            doc.add(new Field(DocumentBuilder.FIELD_NAME_SURF, sf.getByteArrayRepresentation()));
        }
        if (identifier != null)
            doc.add(new Field(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES, Field.Index.NOT_ANALYZED));
        return doc;
    }
}
