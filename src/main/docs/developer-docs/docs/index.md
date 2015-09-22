# Lire - Lucene Image REtrieval
The LIRE (Lucene Image REtrieval) library a simple way to create a Lucene index of image features for content based 
image retrieval (CBIR). There is no complete list of features, but these are some of them:

  * ScalableColor, ColorLayout and EdgeHistogram [MPEG-7](http://mpeg.chiariglione.org/standards/mpeg-7/mpeg-7.htm)
  * CEDD and FCTH (contributed by [Savvas Chatzichristofis](http://savvash.blogspot.com/))
  * Color histograms (HSV and RGB), Tamura & Gabor, auto color correlogram, JPEG coefficient histogram (common global descriptors)
  * Visual words based on [SIFT](http://en.wikipedia.org/wiki/Scale-invariant_feature_transform) and [SURF](http://en.wikipedia.org/wiki/SURF)
  * Visual words based on [SIMPLE](simple.md)
  * Approximate fast search based on hashing and [metric indexing](metricindexing.md).

Furthermore, methods for searching the index based on [Lucene](http://lucene.apache.org) are provided.

The LIRE library started out as part of the Caliph & Emir project and aimed to provide the CBIR features of Caliph & Emir 
to other Java projects in an easy and light weight way. In the meantime it has turned out as big and interesting project itself.

With Lire you can easily [create an index](createindex.md) and [search through the index](searchindex.md). LIRE 1.0 also supports local features based on bag of visual words and the SIMPLE approach, see [Builders](builders.md).

  * How to [create an index](createindex.md) with Lire?
  * How to [search through the index](searchindex.md) with Lire?
  * [Frequently Asked Questions](lirefaq.md)
  
I recommend to start with taking a look at the [SimpleApplication](https://github.com/dermotte/LIRE/tree/master/samples/simpleapplication)
package of LIRE, which covers the most needed stuff including indexing, search and extraction of image features for use 
in other applications. It's also a good idea to work on the current SVN version of LIRE:
[How to check out and set up LIRE in the IDEA IDE](https://www.youtube.com/watch?v=vG_yvB_UfAU&list=PLkb7TymgoWW4zfjepAmYNz03ABDQWGHfl).

Note at this point that LIRE comes with Apache Ant build files, named build.xml. You can use the tasks to create the jar
from the source code as soon as you have Ant installed, or you are using an IDE prepared for that, like IDEA, Eclipse or NetBeans.
Apache Ant can be found at the [Apache Ant Project Page](https://ant.apache.org/)

If you are searching for the Solr plugin of LIRE ... it's still under construction. Some global features are working fine 
and its based on Solr 4.10.2. It can be found at [BitBucket](https://bitbucket.org/dermotte/liresolr). It has been reported
working on distributed installations.

## Making more of LIRE
If you need **more performance** out of LIRE you can consider using approximate indexing. One option is hashing, ie. BitSampling,
the other is to use the [approximate indexing based on metric spaces](metricindexing.md) based on the work of [G. Amato](http://www.nmis.isti.cnr.it/amato/).
Both are supported by the parallel indexer and the generic searcher class, just check the configurations and constructors.

Another option is to switch to [DocValues](docvalues.md). This utilizes another data structure of Lucene and bypasses
the actual index. Please note that you need to use a custom searcher, the GenericDocValuesSearcher for searching. Indexing
can be done by the parallel indexer.

## How does Lire actually work?
Lire employs *global image features* for *content based image retrieval*. For more information on the underlying methods 
and techniques you should consult the basic literature on [content based images retrieval](http://en.wikipedia.org/wiki/CBIR):

  * [Visual Information Retrieval using Java and LIRE](http://www.amazon.com/Information-Retrieval-Synthesis-Lectures-Concepts/dp/1608459187/ref=sr_1_1?ie=UTF8&qid=1434544298&sr=8-1&keywords=lire+lux&pebp=1434544293512&perid=165GFHRF19TFTK35C3PC) (Lux & Marques, 2013)
  * [Image Retrieval: Ideas, Influences, and Trends of the New Age](http://infolab.stanford.edu/~wangz/project/imsearch/review/JOUR/datta.pdf) (Datta et al., 2008)
  * [Content-Based Image Retrieval at the End of the Early Years](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.92.889&rep=rep1&type=pdf) (Smeulders et al., 2000)

Further it uses the Java search engine Lucene to provide
 
  * linear search (opening each and every indexed document and comparing it to the query feature)
  * [approximate indexing based on metric spaces](metricindexing.md) based on the work of [G. Amato](http://www.nmis.isti.cnr.it/amato/)
  * approximate indexing based on locality sensitive hashing


## Performance
Parallel indexing with the [ParallelIndexer](createindex.md) running with 8 threads on a AMD A10 with 4 cores and 4.4 GHz, 
Windows 7 64 bits extracting 7 features at once including hashing is down to ~180 ms per image. On a Intel Core i7, ie. 
the 4770K, it runs a lot faster, using an SSD then speeds up the process even more. Extracting single features with the 
`ParallelIndexer` is on an Intel Core i7 typically faster than 1 MP images can be read from a (magnetic) hard disk. Example:
Extracting PHOG, FCTH, CEDD and OpponentHistogram and indexing with MetricSpaces hashing with 8 concurrent threads from the MIRFlickr images stored on a Samsung 
SSD with a Linux Mint 17.2, OpenJDK 1.7, and a Core i7 3770K takes ~15.15 ms per image, so roughly 4 hours and 13 
minutes for the whole MIRFlickr 1M data set or 1 million images.
        
Search is a matter of index size and number of features. Tests on CEDD with 500,000 images have shown that with cached 
search, LIRE needs around 870 ms per search, with DocValue based indexing and search it's around 630 ms per search.  
Approximate indexing is faster with more images, for 500k images and 0.72 recall it takes around 370ms per search. This 
numbers are before optimization based on query bundling, multithreading. Moreover, using L1 as a distance metric can 
reduce search time significantly. See also [here](http://www.semanticmetadata.net/2015/09/10/a-search-runtime-analysis-of-lire-on-500k-images/) 
