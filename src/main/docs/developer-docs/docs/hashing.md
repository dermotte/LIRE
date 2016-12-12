# Hashing for approximate image search
If you have got hundreds of thousands of image, you might consider applying hashing to speed up search. The main approach is to assign hashes to every feature vector and then use the hashes to do a fast search for candidates, which are then re-ranked using the actual feature vectors. 

Indexing data using hashing is rather easy.

    ParallelIndexer indexer = new ParallelIndexer(
        6,          // gives the number of threads
        "index",    // the directory where your index is stored
        args[0],    // the directory where the indexer can find the images
        GlobalDocumentBuilder.HashingMode.BitSampling); // the hashing mode
    indexer.addExtractor(CEDD.class);
    indexer.addExtractor(FCTH.class);
    indexer.addExtractor(AutoColorCorrelogram.class);
    indexer.run();
    
Alternatively you can control the document using the GlobalDocumentBuilder and the constructor

    public GlobalDocumentBuilder(boolean hashing, HashingMode hashingMode, boolean useDocValues)
    
For search you should use the BitSamplingImageSearcher like in this example:

        IndexReader ir = DirectoryReader.open(FSDirectory.open(Paths.get("index")));
        ImageSearcher searcher = new BitSamplingImageSearcher(30, new CEDD());

        // searching with a image file ...
        ImageSearchHits hits = searcher.search(img, ir);
        // searching with a Lucene document instance ...
        for (int i = 0; i < hits.length(); i++) {
            String fileName = ir.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0];
            System.out.println(hits.score(i) + ": \t" + fileName);
        }