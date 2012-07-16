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
package net.semanticmetadata.lire.indexing.fastmap;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.imageanalysis.*;
import org.apache.lucene.document.Document;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Test class for FastMap. Trying to map some images ...
 * User: Mathias Lux
 * Date: 21.04.2008
 * Time: 11:14:03
 */
public class FastMapTest extends TestCase {
    private String[] testFiles = new String[]{"img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG",
            "img06.JPG", "img07.JPG", "img08.JPG", "error.jpg"};
    //            "img06.JPG", "img07.JPG", "img08.JPG", "img08a.JPG", "error.jpg"};
    private String testFilesPath = "./lire/src/test/resources/images/";
    private String indexPath = "test-index";
    private String testExtensive = "../Caliph/testdata";
    private DocumentBuilder db;
    private LinkedList<Document> docs;


    public void setUp() {
        // creating all descriptors ..
        db = DocumentBuilderFactory.getFullDocumentBuilder();
        docs = new LinkedList<Document>();
        for (String file : testFiles) {
            try {
                docs.add(db.createDocument(new FileInputStream(testFilesPath + file), file));
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
    }

    public void testColorLayoutFastMap() {
        // creating the list of user objects ...
        LinkedList<LireFeature> objs = new LinkedList<LireFeature>();
        for (Iterator<Document> documentIterator = docs.iterator(); documentIterator.hasNext(); ) {
            Document document = documentIterator.next();
            String[] cls = document.getValues(DocumentBuilder.FIELD_NAME_COLORLAYOUT);
            if (cls.length > 0) {
                ColorLayout clt = new ColorLayout();
                clt.setStringRepresentation(cls[0]);
                objs.add(clt);
            }
        }
        System.out.println("--------------- < COLORLAYOUT > ---------------");
        long nano = System.nanoTime();
        createFastMapForObjects(objs, null);
        nano = System.nanoTime() - nano;
        System.out.println("Time taken: ~ " + (nano / (1000 * 1000 * 1000)) + " s");
    }

    public void testIterativeFastMap() throws InstantiationException, IllegalAccessException {
        // creating the list of user objects ...

        // ColorLayout
//        Class descriptor = ColorLayoutImpl.class;
//        String fieldName = DocumentBuilder.FIELD_NAME_COLORLAYOUT;

        // EdgeHistogram
//        Class descriptor = EdgeHistogramImplementation.class;
//        String fieldName = DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM;

        // ScalableColor
//        Class descriptor = ScalableColorImpl.class;
//        String fieldName = DocumentBuilder.FIELD_NAME_SCALABLECOLOR;

        // CEDD
//        Class descriptor = CEDD.class;
//        String fieldName = DocumentBuilder.FIELD_NAME_CEDD;

        // FCTH
//        Class descriptor = FCTH.class;
//        String fieldName = DocumentBuilder.FIELD_NAME_FCTH;

        // Tamura
//        Class descriptor = CEDD.class;
//        String fieldName = DocumentBuilder.FIELD_NAME_CEDD;

        // Gabor
//        Class descriptor = CEDD.class;
//        String fieldName = DocumentBuilder.FIELD_NAME_CEDD;

        // Color histogram
        Class descriptor = SimpleColorHistogram.class;
        String fieldName = DocumentBuilder.FIELD_NAME_COLORHISTOGRAM;

        LinkedList<LireFeature> objs = new LinkedList<LireFeature>();
        for (Iterator<Document> documentIterator = docs.iterator(); documentIterator.hasNext(); ) {
            Document document = documentIterator.next();
            String[] cls = document.getValues(fieldName);
            if (cls.length > 0) {
                LireFeature tempDescriptor = (LireFeature) descriptor.newInstance();
                tempDescriptor.setStringRepresentation(cls[0]);
                objs.add(tempDescriptor);
            }
        }
        // create set of non mapped objects in the first place:
        LinkedList<LireFeature> remainingObj = new LinkedList<LireFeature>();
        remainingObj.add(objs.removeLast());

        System.out.println("--------------- < 1st run of iterative fastmap > ---------------");
        long nano = System.nanoTime();
        // create map and get pivots:
        int[][] p = createFastMapForObjects(objs, null);
        nano = System.nanoTime() - nano;
        System.out.println("---< Time taken: ~ " + (nano / (1000 * 1000 * 1000)) + " s");
        // save pivots:
        SavedPivots sp = new SavedPivots(p, objs);

        System.out.println("--------------- < 2nd run of iterative fastmap > ---------------");
        // first create a set of objects for mapping by adding the pivots:
        int[][] pivots = sp.getPivots(remainingObj, descriptor); // note that the class has to be known.
        p = createFastMapForObjects(remainingObj, pivots);

    }

    public void testScalableColorFastMap() {
        // creating the list of user objects ...
        LinkedList<LireFeature> objs = new LinkedList<LireFeature>();
        for (Iterator<Document> documentIterator = docs.iterator(); documentIterator.hasNext(); ) {
            Document document = documentIterator.next();
            String[] cls = document.getValues(DocumentBuilder.FIELD_NAME_SCALABLECOLOR);
            if (cls.length > 0) {
                ScalableColor sct = new ScalableColor();
                sct.setStringRepresentation(cls[0]);
                objs.add(sct);
            }
        }
        System.out.println("--------------- < ScalableColor > ---------------");
        long nano = System.nanoTime();
        System.out.println("---------< No cache >----------------");
        createFastMapForObjects(objs, null);
        System.out.println("---------< Array cache >----------------");
        createArrayFastMapForObjects(objs, null);
        nano = System.nanoTime() - nano;
        System.out.println("Time taken: ~ " + (nano / (1000 * 1000)) + " ms");
    }

    public void testEdgeHistogramFastMap() {
        // creating the list of user objects ...
        LinkedList<LireFeature> objs = new LinkedList<LireFeature>();
        for (Iterator<Document> documentIterator = docs.iterator(); documentIterator.hasNext(); ) {
            Document document = documentIterator.next();
            String[] cls = document.getValues(DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
            if (cls.length > 0) {
                EdgeHistogram eht = new EdgeHistogram();
                eht.setStringRepresentation(cls[0]);

                objs.add(eht);
            }
        }
        System.out.println("--------------- < EdgeHistogram > ---------------");
        long nano = System.nanoTime();
        createFastMapForObjects(objs, null);
        nano = System.nanoTime() - nano;
        System.out.println("Time taken: ~ " + (nano / (1000 * 1000)) + " ms");
    }

    public void testAutoColorCorrelogramFastMap() {
        // creating the list of user objects ...
        LinkedList<LireFeature> objs = new LinkedList<LireFeature>();
        for (Iterator<Document> documentIterator = docs.iterator(); documentIterator.hasNext(); ) {
            Document document = documentIterator.next();
            String[] cls = document.getValues(DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
            if (cls.length > 0) {
                AutoColorCorrelogram acc = new AutoColorCorrelogram();
                acc.setStringRepresentation(cls[0]);
                objs.add(acc);
            }
        }
        System.out.println("--------------- < AutoColorCorrelogram > ---------------");
        long nano = System.nanoTime();
        createFastMapForObjects(objs, null);
        nano = System.nanoTime() - nano;
        System.out.println("Time taken: ~ " + (nano / (1000 * 1000)) + " ms");
    }

    private int[][] createArrayFastMapForObjects(LinkedList<LireFeature> objs, int[][] savedPivots) {
        ArrayFastmapDistanceMatrix fdm = new ArrayFastmapDistanceMatrix(objs, new FeatureDistanceCalculator());
        // note that fastmap needs at least dimensions*2 objects as it needs enough pivots :)
        FastMap fm;
        if (savedPivots == null) fm = new FastMap(fdm, 3);
        else fm = new FastMap(fdm, 3, savedPivots);
        fm.run();
        for (int i = 0; i < fm.getPoints().length; i++) {
            double[] pts = fm.getPoints()[i];
            System.out.print("Obj " + i + ": ( ");
            for (int j = 0; j < pts.length; j++) {
                System.out.print(pts[j] + " ");
            }
            System.out.println(")");
        }
        int[][] pivots = fm.getPivots();
        for (int i = 0; i < pivots[0].length; i++) {
            System.out.println("dim = " + i);
            System.out.println("p0 = " + objs.get(pivots[0][i]).getStringRepresentation());
            System.out.println("p1 = " + objs.get(pivots[1][i]).getStringRepresentation());
        }
        return fm.getPivots();
    }

    private int[][] createFastMapForObjects(LinkedList<LireFeature> objs, int[][] savedPivots) {
        FastmapDistanceMatrix fdm = new NocacheFastmapDistanceMatrix(objs, new FeatureDistanceCalculator());
        // note that fastmap needs at least dimensions*2 objects as it needs enough pivots :)
        FastMap fm;
        if (savedPivots == null) fm = new FastMap(fdm, 3);
        else fm = new FastMap(fdm, 3, savedPivots);
        fm.run();
        for (int i = 0; i < fm.getPoints().length; i++) {
            double[] pts = fm.getPoints()[i];
            System.out.print("Obj " + i + ": ( ");
            for (int j = 0; j < pts.length; j++) {
                System.out.print(pts[j] + " ");
            }
            System.out.println(")");
        }
        int[][] pivots = fm.getPivots();
        for (int i = 0; i < pivots[0].length; i++) {
            System.out.println("dim = " + i);
            System.out.println("p0 = " + objs.get(pivots[0][i]).getStringRepresentation());
            System.out.println("p1 = " + objs.get(pivots[1][i]).getStringRepresentation());
        }
        return fm.getPivots();
    }

}
