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
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 26.04.13 14:48
 */

package liredemo.indexing;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by: Mathias Lux, mathias@juggle.at
 * Date: 18.02.2010
 * Time: 15:20:59
 */
public class MetadataBuilder implements DocumentBuilder {

    public MetadataBuilder() {
    }

    private Field[] getDescriptorFields(String s) {
        LinkedList<Field> resultList = new LinkedList<Field>();
        // extract available metadata:
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(new FileInputStream(s));
            // add metadata to document:
            Iterator<Directory> i = metadata.getDirectories().iterator();
            Directory dir;
            String prefix;
            while (i.hasNext()) {
                dir = i.next();
                prefix = dir.getName();
                Collection<Tag> tags = dir.getTags();
                for (Tag tag : tags) {
                    // System.out.println(prefix+"-"+tag.getTagName()+" -> " + dir.getString(tag.getTagType()));
                    // add to document:
                    resultList.add(new TextField(prefix + "-" + tag.getTagName(), dir.getString(tag.getTagType()), Field.Store.YES));
                }
            }
        } catch (JpegProcessingException e) {
            System.err.println("Error reading EXIF & IPTC metadata from image file.");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ImageProcessingException e) {
            e.printStackTrace();
        }

        return resultList.toArray(new Field[resultList.size()]);
    }

    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        throw new UnsupportedOperationException("Unsupported Operation");
    }

    @Override
    public Document createDocument(BufferedImage image, String identifier) {
        Document doc = new Document();

        if (identifier != null) {
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
        }

        Field[] fields = getDescriptorFields(identifier);
        for (Field field : fields) {
            doc.add(field);
        }

        return doc;
    }
}
