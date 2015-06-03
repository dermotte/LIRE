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
 * ====================
 * (c) 2002-2013 by Mathias Lux (mathias@juggle.at)
 *  http://www.semanticmetadata.net/lire, http://www.lire-project.net
 *
 * Updated: 23.06.13 19:42
 */

package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;
import net.semanticmetadata.lire.DocumentBuilderFactory;
import net.semanticmetadata.lire.ImageSearcher;
import net.semanticmetadata.lire.ImageSearcherFactory;
import net.semanticmetadata.lire.impl.ChainedDocumentBuilder;
import net.semanticmetadata.lire.impl.FastOpponentImageSearcher;
import net.semanticmetadata.lire.indexing.hashing.BitSampling;
import net.semanticmetadata.lire.indexing.parallel.ParallelIndexer;
import net.semanticmetadata.lire.utils.SerializationUtils;
import org.apache.lucene.codecs.Codec;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.store.MMapDirectory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * User: mlux
 * Date: 18.12.12
 * Time: 13:17
 */
public class OpponentHistogramTest extends TestCase {
    public void testExtraction() throws IOException {
        BufferedImage img = ImageIO.read(new FileInputStream("src\\test\\resources\\images\\test_image.png"));
        OpponentHistogram oh = new OpponentHistogram();
        oh.extract(img);
        System.out.println(Arrays.toString(oh.getDoubleHistogram()));
        OpponentHistogram oh2 = new OpponentHistogram();
        oh2.setByteArrayRepresentation(oh.getByteArrayRepresentation());
        long ms = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++)
            oh2.getDistance(oh);
        ms = System.currentTimeMillis() - ms;
        System.out.println("double distance ms = " + ms);
        byte[] h1 = oh.getByteArrayRepresentation();
        byte[] h2 = oh2.getByteArrayRepresentation();
        ms = System.currentTimeMillis();
        for (int i = 0; i < 1000000; i++) {
            oh.getDistance(h1, h2);
        }
        ms = System.currentTimeMillis() - ms;
        System.out.println("byte[] distance ms = " + ms);
        System.out.println("oh2.getDistance(oh) = " + oh2.getDistance(oh));
        System.out.println("oh2.getDistance(oh) = " + oh.getDistance(oh.getByteArrayRepresentation(), oh2.getByteArrayRepresentation()));
    }

    public void testFromString() {
        String q = "0-0-0-0-0-12-8-0-0-62-5a-0-0-0-0-0-0-0-0-0-0-48-8-0-0-29-2b-0-0-0-0-0-0-0-0-0-0-58-4-0-0-b-7f-0-0-0-0-0-0-0-0-0-0-16-b-0-0-2-72-0-0-0-0-0";
        String[] tmp = q.split("-");
        byte[] tmpByte = new byte[tmp.length];
        for (int i = 0; i < tmp.length; i++) {
            tmpByte[i] = Byte.parseByte(tmp[i], 16);
        }
        OpponentHistogram o = new OpponentHistogram();
        o.setByteArrayRepresentation(tmpByte);
        Document d = new Document();
        d.add(new StoredField("featOpHist", o.getByteArrayRepresentation()));
        d.add(new TextField("featOpHist" + "_hash", SerializationUtils.arrayToString(BitSampling.generateHashes(o.getDoubleHistogram())), Field.Store.YES));
    }

    public void testFastSearch() throws IOException {
        Codec.forName("LireCustomCodec");
//        ParallelIndexer pin = new ParallelIndexer(7, "./index-fast-3", "testdata/wang-1000") {
        ParallelIndexer pin = new ParallelIndexer(7, "./index-fast-3", "D:\\DataSets\\Flickrphotos\\01", true) {
            @Override
            public void addBuilders(ChainedDocumentBuilder builder) {
                builder.addBuilder(DocumentBuilderFactory.getOpponentHistogramDocumentBuilder());
            }
        };
        pin.run();
        IndexReader ir = DirectoryReader.open(MMapDirectory.open(new File("./index-fast-3")));
        System.out.println("ir.maxDoc() = " + ir.maxDoc());

        long ms = System.currentTimeMillis();
        ImageSearcher is = new FastOpponentImageSearcher(50);
        ms = System.currentTimeMillis() - ms;
        System.out.println("init ms = " + ms);

        ms = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) is.search(ir.document(i), ir);
        ms = System.currentTimeMillis() - ms;
        System.out.println("cached ms = " + ms);

        is = ImageSearcherFactory.createOpponentHistogramSearcher(50);
        ms = System.currentTimeMillis();
        for (int i = 0; i < 100; i++) is.search(ir.document(i), ir);
        ms = System.currentTimeMillis() - ms;
        System.out.println("read from Lucene ms = " + ms);
    }
}
