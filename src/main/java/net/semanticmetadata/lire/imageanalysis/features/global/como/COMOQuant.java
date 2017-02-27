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
 * (c) 2017 by Savvas Chatzichristofis (savvash@gmail.com) & Nektarios Anagnostopoulos (nek.anag@gmail.com)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.imageanalysis.features.global.como;

/**
 * Part of the COMO global feature
 *
 * @author Savvas Chatzichristofis, savvash@gmail.com
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class COMOQuant {

    public static double[][] QuantTable = {
            {0.0, 2.2979840251251352E-4, 0.003657613514320107, 0.010357706103550541, 0.022976494474541424, 0.04752827531647437, 0.09151056108704082, 0.1615389408959318},
            {0.0, 1.861677234061732E-9, 3.883162107311353E-5, 2.645481834894222E-4, 4.989157432933257E-4, 0.0010012281919558304, 0.0022903602362873057, 0.005932084363707513},
            {0.0, 1.3951623165035215E-4, 0.0020837868450434803, 0.006022801764285895, 0.014272156711079073, 0.03090212121501537, 0.062053031549716815, 0.11730419112808481},
            {0.0, 8.89367145342922E-5, 0.0019703656430397226, 0.0062326663361094966, 0.014831325860695401, 0.03025253877556776, 0.056160026587483626, 0.10925562299842911},
            {0.0, 7.645076258669699E-4, 0.009060830664973839, 0.022816928919939736, 0.04554064383860326, 0.08050841264707713, 0.13494983145101114, 0.2189523376653257},
            {0.0, 1.2210519150091045E-7, 9.734493518238497E-4, 0.003109401139347163, 0.007469476414828149, 0.01950707582271682, 0.04556422468997043, 0.08878828670561877}
    };

    public byte[] apply(double[] Local_Edge_Histogram) {
        byte[] result = new byte[Local_Edge_Histogram.length];

        double min, temp;

        for (int i = 0; i < 24; i++) {
            min = Math.abs(Local_Edge_Histogram[i] - QuantTable[0][0]);
            result[i] = 0;
            for (byte j = 1; ((j < 8) && (min != 0.0)); j++) {
                temp = Math.abs(Local_Edge_Histogram[i] - QuantTable[0][j]);
                if (temp < min) {
                    min = temp;
                    result[i] = j;
                }
            }
        }

        for (int i = 24; i < 48; i++) {
            min = Math.abs(Local_Edge_Histogram[i] - QuantTable[1][0]);
            result[i] = 0;
            for (byte j = 1; ((j < 8) && (min != 0.0)); j++) {
                temp = Math.abs(Local_Edge_Histogram[i] - QuantTable[1][j]);
                if (temp < min) {
                    min = temp;
                    result[i] = j;
                }
            }
        }

        for (int i = 48; i < 72; i++) {
            min = Math.abs(Local_Edge_Histogram[i] - QuantTable[2][0]);
            result[i] = 0;
            for (byte j = 1; ((j < 8) && (min != 0.0)); j++) {
                temp = Math.abs(Local_Edge_Histogram[i] - QuantTable[2][j]);
                if (temp < min) {
                    min = temp;
                    result[i] = j;
                }
            }
        }

        for (int i = 72; i < 96; i++) {
            min = Math.abs(Local_Edge_Histogram[i] - QuantTable[3][0]);
            result[i] = 0;
            for (byte j = 1; ((j < 8) && (min != 0.0)); j++) {
                temp = Math.abs(Local_Edge_Histogram[i] - QuantTable[3][j]);
                if (temp < min) {
                    min = temp;
                    result[i] = j;
                }
            }
        }

        for (int i = 96; i < 120; i++) {
            min = Math.abs(Local_Edge_Histogram[i] - QuantTable[4][0]);
            result[i] = 0;
            for (byte j = 1; ((j < 8) && (min != 0.0)); j++) {
                temp = Math.abs(Local_Edge_Histogram[i] - QuantTable[4][j]);
                if (temp < min) {
                    min = temp;
                    result[i] = j;
                }
            }
        }

        for (int i = 120; i < 144; i++) {
            min = Math.abs(Local_Edge_Histogram[i] - QuantTable[5][0]);
            result[i] = 0;
            for (byte j = 1; ((j < 8) && (min != 0.0)); j++) {
                temp = Math.abs(Local_Edge_Histogram[i] - QuantTable[5][j]);
                if (temp < min) {
                    min = temp;
                    result[i] = j;
                }
            }
        }

        return result;
    }
}
