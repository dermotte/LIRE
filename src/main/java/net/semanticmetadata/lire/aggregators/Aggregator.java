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

import java.util.List;

/**
 * This class provides the interface for Aggregators, classes that produce
 * vector representations for local features.
 * Created by Nektarios on 03/06/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public interface Aggregator {
    String FIELD_NAME_BOVW = "BOVW";
    String FIELD_NAME_VLAD = "VLAD";

    /**
     * This method is used to create the vector representation of an image using the list of Features and a codebook
     * @param listOfLocalFeatures is the list of features.
     * @param clustersArray is the codebook.
     */
    void createVectorRepresentation(List<? extends LocalFeature> listOfLocalFeatures, Cluster[] clustersArray);

    /**
     * Returns the vector representation in byte[] format.
     * @return the vector representation as a byte array.
     */
    byte[] getByteVectorRepresentation();

    /**
     * Returns the vector representation in string format.
     * @return the vector representation as string.
     */
    String getStringVectorRepresentation();

    /**
     * Returns the vector representation in double[] format.
     * @return the vector representation as a double array.
     */
    double[] getVectorRepresentation();

    /**
     * Returns the FieldName according to the selected aggregator.
     * @return FieldName.
     */
    String getFieldName();
}
