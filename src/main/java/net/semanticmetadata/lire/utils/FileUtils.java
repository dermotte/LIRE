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
 * Updated: 09.12.14 10:27
 */

package net.semanticmetadata.lire.utils;

import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.global.ColorLayout;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.TopDocs;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 04.02.2006
 * <br>Time: 09:44:49
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author sangupta, sandy.pec@gmail.com (closed streams in finally clause)
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class FileUtils {
    enum FileTypes {JPG, GIF, TIF, PNG, PDF, UNKNOWN};
    /**
     * Returns all jpg images from a directory in an array.
     *
     * @param directory                 the directory to start with
     * @param descendIntoSubDirectories should we include sub directories?
     * @return an ArrayList<String> containing all the files or nul if none are found..
     * @throws IOException
     */
    public static ArrayList<String> getAllImages(File directory, boolean descendIntoSubDirectories) throws IOException {
        ArrayList<String> resultList = new ArrayList<String>(256);
        File[] f = directory.listFiles();
        for (File file : f) {
            if (file != null && (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".png") || file.getName().toLowerCase().endsWith(".gif")) && !file.getName().startsWith("tn_")) {
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

//    public static BufferedImage openImage(String path) {
//        BufferedImage result = null;
//        try {
//            result = ImageIO.read(new FileInputStream(path));
//        } catch (Exception e) {
//            System.err.println("Couldn't open image with Java, trying with Sanselan. " + path + ", " + e.getMessage());
//        }
//
//        if (result == null) {
//            try {
//                result = Sanselan.getBufferedImage(new FileInputStream(path));
//            } catch (Exception e) {
//                System.err.println("Couldn't open image with Sanselan, trying with IJ. " + path + ", " + e.getMessage());
//            }
//        }
//
//        if (result == null) {
//            try {
//                ImagePlus imgPlus = new ImagePlus(path);
//                ImageConverter imageConverter = new ImageConverter(imgPlus);
//                imageConverter.convertToRGB();
//                result = imgPlus.getBufferedImage();
//            } catch (Exception e) {
//                System.err.println("Couldn't open image with IJ. " + path + ", " + e.getMessage());
//            }
//        }
//        // try to trim the image to reduce the noise introduced by white borders ...
//        if (result != null) {
//            try {
//                if (result.getColorModel().getPixelSize() != 24) {
//                    BufferedImage tmp = new BufferedImage(result.getWidth(), result.getHeight(), BufferedImage.TYPE_INT_RGB);
//                    tmp.getGraphics().drawImage(result, 0, 0, null);
//                    result = tmp;
//                }
//                result = trimWhiteSpace(result);
//            } catch (Exception e) {
//                // do nothing here ...
//                System.err.println("Could not trim image " + path);
//            }
//        }
//        // check image size to find out if image is some kind of placeholder gif or something like that
//        // or a just white or just black image, which has been trimmed to nearly nothing.
//        if (result != null) {
//            if (result.getWidth() < 5 || result.getHeight() < 5) {
//                result = null; // we don't need those
//                System.err.println("Skipping file due to its size: " + path);
//            }
//        }
//        return result;
//    }

    /**
     * Returns all jpg & png images from a directory in an array.
     *
     * @param directory                 the directory to start with
     * @param descendIntoSubDirectories should we include sub directories?
     * @return an ArrayList<File> containing all the files or nul if none are found..
     * @throws IOException
     */
    public static ArrayList<File> getAllImageFiles(File directory, boolean descendIntoSubDirectories) throws IOException {
        ArrayList<File> resultList = new ArrayList<File>(256);
        File[] f = directory.listFiles();
        for (File file : f) {
            if (file != null && (file.getName().toLowerCase().endsWith(".jpg") || file.getName().toLowerCase().endsWith(".png")) && !file.getName().startsWith("tn_")) {
                resultList.add(file);
            }
            if (descendIntoSubDirectories && file.isDirectory()) {
                ArrayList<File> tmp = getAllImageFiles(file, true);
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

    /**
     * Puts results into a HTML file.
     * @param prefix
     * @param hits
     * @param queryImage
     * @return
     * @throws IOException
     */
    public static String saveImageResultsToHtml(String prefix, ImageSearchHits hits, String queryImage, IndexReader reader) throws IOException {
        long l = System.currentTimeMillis() / 1000;
        String fileName = "results-" + prefix + "-" + l + ".html";
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        bw.write("<html>\n" +
                "<head><title>Search Results</title></head>\n" +
                "<body bgcolor=\"#FFFFFF\">\n");
        bw.write("<h3>query</h3>\n");
        bw.write("<a href=\"file://" + queryImage + "\"><img src=\"file://" + queryImage + "\"></a><p>\n");
        bw.write("<h3>results</h3>\n");
        for (int i = 0; i < hits.length(); i++) {
            bw.write(hits.score(i) + " - <a href=\"file://" + reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0] + "\"><img src=\"file://" + reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0] + "\"></a><p>\n");
        }
        bw.write("</body>\n" +
                "</html>");
        bw.close();
        return fileName;
    }

    /**
     * Puts results into a HTML file.
     * @param prefix
     * @param hits
     * @param reader
     * @param queryImage
     * @return
     * @throws IOException
     */
    public static String saveImageResultsToHtml(String prefix, TopDocs hits, IndexReader reader, String queryImage) throws IOException {
        long l = System.currentTimeMillis() / 1000;
        String fileName = "results-" + prefix + "-" + l + ".html";
        BufferedWriter bw = new BufferedWriter(new FileWriter(fileName));
        bw.write("<html>\n" +
                "<head><title>Search Results</title></head>\n" +
                "<body bgcolor=\"#FFFFFF\">\n");
        bw.write("<h3>query</h3>\n");
        bw.write("<a href=\"file://" + queryImage + "\"><img src=\"file://" + queryImage + "\"></a><p>\n");
        bw.write("<h3>results</h3>\n");
        for (int i = 0; i < hits.scoreDocs.length; i++) {
            bw.write(hits.scoreDocs[i].score + " - <a href=\"file://" + reader.document(hits.scoreDocs[i].doc).get("descriptorImageIdentifier") + "\"><img src=\"file://" + reader.document(hits.scoreDocs[i].doc).get("descriptorImageIdentifier") + "\"></a><p>\n");
        }
        bw.write("</body>\n" +
                "</html>");
        bw.close();
        return fileName;
    }

    /**
     * Opens a browser windows th<t shows the given URI.
     *
     * @param uri the path to the file to show in the browser window.
     */
    public static void browseUri(String uri) {
        if (!java.awt.Desktop.isDesktopSupported()) {
            System.err.println("Desktop is not supported (fatal)");
            System.exit(1);
        }

        java.awt.Desktop desktop = java.awt.Desktop.getDesktop();

        if (!desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
            System.err.println("Desktop doesn't support the browse action (fatal)");
            System.exit(1);
        }

        try {
            java.net.URI url = new java.net.URI(uri);
            desktop.browse(url);
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }


    public static void saveImageResultsToPng(String prefix, ImageSearchHits hits, String queryImage, IndexReader reader) throws IOException {
        LinkedList<BufferedImage> results = new LinkedList<BufferedImage>();
        int width = 0;
        for (int i = 0; i < hits.length(); i++) {
            // hits.score(i)
            // hits.doc(i).get("descriptorImageIdentifier")
            BufferedImage tmp = ImageIO.read(new FileInputStream(reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]));
//            if (tmp.getHeight() > 200) {
            double factor = 200d / ((double) tmp.getHeight());
            tmp = ImageUtils.scaleImage(tmp, (int) (tmp.getWidth() * factor), 200);
//            }
            width += tmp.getWidth() + 5;
            results.add(tmp);
        }
        BufferedImage result = new BufferedImage(width, 220, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) result.getGraphics();
        g2.setColor(Color.white);
        g2.setBackground(Color.white);
        g2.clearRect(0, 0, result.getWidth(), result.getHeight());
        g2.setColor(Color.black);
        g2.setFont(Font.decode("\"Arial\", Font.BOLD, 12"));
        int offset = 0;
        int count = 0;
        for (Iterator<BufferedImage> iterator = results.iterator(); iterator.hasNext(); ) {
            BufferedImage next = iterator.next();
            g2.drawImage(next, offset, 20, null);
            g2.drawString(hits.score(count) + "", offset + 5, 12);
            offset += next.getWidth() + 5;
            count++;
        }
        ImageIO.write(result, "PNG", new File(prefix + "_" + (System.currentTimeMillis() / 1000) + ".png"));
    }

    public static void saveImageResultsToPng(String prefix, TopDocs hits, String queryImage, IndexReader ir) throws IOException {
        LinkedList<BufferedImage> results = new LinkedList<BufferedImage>();
        int width = 0;
        for (int i = 0; i < Math.min(hits.scoreDocs.length, 10); i++) {
            // hits.score(i)
            // hits.doc(i).get("descriptorImageIdentifier")
            BufferedImage tmp = ImageIO.read(new FileInputStream(ir.document(hits.scoreDocs[i].doc).get("descriptorImageIdentifier")));
            if (tmp.getHeight() > 200) {
                double factor = 200d / ((double) tmp.getHeight());
                tmp = ImageUtils.scaleImage(tmp, (int) (tmp.getWidth() * factor), 200);
            }
            width += tmp.getWidth() + 5;
            results.add(tmp);
        }
        BufferedImage result = new BufferedImage(width, 220, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D) result.getGraphics();
        g2.setColor(Color.black);
        g2.clearRect(0, 0, result.getWidth(), result.getHeight());
        g2.setColor(Color.green);
        g2.setFont(Font.decode("\"Arial\", Font.BOLD, 12"));
        int offset = 0;
        int count = 0;
        for (Iterator<BufferedImage> iterator = results.iterator(); iterator.hasNext(); ) {
            BufferedImage next = iterator.next();
            g2.drawImage(next, offset, 20, null);
            g2.drawString(hits.scoreDocs[count].score + "", offset + 5, 12);
            offset += next.getWidth() + 5;
            count++;
        }
        ImageIO.write(result, "PNG", new File(prefix + "_" + (System.currentTimeMillis() / 1000) + ".png"));
    }

    public static void zipDirectory(File directory, File base, ZipOutputStream zos) throws IOException {
        File[] files = directory.listFiles();
        byte[] buffer = new byte[8192];
        int read = 0;
        for (int i = 0, n = files.length; i < n; i++) {
            if (files[i].isDirectory()) {
                zipDirectory(files[i], base, zos);
            } else {
                FileInputStream in = new FileInputStream(files[i]);
                ZipEntry entry = new ZipEntry(files[i].getPath().substring(base.getPath().length() + 1));
                zos.putNextEntry(entry);
                while (-1 != (read = in.read(buffer))) {
                    zos.write(buffer, 0, read);
                }
                in.close();
            }
        }
    }

    /**
     * Identifies the type of image based on the magic bytes at the beginning of the file.
     * @param file the File to test.
     * @return the file type by enumeration FileTypes.
     * @throws IOException
     */
    @SuppressWarnings("unused")
    private static FileTypes identifyFileType(File file) throws IOException {
        byte[] buffer = new byte[8];
        InputStream in = null;
        try {
            in = new FileInputStream(file);
            in.read(buffer);
            if ((buffer[0] == -119) && (buffer[1] == 0x50) && (buffer[2] == 0x4E) && (buffer[3] == 0x47)) {
                // PNG: 89 50 4E 47 ...
                return FileTypes.PNG;
            } else if ((buffer[0] == 0xFF) && (buffer[1] == 0xD8)) {
                // JPEG image files begin with FF D8 and end with FF D9
                return FileTypes.JPG;
            } else if ((buffer[0] == 0x25) && (buffer[1] == 0x50) && (buffer[2] == 0x44) && (buffer[3] == 0x46)) {
                // PDF 25 50 44 46
                return FileTypes.PDF;
            } else if ((buffer[0] == 0x49) && (buffer[1] == 0x49) && (buffer[2] == 0x2A) && (buffer[3] == 0x00)) {
                // TIFF: 49 49 2A 00 or 4D 4D 00 2A
                return FileTypes.TIF;
            } else if ((buffer[0] == 0x4D) && (buffer[1] == 0x4D) && (buffer[2] == 0x00) && (buffer[3] == 0x2A)) {
                // TIFF: 49 49 2A 00 or 4D 4D 00 2A
                return FileTypes.TIF;
            } else if ((buffer[0] == 0x47) && (buffer[1] == 0x49) && (buffer[2] == 0x46) && (buffer[3] == 0x38)) {
                // GIF: 47 49 46 38 ...
                return FileTypes.GIF;
            } else {
                return FileTypes.UNKNOWN;
            }
        } finally {
            if(in != null) {
                in.close();
            }
        }
    }

    /**
     * Just opens an image with Java and reports if false if there are problems. This method can be used
     * to check for JPG etc. that are not supported by the employed Java version.
     * @param f the file to check.
     * @return true if no exceptions are thrown bey the decoder.
     */
    public static boolean isImageFileCompatible(File f) {
        boolean result = true;
        try {
            BufferedImage img = ImageIO.read(f);
            ColorLayout cl = new ColorLayout();
            cl.extract(img);
        } catch (Exception e) {
            result = false;
        }
        return result;
    }

    public static ArrayList<String> readFileLines(File directory, boolean descendIntoSubDirectories) throws IOException {
        ArrayList<String> resultList = new ArrayList<String>(256);
        String[] extensions = new String[]{"jpg", "JPG", "jpeg", "png", "gif", "tif", "tiff"};

        System.out.print("Getting all images in " + directory.getCanonicalPath() + " " + ((descendIntoSubDirectories) ? "including" : "not including") + " those in subdirectories");
        java.util.List<File> files = (LinkedList<File>) org.apache.commons.io.FileUtils.listFiles(directory, extensions, descendIntoSubDirectories);
        System.out.println(" ~ Found " + files.size() + " images");
        for (File file : files) {
            resultList.add(file.getCanonicalPath());
        }

        return resultList;
    }

    /**
     * Reads a whole file into a StringBuffer based on java.nio
     * @param file the file to open.
     * @param stringBuilder to write the File to.
     * @throws IOException
     */
    public static void readWholeFile(File file, StringBuilder stringBuilder) throws IOException {
        long length =file.length();
        MappedByteBuffer in = new FileInputStream(file).getChannel().map(
                FileChannel.MapMode.READ_ONLY, 0, length);
        int i = 0;
        while (i < length)
            stringBuilder.append((char) in.get(i++));
    }


    /**
     * Reads a whole file into a StringBuffer based on java.nio
     * @param file the file to open.
     * @throws IOException
     */
    public static byte[] readFileToByteArray(File file) throws IOException {
        int length = (int) file.length();
        MappedByteBuffer in = new FileInputStream(file).getChannel().map(
                FileChannel.MapMode.READ_ONLY, 0, length);
        int i = 0;
        byte[] result = new byte[length];
        while (i < length)
            result[i] = in.get(i++);
        return result;
    }


}
