package com.matrix;

/**
 * A rapture server. There must be only one instance of rapture server per JVM.
 *
 */
public interface RaptureServer {
    /**
     * Start the rapture server. must be executed only once per JVM.
     * @param args The command line args.
     */
    void start(String[] args);
}
