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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.logging.Logger;

/**
 * Factory for creating documents for one color images in a fast and efficient way.
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 14.12.2006
 * <br>Time: 13:03:30
 *
 * @author Mathias Lux, mathias@juggle.at
 * @deprecated no longer needed, will be deleted.
 */
public class DocumentFactory {
    // create the logger for this factory:
    static Logger logger = Logger.getLogger(DocumentFactory.class.getName());

    /**
     * Creates a document from a (non existent) one color image. Can be used for
     * color search.
     *
     * @param color the color for the image
     * @return the document for searching.
     */
    public static Document createColorOnlyDocument(Color color) {
        assert (color != null);
        // Create a one pixel image
        int imgWidth = 64;
        BufferedImage img = new BufferedImage(imgWidth, imgWidth, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = (Graphics2D) img.getGraphics();
        g.setColor(color);
        g.fillRect(0, 0, imgWidth, imgWidth);
        // Hand it over to ScalableColor to create a descriptor:
        ColorLayout scd = new ColorLayout();
        scd.extract(img);
        // create the string representation
        String sc = scd.getStringRepresentation();
//        System.out.println("sc = " + sc);
//        System.out.println("color = " + color);
        logger.fine("Extraction from image finished");
        Document doc = new Document();
        if (sc != null)
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_COLORLAYOUT, sc, Field.Store.YES));
        return doc;
    }
}
