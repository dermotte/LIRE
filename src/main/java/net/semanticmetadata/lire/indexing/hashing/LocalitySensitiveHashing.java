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

package net.semanticmetadata.lire.indexing.hashing;

import java.io.*;

/**
 * <p>Each feature vector v with dimension d gets k hashes from a hash bundle h(v) = (h^1(v), h^2(v), ..., h^k(v)) with
 * h^i(v) = (a^i*v + b^i)/w (rounded down), with a^i from R^d and b^i in [0,w) <br/>
 * If m of the k hashes match, then we assume that the feature vectors belong to similar images. Note that m*k has to be bigger than d!<br/>
 * If a^i is drawn from a normal (Gaussian) distribution LSH approximates L2. </p>
 * <p/>
 * Note that this is just to be used with bounded (normalized) descriptors.
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Created: 04.06.12, 13:42
 */
public class LocalitySensitiveHashing {
    private static String name = "lshHashFunctions.obj";
    private static int dimensions = 512;           // max d
    public static int numFunctionBundles = 25;     // k
    public static double binLength = 0.01;         // w

    private static double[][] hashA = null;      // a
    private static double[] hashB = null;        // b
    private static double dilation = 1d;         // defines how "stretched out" the hash values are.

    /**
     * Writes a new file to disk to be read for hashing with LSH.
     *
     * @throws java.io.IOException
     */
    public static void generateHashFunctions() throws IOException {
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(name));
        oos.writeInt(dimensions);
        oos.writeInt(numFunctionBundles);
        for (int c = 0; c < numFunctionBundles; c++) {
            oos.writeDouble(Math.random() * binLength);
        }
        for (int c = 0; c < numFunctionBundles; c++) {
            for (int j = 0; j < dimensions; j++) {
                oos.writeDouble(drawNumber() * dilation);
            }
        }
        oos.close();
    }

    /**
     * Reads a file from disk and sets the hash functions.
     *
     * @return
     * @throws IOException
     * @see LocalitySensitiveHashing#generateHashFunctions()
     */
    public static double[][] readHashFunctions() throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(name));
        int dimensions = ois.readInt();
        int numFunctionBundles = ois.readInt();
        double[] tmpB = new double[numFunctionBundles];
        for (int k = 0; k < numFunctionBundles; k++) {
            tmpB[k] = ois.readDouble();
        }
        LocalitySensitiveHashing.hashB = tmpB;
        double[][] hashFunctions = new double[numFunctionBundles][dimensions];
        for (int i = 0; i < hashFunctions.length; i++) {
            double[] functionBundle = hashFunctions[i];
            for (int j = 0; j < functionBundle.length; j++) {
                functionBundle[j] = ois.readDouble();
            }
        }
        LocalitySensitiveHashing.hashA = hashFunctions;
        return hashFunctions;
    }

    /**
     * Generates the hashes from the given hash bundles.
     *
     * @param histogram
     * @return
     */
    public static int[] generateHashes(double[] histogram) {
        double product = 0d;
        int[] result = new int[numFunctionBundles];
        for (int k = 0; k < numFunctionBundles; k++) {
            for (int i = 0; i < histogram.length; i++) {
                product += histogram[i] * hashA[k][i];
            }
            result[k] = (int) Math.floor((product + hashB[k]) / binLength);
        }
        return result;
    }


    /**
     * Returns a random number distributed with standard normal distribution based on the Box-Muller method.
     *
     * @return
     */
    private static double drawNumber() {
        return Math.sqrt(-2 * Math.log(Math.random())) * Math.cos(2 * Math.PI * Math.random());
    }

    public static void main(String[] args) {
        try {
            generateHashFunctions();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }


}
