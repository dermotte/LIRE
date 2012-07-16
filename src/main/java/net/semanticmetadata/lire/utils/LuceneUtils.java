/*
 * This file is part of the LIRe project: http://www.semanticmetadata.net/lire
 * LIRe is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * LIRe is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with LIRe; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * We kindly ask you to refer the following paper in any publication mentioning Lire:
 *
 * Lux Mathias, Savvas A. Chatzichristofis. Lire: Lucene Image Retrieval –
 * An Extensible Java CBIR Library. In proceedings of the 16th ACM International
 * Conference on Multimedia, pp. 1085-1088, Vancouver, Canada, 2008
 *
 * http://doi.acm.org/10.1145/1459359.1459577
 *
 * Copyright statement:
 * ~~~~~~~~~~~~~~~~~~~~
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.utils;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.KeywordAnalyzer;
import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
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
 */
public class LuceneUtils {
    /**
     * Currently employed version of Lucene
     */
    public static final Version LUCENE_VERSION = Version.LUCENE_36;

    /**
     * Different types of analyzers
     */
    public enum AnalyzerType {
        SimpleAnalyzer, WhitespaceAnalyzer, KeywordAnalyzer
    }

    ;

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
        if (analyzer == AnalyzerType.SimpleAnalyzer) tmpAnalyzer = new SimpleAnalyzer(LUCENE_VERSION);
        else if (analyzer == AnalyzerType.WhitespaceAnalyzer) tmpAnalyzer = new WhitespaceAnalyzer(LUCENE_VERSION);
        else if (analyzer == AnalyzerType.KeywordAnalyzer) tmpAnalyzer = new KeywordAnalyzer();

        // The config
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, tmpAnalyzer);
        if (create)
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // overwrite if it exists.
        else
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND); // create new if none is there, append otherwise.

        return new IndexWriter(directory, config);
    }

    public static IndexWriter createIndexWriter(Directory directory, boolean create, AnalyzerType analyzer, double RAMBufferSize) throws IOException {
        // set the analyzer according to the method params
        Analyzer tmpAnalyzer = null;
        if (analyzer == AnalyzerType.SimpleAnalyzer) tmpAnalyzer = new SimpleAnalyzer(LUCENE_VERSION);
        else if (analyzer == AnalyzerType.WhitespaceAnalyzer) tmpAnalyzer = new WhitespaceAnalyzer(LUCENE_VERSION);

        // The config
        IndexWriterConfig config = new IndexWriterConfig(LUCENE_VERSION, tmpAnalyzer);
        if (create)
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // overwrite if it exists.
        else
            config.setOpenMode(IndexWriterConfig.OpenMode.CREATE_OR_APPEND); // create new if none is there, append otherwise.
        config.setRAMBufferSizeMB(RAMBufferSize);
        return new IndexWriter(directory, config);
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
        return createIndexWriter(indexPath, create, AnalyzerType.SimpleAnalyzer);
    }

//    public static void moveIndexToMemory(IndexReader ir) {
//        IndexWriter iw = new IndexWriter(RAMDirectory);
//    }
}
