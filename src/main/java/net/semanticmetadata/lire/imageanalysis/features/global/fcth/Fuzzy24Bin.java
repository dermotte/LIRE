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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */
package net.semanticmetadata.lire.imageanalysis.features.global.fcth;

/**
 * The FCTH feature was created, implemented and provided by Savvas A. Chatzichristofis<br/>
 * More information can be found in: Savvas A. Chatzichristofis and Yiannis S. Boutalis,
 * <i>FCTH: Fuzzy Color and Texture Histogram - A Low Level Feature for Accurate Image
 * Retrieval</i>, in Proceedings of the Ninth International Workshop on Image Analysis for
 * Multimedia Interactive Services, IEEE, Klagenfurt, May, 2008.
 *
 * @author: Savvas A. Chatzichristofis, savvash@gmail.com
 */
public class Fuzzy24Bin {
    public double[] ResultsTable = new double[3];
    public double[] Fuzzy24BinHisto = new double[24];
    public boolean KeepPreviusValues = false;

    protected double[] SaturationMembershipValues = {0, 0, 68, 188,
            68, 188, 255, 255};

    protected double[] ValueMembershipValues = {0, 0, 68, 188,
            68, 188, 255, 255};

    public FuzzyRules[] Fuzzy24BinRules = new FuzzyRules[4];

    public double[] SaturationActivation = new double[2];
    public double[] ValueActivation = new double[2];

    public int[][] Fuzzy24BinRulesDefinition = {
            {1, 1, 1},
            {0, 0, 2},
            {0, 1, 0},
            {1, 0, 2}
    };


    public Fuzzy24Bin(boolean KeepPreviuesValues) {
        for (int R = 0; R < 4; R++) {
            Fuzzy24BinRules[R] = new FuzzyRules();
            Fuzzy24BinRules[R].Input1 = Fuzzy24BinRulesDefinition[R][0];
            Fuzzy24BinRules[R].Input2 = Fuzzy24BinRulesDefinition[R][1];
            Fuzzy24BinRules[R].Output = Fuzzy24BinRulesDefinition[R][2];

        }

        this.KeepPreviusValues = KeepPreviuesValues;


    }

    private void FindMembershipValueForTriangles(double Input, double[] Triangles, double[] MembershipFunctionToSave) {
        int Temp = 0;

        for (int i = 0; i <= Triangles.length - 1; i += 4) {

            MembershipFunctionToSave[Temp] = 0;

            if (Input >= Triangles[i + 1] && Input <= +Triangles[i + 2]) {
                MembershipFunctionToSave[Temp] = 1;
            }

            if (Input >= Triangles[i] && Input < Triangles[i + 1]) {
                MembershipFunctionToSave[Temp] = (Input - Triangles[i]) / (Triangles[i + 1] - Triangles[i]);
            }


            if (Input > Triangles[i + 2] && Input <= Triangles[i + 3]) {
                MembershipFunctionToSave[Temp] = (Input - Triangles[i + 2]) / (Triangles[i + 2] - Triangles[i + 3]) + 1;
            }

            Temp += 1;
        }

    }

    private void LOM_Defazzificator(FuzzyRules[] Rules, double[] Input1, double[] Input2, double[] ResultTable) {
        int RuleActivation = -1;
        double LOM_MAXofMIN = 0;

        for (int i = 0; i < Rules.length; i++) {

            if ((Input1[Rules[i].Input1] > 0) && (Input2[Rules[i].Input2] > 0)) {

                double Min = 0;
                Min = Math.min(Input1[Rules[i].Input1], Input2[Rules[i].Input2]);

                if (Min > LOM_MAXofMIN) {
                    LOM_MAXofMIN = Min;
                    RuleActivation = Rules[i].Output;
                }

            }

        }


        ResultTable[RuleActivation]++;


    }

    private void MultiParticipate_Equal_Defazzificator(FuzzyRules[] Rules, double[] Input1, double[] Input2, double[] ResultTable) {

        int RuleActivation = -1;

        for (int i = 0; i < Rules.length; i++) {
            if ((Input1[Rules[i].Input1] > 0) && (Input2[Rules[i].Input2] > 0)) {
                RuleActivation = Rules[i].Output;
                ResultTable[RuleActivation]++;

            }

        }
    }

    private void MultiParticipate_Defazzificator(FuzzyRules[] Rules, double[] Input1, double[] Input2, double[] ResultTable) {

        int RuleActivation = -1;
        double Min = 0;
        for (int i = 0; i < Rules.length; i++) {
            if ((Input1[Rules[i].Input1] > 0) && (Input2[Rules[i].Input2] > 0)) {
                Min = Math.min(Input1[Rules[i].Input1], Input2[Rules[i].Input2]);

                RuleActivation = Rules[i].Output;
                ResultTable[RuleActivation] += Min;

            }

        }
    }


    public double[] ApplyFilter(double Hue, double Saturation, double Value, double[] ColorValues, int Method) {
        // Method   0 = LOM
        //          1 = Multi Equal Participate
        //          2 = Multi Participate

        ResultsTable[0] = 0;
        ResultsTable[1] = 0;
        ResultsTable[2] = 0;
        double Temp = 0;


        FindMembershipValueForTriangles(Saturation, SaturationMembershipValues, SaturationActivation);
        FindMembershipValueForTriangles(Value, ValueMembershipValues, ValueActivation);


        if (this.KeepPreviusValues == false) {
            for (int i = 0; i < 24; i++) {
                Fuzzy24BinHisto[i] = 0;
            }

        }

        for (int i = 3; i < 10; i++) {
            Temp += ColorValues[i];
        }

        if (Temp > 0) {
            if (Method == 0) LOM_Defazzificator(Fuzzy24BinRules, SaturationActivation, ValueActivation, ResultsTable);
            if (Method == 1)
                MultiParticipate_Equal_Defazzificator(Fuzzy24BinRules, SaturationActivation, ValueActivation, ResultsTable);
            if (Method == 2)
                MultiParticipate_Defazzificator(Fuzzy24BinRules, SaturationActivation, ValueActivation, ResultsTable);


        }

        for (int i = 0; i < 3; i++) {
            Fuzzy24BinHisto[i] += ColorValues[i];
        }


        for (int i = 3; i < 10; i++) {
            Fuzzy24BinHisto[(i - 2) * 3] += ColorValues[i] * ResultsTable[0];
            Fuzzy24BinHisto[(i - 2) * 3 + 1] += ColorValues[i] * ResultsTable[1];
            Fuzzy24BinHisto[(i - 2) * 3 + 2] += ColorValues[i] * ResultsTable[2];
        }

        return (Fuzzy24BinHisto);

    }


}
