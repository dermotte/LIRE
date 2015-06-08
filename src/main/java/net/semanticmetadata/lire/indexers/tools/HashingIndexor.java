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
 * Updated: 02.06.13 08:13
 */

package net.semanticmetadata.lire.indexers.tools;

import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.global.PHOG;
import net.semanticmetadata.lire.indexers.hashing.BitSampling;
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
    protected Class featureClass = PHOG.class;

    public static void main(String[] args) throws IOException, IllegalAccessException, InstantiationException {
        HashingIndexor indexor = new HashingIndexor();
        BitSampling.readHashFunctions();
//        BitSampling.readHashFunctions(new FileInputStream(BitSampling.hashFunctionsFileName));
//        LocalitySensitiveHashing.readHashFunctions();
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
            } else if (arg.startsWith("-s")) {
                // silent ...
                verbose = false;
            } else if (arg.startsWith("-c")) {
                // list of input files within a file.
                if ((i + 1) < args.length) {
                    BufferedReader br = new BufferedReader(new FileReader(new File(args[i + 1])));
                    String file;
                    while ((file = br.readLine()) != null) {
                        if (file.trim().length() > 2) {
                            File f = new File(file);
                            if (f.exists()) indexor.addInputFile(f);
                            else System.err.println("Did not find file " + f.getName());
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

    protected void addToDocument(GlobalFeature feature, Document document, String featureFieldName) {
        // This is for debugging the image features.
//        try {
////            System.out.println(feature.getClass().getName() + " " + document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
//            LireFeature f1 = feature.getClass().newInstance();
//            f1.extract(ImageIO.read(new File(document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0])));
//            float distance = feature.getDistance(f1);
//            if (distance != 0) {
//                System.out.println("Extracted:" + java.util.Arrays.toString(f1.getFeatureVector()).replaceAll("\\.0,", "") + "\n" +
//                        "Data     :" + java.util.Arrays.toString(feature.getFeatureVector()).replaceAll("\\.0,", "") + "\n" +
//                        "Problem with " + f1.getClass().getName() + " at file " + document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0] + ", distance=" + distance
//                );
////                System.out.println("Problem with " + f1.getClass().getName() + " at file " + document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0] + ", distance=" + distance);
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//
//        }
        if (feature.getClass().getCanonicalName().equals(featureClass.getCanonicalName())) {
            // generate hashes here:
//            int[] hashes = LocalitySensitiveHashing.generateHashes(feature.getFeatureVector());
            int[] hashes = BitSampling.generateHashes(feature.getFeatureVector());
//            System.out.println(Arrays.toString(hashes));
            // store hashes in index as terms
            document.add(new TextField(featureFieldName + "_hash", SerializationUtils.arrayToString(hashes), Field.Store.YES));
            // add the specific feature
            document.add(new StoredField(featureFieldName, feature.getByteArrayRepresentation()));
        }
        // add the specific feature
//        document.add(new StoredField(featureFieldName, feature.getByteArrayRepresentation()));
    }


}
