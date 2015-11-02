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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 11.07.13 10:38
 */

package net.semanticmetadata.lire.imageanalysis.features.global.joint;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * A simple implementation of a joint histogram combining 64-bin RGB and pixel rank.
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class JointHistogram implements GlobalFeature {
    private int[] tmpIntensity = new int[1];
    double[] descriptor;

    @Override
    public void extract(BufferedImage bimg) {
        bimg = ImageUtils.get8BitRGBImage(bimg);
        // extract:
        int[][] histogram = new int[64][9];
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++)
                histogram[i][j] = 0;
        }
        WritableRaster grey = ImageUtils.getGrayscaleImage(bimg).getRaster();
        WritableRaster raster = bimg.getRaster();
        int[] px = new int[3];
        int[] intens = new int[1];
        for (int x = 1; x < raster.getWidth() - 1; x++) {
            for (int y = 1; y < raster.getHeight() - 1; y++) {
                raster.getPixel(x, y, px);
                int colorPos = (int) Math.round((double) px[2] / 85d) +
                        (int) Math.round((double) px[1] / 85d) * 4 +
                        (int) Math.round((double) px[0] / 85d) * 4 * 4;
                int rank = 0;
                grey.getPixel(x, y, intens);
                if (getIntensity(x - 1, y - 1, grey) > intens[0]) rank++;
                if (getIntensity(x, y - 1, grey) > intens[0]) rank++;
                if (getIntensity(x + 1, y - 1, grey) > intens[0]) rank++;
                if (getIntensity(x - 1, y + 1, grey) > intens[0]) rank++;
                if (getIntensity(x, y + 1, grey) > intens[0]) rank++;
                if (getIntensity(x + 1, y + 1, grey) > intens[0]) rank++;
                if (getIntensity(x - 1, y, grey) > intens[0]) rank++;
                if (getIntensity(x + 1, y, grey) > intens[0]) rank++;
                histogram[colorPos][rank]++;
            }
        }
        // normalize with max norm & quantize to [0,127]:
        descriptor = new double[64 * 9];
        double max = 0;
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++) {
                max = Math.max(histogram[i][j], max);
            }
        }
        for (int i = 0; i < histogram.length; i++) {
            for (int j = 0; j < histogram[i].length; j++) {
                descriptor[i + 64 * j] = Math.floor(127d * (histogram[i][j] / max));
            }
        }
    }


    private int getIntensity(int x, int y, WritableRaster grey) {
        grey.getPixel(x, y, tmpIntensity);
        return tmpIntensity[0];
    }

//    public String getStringRepresentation() {
//        StringBuilder sb = new StringBuilder(descriptor.length * 2 + 25);
//        sb.append("jhist");
//        sb.append(' ');
//        sb.append(descriptor.length);
//        sb.append(' ');
//        for (double aData : descriptor) {
//            sb.append((int) aData);
//            sb.append(' ');
//        }
//        return sb.toString().trim();
//    }
//
//    public void setStringRepresentation(String s) {
//        StringTokenizer st = new StringTokenizer(s);
//        if (!st.nextToken().equals("jhist"))
//            throw new UnsupportedOperationException("This is not a JointHistogram descriptor.");
//        descriptor = new double[Integer.parseInt(st.nextToken())];
//        for (int i = 0; i < descriptor.length; i++) {
//            if (!st.hasMoreTokens())
//                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
//            descriptor[i] = Integer.parseInt(st.nextToken());
//        }
//
//    }

    /**
     * Provides a much faster way of serialization.
     *
     * @return a byte array that can be read with the corresponding method.
     * @see CEDD#setByteArrayRepresentation(byte[])
     */
    @Override
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[descriptor.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) descriptor[i];
        }
        return result;
    }

    /**
     * Reads descriptor from a byte array. Much faster than the String based method.
     *
     * @param in byte array from corresponding method
     * @see CEDD#getByteArrayRepresentation
     */
    @Override
    public void setByteArrayRepresentation(byte[] in) {
        descriptor = new double[in.length];
        for (int i = 0; i < descriptor.length; i++) {
            descriptor[i] = in[i];
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        descriptor = new double[length];
        for (int i = offset; i < length; i++) {
            descriptor[i] = in[i];
        }
    }

    @Override
    public double[] getFeatureVector() {
        double[] result = new double[descriptor.length];
        for (int i = 0; i < descriptor.length; i++) {
            result[i] = descriptor[i];
        }
        return result;
    }

    @Override
    public double getDistance(LireFeature feature) {
        if (!(feature instanceof JointHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");
        return MetricsUtils.jsd(((JointHistogram) feature).descriptor, descriptor);
    }

    @Override
    public String getFeatureName() {
        return "Joint Histogram Rank RBG";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_JOINT_HISTOGRAM;
    }
}
