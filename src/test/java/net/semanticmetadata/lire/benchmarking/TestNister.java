package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.SurfDocumentBuilder;
import net.semanticmetadata.lire.utils.FileUtils;
import net.semanticmetadata.lire.utils.LuceneUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexWriter;

import javax.imageio.ImageIO;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Created by IntelliJ IDEA.
 * User: mlux
 * Date: 01.08.12
 * Time: 17:41
 * To change this template use File | Settings | File Templates.
 */
public class TestNister extends TestCase {
    public void testIndexing() throws IOException {
        ChainedDocumentBuilder b = new ChainedDocumentBuilder();
        b.addBuilder(new SurfDocumentBuilder());
        b.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());

        ArrayList<String> files = FileUtils.getAllImages(new File("Z:\\MultimediaShare\\image_datasets\\ukbench-nister\\full"), true);

        System.out.println("files.size() = " + files.size());

        IndexWriter writer = LuceneUtils.createIndexWriter("nisterindex", true);
        int count = 0;
        long ms = System.currentTimeMillis();
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext(); ) {
            String next = iterator.next();
            Document d = b.createDocument(ImageIO.read(new File(next)), next);
            writer.addDocument(d);
            count++;
            if (count%100==0) {
                float time = (float) (System.currentTimeMillis() - ms);
                System.out.println("Finished "+count +" images, " + (((float) count)/10200f)*100 + "%. " + (time / (float) count) +" ms per image.");
            }
        }
        writer.close();
    }
}
