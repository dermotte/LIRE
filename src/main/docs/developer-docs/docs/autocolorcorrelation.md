# AutoColorCorrelation Image Feature 
This descriptor is based on the publication [Image Indexing Using Color Correlograms](http://www.cs.utah.edu/~sbasu/cbir_papers/huang97image.pdf) of Jing Huang et al. from  Cornell University and presents an alternative to the MPEG-7 descriptors. Main features:

  * It is based on the color (HSV color space).
  * It includes information upon color correlation in the image.

In my subjective opinion it provides a better color search than ScalableColor, but it is extracted more slowly (see above mentioned paper for details).

For basic LIRE usage see [search](searchindex.md]] and [index creation](createindex.md)

To use the descriptor try the 

  * ``new GlobalDocumentBuilder(AutoColorCorrelogram.class)`` factory method.

To create an appropriate searcher use 

  * ``new GenericFastImageSearcher(maximumHits, AutoColorCorrelogram.class)``

**Very important:** 

  <!-- * Ensure that you use the same options for the ImageSearcher as you used for the DocumentBuilder if you are creating the ImageSearcher manually not using the Factories. -->
  * Ensure that the analyzed image is big enough for the descriptor, otherwise the descriptor cannot be extracted.
  
## Sample code
    public void testCorrelationSearch() throws IOException {
        String[] testFiles = new String[]{"img01.JPG", "img02.JPG", "img03.JPG", "img04.JPG", "img05.JPG",
                "img06.JPG", "img07.JPG", "img08.JPG", "img08a.JPG"};
        String testFilesPath = "./src/test/resources/images/";
        String indexPath = "test-index";

        IndexReader reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        // create the appropriate correlogram searcher:
        ImageSearcher searcher = new GenericFastImageSearcher(10, AutoColorCorrelogram.class, true, reader);
        FileInputStream imageStream = new FileInputStream(testFilesPath + testFiles[0]);
        BufferedImage bimg = ImageIO.read(imageStream);
        ImageSearchHits hits = null;
        // search for the image in the index
        hits = searcher.search(bimg, reader);
        for (int i = 0; i < hits.length(); i++) {
            System.out.println(hits.score(i) + ": " + reader.document(hits.documentID(i)).getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]);
        }
    }