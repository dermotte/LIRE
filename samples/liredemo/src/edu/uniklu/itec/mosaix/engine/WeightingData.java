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
 * <p>Outlines a container for data that is
 * relevant to the weighting evaluation.
 * By default, it should at least contain
 * primitive information about the image
 * slice and a possible replacement, as
 * well as LIRE's initial relevancy
 * factor.</p>
 * <p>Further information can be added but
 * require a slight modification to the
 * engine.</p>
 *
 * @author Manuel Warum
 * @author Mathias Lux, mathias@juggle.at
 * @version 1.0
 * @see edu.uniklu.itec.mosaix.engine.WeightingStrategy
 */
public interface WeightingData {
    /**
     * Gets the initial relevancy as determined
     * by the LIRE framework.
     *
     * @return initial relevancy, a floating point
     *         number between <code>0.0f</code> and
     *         <code>1.0f</code>.
     */
    public float getRelevancy();

    /**
     * For scaling the image in the image cache ..
     *
     * @param scalePercentage from 1-100. if neg. it is ignored.
     */
    public void setScalePercentage(double scalePercentage);

    /**
     * Sets the initial relevacy as determined
     * by the LIRE framework.
     * Weighting implementations et al. should
     * never set this value directly but rather
     * let the engine handle it.
     *
     * @param relevancy the new relevancy, between
     *                  <code>0.0f</code> and <code>1.0f</code>.
     */
    public void setRelevancy(float relevancy);

    /**
     * Gets the input slice.
     * This is a partial image contained in the
     * source image that will be replaced in
     * the output image.
     *
     * @return the current part of the source image.
     */
    public BufferedImage getSlice();

    /**
     * Sets the input slice.
     * This is a partial image contained in the
     * source image that will be replaced in
     * the output image.
     *
     * @param slice a non-<code>null</code> instance
     *              to the current slice that the engine operates
     *              on.
     */
    public void setSlice(BufferedImage slice);

    /**
     * Gets the possible replacement candidate.
     *
     * @return a suitable image that is a cadidate
     *         to replace the slice.
     */
    public BufferedImage getReplacement();

    /**
     * Sets the possible replacement cadidate.
     *
     * @param replacement a suitable image that is a cadidate
     *                    to replace the slice.
     */
    public void setReplacement(BufferedImage replacement);

    public void setId(String id);

    /**
     * Returns the unique identifier provided to the
     * constructor (e.g. the file name).
     *
     * @return a unique identifier.
     */
    public String getId();
}
