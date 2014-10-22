package net.semanticmetadata.lire.impl;

import net.semanticmetadata.lire.imageanalysis.LireFeature;
import net.semanticmetadata.lire.utils.MetricsUtils;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.MultiFields;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Created by Nektarios on 9/10/2014.
 */
public class ImageSearcherUsingWSs extends GenericFastImageSearcher {
    private double[] idfValues;
    private boolean termFrequency = false;
    private boolean inverseDocFrequency = false;
    private boolean normalizeHistogram = false;
    private String ws = "nnn";

    public ImageSearcherUsingWSs(int maxHits, Class<?> descriptorClass, String fieldName) {
        super(maxHits, descriptorClass, fieldName);
    }

    public ImageSearcherUsingWSs(int maxHits, Class<?> descriptorClass, String fieldName, boolean useSimilarityScore) {
        super(maxHits, descriptorClass, fieldName, useSimilarityScore);
    }

    public ImageSearcherUsingWSs(int maxHits, Class<?> descriptorClass) {
        super(maxHits, descriptorClass);
    }

    public ImageSearcherUsingWSs(int maxHits, Class<?> descriptorClass, String fieldName, boolean isCaching, IndexReader reader) {
        super(maxHits, descriptorClass, fieldName, isCaching, reader);
    }

    public ImageSearcherUsingWSs(int maxHits, Class<?> descriptorClass, boolean isCaching, IndexReader reader) {
        super(maxHits, descriptorClass, isCaching, reader);
    }

    public ImageSearcherUsingWSs(int maxHits, Class<?> descriptorClass, String fieldName, boolean isCaching, IndexReader reader, boolean tf, boolean idf, boolean n) {
        super(maxHits, descriptorClass, fieldName, isCaching, reader);
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
    }

    protected void init() {
        isCaching = true;
        // put all respective features into an in-memory cache ...
        if (isCaching && reader != null) {
            int docs = reader.numDocs();
            featureCache = new LinkedList<byte[]>();
            try {
                Document d;
                for (int i = 0; i < docs; i++) {
                    d = reader.document(i);
                    cachedInstance.setByteArrayRepresentation(d.getField(fieldName).binaryValue().bytes, d.getField(fieldName).binaryValue().offset, d.getField(fieldName).binaryValue().length);
                    featureCache.add(cachedInstance.getByteArrayRepresentation());
                    if (idfValues == null)
                        idfValues = new double[cachedInstance.getDoubleHistogram().length];
                    for (int j = 0; j < cachedInstance.getDoubleHistogram().length; j++) {
                        if (cachedInstance.getDoubleHistogram()[j] > 0d) idfValues[j]++;
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
     * @throws java.io.IOException
     */
    protected float findSimilar(IndexReader reader, LireFeature lireFeature) throws IOException {
        maxDistance = -1f;
//        overallMaxDistance = -1f;
        // clear result set ...
        docs.clear();
        // Needed for check whether the document is deleted.
        Bits liveDocs = MultiFields.getLiveDocs(reader);
        Document d;
        float tmpDistance;
        int docs = reader.numDocs();
        if (!isCaching) {
            // we read each and every document from the index and then we compare it to the query.
            for (int i = 0; i < docs; i++) {
                if (reader.hasDeletions() && !liveDocs.get(i)) continue; // if it is deleted, just ignore it.

                d = reader.document(i);
                tmpDistance = getDistance(d, lireFeature);
                assert (tmpDistance >= 0);
                // if it is the first document:
                if (maxDistance < 0) {
                    maxDistance = tmpDistance;
                }
                // if the array is not full yet:
                if (this.docs.size() < maxHits) {
                    this.docs.add(new SimpleResult(tmpDistance, d, i));
                    if (tmpDistance > maxDistance) maxDistance = tmpDistance;
                } else if (tmpDistance < maxDistance) {
                    // if it is nearer to the sample than at least on of the current set:
                    // remove the last one ...
                    this.docs.remove(this.docs.last());
                    // add the new one ...
                    this.docs.add(new SimpleResult(tmpDistance, d, i));
                    // and set our new distance border ...
                    maxDistance = this.docs.last().getDistance();
                }
            }
        } else {
            // we use the in-memory cache to find the matching docs from the index.
            int count = 0;
            for (Iterator<byte[]> iterator = featureCache.iterator(); iterator.hasNext(); ) {
                cachedInstance.setByteArrayRepresentation(iterator.next());
                if (reader.hasDeletions() && !liveDocs.get(count)) {
                    count++;
                    continue; // if it is deleted, just ignore it.
                } else {
                    tmpDistance = getDistance(cachedInstance, lireFeature, reader);
                    assert (tmpDistance >= 0) : tmpDistance;
                    // if it is the first document:
                    if (maxDistance < 0) {
                        maxDistance = tmpDistance;
                    }
                    // if the array is not full yet:
                    if (this.docs.size() < maxHits) {
                        this.docs.add(new SimpleResult(tmpDistance, reader.document(count), count));
                        if (tmpDistance > maxDistance) maxDistance = tmpDistance;
                    } else if (tmpDistance < maxDistance) {
                        // if it is nearer to the sample than at least on of the current set:
                        // remove the last one ...
                        this.docs.remove(this.docs.last());
                        // add the new one ...
                        this.docs.add(new SimpleResult(tmpDistance, reader.document(count), count));
                        // and set our new distance border ...
                        maxDistance = this.docs.last().getDistance();
                    }
                    count++;
                }
            }
        }
        return maxDistance;
    }

    private float getDistance(LireFeature cachedInstance, LireFeature lireFeature, IndexReader reader) {
        double[] h = lireFeature.getDoubleHistogram().clone(), v = cachedInstance.getDoubleHistogram().clone();
        if (termFrequency) {
            for (int i = 0; i < h.length; i++) {
                if (h[i]>0) h[i] = 1 + Math.log10(h[i]);
                if (v[i]>0) v[i] = 1 + Math.log10(v[i]);
            }
        }
        if (inverseDocFrequency) {
            for (int i = 0; i < h.length; i++) {
                if (idfValues[i] > 0) {
                    h[i] = Math.log10((double) reader.numDocs() / idfValues[i]) * h[i];
                    v[i] = Math.log10((double) reader.numDocs() / idfValues[i]) * v[i];
                }
            }
        }
        if (normalizeHistogram) {
            h = MetricsUtils.normalizeL2(h);
            v = MetricsUtils.normalizeL2(v);
        }

        //TODO: change to other metrics if needed.
        return (float) MetricsUtils.distL2(h, v);
    }

    public String toString() {
        return "ImageSearcherUsingWSs using " + descriptorClass.getName() + " and ws: " + ws;
    }

//    @Override
//    protected float getDistance(Document document, LireFeature lireFeature) {
//        if (document.getField(fieldName).binaryValue() != null && document.getField(fieldName).binaryValue().length > 0) {
//            cachedInstance.setByteArrayRepresentation(document.getField(fieldName).binaryValue().bytes, document.getField(fieldName).binaryValue().offset, document.getField(fieldName).binaryValue().length);
//            //lireFeature.getDistance(cachedInstance);
//            double sum = 0d;
//            double[] h = lireFeature.getDoubleHistogram().clone(), v = cachedInstance.getDoubleHistogram().clone();
//            for (int i = 0; i < h.length; i++) {
//                h[i] *= Math.log10((double) reader.numDocs() / idfValues[i]);
//                v[i] *= Math.log10((double) reader.numDocs() / idfValues[i]);
//            }
//            // TODO do normalization here instead of before ..
//            if (normalizeHistogram) {
//                h = MetricsUtils.normalizeL2(h);
//                v = MetricsUtils.normalizeL2(v);
//            }
//
//            // L2 between two documents... TODO: change to other metrics if needed.
//            for (int i = 0; i < h.length; i++) {
//                sum += (h[i] - v[i]) * (h[i] - v[i]);
//            }
//            return (float) Math.sqrt(sum);
//        } else {
//            logger.warning("No feature stored in this document! (" + descriptorClass.getName() + ")");
//        }
//        return 0f;
//    }
}
