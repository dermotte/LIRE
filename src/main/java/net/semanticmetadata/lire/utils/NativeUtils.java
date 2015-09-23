package net.semanticmetadata.lire.utils;

import java.io.*;

/**
 * A simple library class which helps with loading dynamic libraries stored in the
 * JAR archive. These libraries usualy contain implementation of some methods in
 * native code (using JNI - Java Native Interface).
 */

public class NativeUtils {

    private NativeUtils() {
    }

    /**
     * Loads library from current JAR archive
     * <p/>
     * The file from JAR is copied into system temporary directory and then loaded. The temporary file is deleted
     * after exiting. Method uses String as filename because the pathname is "abstract", not system-dependent.
     *
     * @param path the path inside JAR as absolute path (beginning with '/'), e.g. /package/File.ext
     * @throws IOException              If temporary file creation or read/write operation fails
     * @throws IllegalArgumentException If source file (param path) does not exist
     * @throws IllegalArgumentException If the path is not absolute or if the filename is shorter than three
     *                                  characters (restriction of {@see File#createTempFile(java.lang.String, java.lang.String)}).
     */
    public static void loadLibraryFromJar(String path) throws IOException {

        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("The path has to be absolute (start with '/').");
        }

        // Obtain filename from path
        String[] parts = path.split("/");
        String filename = (parts.length > 1) ? parts[parts.length - 1] : null;

        // Split filename to prexif and suffix (extension)
        String prefix = "";
        String suffix = null;
        if (filename != null) {
            parts = filename.split("\\.", 2);
            prefix = parts[0];
            suffix = (parts.length > 1) ? "." + parts[parts.length - 1] : null; // Thanks, davs! :-)
        }

        // Check if the filename is okay
        if (filename == null || prefix.length() < 3) {
            throw new IllegalArgumentException("The filename has to be at least 3 characters long.");
        }

        // Prepare temporary file
        File temp = File.createTempFile(prefix, suffix);
        temp.deleteOnExit();

        if (!temp.exists()) {
            throw new FileNotFoundException("File " + temp.getAbsolutePath() + " does not exist.");
        }

        // Prepare buffer for data copying
        byte[] buffer = new byte[1024];
        int readBytes;

        // Open and check input stream
        InputStream is = NativeUtils.class.getResourceAsStream(path);
        if (is == null) {
            throw new FileNotFoundException("File " + path + " was not found inside JAR.");
        }

        // Open output stream and copy data between source file in JAR and the temporary file
        OutputStream os = new FileOutputStream(temp);
        try {
            while ((readBytes = is.read(buffer)) != -1) {
                os.write(buffer, 0, readBytes);
            }
        } finally {
            // If read/write fails, close streams safely before throwing an exception
            os.close();
            is.close();
        }

        // Finally, load the library
        System.load(temp.getAbsolutePath());
    }
}
