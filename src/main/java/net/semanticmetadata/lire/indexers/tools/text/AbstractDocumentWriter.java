package net.semanticmetadata.lire.indexers.tools.text;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
import net.semanticmetadata.lire.indexers.hashing.MetricSpaces;
import net.semanticmetadata.lire.utils.SerializationUtils;
import net.semanticmetadata.lire.utils.StatsUtils;
import net.semanticmetadata.lire.utils.StopWatch;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.lucene.document.*;
import org.apache.lucene.index.IndexableField;
import org.apache.lucene.util.BytesRef;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by mlux on 12/16/16.
 */
public abstract class AbstractDocumentWriter implements Runnable{
    protected boolean doHashingBitSampling = false;
    protected boolean doMetricSpaceIndexing = false;
    protected boolean useDocValues = false;
    protected File infile;


    public AbstractDocumentWriter(File infile, boolean useDocValues, boolean doHashingBitSampling, boolean doMetricSpaceIndexing) {
        this.infile = infile;
        this.useDocValues = useDocValues;
        this.doHashingBitSampling = doHashingBitSampling;
        this.doMetricSpaceIndexing = doMetricSpaceIndexing;
    }

    /**
     * Called after the last line is read.
     */
    protected abstract void finish();

    /**
     * Called before the first document is read.
     */
    protected abstract void start();

    /**
     * Called for each line in the file.
     * @param fileName the content of the first column
     * @param listOfFeatures the 2nd to nth column values already parsed.
     */
    protected abstract void write(String fileName, ArrayList<GlobalFeature> listOfFeatures);

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
            start();
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
}
