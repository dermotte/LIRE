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
 * Updated: 23.06.13 18:16
 */

package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.imageanalysis.ColorLayout;
import net.semanticmetadata.lire.imageanalysis.PHOG;
import net.semanticmetadata.lire.imageanalysis.RotationInvariantLocalBinaryPatterns;
import net.semanticmetadata.lire.imageanalysis.spatialpyramid.SPLBP;
import net.semanticmetadata.lire.impl.BitSamplingImageSearcher;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.indexing.hashing.LocalitySensitiveHashing;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import net.semanticmetadata.lire.indexing.tools.HashingIndexor;
import net.semanticmetadata.lire.utils.ImageUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * This file is part of LIRE, a Java library for content based image retrieval.
 *
 * @author Mathias Lux, mathias@juggle.at, 24.04.13
 */

public class TestGroundTruth extends TestCase {
    public String indexPath = "test-idx-large-hashed";
    private File fileList;
//    private File truth = new File("E:\\Eval-WIPO\\filtered.txt");
    private File truth = new File("C:\\Temp\\Eval-WIPO\\truth\\trimmed.txt");

    public void testAll() throws IOException {
        testIndexing();
        testSearchBenchmark();
    }

    public void testIndexing() {
//        fileList = new File("E:\\Eval-WIPO\\ca.txt");
        fileList = new File("C:\\Temp\\Eval-WIPO\\ca.txt");
        ParallelIndexer pin = new ParallelIndexer(3, indexPath, fileList, true) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
//                builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getLuminanceLayoutDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());

                builder.addBuilder(new GenericDocumentBuilder(ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT, true));
//                builder.addBuilder(new GenericDocumentBuilder(PHOG.class, DocumentBuilder.FIELD_NAME_PHOG, true));
//                builder.addBuilder(new GenericDocumentBuilder(JCD.class, DocumentBuilder.FIELD_NAME_JCD, true));
//                builder.addBuilder(new GenericDocumentBuilder(EdgeHistogram.class, DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM, true));
//                builder.addBuilder(new GenericDocumentBuilder(LuminanceLayout.class, DocumentBuilder.FIELD_NAME_LUMINANCE_LAYOUT, true));

                builder.addBuilder(new GenericDocumentBuilder(RotationInvariantLocalBinaryPatterns.class, "lbp", true));
//                builder.addBuilder(new GenericDocumentBuilder(SPCEDD.class, "spcedd", true));
            }
        };
        pin.run();
        pin = new ParallelIndexer(3, indexPath, truth, false) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
//                builder.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getPHOGDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getJCDDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getJointHistogramDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getLuminanceLayoutDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
//                builder.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
                builder.addBuilder(new GenericDocumentBuilder(ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT, true));
//                builder.addBuilder(new GenericDocumentBuilder(PHOG.class, DocumentBuilder.FIELD_NAME_PHOG, true));
//                builder.addBuilder(new GenericDocumentBuilder(JCD.class, DocumentBuilder.FIELD_NAME_JCD, true));
//                builder.addBuilder(new GenericDocumentBuilder(EdgeHistogram.class, DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM, true));
//                builder.addBuilder(new GenericDocumentBuilder(LuminanceLayout.class, DocumentBuilder.FIELD_NAME_LUMINANCE_LAYOUT, true));

                builder.addBuilder(new GenericDocumentBuilder(SPLBP.class, "lbp", true));
//                builder.addBuilder(new GenericDocumentBuilder(SPCEDD.class, "spcedd", true));
            }
        };
        pin.run();
    }

    public void testSearchBenchmark() throws IOException {
        IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(new File(indexPath)), IOContext.READONCE));
        System.out.println("Precision @ 20 (linear):");
//        System.out.println("CEDD        " + getPrecision(ImageSearcherFactory.createCEDDImageSearcher(30), reader));
        System.out.println("ColorLayout " + getPrecision(ImageSearcherFactory.createColorLayoutImageSearcher(30), reader));
//        System.out.println("SPCEDD      " + getPrecision(new GenericFastImageSearcher(30, SPCEDD.class, "spcedd"), reader));
//        System.out.println("PHOG        " + getPrecision(ImageSearcherFactory.createPHOGImageSearcher(30), reader));
//        System.out.println("FCTH        " + getPrecision(ImageSearcherFactory.createFCTHImageSearcher(30), reader));
//        System.out.println("JCD         " + getPrecision(ImageSearcherFactory.createJCDImageSearcher(30), reader));
//        System.out.println("EdgeHist    " + getPrecision(ImageSearcherFactory.createEdgeHistogramImageSearcher(30), reader));
//        System.out.println("JointHist   " + getPrecision(ImageSearcherFactory.createJointHistogramImageSearcher(30), reader));
//        System.out.println("Lum.Lay.    " + getPrecision(ImageSearcherFactory.createLuminanceLayoutImageSearcher(30), reader));
//        System.out.println("OpponentHis " + getPrecision(ImageSearcherFactory.createOpponentHistogramSearcher(30), reader));
//        System.out.println("ColorHist   " + getPrecision(ImageSearcherFactory.createColorHistogramImageSearcher(30), reader));
        System.out.println("LBP      " + getPrecision(new GenericFastImageSearcher(30, SPLBP.class, "lbp"), reader));


//        System.out.println("Precision @ 20 (hashed):");
//        System.out.println("ColorLayout " + getPrecision(new BitSamplingImageSearcher(30, DocumentBuilder.FIELD_NAME_COLORLAYOUT,
//                DocumentBuilder.FIELD_NAME_COLORLAYOUT + "_hash", new ColorLayout(), 500), reader));
//        System.out.println("SPCEDD      " + getPrecision(new BitSamplingImageSearcher(30, "spcedd",
//                "spcedd_hash", new SPCEDD(), 500), reader));
//        System.out.println("PHOG        " + getPrecision(new BitSamplingImageSearcher(30, DocumentBuilder.FIELD_NAME_PHOG,
//                DocumentBuilder.FIELD_NAME_PHOG + "_hash", new PHOG(), 2000), reader));
//        System.out.println("JCD         " + getPrecision(new BitSamplingImageSearcher(30, DocumentBuilder.FIELD_NAME_JCD,
//                DocumentBuilder.FIELD_NAME_JCD + "_hash", new JCD(), 2000), reader));
//        System.out.println("EdgeHist    " + getPrecision(new BitSamplingImageSearcher(30, DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM,
//                DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM + "_hash", new EdgeHistogram(), 2000), reader));
//        System.out.println("Lum.Lay.    " + getPrecision(new BitSamplingImageSearcher(30, DocumentBuilder.FIELD_NAME_LUMINANCE_LAYOUT,
//                DocumentBuilder.FIELD_NAME_LUMINANCE_LAYOUT + "_hash", new LuminanceLayout(), 2000), reader));
    }

    /**
     * Precision @ 20 ...
     *
     * @param is
     * @param reader
     * @return
     * @throws IOException
     */
    private double getPrecision(ImageSearcher is, IndexReader reader) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(truth));
        String line = null;
        StringBuilder sb = new StringBuilder(128);
        double count = 0, sum = 0;
        while ((line = br.readLine()) != null) {
            BufferedImage bimg = ImageIO.read(new File(line));
            ImageSearchHits hits = is.search(bimg, reader);
            saveImageResultsToPng("result", hits, line);
            for (int i = 0; i < Math.min(20, hits.length()); i++) {
                Document d = hits.doc(i);
                String fileName = d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                if (fileName.contains("swoosh")) {
                    sum += 1;
                    sb.append('*');
                } else sb.append('.');
            }
            sb.append(" - " + line);
//            System.out.println(sb);
            sb.delete(0, sb.length());
            count++;
        }
        return sum / (20d * count);
    }

    public static void saveImageResultsToPng(String prefix, ImageSearchHits hits, String queryImage) throws IOException {
        LinkedList<BufferedImage> results = new LinkedList<BufferedImage>();
        LinkedList<Boolean> isHit = new LinkedList<Boolean>();
        int width = 0;
        for (int i = 0; i < hits.length(); i++) {
            // hits.score(i)
            String name = hits.doc(i).get("descriptorImageIdentifier");
            BufferedImage tmp = ImageIO.read(new FileInputStream(hits.doc(i).get("descriptorImageIdentifier")));
//            if (tmp.getHeight() > 200) {
            double factor = 200d / ((double) tmp.getHeight());
            tmp = ImageUtils.scaleImage(tmp, (int) (tmp.getWidth() * factor), 200);
//            }
            width += tmp.getWidth() + 5;
            results.add(tmp);
            isHit.add(name.contains("swoosh"));
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
        Iterator<Boolean> it = isHit.iterator();
        for (Iterator<BufferedImage> iterator = results.iterator(); iterator.hasNext(); ) {
            BufferedImage next = iterator.next();
            g2.drawImage(next, offset, 20, null);
            g2.setColor(Color.black);
            g2.drawString(hits.score(count) + "", offset + 5, 12);
            if (it.next()) {
                g2.setColor(Color.green);
                g2.fillRect(offset, 20, next.getWidth(), 20);
            }
            offset += next.getWidth() + 5;
            count++;
        }
        ImageIO.write(result, "PNG", new File(prefix + "_" + (System.currentTimeMillis() / 1000) + ".png"));
    }

    public void testBitSampling() throws IllegalAccessException, IOException, InstantiationException {
//        double w = 4;
//        for (int i=500; i <= 2000; i+=250) {
//            for (int runs = 0; runs<100; runs++) {
                File f = new File(BitSampling.hashFunctionsFileName);
                if (f.exists()) f.delete();
//                BitSampling.setNumFunctionBundles(80);
                BitSampling.generateHashFunctions();
                LocalitySensitiveHashing.generateHashFunctions();
                String[] args = new String[]{"-c", "C:\\Temp\\Eval-WIPO\\2index.txt", "-l", indexPath};
                HashingIndexor.main(args);
                IndexReader reader = DirectoryReader.open(new RAMDirectory(FSDirectory.open(new File(indexPath)), IOContext.READONCE));
                double precision = getPrecision(new BitSamplingImageSearcher(30, DocumentBuilder.FIELD_NAME_PHOG,
                        DocumentBuilder.FIELD_NAME_PHOG + "_hash", new PHOG(), new FileInputStream(BitSampling.hashFunctionsFileName), 500), reader);
                System.out.println(precision);
                f = new File(BitSampling.hashFunctionsFileName);
//                f.renameTo(new File(runs + "_" + precision + "_" + BitSampling.hashFunctionsFileName));
//            }
//        }
    }
}
