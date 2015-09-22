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
        MetricSpaces.indexReferencePoints(CEDD.class, 2000, 50, new File(infile), new File("dir1.cedd.dat"));
        MetricSpaces.indexReferencePoints(FCTH.class, 2000, 50, new File(infile), new File("dir1.fcth.dat"));
        MetricSpaces.indexReferencePoints(PHOG.class, 2000, 50, new File(infile), new File("dir1.phog.dat"));
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

Search can be configured at runtime to be either faster or more accurate. There are two parameters. One defines the number
of results that are retrieved from the index with metric indexing for re-ranking. This is done with the constructor. The
second one defines the length of the query. The default value is that the 25 nearest reference points are used. This is
done with a setter.

Search is as easy as:

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get("ms-index")));
        // set up the searcher, here 120 candidate results are pulled from the index, and then re-ranked.
        MetricSpacesImageSearcher is = new MetricSpacesImageSearcher(10, new File("dir.cedd.dat"), 100);
        // set up the query length, default is 25, more than 45 will result in a crash.
        is.setNumHashesUsedForQuery(20); // this is optional
        // search
        ImageSearchHits hits = is.search(reader.document(i), reader);
        // print results
        for (int j =0; j< hits.length(); j++) {
            System.out.printf("%02d: %06d %02.3f\n", j + 1, hits.documentID(j), hits.score(j));
        }

## Experimental results
Using the MIRFlickr data set with 1 million digital photos we tested search performance. We used 1,000 reference points, 
25 reference points for indexing and search and re-ordered 10,000 results.   

Time for MetricSpaces vs. cached linear search, giving the precision for 100 runs with TF*IDF:

* CEDD: : 72.788 vs. 213.069 seconds for 100 runs at 0.81 recall
* FCTH: 70.397 vs. 167.995 seconds for 100 runs at 0.85 recall
* PHOG: 90.461 vs. 194.189 seconds for 100 runs at 0.57 recall
* OpponentHistogram: 93.684 vs. 146.364 seconds for 100 runs at 0.88 recall

with BaseSimilarity:

* CEDD: 80.537 vs. 239.259 seconds for 100 runs at 0.79 recall
* FCTH: 83.118 vs. 189.970 seconds for 100 runs at 0.84 recall
* PHOG: 74.270 vs. 166.404 seconds for 100 runs at 0.60 recall
* OpponentHistogram: 87.038 vs. 141.582 seconds for 100 runs at 0.85 recall

Using 10,000 reference points and 50 points for indexing and querying, it comes to

Time for MetricSpaces vs. cached linear search, giving the precision for 100 runs with BaseSimilarity:

* CEDD: 93.542 vs. 217.512 seconds for 100 runs at 0.85 recall
* PHOG: 89.676 vs. 161.016 seconds for 100 runs at 0.71 recall