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
                tmp.remove((int) (Math.random() * (double) tmp.size()));
            }
            set.addAll(tmp);
        } else {
            while (set.size() < size) {
                set.add((int) (Math.random() * (double) maximum));
            }
        }
        return set;
    }
}
