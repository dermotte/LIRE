package net.semanticmetadata.lire.indexers.tools.text;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.indexers.parallel.WorkItem;
import net.semanticmetadata.lire.utils.CommandLineUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * Reading a file from {@link ParallelExtraction} and writing it to a lucene index.
 */
public class LuceneIndexWriter extends AbstractDocumentWriter {
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
    protected LinkedBlockingQueue<QueueItem> queue = new LinkedBlockingQueue<>(500);
    List<Thread> threads;
    private int numThreads = 8;

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
                Thread t = new Thread(liw);
                t.start();
                t.join();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Called after the last line is read.
     */
    protected void finishWriting() {
        try {
            for (int i = 0; i < 20; i++) {
                queue.put(new QueueItem(null, null));
            }
            for (Iterator<Thread> iterator = threads.iterator(); iterator.hasNext(); ) {
                iterator.next().join();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        try {
            iw.commit();
            iw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void startWriting() {
        threads = new LinkedList<>();
        for (int i = 0; i < numThreads; i++) {
            Thread t = new Thread(new Consumer());
            t.start();
            threads.add(t);
        }
    }

    /**
     * Called for each line in the file.
     *
     * @param fileName       the content of the first column
     * @param listOfFeatures the 2nd to nth column values already parsed.
     */
    protected void write(String fileName, ArrayList<GlobalFeature> listOfFeatures) {
        // clone the features first:
        ArrayList<GlobalFeature> tmp = new ArrayList<>(listOfFeatures.size());
        try {
            for (Iterator<GlobalFeature> iterator = listOfFeatures.iterator(); iterator.hasNext(); ) {
                GlobalFeature f = iterator.next();
                GlobalFeature n = (GlobalFeature) f.getClass().newInstance();
                n.setByteArrayRepresentation(f.getByteArrayRepresentation());
                tmp.add(n);
            }
            queue.put(new QueueItem(fileName, tmp));
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    class QueueItem {
        String id;
        List<GlobalFeature> features;

        public QueueItem(String id, List<GlobalFeature> features) {
            this.id = id;
            this.features = features;
        }
    }

    class Consumer implements Runnable {
        HashMap<String, Object> document = new HashMap<>();

        @Override
        public void run() {
            try {
                QueueItem data = queue.take();
                while (data.id != null) {
                    document.clear();
                    document.put(DocumentBuilder.FIELD_NAME_IDENTIFIER, data.id);
                    document.put("title", data.id);
                    for (Iterator<GlobalFeature> iterator = data.features.iterator(); iterator.hasNext(); ) {
                        GlobalFeature f = iterator.next();
                        document.put(f.getFieldName(), f.getByteArrayRepresentation());
                        if (doHashingBitSampling) {
                            document.put(f.getFieldName() + DocumentBuilder.HASH_FIELD_SUFFIX,
                                    SerializationUtils.arrayToString((BitSampling.generateHashes(f.getFeatureVector()))));

                        } else if (doMetricSpaceIndexing) {
                            if (MetricSpaces.supportsFeature(f)) {
                                document.put(f.getFieldName() + DocumentBuilder.HASH_FIELD_SUFFIX,
                                        MetricSpaces.generateHashString(f));
                            }

                        }
                    }
                    output(document);
                    data = queue.take();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private void output(HashMap<String, Object> document) {
            StringBuilder sb = new StringBuilder();
            sb.append("<doc>");
            for (Iterator<String> iterator = document.keySet().iterator(); iterator.hasNext(); ) {
                String fieldName = iterator.next();
                sb.append("<field name=\"" + fieldName + "\">");
                sb.append(document.get(fieldName));
                sb.append("</field>");
            }
            sb.append("</doc>\n");
            LinkedList<IndexableField> fields = new LinkedList<>();
            // add all the features.
            for (Iterator<String> iterator = document.keySet().iterator(); iterator.hasNext(); ) {
                String fieldName = iterator.next();
                if (fieldName.startsWith("id") || fieldName.startsWith("title") || fieldName.startsWith(DocumentBuilder.FIELD_NAME_IDENTIFIER) ) {
                    fields.add(new StringField(fieldName, (String) document.get(fieldName), Field.Store.YES));
                } else if (fieldName.endsWith(DocumentBuilder.HASH_FIELD_SUFFIX)) {
                    fields.add(new TextField(fieldName, (String) document.get(fieldName), Field.Store.NO));
                } else {
                    if (!useDocValues) {
                        fields.add(new StoredField(fieldName, (byte[]) document.get(fieldName)));
                    } else {
                        // Alternative: The DocValues field. It's extremely fast to read, but it's all in RAM most likely.
                        fields.add(new BinaryDocValuesField(fieldName, new BytesRef((byte[]) document.get(fieldName))));
                    }
                }
            }
            try {
                synchronized (iw) {
                    iw.addDocument(fields);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
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
