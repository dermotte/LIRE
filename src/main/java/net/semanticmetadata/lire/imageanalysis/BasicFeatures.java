/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
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
 * Updated: 11.07.13 10:34
 */

package net.semanticmetadata.lire.imageanalysis;


import net.semanticmetadata.lire.utils.ImageUtils;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.WritableRaster;

/**
 * This class computes and quantifies several basic image features like contrast, overall sharpness etc.
 *
 * @author Thomas Pairitsch
 * @deprecated other features replace the functionality of this one, do not use!
 */
public class BasicFeatures implements LireFeature {

    //Downscaling
    private static final int SIZE = 1024;
    //For HueCount
    private static final int BINS = 20;
    private static final float threshold = 0.05f;

    //Calculated Values
    //private float value=0;
    private float brightness = 0;
    private float clipping = 0;
    private float contrast = 0;
    private float hueCount = 0;
    private float saturation = 0;
    private float complexity = 0;
    private float skew = 0;
    private float energy = 0;

    /**
     * Analysis and calculates the quality score for the image.
     *
     * @param
     */

    public void extract(BufferedImage bimg) {
        BufferedImage sml = ImageUtils.scaleImage(bimg, SIZE);
        WritableRaster raster = sml.getRaster();
        int bands = raster.getNumBands();

        //extracted Features
        int clipB = 0, clipD = 0;
        float average = 0;
        float sum = 0;
        int[] hist = new int[BINS];
        int[] hist256 = new int[256];
        int max = 0;

        if (bands == 3) {
            int w = raster.getWidth();
            int h = raster.getHeight();
            int numPixels = w * h;
            int[] pixels = raster.getPixels(0, 0, w, h, new int[numPixels * bands]);
            int[] gPixels = new int[numPixels];
            for (int i = 0; i < w * h * bands; i += bands) {
                int grey = (pixels[i] + pixels[i + 1] + pixels[i + 2]) / 3;
                float[] hsv = new float[3];
                //Brightness
                brightness += (float) grey / 255;

                //Clipping
                int cornerSum = pixels[0] + pixels[1] + pixels[2] + pixels[w * h * 3 - 3] + pixels[w * h * 3 - 2] + pixels[w * h * 3 - 1];
                if (!(cornerSum == 0 || cornerSum == 255 * 6)) {
                    if (grey == 255) {
                        clipB += 1;
                    } else if (grey == 0) {
                        clipD += 1;
                    }
                }
                //Contrast
                gPixels[i / 3] = grey;
                sum += grey;

                //Energy
                hist256[grey]++;

                //HueCount&&Saturation
                Color.RGBtoHSB(pixels[i], pixels[i + 1], pixels[i + 2], hsv);
                if (hsv[2] > 0.15 && hsv[2] < 0.95 && hsv[1] > 0.2) {
                    hist[(int) (hsv[0] * 20)]++;
                }
                saturation += hsv[1];
            }
            complexity = getComplexity(bimg);
            clipping = (float) (clipB + clipD / 2) / numPixels;
            brightness /= numPixels;
            average = sum / numPixels;
            float dev = stdDeviation(gPixels, average);
            contrast = dev / 128f;
            //Huecount
            for (int i = 0; i < BINS; i++) {
                if (hist[i] > max) {
                    max = hist[i];
                }
            }
            max *= threshold;
            for (int i = 0; i < BINS; i++) {
                if (hist[i] > max) {
                    hueCount++;
                }
            }

            hueCount /= BINS;
            //Energy
            for (int i = 0; i < 256; i++) {
                float temp = ((float) hist256[i]) / (float) (w * h);
                energy += temp * temp;
            }
            //Skew
            for (int i = 0; i < 256; i++) {
                float temp = ((float) hist256[i]) / (float) (w * h);
                float temp2 = (i - average);
                skew += temp2 * temp2 * temp2 * temp;
            }
            skew /= dev * dev * dev;
            saturation /= numPixels;

        }
        //value = evaluate(brightness,contrast,hueCount,saturation,complexity,clipping,energy,skew );

        //System.out.println("DEBUG: {"+brightness+","+contrast+","+hueCount+","+saturation+","+complexity+","+clipping+","+energy+","+ skew+"}");
    }

    public byte[] getByteArrayRepresentation() {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public void setByteArrayRepresentation(byte[] in) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        throw new UnsupportedOperationException("Not implemented!");
    }

    public double[] getDoubleHistogram() {
        double[] result = new double[8];
        result[0] = brightness;
        result[1] = clipping;
        result[2] = contrast;
        result[3] = hueCount;
        result[4] = saturation;
        result[5] = complexity;
        result[6] = skew;
        result[7] = energy;
        return result;
    }

    /*
     private float evaluate(float brightness, float contrast, float hueCount,
             float saturation, float complexity, float clipping,float energy, float skew) {
         QPrediction qPred = new QPrediction();
         try {
             qPred = WekaQuality.predictQuality(new float[]{brightness,contrast,hueCount,saturation,complexity,clipping,energy,skew});
         } catch (Exception e) {
             System.err.println("Could not predict value, setting quality=0");
             e.printStackTrace();
         }
         return qPred.prGood;
     }
     */

    private float getComplexity(BufferedImage img) {
        //Uses its own resizing method to remove color in the same step
        BufferedImage sml = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_BYTE_GRAY);
        sml.getGraphics().drawImage(img, 0, 0, SIZE, SIZE, null);
        float ret = 0;
        int w = sml.getWidth();
        int h = sml.getHeight();
        Kernel laplace = new Kernel(3, 3, new float[]{1, 1, 1, 1, -8, 1, 1, 1, 1});
        ConvolveOp filter = new ConvolveOp(laplace);
        BufferedImage dest = filter.createCompatibleDestImage(sml, null);
        filter.filter(sml, dest);
        WritableRaster data = dest.getRaster();
        int[] pixels = data.getPixels(0, 0, w, h, new int[w * h]);
        int sum = 0;
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int temp = pixels[i + j * w];
                sum += temp;
            }
        }
        ret = (float) sum / (w * h * 256);
        if (ret < 0.01) {
            ret = 1;
        }
        return ret;
    }

    private float stdDeviation(int[] input, float mean) {
        float sum = 0;
        for (int i : input) {
            float temp = i - mean;
            sum += temp * temp;
        }
        return (float) Math.sqrt(sum / (input.length - 1));
    }


    public float getDistance(LireFeature arg0) {
        if (!(arg0 instanceof BasicFeatures))
            throw new UnsupportedOperationException("Wrong descriptor.");
        BasicFeatures in = (BasicFeatures) arg0;
        float tmp = brightness - in.brightness;
        float dst = tmp * tmp;
        tmp = clipping - in.clipping;
        dst += tmp * tmp;
        tmp = contrast - in.contrast;
        dst += tmp * tmp;
        tmp = hueCount - in.hueCount;
        dst += tmp * tmp;
        tmp = saturation - in.saturation;
        dst += tmp * tmp;
        tmp = complexity - in.complexity;
        dst += tmp * tmp;
        tmp = skew - in.skew;
        dst += tmp * tmp;
        tmp = energy - in.energy;
        dst += tmp * tmp;
        return (float) Math.sqrt(dst);
    }


    public String getStringRepresentation() {
        return brightness + " " + clipping + " " + contrast + " " + hueCount + " " + saturation + " " + complexity + " " + skew + " " + energy;
    }

    public void setStringRepresentation(String arg0) {
        String[] values = arg0.split(" ");
        brightness = Float.parseFloat(values[0]);
        clipping = Float.parseFloat(values[1]);
        contrast = Float.parseFloat(values[2]);
        hueCount = Float.parseFloat(values[3]);
        saturation = Float.parseFloat(values[4]);
        complexity = Float.parseFloat(values[5]);
        skew = Float.parseFloat(values[6]);
        energy = Float.parseFloat(values[7]);
    }

    @Override
    public String getFeatureName() {
        return "BasicFeatures";
    }

    @Override
    public String getFieldName() {
        return "f_baf";
    }
}
