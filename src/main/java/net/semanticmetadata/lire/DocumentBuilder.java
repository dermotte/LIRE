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
 * Updated: 07.07.13 08:40
 */

package net.semanticmetadata.lire;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

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

    public static final String FIELD_NAME_AUTOCOLORCORRELOGRAM = "featureAutoColorCorrelogram";
    public static final String FIELD_NAME_BINARY_PATTERNS_PYRAMID ="feat_bpp";
    public static final String FIELD_NAME_CEDD = "featureCEDD";
    public static final String FIELD_NAME_COLORHISTOGRAM = "featureColorHistogram";
    public static final String FIELD_NAME_COLORLAYOUT = "descriptorColorLayout";
    public static final String FIELD_NAME_EDGEHISTOGRAM = "descriptorEdgeHistogram";
    public static final String FIELD_NAME_FCTH = "featureFCTH";
    public static final String FIELD_NAME_GABOR = "featureGabor";
    public static final String FIELD_NAME_IDENTIFIER = "descriptorImageIdentifier";
    public static final String FIELD_NAME_JCD = "featureJCD";
    public static final String FIELD_NAME_JOINT_HISTOGRAM = "featureJointHist";
    public static final String FIELD_NAME_JPEGCOEFFS = "featureJpegCoeffs";
    public static final String FIELD_NAME_LOCAL_BINARY_PATTERNS = "featLBP";
    public static final String FIELD_NAME_LUMINANCE_LAYOUT = "featLumLay";
    public static final String FIELD_NAME_MSER = "featureMSER";
//    public static final String FIELD_NAME_MSER_LOCAL_FEATURE_HISTOGRAM = "featureMSERHistogram";
//    public static final String FIELD_NAME_MSER_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS = "featureMSERHistogramVWords";
    public static final String FIELD_NAME_OPPONENT_HISTOGRAM = "featOpHist";
    public static final String FIELD_NAME_PHOG = "featPHOG";
    public static final String FIELD_NAME_ROTATION_INVARIANT_LOCAL_BINARY_PATTERNS = "featRILBP";
    public static final String FIELD_NAME_SCALABLECOLOR = "descriptorScalableColor";
    public static final String FIELD_NAME_SIFT = "featureSift";
//    public static final String FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM = "featureSiftHistogram";
//    public static final String FIELD_NAME_SIFT_VISUAL_WORDS = "featureSiftHistogramVWords";
    public static final String FIELD_NAME_CVSIFT = "featureCvSift";
    public static final String FIELD_NAME_SURF = "featureSurf";
//    public static final String FIELD_NAME_SURF_LOCAL_FEATURE_HISTOGRAM = "featureSurfHistogram";
//    public static final String FIELD_NAME_SURF_VISUAL_WORDS = "featureSurfHistogramVWords";
//    public static final String FIELD_NAME_SURF_VLAD = "featSurfVlad";
    public static final String FIELD_NAME_CVSURF = "featureCvSurf";
    public static final String FIELD_NAME_CVOPPSIFT = "featureCvOppSift";
    public static final String FIELD_NAME_CVORB = "featureCvOrb";
    public static final String FIELD_NAME_CVBRISK = "featureCvBrisk";
    public static final String FIELD_NAME_TAMURA = "featureTAMURA";
    public static final String FIELD_NAME_SELF_SIMILARITIES = "featureSelfSimilarities";


    public static final String FIELD_NAME_SIMPLE = "SIMPLE";
    public static final String FIELD_NAME_BOVW = "BOVW";
    public static final String FIELD_NAME_BOVW_VECTOR = "BOVWVec";
    public static final String FIELD_NAME_VLAD = "VLAD";
    public static final String FIELD_NAME_VLAD_VECTOR = "VLADVec";



    /**
     * Creates the feature fields for a Lucene Document without creating the document itself.
     *
     * @param image the image to analyze.
     * @return the fields resulting from the analysis.
     */
    public Field[] createDescriptorFields(BufferedImage image);

    /**
     * Creates a new Lucene document from a BufferedImage. The identifier can be used like an id
     * (e.g. the file hashFunctionsFileName or the url of the image)
     *
     * @param image      the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or an URL. Can be NULL.
     * @return a Lucene Document containing the indexed image.
     */
    public Document createDocument(BufferedImage image, String identifier) throws FileNotFoundException;

    /**
     * Creates a new Lucene document from an InputStream. The identifier can be used like an id
     * (e.g. the file hashFunctionsFileName or the url of the image)
     *
     * @param image      the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or an URL. Can be NULL.
     * @return a Lucene Document containing the indexed image.
     * @throws IOException in case the image cannot be retrieved from the InputStream
     */
    public Document createDocument(InputStream image, String identifier) throws IOException;

}
