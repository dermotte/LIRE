package net.semanticmetadata.lire.impl;

import com.stromberglabs.jopensurf.FastHessian;
import com.stromberglabs.jopensurf.ImageWrapper;
import com.stromberglabs.jopensurf.IntegralImage;
import com.stromberglabs.jopensurf.SURFInterestPoint;
import net.semanticmetadata.lire.AbstractDocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.imageanalysis.ScalableColor;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Implementation based on the paper Searching Images with MPEG-7 (& MPEG-7-like) Powered Localized
 * dEscriptors: The SIMPLE answer to effective Content Based Image Retrieval (Savvas Chatzichristofis
 * sent me the code :))
 * Created by mlux on 13.06.2014.
 */
public class LoDeBuilder extends AbstractDocumentBuilder {
    LireFeature lireFeature = new ScalableColor();

    @Override
    public Field[] createDescriptorFields(BufferedImage image) {
        ArrayList<Field> fields = new ArrayList<Field>();
        IntegralImage integralImage = new IntegralImage(new ImageWrapper(image));
        // originally from Savvas in c#: FastHessian.getIpoints(0.00005f, 4, 2, img);
        // those are the params from JOpenSurf:
//        FastHessian fh = new FastHessian(integralImage, 5, 2, 0.0004f, 0.81F);
        FastHessian fh = new FastHessian(integralImage, 4, 2, 0.00005f, 0.81F);
        List<SURFInterestPoint> iPoints = fh.getIPoints();
        double s = 0d;
        for (Iterator<SURFInterestPoint> iterator = iPoints.iterator(); iterator.hasNext(); ) {
            SURFInterestPoint next = iterator.next();
            s = next.getScale() * 2.5;
            // System.out.println("next.getX() = " + Math.floor(next.getX() - s / 2) + ", " + Math.floor(next.getY() - s / 2) + " " + ((int) s));
            lireFeature.extract(ImageUtils.cropImage(image, (int) Math.floor(next.getX() - s / 2), (int) Math.floor(next.getY() - s / 2), (int) s, (int) s));
            // System.out.println(s + " - lireFeature = " + lireFeature.getStringRepresentation());
            fields.add(new StoredField(lireFeature.getFieldName(), lireFeature.getByteArrayRepresentation()));
        }
        return fields.toArray(new Field[fields.size()]);
    }
}
