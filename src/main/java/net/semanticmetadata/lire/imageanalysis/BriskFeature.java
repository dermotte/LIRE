package net.semanticmetadata.lire.imageanalysis;

import net.semanticmetadata.lire.DocumentBuilder;

/**
 * BRISK creates a 512-dimensional binary feature vector (64 bytes)
 * for each detected keypoint.
 * 
 * @author Mario Taschwer
 * @version $Id$
 */
public class BriskFeature extends LocalBinaryFeature
{
    private static final long serialVersionUID = 202839550883583630L;

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
    public LocalFeature clone()
    {
        return new BriskFeature().copyOf(this);
    }

}
