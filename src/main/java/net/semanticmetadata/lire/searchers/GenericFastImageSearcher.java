/*
 * This file is part of the LIRE project: http://lire-project.net
 * LIRE is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRE is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRE; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the any or one of the following publications in
 * any publication mentioning or employing Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 * URL: http://doi.acm.org/10.1145/1459359.1459577
 *
 * Lux Mathias. Content Based Image Retrieval with LIRE. In proceedings of the
 * 19th ACM International Conference on Multimedia, pp. 735-738, Scottsdale,
 * Arizona, USA, 2011
 * URL: http://dl.acm.org/citation.cfm?id=2072432
 *
 * Mathias Lux, Oge Marques. Visual Information Retrieval using Java and LIRE
 * Morgan & Claypool, 2013
 * URL: http://www.morganclaypool.com/doi/abs/10.2200/S00468ED1V01Y201301ICR025
 *
 * Copyright statement:
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 18.01.15 07:31
 */
package net.semanticmetadata.lire.searchers;

import net.semanticmetadata.lire.aggregators.Aggregator;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.builders.GlobalDocumentBuilder;
import net.semanticmetadata.lire.builders.LocalDocumentBuilder;
import net.semanticmetadata.lire.builders.SimpleDocumentBuilder;
import net.semanticmetadata.lire.classifiers.Cluster;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import net.semanticmetadata.lire.indexers.parallel.ExtractorItem;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.util.Bits;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

/**
 * Created by mlux on 01/02/2006.
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class GenericFastImageSearcher extends AbstractImageSearcher {
    protected Logger logger = Logger.getLogger(getClass().getName());
    protected String fieldName, codebookName;
    protected LireFeature cachedInstance = null;
    protected ExtractorItem extractorItem;
    protected boolean isCaching = false;

    protected LinkedHashMap<Integer, byte[]> featureCache = null;
    protected IndexReader reader = null;

    protected int maxHits = 50;
    protected TreeSet<SimpleResult> docs = new TreeSet<SimpleResult>();
    protected double maxDistance;
    protected boolean useSimilarityScore = false;

    Aggregator aggregator;
    private String codebooksDir;

    protected LinkedBlockingQueue<Map.Entry<Integer, byte[]>> queue = new LinkedBlockingQueue<Map.Entry<Integer, byte[]>>(100);
    protected int numThreads = DocumentBuilder.NUM_OF_THREADS;


    public GenericFastImageSearcher(int maxHits, Class<? extends GlobalFeature> globalFeature) {
        this.maxHits = maxHits;
        this.extractorItem = new ExtractorItem(globalFeature);
        this.fieldName = extractorItem.getFieldName();
        try {
            this.cachedInstance = (GlobalFeature)extractorItem.getExtractorInstance().getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        init();
    }

    public GenericFastImageSearcher(int maxHits, Class<? extends LocalFeatureExtractor> localFeatureExtractor, Aggregator aggregator, int codebookSize, String codebooksDir) {
        this.maxHits = maxHits;
        this.codebooksDir = codebooksDir;
        this.extractorItem = new ExtractorItem(localFeatureExtractor);
        this.fieldName = extractorItem.getFieldName() + aggregator.getFieldName() + codebookSize;
        try {
            this.cachedInstance = ((LocalFeatureExtractor)extractorItem.getExtractorInstance()).getClassOfFeatures().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.aggregator = aggregator;
        init();
    }

    public GenericFastImageSearcher(int maxHits, Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector detector, Aggregator aggregator, int codebookSize, String codebooksDir) {
        this.maxHits = maxHits;
        this.codebooksDir = codebooksDir;
        this.extractorItem = new ExtractorItem(globalFeatureClass, detector);
        this.fieldName = extractorItem.getFieldName() + aggregator.getFieldName() + codebookSize;
        try {
            this.cachedInstance = ((SimpleExtractor)extractorItem.getExtractorInstance()).getClassOfFeatures().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.aggregator = aggregator;
        init();
    }

    public GenericFastImageSearcher(int maxHits, Class<? extends GlobalFeature> globalFeature, boolean isCaching, IndexReader reader) {
        this.maxHits = maxHits;
        this.extractorItem = new ExtractorItem(globalFeature);
        this.fieldName = extractorItem.getFieldName();
        try {
            this.cachedInstance = (GlobalFeature)extractorItem.getExtractorInstance().getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.isCaching = isCaching;
        this.reader = reader;
        init();
    }

    public GenericFastImageSearcher(int maxHits, Class<? extends LocalFeatureExtractor> localFeatureExtractor, Aggregator aggregator, int codebookSize, boolean isCaching, IndexReader reader, String codebooksDir) {
        this.maxHits = maxHits;
        this.codebooksDir = codebooksDir;
        this.extractorItem = new ExtractorItem(localFeatureExtractor);
        this.fieldName = extractorItem.getFieldName() + aggregator.getFieldName() + codebookSize;
        this.codebookName = extractorItem.getFieldName() + codebookSize;
        try {
            this.cachedInstance = ((LocalFeatureExtractor)extractorItem.getExtractorInstance()).getClassOfFeatures().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.isCaching = isCaching;
        this.reader = reader;
        this.aggregator = aggregator;
        init();
    }

    public GenericFastImageSearcher(int maxHits, Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector detector, Aggregator aggregator, int codebookSize, boolean isCaching, IndexReader reader, String codebooksDir) {
        this.maxHits = maxHits;
        this.codebooksDir = codebooksDir;
        this.extractorItem = new ExtractorItem(globalFeatureClass, detector);
        this.fieldName = extractorItem.getFieldName() + aggregator.getFieldName() + codebookSize;
        this.codebookName = extractorItem.getFieldName() + codebookSize;
        try {
            this.cachedInstance = ((SimpleExtractor)extractorItem.getExtractorInstance()).getClassOfFeatures().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.isCaching = isCaching;
        this.reader = reader;
        this.aggregator = aggregator;
        init();
    }

    public GenericFastImageSearcher(int maxHits, Class<? extends GlobalFeature> globalFeature, boolean isCaching, IndexReader reader, boolean useSimilarityScore) {
        this.maxHits = maxHits;
        this.extractorItem = new ExtractorItem(globalFeature);
        this.fieldName = extractorItem.getFieldName();
        try {
            this.cachedInstance = (GlobalFeature)extractorItem.getExtractorInstance().getClass().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.useSimilarityScore = useSimilarityScore;
        this.isCaching = isCaching;
        this.reader = reader;
        init();
    }

    public GenericFastImageSearcher(int maxHits, Class<? extends LocalFeatureExtractor> localFeatureExtractor, Aggregator aggregator, int codebookSize, boolean isCaching, IndexReader reader, boolean useSimilarityScore, String codebooksDir) {
        this.maxHits = maxHits;
        this.codebooksDir = codebooksDir;
        this.extractorItem = new ExtractorItem(localFeatureExtractor);
        this.fieldName = extractorItem.getFieldName() + aggregator.getFieldName() + codebookSize;
        this.codebookName = extractorItem.getFieldName() + codebookSize;
        try {
            this.cachedInstance = ((LocalFeatureExtractor)extractorItem.getExtractorInstance()).getClassOfFeatures().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.useSimilarityScore = useSimilarityScore;
        this.isCaching = isCaching;
        this.reader = reader;
        this.aggregator = aggregator;
        init();
    }

    public GenericFastImageSearcher(int maxHits, Class<? extends GlobalFeature> globalFeatureClass, SimpleExtractor.KeypointDetector detector, Aggregator aggregator, int codebookSize, boolean isCaching, IndexReader reader, boolean useSimilarityScore, String codebooksDir) {
        this.maxHits = maxHits;
        this.codebooksDir = codebooksDir;
        this.extractorItem = new ExtractorItem(globalFeatureClass, detector);
        this.fieldName = extractorItem.getFieldName() + aggregator.getFieldName() + codebookSize;
        this.codebookName = extractorItem.getFieldName() + codebookSize;
        try {
            this.cachedInstance = ((SimpleExtractor)extractorItem.getExtractorInstance()).getClassOfFeatures().newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        this.useSimilarityScore = useSimilarityScore;
        this.isCaching = isCaching;
        this.reader = reader;
        this.aggregator = aggregator;
        init();
    }


    protected void init() {
        // put all respective features into an in-memory cache ...
        if (isCaching && reader != null) {
            Bits liveDocs = MultiFields.getLiveDocs(reader);
            int docs = reader.numDocs();
            featureCache = new LinkedHashMap<Integer, byte[]>(docs);
            try {
                Document d;
                for (int i = 0; i < docs; i++) {
                    if (!(reader.hasDeletions() && !liveDocs.get(i))) {
                        d = reader.document(i);
                        if (d.getField(fieldName) != null) {
                            cachedInstance.setByteArrayRepresentation(d.getField(fieldName).binaryValue().bytes, d.getField(fieldName).binaryValue().offset, d.getField(fieldName).binaryValue().length);
//                        featureCache.put(i, new SearchItem(cachedInstance.getByteArrayRepresentation(), new SimpleResult(-1d, i, d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0])));
//                        featureCache.put(i, new SearchItem(i, cachedInstance.getByteArrayRepresentation(), d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0]));
                            featureCache.put(i, cachedInstance.getByteArrayRepresentation());
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * @param reader
     * @param lireFeature
     * @return the maximum distance found for normalizing.
     * @throws IOException
     */
    protected double findSimilar(IndexReader reader, LireFeature lireFeature) throws IOException {
        maxDistance = -1d;

        // clear result set ...
        docs.clear();
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        Document d;
        double tmpDistance;
        int docs = reader.numDocs();
        if (!isCaching) {
            // we read each and every document from the index and then we compare it to the query.
            for (int i = 0; i < docs; i++) {
                if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.

                d = reader.document(i);
                tmpDistance = getDistance(d, lireFeature);
                assert (tmpDistance >= 0);
                // if the array is not full yet:
                if (this.docs.size() < maxHits) {
                    this.docs.add(new SimpleResult(tmpDistance, i));
                    if (tmpDistance > maxDistance) maxDistance = tmpDistance;
                } else if (tmpDistance < maxDistance) {
                    // if it is nearer to the sample than at least on of the current set:
                    // remove the last one ...
                    this.docs.remove(this.docs.last());
                    // add the new one ...
                    this.docs.add(new SimpleResult(tmpDistance, i));
                    // and set our new distance border ...
                    maxDistance = this.docs.last().getDistance();
                }
            }
        } else {
            LinkedList<Consumer> tasks = new LinkedList<Consumer>();
            LinkedList<Thread> threads = new LinkedList<Thread>();
            Consumer consumer;
            Thread thread;
            Thread p = new Thread(new Producer());
            p.start();
            for (int i = 0; i < numThreads; i++) {
                consumer = new Consumer(lireFeature);
                thread = new Thread(consumer);
                thread.start();
                tasks.add(consumer);
                threads.add(thread);
            }
            for (Thread next : threads) {
                try {
                    next.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            TreeSet<SimpleResult> tmpDocs;
            boolean flag;
            SimpleResult simpleResult;
            for (Consumer task : tasks) {
                tmpDocs = task.getResult();
                flag = true;
                while (flag && (tmpDocs.size() > 0)){
                    simpleResult = tmpDocs.pollFirst();
                    if (this.docs.size() < maxHits) {
                        this.docs.add(simpleResult);
                        if (simpleResult.getDistance() > maxDistance) maxDistance = simpleResult.getDistance();
                    } else if (simpleResult.getDistance() < maxDistance) {
//                        this.docs.remove(this.docs.last());
                        this.docs.pollLast();
                        this.docs.add(simpleResult);
                        maxDistance = this.docs.last().getDistance();
                    } else flag = false;
                }
            }
        }
        return maxDistance;
    }

    class Producer implements Runnable {

        private Producer() {
            queue.clear();
        }

        public void run() {
            for (Map.Entry<Integer, byte[]> documentEntry : featureCache.entrySet()) {
                try {
                    queue.put(documentEntry);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LinkedHashMap<Integer, byte[]> tmpMap = new LinkedHashMap<Integer, byte[]>(numThreads * 3);
            for (int i = 1; i < numThreads * 3; i++)  {
                tmpMap.put(-i, null);
            }
            for (Map.Entry<Integer, byte[]> documentEntry : tmpMap.entrySet()) {
                try {
                    queue.put(documentEntry);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private class Consumer implements Runnable {
        private boolean locallyEnded = false;
        private TreeSet<SimpleResult> localDocs  = new TreeSet<SimpleResult>();
        private LireFeature localCachedInstance;
        private LireFeature localLireFeature;

        private Consumer(LireFeature lireFeature) {
            try {
                this.localCachedInstance = cachedInstance.getClass().newInstance();
                this.localLireFeature = lireFeature.getClass().newInstance();
                this.localLireFeature.setByteArrayRepresentation(lireFeature.getByteArrayRepresentation());
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            Map.Entry<Integer, byte[]> tmp;
            double tmpDistance;
            double localMaxDistance = -1d;
            while (!locallyEnded) {
                try {
                    tmp = queue.take();
                    if (tmp.getKey() < 0 ) locallyEnded = true;
                    if (!locallyEnded) {    // && tmp != -1
                        localCachedInstance.setByteArrayRepresentation(tmp.getValue());
                        tmpDistance = localLireFeature.getDistance(localCachedInstance);
                        assert (tmpDistance >= 0);
                        // if the array is not full yet:
                        if (localDocs.size() < maxHits) {
                            localDocs.add(new SimpleResult(tmpDistance, tmp.getKey()));
                            if (tmpDistance > localMaxDistance) localMaxDistance = tmpDistance;
                        } else if (tmpDistance < localMaxDistance) {
                            // if it is nearer to the sample than at least on of the current set:
                            // remove the last one ...
//                            localDocs.remove(localDocs.last());
                            localDocs.pollLast();
                            // add the new one ...
                            localDocs.add(new SimpleResult(tmpDistance, tmp.getKey()));
                            // and set our new distance border ...
                            localMaxDistance = localDocs.last().getDistance();
                        }
                    }
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
        }

        public TreeSet<SimpleResult> getResult() {
            return localDocs;
        }
    }

    /**
     * Main similarity method called for each and every document in the index.
     *
     * @param document
     * @param lireFeature
     * @return the distance between the given feature and the feature stored in the document.
     */
    protected double getDistance(Document document, LireFeature lireFeature) {
        if (document.getField(fieldName).binaryValue() != null && document.getField(fieldName).binaryValue().length > 0) {
            cachedInstance.setByteArrayRepresentation(document.getField(fieldName).binaryValue().bytes, document.getField(fieldName).binaryValue().offset, document.getField(fieldName).binaryValue().length);
            return lireFeature.getDistance(cachedInstance);
        } else {
            logger.warning("No feature stored in this document! (" + extractorItem.getExtractorClass().getName() + ")");
        }
        return 0d;
    }

    /*
    // This is an approach based on DocValues. It's extremely fast, even without caching, but I don't know if it's in
    // RAM or not, ie. if I can fill up RAM with all documents at once.
    public ImageSearchHits search(int doc, IndexReader reader) throws IOException {
        SimpleImageSearchHits searchHits = null;
        LireFeature lireFeature = extractorItem.getFeatureInstance();
        BinaryDocValues binaryValues = MultiDocValues.getBinaryValues(reader, lireFeature.getFieldName());
        lireFeature.setByteArrayRepresentation(binaryValues.get(doc).bytes, binaryValues.get(doc).offset, binaryValues.get(doc).length);
        double maxDistance = findSimilar(reader, lireFeature);

        if (!useSimilarityScore) {
            searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
        } else {
            searchHits = new SimpleImageSearchHits(this.docs, maxDistance, useSimilarityScore);
        }
        return searchHits;
    }
    */

    public ImageSearchHits search(Document doc, IndexReader reader) throws IOException {
        SimpleImageSearchHits searchHits = null;
//        try {
        LireFeature lireFeature = extractorItem.getFeatureInstance();

        if (doc.getField(fieldName).binaryValue() != null && doc.getField(fieldName).binaryValue().length > 0)
            lireFeature.setByteArrayRepresentation(doc.getField(fieldName).binaryValue().bytes, doc.getField(fieldName).binaryValue().offset, doc.getField(fieldName).binaryValue().length);
        double maxDistance = findSimilar(reader, lireFeature);

        if (!useSimilarityScore) {
            searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
        } else {
            searchHits = new SimpleImageSearchHits(this.docs, maxDistance, useSimilarityScore);
        }
//        } catch (InstantiationException e) {
//            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
//        } catch (IllegalAccessException e) {
//            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
//        }
        return searchHits;
    }

    public ImageSearchHits search(BufferedImage image, IndexReader reader) throws IOException {
        logger.finer("Starting extraction.");
        SimpleImageSearchHits searchHits = null;

        if (extractorItem.isGlobal()){
            GlobalDocumentBuilder globalDocumentBuilder = new GlobalDocumentBuilder();
            GlobalFeature globalFeature = globalDocumentBuilder.extractGlobalFeature(image, (GlobalFeature) extractorItem.getExtractorInstance());

            double maxDistance = findSimilar(reader, globalFeature);
            if (!useSimilarityScore) {
                searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
            } else {
                searchHits = new SimpleImageSearchHits(this.docs, maxDistance, useSimilarityScore);
            }
        } else if (extractorItem.isLocal()){
            LocalDocumentBuilder localDocumentBuilder = new LocalDocumentBuilder();
            LocalFeatureExtractor localFeatureExtractor = localDocumentBuilder.extractLocalFeatures(image, (LocalFeatureExtractor) extractorItem.getExtractorInstance());
            aggregator.createVectorRepresentation(localFeatureExtractor.getFeatures(), Cluster.readClusters(codebooksDir + File.separator + codebookName));
            extractorItem.getFeatureInstance().setByteArrayRepresentation(aggregator.getByteVectorRepresentation());

            double maxDistance = findSimilar(reader, extractorItem.getFeatureInstance());
            if (!useSimilarityScore) {
                searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
            } else {
                searchHits = new SimpleImageSearchHits(this.docs, maxDistance, useSimilarityScore);
            }
        } else if (extractorItem.isSimple()){
            SimpleDocumentBuilder simpleDocumentBuilder = new SimpleDocumentBuilder();
            LocalFeatureExtractor localFeatureExtractor = simpleDocumentBuilder.extractLocalFeatures(image, (LocalFeatureExtractor) extractorItem.getExtractorInstance());
            aggregator.createVectorRepresentation(localFeatureExtractor.getFeatures(), Cluster.readClusters(codebooksDir + File.separator + codebookName));
            extractorItem.getFeatureInstance().setByteArrayRepresentation(aggregator.getByteVectorRepresentation());
            double maxDistance = findSimilar(reader, extractorItem.getFeatureInstance());
            if (!useSimilarityScore) {
                searchHits = new SimpleImageSearchHits(this.docs, maxDistance);
            } else {
                searchHits = new SimpleImageSearchHits(this.docs, maxDistance, useSimilarityScore);
            }
        } else throw new UnsupportedOperationException("");

        return searchHits;

    }

    public ImageDuplicates findDuplicates(IndexReader reader) throws IOException {
        // get the first document:
        SimpleImageDuplicates simpleImageDuplicates = null;
//        try {
//            if (!IndexReader.indexExists(reader.directory()))
//                throw new FileNotFoundException("No index found at this specific location.");
        Document doc = reader.document(0);

        LireFeature lireFeature = extractorItem.getFeatureInstance();
        if (doc.getField(fieldName).binaryValue() != null && doc.getField(fieldName).binaryValue().length > 0)
            lireFeature.setByteArrayRepresentation(doc.getField(fieldName).binaryValue().bytes, doc.getField(fieldName).binaryValue().offset, doc.getField(fieldName).binaryValue().length);

        HashMap<Double, List<String>> duplicates = new HashMap<Double, List<String>>();

        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);

        int docs = reader.numDocs();
        int numDuplicates = 0;
        for (int i = 0; i < docs; i++) {
            if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.

            Document d = reader.document(i);
            double distance = getDistance(d, lireFeature);

            if (!duplicates.containsKey(distance)) {
                duplicates.put(distance, new LinkedList<String>());
            } else {
                numDuplicates++;
            }
            duplicates.get(distance).add(d.getField(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue());
        }

        if (numDuplicates == 0) return null;

        LinkedList<List<String>> results = new LinkedList<List<String>>();
        for (double d : duplicates.keySet()) {
            if (duplicates.get(d).size() > 1) {
                results.add(duplicates.get(d));
            }
        }
        simpleImageDuplicates = new SimpleImageDuplicates(results);
//        } catch (InstantiationException e) {
//            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
//        } catch (IllegalAccessException e) {
//            logger.log(Level.SEVERE, "Error instantiating class for generic image searcher: " + e.getMessage());
//        }
        return simpleImageDuplicates;

    }

    public String toString() {
        return "GenericSearcher using " + extractorItem.getExtractorClass().getName();
    }

}
