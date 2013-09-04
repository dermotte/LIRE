package net.semanticmetadata.lire.imageanalysis;

import java.nio.ByteBuffer;
import java.util.HashSet;

/**
 * Local feature represented by a binary feature vector.
 * 
 * @author Mario Taschwer
 * @version $Id$
 */
public abstract class LocalBinaryFeature extends LocalFeature
{
    private static final long serialVersionUID = 4486301452248715455L;

    /**
     * Create byte array representation from ByteBuffer.
     * The ByteBuffer is supposed to contain one feature descriptor
     * starting at position <code>offset</code>.
     * The result is compatible with {@link #getByteArrayRepresentation()}.
     * 
     * @param dst      destination array of length greater than or equal to <code>length</code>,
     *                 will be filled starting at position 0.
     * @param buffer   source buffer.
     * @param offset   offset in <code>buffer</code> pointing to the first descriptor element.
     * @param length   length of descriptor (in bytes).
     */
    public static void byteArrayFromBuffer(byte[] dst, ByteBuffer buffer, int offset, int length)
    {
        buffer.position(offset);
        for (int i=0; i < length; i++) {
            dst[i] = buffer.get();
        }
    }

    /** Lookup table: countOnes[k] gives the number of 1s in the binary representation of k (0 <= k <= 255). */
    private static final byte[] countOnes = new byte[] { 
        0, 1, 1, 2, 1, 2, 2, 3, 1, 2, 2, 3, 2, 3, 3, 4, 
        1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 
        1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 
        2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
        1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 
        2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
        2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
        3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 
        1, 2, 2, 3, 2, 3, 3, 4, 2, 3, 3, 4, 3, 4, 4, 5, 
        2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
        2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
        3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 
        2, 3, 3, 4, 3, 4, 4, 5, 3, 4, 4, 5, 4, 5, 5, 6, 
        3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 
        3, 4, 4, 5, 4, 5, 5, 6, 4, 5, 5, 6, 5, 6, 6, 7, 
        4, 5, 5, 6, 5, 6, 6, 7, 5, 6, 6, 7, 6, 7, 7, 8};
    
    /** binary descriptor */
    protected byte[] binDesc;
    
    /** component counters for {@link #setCentroid()}. */
    private short[] count = null;
    
    protected LocalBinaryFeature()
    {
        // NOP
    }

    protected LocalBinaryFeature(byte[] descriptor)
    {
        binDesc = descriptor;
    }

    @Override
    public String getFeatureName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getFieldName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    protected LocalFeature copyOf(LocalFeature src)
    {
        // no need to call super.copyOf() as super.descriptor is not used for binary descriptors
        if (src instanceof LocalBinaryFeature) {
            LocalBinaryFeature srcBin = (LocalBinaryFeature) src;
            final int len = srcBin.binDesc.length;
            binDesc = new byte[len];
            System.arraycopy(srcBin.binDesc, 0, binDesc, 0, len);
        }
        return this;
    }

    @Override
    public byte[] getByteArrayRepresentation()
    {
        return binDesc;
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length)
    {
        binDesc = new byte[length];
        System.arraycopy(in, offset, binDesc, 0, length);
    }

    /**
     * Compute Hamming distance between this and the given LocalBinaryFeature.
     * @param feature       needs to be an instance of LocalBinaryFeature.
     */
    @Override
    public float getDistance(LireFeature feature)
    {
        LocalBinaryFeature f = (LocalBinaryFeature) feature;
        int d = 0;
        for (int i=0; i < binDesc.length; i++) {
            d += countOnes[((binDesc[i] ^ f.binDesc[i]) + 256) & 0xff];
        }
        return (float) d;
    }

    /**
     * Compute the component-wise median of the given LocalBinaryFeatures, setting the result
     * in this instance. This amounts to a majority vote in each component of the binary feature vector.
     */
    @Override
    public void setCentroid(HashSet<LocalFeature> features)
    {
        if (count == null)
            count = new short[binDesc.length * 8];
        for (int i=0; i < count.length; i++) {
            count[i] = 0;
        }
        int byteval = 0;
        for (LocalFeature feature : features) {
            LocalBinaryFeature f = (LocalBinaryFeature) feature;
            for (int i=0, j=0; i < count.length; i++) {
                final int shift = i & 7;
                if (shift == 0) {
                    byteval = (f.binDesc[j] + 256) & 0xff;   // get unsigned byte value
                    j++;
                }
                count[i] += (byteval >> shift) & 1;
            }
        }
        byteval = 0;
        final int thresh = features.size() >> 1;
        for (int i=0, j=0; i < count.length; i++) {
            final int shift = i & 7;
            // if count[i] <= thresh, the median value is 0, and byteval does not need to be updated
            if (count[i] > thresh) {
                byteval |= 1 << shift;
            }
            if (shift == 7) {
                binDesc[j] = (byte) byteval;
                byteval = 0;
                j++;
            }
        }
    }

}
