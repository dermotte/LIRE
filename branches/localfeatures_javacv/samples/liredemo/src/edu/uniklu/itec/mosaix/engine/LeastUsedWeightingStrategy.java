package edu.uniklu.itec.mosaix.engine;

import java.util.HashMap;
/*
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2007 by Mathias Lux (mathias@juggle.at), Lukas Esterle & Manuel Warum.
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * <p>Outlines a strategy that takes the usage count
 * of an image into account.</p>
 * <p>To achieve this, the following to things are
 * required for this strategy to be actually useful:</p>
 * <ol>
 * <li>The implementation of the <code>WeightingData</code>
 * instance must implement <code>hashCode()</code> in such
 * a way, that the resulting integer is always equal to
 * other instances handling the very same image.</li>
 * <li>The engine using this strategy also has to add this
 * strategy to its list of observers.</li>
 * </ol>
 * <p>If any of the above preconditions are not given,
 * then this strategy will yield false factors, most
 * often just <code>1.0f</code>.</p>
 *
 * @author Manuel Warum
 * @version 1.02
 * @see java.lang.Object#hashCode()
 */
public final class LeastUsedWeightingStrategy implements WeightingStrategy, EngineObserver {
    private HashMap<Integer, Integer> usageCount_;

    /**
     * Instantiates a new instance with the initial state.
     */
    public LeastUsedWeightingStrategy() {
        reset();
    }

    public float getFactor(final WeightingData data) {
        int hash = data.hashCode();
        int n = (usageCount_.containsKey(hash) ? usageCount_.get(hash) : 0) + 1;
        // Logging.log(this, "Image " + hash + " has been used " + n + " times already. " + n + "^-1 penalty.");
        return 1f / (float) n;  // force floating point arithmetic
    }

    /**
     * Resets the usage counter to its initial state.
     * All usage counters will be removed or reset to
     * zero.
     */
    public void reset() {
        usageCount_ = new HashMap<Integer, Integer>();
        // Logging.log(this, "(Re-)Set to initial state");
    }

    public void notifyState(final WeightingData data, int state) {
        if (state == EngineObserver.USED && data != null)
            shiftCounter(data.hashCode(), 1);
    }

    private void shiftCounter(int hash, int shaft) {
        int cnt = usageCount_.containsKey(hash) ? usageCount_.get(hash) : 0;
        cnt += shaft;
        usageCount_.put(hash, cnt);
    }
}
