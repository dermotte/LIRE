package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.imageanalysis.features.GenericDoubleLireFeature;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;

import java.awt.image.BufferedImage;

/**
 * Created by mlux on 10.03.2017.
 */
public class GenericGlobalDoubleFeature extends GenericDoubleLireFeature implements GlobalFeature {
    @Override
    public void extract(BufferedImage image) {
        throw new UnsupportedOperationException("Not implemented!");
    }
}
