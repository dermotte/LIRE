package net.semanticmetadata.lire.imageanalysis;

import junit.framework.TestCase;

import java.util.HashMap;

/**
 * User: mlux
 * Date: 09.10.12
 * Time: 11:50
 */
public class FuzzyColorTest extends TestCase {

    public void testFuzziness() {
        int[] pixel = new int[]{40, 120, 230};

        int[] bins = new int[8];
        double[] amount = new double[8];

        for (int i = 0; i < pixel.length; i++) {
            double[] bt = new double[4];
            bt[0] = 0;
            bt[1] = 0;
            bt[2] = 0;
            bt[3] = 0;
            double val = pixel[i];
            if (val <= 30) bt[0] = 1;
            else if (val >= 55 && val <= 115) bt[1] = 1;
            else if (val >= 140 && val <= 200) bt[2] = 1;
            else if (val >= 225) bt[3] = 1;
            else { // its fuzzy ...
                if (val > 30 && val < 55) {
                    bt[1] = (val - 30d) / 25;
                    bt[0] = 1 - bt[1];
                } else if (val > 115 && val < 140) {
                    bt[2] = (val - 115d) / 25;
                    bt[1] = 1 - bt[2];
                } else if (val > 200 && val < 225) {
                    bt[3] = (val - 115d) / 25;
                    bt[2] = 1 - bt[3];
                }
            }
            for (int j = 0; j < bt.length; j++) {
                if (bt[j] > 0) System.out.printf("%d: %1.2f \t", j, bt[j]);
            }
            System.out.println();
        }

    }


}
