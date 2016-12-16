package net.semanticmetadata.lire.indexers.tools.text;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.utils.CommandLineUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Properties;

/**
 * Reading a file from {@link ParallelExtraction} and writing it to a lucene index.
 */
public class LuceneIndexWriter extends AbstractDocumentWriter implements Runnable {
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
    private IndexWriter iw;

    public LuceneIndexWriter(File infile, File indexDirectory, boolean doHashingBitSampling, boolean doMetricSpaceIndexing, boolean useDocValues) throws IOException {
        super(infile, useDocValues, doHashingBitSampling, doMetricSpaceIndexing);
        this.iw = LuceneUtils.createIndexWriter(indexDirectory.getPath(), true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
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

    @Override
    protected void start() {
        // do nothing in this case.
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
