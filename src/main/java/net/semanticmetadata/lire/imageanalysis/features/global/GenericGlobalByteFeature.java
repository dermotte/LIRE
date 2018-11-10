package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.imageanalysis.features.GenericByteLireFeature;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;

import java.awt.image.BufferedImage;

public class GenericGlobalByteFeature extends GenericByteLireFeature implements GlobalFeature {
    @Override
    public void extract(BufferedImage image) {
        throw new UnsupportedOperationException("No extraction method implemented.");
    }
}
