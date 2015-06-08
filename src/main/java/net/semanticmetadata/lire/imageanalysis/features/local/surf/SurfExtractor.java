package net.semanticmetadata.lire.imageanalysis.features.local.surf;

import com.stromberglabs.jopensurf.SURFInterestPoint;
import com.stromberglabs.jopensurf.Surf;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeature;
import net.semanticmetadata.lire.imageanalysis.features.LocalFeatureExtractor;

import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by Nektarios on 8/6/2015.
 *
 * @author Nektarios
 */
public class SurfExtractor implements LocalFeatureExtractor {
    LinkedList<SurfFeature> features = null;


    @Override
    public List<? extends LocalFeature> getFeatures() {
        return features;
    }

    @Override
    public Class<? extends LocalFeature> getClassOfFeatures() {
        return SurfFeature.class;
    }

    @Override
    public void extract(BufferedImage image) {
        Surf s = new Surf(image);
        List<SURFInterestPoint> interestPoints = s.getFreeOrientedInterestPoints();
        features = new LinkedList<SurfFeature>();
        for (SURFInterestPoint interestPoint : interestPoints) {
            features.add(new SurfFeature(interestPoint));
        }
    }
}
