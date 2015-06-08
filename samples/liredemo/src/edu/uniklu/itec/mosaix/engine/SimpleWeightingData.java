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
 * Updated: 04.05.13 11:18
 */

package edu.uniklu.itec.mosaix.engine;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifReader;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.WeakHashMap;

/**
 * <p>Simple example implementation of the
 * <code>WeightingData</code> interface.</p>
 * <p>This implementation also meets the first
 * requirement of the
 * <code>LeastUsedWeightingStrategy</code>.</p>
 *
 * @author Manuel Warum
 * @author Mathias Lux, mathias@juggle.at
 * @version 1.02
 */
public final class SimpleWeightingData implements WeightingData {
    private float rel_;
    private String id_;
    private BufferedImage repl_ = null;
    private BufferedImage slice_;
    private static WeakHashMap<String, BufferedImage> imageCache = new WeakHashMap<String, BufferedImage>(100);
    private double scalePercentage = -1d;

    public float getRelevancy() {
        return rel_;
    }

    public void setScalePercentage(double scalePercentage) {
        this.scalePercentage = scalePercentage;
    }

    public BufferedImage getReplacement() {
        // lazy init of the file ....
        if (repl_ == null) {
            repl_ = imageCache.get(id_);
            if (repl_ == null) {
//                System.out.print(".");
                try {
                    repl_ = readFile();
                    // if (scalePercentage > 0) repl_ = ImageFunctions.scale(repl_, scalePercentage);
                    imageCache.put(id_, repl_);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                // System.out.println("Cache hit ...");
//                System.out.print("|");
            }
        }
        return repl_;
    }

    /**
     * Read the file from thumbnail or from original file.
     *
     * @return
     * @throws IOException
     */
    private BufferedImage readFile() throws IOException {
        BufferedImage image = null;
        FileInputStream jpegFile = new FileInputStream(id_);
        Metadata metadata = new Metadata();
        try {
            new ExifReader(jpegFile).extract(metadata);
            byte[] thumb = ((ExifDirectory) metadata.getDirectory(ExifDirectory.class)).getThumbnailData();
            if (thumb != null) image = ImageIO.read(new ByteArrayInputStream(thumb));
        } catch (JpegProcessingException e) {
            System.err.println("Could not extract thumbnail");
            e.printStackTrace();
        } catch (MetadataException e) {
            System.err.println("Could not extract thumbnail");
            e.printStackTrace();
        }
        // Fallback:
        if (image == null) {
            image = ImageIO.read(new FileInputStream(id_));
            // System.out.println("Could not read thumbnail.");
        }
        return image;
    }

    public void setId(String id_) {
        this.id_ = id_;
    }

    public BufferedImage getSlice() {
        return slice_;
    }

    /**
     * Returns the unique identifier provided to the
     * constructor (e.g. the file hashFunctionsFileName).
     *
     * @return a unique identifier.
     */
    public String getId() {
        return id_;
    }

    public void setRelevancy(float relevancy) {
        assert relevancy >= 0 && relevancy <= 1;
        rel_ = relevancy;
    }

    public void setReplacement(final BufferedImage replacement) {
        assert replacement != null;
        repl_ = replacement;
    }

    public void setSlice(final BufferedImage slice) {
        assert slice != null;
        slice_ = slice;
    }

    /**
     * Creates a new instance of this <code>WeightingData</code>
     * implementation.
     *
     * @param id a non-<code>null</code> String with a length
     *           greater than zero. This should uniquely identify the
     *           resource (e.g. the file hashFunctionsFileName).
     */
    public SimpleWeightingData(String id) {
        assert id != null && id.length() > 0;
        this.id_ = id;
    }

    @Override
    public int hashCode() {
        return this.id_.hashCode();
    }
}
