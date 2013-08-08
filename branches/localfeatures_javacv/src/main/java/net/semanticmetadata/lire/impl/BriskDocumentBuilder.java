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
import net.semanticmetadata.lire.imageanalysis.LocalFeature;

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
 * @version $Id$
 */
public class BriskDocumentBuilder extends AbstractDocumentBuilder 
{
    private BRISK extractor;
    private KeyPoint kpoints;
    private CvMat descriptor;
    
    public BriskDocumentBuilder()
    {
        extractor = new BRISK();
        kpoints = new KeyPoint();
        descriptor = new CvMat(null);
    }
    
    private void detectFeatures(BufferedImage bimg)
    {
        IplImage img = IplImage.createFrom(bimg);
        if (img.depth() != IPL_DEPTH_8U) {
            IplImage img2 = new IplImage(null);
            cvCvtColor(img, img2, CV_BGR2GRAY);
            img = img2;
        }
        kpoints.capacity(0);         // clear data buffer
        descriptor.capacity(0);      // clear data buffer 
        extractor.detectAndCompute(img, null, kpoints, descriptor, false);
        descriptor.reset();          // make internal state consistent after .capacity(0)
        assert(descriptor.type() == CV_8UC1);    // BRISK features are unsigned byte values
    }
    
    public CvMat getDescriptor()
    {
        return descriptor;
    }
    
    public int numKeyPoints()
    {
        return kpoints.capacity();
    }
    
	@Override
	public Field[] createDescriptorFields(BufferedImage bimg) 
	{
	    detectFeatures(bimg);
        ByteBuffer buff = descriptor.getByteBuffer();
		final int nDesc = descriptor.rows();     // number of descriptors
		final int lenDesc = descriptor.cols();   // descriptor length (in bytes)
		final int step = descriptor.step();      // row length of desc in bytes
		Field[] result = new Field[nDesc];
		for (int i=0; i < nDesc; i++) {
	        byte[] b = new byte[lenDesc];  // necessary, because StoredField keeps a reference to b
		    LocalFeature.byteArrayFromBuffer(b, buff, i*step, lenDesc);
		    result[i] = new StoredField(FIELD_NAME_BRISK, b);
		}
		return result;
	}

	@Override
	public Document createDocument(BufferedImage bimg, String identifier) throws FileNotFoundException 
	{
        detectFeatures(bimg);
        ByteBuffer buff = descriptor.getByteBuffer();
        final int nDesc = descriptor.rows();     // number of descriptors
        final int lenDesc = descriptor.cols();   // descriptor length (in bytes)
        final int step = descriptor.step();      // row length of desc in bytes
        Document doc = new Document();
        for (int i=0; i < nDesc; i++) {
            byte[] b = new byte[lenDesc];  // necessary, because StoredField keeps a reference to b
            LocalFeature.byteArrayFromBuffer(b, buff, i*step, lenDesc);
            doc.add(new StoredField(FIELD_NAME_BRISK, b));
        }
        if (identifier != null)
            doc.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, identifier, Field.Store.YES));
        return doc;
	}

}
