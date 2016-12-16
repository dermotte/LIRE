package net.semanticmetadata.lire.indexers.tools.text;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.utils.*;
import org.apache.commons.io.*;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import java.io.*;
import java.util.*;

/**
 * Reading a file from {@link ParallelExtraction} and writing it to a lucene index.
 */
public class LuceneIndexWriter implements Runnable {
    // -------------< static >------------------------
    private static String helpMessage = "Usage of LuceneIndexWriter\n" +
            "==========================\n" +
            "\n" +
            "$> java LuceneIndexWriter -i <file> -o <index-directory> [-hb] [-hm]\n" +
            "\n" +
            "-i  ... path to the input file\n" +
            "-o  ... path to the Lucene index for output\n" +
            "-d  ... use DocValues\n" +
            "-hb ... employ BitSampling Hashing (overrules MetricSpaces, loads all *.mds files from current directory)\n" +
            "-hm ... employ MetricSpaces Indexing";

    // -------------< instance >------------------------
    private File infile;
    private IndexWriter iw;
    private boolean doHashingBitSampling = false, doMetricSpaceIndexing = false;
    private boolean useDocValues = false;

    public LuceneIndexWriter(File infile, File indexDirectory, boolean doHashingBitSampling, boolean doMetricSpaceIndexing, boolean useDocValues) throws IOException {
        this.infile = infile;
        this.iw = LuceneUtils.createIndexWriter(indexDirectory.getPath(), true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
        this.doHashingBitSampling = doHashingBitSampling;
        this.doMetricSpaceIndexing = doMetricSpaceIndexing;
        this.useDocValues = useDocValues;
    }

    public static void main(String[] args) {
        Properties cmdLine = CommandLineUtils.getProperties(args, helpMessage, new String[]{"-i", "-o"});
        File inputFile = new File(cmdLine.getProperty("-i"));
        File outputFile = new File(cmdLine.getProperty("-o"));
        if (!inputFile.exists()) {
            System.err.println("Input file does not exist.");
            System.out.println(helpMessage);
            System.exit(1);
        } else {
            try {
                LuceneIndexWriter liw = new LuceneIndexWriter(inputFile, outputFile, cmdLine.containsKey("-hb"), cmdLine.containsKey("-hm"), cmdLine.containsKey("-d"));
                liw.run();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void run() {
        StopWatch sw = new StopWatch();
        sw.start();
        double count = 0;
        try {
            LineIterator lineIt = IOUtils.lineIterator(new FileReader(infile));
            // read the first line to determine the fields.
            String[] fields = lineIt.next().split(";");
            ArrayList<GlobalFeature> listOfFeatures = new ArrayList<>(fields.length - 1);
            for (int i = 1; i < fields.length; i++) {
                String f = fields[i];
                if (f.trim().length() > 2)
                    listOfFeatures.add(((GlobalFeature) Class.forName(f).newInstance()));
            }
            String tmpOut = Arrays.toString(fields).replaceAll(", ", "\n");
            System.out.println("Indexing fields " + tmpOut.substring(1, tmpOut.length()-1));
            // init hashing ...
            if (doHashingBitSampling) {
                BitSampling.readHashFunctions();
            }
            else if (doMetricSpaceIndexing) {
                // init metric spaces indexing by reading all files with ending .msd from the current directory.
                Iterator<File> fileIterator = FileUtils.iterateFiles(new File("."), new String[]{"msd"}, false);
                while (fileIterator.hasNext()) {
                    File f = fileIterator.next();
                    System.out.println("Loading reference points from file " + f.getPath());
                    MetricSpaces.loadReferencePoints(new FileInputStream(f));
                }
            }
            // reading the rest of the file ...
            System.out.print("Working now ...\n");
            while (lineIt.hasNext()) {
                String[] d = lineIt.next().split(";");
                String fileName = d[0];
                for (int i = 1; i < d.length; i++) {
                    String s = d[i];
                    if (s.trim().length() > 2) // only if it's not empty.
                        listOfFeatures.get(i-1).setByteArrayRepresentation(org.apache.commons.codec.binary.Base64.decodeBase64(s));
                }
                // write to index ...
                write(fileName, listOfFeatures);
                count++;
                if (count%1000==0) {
                    System.out.printf("Processed %d images took %s minutes, ~%.2f ms per image.\n", (int) count, StatsUtils.convertTime(sw.getTimeSinceStart()), (double) sw.getTimeSinceStart()/count);
                }
            }
            finish();
            sw.stop();
            System.out.printf("\nIt's finished. Processing %d images took %s minutes, ~%.2f ms per image.\n", (int) count, StatsUtils.convertTime(sw.getTime()), (double) sw.getTime()/count);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException | ClassNotFoundException e) {
            System.err.println("There seems to be a problem with the input file, global feature not found.");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called after the last line is read.
     */
    protected void finish() {
        try {
            iw.commit();
            iw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Called for each line in the file.
     * @param fileName the content of the first column
     * @param listOfFeatures the 2nd to nth column values already parsed.
     */
    protected void write(String fileName, ArrayList<GlobalFeature> listOfFeatures) {
        LinkedList<IndexableField> fields = new LinkedList<>();
        // add all the features.
        for (Iterator<GlobalFeature> iterator = listOfFeatures.iterator(); iterator.hasNext(); ) {
            GlobalFeature globalFeature = iterator.next();
            // feature vector:
            if (!useDocValues) {
                fields.add(new StoredField(globalFeature.getFieldName(), new BytesRef(globalFeature.getByteArrayRepresentation())));
            } else {
                // Alternative: The DocValues field. It's extremely fast to read, but it's all in RAM most likely.
                fields.add(new BinaryDocValuesField(globalFeature.getFieldName(), new BytesRef(globalFeature.getByteArrayRepresentation())));
            }
            // hashing:
            if (doHashingBitSampling) {
                fields.add(new TextField(globalFeature.getFieldName() + DocumentBuilder.HASH_FIELD_SUFFIX,
                        SerializationUtils.arrayToString(BitSampling.generateHashes(globalFeature.getFeatureVector())), Field.Store.YES));
            } else if (doMetricSpaceIndexing) {
                if (MetricSpaces.supportsFeature(globalFeature)) {
                    fields.add(new TextField(globalFeature.getFieldName() + DocumentBuilder.HASH_FIELD_SUFFIX,
                            MetricSpaces.generateHashString(globalFeature), Field.Store.YES));
                }
            }
        }
        fields.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, fileName, Field.Store.YES));
        try {
            iw.addDocument(fields);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
/*
Usage of LuceneIndexWriter
==========================

$> java LuceneIndexWriter -i <file> -o <index-directory> [-hb] [-hm]

-i  ... path to the input file
-o  ... path to the Lucene index for output
-d  ... use DocValues
-hb ... employ BitSampling Hashing (overrules MetricSpaces, loads all *.mds files from current directory)
-hm ... employ MetricSpaces Indexing
 */
