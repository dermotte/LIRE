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

package net.semanticmetadata.lire.benchmarking;

import junit.framework.TestCase;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
 * User: Mathias Lux, mathias@juggle.at, http://www.semanticmetadata.net
 * Date: 12.03.2010
 * Time: 12:10:38
 */
public class SerializationTest extends TestCase {

    public void testPerformance() throws IOException {
        double[] array = new double[128];
        for (int i = 0; i < array.length; i++) {
            array[i] = Math.random() * 16;
        }

        int timesDone = 100;

        long ms = System.currentTimeMillis();
        for (int i = 0; i < timesDone; i++) {
            setStringRepresentationStringBuilder(getStringRepresentationStringBuilder(array));
        }
        System.out.println("ms = " + (System.currentTimeMillis() - ms));

        ms = System.currentTimeMillis();
        for (int i = 0; i < timesDone; i++) {
            setStringRepresentationDataOut(getStringRepresentationDataOut(array));
        }
        System.out.println("ms = " + (System.currentTimeMillis() - ms));

        ms = System.currentTimeMillis();
        for (int i = 0; i < timesDone; i++) {
            setStringRepresentationBB(getStringRepresentationBB(array));
        }
        System.out.println("ms = " + (System.currentTimeMillis() - ms));

        double[] doubles = setStringRepresentationBB(getStringRepresentationBB(array));
        for (int i = 0; i < array.length; i++) {
            System.out.println((int) array[i] - doubles[i]);
            assertTrue((int) array[i] - doubles[i] == 0);
        }

    }

    public String getStringRepresentationStringBuilder(double[] data) {
        StringBuilder sb = new StringBuilder(data.length * 2 + 25);
        sb.append("cedd");
        sb.append(' ');
        sb.append(data.length);
        sb.append(' ');
        for (double aData : data) {
            sb.append((int) aData);
            sb.append(' ');
        }
        return sb.toString().trim();
    }

    public double[] setStringRepresentationStringBuilder(String s) {
        StringTokenizer st = new StringTokenizer(s);
        if (!st.nextToken().equals("cedd"))
            throw new UnsupportedOperationException("This is not a CEDD descriptor.");
        double[] data = new double[Integer.parseInt(st.nextToken())];
        for (int i = 0; i < data.length; i++) {
            if (!st.hasMoreTokens())
                throw new IndexOutOfBoundsException("Too few numbers in string representation.");
            data[i] = Integer.parseInt(st.nextToken());
        }
        return data;
    }

    public byte[] getStringRepresentationDataOut(double[] data) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(out);
        dout.writeInt(data.length);
        for (double aData : data) {
            dout.writeInt((int) aData);
        }
        dout.flush();
        return out.toByteArray();
    }

    public double[] setStringRepresentationDataOut(byte[] in) throws IOException {
        ByteArrayInputStream bin = new ByteArrayInputStream(in);
        DataInputStream din = new DataInputStream(bin);
        double[] data = new double[din.readInt()];
        for (int i = 0; i < data.length; i++) {
            data[i] = din.readInt();
        }
        return data;
    }

    public byte[] getStringRepresentationBB(double[] data) throws IOException {
        byte[] result = new byte[data.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = (byte) data[i];
        }
        return result;
    }

    public double[] setStringRepresentationBB(byte[] in) throws IOException {
        double[] data = new double[in.length];
        for (int i = 0; i < data.length; i++) {
            data[i] = in[i];
        }
        return data;
    }
}
