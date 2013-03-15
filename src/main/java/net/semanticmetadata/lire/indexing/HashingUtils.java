package net.semanticmetadata.lire.indexing;

import java.io.*;

/**
 * Provides a simple way to hashing ... based on min-hash.
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Created: 24.02.12, 14:00
 */
public class HashingUtils {
    private static int bits = 16, dimensions = 512;
    private static int numFunctionBundles = 20;
    private static String name = "hashFunctions.obj";
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
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(name));
        oos.writeInt(bits);
        oos.writeInt(dimensions);
        oos.writeInt(numFunctionBundles);
        for (int c = 0; c < numFunctionBundles; c++) {
            for (int i = 0; i < bits; i++) {
                for (int j = 0; j < dimensions; j++) {
                    oos.writeDouble((Math.random() * w - w/2));
                }
            }
        }
        oos.close();
    }

    /**
     * Reads a file from disk, where the hash bundles are specified. Make sure to generate it first
     * and make sure to re-use it for search.
     *
     * @return
     * @throws IOException
     */
    public static double[][][] readHashFunctions() throws IOException {
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(name));
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
        HashingUtils.hashes = hashFunctions;
        return hashFunctions;
    }

    /**
     * Generates and returns the hashes for a given histogram input.
     *
     * @param histogram
     * @return
     */
    public static int[] generateHashes(double[] histogram) {
        double val = 0d;
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
