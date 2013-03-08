package net.semanticmetadata.lire.indexing;

import net.semanticmetadata.lire.utils.SerializationUtils;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.util.zip.GZIPInputStream;

/**
 * The Indexor (yes I know the name sounds weird, but it should match the Extractor class, and not
 * the Lucene Indexing classes) reads data files created by the {@link Extractor}.
 *
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 08.03.13
 *         Time: 14:28
 */
public class Indexor {
    public static void main(String[] args) {
        Indexor i = new Indexor();
        i.run();
    }

    public void run() {
        // do it ...
        byte[] tempInt = new byte[4];
        int tmp, tmpFeature;
        byte[] temp = new byte[2064];
        try {
            BufferedInputStream in = new BufferedInputStream(new GZIPInputStream(new FileInputStream("out.data")));
            // read file name length:
            while (in.read(tempInt, 0, 4) > 0) {
                tmp = SerializationUtils.toInt(tempInt);
                // read file name:
                in.read(temp, 0, tmp);
                String filename = new String(temp, 0, tmp);
                System.out.print(filename);
                while ((tmpFeature = in.read()) < 255) {
                    System.out.print(", " + tmpFeature);
                    // byte[] length ...
                    in.read(tempInt, 0, 4);
                    tmp = SerializationUtils.toInt(tempInt);
                    // read feature byte[]
                    in.read(temp, 0, tmp);
                }
                System.out.println();
            }
        // todo: put it into an index ...
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
