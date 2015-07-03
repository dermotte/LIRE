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
 * Updated: 07.07.13 08:40
 */

package net.semanticmetadata.lire.builders;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;

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
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public interface DocumentBuilder {
    int MAX_IMAGE_DIMENSION = 1024;

    int NUM_OF_THREADS = 16;

    String HASH_FIELD_SUFFIX = "_hash";

    String FIELD_NAME_IDENTIFIER = "ImageIdentifier";

    String FIELD_NAME_AUTOCOLORCORRELOGRAM = "ACC";
    String FIELD_NAME_BINARY_PATTERNS_PYRAMID ="BPP";
    String FIELD_NAME_CEDD = "CEDD";
    String FIELD_NAME_COLORHISTOGRAM = "ColorHist";
    String FIELD_NAME_COLORLAYOUT = "CLD";
    String FIELD_NAME_EDGEHISTOGRAM = "EHD";
    String FIELD_NAME_FCTH = "FCTH";
    String FIELD_NAME_GABOR = "Gabor";
    String FIELD_NAME_JCD = "JCD";
    String FIELD_NAME_JOINT_HISTOGRAM = "JointHist";
    String FIELD_NAME_JPEGCOEFFS = "JpegCoeffs";
    String FIELD_NAME_LOCAL_BINARY_PATTERNS = "LBP";
    String FIELD_NAME_LOCAL_BINARY_PATTERNS_AND_OPPONENT = "LBPOpp";
    String FIELD_NAME_LUMINANCE_LAYOUT = "LuminLayout";
    String FIELD_NAME_OPPONENT_HISTOGRAM = "OppHist";
    String FIELD_NAME_PHOG = "PHOG";
    String FIELD_NAME_Rank_Opponent = "JHROpp";
    String FIELD_NAME_ROTATION_INVARIANT_LOCAL_BINARY_PATTERNS = "RILBP";
    String FIELD_NAME_SCALABLECOLOR = "SCD";
    String FIELD_NAME_TAMURA = "TAMURA";
    String FIELD_NAME_ACCID = "ACCID";


    //    public static final String FIELD_NAME_MSER = "MSER";
    String FIELD_NAME_SURF = "SURF";
    String FIELD_NAME_SIFT = "SIFT";
    String FIELD_NAME_CVSIFT = "CvSIFT";
    String FIELD_NAME_CVOPPSIFT = "CvOppSIFT";
    String FIELD_NAME_CVSURF = "CvSURF";
    //    public static final String FIELD_NAME_CVORB = "CvORB";
    //    public static final String FIELD_NAME_CVBRISK = "CvBRISK";
    String FIELD_NAME_SELF_SIMILARITIES = "SelfSimilarities";
    String FIELD_NAME_SELF_SIMILARITIES_ORIG = "SelfSimilaritiesOrig";


    String FIELD_NAME_SIMPLE = "SIMPLE";


    /**
     * Creates the feature fields for a Lucene Document without creating the document itself.
     *
     * @param image the image to analyze.
     * @return the fields resulting from the analysis.
     */
    Field[] createDescriptorFields(BufferedImage image);

    /**
     * Creates a new Lucene document from a BufferedImage. The identifier can be used like an id
     * (e.g. the file hashFunctionsFileName or the url of the image)
     *
     * @param image the image to index. Cannot be NULL.
     * @param identifier an id for the image, for instance the filename or a URL. Can be NULL.
     * @return a Lucene Document containing the indexed image.
     */
    Document createDocument(BufferedImage image, String identifier) throws FileNotFoundException;

}
