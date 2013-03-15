package net.semanticmetadata.lire.indexing;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;

import java.io.*;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.GZIPInputStream;

/**
 * The Indexor (yes, I know the name sounds weird, but it should match the Extractor class, and not
 * the Lucene Indexing classes) reads data files created by the {@link Extractor}. They are added to
 * a given index. Note that the index is not overwritten, but the documents are appended.
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 08.03.13
 *         Time: 14:28
 */
public class Indexor {
    private LinkedList<File> inputFiles = new LinkedList<File>();
    private String indexPath = null;

    public static void main(String[] args) throws IOException {
        Indexor indexor = new Indexor();
//        HashingUtils.generateHashFunctions();
//        HashingUtils.readHashFunctions();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-i") || arg.startsWith("--input-file") ) {
                // infile ...
                if ((i+1) < args.length)
                    indexor.addInputFile(new File(args[i + 1]));
                else printHelp();
            } else if (arg.startsWith("-l")|| arg.startsWith("--index") ) {
                // index
                if ((i+1) < args.length)
                    indexor.setIndexPath(args[i+1]);
                else printHelp();
            } else if (arg.startsWith("-h")) {
                // help
                printHelp();
            } else if (arg.startsWith("-c")) {
                // list of input files within a file.
                if ((i+1) < args.length) {
                    BufferedReader br = new BufferedReader(new FileReader(new File(args[i + 1])));
                    String file;
                    while ((file = br.readLine()) != null) {
                        if (file.trim().length()>2) {
                            File f = new File(file);
                            if (f.exists()) indexor.addInputFile(f);
                            else System.err.println("Did not find file " + f.getCanonicalPath());
                        }
                    }
                }
                else printHelp();
            }
        }
        // check if there is an infile, an outfile and some features to extract.
        if (!indexor.isConfigured()) {
            printHelp();
        } else {
            indexor.run();
        }
    }

    private boolean isConfigured() {
        boolean isConfigured = true;
        // check if there are input files and if they exist.
        if (inputFiles.size() > 0) {
            for (Iterator<File> iterator = inputFiles.iterator(); iterator.hasNext(); ) {
                File next = iterator.next();
                if (!next.exists()) isConfigured = false;
            }
        }
        return isConfigured;
    }

    private static void printHelp() {
        System.out.println("Help for the Indexor class.\n" +
                "===========================\n" +
                "This help text is shown if you start the Indexor with the '-h' option.\n" +
                "\n" +
                "Usage\n" +
                "=====\n" +
                "$> Indexor -i <input-file> -l <index-directory>\n" +
                "\n" +
                "or \n" +
                "\n" +
                "$> Indexor -c <file-list> -l <index-directory>\n" +
                "\n" +
                "with \n" +
                "\n" +
                "<input-file> ... \t\ta single output file of Extractor.\n" +
                "<index-directory> ...\tthe index to write the data to (it's appended).\n" +
                "<file-list> ...\t\t\ta file containing data files one per line.\n");
    }


    public void run() {
        // do it ...
        try {
            IndexWriter indexWriter = LuceneUtils.createIndexWriter(indexPath, false, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
            for (Iterator<File> iterator = inputFiles.iterator(); iterator.hasNext(); ) {
                File inputFile = iterator.next();
                System.out.println("Processing " + inputFile.getPath() + ".");
                readFile(indexWriter, inputFile);
            }
            indexWriter.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads data from a file and writes it to an index.
     * @param indexWriter the index to write to.
     * @param inputFile the input data for the process.
     * @throws IOException
     * @throws InstantiationException
     * @throws IllegalAccessException
     * @throws ClassNotFoundException
     */
    private void readFile(IndexWriter indexWriter, File inputFile) throws IOException, InstantiationException, IllegalAccessException, ClassNotFoundException {
        BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(new FileInputStream(inputFile)));
        byte[] tempInt = new byte[4];
        int tmp, tmpFeature;
        byte[] temp = new byte[2064];
        // read file name length:
        while (in.read(tempInt, 0, 4) > 0) {
            Document d = new Document();
            tmp = SerializationUtils.toInt(tempInt);
            // read file name:
            in.read(temp, 0, tmp);
            String filename = new String(temp, 0, tmp);
            // normalize Filename to full path.
            filename = inputFile.getCanonicalPath().substring(0, inputFile.getCanonicalPath().lastIndexOf(inputFile.getName())) + filename;
            d.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, filename, Field.Store.YES));
//                System.out.print(filename);
            while ((tmpFeature = in.read()) < 255) {
//                    System.out.print(", " + tmpFeature);
                LireFeature f = (LireFeature) Class.forName(Extractor.features[tmpFeature]).newInstance();
                // byte[] length ...
                in.read(tempInt, 0, 4);
                tmp = SerializationUtils.toInt(tempInt);
                // read feature byte[]
                in.read(temp, 0, tmp);
                f.setByteArrayRepresentation(temp, 0, tmp);
                addToDocument(f, d, Extractor.featureFieldNames[tmpFeature]);
//                d.add(new StoredField(Extractor.featureFieldNames[tmpFeature], f.getByteArrayRepresentation()));
            }
            indexWriter.addDocument(d);
        }
        in.close();
    }

    /**
     * Overwrite this method if you want to filter the input, apply hashing, etc.
     * @param feature the current feature.
     * @param document the current document.
     * @param featureFieldName the field name of the feature.
     */
    private void addToDocument(LireFeature feature, Document document, String featureFieldName) {
//        if (feature instanceof CEDD) {
            document.add(new StoredField(featureFieldName, feature.getByteArrayRepresentation()));
//            int[] hashes = HashingUtils.generateHashes(feature.getDoubleHistogram());
//            System.out.println(Arrays.toString(hashes));
//            document.add(new TextField("Hashes", SerializationUtils.arrayToString(hashes), Field.Store.YES));
//        }
    }

    public void addInputFile(File inputFile) {
        this.inputFiles.add(inputFile);
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }
}
