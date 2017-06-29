package com.matrix.alerts;

import com.matrix.common.Config;

/**
 * The API for sending alerts.
 *
 * @see AlertPublisher
 */
public interface AlertService {

    /**
     * Sends alert with the default configuration.
     *
     * @param message The message.
     * @param t The exception. Can be null.
     */
    void sendAlert(String message, Throwable t);

    /**
     * Sends alert with the default configuration.
     *
     * @param message The message.
     */
    void sendAlert(String message);

    /**
     * Sends alert with the default configuration.
     *
     * @param message The message.
     * @param message The configuration to use. Configuration is implementation dependent. See {@link AlertPublisher} implementation to see how configuration is used.
     * @param t The exception. Can be null.
     */
    void sendAlert(Config config, String message, Throwable t);

    /**
     * Sends alert with the default configuration.
     *
     * @param message The message.
     * @param message The configuration to use. Configuration is implementation dependent. See {@link AlertPublisher} implementation to see how configuration is used.
     */
    void sendAlert(Config config, String message);

}
