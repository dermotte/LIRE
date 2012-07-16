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

import net.semanticmetadata.lire.imageanalysis.LireFeature;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

/**
 * ...
 * Date: 13.08.2008
 * Time: 14:42:16
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public class SavedPivots implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    String[][] pivots;

    public SavedPivots(int[][] piv, List<LireFeature> objs) {
        // create a String array holding the two pivots per dimension.
        this.pivots = new String[2][piv[0].length];
        // save string representations for pivots:
        for (int i = 0; i < piv[0].length; i++) {
            pivots[0][i] = objs.get(piv[0][i]).getStringRepresentation();
            pivots[1][i] = objs.get(piv[1][i]).getStringRepresentation();
        }
    }

    /**
     * Create the pivots index array from the stored pivots and add them
     * to the list of objects to fastmap.
     *
     * @param objs       the list of objects to fastmap
     * @param descriptor the actual descriptor class of the LireFeature
     * @return
     */
    public int[][] getPivots(List<LireFeature> objs, Class<? extends LireFeature> descriptor) throws IllegalAccessException, InstantiationException {
        int[][] retVal = new int[2][pivots[0].length];
        List<LireFeature> pivs = new LinkedList<LireFeature>();
        int countIndex = 0;
        for (int i = 0; i < pivots[0].length; i++) {
            LireFeature vd1 = (LireFeature) descriptor.newInstance();
            vd1.setStringRepresentation(pivots[0][i]);
            pivs.add(vd1);
            retVal[0][i] = countIndex;
            countIndex++;
            LireFeature vd2 = (LireFeature) descriptor.newInstance();
            vd2.setStringRepresentation(pivots[1][i]);
            pivs.add(vd2);
            retVal[1][i] = countIndex;
            countIndex++;
        }
        objs.addAll(0, pivs);
        return retVal;
    }
}
