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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 12.07.13 16:59
 */

package net.semanticmetadata.lire.indexers.hashing;

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
    /* Best values for PHOG: 3000 results include > 80% true positives after re-ranking in the 1str 20 results.*/
//    public static int bits = 16;
    public static double w = 4d;
    public static int numFunctionBundles = 100;

    // Optimal for ColorLayout, 1000 hashed results should be fine and include > 90% true positives after re-ranking in the 1st 20 results.
    private static int bits = 12;
//    private static double w = 4d;
//    private static int numFunctionBundles = 150;


    // Dimensions should cover the maximum dimensions of descriptors used with bit sampling
    public static int dimensions = 640;

    public static final String hashFunctionsFileName = "LshBitSampling.obj";
    private static double[][][] hashes = null;
    private static double[] lookUp = new double[32];

    static {
        for (int i = 0; i < lookUp.length; i++) {
            lookUp[i] = Math.pow(2, i);
        }
    }

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
        File hashFile = new File(hashFunctionsFileName);
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
            System.err.println("Hashes could not be written: " + hashFunctionsFileName + " already exists");
        }
    }

    public static void generateHashFunctions(String hashFunctionsFileName) throws IOException {
        File hashFile = new File(hashFunctionsFileName);
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
            System.err.println("Hashes could not be written: " + hashFunctionsFileName + " already exists");
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
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(BitSampling.class.getResourceAsStream(hashFunctionsFileName)));
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
     *
     * @param inputStream to access the data, most likely a File on a hard disk
     * @return
     * @throws IOException
     */
    public static double[][][] readHashFunctions(InputStream inputStream) throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(inputStream));
        int bits = ois.readInt();
        int dimensions = ois.readInt();
        int numFunctionBundles = ois.readInt();

        double[][][] hashFunctions = new double[numFunctionBundles][bits][dimensions];
        for (int i = 0; i < hashFunctions.length; i++) {
            double[][] functionBundle = hashFunctions[i];
            for (int j = 0; j < functionBundle.length; j++) {
                double[] bitFunctions = functionBundle[j];
                for (int k = 0; k < bitFunctions.length; k++) {
                    bitFunctions[k] = ois.readFloat();
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
                hashResults[i] += lookUp[j] * (val < 0 ? 0 : 1);
            }
        }
        return hashResults;
    }

    public static void setW(double w) {
        BitSampling.w = w;
    }

    public static void setNumFunctionBundles(int numFunctionBundles) {
        BitSampling.numFunctionBundles = numFunctionBundles;
    }

    public static int getBits() {
        return bits;
    }

    public static void setBits(int bits) {
        BitSampling.bits = bits;
    }

    public static double getW() {
        return w;
    }

    public static int getNumFunctionBundles() {
        return numFunctionBundles;
    }
}
