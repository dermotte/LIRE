package net.semanticmetadata.lire.indexers.hashing;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.*;

/**
 * This hashing approach implements the proximity approach "metric spaces" based on the work of Giuseppe Amato.
 * See Gennaro, Claudio, et al. "An approach to content-based image retrieval based on the Lucene search engine
 * library." Research and Advanced Technology for Digital Libraries. Springer Berlin Heidelberg, 2010. 55-66.
 * <p/>
 * While it's actually not a hashing routine it's an easy way to use it within LIRE. Based on a pre-trained set
 * of representatives, the features of the representatives are stored in a text file. For indexing and search they
 * can be used to create hash like text snippets, that are then used to search & index.
 *
 * @author Mathias Lux, mathias@juggle.at, 24.08.2015.
 */
public class MetricSpaces {
//    public static int getNumberOfReferencePoints = 500;
//    public static int lengthOfPostingList = 10;

    // for actual runtime in indexing and search we need per feature index structures:
    static HashMap<String, ArrayList<GlobalFeature>> referencePoints = new HashMap<>();
    static HashMap<String, Parameters> parameters = new HashMap<>();
    private static Class<? extends GlobalFeature> featureClass = CEDD.class;

    public static void main(String[] args) {
        int numberOfReferencePoints = 500;
        int lenghtOfPostingList = 10;
        File inFile = null, outFile = null;
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-i")) {
                // infile ...
                if ((i + 1) < args.length) {
                    File f = new File(args[i + 1]);
                    if (!f.exists() || f.isDirectory()) {
                        printHelp();
                        System.err.println("Input file does not exist or is a directory.");
                        System.exit(1);
                    } else {
                        inFile = f;
                    }
                } else {
                    printHelp();
                    System.err.println("There is something wrong with your input file. Please check the parameters.");
                }
            } else if (arg.startsWith("-c")) { // which class
                if ((i + 1) < args.length) {
                    String className = args[i + 1];
                    if (!className.contains(".")) {
                        className = "net.semanticmetadata.lire.imageanalysis.features.global." + className;
                    }
                    // try loading the class:
                    try {
                        featureClass = (Class<? extends GlobalFeature>) Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        System.err.printf("Class for feature not found: %s (%s)\n", className, e.getMessage());
                        e.printStackTrace();
                    }
                } else {
                    System.err.println("There is something wrong with the parameters.");
                    printHelp();
                }
            } else if (arg.startsWith("-p")) {
                // parameters
                if ((i + 1) < args.length) {
                    String[] p = args[i + 1].split(",");
                    if (p.length < 2) {
                        System.err.println("There are too few parameters: -p " + args[i + 1]);
                        printHelp();
                    } else {
                        try {
                            numberOfReferencePoints = Integer.parseInt(p[0]);
                            lenghtOfPostingList = Integer.parseInt(p[1]);
                        } catch (NumberFormatException e) {
                            System.err.println("One of your parameters does not seem to be a number.");
                            e.printStackTrace();
                            printHelp();
                        }
                    }

                } else {
                    System.err.println("There is something wrong with the parameters.");
                    printHelp();
                }
            } else if (arg.startsWith("-h")) {
                // help
                printHelp();
            }
        }
        // check if we have got an infile & print the configuration.
        if (inFile == null) {
            System.err.println("You did not give an input file.");
            printHelp();
            System.exit(1);
        }
        outFile = new File(FilenameUtils.removeExtension(inFile.getAbsolutePath()) + "_" + featureClass.getSimpleName() + ".dat");
        System.out.printf("MetricSpaces operating on file %s with\n%d number of reference points and %d elements in the posting list.\nIndexing using class %s.\n", inFile.getAbsolutePath(), numberOfReferencePoints, lenghtOfPostingList, featureClass.getName());
        if (outFile.exists()) {
            System.out.printf("NOTE: output file %s will be overwritten.\n", outFile.getAbsolutePath());
        } else {
            System.out.printf("Output will be saved in %s\n", outFile.getAbsolutePath());
        }
        System.out.println("------------------------------------------------------------");
        try {
            try {
                indexReferencePoints(featureClass, numberOfReferencePoints, lenghtOfPostingList, inFile, outFile);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Index reference points for use with a specific feature. A file (infile) containing one image path per line is read and
     * getNumberOfReferencePoints are randomly selected to serve for indexing and search. The resulting data points plus
     * configuration are read written to a file (outfile).
     *
     * @param globalFeatureClass      The feature class to be used, eg. CEDD, PHOG, etc.
     * @param numberOfReferencePoints the number of reference points, eg. 5000
     * @param lenghtOfPostingList     the length of the posting list, ie. how many reference points per image are stored.
     * @param inFile                  the file containing the image data, one image path per line
     * @param outFile                 the output of processing.
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public static void indexReferencePoints(Class globalFeatureClass, int numberOfReferencePoints, int lenghtOfPostingList, File inFile, File outFile) throws IOException, IllegalAccessException, InstantiationException {
        BufferedReader br = new BufferedReader(new FileReader(inFile));
        BufferedWriter bw = new BufferedWriter(new FileWriter(outFile));
        String line;
        LinkedList<String> lines = new LinkedList<>();
        // read all the file paths.
        System.out.println("Reading input file.");
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("#")) { // check for comments.
                if (line.trim().length() > 1) { // check for empty ones.
                    lines.add(line);
                }
            }
        }
        br.close();
        System.out.printf("Read %,d lines from the input file. Now selecting reference points.\n", lines.size());
        // now for the randomness:
        Collections.shuffle(lines);

        // or using Java Math, creating a sampling permutation:
//        RandomDataGenerator randomDataGenerator = new RandomDataGenerator();
//        int[] ints = randomDataGenerator.nextPermutation(lines.size(), getNumberOfReferencePoints);

        // now for the reference points:
        GlobalFeature feature = (GlobalFeature) globalFeatureClass.newInstance();
        // Write the parameters into the file:
        bw.write(feature.getClass().getName() + "\n");
        bw.write(numberOfReferencePoints + "," + lenghtOfPostingList + "\n");
        System.out.print("Indexing ");
        int i = 0;
        for (Iterator<String> iterator = lines.iterator(); iterator.hasNext() && i < numberOfReferencePoints; ) {
            String file = iterator.next();
            try {
                FileInputStream fis = new FileInputStream(file);
                feature.extract(ImageIO.read(fis));
                fis.close(); // and closing it again .. just to leave no open file pointers.
                bw.write(Base64.encodeBase64String(feature.getByteArrayRepresentation()) + "\n");
                i++;
                if (i % 100 == 0) {
                    System.out.print('.');
                }
            } catch (Exception e) {
                System.out.printf("Having problem \"%s\" with file %s\n", e.getMessage(), file);
                // e.printStackTrace();
            }
        }
        System.out.println();
        bw.close();
    }

    /**
     * Init with a single file. In this file the feature class and all parameters are given. To create such a file see
     * {@link MetricSpaces#indexReferencePoints(Class, int, int, File, File)}. Note that you can load multiple files, one for each
     * feature. If you load more than one per feature class, they will be overwritten.
     *
     * @param referencePoints is the outFile from the method {@link MetricSpaces#indexReferencePoints(Class, int, int, File, File)}
     * @throws IOException
     */
    public static Parameters loadReferencePoints(InputStream referencePoints) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        BufferedReader br = new BufferedReader(new InputStreamReader(referencePoints));
        String feature = br.readLine().trim();
        Class<?> featureClass = Class.forName(feature);
        String[] params = br.readLine().trim().split(",");
        Parameters p = new MetricSpaces.Parameters();
        p.numberOfReferencePoints = Integer.parseInt(params[0]);
        p.lengthOfPostingList = Integer.parseInt(params[1]);
        p.featureClass = featureClass;
        parameters.put(feature, p);
        ArrayList<GlobalFeature> ro = new ArrayList<>(p.numberOfReferencePoints);
        String line = null;
        while ((line = br.readLine()) != null) {
            if (!line.startsWith("#")) { // check for comments.
                if (line.length() > 1) { // check for empty ones.
                    GlobalFeature f = (GlobalFeature) featureClass.newInstance();
                    f.setByteArrayRepresentation(Base64.decodeBase64(line));
                    ro.add(f);
                }
            }
        }
        MetricSpaces.referencePoints.put(feature, ro);
        br.close();
        return p;
    }

    public static boolean supportsFeature(GlobalFeature feature) {
        return referencePoints.get(feature.getClass().getName()) != null;
    }

    /**
     * Convenience method for {@link #generateHashString(GlobalFeature, int)}.
     *
     * @param feature feature the feature instance the string is generated for.
     * @return the text for the Lucene index.
     */
    public static String generateHashString(GlobalFeature feature) {
        return generateHashString(feature, parameters.get(feature.getClass().getName()).lengthOfPostingList);
    }

    /**
     * Creates a text String to be used for indexing and search based on the reference points.
     *
     * @param feature     the feature instance the string is generated for.
     * @param queryLength the length of the posting list. If 0, then it's set to the preset.
     * @return the text for the Lucene index.
     */
    public static String generateHashString(GlobalFeature feature, int queryLength) {
        int lengthOfPostingList = Math.min(queryLength, parameters.get(feature.getClass().getName()).lengthOfPostingList);
        if (lengthOfPostingList < 1) {
            lengthOfPostingList = parameters.get(feature.getClass().getName()).lengthOfPostingList;
        }
        TreeSet<Result> results = getResults(feature, queryLength, lengthOfPostingList);
        StringBuilder sb = new StringBuilder(lengthOfPostingList * 8);
        for (Iterator<Result> resultIterator = results.iterator(); resultIterator.hasNext(); ) {
            Result result = resultIterator.next();
//            sb.append(String.format("%d (%2.2f) ", result.index, result.distance)); // debug.
            for (int i = 0; i < results.size(); i++) {
                sb.append(String.format("R%06d ", result.index));
            }
        }
        return sb.toString();
    }

    private static TreeSet<Result> getResults(GlobalFeature feature, int queryLength, int lengthOfPostingList) {
        ArrayList<GlobalFeature> l = referencePoints.get(feature.getClass().getName());
        // break up if the feature is not indexed ...
        if (l == null) return null;
        TreeSet<Result> results = new TreeSet<>();
        double maxDistance = Double.MAX_VALUE;
        double distance;
        for (int i = 0; i < l.size(); i++) {
            GlobalFeature f = l.get(i);
            distance = f.getDistance(feature);
            if (results.size() < lengthOfPostingList) {
                results.add(new Result(distance, i));
                maxDistance = l.get(results.last().index).getDistance(feature);
            } else if (distance < maxDistance) {
                results.add(new Result(distance, i));
                maxDistance = distance;
                if (results.size() > lengthOfPostingList) {
                    results.pollLast();
                }
            }
        }
        return results;
    }

    /**
     * Creates a text String to be used for search based on the reference points using term boosting instead of repetition.
     *
     * @param feature     the feature instance the string is generated for.
     * @param queryLength the length of the posting list. If 0, then it's set to the preset.
     * @return the text for the Lucene index.
     */
    public static String generateBoostedQuery(GlobalFeature feature, int queryLength) {
        int lengthOfPostingList = Math.min(queryLength, parameters.get(feature.getClass().getName()).lengthOfPostingList);
        if (lengthOfPostingList < 1) {
            lengthOfPostingList = parameters.get(feature.getClass().getName()).lengthOfPostingList;
        }
        TreeSet<Result> results = getResults(feature, queryLength, lengthOfPostingList);
        StringBuilder sb = new StringBuilder(results.size() * 12);
        double max = results.size();
        for (Iterator<Result> resultIterator = results.iterator(); resultIterator.hasNext(); ) {
            Result result = resultIterator.next();
            sb.append(String.format("R%06d^%1.2f ", result.index, (double) results.size() / max));
            lengthOfPostingList--;
        }
        return sb.toString();
    }

    /**
     * prints help for main method.
     */
    private static void printHelp() {
        System.out.println("Help for the MetricSpaces class\n" +
                "===============================\n" +
                "\n" +
                "Run the main method to create a compatible file to be used\n" +
                "for indexing and hashing. This text is shown with the\n" +
                "-h option.\n" +
                "\n" +
                "$> MetricSpaces -i <input-file> -p <parameters> [-c <class>]\n" +
                "\n" +
                "<input-file>    ... gives the image data set to sample from,\n" +
                "                    one per line, lines starting with # are\n" +
                "                    ignored.\n" +
                "<parameters>    ... number of reference points and length of\n" +
                "\t\t\t\t    the posting list, eg. \"-p 1000,50\"\n" +
                "<class>         ... which class to use for indexing, default\n" +
                "                    is CEDD.\n" +
                "\n" +
                "\n" +
                "Example usage:\n" +
                "--------------\n" +
                "\n" +
                "$> MetricSpaces -i mylist.txt -p 500,25");
    }


    /**
     * For storing the parameters per feature.
     */
    public static class Parameters {
        public int numberOfReferencePoints;
        public int lengthOfPostingList;
        public Class featureClass;
    }

    public static class Result implements Comparable<Result> {
        public int index;
        public double distance;

        public Result(double distance, int count) {
            this.distance = distance;
            this.index = count;
        }

        @Override
        public int compareTo(Result o) {
            return (int) Math.signum(distance - o.distance);
        }
    }
}



/*
Help for the MetricSpaces class
===============================

Run the main method to create a compatible file to be used
for indexing and hashing. This text is shown with the
-h option.

$> MetricSpaces -i <input-file> -p <parameters> [-c <class>]

<input-file>    ... gives the image data set to sample from,
                    one per line, lines starting with # are
                    ignored.
<parameters>    ... number of reference points and length of
				    the posting list, eg. "-p 1000,50"
<class>         ... which class to use for indexing, default
                    is CEDD.


Example usage:
--------------

$> MetricSpaces -i mylist.txt -p 500,25
 */
