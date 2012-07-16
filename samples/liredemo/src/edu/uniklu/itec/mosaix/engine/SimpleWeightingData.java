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

/*
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2007 by Mathias Lux (mathias@juggle.at), Lukas Esterle & Manuel Warum.
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/*
* This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
*
* Caliph & Emir is free software; you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation; either version 2 of the License, or
* (at your option) any later version.
*
* Caliph & Emir is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with Caliph & Emir; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*
* Copyright statement:
* --------------------
* (c) 2002-2007 by Mathias Lux (mathias@juggle.at), Lukas Esterle & Manuel Warum.
* http://www.juggle.at, http://www.SemanticMetadata.net
*/

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
     * constructor (e.g. the file name).
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
     *           resource (e.g. the file name).
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
