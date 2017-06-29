package com.matrix.logging;

import com.matrix.common.ServiceLocator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public final class NotifyingLogger2 {

    private static final String NOTIFYING_APPENDER_NAME = "NotifyingLogger";

    private static Level defaultAlertLevel = Level.ERROR;

    private NotifyingLogger2() {}

    public static Logger getLogger(Class<?> class1) {
        return getLogger(class1.getName(), defaultAlertLevel);
    }

    public static Logger getLogger(Class<?> class1, Level level) {
        return getLogger(class1.getName(), level);
    }

    public static Logger getLogger(String name) {
        return getLogger(name, defaultAlertLevel);
    }

    public static Logger getLogger(String name, Level level) {
        Logger logger = Logger.getLogger(name);
        register(logger, level);
        return logger;
    }


    public static void register(Logger logger, Level alertLevel) {
        // Remove any previous appender with the same name
        logger.removeAppender(NOTIFYING_APPENDER_NAME);
        //TODO: we will only need two ore three instances. Cache?
        NotifyingAppender appender = new NotifyingAppender(alertLevel, ServiceLocator.getAlertService());
        appender.setName(NOTIFYING_APPENDER_NAME);
        logger.addAppender(appender);
    }

    public static Level getDefaultAlertLevel() {
        return defaultAlertLevel;
    }

    public static void setDefaultAlertLevel(Level defaultAlertLevel) {
        NotifyingLogger2.defaultAlertLevel = defaultAlertLevel;
    }
}
