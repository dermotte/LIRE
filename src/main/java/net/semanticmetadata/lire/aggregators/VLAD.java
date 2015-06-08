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
 */

package net.semanticmetadata.lire.aggregators;

import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Nektarios on 03/06/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class VLAD extends AbstractAggregator {
    private double[] vector;

    public VLAD() { }

    @Override
    public void createVisualWords(List<? extends LocalFeature> listOfLocalFeatures, Cluster[] clustersArray) {
        vector = new double[clustersArray.length * (clustersArray[0].getMean()).length];
        Arrays.fill(vector, 0d);
        int clusterIndex;
        double[] mean;
        // VLAD - Vector of Locally Aggregated Descriptors
        for (LocalFeature localFeature : listOfLocalFeatures) {
            clusterIndex = clusterForFeature(localFeature.getFeatureVector(), clustersArray);
            mean = clustersArray[clusterIndex].getMean();
            for (int i = 0; i < localFeature.getFeatureVector().length; i++) {
                vector[clusterIndex * localFeature.getFeatureVector().length + i] += (localFeature.getFeatureVector()[i] - mean[i]);
            }
        }
        normalize(vector);
    }

    @Override
    public byte[] getByteVectorRepresentation() { return SerializationUtils.toByteArray(vector); }

    @Override
    public String getStringVectorRepresentation() { return SerializationUtils.toString(vector); }

    @Override
    public double[] getVectorRepresentation() { return vector; }

    @Override
    public String getFieldName() {
        return Aggregator.FIELD_NAME_VLAD;
    }

    private void normalize(double[] histogram) {
        // L2
        double sumOfSquares = 0;
        for (double next : histogram) {
            sumOfSquares += (next * next);
        }
        if (sumOfSquares > 0) {
            sumOfSquares = Math.sqrt(sumOfSquares);
            for (int i = 0; i < histogram.length; i++) {
//            histogram[i] = Math.floor(16d * histogram[i] / sumOfSquares);
                histogram[i] /= sumOfSquares;
            }
        }
/*        // L1
        double min = Double.MAX_VALUE, max = Double.MIN_VALUE;
        for (int i = 0; i < histogram.length; i++) {
            min = Math.min(histogram[i], min);
            max = Math.max(histogram[i], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = (histogram[i] - min) / (max - min);
        }*/
    }

}
