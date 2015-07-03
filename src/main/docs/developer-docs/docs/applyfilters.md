# Filters
Basically Lire allows you to create a ranked list of results based on some similarity metric along with a low level image feature. However, some times you need a filtering or re-ranking process to take place afterwards. Common use cases are if Lire search is applied to a large database on a fast but not very precise feature (e.g. hashing) and the resulting list has to be re-ranked accoring to a global feature. Re-ranking can also employ extended analysis method like [[http://en.wikipedia.org/wiki/Latent_semantic_analysis|LSA]] (latent semantic analysis).

## Using a different feature
    public void testRerankFilter() throws IOException {
        // search
        System.out.println("---< searching >-------------------------");
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        Document document = reader.document(0);
        ImageSearcher searcher = new GenericFastImageSearcher(100, AutoColorCorrelogram.class, true, reader);
        ImageSearchHits hits = searcher.search(document, reader);
        // rerank
        System.out.println("---< filtering >-------------------------");
        RerankFilter filter = new RerankFilter(ColorLayout.class, DocumentBuilder.FIELD_NAME_COLORLAYOUT);
        hits = filter.filter(hits, reader, document);

        // output
        FileUtils.saveImageResultsToHtml("filtertest", hits, document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0], reader);
    }

## Using LSA
    public void testLsaFilter() throws IOException {
        // search
        System.out.println("---< searching >-------------------------");
        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        Document document = reader.document(0);
        ImageSearcher searcher = new GenericFastImageSearcher(100, AutoColorCorrelogram.class, true, reader);
        ImageSearchHits hits = searcher.search(document, reader);
        // rerank
        System.out.println("---< filtering >-------------------------");
        LsaFilter filter = new LsaFilter(CEDD.class, DocumentBuilder.FIELD_NAME_CEDD);
        hits = filter.filter(hits, reader, document);

        // output
        FileUtils.saveImageResultsToHtml("filtertest", hits, document.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0], reader);
    }