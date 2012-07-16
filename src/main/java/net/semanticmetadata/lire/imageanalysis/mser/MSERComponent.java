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
 * --------------------
 * (c) 2002-2011 by Mathias Lux (mathias@juggle.at)
 *     http://www.semanticmetadata.net/lire
 */

package net.semanticmetadata.lire.imageanalysis.mser;

import java.util.ArrayList;

/**
 * Created by IntelliJ IDEA.
 * User: Shotty
 * Date: 28.06.2010
 * Time: 22:51:05
 */
public class MSERComponent {
    LinkedImagePoint head;
    LinkedImagePoint tail;
    MSERGrowthHistory history;

    // histories to link to new history (when the next is added)
    ArrayList<MSERGrowthHistory> historiesToLinkToParent = new ArrayList<MSERGrowthHistory>();

    int greyLevel;
    int size;

    public MSERComponent(int level) {
        head = null;
        tail = null;
        history = null;
        greyLevel = level;
        size = 0;
    }

    public void addPixel(BoundaryPixel pixel) {
        if (size == 0) {
            // first pixel in component
            head = new LinkedImagePoint(pixel.getPoint());
            tail = head;
        } else {
            /*
            System.out.println("Size:" + size);
            System.out.println("Head:" + head);
            System.out.println("Tail:" + tail);
            System.out.println("GreyLevel:" + greyLevel);
            System.out.println("History:" + history);
            */
            // update the circle
            LinkedImagePoint newLast = new LinkedImagePoint(pixel.getPoint());
            newLast.setPrev(tail);
            tail.setNext(newLast);
            tail = newLast;
        }
        size++;
    }

    public void mergeComponents(MSERComponent comp, int newGreyLevel) {
        // merge points
        if (comp.getSize() != 0) {
            if (this.size == 0) {
                head = comp.getHead();
                tail = head;
            } else {
                comp.getHead().setPrev(tail);
                tail.setNext(comp.getHead());
                tail = comp.getTail();
            }
            this.size += comp.getSize();
        }

        // winner is always THIS component, not the given one
        // take the historiesToLinkToParents and the current history of the given component
        if (comp.getHistory() != null) {
            historiesToLinkToParent.add(comp.getHistory());
        }
        if (comp.getHistoriesToLinkToParent().size() > 0) {
            historiesToLinkToParent.addAll(comp.getHistoriesToLinkToParent());
        }

        setGreyLevel(newGreyLevel);
    }

    /**
     * My History implementation
     */
    public void addHistory() {
        // add history of greyLevel
        MSERGrowthHistory newHist = new MSERGrowthHistory(size, greyLevel, head);
        if (history != null) {
            history.parent = newHist;
        }
        for (MSERGrowthHistory toAdd : historiesToLinkToParent) {
            toAdd.parent = newHist;
        }
        // clear after setting the parent
        historiesToLinkToParent.clear();

        history = newHist;
    }

    public MSERGrowthHistory getHistory() {
        return history;
    }

    public int getPastSize() {
        if (history != null) {
            return history.getSize();
        } else {
            return 0;
        }
    }

    public void setGreyLevel(int currentGreyLevel) {
        greyLevel = currentGreyLevel;
    }

    public int getGreyLevel() {
        return greyLevel;
    }

    public int getSize() {
        return size;
    }

    public LinkedImagePoint getHead() {
        return head;
    }

    public LinkedImagePoint getTail() {
        return tail;
    }

    public ArrayList<MSERGrowthHistory> getHistoriesToLinkToParent() {
        return historiesToLinkToParent;
    }
}
