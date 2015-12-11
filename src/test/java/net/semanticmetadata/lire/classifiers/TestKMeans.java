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

package net.semanticmetadata.lire.classifiers;

import junit.framework.TestCase;

import java.util.Random;

/**
 * Created by lazaros on 11/12/2015.
 *
 * @author Lazaros Tsochatzidis, ltsochat@ee.duth.gr
 */
public class TestKMeans extends TestCase {
    public void testKMeans() throws Exception {
        //TestCase parameters
        int dimensionality=128;
        int Nclusters=50;
        int Ndata=10000;

        // Populating KMeans
        KMeans kMeans=new KMeans(Nclusters);
        Random rand=new Random();
        double[] point;
        for (int i=0;i<Ndata;i++) {
            point=new double[dimensionality];
            for (int j=0;j<dimensionality;j++){
                point[j]=rand.nextDouble();
            }
            kMeans.addFeature(point);
        }

        //Clustering
        kMeans.init();
        System.out.println("Step.");
        double threshold = Math.max(20.0D, (double) kMeans.getFeatureCount() / 1000.0D);
        double err1 = kMeans.clusteringStep();
        while(err1 > threshold) {
            System.out.println(" -> Next step. Stress difference ~ " + (int) err1);
            err1 = kMeans.clusteringStep();
        }
    }

    public void testParallelKMeans() throws Exception {
        //TestCase parameters
        int dimensionality=128;
        int Nclusters=50;
        int Ndata=10000;

        // Populating KMeans
        KMeans kMeans=new ParallelKMeans(Nclusters);
        Random rand=new Random();
        double[] point;
        for (int i=0;i<Ndata;i++) {
            point=new double[dimensionality];
            for (int j=0;j<dimensionality;j++){
                point[j]=rand.nextDouble();
            }
            kMeans.addFeature(point);
        }

        //Clustering
        kMeans.init();
        System.out.println("Step.");
        double threshold = Math.max(20.0D, (double) kMeans.getFeatureCount() / 1000.0D);
        double err1 = kMeans.clusteringStep();
        while(err1 > threshold) {
            System.out.println(" -> Next step. Stress difference ~ " + (int) err1);
            err1 = kMeans.clusteringStep();
        }
    }
}
