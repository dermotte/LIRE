# Using DocValues for faster search

LIRE already provides classes for using DocValues for faster search & retrieval.

    private File infile = new File("testdata/images.lst");
    private String indexPath = "ms-index-docval";

    public void index() {
        // make sure you use the option useDocValues = true
        ParallelIndexer p = new ParallelIndexer(8, indexPath, infile, GlobalDocumentBuilder.HashingMode.None, true);
        p.addExtractor(CEDD.class);
        p.addExtractor(FCTH.class);
        p.addExtractor(PHOG.class);
        p.run();
    }

    public void testSearch() throws IOException {
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));

        // Note: you have to use this custom searcher!
        GenericDocValuesImageSearcher is = new GenericDocValuesImageSearcher(4, CEDD.class, reader);

        // you can search for a document like with all other searchers
        ImageSearchHits hits = is.search(reader.document(i), reader);
        for (int j =0; j< hits.length(); j++) {
            System.out.printf("%02d: %06d %02.3f\n", j + 1, hits.documentID(j), hits.score(j));
        }
        System.out.println("------< * >-------");

        // but it's even faster when you search for a document id.
        hits = is.search(i);
        for (int j =0; j< hits.length(); j++) {
            System.out.printf("%02d: %06d %02.3f\n", j + 1, hits.documentID(j), hits.score(j));
        }
        System.out.println("------< * >-------");
    }
