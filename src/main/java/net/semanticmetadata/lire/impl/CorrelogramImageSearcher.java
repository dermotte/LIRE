/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.AbstractImageSearcher;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageDuplicates;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 01.02.2006
 * <br>Time: 00:17:02
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class CorrelogramImageSearcher extends AbstractImageSearcher {
    private Logger logger = Logger.getLogger(getClass().getName());
    private AutoColorCorrelogram.Mode mode = AutoColorCorrelogram.Mode.FullNeighbourhood;

    private int maxHits = 10;
    private TreeSet<SimpleResult> docs;

    public CorrelogramImageSearcher(int maxHits, AutoColorCorrelogram.Mode mode) {
        this.maxHits = maxHits;
        this.mode = mode;
        docs = new TreeSet<SimpleResult>();
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        logger.finer("Starting extraction of AutoColorCorrelogram from image");
        AutoColorCorrelogram acc = new AutoColorCorrelogram(CorrelogramDocumentBuilder.MAXIMUM_DISTANCE);
        // Scaling image is especially with the correlogram features very important!
        BufferedImage bimg = image;
        if (Math.max(image.getHeight(), image.getWidth()) > 200) {
            bimg = ImageUtils.scaleImage(image, 200);
        }
        acc.extract(bimg);
        logger.fine("Extraction from image finished");

        float maxDistance = findSimilar(reader, acc);
        return new SimpleImageSearchHits(this.docs, maxDistance);
    }

    /**
     * @param reader
     * @param acc
     * @return the maximum distance found for normalizing.
     * @throws java.io.IOException
     */
    private float findSimilar(IndexReader reader, AutoColorCorrelogram acc) throws IOException {
        float maxDistance = -1f, overallMaxDistance = -1f;
        boolean hasDeletions = reader.hasDeletions();

        // clear result set ...
        docs.clear();

        int docs = reader.numDocs();
        for (int i = 0; i < docs; i++) {
            // bugfix by Roman Kern
            if (hasDeletions && reader.isDeleted(i)) {
                continue;
            }

            Document d = reader.document(i);
            float distance = getDistance(d, acc);
            // calculate the overall max distance to normalize score afterwards
            if (overallMaxDistance < distance) {
                overallMaxDistance = distance;
            }
            // if it is the first document:
            if (maxDistance < 0) {
                maxDistance = distance;
            }
            // if the array is not full yet:
            if (this.docs.size() < maxHits) {
                this.docs.add(new SimpleResult(distance, d));
                if (distance > maxDistance) maxDistance = distance;
            } else if (distance < maxDistance) {
                // if it is nearer to the sample than at least on of the current set:
                // remove the last one ...
                this.docs.remove(this.docs.last());
                // add the new one ...
                this.docs.add(new SimpleResult(distance, d));
                // and set our new distance border ...
                maxDistance = this.docs.last().getDistance();
            }
        }
        return maxDistance;
    }

    private float getDistance(Document d, AutoColorCorrelogram acc) {
        float distance = 0f;
        AutoColorCorrelogram a = new AutoColorCorrelogram();
        String[] cls = d.getValues(DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
        if (cls != null && cls.length > 0) {
            a.setStringRepresentation(cls[0]);
            distance += acc.getDistance(a);
        }
        return distance;
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        AutoColorCorrelogram acc = new AutoColorCorrelogram();

        String[] cls = doc.getValues(DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
        if (cls != null && cls.length > 0)
            acc.setStringRepresentation(cls[0]);
        float maxDistance = findSimilar(reader, acc);

        return new SimpleImageSearchHits(this.docs, maxDistance);
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        // get the first document:
        if (!IndexReader.indexExists(reader.directory()))
            throw new FileNotFoundException("No index found at this specific location.");
        Document doc = reader.document(0);

        AutoColorCorrelogram acc = new AutoColorCorrelogram();
        String[] cls = doc.getValues(DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
        if (cls != null && cls.length > 0)
            acc.setStringRepresentation(cls[0]);

        HashMap<Float, List<String>> duplicates = new HashMap<Float, List<String>>();

        // find duplicates ...
        boolean hasDeletions = reader.hasDeletions();

        int docs = reader.numDocs();
        int numDuplicates = 0;
        for (int i = 0; i < docs; i++) {
            if (hasDeletions && reader.isDeleted(i)) {
                continue;
            }
            Document d = reader.document(i);
            float distance = getDistance(d, acc);

            if (!duplicates.containsKey(distance)) {
                duplicates.put(distance, new LinkedList<String>());
            } else {
                numDuplicates++;
            }
            duplicates.get(distance).add(d.getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

        if (numDuplicates == 0) return null;

        LinkedList<List<String>> results = new LinkedList<List<String>>();
        for (float f : duplicates.keySet()) {
            if (duplicates.get(f).size() > 1) {
                results.add(duplicates.get(f));
            }
        }
        return new SimpleImageDuplicates(results);
    }

}
