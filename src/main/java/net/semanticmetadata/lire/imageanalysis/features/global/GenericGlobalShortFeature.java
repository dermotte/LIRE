package net.semanticmetadata.lire.imageanalysis.features.global;

import net.semanticmetadata.lire.imageanalysis.features.GenericShortLireFeature;
import net.semanticmetadata.lire.imageanalysis.features.GlobalFeature;

import java.awt.image.BufferedImage;

public class GenericGlobalShortFeature extends GenericShortLireFeature implements GlobalFeature {
    @Override
    public void extract(BufferedImage image) {
        throw new UnsupportedOperationException("No extraction method implemented.");
    }
}
