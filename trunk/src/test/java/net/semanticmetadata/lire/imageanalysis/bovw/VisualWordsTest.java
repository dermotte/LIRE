package net.semanticmetadata.lire.imageanalysis.bovw;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.impl.VisualWordsImageSearcher;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 04.11.11
 *         Time: 16:18
 */
public class VisualWordsTest extends TestCase {
    public void testCreateQuery() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File("./index-mirflickr")));
        BufferedImage image = ImageIO.read(new File("./wang-1000/0.jpg"));
        SurfDocumentBuilder sb = new SurfDocumentBuilder();
        Document d = sb.createDocument(image, "query");
        SurfFeatureHistogramBuilder sfb = new SurfFeatureHistogramBuilder(reader);
        d = sfb.getVisualWords(d);
        VisualWordsImageSearcher searcher = new VisualWordsImageSearcher(3, DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
        ImageSearchHits hits = searcher.search(d, reader);
        for (int i = 0; i < hits.length(); i++) {
            hits.doc(i);
        }
    }
}
