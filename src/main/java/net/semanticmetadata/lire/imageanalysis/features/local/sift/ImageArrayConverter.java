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
 * Updated: 04.05.13 11:18
 */
package net.semanticmetadata.lire.imageanalysis.features.local.sift;

import java.awt.image.BufferedImage;
import java.awt.image.Raster;

public class ImageArrayConverter {
    public static boolean CUTOFF_VALUES = true;
    public static boolean NORM_VALUES = false;

//    public static ImagePlus FloatArrayToImagePlus(FloatArray2D image, String hashFunctionsFileName, float min, float max)
//    {
//        ImagePlus imp = IJ.createImage(hashFunctionsFileName,"32-Bit Black", image.width, image.height, 1);
//        FloatProcessor ip = (FloatProcessor)imp.getProcessor();
//        FloatArrayToFloatProcessor(ip, image);
//
//        if (min == max)
//            ip.resetMinAndMax();
//        else
//            ip.setMinAndMax(min, max);
//
//        imp.updateAndDraw();
//
//        return imp;
//    }

    public static FloatArray2D ImageToFloatArray2D(BufferedImage ip) {
        FloatArray2D image;
        Raster pixelArray = ip.getRaster();
        int count = 0;
        int[] rgb = new int[3];
        image = new FloatArray2D(ip.getWidth(), ip.getHeight());
        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                rgb = pixelArray.getPixel(x, y, rgb);
                int b = rgb[2];
                int g = rgb[1];
                int r = rgb[0];
                image.data[count] = 0.3f * r + 0.6f * g + 0.1f * b;
                count++;
            }
        }
        return image;
    }

//    public static void ArrayToByteProcessor(ImageProcessor ip, int[][] pixels)
//    {
//        byte[] data = new byte[pixels.length * pixels[0].length];
//
//        int count = 0;
//        for (int y = 0; y < pixels[0].length; y++)
//            for (int x = 0; x < pixels.length; x++)
//                data[count++] = (byte)(pixels[x][y] & 0xff);
//
//        ip.setPixels(data);
//    }
//
//    public static void ArrayToByteProcessor(ImageProcessor ip, float[][] pixels)
//    {
//        byte[] data = new byte[pixels.length * pixels[0].length];
//
//        int count = 0;
//        for (int y = 0; y < pixels[0].length; y++)
//            for (int x = 0; x < pixels.length; x++)
//                data[count++] = (byte)(((int)pixels[x][y]) & 0xff);
//
//        ip.setPixels(data);
//    }
//
//    public static void ArrayToFloatProcessor(ImageProcessor ip, double[] pixels, int width, int height)
//    {
//        float[] data = new float[width * height];
//
//        int count = 0;
//        for (int y = 0; y < height; y++)
//            for (int x = 0; x < width; x++)
//                data[count] = (float)pixels[count++];
//
//        ip.setPixels(data);
//        ip.resetMinAndMax();
//    }
//
//    public static void ArrayToFloatProcessor(ImageProcessor ip, float[] pixels, int width, int height)
//    {
//        float[] data = new float[width * height];
//
//        int count = 0;
//        for (int y = 0; y < height; y++)
//            for (int x = 0; x < width; x++)
//                data[count] = (float)pixels[count++];
//
//        ip.setPixels(data);
//        ip.resetMinAndMax();
//    }
//
//    public static void FloatArrayToFloatProcessor(ImageProcessor ip, FloatArray2D pixels)
//    {
//        float[] data = new float[pixels.width * pixels.height];
//
//        int count = 0;
//        for (int y = 0; y < pixels.height; y++)
//            for (int x = 0; x < pixels.width; x++)
//                data[count] = pixels.data[count++];
//
//        ip.setPixels(data);
//        ip.resetMinAndMax();
//    }
//
//    public static void normPixelValuesToByte(int[][] pixels, boolean cutoff)
//    {
//        int max = 0, min = 255;
//
//        // check minmal and maximal values or cut of values that are higher or lower than 255 resp. 0
//        for (int y = 0; y < pixels[0].length; y++)
//            for (int x = 0; x < pixels.length; x++)
//            {
//                if (cutoff)
//                {
//                    if (pixels[x][y] < 0)
//                        pixels[x][y] = 0;
//
//                    if (pixels[x][y] > 255)
//                        pixels[x][y] = 255;
//                }
//                else
//                {
//                    if (pixels[x][y] < min)
//                        min = pixels[x][y];
//
//                    if (pixels[x][y] > max)
//                        max = pixels[x][y];
//                }
//            }
//
//        if (cutoff)
//            return;
//
//
//        // if they do not match bytevalues we have to do something
//        if (max > 255 || min < 0)
//        {
//            double factor;
//
//            factor = (max-min) / 255.0;
//
//            for (int y = 0; y < pixels[0].length; y++)
//                for (int x = 0; x < pixels.length; x++)
//                    pixels[x][y] = (int)((pixels[x][y] - min) / factor);
//        }
//    }
//
//    public static void normPixelValuesToByte(float[][] pixels, boolean cutoff)
//    {
//        float max = 0, min = 255;
//
//        // check minmal and maximal values or cut of values that are higher or lower than 255 resp. 0
//        for (int y = 0; y < pixels[0].length; y++)
//            for (int x = 0; x < pixels.length; x++)
//            {
//                if (cutoff)
//                {
//                    if (pixels[x][y] < 0)
//                        pixels[x][y] = 0;
//
//                    if (pixels[x][y] > 255)
//                        pixels[x][y] = 255;
//                }
//                else
//                {
//                    if (pixels[x][y] < min)
//                        min = pixels[x][y];
//
//                    if (pixels[x][y] > max)
//                        max = pixels[x][y];
//                }
//            }
//
//        if (cutoff)
//            return;
//
//
//        // if they do not match bytevalues we have to do something
//        if (max > 255 || min < 0)
//        {
//            double factor;
//
//            factor = (max-min) / 255.0;
//
//            for (int y = 0; y < pixels[0].length; y++)
//                for (int x = 0; x < pixels.length; x++)
//                    pixels[x][y] = (int)((pixels[x][y] - min) / factor);
//        }
//    }
//
}
