package edu.uniklu.itec.mosaix.engine;

import java.util.Random;
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
 * <p>This is a weighting strategy that solely relies on
 * randomness.</p>
 * <p/>
 * <p>This implementation uses a property named <code>impact</code>,
 * that specifies the variety of numbers generated. This impact is
 * usually a number between 0 (excl.) and 1 (incl.) and
 * influences the possible set of numbers returned by the
 * <code>getFactor</code> method. The possible set of numbers is
 * within the following range: max. <code>1</code>, but at least
 * <code>1 - impact</code>.</p>
 *
 * @author Manuel Warum
 * @version 1.0
 */
public final class RandomWeightingStrategy implements WeightingStrategy {
    /**
     * The default impact value, if it has not been set
     * explicitely.
     */
    public final static float DefaultImpact = 0.25f;

    private final static Random random_ = new Random();
    private float impact_;

    /**
     * Gets the current impact.
     *
     * @return current impact value.
     */
    public float getImpact() {
        return impact_;
    }

    /**
     * Sets the impact value.
     *
     * @param impact a floating point number between 0 (excl.) and
     *               1 (incl.)
     */
    public void setImpact(float impact) {
        assert impact > 0f && impact <= 1f;
        impact_ = impact;
    }

    /**
     * Creates a new instance of this class with the
     * specified impact value.
     *
     * @param impact a floating point number between 0 (excl.) and
     *               1 (incl.)
     */
    public RandomWeightingStrategy(float impact) {
        setImpact(impact);
    }

    /**
     * Creates a new instance of this class with the
     * default impact value.
     *
     * @see edu.uniklu.itec.mosaix.engine.RandomWeightingStrategy#DefaultImpact
     */
    public RandomWeightingStrategy() {
        this(DefaultImpact);
    }

    public float getFactor(WeightingData data) {
        float v = random_.nextFloat() * impact_ + (1 - impact_);
        // Logging.log(this, "Random factor: " + v);
        return v;
    }

    public void reset() {
    }
}
