/*
 * This file is part of the LIRE project: http://www.semanticmetadata.net/lire
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
 * --------------------
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire, http://www.lire-project.net
 */

package net.semanticmetadata.lire.invertedlist;

import junit.framework.TestCase;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;

import java.util.StringTokenizer;

//import org.apache.lucene.index.Norm;

public class GeneralInvertedListTest extends TestCase {
    private String indexPath = "./test-index-cedd-flickr";
    private int numRefObjsReferenced = 50;
    private int numRefObjs = 500;

    /*
    public void testIndexing() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath)));
        int numDocs = reader.numDocs();
        System.out.println("numDocs = " + numDocs);
        int docs = reader.numDocs();
        boolean hasDeletions = reader.hasDeletions();

        numRefObjs = 500;
//        int numRefObjs = (int) Math.sqrt(docs);
        System.out.println("numRefObjs = " + numRefObjs);

        // init reference objects:

        IndexWriter iw = LuceneUtils.createIndexWriter(indexPath + "-ro", true);
        HashSet<Integer> referenceObjectIds = new HashSet<Integer>(numRefObjs);

        double numDocsDouble = (double) numDocs;
        while (referenceObjectIds.size() < numRefObjs) {
            referenceObjectIds.add((int) (numDocsDouble * Math.random()));
        }
        int count = 0;
        for (int i : referenceObjectIds) {
            count++;
            // todo: check if deleted ...
            Document document = reader.document(i);
            document.add(new Field("ro-id", count + "", Field.Store.YES, Field.Index.NOT_ANALYZED));
            iw.addDocument(document);
        }
        iw.commit();
        iw.close();

        // now find the reference objects for each entry ;)
        IndexReader readerRo = IndexReader.open(FSDirectory.open(new File(indexPath + "-ro")));
        ImageSearcher searcher = ImageSearcherFactory.createCEDDImageSearcher(numRefObjsReferenced);
        PerFieldAnalyzerWrapper wrapper =
                new PerFieldAnalyzerWrapper(new SimpleAnalyzer(LuceneUtils.LUCENE_VERSION));
        wrapper.addAnalyzer("ro-order", new WhitespaceAnalyzer(LuceneUtils.LUCENE_VERSION));

        iw = LuceneUtils.createIndexWriter(indexPath + "-new", true);
//        iw = new IndexWriter(FSDirectory.open(new File(indexPath + "-new")), wrapper, true, IndexWriter.MaxFieldLength.UNLIMITED);
        StringBuilder sb = new StringBuilder(256);
        for (int i = 0; i < docs; i++) {
            if (hasDeletions && reader.isDeleted(i)) {
                continue;
            }
            Document document = reader.document(i);
            ImageSearchHits hits = searcher.search(document, readerRo);
            sb.delete(0, sb.length());
            for (int j = 0; j < numRefObjsReferenced; j++) {
                sb.append(hits.doc(j).getValues("ro-id")[0]);
                sb.append(' ');
            }
            // System.out.println(sb.toString());
            document.add(new Field("ro-order", sb.toString(), Field.Store.YES, Field.Index.ANALYZED, Field.TermVector.WITH_POSITIONS_OFFSETS));
            iw.addDocument(document);
        }
        iw.commit();
        iw.close();

    }


    public void testExplicitSearch() throws IOException {
        IndexReader reader = IndexReader.open(FSDirectory.open(new File(indexPath + "-new")));
        int numSearches = 50;

        String query = reader.document(2).getValues("ro-order")[0];
        TopDocs docs;
        long ms = System.currentTimeMillis();
        for (int i = 0; i < numSearches; i++) {
            query = reader.document(i).getValues("ro-order")[0];
            docs = scoreDocs(query, reader);
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("ms = " + ms);

//        for (int i = 0; i < docs.scoreDocs.length; i++) {
//            ScoreDoc scoreDoc = docs.scoreDocs[i];
//            System.out.println("<img title=\"Score: "+scoreDoc.score+"\" src=\"file:///"+reader.document(scoreDoc.doc).getValues("descriptorImageIdentifier")[0]+"\"><p>");
//        }

        ImageSearcher ceddSearcher = ImageSearcherFactory.createCEDDImageSearcher(100);
        ms = System.currentTimeMillis();
        for (int i = 0; i < numSearches; i++) {
            ceddSearcher.search(reader.document(i), reader);
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("ms = " + ms);
    }

    public TopDocs scoreDocs(String queryString, IndexReader reader) throws IOException {
        StringTokenizer st = new StringTokenizer(queryString);
        int position = 0;
        HashMap<Integer, Integer> doc2score = new HashMap<Integer, Integer>();
        HashMap<Integer, Integer> doc2count = new HashMap<Integer, Integer>();
        int currDoc = 0;
        while (st.hasMoreTokens()) {
            TermPositions tp = reader.termPositions(new Term("ro-order", st.nextToken()));
            while (tp.next()) {
                currDoc = tp.doc();
                // System.out.println(tp.doc() + ": " + tp.nextPosition());
                if (doc2score.get(currDoc) == null) {
                    doc2score.put(currDoc, Math.abs(tp.nextPosition() - position));
                    doc2count.put(currDoc, 1);
                } else {
                    doc2score.put(currDoc, doc2score.get(currDoc) + Math.abs(tp.nextPosition() - position));
                    doc2count.put(currDoc, doc2count.get(currDoc) + 1);
                }

            }
            position++;
        }
        // fill up all the remaining doc scores,

        throw new UnsupportedOperationException("Not implemented");
    }*/

    public static Query getQuery(String queryString) {
        BooleanQuery b = new BooleanQuery();
        TermQuery t;
        StringTokenizer st = new StringTokenizer(queryString);
        while (st.hasMoreTokens())
            b.add(new BooleanClause(new TermQuery(new Term("ro-order", st.nextToken())), BooleanClause.Occur.SHOULD));
        return b;
    }
}

/*class PositionScorer extends Scorer {
    private int doc;

    private final int[] docs = new int[32];         // buffered doc numbers
    private final int[] freqs = new int[32];        // buffered term freqs
    private int pointer;
    private int pointerMax;

    private TermPositions tp;

    *//**
 * Constructs a Scorer.
 *
 * @param similarity The <code>Similarity</code> implementation used by this scorer.
 *//*
    public PositionScorer(Similarity similarity, TermPositions tp) {
        super(similarity);
        this.tp = this.tp;
    }


    public void score(HitCollector hc) throws IOException {
      next();
      score(hc, Integer.MAX_VALUE);
    }

    protected boolean score(HitCollector c, int end) throws IOException {
      Similarity similarity = getSimilarity();      // cache sim in local
      float[] normDecoder = Similarity.getNormDecoder();
      while (doc < end) {                           // for docs in window
        int f = freqs[pointer];
        float score =  similarity.tf(f);

        score *= normDecoder[norms[doc] & 0xFF];    // normalize for field

        c.collect(doc, score);                      // collect score

        if (++pointer >= pointerMax) {
          pointerMax = termDocs.read(docs, freqs);  // refill buffers
          if (pointerMax != 0) {
            pointer = 0;
          } else {
            termDocs.close();                       // close stream
            doc = Integer.MAX_VALUE;                // set to sentinel value
            return false;
          }
        }
        doc = docs[pointer];
      }
      return true;
    }

    *//** Returns the current document number matching the query.
 * Initially invalid, until {@link #next()} is called the first time.
 *//*
    public int doc() { return doc; }

    *//** Advances to the next document matching the query.
 * <br>The iterator over the matching documents is buffered using
 * {@link TermDocs#read(int[], int[])}.
 * @return true iff there is another document matching the query.
 *//*
    public boolean next() throws IOException {
      pointer++;
      if (pointer >= pointerMax) {
        pointerMax = termDocs.read(docs, freqs);    // refill buffer
        if (pointerMax != 0) {
          pointer = 0;
        } else {
          termDocs.close();                         // close stream
          doc = Integer.MAX_VALUE;                  // set to sentinel value
          return false;
        }
      }
      doc = docs[pointer];
      return true;
    }

    public float score() {
      int f = freqs[pointer];
      float raw =                                   // compute tf(f)*weight
        f < SCORE_CACHE_SIZE                        // check cache
        ? scoreCache[f]                             // cache hit
        : getSimilarity().tf(f)*weightValue;        // cache miss

      return raw * Similarity.decodeNorm(norms[doc]); // normalize for field
    }

    *//** Skips to the first match beyond the current whose document number is
 * greater than or equal to a given target.
 * <br>The implementation uses {@link TermDocs#skipTo(int)}.
 * @param target The target document number.
 * @return true iff there is such a match.
 *//*
    public boolean skipTo(int target) throws IOException {
      // first scan in cache
      for (pointer++; pointer < pointerMax; pointer++) {
        if (docs[pointer] >= target) {
          doc = docs[pointer];
          return true;
        }
      }

      // not found in cache, seek underlying stream
      boolean result = termDocs.skipTo(target);
      if (result) {
        pointerMax = 1;
        pointer = 0;
        docs[pointer] = doc = termDocs.doc();
        freqs[pointer] = termDocs.freq();
      } else {
        doc = Integer.MAX_VALUE;
      }
      return result;
    }

    *//** Returns an explanation of the score for a document.
 * <br>When this method is used, the {@link #next()} method
 * and the {@link #score(HitCollector)} method should not be used.
 * @param doc The document number for the explanation.
 *//*
    public Explanation explain(int doc) throws IOException {
      TermQuery query = (TermQuery)weight.getQuery();
      Explanation tfExplanation = new Explanation();
      int tf = 0;
      while (pointer < pointerMax) {
        if (docs[pointer] == doc)
          tf = freqs[pointer];
        pointer++;
      }
      if (tf == 0) {
          if (termDocs.skipTo(doc))
          {
              if (termDocs.doc() == doc)
              {
                  tf = termDocs.freq();
              }
          }
      }
      termDocs.close();
      tfExplanation.setValue(getSimilarity().tf(tf));
      tfExplanation.setDescription("tf(termFreq("+query.getTerm()+")="+tf+")");

      return tfExplanation;
    }

    */

/**
 * Returns a string representation of this <code>TermScorer</code>.
 *//*
    public String toString() { return "scorer(" + weight + ")"; }
}*/

//class PlainSimilarity extends Similarity {
//    @Override
//    public void computeNorm(FieldInvertState fieldInvertState, Norm norm) {
//        //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public SimWeight computeWeight(float v, CollectionStatistics collectionStatistics, TermStatistics... termStatisticses) {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public ExactSimScorer exactSimScorer(SimWeight simWeight, AtomicReaderContext atomicReaderContext) throws IOException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//
//    @Override
//    public SloppySimScorer sloppySimScorer(SimWeight simWeight, AtomicReaderContext atomicReaderContext) throws IOException {
//        return null;  //To change body of implemented methods use File | Settings | File Templates.
//    }
//}
