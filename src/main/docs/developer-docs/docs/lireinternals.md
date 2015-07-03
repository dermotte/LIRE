# Lire Internals
In general Lire just takes numeric images descriptors, which are mainly vectors or sets of vectors, and stores them inside a Lucene index as text along with the image path within a Lucene document. So its like a very primitive database. A (simplified) example would look like this: Assuming that an image has a dominant color the RGB values of the dominant color are stored in Lucene. The index for 3 photos could look like this:

  * document1 = {path=photo1.jpg, dominantcolor=120,12,34}
  * document2 = {path=photo2.jpg, dominantcolor=128,244,95}
  * document3 = {path=photo3.jpg, dominantcolor=1,39,232}

An IndexSearcher in case of a search opens every single document within the index, parses the vector and compares it to the query vector (e.g. with an L1 distance). The best matching documents are stored in a result vector along with the distance.

## Implementation Details
Available vectors (descriptors capturing image characteristic in numbers like the RGB values in above example) for the images implement the ``at.lux.imageanalysis.VisualDescriptor`` interface. The ``AutoColorCorrelogram`` descriptor is part of LIRE, while the other three available descriptors are part of the ``caliph-emir-cbir.jar`` library developed within Caliph & Emir. The descriptors provide (i) *extraction* of the numbers from a raster image, (ii) a *byte representation* as well as a parser for the very same representation for the index and (iii) a *metric* for the searcher.

The DocumentBuilders implementing the ``DocumentBuilder`` interface then allow the creation of Lucene documents from raster images wrapping the use of the descriptors. The Searchers implementing the ``ImageSearcher`` interface are responsible for search do most of the work described in above example:
  - Open every single document
  - Parse the numbers 
  - Compute the relevance
  - Update the result list

*Why is Lucene used and not a data base?* The answer is quite simple: A database brings along a lot of overhead processes and structures not needed for search like the database server, user management, access management, transaction management, index structures etc. Lucene on the other hand does not need a database server and offers fast disk access and is directly built for searching.
