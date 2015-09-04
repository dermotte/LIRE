# Builders
With `DocumentBuilders`, Lucene documents can be created from images, using the builders' `createDocument(BufferedImage, String)` method, in order to be added to a Lucene index. There are three main `DocumentBuilders`. The `GlobalDocumentBuilder`, the `LocalDocumentBuilder` and the `SimpleDocumentBuilder`. Each one of them supports a different [feature](features.md) type and most importantly, all of them can handle multiple `Extractors`, by using the `addExtractor` method of each `DocumentBuilder`, before creating the Lucene documents. Finally, in both the `LocalDocumentBuilder` and the `SimpleDocumentBuilder`, each `Extractor` has to be accompanied by a codebook, or a list of codebooks, which is necessary in order to aggregate the local features to a vector representation, using an [aggregator](aggregators.md).


## GlobalDocumentBuilder
Use the `GlobalDocumentBuilder` to create a `DocumentBuilder` for global features. This builder can take any `GlobalFeature` implementation and create a builder class from it.

    GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder(CEDD.class);

You can also add multiple `GlobalFeatues` using the `addExtractor`. For instance:

    GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder();
    globalDocumentBuilder.addExtractor(CEDD.class);
    globalDocumentBuilder.addExtractor(FCTH.class);
    globalDocumentBuilder.addExtractor(AutoColorCorrelogram.class);



## LocalDocumentBuilder
Use the `LocalDocumentBuilder` to create a `DocumentBuilder` for local features, like SIFT or SURF. This builder can take any `LocalFeatureExtractor` implementation accompanied by a codebook, or a list of codebooks and create a builder class from it.

    LocalDocumentBuilder localDocumentBuilder = new LocalDocumentBuilder();
    localDocumentBuilder.addExtractor(CvSurfExtractor.class, Cluster.readClusters("./src/test/resources/codebooks/CvSURF32"));


## SimpleDocumentBuilder
Finally, one more builder is available, the `SimpleDocumentBuilder` which can take any combination of a `GlobalFeatue` and a `SimpleExtractor.KeypointDetector`, in order to localize global descriptors according to [SIMPLE](simple.md).

    SimpleDocumentBuilder simpleDocumentBuilder = new SimpleDocumentBuilder();
    simpleDocumentBuilder.addExtractor(CEDD.class, SimpleExtractor.KeypointDetector.CVSURF, Cluster.readClusters("./src/test/resources/codebooks/SIMPLEdetCVSURFCEDD32"));