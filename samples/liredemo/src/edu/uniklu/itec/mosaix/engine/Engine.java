package edu.uniklu.itec.mosaix.engine;

import net.semanticmetadata.lire.DocumentBuilder;
import net.semanticmetadata.lire.ImageSearchHits;
import org.apache.lucene.document.Document;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
/*
 * This file is part of the Caliph and Emir project: http://www.SemanticMetadata.net.
 *
 * Caliph & Emir is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * Caliph & Emir is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Caliph & Emir; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Copyright statement:
 * --------------------
 * (c) 2002-2007 by Mathias Lux (mathias@juggle.at), Lukas Esterle & Manuel Warum.
 * http://www.juggle.at, http://www.SemanticMetadata.net
 */

/**
 * The workhorse and brain of the whole application,
 * that makes use of strategies and generic sub systems.
 *
 * @author Manuel Warum
 * @author Mathias Lux, mathias@juggle.at
 * @version 0.25
 */
public final class Engine {
    private HashMap<String, Integer> file2occurence;
    private final LinkedList<WeightingStrategy> strategies_;
    private final LinkedList<EngineObserver> observer_;
    private WeightingDataFactory weightingDataFactory_;
    private static boolean outweightImageReuse = false;


    public Engine() {
        weightingDataFactory_ = new SimpleWeightingDataFactory();
        strategies_ = new LinkedList<WeightingStrategy>();
        observer_ = new LinkedList<EngineObserver>();
        file2occurence = new HashMap<String, Integer>(1000);
        // Logging.log(this, "Engine instantiated.");
    }

    /**
     * Adds a strategy to the weighting strategy collection.
     *
     * @param strategy the non-<code>null</code> strategy instance.
     */
    public void addStrategy(final WeightingStrategy strategy) {
        assert strategy != null;
        strategies_.add(strategy);
        // Logging.log(this, "Strategy added: " + strategy.getClass().getName());
    }

    /**
     * Removes the strategy from the weighting strategies
     * collection.
     *
     * @param strategy the strategy to remove.
     */
    public void removeStrategy(final WeightingStrategy strategy) {
        strategies_.remove(strategy);
        // Logging.log(this, "Strategy removed: " + strategy.getClass().getName());
    }

    public List<WeightingStrategy> getStrategies() {
        return strategies_;
    }

    /**
     * Gets all engine observers registered to the
     * engine.
     *
     * @return a list of engine observers.
     */
    public List<EngineObserver> getObservers() {
        return observer_;
    }

    /**
     * Adds an engine observer to this engine instance.
     *
     * @param observer the observer to add.
     */
    public void addObserver(EngineObserver observer) {
        assert observer != null;
        observer_.add(observer);
        // Logging.log(this, "Observer added: " + observer.getClass().getName());
    }

    /**
     * Removes an engine observer from this engine instance.
     *
     * @param observer the observer to remove.
     */
    public void removeObserver(EngineObserver observer) {
        observer_.remove(observer);
        // Logging.log(this, "Observer removed: " + observer.getClass().getName());
    }

    /**
     * <p>Evaluates the search results provided by LIRE and
     * returns the best available match.</p>
     * <p>This method takes two aspects into account: First,
     * it uses the relevancy factor as provided by LIRE;
     * second, it uses implementation instances of the
     * <code>WeightingStrategy</code> interface added to this
     * interface.</p>
     *
     * @param original        a non-<code>null</code> image instance.
     * @param hits            a non-<code>null</code> LIRE search result.
     * @param scalePercentage value from 1-100d
     * @return the best match as determined by the relevancy
     *         and the relevancy weighting.
     * @throws IOException if the image could not be loaded.
     * @see edu.uniklu.itec.mosaix.engine.WeightingStrategy
     */
    public BufferedImage findBestMatch(final BufferedImage original, final ImageSearchHits hits, double scalePercentage) throws IOException {
        assert original != null;
        assert hits != null;

        //BufferedImage bestImage = null;
        WeightingData bestHit = null;
        float bestRating = Float.NEGATIVE_INFINITY;

        for (int i = 0; i < hits.length(); i++) {
            Document doc = hits.doc(i);
            String file = doc.getFieldable(DocumentBuilder.FIELD_NAME_IDENTIFIER).stringValue();
//			BufferedImage repl = ImageIO.read(new File(file));
            WeightingData data = weightingDataFactory_.newInstance(doc);
            data.setRelevancy(hits.score(i));
            data.setSlice(original);
            data.setId(file);
            data.setScalePercentage(scalePercentage);
//			data.setReplacement(repl);

            float weight = getWeightedRelevancy(data);
            if (outweightImageReuse) {
                if (file2occurence.containsKey(file))
                    weight *= 1f / (((float) file2occurence.get(file)) + 1f);
            }
//			Logging.log(this, "Rated " + file + " with " + Float.toString(weight));


            if (bestRating < weight) {
                bestRating = weight;
                bestHit = data;
            }
        }

//		Logging.log(this, "Enforcing Garbage Collection.");
//		System.gc(); // suppose, it's badly needed now
        for (EngineObserver observer : observer_)
            observer.notifyState(bestHit, EngineObserver.USED);

//		Logging.log(this, "Evaluation complete");
        if (outweightImageReuse) {
            if (file2occurence.containsKey(bestHit.getId()))
                file2occurence.put(bestHit.getId(), file2occurence.get(bestHit.getId()) + 1);
            else
                file2occurence.put(bestHit.getId(), 1);
        }
        return bestHit.getReplacement();
    }

    /**
     * Evaluates all weighting strategies for the
     * specified evaluation data.
     *
     * @param data the non-<code>null</code> data to evaluate.
     * @return the evaluated weighting to apply to the relevancy.
     */
    private float getWeighting(WeightingData data) {
        assert data != null;
        // Logging.log(this, "Weighting slice with " + strategies_.size() + " strategies.");
        float weight = 1.0f;
        for (WeightingStrategy strategy : strategies_)
            weight *= strategy.getFactor(data);
        return weight;
    }

    /**
     * Gets the weighted relevancy for the specified
     * weighting data instance.
     *
     * @param data the non-<code>null</code> data to evaluate.
     * @return the weighted relevancy within <code>0.0f</code>
     *         (worst case) and <code>1.0f</code> (best case).
     */
    private float getWeightedRelevancy(WeightingData data) {
        assert data != null;
        return data.getRelevancy() * getWeighting(data);
    }

    /**
     * Resets all strategies' state data to their initial
     * level.
     */
    public void reset() {
        // Logging.log(this, "Resetting state.");
        for (WeightingStrategy s : strategies_)
            s.reset();
    }

    /**
     * Switches the option if duplicate tile images (using the same images for a
     * tile more than once) should be avoided. It will still happen, but not so
     * often any more.
     *
     * @param avoid
     */
    public static void setAvoidDuplicateTileImages(boolean avoid) {
        outweightImageReuse = avoid;
    }

}
