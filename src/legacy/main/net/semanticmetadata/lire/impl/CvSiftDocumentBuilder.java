package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSiftExtractor;
import net.semanticmetadata.lire.imageanalysis.opencvfeatures.CvSiftFeature;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Created by Nektarios on 2/10/2014.
 */
public class CvSiftDocumentBuilder extends AbstractDocumentBuilder {
    private Logger logger = Logger.getLogger(getClass().getName());
    private CvSiftExtractor extractor;

    public CvSiftDocumentBuilder() {
        extractor = new CvSiftExtractor();
    }

    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        // extract descriptors from image:
        LinkedList<CvSiftFeature> descriptors = extractor.computeSiftFeatures(image);
        Field[] result = new Field[descriptors.size()];
        int count = 0;
        // create new document:
        for (Iterator<CvSiftFeature> cvsf = descriptors.iterator(); cvsf.hasNext(); ) {
            CvSiftFeature f = cvsf.next();
            result[count] = new StoredField(DocumentBuilder.FIELD_NAME_CVSIFT, f.getByteArrayRepresentation());
            count++;
        }
        return result;
    }

    public Document createDocument(BufferedImage image, String identifier) {
        // extract descriptors from image:
        LinkedList<CvSiftFeature> descriptors = extractor.computeSiftFeatures(image);
        // create new document:
        Document doc = new Document();
        for (Iterator<CvSiftFeature> cvsf = descriptors.iterator(); cvsf.hasNext(); ) {
            CvSiftFeature f = cvsf.next();
            // add each cvSiftFeature to the document:
            doc.add(new StoredField(DocumentBuilder.FIELD_NAME_CVSIFT, f.getByteArrayRepresentation()));
        }
        if (identifier != null)
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
        return doc;
    }

}
