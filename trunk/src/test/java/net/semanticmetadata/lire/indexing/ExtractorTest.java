package net.semanticmetadata.lire.indexing;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;

import javax.imageio.ImageIO;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

/**
 * Created with IntelliJ IDEA.
 * User: mlux
 * Date: 12.03.13
 * Time: 13:21
 */
public class ExtractorTest extends TestCase {
    public void testExtraction() {
        Extractor e = new Extractor();
        e.setFileList(new File("imageList.txt"));
        e.setOutFile(new File("out.data"));
        e.addFeature(new CEDD());
        e.run();

        // do it ...
        byte[] tempInt = new byte[4];
        int tmp, tmpFeature;
        byte[] temp = new byte[2064];
        File inputFile = new File("out.data");
        try {
            BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(new FileInputStream("out.data")));
            // read file name length:
            while (in.read(tempInt, 0, 4) > 0) {
                tmp = SerializationUtils.toInt(tempInt);
                // read file name:
                in.read(temp, 0, tmp);
                String filename = new String(temp, 0, tmp);
                // normalize Filename to full path.
                filename = inputFile.getCanonicalPath().substring(0, inputFile.getCanonicalPath().lastIndexOf(inputFile.getName())) + filename;
                System.out.print(filename);
                while ((tmpFeature = in.read()) < 255) {
                    System.out.print(", " + tmpFeature);
                    LireFeature f = (LireFeature) Class.forName(Extractor.features[tmpFeature]).newInstance();
                    // byte[] length ...
                    in.read(tempInt, 0, 4);
                    tmp = SerializationUtils.toInt(tempInt);
                    // read feature byte[]
                    in.read(temp, 0, tmp);
                    f.setByteArrayRepresentation(temp, 0, tmp);
                    // test f ...
                    LireFeature f2 = (LireFeature) Class.forName(Extractor.features[tmpFeature]).newInstance();
                    f2.extract(ImageIO.read(new File(filename)));
                    System.out.println("f2.getDistance(f) = " + f2.getDistance(f));
                }
                System.out.println();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }
}
