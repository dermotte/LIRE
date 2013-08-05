package edu.uniklu.itec.mosaix.engine;

import net.semanticmetadata.lire.DocumentBuilder;
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
 * Simple example implementation for the
 * <code>WeightingDataFactory</code> interface.
 *
 * @author Manuel Warum
 */
final class SimpleWeightingDataFactory implements WeightingDataFactory {
    public WeightingData newInstance(final Document doc) {
        return new SimpleWeightingData(doc.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
    }
}
