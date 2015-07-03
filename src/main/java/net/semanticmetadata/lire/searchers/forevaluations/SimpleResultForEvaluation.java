package net.semanticmetadata.lire.searchers.forevaluations;

import net.semanticmetadata.lire.searchers.SimpleResult;

/**
 * Created by Nektarios on 11/6/2015.
 *
 * @author Nektarios
 */
public class SimpleResultForEvaluation extends SimpleResult {
    private String path;

    public SimpleResultForEvaluation(double distance, int indexNumber, String path) {
        super(distance, indexNumber);
        this.path = path;
    }

    public String getPath() {
        return path;
    }
}
