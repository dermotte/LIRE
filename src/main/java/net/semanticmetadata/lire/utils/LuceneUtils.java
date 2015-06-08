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
 * Updated: 18.01.15 07:40
 */

package net.semanticmetadata.lire.utils;

import net.semanticmetadata.lire.indexers.LireCustomCodec;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;
import org.apache.lucene.util.Version;

import java.io.File;
import java.io.IOException;

/**
 * This class provides some common functions for Lucene. As there are many changes to
 * the API of Lucene over time this class is the central place to change common variable
 * like the employed Lucene version.
 * User: Mathias
 * Date: 03.08.11
 * Time: 09:33
 *
 * @author Mathias Lux, mathias@juggle.at
 * @author Nektarios Anagnostopoulos, nek.anag@gmail.com
 */
public class LuceneUtils {
    /**
     * Currently employed version of Lucene
     */
    public static final Version LUCENE_VERSION = Version.LUCENE_4_10_2;

    /**
     * Different types of analyzers
     */
    public enum AnalyzerType {
        SimpleAnalyzer, WhitespaceAnalyzer, KeywordAnalyzer, StandardAnalyzer
    }

    /**
     * Creates an IndexWriter for given index path, with a SimpleAnalyzer.
     *
     * @param indexPath the path to the index directory
     * @param create    set to true if you want to create a new index
     * @return the IndexWriter
     * @throws IOException
     */
    public static IndexWriter createIndexWriter(String indexPath, boolean create) throws IOException {
        return createIndexWriter(indexPath, create, AnalyzerType.SimpleAnalyzer);                       //TODO: Simple or Standar ??
    }

    /**
     * Creates an IndexWriter for given index path, with given analyzer.
     *
     * @param indexPath the path to the index directory
     * @param create    set to true if you want to create a new index
     * @param analyzer  gives the analyzer used for the Indexwriter.
     * @return an IndexWriter
     * @throws IOException
     */
    public static IndexWriter createIndexWriter(String indexPath, boolean create, AnalyzerType analyzer) throws IOException {
        return createIndexWriter(FSDirectory.open(new File(indexPath)), create, analyzer);
    }

    /**
     * Creates an IndexWriter for given index path, with given analyzer.
     *
     * @param directory the path to the index directory
     * @param create    set to true if you want to create a new index
     * @param analyzer  gives the analyzer used for the Indexwriter.
     * @return an IndexWriter
     * @throws IOException
     */
    public static IndexWriter createIndexWriter(Directory directory, boolean create, AnalyzerType analyzer) throws IOException {
        // set the analyzer according to the method params
        Analyzer tmpAnalyzer = null;
        if (analyzer == AnalyzerType.SimpleAnalyzer)
            tmpAnalyzer = new SimpleAnalyzer();    // LetterTokenizer with LowerCaseFilter
        else if (analyzer == AnalyzerType.WhitespaceAnalyzer)
            tmpAnalyzer = new WhitespaceAnalyzer();  // WhitespaceTokenizer
        else if (analyzer == AnalyzerType.KeywordAnalyzer)
            tmpAnalyzer = new KeywordAnalyzer(); // entire string as one token.
        else if (analyzer == AnalyzerType.StandardAnalyzer)
            tmpAnalyzer = new StandardAnalyzer();

        // The config
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, tmpAnalyzer);
        if (create)
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // overwrite if it exists.
        else
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND); // create new if none is there, append otherwise.

        config.setCodec(new LireCustomCodec());
        return new IndexWriter(directory, config);
    }

    public static IndexWriter createIndexWriter(Directory directory, boolean create, AnalyzerType analyzer, double RAMBufferSize) throws IOException {
        // set the analyzer according to the method params
        Analyzer tmpAnalyzer = null;
        if (analyzer == AnalyzerType.SimpleAnalyzer) tmpAnalyzer = new SimpleAnalyzer();
        else if (analyzer == AnalyzerType.WhitespaceAnalyzer) tmpAnalyzer = new WhitespaceAnalyzer();

        // The config
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, tmpAnalyzer);
        if (create)
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // overwrite if it exists.
        else
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND); // create new if none is there, append otherwise.
        config.setRAMBufferSizeMB(RAMBufferSize);
        config.setCodec(new LireCustomCodec());
        return new IndexWriter(directory, config);
    }

    /**
     * Optimizes an index.
     * @param iw
     * @throws IOException
     */
    public static void optimizeWriter(IndexWriter iw) throws IOException {
        iw.forceMerge(1);
    }

    public static void commitWriter(IndexWriter iw) throws IOException {
        iw.commit();
    }

    public static void closeWriter(IndexWriter iw) throws IOException {
        iw.close();
    }




    public static IndexReader openIndexReader(String indexPath) throws IOException {
        return openIndexReader(FSDirectory.open(new File(indexPath)), false);
    }

    public static IndexReader openIndexReader(String indexPath, boolean RAMDirectory) throws IOException {
        return openIndexReader(FSDirectory.open(new File(indexPath)), RAMDirectory);
    }

    public static IndexReader openIndexReader(Directory directory) throws IOException {
        return openIndexReader(directory, false);
    }

    public static IndexReader openIndexReader(Directory directory, boolean RAMDirectory) throws IOException {
        if (RAMDirectory)
            return DirectoryReader.open(new RAMDirectory(directory, IOContext.READONCE));
        else
            return DirectoryReader.open(directory);
    }

    public static IndexReader openIndexReader(IndexWriter writer, boolean applyDeletes) throws IOException {
        return DirectoryReader.open(writer, applyDeletes);
    }

    public static void closeReader(IndexReader reader) throws IOException {
        reader.close();
    }


    public static IndexSearcher openIndexSearcher(IndexReader reader){
        return new IndexSearcher(reader);
    }






    /**
     * Method for 'converting' ByteRefs to bytes. This is a horrible way to do it. Main goal is to make it work. Later
     * we'll fix the performance implications of that.
     *
     * @param byteRef
     * @return
     */
    public static byte[] getBytes(BytesRef byteRef) {
        byte[] result = new byte[byteRef.length];
        System.arraycopy(byteRef.bytes, byteRef.offset, result, 0, byteRef.length);
        return result;
    }

}

