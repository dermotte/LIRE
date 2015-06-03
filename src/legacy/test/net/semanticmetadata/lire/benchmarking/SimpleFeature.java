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
 * Updated: 11.07.13 10:43
 */

package net.semanticmetadata.lire.benchmarking;

import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;

import java.awt.image.BufferedImage;

public class SimpleFeature implements LireFeature {
    double[] hist;

    @Override
    public void extract(BufferedImage bimg) {
        throw new UnsupportedOperationException("not implemented!");
    }

    @Override
    public byte[] getByteArrayRepresentation() {
        return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setByteArrayRepresentation(byte[] in) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public double[] getDoubleHistogram() {
        return hist;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public float getDistance(LireFeature feature) {
        double k1 = 1.2, b = 0.75;
        double[] hist1 = new double[hist.length], hist2 = new double[hist.length];
        System.arraycopy(hist, 0, hist1, 0, hist.length);
        System.arraycopy(((SimpleFeature) feature).hist, 0, hist2, 0, hist.length);
        // weighting ...
//        double d1=0d, d2=0d;
//        for (int i = 0; i < hist1.length; i++)
//            d1 += hist1[i];
//        for (int i = 0; i < hist2.length; i++)
//            d2 += hist2[i];
        for (int i = 0; i < hist1.length; i++) {
//            hist1[i] = (hist1[i]*(k1+1.0))/(hist1[i] + k1*(1-b+b*d1/TestNister.avgDocLength)) * Math.log((10200-TestNister.df[i]+0.5)/(TestNister.df[i]+0.5));
//            hist2[i] = (hist2[i]*(k1+1.0))/(hist2[i] + k1*(1-b+b*d1/TestNister.avgDocLength)) * Math.log((10200-TestNister.df[i]+0.5)/(TestNister.df[i]+0.5));
            if (hist1[i] > 0) hist1[i] = 1 + Math.log(hist1[i]);
            if (hist2[i] > 0) hist2[i] = 1 + Math.log(hist2[i]);
        }
//        double dist = (1f - MetricsUtils.cosineCoefficient(hist1, hist2));
//        if (dist<0) {
//            System.out.println(dist);
//            dist = 0;
//        }
        return (float) MetricsUtils.distL2(hist1, hist2);
    }

    @Override
    public String getStringRepresentation() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setStringRepresentation(String s) {
        hist = SerializationUtils.doubleArrayFromString(s);
    }

    @Override
    public String getFeatureName() {
        return "Simple Test Feature";
    }

    @Override
    public String getFieldName() {
        return "f_sitfe";
    }
}
