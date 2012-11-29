/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.imageanalysis;


import java.awt.image.BufferedImage;

/**
 * This is the basic interface for all content based features. It is needed for GenericDocumentBuilder etc.
 * Date: 28.05.2008
 * Time: 14:44:16
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface LireFeature {
    public void extract(BufferedImage bimg);

    public byte[] getByteArrayRepresentation();

    public void setByteArrayRepresentation(byte[] in);

    public void setByteArrayRepresentation(byte[] in, int offset, int length);

    public double[] getDoubleHistogram();

    float getDistance(LireFeature feature);

    java.lang.String getStringRepresentation();

    void setStringRepresentation(java.lang.String s);
}
