# LIRE - Lucene Image Retrieval
LIRE (Lucene Image Retrieval) is an open source library for content based image retrieval, which means you can use LIRE to implement applications that search for images that look similar. Besides providing multiple common and state of the art retrieval mechanisms LIRE allows for easy use on multiple platforms. LIRE is actively used for research, teaching and commercial applications. Due to its modular nature it can be used on process level (e.g. index images and search) as well as on image feature level. Developers and researchers can easily extend and modify LIRE to adapt it to their needs.

An online demo can be found at http://demo-itec.uni-klu.ac.at/liredemo/
Most [recent documentation is found here at the github repo] (https://github.com/dermotte/LIRE/blob/master/src/main/docs/developer-docs/docs/index.md).

❗ **Please cite LIRE if you use it!** LIRE is open source and free, the only thing we ask for is that you cite it if you use it in your work. For references see below.

## Downloads ##
Downloads are currently hosted at: http://www.itec.uni-klu.ac.at/~mlux/lire-release/.

Nightly builds are available at http://www.itec.uni-klu.ac.at/~mlux/lire-release/lire-nightly.zip

## Getting Started ##
The developer documentation & blog are currently hosted [here in the repo](https://github.com/dermotte/LIRE/blob/master/src/main/docs/developer-docs/docs/index.md). In the developer docs common tasks are described, so take a look there if you are starting to use LIRE.

If you are very new to LIRE and just want to try out the image search functionality I recommend to start with _LireDemo_, a GUI application which lets you index and search your own photos. If you want to integrate search functions in your software, then take a look at _Lire-SimpleApplication_, which shows you the most straightforward way to use Lire. Both are available in the Downloads section. Small tutorials are available for creating an index and searching images in the wiki at http://www.semanticmetadata.net/wiki/.

If you want to setup your IDE to use LIRE, there is a Gradle build script for SimpleApplication as well as LireDemo. You can find directions and the files for setup [at the release page](https://github.com/dermotte/LIRE/releases/tag/gradle). If you want to work on the LIRE source code, I recommend starting with checking it out, installing Gradle and IntelliJ IDEA and then using IDEA to import the gradle project. Everything will be set up and ready for work.

We further highly recommend the book titled “Visual Information Retrieval using Java and LIRE”, written by Mathias Lux and Oge Marques. It’s available from Morgan & Claypool, i.e. as PDF eBook (doi:10.2200/S00468ED1V01Y201301ICR025, see [here](http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025) or on Kindle [here](http://www.amazon.de/gp/product/B00CDGMPR0/ref=as_li_tl?ie=UTF8&camp=1638&creative=6742&creativeASIN=B00CDGMPR0&linkCode=as2&tag=liluimre-21)).

[![](http://ecx.images-amazon.com/images/I/41Rot9eQLKL._SS400_.jpg)](http://www.amazon.de/gp/product/B00CDGMPR0/ref=as_li_tl?ie=UTF8&camp=1638&creative=6742&creativeASIN=B00CDGMPR0&linkCode=as2&tag=liluimre-21)

Sometimes you’re stuck with the integration of LIRE in your product, or you don’t exactly know which parameters to choose. In this case (i) we are either happy to help on the [mailing list](https://groups.google.com/forum/#!forum/lire-dev) so all LIRE users can benefit, or to (ii) offer our services for implementation, benchmarking and consulting if there is need for private conversation on LIRE. In the latter case please contact Mathias Lux.

## LIRE and Solr ##
If you are searching for the Solr plugin of LIRE ... there is one and it seems to be working. Several global features are working fine and it's based on Solr 6.4.0. It can be found at [https://github.com/dermotte/liresolr](https://github.com/dermotte/liresolr). For questions please use the [LIRE dev mailing list](https://groups.google.com/forum/#!forum/lire-dev).

## Citation ##

We kindly ask you to refer to either of the following papers in publications mentioning or employing Lire:

Mathias Lux, Savvas A. Chatzichristofis. _LIRE: Lucene Image Retrieval – An Extensible Java CBIR Library_. In proceedings of the 16th ACM International Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008 - Download paper and BibTeX [here](http://dl.acm.org/citation.cfm?id=1459577)

Mathias Lux. _Content Based Image Retrieval with LIRE_. In proceedings of the 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale, Arizona, USA, 2011 - Download paper and BibTeX [here](http://dl.acm.org/citation.cfm?id=2072432)

Mathias Lux,  Oge Marques _Visual Information Retrieval using Java and LIRE_, Morgan Claypool, 2013

## Acknowledgements ##

This work is supported by the Faculty of Technical Sciences of the Alpen-Adria-Universität Klagenfurt: http://technik.aau.at/en/
