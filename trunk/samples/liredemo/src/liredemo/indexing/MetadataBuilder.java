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
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 16.04.13 18:32
 */

package liredemo.indexing;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Iterator;

/**
 * Created by: Mathias Lux, mathias@juggle.at
 * Date: 18.02.2010
 * Time: 15:20:59
 */
public class MetadataBuilder extends ChainedDocumentBuilder {
    public MetadataBuilder() {
        super();
        addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());

        addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
        addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());

        addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getJpegCoefficientHistogramDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getLuminanceLayoutDocumentBuilder());
        addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
        addBuilder(new SurfDocumentBuilder());
    }

    @Override
    public Document createDocument(BufferedImage bufferedImage, String s) throws FileNotFoundException {
        Document d = super.createDocument(ImageUtils.createWorkingCopy(bufferedImage), s);
        // extract available metadata:
        Metadata metadata = new Metadata();
        try {
            new ExifReader(new FileInputStream(s)).extract(metadata);
            new IptcReader(new FileInputStream(s)).extract(metadata);
            // add metadata to document:
            Iterator i = metadata.getDirectoryIterator();
            while (i.hasNext()) {
                Directory dir = (Directory) i.next();
                String prefix = dir.getName();
                Iterator ti = dir.getTagIterator();
                while (ti.hasNext()) {
                    Tag tag = (Tag) ti.next();
                    // System.out.println(prefix+"-"+tag.getTagName()+" -> " + dir.getString(tag.getTagType()));
                    // add to document:
                    d.add(new Field(prefix + "-" + tag.getTagName(), dir.getString(tag.getTagType()), Field.Store.YES, Field.Index.ANALYZED));
                }
            }
        } catch (JpegProcessingException e) {
            System.err.println("Error reading EXIF & IPTC metadata from image file.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        return d;
    }
}
