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

package net.semanticmetadata.lire.aggregators;

import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.util.Arrays;
import java.util.List;

/**
 * General class creating histograms based on BOVW model, given the list of local features and the codebook.
 * Created by Nektarios on 03/06/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class BOVW extends AbstractAggregator {
    private double[] histogram;

    public BOVW() { }

    /**
     * Given a list of features and a codebook, {@link BOVW#createVectorRepresentation(List, Cluster[])} aggregates
     * the features to create the vector representation according to the BOVW model.
     * @param listOfLocalFeatures is the list of features.
     * @param clustersArray is the codebook.
     */
    @Override
    public void createVectorRepresentation(List<? extends LocalFeature> listOfLocalFeatures, Cluster[] clustersArray) {
        histogram = new double[clustersArray.length];
        Arrays.fill(histogram, 0d);
        // find the appropriate cluster for each feature:
        for (LocalFeature listOfLocalFeature : listOfLocalFeatures) {
            histogram[clusterForFeature(listOfLocalFeature.getFeatureVector(), clustersArray)]++;
        }
//        quantize(histogram);
    }

    /**
     * Returns the vector representation in byte[] format.
     * @return the vector representation as a byte array.
     */
    @Override
    public byte[] getByteVectorRepresentation() {
        return SerializationUtils.toByteArray(histogram);
    }

    /**
     * Returns the vector representation in string format, according to the {@link BOVW#arrayToVisualWordString(double[])} method.
     * @return the vector representation as string.
     */
    @Override
    public String getStringVectorRepresentation() { return arrayToVisualWordString(histogram); }

    /**
     * Returns the vector representation in double[] format.
     * @return the vector representation as a double array.
     */
    @Override
    public double[] getVectorRepresentation() { return histogram; }


    @Override
    public String getFieldName() {
        return Aggregator.FIELD_NAME_BOVW;
    }


    /**
     * Returns the vector representation in string format.
     * @return the vector representation as string.
     */
    public String toString() { return SerializationUtils.toString(histogram);}

    private void quantize(double[] data) {
        double max = 0;
        for (double next : data) {
            max = Math.max(max, next);
        }
        for (int i = 0; i < data.length; i++) {
            data[i] = (int) Math.floor((data[i] * 128d) / max);
        }
    }

    private String arrayToVisualWordString(double[] data) {
        StringBuilder sb = new StringBuilder(1024);     //TODO: are 1024 enough??
        int visualWordIndex;
        for (int i = 0; i < data.length; i++) {
            visualWordIndex = (int) data[i];
            for (int j = 0; j < visualWordIndex; j++) {
                // sb.append('v');
                sb.append(Integer.toHexString(i));
                sb.append(' ');
            }
        }
        return sb.toString();
    }

}