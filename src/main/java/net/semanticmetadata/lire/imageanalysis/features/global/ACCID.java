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
 */
package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.accid.PMasks;
import net.semanticmetadata.lire.imageanalysis.features.global.cedd.Fuzzy10Bin;
import net.semanticmetadata.lire.imageanalysis.features.global.cedd.Fuzzy24Bin;
import net.semanticmetadata.lire.imageanalysis.features.global.cedd.RGB2HSV;
import net.semanticmetadata.lire.utils.ImageUtils;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;
import java.util.Arrays;

/**
 * ACCID global feature as designed by Chrysanthi Iakovidou and implemented by Nektarios Anagnostopoulos. Main idea
 * is to find the most important edges on different scales and store them in a histogram along with a fuzzy color
 * scheme taken from the CEDD descriptor.
 *
 * @author Chrysanthi Iakovidou
 * @author Nektarios Anagnostopoulos
 * @author Mathias Lux
 */
public class ACCID implements GlobalFeature {
    double[] feature = new double[120];
    private boolean doQuantize = true;


    @Override
    public void extract(BufferedImage image) {
        int width = 600;
        int height = 600;

        image = ImageUtils.scaleImage(image, width, height);
        image = ImageUtils.get8BitRGBImage(image);

        PMasks myPMasks = new PMasks();

        double[][] smapF = createSmap(image, myPMasks.getPmasks(), myPMasks.getMaskWhite());
        double[][] ThresSmall = filterF(smapF);
        double ThresImg = 0;
        double[] ThresBig = new double[6];
        double[] CVBigArea = new double[6];
        Arrays.fill(ThresBig, 0d);
        for (int ii = 0; ii < ThresSmall.length; ii++) {
            for (int jj = 0; jj < ThresSmall[ii].length; jj++) {
                ThresSmall[ii][jj] /= 100.0;
                ThresBig[ii] += ThresSmall[ii][jj];
            }
            ThresBig[ii] /= 6;
            ThresImg += ThresBig[ii];
        }
        ThresImg /= 6;

        double num;
//        double standDevImg = 0;
        for (int ii = 0; ii < CVBigArea.length; ii++) {
            num = 0;
            for (int jj = 0; jj < ThresSmall[ii].length; jj++) {
                num += (ThresSmall[ii][jj] - ThresBig[ii]) * (ThresSmall[ii][jj] - ThresBig[ii]);
            }
            num = Math.sqrt(num / (ThresSmall[ii].length - 1));
            if (ThresBig[ii] > 0)
                CVBigArea[ii] = num / ThresBig[ii];
            else
                CVBigArea[ii] = 0;
//            standDevImg += (ThresBig[ii] - ThresImg) * (ThresBig[ii] - ThresImg);
        }
//        standDevImg = Math.sqrt(standDevImg / (ThresBig.length - 1));

        double meanSmapF = 0;
        for (int i = 0; i < smapF.length; i++) {
            meanSmapF += smapF[i][1];
        }
        meanSmapF /= smapF.length;
        double standDevSmapF = 0;
        for (int i = 0; i < smapF.length; i++) {
            standDevSmapF += (smapF[i][1] - meanSmapF) * (smapF[i][1] - meanSmapF);
        }
        standDevSmapF = Math.sqrt(standDevSmapF / (smapF.length - 1));

        double CVImg = 0;
        if (meanSmapF > 0) CVImg = standDevSmapF / meanSmapF;


        BufferedImage bimg = ImageUtils.scaleImage(image, (int) (0.5 * image.getWidth()), (int) (0.5 * image.getHeight()));
        double[][] smapM = createSmap(bimg, myPMasks.getPmasks(), myPMasks.getMaskWhite());
        double[] ThresBigm = filterM(smapM);
//        double ThresImgm = 0;
//        for (int i = 0; i < ThresBigm.length; i++) {
//            ThresImgm += ThresBigm[i];
//        }
//        ThresImgm /= ThresBigm.length;
        double ThresImgm = 0;
        for (int i = 0; i < smapM.length; i++) {
            ThresImgm += smapM[i][1];
        }
        ThresImgm /= smapM.length;
//        double CVImgB = 0;
//        for (int i = 0; i < ThresBigm.length; i++) {
//            CVImgB += (ThresBigm[i] - ThresImgm) * (ThresBigm[i] - ThresImgm);
//        }
//        CVImgB = Math.sqrt(CVImgB / (ThresBigm.length - 1))/ThresImgm;
        double CVImgB = 0;
        for (int i = 0; i < smapM.length; i++) {
            CVImgB += (smapM[i][1] - ThresImgm) * (smapM[i][1] - ThresImgm);
        }
        if (ThresImgm > 0)
            CVImgB = Math.sqrt(CVImgB / (smapM.length - 1)) / ThresImgm;
        else
            CVImgB = 0;


        BufferedImage bimg2 = ImageUtils.scaleImage(bimg, (int) (0.5 * bimg.getWidth()), (int) (0.5 * bimg.getHeight()));
        double[][] smapS = createSmap(bimg2, myPMasks.getPmasks(), myPMasks.getMaskWhite());

        double CVImgS = filteringMethodS(smapS);
        filteringMethodF(smapF, ThresSmall, ThresBig, ThresImg, CVImg, CVImgB, CVImgS);
        filteringMethodM(smapM, ThresBigm, ThresImgm, CVImg, CVImgB, CVImgS);

        double[][] smap = ScaleFiltering(smapF, smapM, smapS);
        feature = ComputeDesc(image, smap);
    }

    private double[][] createSmap(BufferedImage img, int[][][] pmasks, int[] whiteMasks) {
        int width = img.getWidth();
        int height = img.getHeight();

        double[][] smap = new double[(height / 10) * (width / 10)][2];
        int pixel, i, j, winMask, counter = 0;
        double w, maxR, TempW, TempB;
        int[][] myTile = new int[10][10];
        for (int a = 0; a < height; a += 10) {
            for (int b = 0; b < width; b += 10) {
                i = 0;
                for (int x = a; x < a + 10; x++) {
                    j = 0;
                    for (int y = b; y < b + 10; y++) {
                        pixel = img.getRGB(y, x);
                        myTile[j][i] = (int) Math.round(0.299d * ((pixel >> 16) & 0xff) + 0.5870d * ((pixel >> 8) & 0xff) + 0.1140d * ((pixel) & 0xff));
                        j++;
                    }
                    i++;
                }

                maxR = 0;
                winMask = 58;
                for (int k = 0; k < 58; k++) {
                    TempW = 0;
                    TempB = 0;
                    for (int x = 0; x < 10; x++) {
                        for (int y = 0; y < 10; y++) {
                            if (pmasks[k][x][y] > 0)
                                TempW += myTile[x][y];
                            else
                                TempB += myTile[x][y];
                        }
                    }
                    TempW /= whiteMasks[k];
                    TempB /= (100 - whiteMasks[k]);
                    w = ((Math.abs(TempW - TempB) * 100) / 255.0);
                    if (w > maxR) {
                        maxR = w;
                        winMask = k;
                    }
                }
                smap[counter][0] = winMask;
                smap[counter][1] = maxR;
                counter++;
            }
        }

        return smap;
    }

    private double[][] filterF(double[][] smapF) {
        int blocks = smapF.length;
        int blocks6 = blocks / 6;
        int step = (int) Math.sqrt(blocks);
        int step6 = step / 6;
        int z, start, end;
        double[][] smallAreas = new double[6][6];
        for (int a = 0; a < 6; a++) {
            start = a * blocks6;
            end = start + blocks6;
            for (int x = start; x < end; x += step) {
                for (int y = 0; y < step6; y++) {
                    z = x + y;
                    if (a < 3) {
                        smallAreas[0][a * 2] += smapF[z][1];
                        smallAreas[0][a * 2 + 1] += smapF[z + step6][1];

                        smallAreas[1][a * 2] += smapF[z + 2 * step6][1];
                        smallAreas[1][a * 2 + 1] += smapF[z + 3 * step6][1];

                        smallAreas[2][a * 2] += smapF[z + 4 * step6][1];
                        smallAreas[2][a * 2 + 1] += smapF[z + 5 * step6][1];
                    } else {
                        smallAreas[3][(a - 3) * 2] += smapF[z][1];
                        smallAreas[3][(a - 3) * 2 + 1] += smapF[z + step6][1];

                        smallAreas[4][(a - 3) * 2] += smapF[z + 2 * step6][1];
                        smallAreas[4][(a - 3) * 2 + 1] += smapF[z + 3 * step6][1];

                        smallAreas[5][(a - 3) * 2] += smapF[z + 4 * step6][1];
                        smallAreas[5][(a - 3) * 2 + 1] += smapF[z + 5 * step6][1];
                    }
                }
            }
        }

        return smallAreas;
    }

    private double[] filterM(double[][] smapM) {
        int z, Blocks = smapM.length;
        int Blocks2 = Blocks / 2;
        int Blocks4 = Blocks / 4;
        int step = (int) Math.sqrt(Blocks);
        int step2 = step / 2;

        double[] AreasQuarters = new double[4];
        Arrays.fill(AreasQuarters, 0d);

        for (int x = 0; x < Blocks2; x += step) {
            for (int y = 0; y < step2; y++) {
                z = y + x;
                AreasQuarters[0] += smapM[z][1];
                AreasQuarters[1] += smapM[z + step2][1];
                AreasQuarters[2] += smapM[z + Blocks2][1];
                AreasQuarters[3] += smapM[z + step2 + Blocks2][1];

            }
        }
        AreasQuarters[0] /= Blocks4;
        AreasQuarters[1] /= Blocks4;
        AreasQuarters[2] /= Blocks4;
        AreasQuarters[3] /= Blocks4;

        return AreasQuarters;
    }

    private void filteringMethodF(double[][] smap, double[][] ThresSmall, double[] ThresBig, double ThresImg, double CVImg, double CVImgB, double CVImgS) {
        int blocks = smap.length;
        int blocks6 = blocks / 6;
        int step = (int) Math.sqrt(blocks);
        int step6 = step / 6;

        if (CVImg < 1) {
            ThresImg = ThresImg * (1 + ((CVImg + CVImgB + CVImgS) / 3));
        } else {
            ThresImg = ThresImg * CVImg;
        }

        for (int x = 0; x < 6; x++) {
            if (ThresImg >= ThresBig[x]) {
                ThresBig[x] = ThresBig[x] * (1 + (1 - (ThresBig[x] / ThresImg)));
            } else {
                ThresBig[x] = ThresImg * (1 + (1 - (ThresImg / ThresBig[x])));
            }

            for (int y = 0; y < 6; y++) {
                if (ThresSmall[x][y] < ThresBig[x]) {
                    if (ThresBig[x] < 5) {
                        ThresSmall[x][y] = ThresBig[x] + 1;
                    } else {
                        ThresSmall[x][y] = ThresBig[x];
                    }
                }
            }
        }

        int start, end, k, l;
        for (int a = 0; a < 6; a++) {
            start = a * blocks6;
            end = start + blocks6;
            for (int x = start; x < end; x += 60) {
                for (int y = 0; y < step6; y++) {
                    k = x + y;
                    if (a < 3) {
                        l = a * 2;
                        if (smap[k][1] < ThresSmall[0][l])
                            smap[k][0] = 58;
                        if (smap[k + step6][1] < ThresSmall[0][l + 1])
                            smap[k + step6][0] = 58;
                        if (smap[k + 2 * step6][1] < ThresSmall[1][l])
                            smap[k + 2 * step6][0] = 58;
                        if (smap[k + 3 * step6][1] < ThresSmall[1][l + 1])
                            smap[k + 3 * step6][0] = 58;
                        if (smap[k + 4 * step6][1] < ThresSmall[2][l])
                            smap[k + 4 * step6][0] = 58;
                        if (smap[k + 5 * step6][1] < ThresSmall[2][l + 1])
                            smap[k + 5 * step6][0] = 58;
                    } else {
                        l = (a - 3) * 2;
                        if (smap[k][1] < ThresSmall[3][l])
                            smap[k][0] = 58;
                        if (smap[k + step6][1] < ThresSmall[3][l + 1])
                            smap[k + step6][0] = 58;
                        if (smap[k + 2 * step6][1] < ThresSmall[4][l])
                            smap[k + 2 * step6][0] = 58;
                        if (smap[k + 3 * step6][1] < ThresSmall[4][l + 1])
                            smap[k + 3 * step6][0] = 58;
                        if (smap[k + 4 * step6][1] < ThresSmall[5][l])
                            smap[k + 4 * step6][0] = 58;
                        if (smap[k + 5 * step6][1] < ThresSmall[5][l + 1])
                            smap[k + 5 * step6][0] = 58;
                    }
                }
            }
        }
    }

    private void filteringMethodM(double[][] smap, double[] ThressBigm, double ThressImgm, double CVImg, double CVImgB, double CVImgS) {
        int Blocks = smap.length;
        int Blocks2 = Blocks / 2;
        int step = (int) Math.sqrt(Blocks);
        int step2 = step / 2;

        if (CVImgB < 1) {
            ThressImgm = ThressImgm * (1 + ((CVImg + CVImgB + CVImgS) / 3));
        } else {
            ThressImgm = ThressImgm * CVImgB;
        }

        for (int x = 0; x < 4; x++) {
            if (ThressImgm >= ThressBigm[x]) {
                ThressBigm[x] = ThressBigm[x] * (1 + (1 - (ThressBigm[x] / ThressImgm)));
            } else {
                ThressBigm[x] = ThressImgm * (1 + (1 - (ThressImgm / ThressBigm[x])));
            }
        }

        int z;
        for (int x = 0; x < Blocks2; x += step) {
            for (int y = 0; y < step2; y++) {
                z = x + y;
                if (smap[z][1] < ThressBigm[0])
                    smap[z][0] = 58;

                if (smap[z + step2][1] < ThressBigm[1])
                    smap[z + step2][0] = 58;

                if (smap[z + Blocks2][1] < ThressBigm[2])
                    smap[z + Blocks2][0] = 58;

                if (smap[z + Blocks2 + step2][1] < ThressBigm[3])
                    smap[z + Blocks2 + step2][0] = 58;
            }
        }
    }

    private double filteringMethodS(double[][] smap) {
        double meanImgS = 0;
        for (int i = 0; i < smap.length; i++) {
            meanImgS += smap[i][1];
        }
        meanImgS /= smap.length;
//        meanImgS = meanImgS -(meanImgS / 10);

        double CVImgS = 0;
        for (int i = 0; i < smap.length; i++) {
            CVImgS += (smap[i][1] - meanImgS) * (smap[i][1] - meanImgS);
        }
        CVImgS = Math.sqrt(CVImgS / (smap.length - 1)) / meanImgS;

        meanImgS = meanImgS * 0.9;


        for (int x = 0; x < smap.length; x++) {
            if (smap[x][1] < meanImgS) {
                smap[x][0] = 58;
            }
        }

        return CVImgS;
    }

    private double[][] ScaleFiltering(double[][] smapF, double[][] smapM, double[][] smapS) {
        double[][] smapFsm = new double[smapF.length][smapF[0].length];
        double[][] smapFm = new double[smapF.length][smapF[0].length];
        double[][] smapUn = new double[smapF.length][smapF[0].length];

        for (int i = 0; i < smapF.length; i++) {
            smapFsm[i][0] = smapF[i][0];
            smapFsm[i][1] = smapF[i][1];
            smapFm[i][0] = smapF[i][0];
            smapFm[i][1] = smapF[i][1];
            smapUn[i][0] = smapF[i][0];
            smapUn[i][1] = smapF[i][1];
        }

        int z, f;
        for (int b = 0; b < 15; b++) {
            for (int a = 0; a < 60; a++) {
                z = a + b * 4 * 60;
                f = (int) Math.floor(a / 4) + 15 * b;
                if (smapS[f][0] == 58) {
                    smapF[z][0] = 58;
                    smapF[z + 60][0] = 58;
                    smapF[z + 2 * 60][0] = 58;
                    smapF[z + 3 * 60][0] = 58;
                }
            }
        }

        for (int b = 0; b < 30; b++) {
            for (int a = 0; a < 60; a++) {
                z = a + b * 2 * 60;
                f = (int) Math.floor(a / 2) + 30 * b;
                if (smapM[f][0] == 58) {
                    smapFm[z][0] = 58;
                    smapFm[z + 60][0] = 58;
                }
            }
        }

        for (int w = 0; w < smapF.length; w++) {
            if ((smapFm[w][0] == 58) && (smapF[w][0] == 58) && (smapUn[w][0] != 58)) smapFsm[w][0] = 58;
            if (smapFsm[w][1] == 0) smapFsm[w][1] = 30;
        }

        return smapFsm;
    }

    private double[] ComputeDesc(BufferedImage image, double[][] smap) {
        double[] desc = new double[120];
        int j, num;

        Fuzzy10Bin Fuzzy10 = new Fuzzy10Bin(false);
        Fuzzy24Bin Fuzzy24 = new Fuzzy24Bin(false);
        RGB2HSV HSVConverter = new RGB2HSV();

        int[] HSV;
        double[] Fuzzy10BinResultTable;
        double[] Fuzzy24BinResultTable;

        int width = image.getWidth();
        int height = image.getHeight();

        int pixel, R, G, B, counter = 0;
        for (int a = 0; a < height; a += 10) {
            for (int b = 0; b < width; b += 10) {
                R = 0;
                G = 0;
                B = 0;
                for (int xx = a; xx < a + 10; xx++) {
                    for (int yy = b; yy < b + 10; yy++) {
                        pixel = image.getRGB(yy, xx);
                        R += (pixel >> 16) & 0xff;
                        G += (pixel >> 8) & 0xff;
                        B += (pixel) & 0xff;
                    }
                }
                HSV = HSVConverter.ApplyFilter(R / 100, G / 100, B / 100);
                Fuzzy10BinResultTable = Fuzzy10.ApplyFilter(HSV[0], HSV[1], HSV[2], 2);
                Fuzzy24BinResultTable = Fuzzy24.ApplyFilter(HSV[0], HSV[1], HSV[2], Fuzzy10BinResultTable, 2);

                num = (int) smap[counter][0];
                if ((num >= 0) && (num <= 4)) j = 0;
                else if ((num >= 5) && (num <= 8)) j = 0;
                else if ((num >= 9) && (num <= 13)) j = 1;
                else if ((num >= 14) && (num <= 19)) j = 1;
                else if ((num >= 20) && (num <= 24)) j = 1;
                else if ((num >= 25) && (num <= 28)) j = 2;
                else if ((num >= 29) && (num <= 33)) j = 2;
                else if ((num >= 34) && (num <= 37)) j = 2;
                else if ((num >= 38) && (num <= 42)) j = 3;
                else if ((num >= 43) && (num <= 48)) j = 3;
                else if ((num >= 49) && (num <= 53)) j = 3;
                else if ((num >= 54) && (num <= 57)) j = 0;
                else j = 4;
                for (int ii = 0; ii < 24; ii++) {
                    desc[j * Fuzzy24BinResultTable.length + ii] += Fuzzy24BinResultTable[ii] * (smap[counter][1] / 100);
                }
                counter++;
            }
        }
        if (doQuantize) desc = quantizeFeature(desc);
        return desc;
    }

    private double[] quantizeFeature(double[] desc) {
        desc = MetricsUtils.normalizeMax(desc);
        for (int i = 0; i < desc.length; i++) {
            desc[i] = Math.floor(desc[i] * (double) Short.MAX_VALUE);
        }
        return desc;
    }


    @Override
    public byte[] getByteArrayRepresentation() {
        byte[] result = new byte[feature.length * 2];
        for (int i = 0; i < feature.length; i += 1) { // convert short to byte ...
            result[2 * i] = (byte) ((((short) feature[i]) >> 8) & 0xff);
            result[2 * i + 1] = (byte) (((short) feature[i]) & 0xff);
        }
        return result;
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        setByteArrayRepresentation(in, 0, in.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        for (int i = 0; i < feature.length; i++) {
            feature[i] = (in[2 * i + offset] << 8) | in[2 * i + 1 + offset]&0xff;
        }
    }

    @Override
    public double[] getFeatureVector() {
        return feature;
    }

    @Override
    public double getDistance(LireFeature f) {
        if (!(f instanceof ACCID)) throw new UnsupportedOperationException("Wrong descriptor.");
        return MetricsUtils.jsd(feature, ((ACCID) f).feature);
    }

    @Override
    public String getFeatureName() {
        return "ACCID";
    }

    @Override
    public String getFieldName() {
        return DocumentBuilder.FIELD_NAME_ACCID;
    }
}
