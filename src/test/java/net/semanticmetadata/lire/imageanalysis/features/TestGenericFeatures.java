package net.semanticmetadata.lire.imageanalysis.features;

import junit.framework.TestCase;

import java.util.Arrays;

public class TestGenericFeatures extends TestCase {
    public void testGenericShortLireFeature() {
        for (int k = 0; k < 100; k++) {
            GenericShortLireFeature f = new GenericShortLireFeature();
            short[] shorts = new short[128];
            for (int i = 0; i < shorts.length; i++) {
                shorts[i] = (short) (Math.random() * (Short.MAX_VALUE - Short.MIN_VALUE) - Short.MIN_VALUE);
            }
            f.setData(shorts);
            byte[] bytes = f.getByteArrayRepresentation();

            GenericShortLireFeature g = new GenericShortLireFeature();
            g.setByteArrayRepresentation(bytes);
            assertEquals(0d, f.getDistance(g), 0.00001d);
        }
    }

    public void testGenericByteLireFeature() {
        for (int k = 0; k < 100; k++) {
            GenericByteLireFeature f = new GenericByteLireFeature();
            byte[] myBytes = new byte[128];
            for (int i = 0; i < myBytes.length; i++) {
                myBytes[i] = (byte) (Math.random() * (Byte.MAX_VALUE - Byte.MIN_VALUE) - Byte.MIN_VALUE);
            }
            f.setData(myBytes);
            byte[] bytes = f.getByteArrayRepresentation();

            GenericByteLireFeature g = new GenericByteLireFeature();
            g.setByteArrayRepresentation(bytes);
            assertEquals(0d, f.getDistance(g), 0.00001d);
        }
    }

    public void testGenericIntLireFeature() {
        for (int k = 0; k < 100; k++) {
            GenericIntLireFeature f = new GenericIntLireFeature();
            int[] myBytes = new int[128];
            for (int i = 0; i < myBytes.length; i++) {
                myBytes[i] = (int) (Math.random() * (Integer.MAX_VALUE - Integer.MIN_VALUE) - Integer.MIN_VALUE);
            }
            f.setData(myBytes);
            byte[] bytes = f.getByteArrayRepresentation();

            GenericIntLireFeature g = new GenericIntLireFeature();
            g.setByteArrayRepresentation(bytes);
            assertEquals(0d, f.getDistance(g), 0.00001d);
        }
    }
}
