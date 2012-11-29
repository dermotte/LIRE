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

package edu.uniklu.itec.mosaix;

import edu.uniklu.itec.mosaix.engine.Engine;
import edu.uniklu.itec.mosaix.engine.LeastUsedWeightingStrategy;
import edu.uniklu.itec.mosaix.engine.ProportionWeightingStrategy;
import liredemo.ProgressMonitor;
import net.semanticmetadata.lire.*;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

/**
 * provides all relevant image-functions for the mosaix project!
 *
 * @author Lukas Esterle
 * @author Mathias Lux, mathias@juggle.at
 */
public class ImageFunctions {

    IndexReader reader = null;
    ImageSearcher searcher = null;
    private Engine eng;

    public ImageFunctions() {
        eng = new Engine();
    }

    /**
     * Scales a BufferedImage to a specified percentage.
     *
     * @param bi   the BufferdImage to be scaled
     * @param perc the scalig percentage
     * @return a scaled BufferedImage
     */
    public static BufferedImage scale(BufferedImage bi, double perc) {

        int cw = (int) (bi.getWidth() * perc / 100);
        int ch = (int) (bi.getHeight() * perc / 100);
        BufferedImage scaled = new BufferedImage(cw, ch, bi.getType());

        Graphics g = scaled.getGraphics();
        g.drawImage(bi, 0, 0, cw, ch, null);

        return scaled;
    }

    /**
     * loads an image as BufferedImage
     *
     * @param file the file to be loaded
     * @return an BufferedImage of the file
     */
    public BufferedImage loadImage(File file) {
        BufferedImage bi = null;
        try {
            bi = ImageIO.read(file);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return bi;
    }

    public void saveImage(BufferedImage bi, File file) {
        try {
            if (file.getName().endsWith(".jpg") || file.getName().endsWith(".JPG")) {
                ImageIO.write(bi, "jpg", file);
            } else {
                System.out.println("umschreiben des files");
                File newFile = new File(file.getAbsolutePath() + ".jpg");
                ImageIO.write(bi, "jpg", newFile);
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    /**
     * Splitts the BufferedImage up into {@code nrRaster} rows and {@code nrRaster} columns.
     * Every element is put into an 2-dimensional array
     *
     * @param bi       the original BufferedImage
     * @param nrRaster number of rows (columns) the image will be splitt
     * @return a 2-dimensional BufferedImage array with the elements
     */
    public BufferedImage[][] splitImage(BufferedImage bi, Dimension nrRaster) {
        BufferedImage[][] retVal = new BufferedImage[(int) nrRaster.getHeight()][(int) nrRaster.getWidth()];
        int vertDist = bi.getHeight() / (int) nrRaster.getHeight();
        int horDist = bi.getWidth() / (int) nrRaster.getWidth();
//		System.out.println("#width=" + nrRaster.width + " #height=" + nrRaster.height);
        for (int i = 0; i < (int) nrRaster.getHeight(); i++) {
            for (int j = 0; j < (int) nrRaster.getWidth(); j++) {
//				System.out.println("actI=" + i + " actJ=" + j);
                retVal[i][j] = bi.getSubimage(j * horDist, i * vertDist, horDist, vertDist); //x, y, w, h
            }
        }

        return retVal;
    }

    /**
     * this method opens searches for a similar image within the allready indexed image-files
     *
     * @param bi   the image for which a similar one is searched
     * @param path the path to the indexed files
     * @return ImageSearchHits - result from LIRe, max. 10 elements
     */
    public ImageSearchHits getLireResults(BufferedImage bi, String path, float colorHist, float colorDist, float texture) {
        ImageSearchHits hits = null;
        try {
            if (reader == null) {
                reader = IndexReader.open(FSDirectory.open(new File(path)));
            }
            if (searcher == null) {
                if (colorHist > 0f) {
//              et  System.out.println("Using AutoColorCorrelogram Searcher ...");
//                    searcher = ImageSearcherFactory.createDefaultCorrelogramImageSearcher(50);
                    searcher = ImageSearcherFactory.createCEDDImageSearcher(50);
                } else if (texture > 0f) {
                    searcher = ImageSearcherFactory.createAutoColorCorrelogramImageSearcher(50);
                } else if (colorDist > 0f) {
//                System.out.println("Using Default Weighted Searcher ...");
//                    searcher = ImageSearcherFactory.createWeightedSearcher(50, colorHist, colorDist, texture); //.createSimpleSearcher(10);
                }
            }
            hits = searcher.search(bi, reader);
            // reader.close();
        } catch (IOException e) {
        }
        return hits;
    }

    /**
     * uses LIRe to index BufferedImages within the param path. but only if
     * an index does not exist or the user forced the indexing process
     *
     * @param path the path to the bufferedImages which will be indexed
     * @return the number of indexed images
     */
    public int imageIndexing(String path, boolean force) {
        int count = 0;
        IndexReader ir;
        boolean hasIndex = false;

        try {
            hasIndex = DirectoryReader.indexExists(FSDirectory.open(new File(path)));
        } catch (IOException e) {
            e.printStackTrace();
        }


        if (force || (hasIndex == false)) {
            try {
                java.util.ArrayList<java.lang.String> images = getAllImages(new java.io.File(path), true);
                IndexWriter iw = LuceneUtils.createIndexWriter(path, true);
//                IndexWriter iw = new IndexWriter(FSDirectory.open(new File(path)), new SimpleAnalyzer(), true, IndexWriter.MaxFieldLength.UNLIMITED);
                DocumentBuilder builder = DocumentBuilderFactory.getFullDocumentBuilder();
                for (String identifier : images) {
                    Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
                    iw.addDocument(doc);
                    count++;
                }
                iw.commit();
                iw.close();
            } catch (IOException ex) {
                ex.printStackTrace();
//	            System.out.println(ex.toString());
            }
        }
        return count;
    }

    /**
     * this method returns all images from a directory in an arrayList
     *
     * @param directory                 the directory where all images should be found
     * @param descendIntoSubDirectories - decides if subdirectories should be used also
     * @return the filenames as ArrayList<String>
     * @throws IOException
     */
    public static ArrayList<String> getAllImages(File directory, boolean descendIntoSubDirectories) throws IOException {
        ArrayList<String> resultList = new ArrayList<String>(256);
        File[] f = directory.listFiles();
        for (File file : f) {
            if (file != null && (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".JPG")) && !file.getName().startsWith("tn_")) {
                resultList.add(file.getCanonicalPath());
            }
            if (descendIntoSubDirectories && file.isDirectory()) {
                ArrayList<String> tmp = getAllImages(file, true);
                if (tmp != null) {
                    resultList.addAll(tmp);
                }
            }
        }
        if (resultList.size() > 0) {
            return resultList;
        } else {
            return null;
        }
    }

    public BufferedImage getMosaic(BufferedImage origImg, Dimension mainframe, Dimension raster, float colh, float cold, float tex, String indexPath, ProgressMonitor progress) {
        eng = new Engine(); // resets the re-weighting of already used images.
        //TODO (origImg.getWidth()/raster.width)*raster.width
        BufferedImage finalImg = new BufferedImage((origImg.getWidth() / raster.width) * raster.width, (origImg.getHeight() / raster.height) * raster.height, origImg.getType());
        BufferedImage result = null;
        BufferedImage scaled = null;
        BufferedImage[][] splitted = this.splitImage(origImg, raster);

        int ch = origImg.getHeight();
        int cw = origImg.getWidth();
        int perc = 100;
        if (origImg.getWidth() < origImg.getHeight()) {
            //high-format
            if (origImg.getHeight() > mainframe.getHeight()) {
                //image is higher as the frame
                ch = mainframe.height;
                perc = 100 * ch / origImg.getHeight();
            }
        } else {
            if (origImg.getWidth() > mainframe.getWidth()) {
                cw = mainframe.width;
                perc = 100 * cw / origImg.getWidth();
            }
        }

        LeastUsedWeightingStrategy luws = new LeastUsedWeightingStrategy();
        ProportionWeightingStrategy pws = new ProportionWeightingStrategy();
        eng.addStrategy(luws);
        eng.addStrategy(pws);
        eng.addObserver(luws);
        float steps = splitted.length * splitted[0].length;
        for (int i = 0; i < splitted.length; i++) {
            for (int j = 0; j < splitted[0].length; j++) {
                ImageSearchHits hits = this.getLireResults(splitted[i][j], indexPath, colh, cold, tex);
                try {
                    result = eng.findBestMatch(splitted[i][j], hits, perc);
//					scaled = this.scale(result, perc);
                    finalImg = this.assemble(finalImg, result, j, i, raster, true);
                    progress.setProgress((int) (100 * ((float) (i * splitted.length + j + 1)) / steps));
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            }
        }
        eng.reset();
        reset();
        return finalImg;
    }

    private void reset() {
        // TODO: reset Searcher and Reader ...
    }

    /**
     * This method assembles two bufferedImages to one new Image! the extending image needs to
     * provide enough space for the extender (second) image.
     * <p/>
     * posX and posY defines the position of the extender image in an imaginary raster
     * <p/>
     * rasternr defines the number of elements in one row (column)
     *
     * @param extending the image which will be extanded
     * @param extender  the image which will extand the extending
     * @param posX      the x-position
     * @param posY      the y-position
     * @param nrRaster  number of elements in one row (column)
     * @return the assembled BufferedImage
     */
    public BufferedImage assemble(BufferedImage extending, BufferedImage extender, int posX, int posY, Dimension nrRaster, boolean scale) {
        BufferedImage result = extending;
        int vertDist = result.getHeight() / (int) nrRaster.getHeight();
        int horDist = result.getWidth() / (int) nrRaster.getWidth();
        if (!scale) {
            horDist = extender.getWidth();
            vertDist = extender.getHeight();
        }
        Graphics g = result.getGraphics();
        g.drawImage(extender, posX * horDist, posY * vertDist, horDist, vertDist, null);

        return result;
    }
}
