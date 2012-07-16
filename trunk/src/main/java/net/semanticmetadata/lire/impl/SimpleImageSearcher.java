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
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.EdgeHistogram;
import net.semanticmetadata.lire.imageanalysis.ScalableColor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 01.02.2006
 * <br>Time: 00:17:02
 *
 * @author Mathias Lux, mathias@juggle.at
 * @deprecated Use ColorLayout, EdgeHistogram and ScalableColor features instead.
 */
public class SimpleImageSearcher extends AbstractImageSearcher {
    private int maxHits = 10;
    float colorHistogramWeight = 1f;
    float colorDistributionWeight = 1f;
    float textureWeight = 1f;
    private TreeSet<SimpleResult> docs;

    public SimpleImageSearcher(int maxHits) {
        this.maxHits = maxHits;
        docs = new TreeSet<SimpleResult>();
    }

    public SimpleImageSearcher(int maxHits,
                               float colorHistogramWeight,
                               float colorDistributionWeight,
                               float textureWeight) {
        this.maxHits = maxHits;
        docs = new TreeSet<SimpleResult>();
        this.colorDistributionWeight = colorDistributionWeight;
        this.colorHistogramWeight = colorHistogramWeight;
        this.textureWeight = textureWeight;
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        ScalableColor sc = null;
        ColorLayout cl = null;
        EdgeHistogram eh = null;
        if (colorHistogramWeight > 0f) {
            sc = new ScalableColor();
            sc.extract(image);
        }
        if (colorDistributionWeight > 0f) {
            cl = new ColorLayout();
            cl.extract(image);
        }
        if (textureWeight > 0f) {
            eh = new EdgeHistogram();
            eh.extract(image);
        }

        float maxDistance = findSimilar(reader, cl, sc, eh);
        return new SimpleImageSearchHits(this.docs, maxDistance);
    }

    /**
     * @param reader
     * @param cl
     * @param sc
     * @param eh
     * @return the maximum distance found for normalizing.
     * @throws IOException
     */
    private float findSimilar(IndexReader reader, ColorLayout cl, ScalableColor sc, EdgeHistogram eh) throws IOException {
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
            float distance = getDistance(d, cl, sc, eh);
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

    private float getDistance(Document d, ColorLayout cl, ScalableColor sc, EdgeHistogram eh) {
        float distance = 0f;
        int descriptorCount = 0;

        if (cl != null) {
            String[] cls = d.getValues(DocumentBuilder.FIELD_NAME_COLORLAYOUT);
            if (cls != null && cls.length > 0) {
                ColorLayout clsi = new ColorLayout();
                clsi.setStringRepresentation(cls[0]);
                distance += cl.getDistance(clsi) * colorDistributionWeight;
                descriptorCount++;
            }
        }

        if (sc != null) {
            String[] scs = d.getValues(DocumentBuilder.FIELD_NAME_SCALABLECOLOR);
            if (scs != null && scs.length > 0) {
                ScalableColor scsi = new ScalableColor();
                scsi.setStringRepresentation(scs[0]);
                distance += sc.getDistance(scsi) * colorHistogramWeight;
                descriptorCount++;
            }
        }

        if (eh != null) {
            String[] ehs = d.getValues(DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
            if (ehs != null && ehs.length > 0) {
                EdgeHistogram ehsi = new EdgeHistogram();
                ehsi.setStringRepresentation(ehs[0]);
                distance += eh.getDistance(ehsi) * textureWeight;
                descriptorCount++;
            }
        }

        if (descriptorCount > 0) {
            // TODO: find some better scoring mechanism, e.g. some normalization. One thing would be linearization of the features!
            // For now: Averaging ...
            distance = distance / (float) descriptorCount;
        }
        return distance;
    }

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        ScalableColor sc = null;
        ColorLayout cl = null;
        EdgeHistogram eh = null;

        String[] cls = doc.getValues(DocumentBuilder.FIELD_NAME_COLORLAYOUT);
        if (cls != null && cls.length > 0) {
            cl = new ColorLayout();
            cl.setStringRepresentation(cls[0]);
        }
        String[] scs = doc.getValues(DocumentBuilder.FIELD_NAME_SCALABLECOLOR);
        if (scs != null && scs.length > 0) {
            sc = new ScalableColor();
            sc.setStringRepresentation(scs[0]);
        }
        String[] ehs = doc.getValues(DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
        if (ehs != null && ehs.length > 0) {
            eh = new EdgeHistogram();
            eh.setStringRepresentation(ehs[0]);
        }

        float maxDistance = findSimilar(reader, cl, sc, eh);

        return new SimpleImageSearchHits(this.docs, maxDistance);
    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        // get the first document:
        if (!IndexReader.indexExists(reader.directory()))
            throw new FileNotFoundException("No index found at this specific location.");
        Document doc = reader.document(0);
        ScalableColor sc = null;
        ColorLayout cl = null;
        EdgeHistogram eh = null;

        String[] cls = doc.getValues(DocumentBuilder.FIELD_NAME_COLORLAYOUT);
        if (cls != null && cls.length > 0) {
            cl = new ColorLayout();
            cl.setStringRepresentation(cls[0]);
        }
        String[] scs = doc.getValues(DocumentBuilder.FIELD_NAME_SCALABLECOLOR);
        if (scs != null && scs.length > 0) {
            sc = new ScalableColor();
            sc.setStringRepresentation(scs[0]);
        }
        String[] ehs = doc.getValues(DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
        if (ehs != null && ehs.length > 0) {
            eh = new EdgeHistogram();
            eh.setStringRepresentation(ehs[0]);
        }

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
            float distance = getDistance(d, cl, sc, eh);

            if (!duplicates.containsKey(distance)) {
                duplicates.put(distance, new LinkedList<String>());
            } else {
                numDuplicates++;
            }
            duplicates.get(distance).add(d.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
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
