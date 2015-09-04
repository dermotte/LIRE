# Metric Spaces based Indexing

Using the Metric Spaces indexing approach one can significantly speed up search. The main idea is to, instead of 
searching through the feature vectors, to index the nearest neighbours and use inverted lists (== Lucene text search)
to then pre-select candidate results first. A complete description is given in 

*Gennaro, Claudio, et al. "An approach to content-based image retrieval based on the Lucene search engine library." 
Research and Advanced Technology for Digital Libraries. Springer Berlin Heidelberg, 2010. 55-66.*

The approach is implemented like a hashing routine, so you have to create the reference points before actually indexing 
your image data. 

    // a list of images, one per line.
    String infile = "testdata/images.lst";

    // create reference points for three features and store them in files. 
    public void testHashIndexing() throws IllegalAccessException, IOException, InstantiationException {
        MetricSpaces.index(CEDD.class, 2000, 50, new File(infile), new File("dir1.cedd.dat"));
        MetricSpaces.index(FCTH.class, 2000, 50, new File(infile), new File("dir1.fcth.dat"));
        MetricSpaces.index(PHOG.class, 2000, 50, new File(infile), new File("dir1.phog.dat"));
    }
    
Indexing then is done with the `ParallelIndexer` with the use of `MetricSpaces`.

    public void indexing() throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        // first load the pre-created reference point files. 
        // It's stored in static class space, so you only need to do it once per VM
        // Only features you load are supported, for all other Extractors only the features will be indexed. 
        MetricSpaces.loadReferencePoints(new File("dir.cedd.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.fcth.dat"));
        MetricSpaces.loadReferencePoints(new File("dir.phog.dat"));

        // then use ParallelIndexer and state MetricSpaces as approach
        ParallelIndexer p = new ParallelIndexer(6, "ms-index-yahoo-gc-1.5M", new File(infile), GlobalDocumentBuilder.HashingMode.MetricSpaces);
        
        // add the Extractors
        p.addExtractor(CEDD.class);
        p.addExtractor(FCTH.class);
        p.addExtractor(PHOG.class);
        
        // index your data.
        p.run();
    }