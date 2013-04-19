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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Provides a simple way to hashing. It's bit sampling and can be put into the
 * locality sensitive hashing family of hashing functions.
 * <p/>
 * Created: 24.02.12, 14:00
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class BitSampling {
    private static int bits = 16, dimensions = 640;  // todo: reduce dimensions & numFunctionBundles ... test if recall changes too much.
    private static int numFunctionBundles = 40;
    private static String name = "LshBitSampling.obj";
    private static double w = 4d;
    private static double[][][] hashes = null;

    /**
     * Generate new hash functions.
     *
     * @param args
     */
    public static void main(String[] args) {
        try {
            generateHashFunctions();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Writes a file to disk to be read for hashing.
     *
     * @throws IOException
     */
    public static void generateHashFunctions() throws IOException {
        File hashFile = new File(name);
        if (!hashFile.exists()) {
            ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(new FileOutputStream(hashFile)));
            oos.writeInt(bits);
            oos.writeInt(dimensions);
            oos.writeInt(numFunctionBundles);
            for (int c = 0; c < numFunctionBundles; c++) {
                for (int i = 0; i < bits; i++) {
                    for (int j = 0; j < dimensions; j++) {
                        oos.writeFloat((float) (Math.random() * w - w / 2));
                    }
                }
            }
            oos.close();
        } else {
            System.err.println("Hashes could not be written: " + name + " already exists");
        }
    }

    /**
     * Reads a file from disk, where the hash bundles are specified. Make sure to generate it first
     * and make sure to re-use it for search. This method reads the in class specified file relative
     * to the execution directory.
     *
     * @return
     * @throws IOException
     */
    public static double[][][] readHashFunctions() throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(BitSampling.class.getResourceAsStream(name)));
        int bits = ois.readInt();
        int dimensions = ois.readInt();
        int numFunctionBundles = ois.readInt();

        double[][][] hashFunctions = new double[numFunctionBundles][bits][dimensions];
        for (int i = 0; i < hashFunctions.length; i++) {
            double[][] functionBundle = hashFunctions[i];
            for (int j = 0; j < functionBundle.length; j++) {
                double[] bitFunctions = functionBundle[j];
                for (int k = 0; k < bitFunctions.length; k++) {
                    bitFunctions[k] = (double) ois.readFloat();
                }
            }
        }
        BitSampling.hashes = hashFunctions;
        return hashFunctions;
    }

    /**
     * Reads a file from a given InputStream, where the hash bundles are specified. Make sure to generate it first
     * and make sure to re-use it for search.
     * @param inputStream to access the data, most likely a File on a hard disk
     * @return
     * @throws IOException
     */
    public static double[][][] readHashFunctions(InputStream inputStream) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(new FileInputStream(name)));
        int bits = ois.readInt();
        int dimensions = ois.readInt();
        int numFunctionBundles = ois.readInt();

        double[][][] hashFunctions = new double[numFunctionBundles][bits][dimensions];
        for (int i = 0; i < hashFunctions.length; i++) {
            double[][] functionBundle = hashFunctions[i];
            for (int j = 0; j < functionBundle.length; j++) {
                double[] bitFunctions = functionBundle[j];
                for (int k = 0; k < bitFunctions.length; k++) {
                    bitFunctions[k] = ois.readDouble();
                }
            }
        }
        BitSampling.hashes = hashFunctions;
        return hashFunctions;
    }

    /**
     * Generates and returns the hashes for a given histogram input.
     *
     * @param histogram
     * @return
     */
    public static int[] generateHashes(double[] histogram) {
        double val;
        int[] hashResults = new int[hashes.length];
        for (int i = 0; i < hashes.length; i++) {
            double[][] hashBundle = hashes[i];
            for (int j = 0; j < hashBundle.length; j++) {
                val = 0d;
                double[] hashBit = hashBundle[j];
                for (int k = 0; k < histogram.length; k++) {
                    val += hashBit[k] * histogram[k];
                }
                hashResults[i] += Math.pow(2, j) * (Math.signum(val) < 0 ? 0 : 1);
            }
        }
        return hashResults;
    }


}
