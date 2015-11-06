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

package net.semanticmetadata.lire.imageanalysis.features.local.simple;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Created by Nektarios on 25/3/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class SimpleFeature implements LocalFeature {
    double[] data = null;
    private double X = -1, Y = -1;
    private double size = -1;
    private String featureName = "GenericSimpleFeature";
    private String fieldName = "GenericSF";
    private Class<? extends GlobalFeature> ExtractorClass;

    public SimpleFeature(){

    }

    public SimpleFeature(double[] data, double x, double y, double size, String fieldName, String featureName, Class<? extends GlobalFeature> ExtractorClass){
        this.data = data;
        this.X = x;
        this.Y = y;
        this.size = size;
        this.featureName = featureName;
        this.fieldName = fieldName;
        this.ExtractorClass = ExtractorClass;
    }

    @Override
    public String getFeatureName() {
        return featureName;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        if (data == null) throw new UnsupportedOperationException("You need to set the histogram first.");
        return SerializationUtils.toByteArray(data);
    }

    @Override
    public void setByteArrayRepresentation(byte[] featureData) {
        setByteArrayRepresentation(featureData, 0, featureData.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] featureData, int offset, int length) {
        data = SerializationUtils.toDoubleArray(featureData, offset, length);
    }

    @Override
    public double[] getFeatureVector() {
        return data;
    }

    @Override
    public double getDistance(LireFeature feature) {
        // it is assumed that the histograms are of equal length.
        assert(feature.getFeatureVector().length == data.length);
        return MetricsUtils.distL2(feature.getFeatureVector(), data);
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
    public Class<? extends GlobalFeature> getClassOfExtractor() {
        return ExtractorClass;
    }
}
