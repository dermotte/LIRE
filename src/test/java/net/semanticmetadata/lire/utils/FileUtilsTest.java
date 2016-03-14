package net.semanticmetadata.lire.utils;

import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Created by Mathias Lux on 14.03.2016.
 */
public class FileUtilsTest extends TestCase {
    public void testListFilesString() {
        // see if it just works.
        try {
            ArrayList<String> testdata1 = FileUtils.getAllImages(new File("testdata/ferrari/black"), true);
            assertEquals(19, testdata1.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // see if it just fails.
        try {
            ArrayList<String> testdata2 = null;
            testdata2 = FileUtils.getAllImages(new File("testdata/ferrari/black/nothing"), true);
            fail();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    public void testListFiles() {
        // see if it just works.
        try {
            ArrayList<File> testdata1 = FileUtils.getAllImageFiles(new File("testdata/ferrari/black"), true);
            assertEquals(19, testdata1.size());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // see if it just fails.
        try {
            ArrayList<File> testdata2 = null;
            testdata2 = FileUtils.getAllImageFiles(new File("testdata/ferrari/black/nothing"), true);
            fail();
        } catch (Exception e) {
            // e.printStackTrace();
        }
    }

    /**
     * litlle helper
     */
    private static class ArrayPrint implements Consumer<String> {
        @Override
        public void accept(String s) {
            System.out.println(s);
        }
    }
}
