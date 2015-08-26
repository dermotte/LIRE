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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.filters;


import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.searchers.ImageSearchHits;
import net.semanticmetadata.lire.searchers.SearchHitsFilter;
import net.semanticmetadata.lire.searchers.SimpleImageSearchHits;
import net.semanticmetadata.lire.searchers.SimpleResult;
import net.semanticmetadata.lire.utils.MetricsUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.TreeSet;
import java.util.logging.Logger;

/**
 * Filtering results based on LSA.
 * Created 05.08.11, 08:41 <br/>
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class LsaFilter implements SearchHitsFilter {
    private Logger logger = Logger.getLogger(getClass().getName());
    private Class featureClass;
    private String fieldName;
    private LireFeature tempFeature = null;
    private int numberOfDimensions = -1;

    public LsaFilter(Class featureClass, String fieldName) {
        this.featureClass = featureClass;
        this.fieldName = fieldName;
    }

    /**
     * @param results
     * @param query
     * @return the filtered results or null if error occurs.
     */
    public ImageSearchHits filter(ImageSearchHits results, IndexReader reader, Document query) {
        // create a double[items][histogram]
        tempFeature = null;
        LinkedList<double[]> features = new LinkedList<double[]>();
        try {
            tempFeature = (LireFeature) featureClass.newInstance();
        } catch (Exception e) {
            logger.severe("Could not create feature " + featureClass.getName() + " (" + e.getMessage() + ").");
            return null;
        }
        // get all features from the result set, take care of those that do not have the respective field.
        for (int i = 0; i < results.length(); i++) {
            Document d = null;
            try {
                d = reader.document(results.documentID(i));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (d.getField(fieldName) != null) {
                tempFeature.setByteArrayRepresentation(d.getField(fieldName).binaryValue().bytes, d.getField(fieldName).binaryValue().offset, d.getField(fieldName).binaryValue().length);
                features.add(tempFeature.getFeatureVector());
            }
        }
        // now go for the query
        if (query.getField(fieldName) != null) {
            tempFeature.setByteArrayRepresentation(query.getField(fieldName).binaryValue().bytes, query.getField(fieldName).binaryValue().offset, query.getField(fieldName).binaryValue().length);
        } else {
            logger.severe("Query document is missing the given feature " + featureClass.getName() + ".");
            return null;
        }
        double[][] matrixData = new double[features.size() + 1][tempFeature.getFeatureVector().length];
        System.arraycopy(tempFeature.getFeatureVector(), 0, matrixData[0], 0, tempFeature.getFeatureVector().length);
        int count = 1;
        for (Iterator<double[]> iterator = features.iterator(); iterator.hasNext(); ) {
            double[] next = iterator.next();
            System.arraycopy(next, 0, matrixData[count], 0, next.length);
            count++;
        }
        for (int i = 0; i < matrixData.length; i++) {
            double[] doubles = matrixData[i];
            for (int j = 0; j < doubles.length; j++) {
                if (Double.isNaN(doubles[j])) System.err.println("Value is NaN");
                ;
            }
        }
        // create a matrix object and do the magic
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(matrixData);
        long ms = System.currentTimeMillis();
        SingularValueDecomposition svd = new SingularValueDecomposition(m);
        ms = System.currentTimeMillis() - ms;
        double[] singularValues = svd.getSingularValues();
        RealMatrix s = svd.getS();
        // if no number of dimensions is given reduce to a tenth.
        if (numberOfDimensions < 1) numberOfDimensions = singularValues.length / 10;
        for (int i = numberOfDimensions; i < singularValues.length; i++) {
            s.setEntry(i, i, 0);
        }
        RealMatrix mNew = svd.getU().multiply(s).multiply(svd.getVT());
        double[][] data = mNew.getData();

        // create the new result set
        TreeSet<SimpleResult> result = new TreeSet<SimpleResult>();
        double maxDistance = 0;
        double[] queryData = data[0];
        for (int i = 1; i < data.length; i++) {
            double[] doubles = data[i];
            double distance = MetricsUtils.distL1(doubles, queryData);
            result.add(new SimpleResult((float) distance, results.documentID(i - 1)));
            maxDistance = Math.max(maxDistance, distance);
        }
        ImageSearchHits hits;
        hits = new SimpleImageSearchHits(result, (float) maxDistance);
        return hits;
    }

    public ImageSearchHits filter(TopDocs results, IndexReader reader, Document query) throws IOException {
        LinkedList<SimpleResult> tmp = new LinkedList<SimpleResult>();
        double max = 0;
        for (int i = 0; i < results.scoreDocs.length; i++) {
            ScoreDoc scoreDoc = results.scoreDocs[i];
            SimpleResult s = new SimpleResult(1/scoreDoc.score, scoreDoc.doc);
            max = Math.max(max, 1/scoreDoc.score);
            tmp.add(s);
        }

        return filter(new SimpleImageSearchHits(tmp, (float) max), reader, query);
    }
}
