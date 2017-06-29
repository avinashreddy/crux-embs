package com.matrix.logging;

import com.google.common.base.Preconditions;
import com.matrix.alerts.AlertService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

/**
 * An appends that sends log messages as alerts.
 *
 * @see AlertService
 */
public class NotifyingAppender extends AppenderSkeleton {

    private final Level alertLevel;

    private final AlertService alertService;

    public NotifyingAppender(Level alertLevel, AlertService alertService) {
        Preconditions.checkArgument(alertLevel != null, "alertLevel is null");
        Preconditions.checkArgument(alertService != null, "alertService is null");

        this.alertLevel = alertLevel;
        this.alertService = alertService;
    }

    @Override
    protected void append(LoggingEvent event) {
        Level level = event.getLevel();
        String message = (String) event.getMessage();
        if (level.isGreaterOrEqual(alertLevel)) {
            ThrowableInformation ti = event.getThrowableInformation();
            try {
                alertService.sendAlert(buildMessage(message, ti));
            } catch (Exception e) {
                Logger.getRootLogger().fatal("Cannot send notification ", e);
            }
        }
        Logger.getRootLogger().log(level, message);
    }

    private String buildMessage(String message, ThrowableInformation ti) {
        if(ti == null) return message;
        return message +
                System.lineSeparator() +
                StringUtils.join(ti.getThrowableStrRep(), System.lineSeparator());
    }

    @Override
    public void close() {
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }
}
