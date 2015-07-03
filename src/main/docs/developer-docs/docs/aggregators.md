# Aggregators
Lire supports indexing and searching using local features. When local features are extracted from an image, those features need to be aggregated, in order to create the vector representation of that image. Use the `createVectorRepresentation()` method of the `BOVW` or the `VLAD` aggregator, from the `net.semanticmetadata.lire.aggregators` package, so as to create the vector representation using an image's list of local features and a precomputed codebook, according to the BOVW and VLAD models respectively. After creating the vector representation, using the methods `getVectorRepresentation`, `getByteVectorRepresentation` you can get the vector in double[] or byte[] format, respectively.

## Sample Code for extracting and aggregating local Features
    public void testAggregate() throws IOException {
        String codebookPath = "./src/test/resources/codebooks/";
        String imagePath = "./src/test/resources/images/";

        LocalFeatureExtractor localFeatureExtractor = new CvSurfExtractor();
        Aggregator aggregator = new BOVW();
        Cluster[] codebook = Cluster.readClusters(codebookPath + "CvSURF128");

        ArrayList<String> images = FileUtils.readFileLines(new File(imagePath), true);
        BufferedImage image;
        double[] featureVector;
        List<? extends LocalFeature> listOfLocalFeatures;
        for (String path : images) {
            image = ImageIO.read(new FileInputStream(path));
            localFeatureExtractor.extract(image);
            listOfLocalFeatures = localFeatureExtractor.getFeatures();
            aggregator.createVectorRepresentation(listOfLocalFeatures, codebook);
            featureVector = aggregator.getVectorRepresentation();

            System.out.println(path.substring(path.lastIndexOf('\\') + 1) + " ~ " + Arrays.toString(featureVector));
        }
    }