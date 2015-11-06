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
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval -
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
 */

package net.semanticmetadata.lire.imageanalysis.features.local.selfsimilarities;

import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 * Implementation based on the paper:
 * Matching Local Self-Similarities across Images and Videos
 *
 * Created by Nektarios on 12/1/2015.
 *
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 * (c) 2015 by Nektarios Anagnostopoulos
 */
public class SelfSimilaritiesOrigExtractor implements LocalFeatureExtractor {

    private int density = 5;
    private int size=5;
    private int coRelWindowRadius=10;
    private int numRadiiIntervals=2;
    private int numThetaIntervals=4;
    private int varNoise=(25*3*36);
    private int autoVarRadius=1;
    private int saliencyThresh=0; //I usually disable saliency checking

    LinkedList<SelfSimilaritiesOrigFeature> features = null;

    public SelfSimilaritiesOrigExtractor(){
    }

    public void extract(BufferedImage img) {
        int nChannels=img.getColorModel().getNumColorComponents();
        int radius = (size-1)/2; // the radius of the patch
        int marg = radius+coRelWindowRadius;
        int NUMradius=(size-1)/2; //the radius of the patch ~ Note: size should be odd only
        int NUMpixels=size*size;

        int NUMcols=img.getWidth(); //width
        int NUMrows=img.getHeight(); //height
        int pixel;
        int[][] ImageGridRed = new int[NUMcols][NUMrows];
        int[][] ImageGridGreen = new int[NUMcols][NUMrows];
        int[][] ImageGridBlue = new int[NUMcols][NUMrows];
        for (int x = 0; x < NUMcols; x++) {
            for (int y = 0; y < NUMrows; y++) {
                pixel = img.getRGB(x, y);
                ImageGridRed[x][y] = (pixel >> 16) & 0xff;
                ImageGridGreen[x][y] = (pixel >> 8) & 0xff;
                ImageGridBlue[x][y] = (pixel) & 0xff;
            }
        }

        if (autoVarRadius > coRelWindowRadius) {
            System.out.println("Incorrect data, autoVarRadius cant be greater than coRelWindowRadius");
            return;
        }

        int interiorH=NUMrows-2*NUMradius;
        int numPixels=(2*NUMradius+1)*(2*NUMradius+1); // patchRad = NUMradius
        int dim = (coRelWindowRadius*2 + 1);
        int[] xGrid = new int[dim*dim];
        int[] yGrid = new int[dim*dim];
        int tempCoRelWindowRadiusY, tempCoRelWindowRadiusX = coRelWindowRadius;
        int counter = 0;
        for (int i = 0; i < dim; i++) {
            tempCoRelWindowRadiusY = coRelWindowRadius;
            for (int j = 0; j < dim; j++) {
                xGrid[counter]=(-1)*tempCoRelWindowRadiusX;
                yGrid[counter]=tempCoRelWindowRadiusY;
                tempCoRelWindowRadiusY--;
                counter++;
            }
            tempCoRelWindowRadiusX--;
        }

        dim=dim*dim;
        int[] circleMask = new int[dim];
        int[] autoVarMask = new int[dim];
        for (int i = 0; i < dim; i++) {
            if ((xGrid[i]*xGrid[i])+(yGrid[i]*yGrid[i])<=coRelWindowRadius*coRelWindowRadius)
                circleMask[i]=1;
            else
                circleMask[i]=0;
            if ((xGrid[i]*xGrid[i])+(yGrid[i]*yGrid[i])<=autoVarRadius*autoVarRadius)
                autoVarMask[i]=1;
            else
                autoVarMask[i]=0;
        }
        circleMask[(dim - 1) / 2]=0;
        autoVarMask[(dim - 1) / 2]=0;

        int[] radii = new int[dim];
        double[] thetas = new double[dim];
        for (int i = 0; i < dim; i++) {
            circleMask[i]=circleMask[i]+autoVarMask[i];
            radii[i]=(xGrid[i]*xGrid[i])+(yGrid[i]*yGrid[i]);
            thetas[i]=0;
        }

        int xQuad, yQuad, xC, yC, quad;
        for (int x = 0; x < dim; x++) {
            xC = xGrid[x];
            yC = yGrid[x];
            if (xC >= 0) xQuad=1; else xQuad=0;
            if (yC >= 0) yQuad=1; else yQuad=0;
            quad = xQuad * 2 + yQuad;
            switch (quad) {
                case 0:
                    if (xC == 0)
                        thetas[x] = Math.PI;
                    else
                        thetas[x] = 1.5 * Math.PI - Math.atan((double)yC / xC);
                    break;
                case 1:
                    if (xC == 0)
                        thetas[x] = 0;
                    else
                        thetas[x] = 1.5 * Math.PI - Math.atan((double)yC / xC);
                    break;
                case 2:
                    if (xC == 0)
                        thetas[x] = Math.PI;
                    else
                        thetas[x] = 0.5 * Math.PI - Math.atan((double)yC / xC);
                    break;
                case 3:
                    if (xC == 0)
                        thetas[x] = 0;
                    else
                        thetas[x] = 0.5 * Math.PI - Math.atan((double)yC / xC);
                    break;
            }
        }

        double thetaInterval=2*Math.PI/numThetaIntervals;
        int[] thetaIndexes = new int[dim];
        for (int i = 0; i < dim; i++) {
            thetaIndexes[i]= (int)Math.floor((double) thetas[i] / thetaInterval); // 0 indexed
        }
        double radiiInterval=Math.log(1 + coRelWindowRadius)/numRadiiIntervals;

        double[] radiiQuants = new double[numRadiiIntervals];
        double num;
        for (int i = 1; i <= numRadiiIntervals-1; i++) {
            num = Math.exp(i * radiiInterval) - 1;
            radiiQuants[i-1] = num*num;
        }
        radiiQuants[numRadiiIntervals-1]=coRelWindowRadius*coRelWindowRadius;

        int[] radiiIndexes = new int[dim];
        for (int i = 0; i < dim; i++) {
            radiiIndexes[i]=0;
            for (int k = 0; k < radiiQuants.length; k++) {
                if (radii[i]<=radiiQuants[k])
                    radiiIndexes[i]++;
            }
            radiiIndexes[i]=radiiIndexes[i]-1;
        }

        int[] binIndexes = new int[dim];
        for (int i = 0; i < dim; i++) {
            binIndexes[i]=thetaIndexes[i]*radiiQuants.length+radiiIndexes[i];
        }

        LinkedList<Integer> nonzero = new LinkedList<Integer>();
        for (int i = 0; i < dim; i++) {
            if (circleMask[i]>0) nonzero.add(i);
        }

        int[] xGridVN = new int[nonzero.size()];
        int[] yGridVN = new int[nonzero.size()];
        int[] circleMaskVN = new int[nonzero.size()];
        int[] binIndexesVN = new int[nonzero.size()];
        int pointer;
        for (int i = 0; i < nonzero.size(); i++) {
            pointer = nonzero.get(i);
            xGridVN[i]=xGrid[pointer];
            yGridVN[i]=yGrid[pointer];
            circleMaskVN[i]=circleMask[pointer];
            binIndexesVN[i]=binIndexes[pointer];
        }

        int[] coRelCircleOffsets = new int[nonzero.size()];
        LinkedList<Integer> autoVarianceIndices = new LinkedList<Integer>();
        for (int i = 0; i < nonzero.size(); i++) {
            coRelCircleOffsets[i]=xGridVN[i]*interiorH+yGridVN[i];
            if (circleMaskVN[i]==2) autoVarianceIndices.add(i);
        }

        int totalBins = numRadiiIntervals*numThetaIntervals;
        int[][] binIndices = new int[totalBins][];
        LinkedList<Integer> myList;
        int[] tempList;
        for (int i = 0; i < totalBins; i++) {
            myList = new LinkedList<Integer>();
            for (int j = 0; j < binIndexesVN.length; j++) {
                if (binIndexesVN[j]==i) myList.add(j);
            }
            tempList = new int[myList.size()];
            for (int j = 0; j < myList.size(); j++) {
                tempList[j]=myList.get(j);
            }
            binIndices[i] = tempList;
        }

        int numAutoVarianceIndices = autoVarianceIndices.size();
        int numSSDs = coRelCircleOffsets.length;

        double[][] simMaps= new double[(((NUMrows-marg*2-1)/density)+1)*(((NUMcols-marg*2-1)/density)+1)][totalBins];
        int[] allXCoords = new int[(((NUMrows-marg*2-1)/density)+1)*(((NUMcols-marg*2-1)/density)+1)];
        int[] allYCoords = new int[(((NUMrows-marg*2-1)/density)+1)*(((NUMcols-marg*2-1)/density)+1)];
        double[] ssdTraveller = new double[xGridVN.length];
        int diff;
        counter=0;
        int fromI = marg - NUMradius;
        int toI = NUMcols-marg-NUMradius;
        int fromJ = marg - NUMradius;
        int toJ = NUMrows-marg-NUMradius;
        for (int i = fromI; i < toI; i+=density) {
            for (int j = fromJ; j < toJ; j+=density) {
                allXCoords[counter] = i;
                allYCoords[counter] = j;
                ssdTraveller = new double[xGridVN.length];
                for (int ii = 0; ii < xGridVN.length; ii++) {
                    ssdTraveller[ii] = 0;
                    for (int k = 0; k < size; k++) {
                        for (int l = 0; l < size; l++) {
                            diff = ImageGridRed[i + k][j + l] - ImageGridRed[i + k + xGridVN[ii]][j + l + yGridVN[ii]];
                            ssdTraveller[ii] += diff * diff;
                            diff = ImageGridGreen[i + k][j + l] - ImageGridGreen[i + k + xGridVN[ii]][j + l + yGridVN[ii]];
                            ssdTraveller[ii] += diff * diff;
                            diff = ImageGridBlue[i + k][j + l] - ImageGridBlue[i + k + xGridVN[ii]][j + l + yGridVN[ii]];
                            ssdTraveller[ii] += diff * diff;
                        }
                    }
                }

                double autoQ = 0;
                for (int jj = 0; jj < numAutoVarianceIndices; jj++)
                    autoQ = (ssdTraveller[autoVarianceIndices.get(jj)] > autoQ) ? ssdTraveller[autoVarianceIndices.get(jj)] : autoQ;

                double divisor = (autoQ > varNoise) ? autoQ : varNoise;
                for (int k = 0; k < numSSDs; k++)
                    ssdTraveller[k] = Math.exp(-1 * (ssdTraveller[k]) / divisor);

                double max;
                for (int l = 0; l < totalBins; l++) {
                    max = ssdTraveller[binIndices[l][0]];
                    for (int jj = 1; jj < binIndices[l].length; jj++) {
                        if (ssdTraveller[binIndices[l][jj]] > max) max = ssdTraveller[binIndices[l][jj]];
                    }
                    simMaps[counter][l] = max;
                }
                counter++;
            }
        }

        int threshMaps;
        LinkedList<Integer> indsSalient = new LinkedList<Integer>();
        LinkedList<Integer> indsOkay = new LinkedList<Integer>();
        //indsUniform=[];

        for (int i = 0; i < simMaps.length; i++) {
            threshMaps = 0;
            for (int j = 0; j < totalBins; j++) {
                if (simMaps[i][j] >= saliencyThresh) threshMaps++;
            }
            if (threshMaps == 0) indsSalient.add(i);
            if (threshMaps != 0) indsOkay.add(i);
        }
        double[] newMaps;
        double max;
        features = new LinkedList<SelfSimilaritiesOrigFeature>();
        double mySize = size + coRelWindowRadius * 2;
        SelfSimilaritiesOrigFeature feat;
        for (int i = 0; i < indsOkay.size(); i++) {
            newMaps= new double[totalBins];
            max = simMaps[indsOkay.get(i)][0];
            for (int j = 0; j < totalBins; j++) {
                newMaps[j] =  simMaps[indsOkay.get(i)][j];
                if (simMaps[indsOkay.get(i)][j]>max) max = simMaps[indsOkay.get(i)][j]; //Takes the max along the columns
            }
            for (int j = 0; j < totalBins; j++) {
                newMaps[j] = newMaps[j] / max;
            }
            feat = new SelfSimilaritiesOrigFeature(newMaps, allXCoords[indsOkay.get(i)], allYCoords[indsOkay.get(i)], mySize);
            features.add(feat);
        }

//        int[][] sCoords = new int[indsSalient.size()][2];
//        for (int i = 0; i < indsSalient.size(); i++) {
//            sCoords[i][0]=allXCoords[indsSalient.get(i)];
//            sCoords[i][1]=allYCoords[indsSalient.get(i)];
//        }

        //uCoords=allCoords(:,indsUniform);
    }

    @Override
    public List<? extends LocalFeature> getFeatures() {
        return features;
    }

    @Override
    public Class<? extends LocalFeature> getClassOfFeatures() {
        return SelfSimilaritiesOrigFeature.class;
    }
}
