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

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

/**
 * <h2>Searching in an Index</h2>
 * Use the ImageSearcherFactory for creating an ImageSearcher, which will retrieve the images
 * for you from the index.
 * <p/>
 * <pre>
 * IndexReader reader = IndexReader.open(indexPath);
 * ImageSearcher searcher = ImageSearcherFactory.createDefaultSearcher();
 * FileInputStream imageStream = new FileInputStream("image.jpg");
 * BufferedImage bimg = ImageIO.read(imageStream);
 * // searching for an image:
 * ImageSearchHits hits = null;
 * hits = searcher.search(bimg, reader);
 * for (int i = 0; i < 5; i++) {
 * System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
 * }
 *
 * // searching for a document:
 * Document document = hits.doc(0);
 * hits = searcher.search(document, reader);
 * for (int i = 0; i < 5; i++) {
 * System.out.println(hits.score(i) + ": " + hits.doc(i).getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
 * }
 * </pre>
 * <p/>
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net
 * <br>Date: 01.02.2006
 * <br>Time: 00:09:42
 *
 * @author Mathias Lux, mathias@juggle.at
 */
public interface ImageSearcher {
    /**
     * Searches for images similar to the given image.
     *
     * @param image  the example image to search for.
     * @param reader the IndexReader which is used to search through the images.
     * @return a sorted list of hits.
     * @throws java.io.IOException in case exceptions in the reader occurs
     */
    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException;

    /**
     * Searches for images similar to the given image, defined by the Document from the index.
     *
     * @param doc    the example image to search for.
     * @param reader the IndexReader which is used to dsearch through the images.
     * @return a sorted list of hits.
     * @throws java.io.IOException in case exceptions in the reader occurs
     */
    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException;

    /**
     * Searches for images similar to the given image.
     *
     * @param image  the example image to search for.
     * @param reader the IndexReader which is used to dsearch through the images.
     * @return a sorted list of hits.
     * @throws IOException in case the image could not be read from stream.
     */
    public ImageSearchHits search(InputStream image, IndexReader reader) throws IOException;

    /**
     * Identifies duplicates in the database.
     *
     * @param reader the IndexReader which is used to dsearch through the images.
     * @return a sorted list of hits.
     * @throws IOException in case the image could not be read from stream.
     */
    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException;

    /**
     * Modifies the given search by the provided positive and negative examples. This process follows the idea
     * of relevance feedback.
     *
     * @param originalSearch
     * @param positives
     * @param negatives
     * @return
     */
    public ImageSearchHits relevanceFeedback(ImageSearchHits originalSearch,
                                             Set<Document> positives, Set<Document> negatives);
}
