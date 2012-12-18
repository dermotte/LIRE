package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.StringTokenizer;

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
public class OpponentHistogram extends Histogram implements LireFeature {
    final double sq2 = Math.sqrt(2d);
    final double sq6 = Math.sqrt(3d);
    final double sq3 = Math.sqrt(6d);

    double o1, o2, o3;

    @Override
    public void extract(BufferedImage bimg) {
        // extract:
        double[] histogram = new double[64];
        for (int i = 0; i < histogram.length; i++) {
            histogram[i] = 0;
        }
        WritableRaster raster = bimg.getRaster();
        int[] px = new int[3];
        int colorPos;
        for (int x = 1; x < raster.getWidth() - 1; x++) {
            for (int y = 1; y < raster.getHeight() - 1; y++) {
                raster.getPixel(x, y, px);
                o1 = (double) (px[0] - px[1]) / sq2;
                o2 = (double) (px[0] + px[1] - 2 * px[2]) / sq6;
                o3 = (double) (px[0] + px[1] + px[2]) / sq3;
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
        descriptor = new double[64];
        double max = 0;
        for (int i = 0; i < histogram.length; i++) {
            max = Math.max(histogram[i], max);
        }
        for (int i = 0; i < histogram.length; i++) {
            descriptor[i] = Math.floor(127d * (histogram[i] / max));
        }
    }

    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[descriptor.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) descriptor[i];
        }
        return result;
    }

    public void setByteArrayRepresentation(byte[] in) {
        descriptor = new double[in.length];
        for (int i = 0; i < descriptor.length; i++) {
            descriptor[i] = in[i];
        }
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        descriptor = new double[length];
        for (int i = offset; i < length; i++) {
            descriptor[i] = in[i];
        }
    }

    public double[] getDoubleHistogram() {
        return descriptor;
    }

    public float getDistance(LireFeature feature) {
        if (!(feature instanceof OpponentHistogram))
            throw new UnsupportedOperationException("Wrong descriptor.");
        return (float) MetricsUtils.jsd(((OpponentHistogram) feature).descriptor, descriptor);
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(descriptor.length * 2 + 25);
        sb.append("ophist");
        sb.append(' ');
        sb.append(descriptor.length);
        sb.append(' ');
        for (double aData : descriptor) {
            sb.append((int) aData);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String s) {
        StringTokenizer st = new StringTokenizer(s);
        if (!st.nextToken().equals("ophist"))
            throw new UnsupportedOperationException("This is not a OpponentHistogram descriptor.");
        descriptor = new double[Integer.parseInt(st.nextToken())];
        for (int i = 0; i < descriptor.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
            descriptor[i] = Integer.parseInt(st.nextToken());
        }

    }
}
