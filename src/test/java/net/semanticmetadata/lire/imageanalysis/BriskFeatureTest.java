package net.semanticmetadata.lire.imageanalysis;

import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.IOException;

import javax.imageio.ImageIO;

import junit.framework.TestCase;
import net.semanticmetadata.lire.impl.BriskDocumentBuilder;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import com.googlecode.javacv.cpp.opencv_core.CvMat;

/**
 * Unit tests for BriskFeature and BriskDocumentBuilder.
 * 
 * @author Mario Taschwer
 * @version $Id$
 */
public class BriskFeatureTest extends TestCase
{
    private final String testImageDir = "src/test/resources/images/";
    private final String[] testImages = new String[] { "img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG" };
    private final double epsilon = 0.01;  // BRISK features are integers

    public void testCreateDescriptorFields() throws IOException
    {
        BriskDocumentBuilder briskBuilder = new BriskDocumentBuilder();
        BriskFeature briskFeature = new BriskFeature();
        for (String testImg : testImages) {
            BufferedImage img = ImageIO.read(new FileInputStream(testImageDir + testImg));
            Field[] fields = briskBuilder.createDescriptorFields(img);
            CvMat desc = briskBuilder.getDescriptor();
            assertTrue(desc.rows() > 0);
            assertTrue(desc.cols() == 64);
            assertTrue(fields.length == desc.rows());
            System.out.format("%s: extracted %d BRISK feature vectors at %d key points\n", 
                    testImg, fields.length, briskBuilder.numKeyPoints());
            for (int i=0; i < fields.length; i++) {
                BytesRef bref = fields[i].binaryValue();
                briskFeature.setByteArrayRepresentation(bref.bytes, bref.offset, bref.length);
                for (int j=0; j < desc.cols(); j++) {
                    assertTrue(Math.abs(desc.get(i, j) - briskFeature.descriptor[j]) < epsilon);
                }
            }
        }
    }
    
    public void testCreateDocument() throws IOException
    {
        BriskDocumentBuilder briskBuilder = new BriskDocumentBuilder();
        BriskFeature briskFeature = new BriskFeature();
        for (String testImg : testImages) {
            BufferedImage img = ImageIO.read(new FileInputStream(testImageDir + testImg));
            Document doc = briskBuilder.createDocument(img, testImg);
            CvMat desc = briskBuilder.getDescriptor();
            assertTrue(desc.rows() > 0);
            assertTrue(desc.cols() == 64);
            IndexableField[] fields = doc.getFields(BriskDocumentBuilder.FIELD_NAME_BRISK);
            assertTrue(fields.length == desc.rows());
            System.out.format("%s: extracted %d BRISK feature vectors at %d key points\n", 
                    testImg, fields.length, briskBuilder.numKeyPoints());
            for (int i=0; i < fields.length; i++) {
                BytesRef bref = fields[i].binaryValue();
                briskFeature.setByteArrayRepresentation(bref.bytes, bref.offset, bref.length);
                for (int j=0; j < desc.cols(); j++) {
                    assertTrue(Math.abs(desc.get(i, j) - briskFeature.descriptor[j]) < epsilon);
                }
            }
        }
    }
}
