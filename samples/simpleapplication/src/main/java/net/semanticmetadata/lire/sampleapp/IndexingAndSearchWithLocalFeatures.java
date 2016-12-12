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
 * (c) 2002-2016 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 */
package net.semanticmetadata.lire.sampleapp;

import net.semanticmetadata.lire.aggregators.AbstractAggregator;
import net.semanticmetadata.lire.aggregators.BOVW;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.imageanalysis.features.local.opencvfeatures.CvSurfExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.indexers.parallel.ImagePreprocessor;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;
import net.semanticmetadata.lire.searchers.GenericFastImageSearcher;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.ImageSearcher;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

/**
 * Simple class showing the process of indexing and searching for local & SIMPLE descriptors.
 * Note that you have to run it with giving the OpenCV library path, eg. "-Djava.library.path="lib\opencv"
 * @author Mathias Lux, mathias@juggle.at
 */
public class IndexingAndSearchWithLocalFeatures {
    public static void main(String[] args) throws IOException {
        // indexing all images in "testdata"
        index("index", "testdata");
        // searching through the images.
        search("index");
    }

    /**
     * Linear search on the indexed data.
     * @param indexPath
     * @throws IOException
     */
    public static void search(String indexPath) throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));

        // make sure that this matches what you used for indexing (see below) ...
        ImageSearcher imgSearcher = new GenericFastImageSearcher(1000, CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), 128, true, reader, indexPath + ".config");
        // just a static example with a given image.
        ImageSearchHits hits = imgSearcher.search(ImageIO.read(new File("testdata/ferrari/black/2828686873_2fa36f83d7_b.jpg")), reader);
        for (int i=0; i<hits.length(); i++) {
            System.out.printf("%.2f: (%d) %s\n", hits.score(i), hits.documentID(i), reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
        }
    }

    /**
     * Indexing data using OpenCV and SURF as well as CEDD and SIMPLE.
     * @param index
     * @param imageDirectory
     */
    public static void index(String index, String imageDirectory) {
        // Checking if arg[0] is there and if it is a directory.
        boolean passed = false;
        // use ParallelIndexer to index all photos from args[0] into "index".
        int numOfDocsForVocabulary = 500;
        Class<? extends AbstractAggregator> aggregator = BOVW.class;
        int[] numOfClusters = new int[] {128};

        ParallelIndexer indexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, index, imageDirectory, numOfClusters, numOfDocsForVocabulary, aggregator);
        indexer.setImagePreprocessor(new ImagePreprocessor() {
            @Override
            public BufferedImage process(BufferedImage image) {
                return ImageUtils.createWorkingCopy(image);
            }
        });
        //Local
        indexer.addExtractor(CvSurfExtractor.class);
        //Simple
        indexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSURF);

        indexer.run();
        System.out.println("Finished indexing.");
    }
}
