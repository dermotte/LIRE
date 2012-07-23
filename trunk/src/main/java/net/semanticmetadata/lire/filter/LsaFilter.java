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

package net.semanticmetadata.lire.filter;


import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.SearchHitsFilter;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.impl.SimpleImageSearchHits;
import net.semanticmetadata.lire.impl.SimpleResult;
import net.semanticmetadata.lire.utils.MetricsUtils;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import org.apache.lucene.document.Document;

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
    public ImageSearchHits filter(ImageSearchHits results, Document query) {
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
            Document d = results.doc(i);
            if (d.getFieldable(fieldName) != null) {
                tempFeature.setByteArrayRepresentation(d.getFieldable(fieldName).getBinaryValue());
                features.add(tempFeature.getDoubleHistogram());
            }
        }
        // now go for the query
        if (query.getFieldable(fieldName) != null) {
            tempFeature.setByteArrayRepresentation(query.getFieldable(fieldName).getBinaryValue());
        } else {
            logger.severe("Query document is missing the given feature " + featureClass.getName() + ".");
            return null;
        }
        double[][] matrixData = new double[features.size() + 1][tempFeature.getDoubleHistogram().length];
        System.arraycopy(tempFeature.getDoubleHistogram(), 0, matrixData[0], 0, tempFeature.getDoubleHistogram().length);
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
            result.add(new SimpleResult((float) distance, results.doc(i - 1)));
            maxDistance = Math.max(maxDistance, distance);
        }
        ImageSearchHits hits;
        hits = new SimpleImageSearchHits(result, (float) maxDistance);
        return hits;
    }
}
