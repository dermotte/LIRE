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
package net.semanticmetadata.lire.imageanalysis.cedd;

public class CEDDQuant {
    private static double[] QuantTable =

            {180.19686541079636, 23730.024499150866, 61457.152912541605, 113918.55437576842, 179122.46400035513, 260980.3325940354, 341795.93301552488, 554729.98648386425};


    double[] QuantTable2 =

            {209.25176965926232, 22490.5872862417345, 60250.8935141849988, 120705.788057580583, 181128.08709063051, 234132.081356900555, 325660.617733105708, 520702.175858657472};


    double[] QuantTable3 =

            {405.4642173212585, 4877.9763319071481, 10882.170090625908, 18167.239081219657, 27043.385568785292, 38129.413201299016, 52675.221316293857, 79555.402607004813};


    double[] QuantTable4 =

            {405.4642173212585, 4877.9763319071481, 10882.170090625908, 18167.239081219657, 27043.385568785292, 38129.413201299016, 52675.221316293857, 79555.402607004813};


    double[] QuantTable5 =

            {968.88475977695578, 10725.159033657819, 24161.205360376698, 41555.917344385321, 62895.628446402261, 93066.271379694881, 136976.13317822068, 262897.86056221306};


    double[] QuantTable6 =

            {968.88475977695578, 10725.159033657819, 24161.205360376698, 41555.917344385321, 62895.628446402261, 93066.271379694881, 136976.13317822068, 262897.86056221306};


    public double[] Apply(double[] Local_Edge_Histogram)

    {

        double[] Edge_HistogramElement = new double[Local_Edge_Histogram.length];

        double[] ElementsDistance = new double[8];

        double Max = 1;


        for (int i = 0; i < 24; i++)

        {

            Edge_HistogramElement[i] = 0;

            for (int j = 0; j < 8; j++)

            {

                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable[j] / 1000000);

            }

            Max = 1;

            for (int j = 0; j < 8; j++)

            {

                if (ElementsDistance[j] < Max)

                {

                    Max = ElementsDistance[j];

                    Edge_HistogramElement[i] = j;

                }

            }


        }


        for (int i = 24; i < 48; i++)

        {

            Edge_HistogramElement[i] = 0;

            for (int j = 0; j < 8; j++)

            {

                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable2[j] / 1000000);

            }

            Max = 1;

            for (int j = 0; j < 8; j++)

            {

                if (ElementsDistance[j] < Max)

                {

                    Max = ElementsDistance[j];

                    Edge_HistogramElement[i] = j;

                }

            }


        }


        for (int i = 48; i < 72; i++)

        {

            Edge_HistogramElement[i] = 0;

            for (int j = 0; j < 8; j++)

            {

                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable3[j] / 1000000);

            }

            Max = 1;

            for (int j = 0; j < 8; j++)

            {

                if (ElementsDistance[j] < Max)

                {

                    Max = ElementsDistance[j];

                    Edge_HistogramElement[i] = j;

                }

            }


        }


        for (int i = 72; i < 96; i++)

        {

            Edge_HistogramElement[i] = 0;

            for (int j = 0; j < 8; j++)

            {

                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable4[j] / 1000000);

            }

            Max = 1;

            for (int j = 0; j < 8; j++)

            {

                if (ElementsDistance[j] < Max)

                {

                    Max = ElementsDistance[j];

                    Edge_HistogramElement[i] = j;

                }

            }


        }


        for (int i = 96; i < 120; i++)

        {

            Edge_HistogramElement[i] = 0;

            for (int j = 0; j < 8; j++)

            {

                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable5[j] / 1000000);

            }

            Max = 1;

            for (int j = 0; j < 8; j++)

            {

                if (ElementsDistance[j] < Max)

                {

                    Max = ElementsDistance[j];

                    Edge_HistogramElement[i] = j;

                }

            }


        }


        for (int i = 120; i < 144; i++)

        {

            Edge_HistogramElement[i] = 0;

            for (int j = 0; j < 8; j++)

            {

                ElementsDistance[j] = Math.abs(Local_Edge_Histogram[i] - QuantTable6[j] / 1000000);

            }

            Max = 1;

            for (int j = 0; j < 8; j++)

            {

                if (ElementsDistance[j] < Max)

                {

                    Max = ElementsDistance[j];

                    Edge_HistogramElement[i] = j;

                }

            }


        }


        return Edge_HistogramElement;

    }
}
