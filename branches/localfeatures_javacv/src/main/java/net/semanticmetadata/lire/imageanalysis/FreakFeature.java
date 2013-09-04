package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.DocumentBuilder;

/**
 * FREAK creates a 512-dimensional binary feature vector (64 bytes)
 * for each appropriate keypoint.
 * 
 * @author Mario Taschwer
 * @version $Id$
 */
public class FreakFeature extends LocalBinaryFeature
{
    
    private static final long serialVersionUID = -3236386571130123265L;

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
    public LocalFeature clone()
    {
        return new FreakFeature().copyOf(this);
    }

}
