package net.semanticmetadata.lire.impl;

import static com.googlecode.javacv.cpp.opencv_core.CV_8UC1;
import static com.googlecode.javacv.cpp.opencv_core.IPL_DEPTH_8U;
import static com.googlecode.javacv.cpp.opencv_imgproc.CV_BGR2GRAY;
import static com.googlecode.javacv.cpp.opencv_imgproc.cvCvtColor;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.nio.ByteBuffer;

import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.BriskFeature;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;

import com.googlecode.javacv.cpp.opencv_core.CvMat;
import com.googlecode.javacv.cpp.opencv_core.IplImage;
import com.googlecode.javacv.cpp.opencv_features2d.BRISK;
import com.googlecode.javacv.cpp.opencv_features2d.KeyPoint;

/**
 * DocumentBuilder extracting BRISK local features from images.
 * 
 * @author Mario Taschwer, mt@itec.aau.at
 * @version 27.06.2013
 */
public class BriskDocumentBuilder extends AbstractDocumentBuilder 
{

    public static CvMat detectFeatures(BufferedImage bimg)
    {
        IplImage img = IplImage.createFrom(bimg);
        if (img.depth() != IPL_DEPTH_8U) {
            IplImage img2 = new IplImage(null);
            cvCvtColor(img, img2, CV_BGR2GRAY);
            img = img2;
        }
        BRISK feature = new BRISK();
        KeyPoint kpoints = new KeyPoint();
        CvMat desc = new CvMat(null);
        feature.detectAndCompute(img, null, kpoints, desc, false);
        assert(desc.type() == CV_8UC1);    // BRISK features are unsigned byte values
        return desc;
    }
    
	@Override
	public Field[] createDescriptorFields(BufferedImage bimg) 
	{
	    CvMat desc = detectFeatures(bimg);
        ByteBuffer buff = desc.getByteBuffer();
		final int nDesc = desc.rows();     // number of descriptors
		final int lenDesc = desc.cols();   // descriptor length (in bytes)
		final int step = desc.step();      // row length of desc in bytes
		Field[] result = new Field[nDesc];
		for (int i=0; i < nDesc; i++) {
	        byte[] b = new byte[lenDesc];  // necessary, because StoredField keeps a reference to b
		    BriskFeature.byteArrayFromBuffer(b, buff, i*step, lenDesc);
		    result[i] = new StoredField(FIELD_NAME_BRISK, b);
		}
		return result;
	}

	@Override
	public Document createDocument(BufferedImage bimg, String identifier) throws FileNotFoundException 
	{
        CvMat desc = detectFeatures(bimg);
        ByteBuffer buff = desc.getByteBuffer();
        final int nDesc = desc.rows();     // number of descriptors
        final int lenDesc = desc.cols();   // descriptor length (in bytes)
        final int step = desc.step();      // row length of desc in bytes
        Document doc = new Document();
        for (int i=0; i < nDesc; i++) {
            byte[] b = new byte[lenDesc];  // necessary, because StoredField keeps a reference to b
            BriskFeature.byteArrayFromBuffer(b, buff, i*step, lenDesc);
            doc.add(new StoredField(FIELD_NAME_BRISK, b));
        }
        if (identifier != null)
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
        return doc;
	}

}
