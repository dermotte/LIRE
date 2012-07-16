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

import net.semanticmetadata.lire.imageanalysis.*;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.CorrelogramDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericDocumentBuilder;
import net.semanticmetadata.lire.impl.GenericFastDocumentBuilder;

/**
 * Use DocumentBuilderFactory to create a DocumentBuilder, which
 * will create Lucene Documents from images.  <br/>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 31.01.2006
 * <br>Time: 23:00:32
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class DocumentBuilderFactory {
    /**
     * Creates a simple version of a DocumentBuilder. In this case the
     * {@link net.semanticmetadata.lire.imageanalysis.CEDD} is used as a feature
     *
     * @return a simple and efficient DocumentBuilder.
     * @see net.semanticmetadata.lire.imageanalysis.CEDD
     */
    public static DocumentBuilder getDefaultDocumentBuilder() {
        return new GenericFastDocumentBuilder(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
    }

    /**
     * Creates a simple version of a DocumentBuilder using the MPEG/-7 visual features features
     * all available descriptors are used.
     *
     * @return a fully featured DocumentBuilder.
     * @see net.semanticmetadata.lire.imageanalysis.ColorLayout
     * @see net.semanticmetadata.lire.imageanalysis.EdgeHistogram
     * @see net.semanticmetadata.lire.imageanalysis.ScalableColor
     * @deprecated Use ChainedDocumentBuilder instead
     */
    public static DocumentBuilder getExtensiveDocumentBuilder() {
        ChainedDocumentBuilder cb = new ChainedDocumentBuilder();
        cb.addBuilder(DocumentBuilderFactory.getColorLayoutBuilder());
        cb.addBuilder(DocumentBuilderFactory.getEdgeHistogramBuilder());
        cb.addBuilder(DocumentBuilderFactory.getScalableColorBuilder());
        return cb;
    }

    /**
     * Creates a fast (byte[] based) version of the MPEG-7 ColorLayout document builder.
     *
     * @return the document builder.
     */
    public static DocumentBuilder getColorLayoutBuilder() {
        return new GenericFastDocumentBuilder(ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT);
    }

    /**
     * Creates a fast (byte[] based) version of the MPEG-7 EdgeHistogram document builder.
     *
     * @return the document builder.
     */
    public static DocumentBuilder getEdgeHistogramBuilder() {
        return new GenericFastDocumentBuilder(EdgeHistogram.class, DocumentBuilder.FIELD_NAME_EDGEHISTOGRAM);
    }

    /**
     * Creates a fast (byte[] based) version of the MPEG-7 ColorLayout document builder.
     *
     * @return the document builder.
     */
    public static DocumentBuilder getScalableColorBuilder() {
        return new GenericFastDocumentBuilder(ScalableColor.class, DocumentBuilder.FIELD_NAME_SCALABLECOLOR);
    }

    /**
     * Creates a simple version of a DocumentBuilder using ScalableColor.
     *
     * @return a fully featured DocumentBuilder.
     * @see net.semanticmetadata.lire.imageanalysis.ScalableColor
     * @deprecated Use ColorHistogram and the respective factory methods to get it instead
     */
    public static DocumentBuilder getColorOnlyDocumentBuilder() {
        return DocumentBuilderFactory.getScalableColorBuilder();
    }

    /**
     * Creates a simple version of a DocumentBuilder using the ColorLayout feature. Don't use this method any more but
     * use the respective feature bound method instead.
     *
     * @return a simple and fast DocumentBuilder.
     * @see net.semanticmetadata.lire.imageanalysis.ColorLayout
     * @deprecated use MPEG-7 feature ColorLayout or CEDD, which are both really fast.
     */
    public static DocumentBuilder getFastDocumentBuilder() {
        return DocumentBuilderFactory.getColorLayoutBuilder();
    }

    /**
     * Creates a DocumentBuilder for the AutoColorCorrelation feature. Note that the extraction of this feature
     * is especially slow! So use it only on small images! Images that do not fit in a 200x200 pixel box are
     * resized by the document builder to ensure shorter processing time. See
     * {@link net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram} for more information on the image feature.
     * Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @return the created AutoCorrelation feature DocumentBuilder.
     */
    public static DocumentBuilder getAutoColorCorrelogramDocumentBuilder() {
        return new GenericDocumentBuilder(AutoColorCorrelogram.class, DocumentBuilder.FIELD_NAME_AUTOCOLORCORRELOGRAM, GenericDocumentBuilder.Mode.Fast);
    }

    /**
     * Creates a DocumentBuilder for the AutoColorCorrelation feature. Note that the extraction of this feature
     * is especially slow, but this is a more fast, but less accurate settings version!
     * Images that do not fit in a defined bounding box they are
     * resized by the document builder to ensure shorter processing time. See
     * {@link net.semanticmetadata.lire.imageanalysis.AutoColorCorrelogram} for more information on the image feature.
     * Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @return the created AutoCorrelation feature DocumentBuilder.
     * @deprecated Use #getAutoColorCorrelogramDocumentBuilder instead.
     */
    public static DocumentBuilder getFastAutoColorCorrelationDocumentBuilder() {
        return new CorrelogramDocumentBuilder(AutoColorCorrelogram.Mode.SuperFast);
    }

    /**
     * Creates a DocumentBuilder for the CEDD feature. See
     * {@link net.semanticmetadata.lire.imageanalysis.CEDD} for more information on the image feature.
     * Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @return the created CEDD feature DocumentBuilder.
     */
    public static DocumentBuilder getCEDDDocumentBuilder() {
//        return new CEDDDocumentBuilder();
        return new GenericFastDocumentBuilder(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
    }


    /**
     * Creates a DocumentBuilder for the FCTH feature. See
     * {@link net.semanticmetadata.lire.imageanalysis.FCTH} for more information on the image feature.
     * Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @return the created FCTH feature DocumentBuilder.
     */
    public static DocumentBuilder getFCTHDocumentBuilder() {
        return new GenericDocumentBuilder(FCTH.class, DocumentBuilder.FIELD_NAME_FCTH, GenericDocumentBuilder.Mode.Fast);
    }

    /**
     * Creates a DocumentBuilder for the JCD feature. See
     * {@link net.semanticmetadata.lire.imageanalysis.JCD} for more information on the image feature.
     * Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @return the created DocumentBuilder
     */
    public static DocumentBuilder getJCDDocumentBuilder() {
        return new GenericFastDocumentBuilder(JCD.class, DocumentBuilder.FIELD_NAME_JCD);
    }

    /**
     * Creates a DocumentBuilder for the JpegCoefficientHistogram feature. See
     * {@link net.semanticmetadata.lire.imageanalysis.JpegCoefficientHistogram} for more
     * information on the image feature.
     * Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @return the created DocumentBuilder
     */
    public static DocumentBuilder getJpegCoefficientHistogramDocumentBuilder() {
        return new GenericDocumentBuilder(JpegCoefficientHistogram.class, DocumentBuilder.FIELD_NAME_JPEGCOEFFS, GenericDocumentBuilder.Mode.Fast);
    }

    /**
     * Creates a DocumentBuilder for simple RGB color histograms. See
     * {@link net.semanticmetadata.lire.imageanalysis.SimpleColorHistogram} for more
     * information on the image feature.
     * Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @return the created feature DocumentBuilder.
     */
    public static DocumentBuilder getColorHistogramDocumentBuilder() {
        return new GenericDocumentBuilder(SimpleColorHistogram.class, DocumentBuilder.FIELD_NAME_COLORHISTOGRAM, GenericDocumentBuilder.Mode.Fast);
    }

    /**
     * Creates a DocumentBuilder for three Tamura features. See
     * {@link net.semanticmetadata.lire.imageanalysis.Tamura} for more
     * information on the image feature.
     * Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @return the created Tamura feature DocumentBuilder.
     */
    public static DocumentBuilder getTamuraDocumentBuilder() {
        return new GenericFastDocumentBuilder(Tamura.class, DocumentBuilder.FIELD_NAME_TAMURA);
    }

    /**
     * Creates a DocumentBuilder for the Gabor feature. See
     * {@link net.semanticmetadata.lire.imageanalysis.Gabor} for more
     * information on the image feature.
     * Be sure to use the same options for the ImageSearcher as you used for the DocumentBuilder.
     *
     * @return the created Tamura feature DocumentBuilder.
     */
    public static DocumentBuilder getGaborDocumentBuilder() {
        return new GenericFastDocumentBuilder(Gabor.class, DocumentBuilder.FIELD_NAME_GABOR);
    }


    /**
     * Creates and returns a DocumentBuilder, which contains all available features. For
     * AutoColorCorrelogram the getAutoColorCorrelogramDocumentBuilder() is used. Therefore
     * it is compatible with the respective Searcher.
     *
     * @return a combination of all available features.
     */
    public static DocumentBuilder getFullDocumentBuilder() {
        ChainedDocumentBuilder cdb = new ChainedDocumentBuilder();
        cdb.addBuilder(DocumentBuilderFactory.getExtensiveDocumentBuilder());
        cdb.addBuilder(DocumentBuilderFactory.getAutoColorCorrelogramDocumentBuilder());
        cdb.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());
        cdb.addBuilder(DocumentBuilderFactory.getFCTHDocumentBuilder());
        cdb.addBuilder(DocumentBuilderFactory.getColorHistogramDocumentBuilder());
        cdb.addBuilder(DocumentBuilderFactory.getTamuraDocumentBuilder());
        cdb.addBuilder(DocumentBuilderFactory.getGaborDocumentBuilder());
        return cdb;
    }
}
