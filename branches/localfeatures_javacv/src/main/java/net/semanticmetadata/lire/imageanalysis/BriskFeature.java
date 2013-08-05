package net.semanticmetadata.lire.imageanalysis;

import java.nio.ByteBuffer;

import net.semanticmetadata.lire.DocumentBuilder;

/**
 * BRISK creates a 64-dimensional feature vector of byte values
 * for each detected keypoint.
 * 
 * @author Mario Taschwer
 * @version $Id$
 */
public class BriskFeature extends LocalFeature
{
    /**
     * Create byte array representation from ByteBuffer.
     * The ByteBuffer is supposed to contain a BRISK descriptor of one keypoint
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
    
    private byte[] briskDescriptor;

    public BriskFeature()
    {
        // NOP
    }
    
    @Override
    public String getFeatureName()
    {
        return "BRISK";
    }

    @Override
    public String getFieldName()
    {
        return DocumentBuilder.FIELD_NAME_BRISK;
    }

    @Override
    public byte[] getByteArrayRepresentation()
    {
        return briskDescriptor;
    }

    @Override
    public void setByteArrayRepresentation(byte[] in)
    {
        setByteArrayRepresentation(in, 0, in.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length)
    {
        if (briskDescriptor == null || briskDescriptor.length != length)
            briskDescriptor = new byte[length];
        System.arraycopy(in, offset, briskDescriptor, 0, length);
        if (descriptor == null || descriptor.length != length)
            this.descriptor = new double[length];
        for (int i=0; i < length; i++) {
            this.descriptor[i] = (briskDescriptor[i] + 256) % 256;   // BRISK features are unsigned byte values
        }
    }

}
