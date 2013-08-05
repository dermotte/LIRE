package edu.uniklu.itec.mosaix.engine;

import java.awt.image.BufferedImage;
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
 * <p>Implements a strategy that takes the proportions
 * of an image into account.</p>
 * <p/>
 * <p>This implementation takes the image to be replaced
 * into account as well as the replacement cadidate. This
 * weighting strategy will yield into a factor between
 * <code>0</code> (excl.) and <code>1</code> (incl.).</p>
 *
 * @author Manuel Warum
 * @version 0.1337
 */
@Experimental
public final class ProportionWeightingStrategy implements WeightingStrategy {
    public float getFactor(final WeightingData data) {
        BufferedImage a = data.getSlice();
        BufferedImage b = data.getReplacement();
        float ap = getProportions(a);
        float bp = getProportions(b);
        // Logging.log(this, "Proportions evaluated, slice=" + ap + ", repl=" + bp);
        float v = Math.min(ap, bp) / Math.max(ap, bp);
        // Logging.log(this, "Proportional similarity: " + v);
        return v;
    }

    /**
     * Calculates a number representing the
     * relation between the width and the
     * height of an image.
     *
     * @param img the image to inspect, may not be <code>null</code>.
     * @return a value greater than zero representing the
     *         dimensional aspect of an image.
     */
    private static float getProportions(final BufferedImage img) {
        assert img != null;
        return (float) img.getWidth() / (float) img.getHeight();
    }

    /**
     * This method does nothing as this implementation
     * is stateless, and therefore solely exists
     * to satisfy the interface specifications.
     */
    public void reset() {
    }
}
