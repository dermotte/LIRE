package net.semanticmetadata.lire.indexers.hashing;

import net.semanticmetadata.lire.imageanalysis.features.Extractor;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import javax.imageio.ImageIO;
import java.io.*;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * This hashing approach implements the proximity approach "metric spaces" based on the work of Giuseppe Amato.
 * See Gennaro, Claudio, et al. "An approach to content-based image retrieval based on the Lucene search engine
 * library." Research and Advanced Technology for Digital Libraries. Springer Berlin Heidelberg, 2010. 55-66.
 * <p/>
 * While it's actually not a hashing routine it's an easy way to use it within LIRE. Based on a pre-trained set
 * of representatives, the features of the representatives are stored in a text file. For indexing and search they
 * can be used to create hash like text snippetsd, that are then used to search & index.
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Created by mlux on 24.08.2015.
 */
public class MetricSpaces {
    static File inFile = null, outFile = null;
    static int numberOfReferencePoints = 500;
    static int lenghtOfPostingList = 10;

    public static void main(String[] args) {
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
        outFile = new File(FilenameUtils.removeExtension(inFile.getAbsolutePath()) + ".dat");
        System.out.printf("MetricSpaces operating on file %s with\n%d number of reference points and %d elements in the posting list.\n", inFile.getAbsolutePath(), numberOfReferencePoints, lenghtOfPostingList);
        if (outFile.exists()) {
            System.out.printf("NOTE: output file %s will be overwritten.\n", outFile.getAbsolutePath());
        } else {
            System.out.printf("Output will be saved in %s\n", outFile.getAbsolutePath());
        }
        try {
            index();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Actual indexing of the
     */
    private static void index() throws IOException {
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
        System.out.printf("Read %d lines from the input file. Now selecting reference points.\n", lines.size());
        // now for the randomness:
        Collections.shuffle(lines);
        // now for the reference points:
        GlobalFeature feature = new CEDD();
        // Write the parameters into the file:
        bw.write(numberOfReferencePoints+","+lenghtOfPostingList+"\n");
        int i = 0;
        for (Iterator<String> iterator = lines.iterator(); iterator.hasNext() && i < numberOfReferencePoints; i++) {
            String file = iterator.next();
            try {
                feature.extract(ImageIO.read(new File(file)));
                bw.write(Base64.encodeBase64String(feature.getByteArrayRepresentation()) + "\n");
            } catch (Exception e) {
                System.out.printf("Having problem \"%s\" with file %s\n", e.getMessage(), file);
                // e.printStackTrace();
            }
        }
        bw.close();
    }

    /**
     * prints help for main method.
     */
    private static void printHelp() {
        System.out.println("Help for the MetricSpaces class\n" +
                "===============================\n" +
                "\n" +
                "Run the main method to create a compatible file to be used \n" +
                "for indexing and hashing. This text is shown with the \n" +
                "-h option.\n" +
                "\n" +
                "$> MetricSpaces -i <input-file> -p <parameters>\n" +
                "\n" +
                "<input-file> ... gives the image data set to sample from, \n" +
                "                 one per line, lines starting with # are \n" +
                "\t\t\t\t ignored.\n" +
                "<parameters> ... number of reference points and length of \n" +
                "\t\t\t\t the posting list, eg. \"-p 1000,50\"\n" +
                "\t\t\t\t \n" +
                "\n" +
                "Example usage:\n" +
                "--------------\n" +
                "\n" +
                "$> MetricSpaces -i mylist.txt -p 500,25");
    }
}

/*
Help for the MetricSpaces class
===============================

Run the main method to create a compatible file to be used
for indexing and hashing. This text is shown with the
-h option.

$> MetricSpaces -i <input-file> -p <parameters>

<input-file> ... gives the image data set to sample from,
                 one per line, lines starting with # are
				 ignored.
<parameters> ... number of reference points and length of
				 the posting list, eg. "-p 1000,50"


Example usage:
--------------

$> MetricSpaces -i mylist.txt -p 500,25
 */
