package net.semanticmetadata.lire.searchers.forevaluations;

import net.semanticmetadata.lire.aggregators.Aggregator;
import net.semanticmetadata.lire.builders.DocumentBuilder;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LireFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;
import net.semanticmetadata.lire.imageanalysis.features.local.simple.SimpleExtractor;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;

/**
 * Created by Nektarios on 9/10/2014.
 */
public class ImageSearcherUsingWSsForEvaluation extends GenericFastImageSearcherForEvaluation {
    private double[] idfValues;
    private boolean termFrequency = false;
    private boolean inverseDocFrequency = false;
    private boolean normalizeHistogram = false;
    private String ws = "nnn";

    public ImageSearcherUsingWSsForEvaluation(int maxHits, Class<? extends LocalFeatureExtractor> localFeatureExtractor, Aggregator aggregator, int codebookSize, IndexReader reader, String codebooksDir, boolean tf, boolean idf, boolean n) {
        super(maxHits, localFeatureExtractor, aggregator, codebookSize, true, reader, codebooksDir);
        termFrequency = tf;
        inverseDocFrequency = idf;
        normalizeHistogram = n;
        setWS();
    }

    public ImageSearcherUsingWSsForEvaluation(int maxHits, Class<? extends GlobalFeature> globalFeature, SimpleExtractor.KeypointDetector detector, Aggregator aggregator, int codebookSize, IndexReader reader, String codebooksDir, boolean tf, boolean idf, boolean n) {
        super(maxHits, globalFeature, detector, aggregator, codebookSize, true, reader, codebooksDir);
        termFrequency = tf;
        inverseDocFrequency = idf;
        normalizeHistogram = n;
        setWS();
    }

    private void setWS()
    {
        if (termFrequency)
        {
            if (inverseDocFrequency)
            {
                if (normalizeHistogram)
                    ws = "ltc";
                else if (!normalizeHistogram)
                    ws = "ltn";
            }
            else if (!inverseDocFrequency)
            {
                if (normalizeHistogram)
                    ws = "lnc";
                else if (!normalizeHistogram)
                    ws = "lnn";
            }
        }
        else if (!termFrequency)
        {
            if (inverseDocFrequency)
            {
                if (normalizeHistogram)
                    ws = "ntc";
                else if (!normalizeHistogram)
                    ws = "ntn";
            }
            else if (!inverseDocFrequency)
            {
                if (normalizeHistogram)
                    ws = "nnc";
                else if (!normalizeHistogram)
                    ws = "nnn";
            }
        }

        LinkedList<Thread> threads = new LinkedList<Thread>();
        Thread thread;
        Thread p = new Thread(new Producer());
        p.start();
        for (int i = 0; i < numThreads; i++) {
            thread = new Thread(new Compute());
            thread.start();
            threads.add(thread);
        }
        for (Thread next : threads) {
            try {
                next.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private class Compute implements Runnable {
        private boolean locallyEnded = false;
        private LireFeature localCachedInstance;

        private Compute() {
            try {
                this.localCachedInstance = cachedInstance.getClass().newInstance();
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            Map.Entry<Integer, SearchItemForEvaluation> tmp;
            while (!locallyEnded) {
                try {
                    tmp = queue.take();
                    if (tmp.getKey() < 0 ) locallyEnded = true;
                    if (!locallyEnded) {    // && tmp != -1
                        localCachedInstance.setByteArrayRepresentation(tmp.getValue().getBuffer());
                        computeFeatureCache(localCachedInstance);
//                        featureCache.put(tmp.getKey(), localCachedInstance.getByteArrayRepresentation());
                        tmp.getValue().setBuffer(localCachedInstance.getByteArrayRepresentation());
                    }
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
        }
    }

    private void computeFeatureCache(LireFeature f) {
        double[] v = f.getFeatureVector();
        if (termFrequency) {
            for (int i = 0; i < v.length; i++) {
                if (v[i] > 0) v[i] = 1 + Math.log10(v[i]);
            }
        }
        if (inverseDocFrequency) {
            for (int i = 0; i < v.length; i++) {
                if (idfValues[i] > 0) {
                    v[i] = Math.log10((double) reader.numDocs() / idfValues[i]) * v[i];
                }
            }
        }
        if (normalizeHistogram) {
            double len = 0;
            for (double next : v) {
                len += next * next;
            }
            len = Math.sqrt(len);
            for (int i = 0; i < v.length; i++) {
                if (v[i] != 0)
                    v[i] /= len;
            }
        }
    }


    protected void init() {
        // put all respective features into an in-memory cache ...
        if (reader != null && reader.numDocs() > 0) {
            Bits liveDocs = MultiFields.getLiveDocs(reader);
            int docs = reader.numDocs();
            featureCache = new LinkedHashMap<Integer, SearchItemForEvaluation>(docs);
            try {
                int counter = 0;
                while ((reader.hasDeletions() && !liveDocs.get(counter))&&(counter<docs)){
                    counter++;
                }

                Document d = reader.document(counter);
                cachedInstance.setByteArrayRepresentation(d.getField(fieldName).binaryValue().bytes, d.getField(fieldName).binaryValue().offset, d.getField(fieldName).binaryValue().length);
                featureCache.put(counter, new SearchItemForEvaluation(cachedInstance.getByteArrayRepresentation(), new SimpleResultForEvaluation(-1d, counter, d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0])));
                idfValues = new double[cachedInstance.getFeatureVector().length];
                for (int j = 0; j < cachedInstance.getFeatureVector().length; j++) {
                    if (cachedInstance.getFeatureVector()[j] > 0d) idfValues[j]++;
                }
                counter++;
                for (int i = counter; i < docs; i++) {
                    if (!(reader.hasDeletions() && !liveDocs.get(i))) {
                        d = reader.document(i);
                        cachedInstance.setByteArrayRepresentation(d.getField(fieldName).binaryValue().bytes, d.getField(fieldName).binaryValue().offset, d.getField(fieldName).binaryValue().length);
                        featureCache.put(i, new SearchItemForEvaluation(cachedInstance.getByteArrayRepresentation(), new SimpleResultForEvaluation(-1d, i, d.getValues(DocumentBuilder.FIELD_NAME_IDENTIFIER)[0])));
                        for (int j = 0; j < cachedInstance.getFeatureVector().length; j++) {
                            if (cachedInstance.getFeatureVector()[j] > 0d) idfValues[j]++;
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
//        overallMaxDistance = -1f;
        // clear result set ...
        docs.clear();
        // Needed for check whether the document is deleted.
//        Bits liveDocs = MultiFields.getLiveDocs(reader);
//        Document d;
//        float tmpDistance;
//        int docs = reader.numDocs();
        if (!isCaching) {
            throw new UnsupportedOperationException("ImageSearcherUsingWSs works only with Caching!!!");
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
            TreeSet<SimpleResultForEvaluation> tmpDocs;
            boolean flag;
            SimpleResultForEvaluation simpleResult;
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
            for (Map.Entry<Integer, SearchItemForEvaluation> documentEntry : featureCache.entrySet()) {
                try {
                    queue.put(documentEntry);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            LinkedHashMap<Integer, SearchItemForEvaluation> tmpMap = new LinkedHashMap<Integer, SearchItemForEvaluation>(numThreads * 3);
            for (int i = 1; i < numThreads * 3; i++)  {
                tmpMap.put(-i, null);
            }
            for (Map.Entry<Integer, SearchItemForEvaluation> documentEntry : tmpMap.entrySet()) {
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
        private TreeSet<SimpleResultForEvaluation> localDocs  = new TreeSet<SimpleResultForEvaluation>();
        private LireFeature localCachedInstance;
        private LireFeature localLireFeature;

        private Consumer(LireFeature lireFeature) {
            try {
                this.localCachedInstance = cachedInstance.getClass().newInstance();
                this.localLireFeature = lireFeature.getClass().newInstance();
                this.localLireFeature.setByteArrayRepresentation(lireFeature.getByteArrayRepresentation());
                computeFeatureCache(this.localLireFeature);
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            Map.Entry<Integer, SearchItemForEvaluation> tmp;
            double tmpDistance;
            double localMaxDistance = -1;
            while (!locallyEnded) {
                try {
                    tmp = queue.take();
                    if (tmp.getKey() < 0 )  locallyEnded = true;
                    if (!locallyEnded) {
                        localCachedInstance.setByteArrayRepresentation(tmp.getValue().getBuffer());
                        tmpDistance = localLireFeature.getDistance(localCachedInstance);
                        assert (tmpDistance >= 0);
                        // if the array is not full yet:
                        if (localDocs.size() < maxHits) {
                            tmp.getValue().simpleResultForEvaluation.setDistance(tmpDistance);
                            localDocs.add(tmp.getValue().getSimpleResultForEvaluation());
                            if (tmpDistance > localMaxDistance) localMaxDistance = tmpDistance;
                        } else if (tmpDistance < localMaxDistance) {
                            tmp.getValue().simpleResultForEvaluation.setDistance(tmpDistance);
                            // if it is nearer to the sample than at least on of the current set:
                            // remove the last one ...
//                            localDocs.remove(localDocs.last());
                            localDocs.pollLast();
                            // add the new one ...
                            localDocs.add(tmp.getValue().getSimpleResultForEvaluation());
                            // and set our new distance border ...
                            localMaxDistance = localDocs.last().getDistance();
                        }
                    }
                } catch (InterruptedException e) {
                    e.getMessage();
                }
            }
        }

        public TreeSet<SimpleResultForEvaluation> getResult() {
            return localDocs;
        }
    }

    public String toString() {
        return "ImageSearcherUsingWSsForEvaluation using " + extractorItem.getExtractorClass().getName() + " and ws: " + ws;
    }

}
