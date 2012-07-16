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

package net.semanticmetadata.lire.imageanalysis.fcth;

public class FCTHQuant {

    private static double[] QuantTable =
            {130.0887781556944, 9317.31301788632, 22434.355689233365, 43120.548602722061, 83168.640165905046, 101430.52589975641, 174840.65838706805, 224480.41479670047};

    double[] QuantTable2 =
            {130.0887781556944, 9317.31301788632, 22434.355689233365, 43120.548602722061, 83168.640165905046, 151430.52589975641, 174840.65838706805, 224480.41479670047};

    double[] QuantTable3 =
            {239.769468748322, 17321.704312335689, 39113.643180734696, 69333.512093874378, 79122.46400035513, 90980.3325940354, 161795.93301552488, 184729.98648386425};

    double[] QuantTable4 =
            {239.769468748322, 17321.704312335689, 39113.643180734696, 69333.512093874378, 79122.46400035513, 90980.3325940354, 161795.93301552488, 184729.98648386425};

    double[] QuantTable5 =
            {239.769468748322, 17321.704312335689, 39113.643180734696, 69333.512093874378, 79122.46400035513, 90980.3325940354, 161795.93301552488, 184729.98648386425};

    double[] QuantTable6 =
            {239.769468748322, 17321.704312335689, 39113.643180734696, 69333.512093874378, 79122.46400035513, 90980.3325940354, 161795.93301552488, 184729.98648386425};

    double[] QuantTable7 =
            {180.19686541079636, 23730.024499150866, 41457.152912541605, 53918.55437576842, 69122.46400035513, 81980.3325940354, 91795.93301552488, 124729.98648386425};

    double[] QuantTable8 =
            {180.19686541079636, 23730.024499150866, 41457.152912541605, 53918.55437576842, 69122.46400035513, 81980.3325940354, 91795.93301552488, 124729.98648386425};


    public double[] Apply(double[] Local_Edge_Histogram) {
        double[] Edge_HistogramElement = new double[Local_Edge_Histogram.length];
        double[] ElementsDistance = new double[8];
        double Max = 1;

        for (int i = 0; i < 24; i++) {
            Edge_HistogramElement[i] = 0;
            for (int j = 0; j < 8; j++) {
                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable[j] / 1000000);
            }
            Max = 1;
            for (int j = 0; j < 8; j++) {
                if (ElementsDistance[j] < Max) {
                    Max = ElementsDistance[j];
                    Edge_HistogramElement[i] = j;
                }
            }


        }


        for (int i = 24; i < 48; i++) {
            Edge_HistogramElement[i] = 0;
            for (int j = 0; j < 8; j++) {
                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable2[j] / 1000000);
            }
            Max = 1;
            for (int j = 0; j < 8; j++) {
                if (ElementsDistance[j] < Max) {
                    Max = ElementsDistance[j];
                    Edge_HistogramElement[i] = j;
                }
            }


        }


        for (int i = 48; i < 72; i++) {
            Edge_HistogramElement[i] = 0;
            for (int j = 0; j < 8; j++) {
                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable3[j] / 1000000);
            }
            Max = 1;
            for (int j = 0; j < 8; j++) {
                if (ElementsDistance[j] < Max) {
                    Max = ElementsDistance[j];
                    Edge_HistogramElement[i] = j;
                }
            }


        }


        for (int i = 72; i < 96; i++) {
            Edge_HistogramElement[i] = 0;
            for (int j = 0; j < 8; j++) {
                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable4[j] / 1000000);
            }
            Max = 1;
            for (int j = 0; j < 8; j++) {
                if (ElementsDistance[j] < Max) {
                    Max = ElementsDistance[j];
                    Edge_HistogramElement[i] = j;
                }
            }


        }


        for (int i = 96; i < 120; i++) {
            Edge_HistogramElement[i] = 0;
            for (int j = 0; j < 8; j++) {
                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable5[j] / 1000000);
            }
            Max = 1;
            for (int j = 0; j < 8; j++) {
                if (ElementsDistance[j] < Max) {
                    Max = ElementsDistance[j];
                    Edge_HistogramElement[i] = j;
                }
            }


        }


        for (int i = 120; i < 144; i++) {
            Edge_HistogramElement[i] = 0;
            for (int j = 0; j < 8; j++) {
                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable6[j] / 1000000);
            }
            Max = 1;
            for (int j = 0; j < 8; j++) {
                if (ElementsDistance[j] < Max) {
                    Max = ElementsDistance[j];
                    Edge_HistogramElement[i] = j;
                }
            }


        }


        for (int i = 144; i < 168; i++) {
            Edge_HistogramElement[i] = 0;
            for (int j = 0; j < 8; j++) {
                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable7[j] / 1000000);
            }
            Max = 1;
            for (int j = 0; j < 8; j++) {
                if (ElementsDistance[j] < Max) {
                    Max = ElementsDistance[j];
                    Edge_HistogramElement[i] = j;
                }
            }


        }


        for (int i = 168; i < 192; i++) {
            Edge_HistogramElement[i] = 0;
            for (int j = 0; j < 8; j++) {
                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable8[j] / 1000000);
            }
            Max = 1;
            for (int j = 0; j < 8; j++) {
                if (ElementsDistance[j] < Max) {
                    Max = ElementsDistance[j];
                    Edge_HistogramElement[i] = j;
                }
            }


        }


        return Edge_HistogramElement;
    }
}
