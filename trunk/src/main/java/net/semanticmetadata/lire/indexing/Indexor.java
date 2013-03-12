package net.semanticmetadata.lire.indexing;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.Enumeration;
import java.util.Properties;
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
    private File inputFile = null;
    private String indexPath = null;

    public static void main(String[] args) {
        Indexor indexor = new Indexor();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-i") || arg.startsWith("--input-file") ) {
                // infile ...
                if ((i+1) < args.length)
                    indexor.setInputFile(new File(args[i+1]));
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
                // config file ...
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
        if (inputFile == null || !inputFile.exists())
            isConfigured = false;
        return isConfigured;
    }

    private static void printHelp() {
        System.out.println("Help for the Indexor class.\n" +
                "=============================\n" +
                "This help text is shown if you start the Indexor with the '-h' option.\n" +
                "\n" +
                "1. Usage\n" +
                "========\n" +
                "$> Indexor -i <input-file> -l <index-directory>");
    }


    public void run() {
        // do it ...
        byte[] tempInt = new byte[4];
        int tmp, tmpFeature;
        byte[] temp = new byte[2064];
        try {
            BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(new FileInputStream("out.data")));
            // read file name length:
            IndexWriter indexWriter = LuceneUtils.createIndexWriter(indexPath, false);
            while (in.read(tempInt, 0, 4) > 0) {
                Document d = new Document();
                tmp = SerializationUtils.toInt(tempInt);
                // read file name:
                in.read(temp, 0, tmp);
                String filename = new String(temp, 0, tmp);
                // normalize Filename to full path.
                filename = inputFile.getCanonicalPath().substring(0, inputFile.getCanonicalPath().lastIndexOf(inputFile.getName())) + filename;
                d.add(new StringField(DocumentBuilder.FIELD_NAME_IDENTIFIER, filename, Field.Store.YES));
                System.out.print(filename);
                while ((tmpFeature = in.read()) < 255) {
                    System.out.print(", " + tmpFeature);
                    LireFeature f = (LireFeature) Class.forName(Extractor.features[tmpFeature]).newInstance();
                    // byte[] length ...
                    in.read(tempInt, 0, 4);
                    tmp = SerializationUtils.toInt(tempInt);
                    // read feature byte[]
                    in.read(temp, 0, tmp);
                    f.setByteArrayRepresentation(temp, 0, tmp);
                    d.add(new StoredField(Extractor.featureFieldNames[tmpFeature], f.getByteArrayRepresentation()));
                }
                indexWriter.addDocument(d);
                System.out.println();
            }
            indexWriter.commit();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public void setIndexPath(String indexPath) {
        this.indexPath = indexPath;
    }
}
