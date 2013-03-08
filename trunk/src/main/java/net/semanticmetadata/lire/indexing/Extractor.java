package net.semanticmetadata.lire.indexing;

import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.SerializationUtils;
import sun.security.provider.SystemSigner;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.zip.GZIPOutputStream;

/**
 * The Extractor is a configurable class that extracts multiple features from multiple images
 * and puts them into a data file. Main purpose is run multiple extractors at multiple machines
 * and put the data files into one single index. Images are references relatively to the data file,
 * so it should work fine for network file systems.
 *
 * File format is specified as: (12(345)+)+ with 1-5 being ...
 *
 * 1. Length of the file name [4 bytes], an int n giving the number of bytes for the file name
 * 2. File name, relative to the outfile [n bytes, see above]
 * 3. Feature index [1 byte], see static members
 * 4. Feature value length [4 bytes], an int k giving the number of bytes encoding the value
 * 5. Feature value [k bytes, see above]
 *
 * The file is sent through an GZIPOutputStream, so it's compressed in addition.
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 08.03.13
 *         Time: 13:15
 */
public class Extractor implements Runnable {
    public static final String[] features = new String[]{
            "net.semanticmetadata.lire.imageanalysis.CEDD",                 // 0
            "net.semanticmetadata.lire.imageanalysis.FCTH",                 // 1
            "net.semanticmetadata.lire.imageanalysis.OpponentHistogram",    // 2
            "net.semanticmetadata.lire.imageanalysis.JointHistogram"        // 3
    };

    static HashMap<String, Integer> feature2index;

    static{
        feature2index = new HashMap<String, Integer>(features.length);
        for (int i = 0; i < features.length; i++) {
            feature2index.put(features[i], i);
        }
    }
    LinkedList<LireFeature> listOfFeatures;
    File fileList = null;
    File outFile = null;

    public Extractor() {
        // default constructor.
        listOfFeatures = new LinkedList<LireFeature>();
    }

    /**
     * Adds a feature to the extractor chain. All those features are extracted from images.
     *
     * @param feature
     */
    public void addFeature(LireFeature feature) {
        listOfFeatures.add(feature);
    }

    /**
     * Sets the file list for processing. One image file per line is fine.
     *
     * @param fileList
     */
    public void setFileList(File fileList) {
        this.fileList = fileList;
    }

    /**
     * Sets the outfile. The outfile has to be in a folder parent to all input images.
     *
     * @param outFile
     */
    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    public static void main(String[] args) {
        Extractor e = new Extractor();
        e.setFileList(new File(args[0]));
        e.setOutFile(new File(args[1]));
        try {
            e.addFeature((LireFeature) Class.forName(features[0]).newInstance());
            e.addFeature((LireFeature) Class.forName(features[1]).newInstance());
            e.addFeature((LireFeature) Class.forName(features[2]).newInstance());
            e.addFeature((LireFeature) Class.forName(features[3]).newInstance());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        e.run();
    }


    @Override
    public void run() {
        // check:
        if (fileList == null || !fileList.exists()) {
            System.err.println("No text file with a list of images given.");
            return;
        }
        if (listOfFeatures.size() == 0) {
            System.err.println("No features to extract given.");
            return;
        }

        // do it ...
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileList));
            BufferedOutputStream dos = new BufferedOutputStream(new GZIPOutputStream(new FileOutputStream(outFile)));
            String file = null;
            String outFilePath = outFile.getCanonicalPath();
            outFilePath = outFilePath.substring(0, outFilePath.lastIndexOf(outFile.getName()));
            while ((file = br.readLine()) != null) {
                File input = new File(file);
                String relFile = input.getCanonicalPath().substring(outFilePath.length());
                byte[] tmpBytes = relFile.getBytes();
                dos.write(SerializationUtils.toBytes(tmpBytes.length));
                dos.write(tmpBytes);
                BufferedImage img = ImageIO.read(input);
                for (LireFeature feature : listOfFeatures) {
                    feature.extract(img);
                    dos.write(feature2index.get(feature.getClass().getName()));
                    tmpBytes = feature.getByteArrayRepresentation();
                    dos.write(SerializationUtils.toBytes(tmpBytes.length));
                    dos.write(tmpBytes);
                }
                dos.write(-1);
            }
            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
