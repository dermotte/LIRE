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
        descriptor = ubyte2doubleArray(descriptor, in, offset, length);
    }

}
