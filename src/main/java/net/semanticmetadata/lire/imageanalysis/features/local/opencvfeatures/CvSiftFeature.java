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
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval -
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
 */

package net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.io.Serializable;

/**
 * Created by Nektarios on 1/10/2014.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class CvSiftFeature implements Serializable, LocalFeature {
    //    public float angle;
//    public int class_id;
//    public int octave;
//    public double[] point;
//    public float response;
//    public float size;
//    double[] feature;
    private double X = -1, Y = -1;
    private double size = -1;
    private double[] feature = null;

    public CvSiftFeature() {
    }

    public CvSiftFeature(double X, double Y, double size, double[] feature) {
        this.X = X;
        this.Y = Y;
        this.size = size;
        this.feature = feature;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < feature.length; i++) {
            sb.append(feature[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public double getDistance(LireFeature f) {
        if (!(f instanceof CvSiftFeature)) return -1;
        return MetricsUtils.distL2(feature, ((CvSiftFeature) f).feature);
    }

    public String getStringRepresentation() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public void setStringRepresentation(String featureVector) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public void extract(BufferedImage image) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(feature);
    }

    public void setByteArrayRepresentation(byte[] in) {
        feature = SerializationUtils.toDoubleArray(in);
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        feature = SerializationUtils.toDoubleArray(in, offset, length);
    }

    public double[] getFeatureVector() {
        return feature;
    }

    @Override
    public String getFeatureName() {
        return "cvSIFT";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_CVSIFT;
    }

    @Override
    public double getX() {
        return X;
    }

    @Override
    public double getY() {
        return Y;
    }

    @Override
    public double getSize() {
        return size;
    }

    @Override
    public Class<? extends LocalFeatureExtractor> getClassOfExtractor() {
        return CvSiftExtractor.class;
    }
}