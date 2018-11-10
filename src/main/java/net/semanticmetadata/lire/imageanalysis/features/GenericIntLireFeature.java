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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.imageanalysis.features;

import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

/**
 * Generic int[] based feature implementation using L1 to compare features. Users have to make sure themselves that all features have the same length and L1 makes sense for them.
 * @author Mathias Lux, mathias@juggle.at, 27.09.13 17:00
 */
public class GenericIntLireFeature implements LireFeature {
    protected int[] data = null;
    private String featureName = "GenericIntFeature";
    private String fieldName = "featGenericInt";

    @Override
    public String getFeatureName() {
        return featureName;
    }

    @Override
    public String getFieldName() {
        return fieldName;
    }

//    @Override
//    public void extract(BufferedImage image) {
//        throw new UnsupportedOperationException("Extraction not supported.");
//    }

    @Override
    public byte[] getByteArrayRepresentation() {
        if (data == null)
            throw new UnsupportedOperationException("You need to set the histogram first.");
        return SerializationUtils.toByteArray(data);
    }

    @Override
    public void setByteArrayRepresentation(byte[] featureData) {
        setByteArrayRepresentation(featureData, 0, featureData.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] featureData, int offset, int length) {
        data = SerializationUtils.toIntArray(featureData, offset, length);
    }

    @Override
    public double[] getFeatureVector() {
        return SerializationUtils.castToDoubleArray(data);
    }

    @Override
    public double getDistance(LireFeature feature) {
        // it is assumed that the histograms are of equal length.
        if (! (feature instanceof GenericIntLireFeature)) throw new UnsupportedOperationException("This is not a GenericByteLireFeature object.");
        assert ((GenericIntLireFeature) feature).data.length == data.length;
        return MetricsUtils.distL1(((GenericIntLireFeature) feature).data, data);
    }

    /**
     * We assume that it is numbers ...
     * @param data
     */
    public void setData(int[] data) {
        this.data = new int[data.length];
        System.arraycopy(data, 0, this.data, 0, data.length);
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public void getFeatureName(String featureName) {
        this.featureName = featureName;
    }
}
