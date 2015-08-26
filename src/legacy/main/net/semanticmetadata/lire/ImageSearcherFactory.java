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
 * Updated: 01.07.13 16:15
 */

package net.semanticmetadata.lire;

import net.semanticmetadata.lire.imageanalysis.joint.JointHistogram;
import net.semanticmetadata.lire.impl.BitSamplingImageSearcher;
import net.semanticmetadata.lire.impl.GenericFastImageSearcher;

/**
 * <h2>Searching in an Index</h2>
 * Use the ImageSearcherFactory for creating an ImageSearcher, which will retrieve the images
 * for you from the index.
 * <p/>
 * <pre>
 * IndexReader reader = IndexReader.open(indexPath);
 * ImageSearcher searcher = ImageSearcherFactory.createDefaultSearcher();
 * FileInputStream imageStream = new FileInputStream("image.jpg");
 * BufferedImage bimg = ImageIO.read(imageStream);
 * // searching for an image:
 * ImageSearchHits hits = null;
 * hits = searcher.search(bimg, reader);
 * for (int i = 0; i < 5; i++) {
 * System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
 * }
 *
 * // searching for a document:
 * Document document = hits.doc(0);
 * hits = searcher.search(document, reader);
 * for (int i = 0; i < 5; i++) {
 * System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
 * }
 * </pre>
 * <p/>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 03.02.2006
 * <br>Time: 00:30:07
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class ImageSearcherFactory {
    /**
     * Default number of maximum hits.
     */
    public static int NUM_MAX_HITS = 100;

    /**
     * Creates a new simple image searcher with the desired number of maximum hits.
     *
     * @param maximumHits
     * @return the searcher instance
     * @deprecated Use ColorLayout, EdgeHistogram and ScalableColor features instead.
     */
    public static ImageSearcher createSimpleSearcher(int maximumHits) {
        return ImageSearcherFactory.createColorLayoutImageSearcher(maximumHits);
    }

    /**
     * Returns a new default ImageSearcher with a predefined number of maximum
     * hits defined in the {@link ImageSearcherFactory#NUM_MAX_HITS} based on the {@link net.semanticmetadata.lire.imageanalysis.CEDD} feature
     *
     * @return the searcher instance
     */
    public static ImageSearcher createDefaultSearcher() {
        return new GenericFastImageSearcher(NUM_MAX_HITS, CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
    }

    /**
     * Returns a new ImageSearcher with the given number of maximum hits
     * which only takes the overall color into account. texture and color
     * distribution are ignored.
     *
     * @param maximumHits defining how many hits are returned in max (e.g. 100 would be ok)
     * @return the ImageSearcher
     * @see ImageSearcher
     * @deprecated Use ColorHistogram or ScalableColor instead
     */
    public static ImageSearcher createColorOnlySearcher(int maximumHits) {
        return ImageSearcherFactory.createScalableColorImageSearcher(maximumHits);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits number of hits returned.
     * @return
     */
    public static ImageSearcher createAutoColorCorrelogramImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, AutoColorCorrelogram.class, DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.CEDD}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createCEDDImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.CEDD}
     * image feature based on {@link net.semanticmetadata.lire.indexing.hashing.BitSampling} hashes.
     * Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     * It won't work out if you don't use {@link DocumentBuilderFactory#getHashingCEDDDocumentBuilder()}
     * or the code within.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createHashingCEDDImageSearcher(int maximumHits) {
        return new BitSamplingImageSearcher(maximumHits, DocumentBuilder.FIELD_NAME_CEDD,
                DocumentBuilder.FIELD_NAME_CEDD+"_hash", new CEDD());
    }


    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.FCTH}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createFCTHImageSearcher(int maximumHits) {
//        return new GenericImageSearcher(maximumHits, FCTH.class, DocumentBuilder.FIELD_NAME_FCTH);
        return new GenericFastImageSearcher(maximumHits, FCTH.class, DocumentBuilder.FIELD_NAME_FCTH);
    }


    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.JCD}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createJCDImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, JCD.class, DocumentBuilder.FIELD_NAME_JCD);
    }


    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.JpegCoefficientHistogram}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createJpegCoefficientHistogramImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, JpegCoefficientHistogram.class, DocumentBuilder.FIELD_NAME_JPEGCOEFFS);
    }


    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createColorHistogramImageSearcher(int maximumHits) {
//        return new GenericImageSearcher(maximumHits, SimpleColorHistogram.class, DocumentBuilder.FIELD_NAME_COLORHISTOGRAM);
        return new GenericFastImageSearcher(maximumHits, SimpleColorHistogram.class, DocumentBuilder.FIELD_NAME_COLORHISTOGRAM);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.Tamura}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createTamuraImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, Tamura.class, DocumentBuilder.FIELD_NAME_TAMURA);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.Gabor}
     * image feature. Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createGaborImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, Gabor.class, DocumentBuilder.FIELD_NAME_GABOR);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.ColorLayout}
     * image feature using the byte[] serialization. Be sure to use the same options for the ImageSearcher as
     * you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createColorLayoutImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.ScalableColor}
     * image feature using the byte[] serialization. Be sure to use the same options for the ImageSearcher as
     * you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createScalableColorImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, ScalableColor.class, DocumentBuilder.FIELD_NAME_SCALABLECOLOR);
    }

    /**
     * Create and return an ImageSearcher for the {@link net.semanticmetadata.lire.imageanalysis.EdgeHistogram}
     * image feature using the byte[] serialization. Be sure to use the same options for the ImageSearcher as
     * you used for the DocumentBuilder.
     *
     * @param maximumHits
     * @return
     */
    public static ImageSearcher createEdgeHistogramImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, EdgeHistogram.class, DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
    }


    /**
     * Creates a new ImageSearcher instance based on the class {@link net.semanticmetadata.lire.imageanalysis.joint.JointHistogram}.
     *
     * @param maximumHits
     * @return a new searcher instance
     * @see net.semanticmetadata.lire.imageanalysis.joint.JointHistogram
     */
    public static ImageSearcher createJointHistogramImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, JointHistogram.class, DocumentBuilder.FIELD_NAME_JOINT_HISTOGRAM);
    }

    /**
     * Creates a new ImageSearcher instance based on the class {@link net.semanticmetadata.lire.imageanalysis.OpponentHistogram}
     * @param maximumHits
     * @return a new searcher instance
     * @see net.semanticmetadata.lire.imageanalysis.OpponentHistogram
     */
    public static ImageSearcher createOpponentHistogramSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, OpponentHistogram.class, DocumentBuilder.FIELD_NAME_OPPONENT_HISTOGRAM);
    }

    /**
     * Creates a new ImageSearcher instance based on the class {@link net.semanticmetadata.lire.imageanalysis.LuminanceLayout}
     * @param maximumHits
     * @return a new searcher instance
     * @see net.semanticmetadata.lire.imageanalysis.LuminanceLayout
     */
    public static ImageSearcher createLuminanceLayoutImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, LuminanceLayout.class, DocumentBuilder.FIELD_NAME_LUMINANCE_LAYOUT);
    }

    /**
     * Creates a new ImageSearcher instance based on the class {@link net.semanticmetadata.lire.imageanalysis.PHOG}
     * @param maximumHits
     * @return a new searcher instance
     * @see net.semanticmetadata.lire.imageanalysis.PHOG
     */
    public static ImageSearcher createPHOGImageSearcher(int maximumHits) {
        return new GenericFastImageSearcher(maximumHits, PHOG.class, DocumentBuilder.FIELD_NAME_PHOG);
    }

    /**
     * Checks if the weight is in [0,1]
     *
     * @param f the weight to check
     * @return true if the weight is in [0,1], false otherwise
     */
    @SuppressWarnings("unused")
	private static boolean isAppropriateWeight(float f) {
        boolean result = false;
        if (f <= 1f && f >= 0) result = true;
        return result;

    }
}
