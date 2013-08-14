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

/*
 * This file is part of the LIRE project: http://www.SemanticMetadata.net/lire.
 *
 * Lire is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Lire is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Lire; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * (c) 2008-2010 by Mathias Lux, mathias@juggle.at
 */
package net.semanticmetadata.lire.clustering;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashSet;

import net.semanticmetadata.lire.imageanalysis.LocalFeature;

/**
 * Provides a simple implementation for a cluster used with the visual bag of words approach.
 * User: Mathias Lux, mathias@juggle.at
 * Date: 26.03.2010
 * Time: 12:10:19
 */
public class Cluster implements Comparable<Object>, Serializable 
{
    private static final long serialVersionUID = 4534242151035576519L;
    
    LocalFeature centroid;
    HashSet<LocalFeature> members = new HashSet<LocalFeature>();

    public Cluster(LocalFeature mean) {
        this.centroid = mean.clone();
    }

    /**
     * Return the distance between the given LocalFeature and this cluster's centroid.
     */
    public double getDistance(LocalFeature f)
    {
        return centroid.getDistance(f);
    }
    
    /*
    public String toString() {
        StringBuilder sb = new StringBuilder(512);
        for (Integer integer : members) {
            sb.append(integer);
            sb.append(", ");
        }
        for (int i = 0; i < mean.length; i++) {
            sb.append(mean[i]);
            sb.append(';');
        }
        return sb.toString();
    }
    */
    
    @Override
    public int compareTo(Object o) {
        return ((Cluster) o).members.size() - members.size();
    }

    /*
    public double getStress() {
        return stress;
    }

    public void setStress(double stress) {
        this.stress = stress;
    }

    public HashSet<LocalFeature> getMembers() {
        return members;
    }

    public void setMembers(HashSet<LocalFeature> members) {
        this.members = members;
    }
    */
    
    /**
     * Write cluster centroids to the given file.
     * Deletes membership mappings of all clusters before writing!
     */
    public static void writeClusters(Cluster[] clusters, String file) throws IOException {
        ObjectOutputStream fout = new ObjectOutputStream(new FileOutputStream(file));
        for (Cluster c : clusters) {
            c.members.clear();
        }
        fout.writeObject(clusters);
        fout.close();
    }

    /**
     * Read cluster centroids from file.
     * The file is expected to be written using {@link #writeClusters()}.
     */
    public static Cluster[] readClusters(String file) throws IOException  {
        ObjectInputStream fin = new ObjectInputStream(new FileInputStream(file));
        Cluster[] result = null;
        try {
            result = (Cluster[]) fin.readObject();
        } catch (ClassNotFoundException e) {
            throw new IOException(e);
        } finally {
            fin.close();
        }
        return result;
    }

}
