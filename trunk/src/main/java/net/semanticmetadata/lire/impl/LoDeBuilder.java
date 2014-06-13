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

    /**
     * Set the Local Descriptor feature to the one given.
     * @param lireFeature
     */
    public LoDeBuilder(LireFeature lireFeature) {
        this.lireFeature = lireFeature;
    }

    public LoDeBuilder() {}

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
