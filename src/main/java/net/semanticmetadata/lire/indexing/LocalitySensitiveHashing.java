package net.semanticmetadata.lire.indexing;

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
    private static int dimensions = 512;         // max d
    public static int numFunctionBundles = 25;   // k
    public static double binLength = 1.8d;         // w

    private static double[][] hashA = null;      // a
    private static double[] hashB = null;        // b
    private static double dilation = 1d;        // defines how "stretched out" the hash values are.

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
     * @see net.semanticmetadata.lire.indexing.LocalitySensitiveHashing#generateHashFunctions()
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
