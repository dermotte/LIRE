package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.DocumentBuilder;

/**
 * FREAK creates a 64-dimensional feature vector of byte values
 * for each appropriate keypoint.
 * 
 * @author Mario Taschwer
 * @version $Id$
 */
public class FreakFeature extends LocalFeature
{
    
    private byte[] freakDescriptor;

    public FreakFeature()
    {
        // NOP
    }
    
    @Override
    public String getFeatureName()
    {
        return "FREAK";
    }

    @Override
    public String getFieldName()
    {
        return DocumentBuilder.FIELD_NAME_FREAK;
    }

    @Override
    public byte[] getByteArrayRepresentation()
    {
        return freakDescriptor;
    }

    @Override
    public void setByteArrayRepresentation(byte[] in)
    {
        setByteArrayRepresentation(in, 0, in.length);
    }

    @Override
    public void setByteArrayRepresentation(byte[] in, int offset, int length)
    {
        if (freakDescriptor == null || freakDescriptor.length != length)
            freakDescriptor = new byte[length];
        System.arraycopy(in, offset, freakDescriptor, 0, length);
        descriptor = ubyte2doubleArray(descriptor, in, offset, length);
    }

}
