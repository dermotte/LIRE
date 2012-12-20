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

package net.semanticmetadata.lire;

import org.apache.lucene.document.Document;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * <h2>Creating an Index</h2>
 * <p/>
 * Use DocumentBuilderFactory to create a DocumentBuilder, which
 * will create Lucene Documents from images. Add this documents to
 * an index like this:
 * <p/>
 * <pre>
 * System.out.println(">> Indexing " + images.size() + " files.");
 * DocumentBuilder builder = DocumentBuilderFactory.getExtensiveDocumentBuilder();
 * IndexWriter iw = new IndexWriter(indexPath, new SimpleAnalyzer(LuceneUtils.LUCENE_VERSION), true);
 * int count = 0;
 * long time = System.currentTimeMillis();
 * for (String identifier : images) {
 * Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
 * iw.addDocument(doc);
 * count ++;
 * if (count % 25 == 0) System.out.println(count + " files indexed.");
 * }
 * long timeTaken = (System.currentTimeMillis() - time);
 * float sec = ((float) timeTaken) / 1000f;
 *
 * System.out.println(sec + " seconds taken, " + (timeTaken / count) + " ms per image.");
 * iw.optimize();
 * iw.close();
 * </pre>
 * <p/>
 * <p/>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 31.01.2006
 * <br>Time: 23:02:00
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface DocumentBuilder {
    public static final int MAX_IMAGE_SIDE_LENGTH = 800;

    public static final String FIELD_NAME_SCALABLECOLOR = "descriptorScalableColor";
    public static final String FIELD_NAME_COLORLAYOUT = "descriptorColorLayout";
    public static final String FIELD_NAME_EDGEHISTOGRAM = "descriptorEdgeHistogram";
    public static final String FIELD_NAME_AUTOCOLORCORRELOGRAM = "featureAutoColorCorrelogram";
    public static final String FIELD_NAME_COLORHISTOGRAM = "featureColorHistogram";
    public static final String FIELD_NAME_CEDD = "featureCEDD";
    public static final String FIELD_NAME_FCTH = "featureFCTH";
    public static final String FIELD_NAME_JCD = "featureJCD";
    public static final String FIELD_NAME_TAMURA = "featureTAMURA";
    public static final String FIELD_NAME_GABOR = "featureGabor";
    public static final String FIELD_NAME_SIFT = "featureSift";
    public static final String FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM = "featureSiftHistogram";
    public static final String FIELD_NAME_SIFT_VISUAL_WORDS = "featureSiftHistogramVWords";
    public static final String FIELD_NAME_IDENTIFIER = "descriptorImageIdentifier";
    public static final String FIELD_NAME_CEDD_FAST = "featureCEDDfast";
    public static final String FIELD_NAME_COLORLAYOUT_FAST = "featureColorLayoutfast";
    public static final String FIELD_NAME_SURF = "featureSurf";
    public static final String FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM = "featureSURFHistogram";
    public static final String FIELD_NAME_SURF_VISUAL_WORDS = "featureSurfHistogramVWords";
    public static final String FIELD_NAME_MSER_LOCAL_FEATURE_HISTOGRAM = "featureMSERHistogram";
    public static final String FIELD_NAME_MSER_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS = "featureMSERHistogramVWords";
    public static final String FIELD_NAME_MSER = "featureMSER";
    public static final String FIELD_NAME_BASIC_FEATURES = "featureBasic";
    public static final String FIELD_NAME_JPEGCOEFFS = "featureJpegCoeffs";
    public static final String FIELD_NAME_JOINT_HISTOGRAM = "featureJointHist";
    public static final String  FIELD_NAME_OPPONENT_HISTOGRAM = "featOpHist";


    /**
     * Creates a new Lucene document from a BufferedImage. The identifier can be used like an id
     * (e.g. the file name or the url of the image)
     *
     * @param image      the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or an URL. Can be NULL.
     * @return a Lucene Document containing the indexed image.
     */
    public Document createDocument(BufferedImage image, String identifier) throws FileNotFoundException;

    /**
     * Creates a new Lucene document from an InputStream. The identifier can be used like an id
     * (e.g. the file name or the url of the image)
     *
     * @param image      the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or an URL. Can be NULL.
     * @return a Lucene Document containing the indexed image.
     * @throws IOException in case the image cannot be retrieved from the InputStream
     */
    public Document createDocument(InputStream image, String identifier) throws IOException;

}
