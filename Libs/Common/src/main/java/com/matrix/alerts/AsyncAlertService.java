package com.matrix.alerts;

import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.matrix.common.Config;
import org.apache.log4j.Logger;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Asynchronously sends alerts. Any errors in sending alerts are logged.
 */
public class AsyncAlertService implements AlertService {

    private final Logger log = Logger.getLogger(AsyncAlertService.class);

    private final AlertService delegate;

    private final ExecutorService executorService;

    public AsyncAlertService(AlertService alertService) {
        this(alertService, null);
    }

    public AsyncAlertService(AlertService alertService, ExecutorService executorService) {
        Preconditions.checkArgument(alertService != null, "alertService is null");
        if(executorService == null) {
            executorService =
                    Executors.newFixedThreadPool(10,
                    new ThreadFactoryBuilder()
                            .setNameFormat("alertservice-pool-%d")
                            .setUncaughtExceptionHandler((t, e) -> log.error("Error sending alert." + t, e)).build()
            );
        }
        this.executorService = executorService;
        this.delegate = alertService;
    }

    @Override
    public void sendAlert(String message, Throwable t) {
        executorService.execute(() -> delegate.sendAlert(message, t));
    }

    @Override
    public void sendAlert(String message) {
        executorService.execute(() -> delegate.sendAlert(message));

    }

    @Override
    public void sendAlert(Config config, String message, Throwable t) {
        executorService.execute(() -> delegate.sendAlert(config, message, t));

    }

    @Override
    public void sendAlert(Config config, String message) {
        executorService.execute(() -> delegate.sendAlert(config, message));

    }
}
