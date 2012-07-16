package edu.uniklu.itec.mosaix.engine;

/**
 * Outlines an algorithm roughly following the
 * Strategy pattern. This pattern is used to
 * take further data into account to adapt the
 * relevancy factor provided by the LIRE
 * framework.
 *
 * @author Manuel Warum
 * @version 1.0
 */
public interface WeightingStrategy {
    /**
     * Gets the weighting factor for the specified
     * evaluation data.
     *
     * @param data - a non-<code>null</code> WeightingData instance.
     * @return the factor to modify the relevancy factor by.
     */
    public float getFactor(WeightingData data);

    /**
     * Resets the implementation of the strategy
     * to it's initial state.
     */
    public void reset();
}
