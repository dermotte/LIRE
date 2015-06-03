package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSurfFeature;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Nektarios on 2/10/2014.
 */
public class CvSurfDocumentBuilder extends AbstractDocumentBuilder{

    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        CvSurfExtractor s = new CvSurfExtractor();
        LinkedList<CvSurfFeature> descriptors = s.computeSurfFeatures(image);
        Field[] result = new Field[descriptors.size()];
        int count = 0;
        for (Iterator<CvSurfFeature> cvsf = descriptors.iterator(); cvsf.hasNext(); ) {
            CvSurfFeature sf = cvsf.next();
            result[count] = (new StoredField(DocumentBuilder.FIELD_NAME_CVSURF, sf.getByteArrayRepresentation()));
            count++;
        }
        return result;
    }

    public Document createDocument(BufferedImage image, String identifier) {
        CvSurfExtractor s = new CvSurfExtractor();
        LinkedList<CvSurfFeature> descriptors = s.computeSurfFeatures(image);
        Document doc = new Document();
        for (Iterator<CvSurfFeature> cvsf = descriptors.iterator(); cvsf.hasNext(); ) {
            CvSurfFeature sf = cvsf.next();
            doc.add(new StoredField(DocumentBuilder.FIELD_NAME_CVSURF, sf.getByteArrayRepresentation()));
        }
        if (identifier != null)
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
        return doc;
    }

}