# Bag of Visual Words Indexing 

Lire supports creation of a bag of visual words index and search within. You need to index documents the traditional way first and you have to take care of creating the right local features. This basically means that you'll need to use the SurfDocumentBuilder to get the local features for later BoVW indexing based on SURF features.

Please not that the BoVW approach has been designed for big data sets. Therefore the implementation will not work properly with small data sets and usage will lead to errors and warnings. Please make sure that you use enough images, i.e. more than 1,000 (although it was meant to be used with millions), and please take notice of the warnings.

        public void createIndex() throws IOException {
                String indexPath = "./bovw-test"; // change if oyou want a different one.
        
                // create the initial local features:
                ChainedDocumentBuilder builder = new ChainedDocumentBuilder();
                builder.addBuilder(new SurfDocumentBuilder());
                IndexWriter iw = LuceneUtils.createIndexWriter(indexPath, true);
                ArrayList<String> images = FileUtils.getAllImages(new File("path/to/images..."), true);
                for (String identifier : images) {
                    Document doc = builder.createDocument(new FileInputStream(identifier), identifier);
                    iw.addDocument(doc);
                }
                iw.close();
        
                // create the visual words.
                IndexReader ir = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
                // create a BoVW indexer, "-1" means that half of the images in the index are
                // employed for creating the vocabulary. "100" is the number of visual words to be created.
                SurfFeatureHistogramBuilder sh = new SurfFeatureHistogramBuilder(ir, -1, 100);
                // progress monitoring is optional and opens a window showing you the progress.
                sh.setProgressMonitor(new ProgressMonitor(null, "", "", 0, 100));
                sh.index();
        }

## Bag of Visual Words Search 

With the ``VisualWordsImageSearcher`` you can search in BoVW indexes. Note that you need to take a document from the index for a query.

        public void searchIndex() {
                String indexPath = "./bovw-test";
                VisualWordsImageSearcher searcher = new VisualWordsImageSearcher(1000,
                        DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
                IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
                // let's take the first document for a query:
                Document query = reader.document(0);
                ImageSearchHits hits = searcher.search(query, reader);
                // show or analyze your results ....
        }

If you are searching for an image that is not in the index you would do something like ...

        public void searchIndex() {
                String indexPath = "./bovw-test";
                VisualWordsImageSearcher searcher = new VisualWordsImageSearcher(1000,
                        DocumentBuilder.FIELD_NAME_SURF_VISUAL_WORDS);
                IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
                
                // employed for creating the visual words for the query document. 
                // Make sure you are using the same parameters as for indexing. 
                SurfFeatureHistogramBuilder sh = new SurfFeatureHistogramBuilder(reader, -1, 100);
                // extract SURF features and create query
                SurfDocumentBuilder sb = new SurfDocumentBuilder();
                Document query = sb.createDocument(image, identifier);
                // create visual words for the query
                query = sh.getVisualWords(query);
                // search
                ImageSearchHits hits = searcher.search(query, reader);
                // show or analyze your results ....
        }

## Bag of Visual Words Incremental Update

Let's assume you have already indexed additional documents with the ``SurfDocumentBuilder``, so they are in the index, but do not have visual words attached yet. then you need to create a new (or use the old) ``SurfFeatureHistogramBuilder`` and employ the ``indexMissing()`` methods. The constructor parameters do not matter in this case as they are read from disk in the course of the ``indexMissing()`` method.