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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */
package net.semanticmetadata.lire.imageanalysis.utils;

/**
 * This class provides some basic routines for color space conversion on a pixel basis.
 * Date: 28.05.2008
 * Time: 11:27:46
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class ColorConversion {
    /**
     * Adapted from ImageJ documentation:
     * http://www.f4.fhtw-berlin.de/~barthel/ImageJ/ColorInspector//HTMLHelp/farbraumJava.htm
     *
     * @param r   from [0-255]
     * @param g   from [0-255]
     * @param b   from [0-255]
     * @param hsv where HSV values (results) are stored. hsv[0] is h from [0-359], hsv[1] is s from [0-100] and hsv[2] is v from [0-100]
     */
    public static void rgb2hsv(int r, int g, int b, int hsv[]) {
        int min = Math.min(Math.min(r, g), b);  //Min. value of RGB
        int max = Math.max(Math.max(r, g), b);  //Max. value of RGB
        double delta = (double) max - min;        //Delta RGB value

        double H = 0d, S = 0d;
        double V = max / 255d;

        if (delta != 0) {
            S = (delta / max);
            if (r == max) {
                if (g >= b) {
                    H = ((g - b) / delta) * 60;
                } else {
                    H = ((g - b) / delta) * 60 + 360;
                }
            } else if (g == max) {
                H = (2 + ((b - r) / delta)) * 60;
            } else {
                H = (4 + ((r - g) / delta)) * 60;
            }
        }
        hsv[0] = (int) (H);
        hsv[1] = (int) (S * 100);
        hsv[2] = (int) (V * 100);
    }

}
