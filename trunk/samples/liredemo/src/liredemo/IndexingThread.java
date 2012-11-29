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
 * ~~~~~~~~~~~~~~~~~~~~
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package liredemo;

import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.MetadataException;
import com.drew.metadata.exif.ExifDirectory;
import com.drew.metadata.exif.ExifReader;
import liredemo.indexing.MetadataBuilder;
import liredemo.indexing.ParallelIndexer;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class IndexingThread extends Thread {
    LireDemoFrame parent;

    /**
     * Creates a new instance of FlickrIndexingThread
     *
     * @param parent
     */
    public IndexingThread(LireDemoFrame parent) {
        this.parent = parent;
    }

    // TODO: make parallel

    public void run() {
        DecimalFormat df = (DecimalFormat) DecimalFormat.getInstance();
        df.setMaximumFractionDigits(0);
        df.setMinimumFractionDigits(0);
        try {
            parent.progressBarIndexing.setValue(0);
            java.util.ArrayList<java.lang.String> images =
                    getAllImages(
                            new java.io.File(parent.textfieldIndexDir.getText()), true);
            if (images == null) {
                JOptionPane.showMessageDialog(parent, "Could not find any files in " + parent.textfieldIndexDir.getText(), "No files found", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean create = !parent.checkBoxAddToExisintgIndex.isSelected();
            IndexWriter iw;
            if (create) {
                iw = LuceneUtils.createIndexWriter(parent.textfieldIndexName.getText(), true);//new IndexWriter(FSDirectory.open(new File(parent.textfieldIndexName.getText())), new SimpleAnalyzer(), create, IndexWriter.MaxFieldLength.UNLIMITED);
            } else {
                iw = LuceneUtils.createIndexWriter(parent.textfieldIndexName.getText(), false);
            }
            int builderIdx = parent.selectboxDocumentBuilder.getSelectedIndex();
            DocumentBuilder builder = new MetadataBuilder();
            int count = 0;
            long time = System.currentTimeMillis();
            Document doc;
            ParallelIndexer indexer = new ParallelIndexer(images, builder);
            new Thread(indexer).start();
            while ((doc = indexer.getNext()) != null) {
                try {
                    iw.addDocument(doc);
                } catch (Exception e) {
                    System.err.println("Could not add document.");
                    e.printStackTrace();
                }
                count++;
                float percentage = (float) count / (float) images.size();
                parent.progressBarIndexing.setValue((int) Math.floor(100f * percentage));
                float msleft = (float) (System.currentTimeMillis() - time) / percentage;
                float secLeft = msleft * (1 - percentage) / 1000f;
                String toPaint = "~ " + df.format(secLeft) + " sec. left";
                if (secLeft > 90) toPaint = "~ " + Math.ceil(secLeft / 60) + " min. left";
                parent.progressBarIndexing.setString(toPaint);
            }
            long timeTaken = (System.currentTimeMillis() - time);
            float sec = ((float) timeTaken) / 1000f;
            System.out.println("Finished indexing ...");
            parent.progressBarIndexing.setString(Math.round(sec) + " sec. for " + count + " files");
            parent.buttonStartIndexing.setEnabled(true);
            parent.progressBarIndexing.setValue(100);
            iw.commit();
            iw.close();

        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }

    public static ArrayList<String> getAllImages(File directory, boolean descendIntoSubDirectories) throws IOException {
        ArrayList<String> resultList = new ArrayList<String>(256);
        File[] f = directory.listFiles();
        for (File file : f) {
            if (file != null && (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".png")) && !file.getName().startsWith("tn_")) {
                resultList.add(file.getCanonicalPath());
            }
            if (descendIntoSubDirectories && file.isDirectory()) {
                ArrayList<String> tmp = getAllImages(file, true);
                if (tmp != null) {
                    resultList.addAll(tmp);
                }
            }
        }
        if (resultList.size() > 0)
            return resultList;
        else
            return null;
    }

    private BufferedImage readFile(String path) throws IOException {
        BufferedImage image = null;
        FileInputStream jpegFile = new FileInputStream(path);
        Metadata metadata = new Metadata();
        try {
            new ExifReader(jpegFile).extract(metadata);
            byte[] thumb = ((ExifDirectory) metadata.getDirectory(ExifDirectory.class)).getThumbnailData();
            if (thumb != null) image = ImageIO.read(new ByteArrayInputStream(thumb));
//            System.out.print("Read from thumbnail data ... ");
//            System.out.println(image.getWidth() + " x " + image.getHeight());
        } catch (JpegProcessingException e) {
            System.err.println("Could not extract thumbnail");
            e.printStackTrace();
        } catch (MetadataException e) {
            System.err.println("Could not extract thumbnail");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Could not extract thumbnail");
            e.printStackTrace();
        }
        // Fallback:
        if (image == null) image = ImageIO.read(new FileInputStream(path));
        return image;
    }

}
