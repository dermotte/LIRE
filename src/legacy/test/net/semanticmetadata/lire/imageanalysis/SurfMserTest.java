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

package net.semanticmetadata.lire.imageanalysis;

import com.stromberglabs.jopensurf.SURFInterestPoint;
import com.stromberglabs.jopensurf.Surf;
import junit.framework.TestCase;

import javax.imageio.ImageIO;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

/**
 * @author Mathias Lux, mathias@juggle.at
 *         Date: 04.10.2010
 *         Time: 13:21:55
 */
public class SurfMserTest extends TestCase {

    public void testExtract() throws IOException {
        Surf sm = new Surf(ImageIO.read(new FileInputStream("wang-1000/0.jpg")));
//        Surf sm = new SurfMser(ImageIO.read(new FileInputStream("wang-1000/0.jpg")));
        List<SURFInterestPoint> pts = sm.getFreeOrientedInterestPoints();
        for (Iterator<SURFInterestPoint> surfInterestPointIterator = pts.iterator(); surfInterestPointIterator.hasNext(); ) {
            SURFInterestPoint pt = surfInterestPointIterator.next();
            System.out.println(pt.getX() + ", " + pt.getY());
        }
    }
}
