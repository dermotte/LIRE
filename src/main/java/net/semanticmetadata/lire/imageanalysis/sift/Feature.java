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
package net.semanticmetadata.lire.imageanalysis.sift;


import net.semanticmetadata.lire.imageanalysis.Histogram;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.io.Serializable;
import java.util.LinkedList;
import java.util.StringTokenizer;
import java.util.logging.Logger;

/**
 * SIFT feature container
 */
public class Feature extends Histogram implements Comparable<Feature>, Serializable, LireFeature {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private Logger logger = Logger.getLogger(getClass().getName());
    public float scale;
    public float orientation;
    public float[] location;
//    public float[] descriptor;

    /**
     * Dummy constructor for Serialization to work properly.
     */
    public Feature() {
    }

    public Feature(float s, float o, float[] l, float[] d) {
        scale = s;
        orientation = o;
        location = l;
        descriptor = SerializationUtils.toDoubleArray(d);
    }

    /**
     * comparator for making Features sortable
     * please note, that the comparator returns -1 for
     * this.scale &gt; o.scale, to sort the features in a descending order
     */
    public int compareTo(Feature f) {
        return scale < f.scale ? 1 : scale == f.scale ? 0 : -1;
    }

    public float descriptorDistance(Feature f) {
        if (!(f instanceof Feature)) return -1;
        return (float) MetricsUtils.distL2(descriptor, ((Feature) f).descriptor);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < descriptor.length; i++) {
            sb.append(descriptor[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public float getDistance(LireFeature feature) {
        if (feature instanceof Feature) return descriptorDistance((Feature) feature);
        else return -1f;
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(512);
        sb.append("sift");
        sb.append(' ');
        sb.append(scale);
        sb.append(' ');
        sb.append(orientation);
        sb.append(' ');
        // we assume that the location is 2D:
        assert (location.length == 2);
        sb.append(location[0]);
        sb.append(' ');
        sb.append(location[1]);
        sb.append(' ');
        // we add the descriptor: (default size == 4*4*8
        for (int i = 0; i < descriptor.length; i++) {
            sb.append(descriptor[i]);
            sb.append(' ');
        }
        return sb.toString();
    }

    public void setStringRepresentation(String s) {
        StringTokenizer st = new StringTokenizer(s, " ");
        if (!st.nextToken().equals("sift")) {
            logger.warning("This is not a SIFT feature.");
            return;
        }
        scale = Float.parseFloat(st.nextToken());
        orientation = Float.parseFloat(st.nextToken());
        location = new float[2];
        location[0] = Float.parseFloat(st.nextToken());
        location[1] = Float.parseFloat(st.nextToken());

        // parse descriptor:
        LinkedList<Float> descVals = new LinkedList<Float>();
        while (st.hasMoreTokens()) descVals.add(Float.parseFloat(st.nextToken()));

        // set descriptor:
        descriptor = new double[descVals.size()];
        for (int i = 0; i < descriptor.length; i++) {
            descriptor[i] = descVals.get(i);
        }
    }

    public void extract(BufferedImage bimg) {
        throw new UnsupportedOperationException("No implemented!");
    }

    /**
     * Writing out to bytes ... just to save some time and space.
     *
     * @return
     */
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[descriptor.length * 4 + 4 * 4];
        byte[] tmp;

        tmp = SerializationUtils.toBytes(scale);
        for (int j = 0; j < 4; j++) result[j] = tmp[j];
        tmp = SerializationUtils.toBytes(orientation);
        for (int j = 0; j < 4; j++) result[4 + j] = tmp[j];
        tmp = SerializationUtils.toBytes(location[0]);
        for (int j = 0; j < 4; j++) result[8 + j] = tmp[j];
        tmp = SerializationUtils.toBytes(location[1]);
        for (int j = 0; j < 4; j++) result[12 + j] = tmp[j];

        for (int i = 16; i < result.length; i += 4) {
            tmp = SerializationUtils.toBytes(descriptor[(i - 16) / 4]);
            for (int j = 0; j < 4; j++) {
                result[i + j] = tmp[j];
            }
        }
        return result;
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see net.semanticmetadata.lire.imageanalysis.CEDD#getByteArrayRepresentation
     */
    public void setByteArrayRepresentation(byte[] in) {
        byte[] tmp = new byte[4];
        descriptor = new double[in.length / 4 - 4];
        location = new float[2];

        System.arraycopy(in, 0, tmp, 0, 4);
        scale = SerializationUtils.toFloat(tmp);
        System.arraycopy(in, 4, tmp, 0, 4);
        orientation = SerializationUtils.toFloat(tmp);
        System.arraycopy(in, 8, tmp, 0, 4);
        location[0] = SerializationUtils.toFloat(tmp);
        System.arraycopy(in, 12, tmp, 0, 4);
        location[1] = SerializationUtils.toFloat(tmp);

        for (int i = 0; i < descriptor.length; i++) {
            System.arraycopy(in, 16 + i * 4, tmp, 0, 4);
            descriptor[i] = SerializationUtils.toFloat(tmp);
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        byte[] tmp = new byte[4];
        descriptor = new double[length / 4 - 4];
        location = new float[2];

        System.arraycopy(in, offset, tmp, 0, 4);
        scale = SerializationUtils.toFloat(tmp);
        System.arraycopy(in, offset + 4, tmp, 0, 4);
        orientation = SerializationUtils.toFloat(tmp);
        System.arraycopy(in, offset + 8, tmp, 0, 4);
        location[0] = SerializationUtils.toFloat(tmp);
        System.arraycopy(in, offset + 12, tmp, 0, 4);
        location[1] = SerializationUtils.toFloat(tmp);

        for (int i = 0; i < descriptor.length; i++) {
            System.arraycopy(in, offset + 16 + i * 4, tmp, 0, 4);
            descriptor[i] = SerializationUtils.toFloat(tmp);
        }
    }

    public double[] getDoubleHistogram() {
        double[] result = new double[descriptor.length];
        for (int i = 0; i < descriptor.length; i++) {
            result[i] = descriptor[i];
        }
        return result;
    }
}

