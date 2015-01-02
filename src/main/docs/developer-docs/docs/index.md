# Lire - Lucene Image REtrieval

The LIRE (Lucene Image REtrieval) library a simple way to create a Lucene index of image features for content based image retrieval (CBIR). The implemented features are 

  * ScalableColor, ColorLayout and EdgeHistogram [MPEG-7](http://mpeg.chiariglione.org/standards/mpeg-7/mpeg-7.htm)
  * CEDD and FCTH (contributed by [Savvas Chatzichristofis](http://savvash.blogspot.com/))
  * Color histograms (HSV and RGB), Tamura & Gabor, auto color correlogram, JPEG coefficient histogram (common global descriptors)
  * Visual words based on [SIFT](http://en.wikipedia.org/wiki/Scale-invariant_feature_transform) and [SURF](http://en.wikipedia.org/wiki/SURF)

Furthermore methods for searching the index based on [[http://lucene.apache.org|Lucene]] are provided.

The LIRE library started out as part of the Caliph & Emir project and aimed to provide the CBIR features of Caliph & Emir to other Java projects in an easy and light weight way. In the meantime it has turned out as big and interesting projekt itself.

With Lire you can easily [create an index](createindex.md) and [search through the index](searchindex.md).

  * How to [create an index](createindex.md) with Lire?
  * How to [search through the index](searchindex.md) with Lire?
  * [Frequently Asked Questions](lirefaq.md)

## How does Lire actually work?

Lire employs *global image features* for *content based image retrieval*. For more information on the underlying methods and techniques you should consult the basic literature on [[http://en.wikipedia.org/wiki/CBIR|content based images retrieval]]:

  * [Image Retrieval: Ideas, Influences, and Trends of the New Age](http://infolab.stanford.edu/~wangz/project/imsearch/review/JOUR/datta.pdf) (Datta et al., 2008)
  * [Content-Based Image Retrieval at the End of the Early Years](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.92.889&rep=rep1&type=pdf) (Smeulders et al., 2000)

Further it uses the Java search engine Lucene to provide
 
  * linear search (opening each and every indexed document and comparing it to the query feature)
  * approximate indexing based on metric spaces based on the work of [G. Amato](http://www.nmis.isti.cnr.it/amato/)
  * approximate indexing based on locality sensitive hashing


## Performance
The performance of Lire has been tested initially with a test data set consisting of 3890 mixed size digital photos (1-2 MP) on an AMD Athlon XP 2600, 1GB RAM, JSDK 1.5.0_05 running Windows XP. Parameters for the Java VM were ''-server -Xms256M -Xmx512M''.

### Test A
Creation (FastDocumentBuilder):

  * 1200 seconds for all files
  * 308 ms per image on average

Searching with default Searcher ... (averaged on 50 searches)

  * BufferedImage as input: 341 ms per search
  * Document as input: 64 ms per search

### Test B
Creation (with ExtensiveDocumentBuilder):

    * 2813 seconds for all files
    * 723 ms per image on average (*note: this is outdated as the number of features has increased with v0.6*)

Searching with default Searcher on this index B (averaged on 50 searches)

    * ''BufferedImage'' as input: 589 ms per search
    * ''Document'' as input: 100 ms per search
