#SIMPLE Descriptors

SIMPLE [Searching Images with Mpeg-7 (& Mpeg-7 like) Powered Localized dEscriptors] begun as a collection of four descriptors [Simple-SCD, Simple-CLD, Simple-EHD and Simple-CEDD (or LoCATe)]. The main idea behind SIMPLE is to utilize global descriptors as local ones. To do this, the SURF detector is employed to define regions-of-interest on an image, and instead of using the SURF descriptor, one of the MPEG-7 SCD, the MPEG-7 CLD, the MPEG-7 EHD and the CEDD descriptors is utilized to extract the features of those image's patches. Finally, the Bag-Of-Visual-Words framework is used to test the performance of those descriptors in CBIR tasks.
Furthermore, recently SIMPLE was extended from a collection of descriptors, to a scheme (as a combination of a detector and a global descriptor). Tests have been carried out after utilizing other detectors [the SIFT detector and two Random Image Patches’ Generators (The Random Generator has produced the best results and is portrayed as the preferred choice.)] and currently the performance of that scheme with more global descriptors is being tested. You can find more details [here](http://chatzichristofis.info/?page_id=1479).

Use the `SimpleBuilder`, combined to the desired detector and descriptor, with the [ParallelIndexer](createindex.md), as follows, in order to create your index:

	ParallelIndexer indexer = new ParallelIndexer(numberOfThreads, "myIndex", "c:/temp/images/") {
		// use this to add you preferred builders.
		public void addBuilders(ChainedDocumentBuilder builder) {	
	                // builder.addBuilder(new SimpleBuilder(new CEDD(), SimpleBuilder.KeypointDetector.CVSURF));
	                // builder.addBuilder(new SimpleBuilder(new CEDD(), SimpleBuilder.KeypointDetector.CVSIFT));
					builder.addBuilder(new SimpleBuilder(new CEDD(), SimpleBuilder.KeypointDetector.Random, 600));
					// builder.addBuilder(new SimpleBuilder(new CEDD(), SimpleBuilder.KeypointDetector.GaussRandom));
					
					// builder.addBuilder(new SimpleBuilder(new ScalableColor(), SimpleBuilder.KeypointDetector.SURF));
		}
	};
	indexer.run();

You can switch between various global descriptors and the following detectors: {CVSURF, CVSIFT, Random, GaussRandom}.
Furthermore, for the Random and GaussRandom detectors you can set the number of the random patches to take as sample from each image.

As a list of features is created for each image, an extra step is required so as to create the histogram of visual words. For that step you can use the `SimpleFeatureBOVWBuilder` as follows:

	System.out.println("** SIMPLE BoVW using CEDD and CVSURF");
	SimpleFeatureBOVWBuilder simpleBovwBuilder = new SimpleFeatureBOVWBuilder(DirectoryReader.open(FSDirectory.open(new File(indexPath))), new CEDD(), SimpleBuilder.KeypointDetector.CVSURF, sampleToCreateCodebook, numberOfClusters);
	simpleBovwBuilder.index();

WARNING: What you need to do is to set the corresponding descriptor (For now CEDD is the one) and detector that were used during the extraction process.
(You can use the `SimpleFeatureVLADBuilder`, respectively, if you desire to use the VLAD representation)

After that, you can continue to the [Searching process](searchindex.md), using the `GenericFastImageSearcher` as follows:

	new GenericFastImageSearcher(numberOfHits, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new CEDD()) + DocumentBuilder.FIELD_NAME_BOVW_VECTOR, true, reader);
	
or for VLAD:

	new GenericFastImageSearcher(numberOfHits, GenericDoubleLireFeature.class, (new SimpleBuilder()).getFieldName(SimpleBuilder.KeypointDetector.CVSURF, new CEDD()) + DocumentBuilder.FIELD_NAME_VLAD_VECTOR, true, reader);
	
If you use SIMPLE please cite:
C. Iakovidou, N. Anagnostopoulos, A. Ch. Kapoutsis, Y. Boutalis and S. A. Chatzichristofis, “SEARCHING IMAGES WITH MPEG-7  (& MPEG-7 LIKE) POWERED LOCALIZED DESCRIPTORS: THE SIMPLE ANSWER TO EFFECTIVE CONTENT BASED IMAGE RETRIEVAL”, «12th International Content Based Multimedia Indexing Workshop», June 18-20 2014, Klagenfurt – Austria.