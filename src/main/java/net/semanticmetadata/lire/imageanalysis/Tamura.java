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

import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.*;
import java.awt.color.ColorSpace;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.Raster;
import java.util.StringTokenizer;

/**
 * Implementation of (three) Tamura features done by  Marko Keuschnig & Christian Penz<br>
 * Changes by
 * <ul>
 * <li> Ankit Jain (jankit87@gmail.com): histogram length in set string
 * <li> shen72@users.sourceforge.net: bugfixes in math (casting and brackets)
 * <li> Arthur Lin (applefan99@gmail.com) 2011-05-10: fix to avoid NaN
 * </ul>
 * Date: 28.05.2008
 * Time: 11:52:03
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class Tamura implements LireFeature {
    private static final int MAX_IMG_HEIGHT = 64;
    private int[][] grayScales;
    private int imgWidth, imgHeight;
    private double[] histogram; // stores all three tamura features in one histogram.
    private static final double[][] filterH = {{-1, 0, 1}, {-1, 0, 1}, {-1, 0, 1}};
    private static final double[][] filterV = {{-1, -1, -1}, {0, 0, 0}, {1, 1, 1}};
    private static final String TAMURA_NAME = "tamura";

    public double coarseness(int n0, int n1) {
        double result = 0;
        for (int i = 1; i < n0 - 1; i++) {
            for (int j = 1; j < n1 - 1; j++) {
                result = result + Math.pow(2, this.sizeLeadDiffValue(i, j));
            }
        }
        // fixed based on the patch by shen72@users.sourceforge.net
        result = (1.0 / (n0 * n1)) * result;
        return result;
    }

    /**
     * 1. For every point(x, y) calculate the average over neighborhoods.
     *
     * @param x
     * @param y
     * @return
     */
    public double averageOverNeighborhoods(int x, int y, int k) {
        double result = 0, border;
        border = Math.pow(2, 2 * k);
        int x0 = 0, y0 = 0;

        for (int i = 0; i < border; i++) {
            for (int j = 0; j < border; j++) {
                x0 = x - (int) Math.pow(2, k - 1) + i;
                y0 = y - (int) Math.pow(2, k - 1) + j;
                if (x0 < 0) x0 = 0;
                if (y0 < 0) y0 = 0;
                if (x0 >= imgWidth) x0 = imgWidth - 1;
                if (y0 >= imgHeight) y0 = imgHeight - 1;
                result = result + grayScales[x0][y0];
            }
        }
        result = (1 / Math.pow(2, 2 * k)) * result;
        return result;
    }

    /**
     * 2. For every point (x, y) calculate differences between the not overlapping neighborhoods
     * on opposite sides of the point in horizontal direction.
     *
     * @param x
     * @param y
     * @return
     */
    public double differencesBetweenNeighborhoodsHorizontal(int x, int y, int k) {
        double result = 0;
        result = Math.abs(this.averageOverNeighborhoods(x + (int) Math.pow(2, k - 1), y, k) -
                this.averageOverNeighborhoods(x - (int) Math.pow(2, k - 1), y, k));
        return result;
    }

    /**
     * 2. For every point (x, y) calculate differences between the not overlapping neighborhoods
     * on opposite sides of the point in vertical direction.
     *
     * @param x
     * @param y
     * @return
     */
    public double differencesBetweenNeighborhoodsVertical(int x, int y, int k) {
        double result = 0;
        result = Math.abs(this.averageOverNeighborhoods(x, y + (int) Math.pow(2, k - 1), k) -
                this.averageOverNeighborhoods(x, y - (int) Math.pow(2, k - 1), k));
        return result;
    }

    /**
     * 3. At each point (x, y) select the size leading to the highest difference value.
     *
     * @param x
     * @param y
     * @return
     */
    public int sizeLeadDiffValue(int x, int y) {
        double result = 0, tmp;
        int maxK = 1;

        for (int k = 0; k < 3; k++) {
            tmp = Math.max(this.differencesBetweenNeighborhoodsHorizontal(x, y, k),
                    this.differencesBetweenNeighborhoodsVertical(x, y, k));
            if (result < tmp) {
                maxK = k;
                result = tmp;
            }
        }
        return maxK;
    }

    /**
     * Picture Quality.
     *
     * @return
     */
    public double contrast() {
        double result = 0, my, sigma, my4 = 0, alpha4 = 0;
        my = this.calculateMy();
        sigma = this.calculateSigma(my);

        if (sigma <= 0)
            return 0; // fix based on the comments orf Arthur Lin. Black images would lead to a NaN in later division.

        for (int x = 0; x < this.imgWidth; x++) {
            for (int y = 0; y < this.imgHeight; y++) {
                my4 = my4 + Math.pow(this.grayScales[x][y] - my, 4);
            }
        }
        alpha4 = my4 / (Math.pow(sigma, 4));
        // fixed based on the patches of shen72@users.sourceforge.net
        result = sigma / (Math.pow(alpha4, 0.25));
        return result;
    }

    /**
     * @return
     */
    public double calculateMy() {
        double mean = 0;

        for (int x = 0; x < this.imgWidth; x++) {
            for (int y = 0; y < this.imgHeight; y++) {
                mean = mean + this.grayScales[x][y];
            }
        }
        mean = mean / (this.imgWidth * this.imgHeight);
        return mean;
    }

    /**
     * @return
     */
    public double calculateSigma(double mean) {
        double result = 0;

        for (int x = 0; x < this.imgWidth; x++) {
            for (int y = 0; y < this.imgHeight; y++) {
                result = result + Math.pow(this.grayScales[x][y] - mean, 2);
            }
        }
        result = result / (this.imgWidth * this.imgHeight);
        return Math.sqrt(result);
    }

    /**
     * @return
     */
    public double[] directionality() {
        double[] histogram = new double[16];
        double maxResult = 3;
        double binWindow = maxResult / (double) (histogram.length - 1);
        int bin = -1;
        for (int x = 1; x < this.imgWidth - 1; x++) {
            for (int y = 1; y < this.imgHeight - 1; y++) {
                bin = (int) ((Math.PI / 2 + Math.atan(this.calculateDeltaV(x, y) / this.calculateDeltaH(x, y))) / binWindow);
                histogram[bin]++;
            }
        }
        return histogram;
    }

    /**
     * @return
     */
    public double calculateDeltaH(int x, int y) {
        double result = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result = result + this.grayScales[x - 1 + i][y - 1 + j] * filterH[i][j];
            }
        }
        return result;
    }

    /**
     * @return
     */
    public double calculateDeltaV(int x, int y) {
        double result = 0;

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                result = result + this.grayScales[x - 1 + i][y - 1 + j] * filterV[i][j];
            }
        }
        return result;
    }


    public double getDistance(double[] targetFeature, double[] queryFeature) {
        double result = 0;
        for (int i = 2; i < targetFeature.length; i++) {
            result += Math.pow(targetFeature[i] - queryFeature[i], 2);
        }
        return result;
    }

    public void extract(BufferedImage image) {
        histogram = new double[18];
        double[] directionality;
        ColorConvertOp op = new ColorConvertOp(image.getColorModel().getColorSpace(),
                ColorSpace.getInstance(ColorSpace.CS_GRAY),
                new RenderingHints(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY));
        BufferedImage bimg = op.filter(image, null);
        bimg = ImageUtils.scaleImage(bimg, MAX_IMG_HEIGHT);
        Raster raster = bimg.getRaster();
        int[] tmp = new int[3];
        this.grayScales = new int[raster.getWidth()][raster.getHeight()];
        for (int i = 0; i < raster.getWidth(); i++) {
            for (int j = 0; j < raster.getHeight(); j++) {
                raster.getPixel(i, j, tmp);
                this.grayScales[i][j] = tmp[0];
            }
        }
        imgWidth = bimg.getWidth();
        imgHeight = bimg.getHeight();
        histogram[0] = this.coarseness(bimg.getWidth(), bimg.getHeight());
        histogram[1] = this.contrast();
        directionality = this.directionality();
        for (int i = 2; i < histogram.length; i++) {
            histogram[i] = directionality[i - 2];
        }
    }

    public byte[] getByteArrayRepresentation() {
        return SerializationUtils.toByteArray(histogram);
    }

    public void setByteArrayRepresentation(byte[] in) {
        histogram = SerializationUtils.toDoubleArray(in);
    }

    public double[] getDoubleHistogram() {
        return histogram;
    }

    public float getDistance(LireFeature feature) {
        // Check if instance of the right class ...
        if (!(feature instanceof Tamura))
            throw new UnsupportedOperationException("Wrong descriptor.");

        // casting ...
        Tamura tamura = (Tamura) feature;
        return (float) getDistance(tamura.histogram, histogram);
    }

    public String getStringRepresentation() {
        StringBuilder sb = new StringBuilder(histogram.length * 16);
        sb.append(TAMURA_NAME);
        sb.append(' ');
        sb.append(histogram.length);
        sb.append(' ');
        for (int i = 0; i < histogram.length; i++) {
            sb.append(histogram[i]);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public void setStringRepresentation(String s) {
        StringTokenizer st = new StringTokenizer(s);
        String name = st.nextToken();
        if (!name.equals(TAMURA_NAME)) {
            throw new UnsupportedOperationException("This is not a Tamura feature string.");
        }

        /*
        * changes made by Ankit Jain here otherwise the histogram length would be assigned to histogram[i]
        * jankit87@gmail.com
        * */
        histogram = new double[Integer.parseInt(st.nextToken())];

        for (int i = 0; i < histogram.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
            histogram[i] = Double.parseDouble(st.nextToken());
        }
    }
}
