package com.matrix.common;

import com.google.common.base.Preconditions;
import com.matrix.alerts.AlertService;
import com.matrix.alerts.AlertServiceImpl;
import com.matrix.alerts.AsyncAlertService;

/**
 * A class for all effectively singleton services. A service typically is a singleton and creating new instances
 * is generally not required. Also, having more than one instance is harmless and in some cases desirable. So
 * making a service a true singleton might be restrictive.
 */
public class ServiceLocator {

    private static boolean initialized;

    private static AlertService alertService;

    private static AlertService asyncAlertService;

    private static Config defaultConfig;

    public static void init(Config config) {
        Preconditions.checkState(!initialized, "Already initialized.");
        Preconditions.checkState(config != null, "config is null.");
        defaultConfig = config;
        alertService = new AlertServiceImpl(config);
        asyncAlertService = new AsyncAlertService(alertService);
        initialized = true;
    }

    public static AlertService getAlertService() {
        return checkNull(alertService, "alertService");
    }

    public static AlertService getAsyncAlertService() {
        return checkNull(asyncAlertService, "asyncAlertService");
    }

    public static Config getDefaultConfig() {
        return defaultConfig;
    }

    private static <T> T checkNull(T service, String serviceName) {
        if(service == null) {
            throw new IllegalStateException("Could not find service " + serviceName);
        }
        return service;
    }

}
