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
public class FuzzyFCTHpart {
    public double[] ResultsTable = new double[8];

    public double[] HorizontalMembershipValues = {0, 0, 20, 90, 20, 90, 255, 255};

    public double[] VerticalMembershipValues = {0, 0, 20, 90, 20, 90, 255, 255};

    public double[] EnergyMembershipValues = {0, 0, 20, 80, 20, 80, 255, 255};

    public FuzzyRules[] TextureRules = new FuzzyRules[8];

    public double[] FCTH = new double[192];
    public double[] HActivation = new double[2];
    public double[] VActivation = new double[2];
    public double[] EActivation = new double[2];

    public int[][] RulesDefinition = {
            {0, 0, 0, 0},
            {0, 0, 1, 1},
            {0, 1, 0, 2},
            {0, 1, 1, 3},
            {1, 0, 0, 4},
            {1, 0, 1, 5},
            {1, 1, 0, 6},
            {1, 1, 1, 7}};

    public FuzzyFCTHpart() {
        for (int R = 0; R < 8; R++) {
            TextureRules[R] = new FuzzyRules();
            TextureRules[R].Input1 = RulesDefinition[R][0];
            TextureRules[R].Input2 = RulesDefinition[R][1];
            TextureRules[R].Input3 = RulesDefinition[R][2];
            TextureRules[R].Output = RulesDefinition[R][3];

        }

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


    private void LOM_Defazzificator(FuzzyRules[] Rules, double[] Input1, double[] Input2, double[] Input3, double[] ResultTable) {
        int RuleActivation = -1;
        double LOM_MAXofMIN = 0;

        for (int i = 0; i < Rules.length; i++) {

            if ((Input1[Rules[i].Input1] > 0) && (Input2[Rules[i].Input2] > 0) && (Input3[Rules[i].Input3] > 0)) {

                double Min = 0;
                Min = Math.min(Input1[Rules[i].Input1], Math.min(Input2[Rules[i].Input2], Input3[Rules[i].Input3]));

                if (Min > LOM_MAXofMIN) {
                    LOM_MAXofMIN = Min;
                    RuleActivation = Rules[i].Output;
                }

            }

        }


        ResultTable[RuleActivation]++;


    }


    private void MultiParticipate_Equal_Defazzificator(FuzzyRules[] Rules, double[] Input1, double[] Input2, double[] Input3, double[] ResultTable) {

        int RuleActivation = -1;

        for (int i = 0; i < Rules.length; i++) {
            if ((Input1[Rules[i].Input1] > 0) && (Input2[Rules[i].Input2] > 0) && (Input3[Rules[i].Input3] > 0)) {
                RuleActivation = Rules[i].Output;
                ResultTable[RuleActivation]++;

            }

        }
    }


    private void MultiParticipate_Defazzificator(FuzzyRules[] Rules, double[] Input1, double[] Input2, double[] Input3, double[] ResultTable) {

        int RuleActivation = -1;

        for (int i = 0; i < Rules.length; i++) {
            if ((Input1[Rules[i].Input1] > 0) && (Input2[Rules[i].Input2] > 0) && (Input3[Rules[i].Input3] > 0)) {
                RuleActivation = Rules[i].Output;
                double Min = 0;
                Min = Math.min(Input1[Rules[i].Input1], Math.min(Input2[Rules[i].Input2], Input3[Rules[i].Input3]));

                ResultTable[RuleActivation] += Min;

            }

        }
    }


    public double[] ApplyFilter(double F1, double F2, double F3, double[] ColorValues, int Method, int NumberOfColors) {
        // Method   0 = LOM
        //          1 = Multi Equal Participate
        //          2 = Multi Participate

        for (int i = 0; i < 8; i++) {
            ResultsTable[i] = 0;

        }

        FindMembershipValueForTriangles(F1, HorizontalMembershipValues, HActivation);
        FindMembershipValueForTriangles(F2, VerticalMembershipValues, VActivation);
        FindMembershipValueForTriangles(F3, EnergyMembershipValues, EActivation);


        if (Method == 0) LOM_Defazzificator(TextureRules, HActivation, VActivation, EActivation, ResultsTable);
        if (Method == 1)
            MultiParticipate_Equal_Defazzificator(TextureRules, HActivation, VActivation, EActivation, ResultsTable);
        if (Method == 2)
            MultiParticipate_Defazzificator(TextureRules, HActivation, VActivation, EActivation, ResultsTable);


        for (int i = 0; i < 8; i++) {
            if (ResultsTable[i] > 0) {
                for (int j = 0; j < NumberOfColors; j++) {

                    if (ColorValues[j] > 0) FCTH[NumberOfColors * i + j] += ResultsTable[i] * ColorValues[j];

                }

            }


        }

        return (FCTH);

    }


}

