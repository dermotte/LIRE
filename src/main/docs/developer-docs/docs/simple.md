#SIMPLE Descriptors
SIMPLE [Searching Images with Mpeg-7 (& Mpeg-7 like) Powered Localized dEscriptors] begun as a collection of four descriptors [Simple-SCD, Simple-CLD, Simple-EHD and Simple-CEDD (or LoCATe)]. The main idea behind SIMPLE is to utilize global descriptors as local ones. To do this, the SURF detector is employed to define regions-of-interest on an image, and instead of using the SURF descriptor, one of the MPEG-7 SCD, the MPEG-7 CLD, the MPEG-7 EHD and the CEDD descriptors is utilized to extract the features of those image's patches. Finally, the Bag-Of-Visual-Words framework is used to test the performance of those descriptors in CBIR tasks.
Furthermore, recently SIMPLE was extended from a collection of descriptors, to a scheme (as a combination of a detector and a global descriptor). Tests have been carried out after utilizing other detectors [the SIFT detector and two Random Image Patches’ Generators (The Random Generator has produced the best results and is portrayed as the preferred choice.)] and currently the performance of that scheme with more global descriptors is being tested. You can find more details [here](http://chatzichristofis.info/?page_id=1479).

Use a combination of the desired `GlobalFeatue` and `SimpleExtractor.KeypointDetector`, with the [ParallelIndexer](createindex.md), as follows, in order to create your index:

            ParallelIndexer parallelIndexer = new ParallelIndexer(DocumentBuilder.NUM_OF_THREADS, "test-index", "testdata/ferrari");
            //parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSURF);
            //parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSIFT);
            parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.Random);
            //parallelIndexer.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.GaussRandom);

            parallelIndexer.run();

You can switch between various global descriptors and the following detectors: {CVSURF, CVSIFT, Random, GaussRandom}.

After that, you can continue to the [Searching process](searchindex.md), using the `GenericFastImageSearcher` as follows:

	new GenericFastImageSearcher(10, CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new BOVW(), numOfClusters, true, reader, indexPath + ".config")
	
or for VLAD:

	new GenericFastImageSearcher(10, CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, new VLAD(), numOfClusters, true, reader, indexPath + ".config")
	
If you use SIMPLE please cite:
C. Iakovidou, N. Anagnostopoulos, A. Ch. Kapoutsis, Y. Boutalis and S. A. Chatzichristofis, “SEARCHING IMAGES WITH MPEG-7  (& MPEG-7 LIKE) POWERED LOCALIZED DESCRIPTORS: THE SIMPLE ANSWER TO EFFECTIVE CONTENT BASED IMAGE RETRIEVAL”, «12th International Content Based Multimedia Indexing Workshop», June 18-20 2014, Klagenfurt – Austria.