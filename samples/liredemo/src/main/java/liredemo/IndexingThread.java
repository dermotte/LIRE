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
 * Updated: 26.04.13 14:48
 */

package liredemo;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.jpeg.JpegProcessingException;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifThumbnailDirectory;
import liredemo.indexing.MetadataBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.*;
import net.semanticmetadata.lire.imageanalysis.features.global.joint.JointHistogram;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;

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
                            new java.io.File(parent.textfieldImageDirectoryToIndex.getText()), true);
            if (images == null) {
                JOptionPane.showMessageDialog(parent, "Could not find any files in " + parent.textfieldImageDirectoryToIndex.getText(), "No files found", JOptionPane.WARNING_MESSAGE);
                return;
            }
            boolean create = !parent.checkBoxAddToExisintgIndex.isSelected();
//            IndexWriter iw;
//            if (create) {
//                iw = LuceneUtils.createIndexWriter(parent.textfieldIndexName.getText(), true);//new IndexWriter(FSDirectory.open(new File(parent.textfieldIndexName.getText())), new SimpleAnalyzer(), create, IndexWriter.MaxFieldLength.UNLIMITED);
//            } else {
//                iw = LuceneUtils.createIndexWriter(parent.textfieldIndexName.getText(), false);
//            }
            int builderIdx = parent.selectboxDocumentBuilder.getSelectedIndex();
//            DocumentBuilder builder = new MetadataBuilder();
            int count = 0;
            long time = System.currentTimeMillis();
//            Document doc;
//            ParallelIndexer indexer = new ParallelIndexer(images, builder);
            ParallelIndexer pin = new ParallelIndexer(8, parent.textfieldIndexName.getText(), parent.textfieldImageDirectoryToIndex.getText());
//            ParallelIndexer pin =
//                    new ParallelIndexer(8, parent.textfieldIndexName.getText(), parent.textfieldImageDirectoryToIndex.getText(), create){
//                        @Override
//                        public void addBuilders(ChainedDocumentBuilder builder) {
//                            builder.addBuilder(new MetadataBuilder());
//                        }
//                    };
////            new Thread(indexer).start();
            pin.addExtractor(ColorLayout.class);
            pin.addExtractor(CEDD.class);
            pin.addExtractor(FCTH.class);
            pin.addExtractor(JCD.class);
            pin.addExtractor(ScalableColor.class);
            pin.addExtractor(EdgeHistogram.class);
            pin.addExtractor(AutoColorCorrelogram.class);
            pin.addExtractor(Tamura.class);
            pin.addExtractor(Gabor.class);
            pin.addExtractor(SimpleColorHistogram.class);
            pin.addExtractor(OpponentHistogram.class);
            pin.addExtractor(JointHistogram.class);
            pin.addExtractor(LuminanceLayout.class);
            pin.addExtractor(PHOG.class);
            pin.addExtractor(ACCID.class);
            pin.addExtractor(COMO.class);
            pin.setCustomDocumentBuilder(MetadataBuilder.class);
            Thread t = new Thread(pin);
            t.start();
            while (!pin.hasEnded()) {
                float percentage = (float) pin.getPercentageDone();
                parent.progressBarIndexing.setValue((int) Math.floor(100f * percentage));
                parent.progressBarIndexing.setString("~ " + ((int) Math.floor(100f * percentage)) + "% analyzed.");
//                float msleft = (float) (System.currentTimeMillis() - time) / percentage;
//                float secLeft = msleft * (1 - percentage) / 1000f;
//                String toPaint = "~ " + df.format(secLeft) + " sec. left";
//                if (secLeft > 90) toPaint = "~ " + Math.ceil(secLeft / 60) + " min. left";
//                parent.progressBarIndexing.setString(toPaint);
                try {
                    Thread.currentThread().sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            long timeTaken = (System.currentTimeMillis() - time);
            float sec = ((float) timeTaken) / 1000f;
            System.out.println("Finished indexing ...");
            parent.progressBarIndexing.setString("Indexing finished in " + Math.round(sec) + " seconds.");
//            parent.progressBarIndexing.setString(Math.round(sec) + " sec. for " + count + " files");
            parent.buttonStartIndexing.setEnabled(true);
            parent.progressBarIndexing.setValue(100);
//            iw.commit();
//            iw.close();

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
         new Metadata();
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(jpegFile);
            ExifThumbnailDirectory tDir = metadata.getFirstDirectoryOfType(ExifThumbnailDirectory.class);
            if (tDir.hasThumbnailData()) {
                byte[] thumb = tDir.getThumbnailData();
                if (thumb != null) image = ImageIO.read(new ByteArrayInputStream(thumb));
            }
//            System.out.print("Read from thumbnail data ... ");
//            System.out.println(image.getWidth() + " x " + image.getHeight());
        } catch (JpegProcessingException e) {
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
