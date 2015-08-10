# Frequently Asked Questions 
If you can't find any answer to your questions please drop a mail to the [LIRE mailing list](https://groups.google.com/forum/#!forum/lire-dev). 
If you are in need for a license or need consulting, please contact me directly. 

## I don't know how to compile this. Can you help me?
I'm really sorry, but that is something I can't help you with. There are great [Java tutorials](http://docs.oracle.com/javase/tutorial/) 
available for self directed learning. Get an IDE like Eclipse , NetBeans or Idea and go through the tutorials to learn about classes, .java files, packages, jar files, imports, classpath etc.  

## What can Lire do for me?
Lire is the right choice if you want to search in a repository of raster images (ditial photos, jpegs, pngs ...) for 
similar images. It provides an easy way to (i) create an index for your images and (ii) search the index.

## How can I try Lire?
There is a GUI application, which exemplifies the use of Lire and gives you an idea what Lire can do for you. You can use it to 
index a photo collection, browse the index and retrieve similar photos. Furthermore an image mosaicing application built 
on top of Lire has been integrated. Find the application and more infos on the application here: [LIRE](http://www.semanticmetadata.net/lire/)


## How do I create an index for images using Lire? #
Please consult the following wiki page: [How to create an index with Lire?](createindex.md)

## How do I search for similar images using Lire?
Note that you will need an index for searching for similar images, so cerate an index for your image repository. Consult 
[How to create an index with Lire?](createindex.md) for detailed explanation on how to create the index. Then consult 
the following wiki page: [How to search for similar images with Lire?](searchindex.md)

## Where are the sources of Lire?
The content based image retrieval library, that comes with Lire is part of the Caliph & Emir project. The sources of 
Caliph and Emir are available at http://sourceforge.net/projects/caliph-emir/. For more 
information on Caliph & Emir visit http://www.SemanticMetadata.net.

## How many images can Lire manage to index & retrieve?
Up to a million images LIRE works fast and reliable, considering that you count in the size of the feature and the time
for extraction. Rule of thumb is: the smaller the index the faster your search. However, the size of the index is not the only criterion for
good performance: If there are many concurrent users, they will lock each other out from the index, and the average 
performance will be rather bad. Also there are -- in addition to linear search -- implementations for hashing, bag of 
visual words and metric spaces inverted indexing included in Lire. These are advanced methods requiring some more 
knowledge upon the methods from the developers, but they are well known and typically work fine with millions of images.

## Which image formats are supported by Lire?
As Lire uses the [[http://java.sun.com/javase/6/docs/api/javax/imageio/ImageIO.html|javax.imageio.ImageIO]] class all 
formats your JDK supports can be indexed by Lire. If you stick to JPG and PNG you're on the save side. As far as I know 
BMP and GIF are also supported out of the box. For Java 1.6 the list of supported formats is in the 
[[http://java.sun.com/javase/6/docs/api/javax/imageio/package-summary.html|API docs]]. In addition you can use third 
party libraries like Apache Sanselan, OpenIMAJ, Twelve Monkeys or ImageJ to decode images and create a BufferedImage 
instance, which can be handled by Lire. See also [this page](imageread.md).

## I can't find the classes from at.lux.imageanalysis. Where are they?
The classes from ``at.lux.imageanalysis`` are in the ``caliph-emir-cbir.jar`` that comes in the lib folder of the Lire 
distribution. They are part of the Caliph & Emir project and separated from the rest as they are built on top of 
other people's code. The source is available from Caliph & Emir. With newer Lire distributions these classes have been 
moved to the Lire development trunk and the external libary is no longer needed.

## Which one is the right ImageSearcher? 
This is a horribly complicated question. It heavily depends on (i) the domain of your images and (ii) the features 
you indexed (== the employed DocumentBuilder). For (i) you have to take a look at your data set and decide yourself. 
Try to implement different approaches and compare the results to each other. There are methods to measure this based on 
numbers (precision, recall, mean average precision, precision at ten. For (ii) take care that the feature you want to 
search for has been extracted and indexed. It's worth to mention that the DocumentBuilder you get from 
``DocumentBuilderFactory.getFullDocumentBuilder()`` extracts all available features from an image. Therefore one can 
use all available ``ImageSearcher`` implementations. Note that an ``ImageSearcher`` just uses one single feature for 
searching.

Other than that I offer consulting services for implementation and testing.

## Can I use multiple features for search? 
This depends very much on the way how you combine the features. You should not add or subtract relevance scores -- not 
even weighted! That's because of the fact that the metric spaces, spanned by each feature, are not compatible (no real 
scientific explanation, but still you might get the idea). Possible approaches are

  * Result list merging, where search for each feature returns its own result list and you mash them in some way (e.g. medium rank)
  * Combined filtering and ranking, where you use one feature to select 20 results and another to rank them for a ranked result list
  * Machine learning, where you use learning methods to fit parameters and select appropriate dimensions for retrieval in your image domain