package net.semanticmetadata.lire.utils;

/**
 * Simple stop watch for taking the time of a process. Inspired by the Apache Commons Lang version.
 *
 * @author Mathias Lux, mathias@juggle.at, 04.09.2015.
 */
public class StopWatch {
    long sumTime = 0, startTime;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public void stop() {
        sumTime += System.currentTimeMillis() - startTime;
    }

    public long getTime() { return sumTime;}
}
