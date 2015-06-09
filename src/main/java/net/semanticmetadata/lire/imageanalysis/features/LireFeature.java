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

package net.semanticmetadata.lire.imageanalysis.features;

/**
 * This is the basic interface for all content based features.
 * Created by mlux on 28/05/2008.
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public interface LireFeature extends FeatureVector {
    /**
     * Gives a descriptive name of the feature, i.e. a name to show up in benchmarks, menus, UIs, etc.
     * @return the name of the feature.
     */
    public String getFeatureName();

    /**
     * Returns the preferred field name for indexing.
     * @return the field name preferred for indexing in a Lucene index.
     */
    public String getFieldName();

    /**
     * Returns a compact byte[] based representation of the feature vector.
     * @return a compact byte[] array containing the feature vector.
     * @see LireFeature#setByteArrayRepresentation(byte[])
     */
    public byte[] getByteArrayRepresentation();

    /**
     * Sets the feature vector values based on the byte[] data. Use
     * {@link LireFeature#getByteArrayRepresentation()}
     * to generate a compatible byte[] array.
     * @param featureData the byte[] data.
     * @see LireFeature#getByteArrayRepresentation()
     */
    public void setByteArrayRepresentation(byte[] featureData);

    /**
     * Sets the feature vector values based on the byte[] data.
     * Use {@link LireFeature#getByteArrayRepresentation()}
     * to generate a compatible byte[] array.
     * @param featureData the byte[] array containing the data.
     * @param offset the offset, i.e. where the feature vector starts.
     * @param length the length of the data representing the feature vector.
     * @see LireFeature#getByteArrayRepresentation()
     */
    public void setByteArrayRepresentation(byte[] featureData, int offset, int length);

    /**
     * The distance function for this type of feature
     * @param feature the feature vector to compare the current instance to.
     * @return the distance (or dissimilarity) between the instance and the parameter.
     */
    double getDistance(LireFeature feature);
}
