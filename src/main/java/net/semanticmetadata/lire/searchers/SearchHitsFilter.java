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

package net.semanticmetadata.lire.searchers;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.TopDocs;

import java.io.IOException;

/**
 * Interface for all filters that take a result list and create a new result list. This can be done by actual
 * filtering (removing parts) or by re-ranking, which would be a change in the ranking of the given results.
 * Created 03.08.11, 10:28 <br/>
 *
 * @author Mathias Lux, mathias@juggle.at
 * @see net.semanticmetadata.lire.filters.RerankFilter
 */
public interface SearchHitsFilter {
    /**
     * Filters a given result list based on the given query document.
     *
     * @param results the results
     * @param query the original query document
     * @return
     */
    public ImageSearchHits filter(ImageSearchHits results, IndexReader reader, Document query);

    /**
     * Filters the result list of a Lucene search based on image features.
     * @param results the results
     * @param reader the IndexReader employed
     * @param query the original query document
     * @return
     * @throws IOException
     */
    public ImageSearchHits filter(TopDocs results, IndexReader reader, Document query) throws IOException;
}
