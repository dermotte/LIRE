package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.imageanalysis.CEDD;
import net.semanticmetadata.lire.imageanalysis.bovw.SiftFeatureHistogramBuilder;
import net.semanticmetadata.lire.imageanalysis.bovw.SurfFeatureHistogramBuilder;
import net.semanticmetadata.lire.impl.*;
import net.semanticmetadata.lire.utils.LuceneUtils;
import net.semanticmetadata.lire.utils.StatsUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.store.FSDirectory;

import javax.imageio.ImageIO;
import java.io.*;
import java.math.BigInteger;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/**
 * User: mlux
 * Date: 01.08.12
 * Time: 17:41
 */
public class TestNister extends TestCase {
    public static double[] df;
    public static double avgDocLength;
    int[] tests = {1091, 8279, 24, 9064, 4092, 7403, 1894, 3558, 4642, 8290, 2948, 7079, 4382, 4900, 3575, 528, 7908, 2494, 4360, 8552, 776, 10106, 8195, 4477, 7833, 4010, 7599, 8998, 8448, 3204, 7123, 6374, 92, 8457, 7343, 5175, 2243, 6605, 3492, 5398, 9587, 4441, 8480, 6347, 8114, 3513, 6609, 7848, 7769, 5346, 2850, 6913, 9623, 4531, 6110, 8005, 6183, 3144, 2592, 6455, 9918, 3138, 6162, 3918, 1251, 9181, 9175, 5333, 9632, 5573, 9877, 8060, 6117, 8443, 5337, 9685, 474, 6744, 8347, 6720, 5538, 2389, 3898, 8885, 3344, 6026, 4338, 3078, 4336, 235, 704, 7426, 1193, 1946, 2625, 7724, 9942, 3390, 5103, 8609};
    public static int numVisualWords = 512;

    public void testIndexing() throws IOException {
        ChainedDocumentBuilder b = new ChainedDocumentBuilder();
        b.addBuilder(new SiftDocumentBuilder());
        // b.addBuilder(DocumentBuilderFactory.getCEDDDocumentBuilder());

        ArrayList<String> files = net.semanticmetadata.lire.utils.FileUtils.getAllImages(new File("W:\\MultimediaShare\\image_datasets\\ukbench-nister\\full"), true);

        System.out.println("files.size() = " + files.size());

        IndexWriter writer = LuceneUtils.createIndexWriter("nisterindex_sift", true);
        int count = 0;
        long ms = System.currentTimeMillis();
        for (Iterator<String> iterator = files.iterator(); iterator.hasNext(); ) {
            String next = iterator.next();
            Document d = b.createDocument(ImageIO.read(new File(next)), next);
            writer.addDocument(d);
            count++;
            if (count % 100 == 0) {
                float time = (float) (System.currentTimeMillis() - ms);
                System.out.println("Finished " + count + " images, " + (((float) count) / 10200f) * 100 + "%. " + (time / (float) count) + " ms per image.");
            }
        }
        writer.close();
    }

    public void createVocabulary(String pathName) throws IOException {
        // first: copy index to a new location.
        FileUtils.copyDirectory(new File("nisterindex"), new File(pathName));
        System.out.println("Index copied to " + pathName + ".");
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(pathName)));
//        SiftFeatureHistogramBuilder sfh = new SiftFeatureHistogramBuilder(reader, 1000, numVisualWords);
        SurfFeatureHistogramBuilder sfh = new SurfFeatureHistogramBuilder(reader, 2000, numVisualWords);
        sfh.index();
        reader.close();
    }

    public void computePrecision(String pathName, Similarity similarity, String label) throws IOException {
        ImageSearcher vis = new GenericImageSearcher(4, SimpleFeature.class, "featureSURFHistogram");
//        ImageSearcher vis = new GenericFastImageSearcher(4, CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
//        VisualWordsImageSearcher vis = new VisualWordsImageSearcher(4, similarity, DocumentBuilder.FIELD_NAME_SIFT_LOCAL_FEATURE_HISTOGRAM_VISUAL_WORDS);
//        VisualWordsImageSearcher vis = new VisualWordsImageSearcher(4, similarity, DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(pathName)));

        int queryID, resultID;
        int countSearches = 0, countTruePositives = 0;
        float avgPrecision = 0f;

        Set<Integer> test = StatsUtils.drawSample(100, 10200);

        for (int i : test)  {
//        for (int j = 0; j < tests.length; j++) {
//            int i = tests[j];
//        for (int i =0; i < 1000; i++) {
//        for (int i =0; i < reader.numDocs(); i++) {
            if (!reader.isDeleted(i)) {
                ImageSearchHits hits = vis.search(reader.document(i), reader);
                String s = reader.document(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                s = s.replaceAll("\\D", "");
                queryID = Integer.parseInt(s);
                countTruePositives = 0;
                for (int k = 0; k < hits.length(); k++) {
                    String name = hits.doc(k).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                    name = name.replaceAll("\\D", "");
                    resultID = Integer.parseInt(name);
                    if (queryID / 4 == resultID / 4) {
                        //System.out.print("X");
                        countTruePositives++;
                    }
                    //else System.out.print("O");
                }
                countSearches++;
                avgPrecision += (float) countTruePositives / 4f;
                // progress:
//                if (countSearches%100==0) System.out.print('.');
//                if (countSearches%1000==0) System.out.print(':');
                //System.out.println();
            }
        }
        avgPrecision = avgPrecision / (float) countSearches;
        FileWriter fw = new FileWriter(new File("precision_results.txt"), true);
        System.out.println(label + " p@4= " + avgPrecision);
        fw.write(label + " p@4= " + avgPrecision + "\n");
        fw.close();
    }

    public void benchmark(int numWords) throws IOException {
        numVisualWords = numWords;
        String pathName = "nis_test_surf_" + numWords;
        createVocabulary(pathName);
//        testDocLengthIDF(pathName);
        for (int k = 0; k < 5; k++) { // run the test 5 times ...
            computePrecision(pathName, new TfIdfSimilarity(), "SURF_lfhist_" + numWords + "_bm25");
        }
        System.out.println();
    }

    public void testBenchmark() throws IOException {
        //testIndexing();
//        benchmark(256);
        benchmark(512);
//        benchmark(1024);
//        benchmark(2048);
//        benchmark(2048 + 1024);
//        benchmark(2048 + 2048);
//        computePrecision("nisterindex", DefaultSimilarity.getDefault(), "_hist_cedd");
//        computePrecision("nis_test_512", DefaultSimilarity.getDefault(), "SURF_lfhist_512_norm_log");
//        computePrecision("nis_test_256", DefaultSimilarity.getDefault(), "SURF_lfhist_256_norm_log");
    }

    public void testDocLengthIDF(String pathName) throws IOException {
        df = new double[1024];
        int[] len = new int[10200];

        avgDocLength = 0;
        double numDocs = 0;
        for (int i = 0; i < df.length; i++)
            df[i] = 0;
        for (int i = 0; i < len.length; i++)
            len[i] = 0;
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(pathName)));
        for (int i = 0; i < reader.numDocs(); i++) {
            if (!reader.isDeleted(i)) {
                String s = reader.document(i).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
                String f = reader.document(i).getValues("featureSURFHistogram")[0];
                SimpleFeature sf = new SimpleFeature();
                sf.setStringRepresentation(f);
                double[] h = sf.getDoubleHistogram();
                for (int j = 0; j < h.length; j++) {
                    if (h[j] > 0.0) df[j] += 1; // add to the document frequency
                    avgDocLength += h[j];
                    len[i] += h[j];
                }
                numDocs +=1;
            }
        }
//        System.out.println("avgDocLength = " + avgDocLength/numDocs);
//        for (int i = 0; i < df.length; i++)
//            System.out.print(df[i] + ",");
//        System.out.println();
//        for (int i = 0; i < len.length; i++)
//            System.out.print(len[i] + ", ");
//        System.out.println();
    }
}

/**
 * General file manipulation utilities.
 * <p/>
 * Facilities are provided in the following areas:
 * <ul>
 * <li>writing to a file
 * <li>reading from a file
 * <li>make a directory including parent directories
 * <li>copying files and directories
 * <li>deleting files and directories
 * <li>converting to and from a URL
 * <li>listing files and directories by filter and extension
 * <li>comparing file content
 * <li>file last changed date
 * <li>calculating a checksum
 * </ul>
 * <p/>
 * Origin of code: Excalibur, Alexandria, Commons-Utils
 *
 * @version $Id: FileUtils.java 1349509 2012-06-12 20:39:23Z ggregory $
 */

class FileUtils {

    /**
     * Instances should NOT be constructed in standard programming.
     */
    public FileUtils() {
        super();
    }

    /**
     * The number of bytes in a kilobyte.
     */
    public static final long ONE_KB = 1024;

    /**
     * The number of bytes in a kilobyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_KB_BI = BigInteger.valueOf(ONE_KB);

    /**
     * The number of bytes in a megabyte.
     */
    public static final long ONE_MB = ONE_KB * ONE_KB;

    /**
     * The number of bytes in a megabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_MB_BI = ONE_KB_BI.multiply(ONE_KB_BI);

    /**
     * The file copy buffer size (30 MB)
     */
    private static final long FILE_COPY_BUFFER_SIZE = ONE_MB * 30;

    /**
     * The number of bytes in a gigabyte.
     */
    public static final long ONE_GB = ONE_KB * ONE_MB;

    /**
     * The number of bytes in a gigabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_GB_BI = ONE_KB_BI.multiply(ONE_MB_BI);

    /**
     * The number of bytes in a terabyte.
     */
    public static final long ONE_TB = ONE_KB * ONE_GB;

    /**
     * The number of bytes in a terabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_TB_BI = ONE_KB_BI.multiply(ONE_GB_BI);

    /**
     * The number of bytes in a petabyte.
     */
    public static final long ONE_PB = ONE_KB * ONE_TB;

    /**
     * The number of bytes in a petabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_PB_BI = ONE_KB_BI.multiply(ONE_TB_BI);

    /**
     * The number of bytes in an exabyte.
     */
    public static final long ONE_EB = ONE_KB * ONE_PB;

    /**
     * The number of bytes in an exabyte.
     *
     * @since 2.4
     */
    public static final BigInteger ONE_EB_BI = ONE_KB_BI.multiply(ONE_PB_BI);

    /**
     * The number of bytes in a zettabyte.
     */
    public static final BigInteger ONE_ZB = BigInteger.valueOf(ONE_KB).multiply(BigInteger.valueOf(ONE_EB));

    /**
     * The number of bytes in a yottabyte.
     */
    public static final BigInteger ONE_YB = ONE_KB_BI.multiply(ONE_ZB);

    /**
     * An empty array of type <code>File</code>.
     */
    public static final File[] EMPTY_FILE_ARRAY = new File[0];

    /**
     * The UTF-8 character set, used to decode octets in URLs.
     */
    private static final Charset UTF8 = Charset.forName("UTF-8");

    //-----------------------------------------------------------------------

    /**
     * Construct a file from the set of name elements.
     *
     * @param directory the parent directory
     * @param names     the name elements
     * @return the file
     * @since 2.1
     */
    public static File getFile(File directory, String... names) {
        if (directory == null) {
            throw new NullPointerException("directorydirectory must not be null");
        }
        if (names == null) {
            throw new NullPointerException("names must not be null");
        }
        File file = directory;
        for (String name : names) {
            file = new File(file, name);
        }
        return file;
    }

    /**
     * Construct a file from the set of name elements.
     *
     * @param names the name elements
     * @return the file
     * @since 2.1
     */
    public static File getFile(String... names) {
        if (names == null) {
            throw new NullPointerException("names must not be null");
        }
        File file = null;
        for (String name : names) {
            if (file == null) {
                file = new File(name);
            } else {
                file = new File(file, name);
            }
        }
        return file;
    }

    /**
     * Returns the path to the system temporary directory.
     *
     * @return the path to the system temporary directory.
     * @since 2.0
     */
    public static String getTempDirectoryPath() {
        return System.getProperty("java.io.tmpdir");
    }

    /**
     * Returns a {@link File} representing the system temporary directory.
     *
     * @return the system temporary directory.
     * @since 2.0
     */
    public static File getTempDirectory() {
        return new File(getTempDirectoryPath());
    }

    /**
     * Returns the path to the user's home directory.
     *
     * @return the path to the user's home directory.
     * @since 2.0
     */
    public static String getUserDirectoryPath() {
        return System.getProperty("user.home");
    }

    /**
     * Returns a {@link File} representing the user's home directory.
     *
     * @return the user's home directory.
     * @since 2.0
     */
    public static File getUserDirectory() {
        return new File(getUserDirectoryPath());
    }

    //-----------------------------------------------------------------------

    /**
     * Opens a {@link FileInputStream} for the specified file, providing better
     * error messages than simply calling <code>new FileInputStream(file)</code>.
     * <p/>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p/>
     * An exception is thrown if the file does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be read.
     *
     * @param file the file to open for input, must not be {@code null}
     * @return a new {@link FileInputStream} for the specified file
     * @throws FileNotFoundException if the file does not exist
     * @throws IOException           if the file object is a directory
     * @throws IOException           if the file cannot be read
     * @since 1.3
     */
    public static FileInputStream openInputStream(File file) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canRead() == false) {
                throw new IOException("File '" + file + "' cannot be read");
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' does not exist");
        }
        return new FileInputStream(file);
    }

    //-----------------------------------------------------------------------

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p/>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p/>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     *
     * @param file the file to open for output, must not be {@code null}
     * @return a new {@link FileOutputStream} for the specified file
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be written to
     * @throws IOException if a parent directory needs creating but that fails
     * @since 1.3
     */
    public static FileOutputStream openOutputStream(File file) throws IOException {
        return openOutputStream(file, false);
    }

    /**
     * Opens a {@link FileOutputStream} for the specified file, checking and
     * creating the parent directory if it does not exist.
     * <p/>
     * At the end of the method either the stream will be successfully opened,
     * or an exception will have been thrown.
     * <p/>
     * The parent directory will be created if it does not exist.
     * The file will be created if it does not exist.
     * An exception is thrown if the file object exists but is a directory.
     * An exception is thrown if the file exists but cannot be written to.
     * An exception is thrown if the parent directory cannot be created.
     *
     * @param file   the file to open for output, must not be {@code null}
     * @param append if {@code true}, then bytes will be added to the
     *               end of the file rather than overwriting
     * @return a new {@link FileOutputStream} for the specified file
     * @throws IOException if the file object is a directory
     * @throws IOException if the file cannot be written to
     * @throws IOException if a parent directory needs creating but that fails
     * @since 2.1
     */
    public static FileOutputStream openOutputStream(File file, boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileOutputStream(file, append);
    }

    //-----------------------------------------------------------------------

    /**
     * Returns a human-readable version of the file size, where the input represents a specific number of bytes.
     * <p>
     * If the size is over 1GB, the size is returned as the number of whole GB, i.e. the size is rounded down to the
     * nearest GB boundary.
     * </p>
     * <p>
     * Similarly for the 1MB and 1KB boundaries.
     * </p>
     *
     * @param size the number of bytes
     * @return a human-readable display value (includes units - EB, PB, TB, GB, MB, KB or bytes)
     * @see <a href="https://issues.apache.org/jira/browse/IO-226">IO-226 - should the rounding be changed?</a>
     * @since 2.4
     */
    // See https://issues.apache.org/jira/browse/IO-226 - should the rounding be changed?
    public static String byteCountToDisplaySize(BigInteger size) {
        String displaySize;

        if (size.divide(ONE_EB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_EB_BI)) + " EB";
        } else if (size.divide(ONE_PB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_PB_BI)) + " PB";
        } else if (size.divide(ONE_TB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_TB_BI)) + " TB";
        } else if (size.divide(ONE_GB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_GB_BI)) + " GB";
        } else if (size.divide(ONE_MB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_MB_BI)) + " MB";
        } else if (size.divide(ONE_KB_BI).compareTo(BigInteger.ZERO) > 0) {
            displaySize = String.valueOf(size.divide(ONE_KB_BI)) + " KB";
        } else {
            displaySize = String.valueOf(size) + " bytes";
        }
        return displaySize;
    }

    /**
     * Returns a human-readable version of the file size, where the input represents a specific number of bytes.
     * <p>
     * If the size is over 1GB, the size is returned as the number of whole GB, i.e. the size is rounded down to the
     * nearest GB boundary.
     * </p>
     * <p>
     * Similarly for the 1MB and 1KB boundaries.
     * </p>
     *
     * @param size the number of bytes
     * @return a human-readable display value (includes units - EB, PB, TB, GB, MB, KB or bytes)
     * @see <a href="https://issues.apache.org/jira/browse/IO-226">IO-226 - should the rounding be changed?</a>
     */
    // See https://issues.apache.org/jira/browse/IO-226 - should the rounding be changed?
    public static String byteCountToDisplaySize(long size) {
        return byteCountToDisplaySize(BigInteger.valueOf(size));
    }

    private static void doCopyFile(File srcFile, File destFile, boolean preserveFileDate) throws IOException {
        if (destFile.exists() && destFile.isDirectory()) {
            throw new IOException("Destination '" + destFile + "' exists but is a directory");
        }

        FileInputStream fis = null;
        FileOutputStream fos = null;
        FileChannel input = null;
        FileChannel output = null;
        try {
            fis = new FileInputStream(srcFile);
            fos = new FileOutputStream(destFile);
            input = fis.getChannel();
            output = fos.getChannel();
            long size = input.size();
            long pos = 0;
            long count = 0;
            while (pos < size) {
                count = size - pos > FILE_COPY_BUFFER_SIZE ? FILE_COPY_BUFFER_SIZE : size - pos;
                pos += output.transferFrom(input, pos, count);
            }
        } finally {
            output.close();
            fos.close();
            input.close();
            fis.close();
//            IOUtils.closeQuietly(output);
//            IOUtils.closeQuietly(fos);
//            IOUtils.closeQuietly(input);
//            IOUtils.closeQuietly(fis);
        }

        if (srcFile.length() != destFile.length()) {
            throw new IOException("Failed to copy full contents from '" +
                    srcFile + "' to '" + destFile + "'");
        }
        if (preserveFileDate) {
            destFile.setLastModified(srcFile.lastModified());
        }
    }

    public static void copyDirectory(File srcDir, File destDir) throws IOException {
        copyDirectory(srcDir, destDir, true);
    }

    /**
     * Copies a whole directory to a new location.
     * <p/>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.
     * <p/>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     * <p/>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the files' last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.
     *
     * @param srcDir           an existing directory to copy, must not be {@code null}
     * @param destDir          the new directory, must not be {@code null}
     * @param preserveFileDate true if the file date of the copy
     *                         should be the same as the original
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @since 1.1
     */
    public static void copyDirectory(File srcDir, File destDir,
                                     boolean preserveFileDate) throws IOException {
        copyDirectory(srcDir, destDir, null, preserveFileDate);
    }

    /**
     * Copies a filtered directory to a new location preserving the file dates.
     * <p/>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.
     * <p/>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     * <p/>
     * <strong>Note:</strong> This method tries to preserve the files' last
     * modified date/times using {@link File#setLastModified(long)}, however
     * it is not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.
     * <p/>
     * <h4>Example: Copy directories only</h4>
     * <pre>
     *  // only copy the directory structure
     *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY);
     *  </pre>
     *
     * <h4>Example: Copy directories and txt files</h4>
     * <pre>
     *  // Create a filter for ".txt" files
     *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *  // Create a filter for either directories or ".txt" files
     *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     *  // Copy using the filter
     *  FileUtils.copyDirectory(srcDir, destDir, filter);
     *  </pre>
     *
     * @param srcDir  an existing directory to copy, must not be {@code null}
     * @param destDir the new directory, must not be {@code null}
     * @param filter  the filter to apply, null means copy all directories and files
     *                should be the same as the original
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @since 1.4
     */
    public static void copyDirectory(File srcDir, File destDir,
                                     FileFilter filter) throws IOException {
        copyDirectory(srcDir, destDir, filter, true);
    }

    /**
     * Copies a filtered directory to a new location.
     * <p/>
     * This method copies the contents of the specified source directory
     * to within the specified destination directory.
     * <p/>
     * The destination directory is created if it does not exist.
     * If the destination directory did exist, then this method merges
     * the source with the destination, with the source taking precedence.
     * <p/>
     * <strong>Note:</strong> Setting <code>preserveFileDate</code> to
     * {@code true} tries to preserve the files' last modified
     * date/times using {@link File#setLastModified(long)}, however it is
     * not guaranteed that those operations will succeed.
     * If the modification operation fails, no indication is provided.
     * <p/>
     * <h4>Example: Copy directories only</h4>
     * <pre>
     *  // only copy the directory structure
     *  FileUtils.copyDirectory(srcDir, destDir, DirectoryFileFilter.DIRECTORY, false);
     *  </pre>
     *
     * <h4>Example: Copy directories and txt files</h4>
     * <pre>
     *  // Create a filter for ".txt" files
     *  IOFileFilter txtSuffixFilter = FileFilterUtils.suffixFileFilter(".txt");
     *  IOFileFilter txtFiles = FileFilterUtils.andFileFilter(FileFileFilter.FILE, txtSuffixFilter);
     *
     *  // Create a filter for either directories or ".txt" files
     *  FileFilter filter = FileFilterUtils.orFileFilter(DirectoryFileFilter.DIRECTORY, txtFiles);
     *
     *  // Copy using the filter
     *  FileUtils.copyDirectory(srcDir, destDir, filter, false);
     *  </pre>
     *
     * @param srcDir           an existing directory to copy, must not be {@code null}
     * @param destDir          the new directory, must not be {@code null}
     * @param filter           the filter to apply, null means copy all directories and files
     * @param preserveFileDate true if the file date of the copy
     *                         should be the same as the original
     * @throws NullPointerException if source or destination is {@code null}
     * @throws IOException          if source or destination is invalid
     * @throws IOException          if an IO error occurs during copying
     * @since 1.4
     */
    public static void copyDirectory(File srcDir, File destDir,
                                     FileFilter filter, boolean preserveFileDate) throws IOException {
        if (srcDir == null) {
            throw new NullPointerException("Source must not be null");
        }
        if (destDir == null) {
            throw new NullPointerException("Destination must not be null");
        }
        if (srcDir.exists() == false) {
            throw new FileNotFoundException("Source '" + srcDir + "' does not exist");
        }
        if (srcDir.isDirectory() == false) {
            throw new IOException("Source '" + srcDir + "' exists but is not a directory");
        }
        if (srcDir.getCanonicalPath().equals(destDir.getCanonicalPath())) {
            throw new IOException("Source '" + srcDir + "' and destination '" + destDir + "' are the same");
        }

        // Cater for destination being directory within the source directory (see IO-141)
        List<String> exclusionList = null;
        if (destDir.getCanonicalPath().startsWith(srcDir.getCanonicalPath())) {
            File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
            if (srcFiles != null && srcFiles.length > 0) {
                exclusionList = new ArrayList<String>(srcFiles.length);
                for (File srcFile : srcFiles) {
                    File copiedFile = new File(destDir, srcFile.getName());
                    exclusionList.add(copiedFile.getCanonicalPath());
                }
            }
        }
        doCopyDirectory(srcDir, destDir, filter, preserveFileDate, exclusionList);
    }

    /**
     * Internal copy directory method.
     *
     * @param srcDir           the validated source directory, must not be {@code null}
     * @param destDir          the validated destination directory, must not be {@code null}
     * @param filter           the filter to apply, null means copy all directories and files
     * @param preserveFileDate whether to preserve the file date
     * @param exclusionList    List of files and directories to exclude from the copy, may be null
     * @throws IOException if an error occurs
     * @since 1.1
     */
    private static void doCopyDirectory(File srcDir, File destDir, FileFilter filter,
                                        boolean preserveFileDate, List<String> exclusionList) throws IOException {
        // recurse
        File[] srcFiles = filter == null ? srcDir.listFiles() : srcDir.listFiles(filter);
        if (srcFiles == null) {  // null if abstract pathname does not denote a directory, or if an I/O error occurs
            throw new IOException("Failed to list contents of " + srcDir);
        }
        if (destDir.exists()) {
            if (destDir.isDirectory() == false) {
                throw new IOException("Destination '" + destDir + "' exists but is not a directory");
            }
        } else {
            if (!destDir.mkdirs() && !destDir.isDirectory()) {
                throw new IOException("Destination '" + destDir + "' directory cannot be created");
            }
        }
        if (destDir.canWrite() == false) {
            throw new IOException("Destination '" + destDir + "' cannot be written to");
        }
        for (File srcFile : srcFiles) {
            File dstFile = new File(destDir, srcFile.getName());
            if (exclusionList == null || !exclusionList.contains(srcFile.getCanonicalPath())) {
                if (srcFile.isDirectory()) {
                    doCopyDirectory(srcFile, dstFile, filter, preserveFileDate, exclusionList);
                } else {
                    doCopyFile(srcFile, dstFile, preserveFileDate);
                }
            }
        }

        // Do this last, as the above has probably affected directory metadata
        if (preserveFileDate) {
            destDir.setLastModified(srcDir.lastModified());
        }
    }

}

class TfIdfSimilarity extends DefaultSimilarity {
    public float tf(float freq) {
        return (float) freq;
    }

    public float idf(int docfreq, int numdocs) {
        return 1f;
    }

    @Override
    public float queryNorm(float sumOfSquaredWeights) {
        return 1;    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public float computeNorm(String field, FieldInvertState state) {
        return 1;    //To change body of overridden methods use File | Settings | File Templates.
    }

    @Override
    public float coord(int overlap, int maxOverlap) {
        return 1;
    }
}

