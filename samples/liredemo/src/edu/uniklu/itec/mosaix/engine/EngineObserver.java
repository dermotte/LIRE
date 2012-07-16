package edu.uniklu.itec.mosaix.engine;

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
 * Outlines the basic structure of an observer
 * that gets notified of results of certain decisions
 * that occur during the image evaluation process.
 *
 * @author Manuel Warum
 * @version 1.0
 */
public interface EngineObserver {
    /**
     * Flag to mark an image as used.
     */
    public final static int USED = 1;
    /**
     * Flag to mark an image as unused.
     */
    public final static int UNUSED = 2;

    /**
     * Notifies the observer of a state change.
     *
     * @param data  the relevant document.
     * @param state the flag to set.
     * @see edu.uniklu.itec.mosaix.engine.EngineObserver#USED
     * @see edu.uniklu.itec.mosaix.engine.EngineObserver#UNUSED
     */
    public void notifyState(WeightingData data, int state);
}
