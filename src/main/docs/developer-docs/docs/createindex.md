# Creating an Index with Lire

Use one of the available [builders](builders.md) to create Lucene domunets and add them in your index. Following, you can find sample code on how to create an index using one or multiple features.

## Sample Code - Indexing
    /**
     * Simple class showing the process of indexing
     * @author Mathias Lux, mathias@juggle.at and Nektarios Anagnostopoulos, nek.anag@gmail.com
     */
    public class Indexer {
        public static void main(String[] args) throws IOException {
            // Checking if arg[0] is there and if it is a directory.
            boolean passed = false;
            if (args.length > 0) {
                File f = new File(args[0]);
                System.out.println("Indexing images in " + args[0]);
                if (f.exists() && f.isDirectory()) passed = true;
            }
            if (!passed) {
                System.out.println("No directory given as first argument.");
                System.out.println("Run \"Indexer <directory>\" to index files of a directory.");
                System.exit(1);
            }
            // Getting all images from a directory and its sub directories.
            ArrayList<String> images = FileUtils.readFileLines(new File(args[0]), true);

            // Creating a CEDD document builder and indexing all files.
            GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);
            // Creating an Lucene IndexWriter
            IndexWriter iw = LuceneUtils.createIndexWriter("index", true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
            // Iterating through images building the low level features
            for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
                String imageFilePath = it.next();
                System.out.println("Indexing " + imageFilePath);
                try {
                    BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                    Document document = globalDocumentBuilder.createDocument(img, imageFilePath);
                    iw.addDocument(document);
                } catch (Exception e) {
                    System.err.println("Error reading image or indexing it.");
                    e.printStackTrace();
                }
            }
            // closing the IndexWriter
            LuceneUtils.closeWriter(iw);
            System.out.println("Finished indexing.");
        }
    }


## Sample Code - Multiple descriptors at once with Extractors
Use a new `GlobalDocumentBuilder` and add the `Extractor` classes you like, such as ...

    public class Indexer {
        public static void main(String[] args) throws IOException {
            // Checking if arg[0] is there and if it is a directory.
            boolean passed = false;
            if (args.length > 0) {
                File f = new File(args[0]);
                System.out.println("Indexing images in " + args[0]);
                if (f.exists() && f.isDirectory()) passed = true;
            }
            if (!passed) {
                System.out.println("No directory given as first argument.");
                System.out.println("Run \"Indexer <directory>\" to index files of a directory.");
                System.exit(1);
            }
            // Getting all images from a directory and its sub directories.
            ArrayList<String> images = FileUtils.readFileLines(new File(args[0]), true);

            // Creating a CEDD document builder and indexing all files.
            GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);
            // and here we add those features we want to extract in a single run:
            globalDocumentBuilder.addExtractor(FCTH.class);
            globalDocumentBuilder.addExtractor(AutoColorCorrelogram.class);
            // Creating an Lucene IndexWriter
            IndexWriter iw = LuceneUtils.createIndexWriter("index", true, LuceneUtils.AnalyzerType.WhitespaceAnalyzer);
            // Iterating through images building the low level features
            for (Iterator<String> it = images.iterator(); it.hasNext(); ) {
                String imageFilePath = it.next();
                System.out.println("Indexing " + imageFilePath);
                try {
                    BufferedImage img = ImageIO.read(new FileInputStream(imageFilePath));
                    Document document = globalDocumentBuilder.createDocument(img, imageFilePath);
                    iw.addDocument(document);
                } catch (Exception e) {
                    System.err.println("Error reading image or indexing it.");
                    e.printStackTrace();
                }
            }
            // closing the IndexWriter
            LuceneUtils.closeWriter(iw);
            System.out.println("Finished indexing.");
        }
    }


## Sample Code - Parallel Indexing
If you have multiple CPU cores you can use the parallel indexing tool. Note that with the option for the threads, you just configure the number of consumer threads. There will be a monitor thread, a main thread and a producer thread too. However, only the n consumer threads plus the one producer thread will create CPU load, with the producer just reading from storage and putting it into a queue. Note also that for indexing wioth few features the I/O poses a serious bottleneck, so you should try to use SSDs if possible.

    /**
     * Simple class showing the use of the ParallelIndexer, which uses up as much CPU as it can get.
     * @author Mathias Lux, mathias@juggle.at and Nektarios Anagnostopoulos, nek.anag@gmail.com
     */
    public class ParallelIndexing {
        public static void main(String[] args) throws IOException {
            // Checking if arg[0] is there and if it is a directory.
            boolean passed = false;
            if (args.length > 0) {
                File f = new File(args[0]);
                System.out.println("Indexing images in " + args[0]);
                if (f.exists() && f.isDirectory()) passed = true;
            }
            if (!passed) {
                System.out.println("No directory given as first argument.");
                System.out.println("Run \"ParallelIndexing <directory>\" to index files of a directory.");
                System.exit(1);
            }

            // use ParallelIndexer to index all photos from args[0] into "index" ... use 6 threads (actually 7 with the I/O thread).
            ParallelIndexer indexer = new ParallelIndexer(6, "index", args[0]);
            // use this to add you preferred builders. For now we go for CEDD, FCTH and AutoColorCorrelogram
            indexer.addExtractor(CEDD.class);
            indexer.addExtractor(FCTH.class);
            indexer.addExtractor(AutoColorCorrelogram.class);
            indexer.run();
            System.out.println("Finished indexing.");
        }
    }


## Sample Code - Parallel Indexing using multiple and different features
Parallel Indexer can be used to create index using global as well as local [features](features.md), at the same time.

    /**
     * Simple class showing the use of the ParallelIndexer, which uses up as much CPU as it can get.
     * @author Mathias Lux, mathias@juggle.at and Nektarios Anagnostopoulos, nek.anag@gmail.com
     */
    public class ParallelIndexing {
        public static void main(String[] args) throws IOException {
            // Checking if arg[0] is there and if it is a directory.
            boolean passed = false;
            if (args.length > 0) {
                File f = new File(args[0]);
                System.out.println("Indexing images in " + args[0]);
                if (f.exists() && f.isDirectory()) passed = true;
            }
            if (!passed) {
                System.out.println("No directory given as first argument.");
                System.out.println("Run \"ParallelIndexing <directory>\" to index files of a directory.");
                System.exit(1);
            }

            // use ParallelIndexer to index all photos from args[0] into "index".
            int numOfDocsForVocabulary = 500;
            Class<? extends AbstractAggregator> aggregator = BOVW.class;
            int[] numOfClusters = new int[] {128, 512};

            ParallelIndexer indexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, "index", args[0], numOfClusters, numOfDocsForVocabulary, aggregator);
            //Global
            indexer.addExtractor(CEDD.class);
            indexer.addExtractor(FCTH.class);
            indexer.addExtractor(AutoColorCorrelogram.class);
            //Local
            indexer.addExtractor(CvSurfExtractor.class);
            indexer.addExtractor(CvSiftExtractor.class);
            //Simple
            indexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSURF);
            indexer.addExtractor(JCD.class, SimpleExtractor.KeypointDetector.Random);

            indexer.run();
            System.out.println("Finished indexing.");
        }
    }

