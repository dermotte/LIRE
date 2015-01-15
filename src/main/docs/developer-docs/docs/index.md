# Lire - Lucene Image REtrieval

The LIRE (Lucene Image REtrieval) library a simple way to create a Lucene index of image features for content based 
image retrieval (CBIR). There is no complete list of features, but these are some of them:

  * ScalableColor, ColorLayout and EdgeHistogram [MPEG-7](http://mpeg.chiariglione.org/standards/mpeg-7/mpeg-7.htm)
  * CEDD and FCTH (contributed by [Savvas Chatzichristofis](http://savvash.blogspot.com/))
  * Color histograms (HSV and RGB), Tamura & Gabor, auto color correlogram, JPEG coefficient histogram (common global descriptors)
  * Visual words based on [SIFT](http://en.wikipedia.org/wiki/Scale-invariant_feature_transform) and [SURF](http://en.wikipedia.org/wiki/SURF)
  * Approximate fast search based on hashing and metric indexing.

Furthermore, methods for searching the index based on [[http://lucene.apache.org|Lucene]] are provided.

The LIRE library started out as part of the Caliph & Emir project and aimed to provide the CBIR features of Caliph & Emir 
to other Java projects in an easy and light weight way. In the meantime it has turned out as big and interesting project itself.

With Lire you can easily [create an index](createindex.md) and [search through the index](searchindex.md).

  * How to [create an index](createindex.md) with Lire?
  * How to [search through the index](searchindex.md) with Lire?
  * [Frequently Asked Questions](lirefaq.md)
  
I recommend to start with taking a look at the [SimpleApplication](https://code.google.com/p/lire/source/browse/#svn%2Ftrunk%2Fsamples%2Fsimpleapplication%253Fstate%253Dclosed) 
package of LIRE, which covers the most needed stuff including indexing, search and extraction of image features for use 
in other applications.    

If you are searching for the Solr plugin of LIRE ... it's still under construction. Some global features are working fine 
and its based on Solr 4.10.2. It can be found at [BitBucket](https://bitbucket.org/dermotte/liresolr)

## How does Lire actually work?

Lire employs *global image features* for *content based image retrieval*. For more information on the underlying methods 
and techniques you should consult the basic literature on [content based images retrieval](http://en.wikipedia.org/wiki/CBIR):

  * [Image Retrieval: Ideas, Influences, and Trends of the New Age](http://infolab.stanford.edu/~wangz/project/imsearch/review/JOUR/datta.pdf) (Datta et al., 2008)
  * [Content-Based Image Retrieval at the End of the Early Years](http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.92.889&rep=rep1&type=pdf) (Smeulders et al., 2000)

Further it uses the Java search engine Lucene to provide
 
  * linear search (opening each and every indexed document and comparing it to the query feature)
  * approximate indexing based on metric spaces based on the work of [G. Amato](http://www.nmis.isti.cnr.it/amato/)
  * approximate indexing based on locality sensitive hashing


## Performance

Parallel indexing with the [ParallelIndexer](createindex.md) running with 8 threads on a AMD A10 with 4 cores and 4.4 GHz, 
Windows 7 64 bits extracting 7 features at once including hashing is down to ~180 ms per image. On a Intel Core i7, ie. 
the 4770K, it runs a lot faster, using an SSD then speeds up the process even more. Extracting single features with the 
ParallelIndexer is on a core i7 typically faster than 1 MP images can be read from a (magnetic) hard disk.
        
Search is a matter of index size and is down to a few ms for 100k and less images, and increases linearly. 
