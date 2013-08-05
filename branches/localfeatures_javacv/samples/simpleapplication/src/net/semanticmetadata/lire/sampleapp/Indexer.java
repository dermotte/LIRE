/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
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
 * Updated: 21.04.13 08:13
 */

package net.semanticmetadata.lire.sampleapp;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * User: Mathias Lux, mathias@juggle.at
 * Date: 25.05.12
 * Time: 12:04
 */
public class Indexer {
    public static void main(String[] args) throws IOException {
        // Checking if arg[0] is there and if it is a directory.
        boolean passed = false;
        if (args.length > 0) {
            File f = new File(args[0]);
            System.out.println("Indexing images in " + args[0]);
            if (f.exists() && f.isDirectory()) passed = true;
        }
        if (!passed) {
            System.out.println("No directory given as first argument.");
            System.out.println("Run \"Indexer <directory>\" to index files of a directory.");
            System.exit(1);
        }
        // Getting all images from a directory and its sub directories.
        ArrayList<String> images = FileUtils.getAllImages(new File(args[0]), true);

        // Creating a CEDD document builder and indexing all files.
        DocumentBuilder builder = DocumentBuilderFactory.getCEDDDocumentBuilder();
        // Creating an Lucene IndexWriter
        IndexWriterConfig conf = new IndexWriterConfig(LuceneUtils.LUCENE_VERSION,
                new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));
        IndexWriter iw = new IndexWriter(FSDirectory.open(new File("index")), conf);
        // Iterating through images building the low level features
        for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
            String imageFilePath = it.next();
            System.out.println("Indexing " + imageFilePath);
            try {
                BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                Document document = builder.createDocument(img, imageFilePath);
                iw.addDocument(document);
            } catch (Exception e) {
                System.err.println("Error reading image or indexing it.");
                e.printStackTrace();
            }
        }
        // closing the IndexWriter
        iw.close();
        System.out.println("Finished indexing.");
    }
}
