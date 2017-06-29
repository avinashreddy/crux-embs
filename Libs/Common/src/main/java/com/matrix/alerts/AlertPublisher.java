package com.matrix.alerts;

import com.matrix.common.Config;

/**
 * Handles the sending of alerts to destinations - email, slack, workflow etc.
 *
 * @see AlertService
 */
public interface AlertPublisher {

    /**
     * Sends the alert.
     *
     * @param config - The configuration. What params should be present is implementation dependent. Cannot be null.
     * @param message - The message.
     */
    void sendAlert(Config config, String message);

}
