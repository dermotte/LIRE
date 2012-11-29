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

import com.stromberglabs.jopensurf.SURFInterestPoint;
import net.semanticmetadata.lire.utils.ConversionUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

/**
 * Mathias Lux, mathias@juggle.at
 * Date: 29.09.2010
 * Time: 15:44:14
 * To change this template use File | Settings | File Templates.
 */
public class SurfFeature extends Histogram implements LireFeature {
    SURFInterestPoint sip;

    public SurfFeature(SURFInterestPoint surfInterestPoint) {
        this.sip = surfInterestPoint;
        descriptor = sip.getDescriptor();
    }

    public SurfFeature() {
        sip = null;
    }

    public void extract(BufferedImage bimg) {
        // does nothing ....
    }

    public float getDistance(LireFeature feature) {
        if (!(feature instanceof SurfFeature)) return -1;
        return MetricsUtils.distL2(descriptor, ((SurfFeature) feature).descriptor);
    }

    public String getStringRepresentation() {
        throw new UnsupportedOperationException("No implemented!");
    }

    public void setStringRepresentation(String s) {
        float[] result = null;
        LinkedList<Float> tmp = new LinkedList<Float>();
        StringTokenizer st = new StringTokenizer(s);
        st.nextToken(); // point.x
        st.nextToken(); // point.y
        st.nextToken(); // response
        while (st.hasMoreTokens())
            tmp.add(Float.parseFloat(st.nextToken()));
        result = new float[tmp.size()];
        int i = 0;
        for (Iterator<Float> iterator = tmp.iterator(); iterator.hasNext(); ) {
            Float next = iterator.next();
            result[i] = next;
            i++;
        }
        descriptor = result;

    }

    /**
     * Provides a much faster way of serialization.
     *
     * @return a byte array that can be read with the corresponding method.
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#setByteArrayRepresentation(byte[])
     */
    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(descriptor);
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#getByteArrayRepresentation
     */
    public void setByteArrayRepresentation(byte[] in) {
        descriptor = SerializationUtils.toFloatArray(in);
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        descriptor = SerializationUtils.toFloatArray(in, offset, length);
    }

    public double[] getDoubleHistogram() {
        return ConversionUtils.toDouble(descriptor);
    }

}
