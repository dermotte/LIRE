package net.semanticmetadata.lire.indexers.parallel;

import java.awt.image.BufferedImage;

/**
 * Created by luknoch on 01.10.15.
 */
public interface ImagePreprocessor {
    BufferedImage process(BufferedImage image);
}
