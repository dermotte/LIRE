package net.semanticmetadata.lire.utils;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author Mathias Lux, mathias@juggle.at, 04.04.13, 09:30
 */
public class ImageUtilsTest extends TestCase {
    public void testCheckOpen() throws IOException {
        ArrayList<File> allImageFiles = FileUtils.getAllImageFiles(new File("E:\\MirFlickr\\images10"), true);
        BufferedWriter bw = new BufferedWriter(new FileWriter("faulty10.txt"));
        long ms = System.currentTimeMillis();
        int count = 0;
        for (Iterator<File> iterator = allImageFiles.iterator(); iterator.hasNext(); ) {
            File next = iterator.next();
            if (!FileUtils.isImageFileCompatible(next)) {
                bw.write(next.getCanonicalPath() + "\n");
            }
            count++;
            if (count%5000 == 0) System.out.println(count + " images analyzed, " + (System.currentTimeMillis()-ms)/count + " ms / image");
        }
        bw.close();
    }
}
