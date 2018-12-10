package net.semanticmetadata.lire.utils;

import org.junit.Test;

import static org.junit.Assert.*;

public class MetricsUtilsTest {

    @Test
    public void cosineCoefficient() {
        double[] h1 = new double[64];
        double[] h2 = new double[64];

        for (int j = 0; j< 100; j++) {
            for (int i = 0; i < h2.length; i++) {
                h2[i] = Math.random();
                h1[i] = Math.random();
            }

            System.out.println(MetricsUtils.cosineCoefficient(h1, h2));
            System.out.println(MetricsUtils.cosineCoefficient(h2, h1));
            System.out.println(MetricsUtils.cosineCoefficient(h2, h2));
            System.out.println(MetricsUtils.cosineCoefficient(h1, h1));
        }
    }

//    static public float cosine(SparseVector v1, SparseVector v2) {
//        float c;
//        if (v1.normalized && v2.normalized) {
//            c=dotproduct(v1,v2);
//        } else {
//            float n1=(float)v1.norm();
//            float n2=(float)v2.norm();
//            c=dotproduct(v1,v2) / (n1 * n2);
//        }
//        // to avoid round effect return 1 when it is almost 1
//        if (c>1) return 1f;
//        return c;
//    }
//
//    static public float dotproduct(SparseVector v1, SparseVector v2) {
//        float sum=0;
//        if (v1.normalized && v2.normalized) {
//            for (String w1:v1.vector.keySet()) {
//                if (v2.vector.containsKey(w1)) {
//                    sum+=v1.vector.get(w1)*v2.vector.get(w1);
//                }
//            }
//            return sum;
//        } else {
//            // Non normalized, we have to do it ourselves
//            for (String w1:v1.vector.keySet()) {
//                if (v2.vector.containsKey(w1)) {
//                    sum+=v1.vector.get(w1)*v2.vector.get(w1);
//                }
//            }
//
//            return sum;
//        }
//    }
}