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
 * Updated: 07.08.13 12:18
 */

package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

/**
 * Simple 64 bin Opponent Histogram, based on the Opponent color space as described in van de Sande, Gevers & Snoek (2010)
 * "Evaluating Color Descriptors for Object and Scene Recognition", IEEE PAMI (see BibTeX in the source code).
 *
 *
 * @author Mathias Lux, mathias@juggle.at
 * Date: 18.12.12
 * Time: 11:53
 */

/*
@ARTICLE{Sande2010,
    author={van de Sande, K.E.A. and Gevers, T. and Snoek, C.G.M.},
    journal={Pattern Analysis and Machine Intelligence, IEEE Transactions on},
    title={Evaluating Color Descriptors for Object and Scene Recognition},
    year={2010},
    month={sept. },
    volume={32},
    number={9},
    pages={1582 -1596},
    doi={10.1109/TPAMI.2009.154},
    ISSN={0162-8828},
}
*/
public class OpponentHistogram implements GlobalFeature {
    final double sq2 = Math.sqrt(2d);
    final double sq6 = Math.sqrt(3d);
    final double sq3 = Math.sqrt(6d);
    double[] descriptor;

    double o1, o2, o3;

    double tmpVal, tmpSum;

    byte[] histogram = new byte[64];

    @Override
    public void extract(BufferedImage bimg) {
        // check if it's (i) RGB and (ii) 8 bits per pixel.
        bimg = ImageUtils.get8BitRGBImage(bimg);
        // extract:
        double[] histogram = new double[64];
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = 0;
        }
        WritableRaster raster = bimg.getRaster();
        int[] px = new int[3*(raster.getHeight()-2)];
        int colorPos;
        for (int x = 1; x < raster.getWidth() - 1; x++) {
            raster.getPixels(x, 1, 1, raster.getHeight()-2, px);
            for (int y = 0; y < raster.getHeight() - 2; y++) {
                o1 = (double) (px[y*3] - px[y*3+1]) / sq2;
                o2 = (double) (px[y*3] + px[y*3+1] - 2 * px[y*3+2]) / sq6;
                o3 = (double) (px[y*3] + px[y*3+1] + px[y*3+2]) / sq3;
                // Normalize ... easier to handle.
                o1 = (o1 + 255d / sq2) / (510d / sq2);
                o2 = (o2 + 510d / sq6) / (1020d / sq6);
                o3 = o3 / (3d * 255d / sq3);
                // get the array position.
                colorPos = (int) Math.min(Math.floor(o1 * 4d), 3d) + (int) Math.min(Math.floor(o2 * 4d), 3d) * 4 + (int) Math.min(3d, Math.floor(o3 * 4d)) * 4 * 4;
                histogram[colorPos]++;
            }
        }
        // normalize with max norm & quantize to [0,127]:
        double max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(histogram[i], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            this.histogram[i] = (byte) Math.floor(127d * (histogram[i] / max));
        }
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[histogram.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = histogram[i];
        }
        return result;
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = in[i];
        }
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        for (int i = 0; i < length; i++) {
            histogram[i] = in[i+offset];
        }
    }

    @Override
    public double[] getFeatureVector() {
        return SerializationUtils.castToDoubleArray(histogram);
    }

    @Override
    public double getDistance(LireFeature feature) {
        if (!(feature instanceof OpponentHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");
        return MetricsUtils.jsd(((OpponentHistogram) feature).histogram, histogram);
    }

    public double getDistance(byte[] h1, byte[] h2) {
        return getDistance(h1, 0, h1.length, h2, 0, h2.length);
    }

    /**
     * Jeffrey Divergence or Jensen-Shannon divergence (JSD) from
     * Deselaers, T.; Keysers, D. & Ney, H. Features for image retrieval:
     * an experimental comparison Inf. Retr., Kluwer Academic Publishers, 2008, 11, 77-107
     * @param h1
     * @param offset1
     * @param length1
     * @param h2
     * @param offset2
     * @param length2
     * @return
     */
    public double getDistance(byte[] h1, int offset1, int length1, byte[] h2, int offset2, int length2) {
//        double sum = 0f;
//        for (int i = 0; i < h1.length; i++) {
//            sum += (h1[i] > 0 ? (h1[i] / 2f) * Math.log((2f * h1[i]) / (h1[i] + h2[i])) : 0) +
//                    (h2[i] > 0 ? (h2[i] / 2f) * Math.log((2f * h2[i]) / (h1[i] + h2[i])) : 0);
//        }
//        return (float) sum;
        tmpSum = 0;
        for (int i = 0; i < length1; i++) {
            tmpVal = (double) (h1[i+offset1] + h2[i+offset2]);
            tmpSum += (h1[i+offset1] > 0 ? ((double) h1[i+offset1] / 2d) * Math.log((2d * h1[i+offset1]) / tmpVal) : 0) +
                    (h2[i+offset2] > 0 ? ((double) h2[i+offset2] / 2d) * Math.log((2d * h2[i+offset2]) / tmpVal) : 0);
        }
        return tmpSum;
    }

//    public String getStringRepresentation() {
//        StringBuilder sb = new StringBuilder(histogram.length * 2 + 25);
//        sb.append("ophist");
//        sb.append(' ');
//        sb.append(histogram.length);
//        sb.append(' ');
//        for (double aData : histogram) {
//            sb.append((int) aData);
//            sb.append(' ');
//        }
//        return sb.toString().trim();
//    }
//
//    public void setStringRepresentation(String s) {
//        StringTokenizer st = new StringTokenizer(s);
//        if (!st.nextToken().equals("ophist"))
//            throw new UnsupportedOperationException("This is not a OpponentHistogram descriptor.");
//        for (int i = 0; i < histogram.length; i++) {
//            if (!st.hasMoreTokens())
//                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
//            histogram[i] = (byte) Integer.parseInt(st.nextToken());
//        }
//
//    }

    @Override
    public String getFeatureName() {
        return "OpponentHistogram";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM;
    }
}
