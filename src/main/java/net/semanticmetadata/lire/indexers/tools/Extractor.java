/*
 * This file is part of the LIRE project: http://lire-project.net
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 01.07.13 16:15
 */

package net.semanticmetadata.lire.indexers.tools;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.utils.SerializationUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

/**
 * The Extractor is a configurable class that extracts multiple features from multiple images
 * and puts them into a data file. Main purpose is run multiple extractors at multiple machines
 * and put the data files into one single index. Images are references relatively to the data file,
 * so it should work fine for network file systems.
 * <p/>
 * File format is specified as: (12(345)+)+6 with 1-6 being ...
 * <p/>
 * 1. Length of the file name [4 bytes], an int n giving the number of bytes for the file hashFunctionsFileName
 * 2. File name, relative to the output file this data is written to [n bytes, see above]
 * 3. Feature index [1 byte], see static members
 * 4. Feature value length [4 bytes], an int k giving the number of bytes encoding the value
 * 5. Feature value [k bytes, see above]
 * 6. One single byte with the value -1
 * <p/>
 * The file is sent through an GZIPOutputStream, so it's compressed in addition.
 *
 * Note that the outfile has to be in a folder parent to all images!
 *
 * @author Mathias Lux, mathias@juggle.at, 08.03.13
 */
public class Extractor implements Runnable {
    public static final String[] features = new String[]{
            "CEDD",                  // 0
            "FCTH",                  // 1
            "OpponentHistogram",     // 2
            "JointHistogram",        // 3
            "AutoColorCorrelogram",  // 4
            "ColorLayout",           // 5
            "EdgeHistogram",         // 6
            "Gabor",                 // 7
            "JCD",                   // 8
            "JpegCoefficientHistogram",
            "ScalableColor",         // 10
            "SimpleColorHistogram",  // 11
            "Tamura",                // 12
            "LuminanceLayout",       // 13
            "PHOG",                  // 14
    };

    public static final String[] featureFieldNames = new String[]{
            DocumentBuilder.FIELD_NAME_CEDD,                 // 0
            DocumentBuilder.FIELD_NAME_FCTH,                 // 1
            DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM,   // 2
            DocumentBuilder.FIELD_NAME_JOINT_HISTOGRAM,      // 3
            DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM, // 4
            DocumentBuilder.FIELD_NAME_COLORLAYOUT,          // 5
            DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM,        // 6
            DocumentBuilder.FIELD_NAME_GABOR,                // 7
            DocumentBuilder.FIELD_NAME_JCD,                  // 8
            DocumentBuilder.FIELD_NAME_JPEGCOEFFS,
            DocumentBuilder.FIELD_NAME_SCALABLECOLOR,
            DocumentBuilder.FIELD_NAME_COLORHISTOGRAM,
            DocumentBuilder.FIELD_NAME_TAMURA,               // 12
            DocumentBuilder.FIELD_NAME_LUMINANCE_LAYOUT,     // 13
            DocumentBuilder.FIELD_NAME_PHOG,                  // 14
    };

    static HashMap<String, Integer> feature2index;

    static {
        feature2index = new HashMap<String, Integer>(features.length);
        for (int i = 0; i < features.length; i++) {
            feature2index.put(features[i], i);
        }
    }

    LinkedList<GlobalFeature> listOfFeatures;
    File fileList = null;
    File outFile = null;

    public Extractor() {
        // default constructor.
        listOfFeatures = new LinkedList<GlobalFeature>();
    }

    /**
     * Adds a feature to the extractor chain. All those features are extracted from images.
     *
     * @param feature
     */
    public void addFeature(GlobalFeature feature) {
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

    public static void main(String[] args) throws IOException {
        Extractor e = new Extractor();

        // parse programs args ...
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if (arg.startsWith("-i")) {
                // infile ...
                if ((i+1) < args.length)
                    e.setFileList(new File(args[i + 1]));
                else printHelp();
            } else if (arg.startsWith("-o")) {
                // out file
                if ((i+1) < args.length)
                    e.setOutFile(new File(args[i + 1]));
                else printHelp();
            } else if (arg.startsWith("-h")) {
                // help
                printHelp();
            } else if (arg.startsWith("-c")) {
                // config file ...
                Properties p = new Properties();
                p.load(new FileInputStream(new File(args[i + 1])));
                Enumeration<?> enumeration = p.propertyNames();
                while (enumeration.hasMoreElements()) {
                    String key = (String) enumeration.nextElement();
                    if (key.toLowerCase().startsWith("feature.")) {
                        try {
                            e.addFeature((GlobalFeature) Class.forName(p.getProperty(key)).newInstance());
                        } catch (Exception e1) {
                            System.err.println("Could not add feature named " + p.getProperty(key));
                            e1.printStackTrace();
                        }
                    }
                }
            }
        }
        // check if there is an infile, an outfile and some features to extract.
        if (!e.isConfigured()) {
            printHelp();
        } else {
            e.run();
        }
    }

    private boolean isConfigured() {
        boolean configured = true;
        if (fileList==null || !fileList.exists()) configured = false;
        else if (outFile==null) {
            // create an outfile ...
            try {
                outFile = new File(fileList.getCanonicalPath() + ".data");
                System.out.println("Setting out file to " + outFile.getCanonicalFile());
            } catch (IOException e) {
                configured = false;
            }
        } else if (outFile.exists()) {
            System.err.println(outFile.getName() + " already exists. Please delete or choose another outfile.");
            configured = false;
        }
        if (listOfFeatures.size()<1) configured = false;
        return configured;
    }

    private static void printHelp() {
        System.out.println("Help for the Extractor class.\n" +
                "=============================\n" +
                "This help text is shown if you start the Extractor with the '-h' option.\n" +
                "\n" +
                "1. Usage\n" +
                "========\n" +
                "$> Extractor -i <infile> [-o <outfile>] -c <configfile>\n" +
                "\n" +
                "Note: if you don't specify an outfile just \".data\" is appended to the infile for output.\n" +
                "\n" +
                "2. Config File\n" +
                "==============\n" +
                "The config file is a simple java Properties file. It basically gives the \n" +
                "employed features as a list of properties, just like:\n" +
                "\n" +
                "feature.1=CEDD\n" +
                "feature.2=FCTH\n" +
                "\n" +
                "... and so on. ");
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
        byte[] myBuffer = new byte[1024*1024*10];
        int bufferCount = 0;
        try {
            BufferedReader br = new BufferedReader(new FileReader(fileList));
            OutputStream dos = new FileOutputStream(outFile);
            String file = null;
            String outFilePath = outFile.getCanonicalPath();
//            outFilePath = outFilePath.substring(0, outFilePath.lastIndexOf(outFile.getName()));
            long ms = System.currentTimeMillis();
            int count=0;
            while ((file = br.readLine()) != null) {
                File input = new File(file);
                String relFile = input.getCanonicalPath();//.substring(outFilePath.length());
                try {
                    BufferedImage img = ImageIO.read(input);
                    byte[] tmpBytes = relFile.getBytes();
                    // everything is written to a buffer and only if no exception is thrown, the image goes to index.
                    System.arraycopy(SerializationUtils.toBytes(tmpBytes.length), 0, myBuffer, 0, 4);
                    bufferCount += 4;
                    // dos.write(SerializationUtils.toBytes(tmpBytes.length));
                    System.arraycopy(tmpBytes, 0, myBuffer, bufferCount, tmpBytes.length);
                    bufferCount += tmpBytes.length;
                    // dos.write(tmpBytes);
                    for (GlobalFeature feature : listOfFeatures) {
                        feature.extract(img);
                        myBuffer[bufferCount] = (byte) feature2index.get(feature.getClass().getName()).intValue();
                        bufferCount++;
                        // dos.write(feature2index.get(feature.getClass().getName()));
                        tmpBytes = feature.getByteArrayRepresentation();
                        System.arraycopy(SerializationUtils.toBytes(tmpBytes.length), 0, myBuffer, bufferCount, 4);
//                        System.out.println(SerializationUtils.toInt(SerializationUtils.toBytes(tmpBytes.length)));
                        bufferCount += 4;
//                        System.out.println(file + ": " + Arrays.toString(feature.getByteArrayRepresentation()));
//                        System.out.println(file + ": " + Arrays.toString(feature.getFeatureVector()).replaceAll(", ", " "));
                        // dos.write(SerializationUtils.toBytes(tmpBytes.length));
                        System.arraycopy(tmpBytes, 0, myBuffer, bufferCount, tmpBytes.length);
                        bufferCount += tmpBytes.length;
                        // dos.write(tmpBytes);
                    }
                    // finally write everything to the stream - in case no exception was thrown..
                    dos.write(myBuffer, 0, bufferCount);
                    dos.write(-1);
                    bufferCount = 0;
                    count++;
                } catch (Exception e) {
                    System.err.println("Error processing image " + relFile + ": " + e.getMessage());
                    // e.printStackTrace();
                }
                if (count%100==0 && count > 0)
                    System.out.println(count + " files processed, " + (System.currentTimeMillis()-ms)/count + " ms per file.");
            }
            dos.flush();
            dos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
