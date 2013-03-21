package net.semanticmetadata.lire.indexing.tools;

import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

/**
 * This class extends Indexor and does hashing (bit sampling) on a given feature.
 * The hashes are stored in a Lucene field named "Hashes".
 * <p/>
 * Created: 21.03.13 10:03
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class HashingIndexor extends Indexor {
    protected Class featureClass = CEDD.class;

    public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException {
        HashingIndexor indexor = new HashingIndexor();
        BitSampling.generateHashFunctions();
        BitSampling.readHashFunctions();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-i") || arg.startsWith("--input-file")) {
                // infile ...
                if ((i + 1) < args.length)
                    indexor.addInputFile(new File(args[i + 1]));
                else printHelp();
            } else if (arg.startsWith("-l") || arg.startsWith("--index")) {
                // index
                if ((i + 1) < args.length)
                    indexor.setIndexPath(args[i + 1]);
                else printHelp();
            } else if (arg.startsWith("-f") || arg.startsWith("--feature")) {
                // index
                if ((i + 1) < args.length)
                    try {
                        indexor.setFeatureClass(Class.forName(args[i + 1]));
                    } catch (ClassNotFoundException e) {
                        System.err.println("Could not find feature class named " + args[i + 1]);
                        printHelp();
                    }
                else printHelp();
            } else if (arg.startsWith("-h")) {
                // help
                printHelp();
            } else if (arg.startsWith("-c")) {
                // list of input files within a file.
                if ((i + 1) < args.length) {
                    BufferedReader br = new BufferedReader(new FileReader(new File(args[i + 1])));
                    String file;
                    while ((file = br.readLine()) != null) {
                        if (file.trim().length() > 2) {
                            File f = new File(file);
                            if (f.exists()) indexor.addInputFile(f);
                            else System.err.println("Did not find file " + f.getCanonicalPath());
                        }
                    }
                } else printHelp();
            }
        }
        // check if there is an infile, an outfile and some features to extract.
        if (!indexor.isConfigured()) {
            printHelp();
        } else {
            indexor.run();
        }
    }

    public void setFeatureClass(Class featureClass) {
        this.featureClass = featureClass;
    }

    protected void addToDocument(LireFeature feature, Document document, String featureFieldName) {
        if (feature.getClass().getCanonicalName().equals(featureClass.getCanonicalName())) {
            document.add(new StoredField(featureFieldName, feature.getByteArrayRepresentation()));
            int[] hashes = BitSampling.generateHashes(feature.getDoubleHistogram());
            // System.out.println(Arrays.toString(hashes));
            document.add(new TextField("Hashes", SerializationUtils.arrayToString(hashes), Field.Store.YES));
        }
    }


}
