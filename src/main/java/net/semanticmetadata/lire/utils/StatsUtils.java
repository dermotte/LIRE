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

package net.semanticmetadata.lire.utils;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * ...
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Created: 06.08.12, 11:02
 */
public class StatsUtils {
    /**
     * Draws a set if size <i>size</i> of sample number from 0 to maximum, following an even distribution. Each drawn number is unique.
     *
     * @param size    the size of the sample set
     * @param maximum the maximum number
     * @return a set of randomly sampled numbers in [0,maximum], null if the request is not possible.
     */
    public static Set<Integer> drawSample(int size, int maximum) {
        if (maximum < size) return null;
        HashSet<Integer> set = new HashSet<Integer>(size);
        if (size * 4 > maximum) {
            // remove numbers from result.
            LinkedList<Integer> tmp = new LinkedList<Integer>();
            for (int i = 0; i < maximum; i++) {
                tmp.add(i);
            }
            while (tmp.size() > size) {
                tmp.remove((int) Math.floor(Math.random() * (double) tmp.size()));
            }
            set.addAll(tmp);
        } else {
            while (set.size() < size) {
                set.add((int) Math.floor((Math.random() * (double) maximum)));
            }
        }
//        for (Iterator<Integer> iterator = set.iterator(); iterator.hasNext(); ) {
//            Integer next = iterator.next();
//            System.out.print(next + ", ");
//        }
//        System.out.println();
        return set;
    }

    /**
     * Simple clamp function for floats.
     * @param val
     * @param min
     * @param max
     * @return
     */
    public static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

    /**
     * Simple clamp function for doubles.
     * @param val
     * @param min
     * @param max
     * @return
     */
    public static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }

    public static String convertTime(long time) {
        double h = time / 3600000.0;
        double m = (h - Math.floor(h)) * 60.0;
        double s = (m - Math.floor(m)) * 60;

//        return String.format("%02d:%02d:%02d", hour, minutes, seconds);
        return String.format("%s%02d:%02d", (((int) h > 0) ? String.format("%02d:", (int) h) : ""), (int) m, (int) s);
    }
}
