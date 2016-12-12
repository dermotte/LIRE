package samples;

import net.semanticmetadata.lire.imageanalysis.features.global.AutoColorCorrelogram;
import net.semanticmetadata.lire.imageanalysis.features.global.CEDD;
import net.semanticmetadata.lire.indexers.parallel.ParallelIndexer;

import java.io.File;

/**
 * Created by Mathias Lux on 24.11.2016.
 */
public class Indexing {
    public static void main(String[] args) {
        int numOfThreads = 6; // the number of thread used.
        // Checking if arg[0] is there and if it is a directory.
        boolean passed = false;
        if (args.length > 0) {
            File f = new File(args[0]);
            System.out.println("samples.Indexing images in " + args[0]);
            if (f.exists() && f.isDirectory()) passed = true;
        }

        if (!passed) {
            System.out.println("No directory given as first argument.");
            System.out.println("Run \"ParallelIndexing <directory>\" to index files of a directory.");
            System.exit(1);
        }

        // use ParallelIndexer to index all photos from args[0] into "index" ... use 6 threads (actually 7 with the I/O thread).
        ParallelIndexer indexer = new ParallelIndexer(numOfThreads, "index", args[0]);
        // use this to add you preferred builders. For now we go for CEDD and AutoColorCorrelogram
        indexer.addExtractor(CEDD.class);
        indexer.addExtractor(AutoColorCorrelogram.class);
        indexer.run();
        System.out.println("Finished indexing.");
    }
}
