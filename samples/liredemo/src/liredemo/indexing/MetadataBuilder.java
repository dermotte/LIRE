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

package liredemo.indexing;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.exif.ExifReader;
import com.drew.metadata.iptc.IptcReader;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.ColorLayoutDocumentBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
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
        addBuilder(new ColorLayoutDocumentBuilder());
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
        addBuilder(new SurfDocumentBuilder());
    }

    @Override
    public Document createDocument(BufferedImage bufferedImage, String s) throws FileNotFoundException {
        Document d = super.createDocument(bufferedImage, s);
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
