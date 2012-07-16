package edu.uniklu.itec.mosaix.engine;

import org.apache.lucene.document.Document;
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
 * <p>This outlines a factory that is capable
 * of instantiating a new WeightingData
 * instance.</p>
 * <p>The primary motivation behind this
 * interface is to make the WeightingData
 * interface more flexible and actually
 * instantiatable by the engine, so no
 * modification to the engine class itself
 * is needed to replace or extends the
 * functionality provided by the WeightingData
 * interface.</p>
 *
 * @author Manuel Warum
 * @author Mathias Lux, mathias@juggle.at
 * @version 1.0
 */
public interface WeightingDataFactory {
    /**
     * Creates a new instance of a class
     * that implements the WeightingData
     * interface.
     *
     * @param doc the document to evaluate.
     * @return a new WeightingData instance.
     */
    public WeightingData newInstance(final Document doc);
}
