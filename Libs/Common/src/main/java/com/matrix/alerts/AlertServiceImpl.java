package com.matrix.alerts;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.matrix.Constants;
import com.matrix.common.Config;
import com.matrix.common.ConfigList;
import com.matrix.common.WorkOrderContextConfig;
import com.matrix.workflow.NotifiableException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import rapture.common.exception.ExceptionToString;
import rapture.kernel.Kernel;

import java.util.Map;

public class AlertServiceImpl implements AlertService {

    private final Logger log = Logger.getLogger(AlertServiceImpl.class);

    private final Map<String, AlertPublisher> alertPublishers = ImmutableMap.of(
            //TODO: nice to allow registering of new publishers at runtime.
            "EMAIL", new EMailAlertPublisher(),
            "SLACK", new SlackAlertPublisher(),
            "WORKFLOW", new WorkflowAlertPublisher()
    );

    private final Config defaultConfig;

    public AlertServiceImpl(Config defaultConfig) {
        Preconditions.checkArgument(defaultConfig != null, "config is null");
        this.defaultConfig = defaultConfig;
    }

    @Override
    public void sendAlert(String message, Throwable t) {
        sendAlert(defaultConfig, message, t);
    }

    @Override
    public void sendAlert(String message) {
        sendAlert(defaultConfig, message, null);

    }

    @Override
    public void sendAlert(Config config, String message) {
        sendAlert(config, message, null);
    }

    @Override
    public void sendAlert(final Config override, final String message, final Throwable t) {
        Config config = override == defaultConfig ? override : new ConfigList(override, defaultConfig);
        Preconditions.checkState(config != null, "config is null");

        final String types = config.getString("NOTIFY_TYPE");
        if(StringUtils.isBlank(types)) {
            log.warn("Cannot send alert. NOTIFY_TYPE not set");
            if(isWorkOrderContext(config)) {
                //If called from within a work-order, we simply log and ignore.
                updateWorkflowAudit(config, String.format("Parameter NOTIFY_TYPE is not set"), true);
            }
        }
        for (String type : types.split("[, ]+")) {
            AlertPublisher publisher = alertPublishers.get(type.toUpperCase());
            if(publisher == null) {
                log.error("Unsupported notification type: " + type);
                updateWorkflowAudit(config, "No AlertPublisher available for " + type, true);
            } else {
                try {
                    log.info("Publishing alert via " + type);
                    publisher.sendAlert(config, buildMessage(message, t));
                    updateWorkflowAudit(config, type + " notification sent successfully", false);
                }catch(Exception e) {
                    log.error("Error sending notification", e);
                    updateWorkflowAudit(config, String.format("%s notification failed. %s", type, ExceptionToString.format(e)), true);
                }
            }
        }

    }

    public boolean isWorkOrderContext(Config config) {
        return config instanceof WorkOrderContextConfig;
    }

    private void updateWorkflowAudit(Config config, String message, boolean isError) {
        if(config instanceof WorkOrderContextConfig) {
            WorkOrderContextConfig workOrderConfig = (WorkOrderContextConfig)config;
            String stepName =  workOrderConfig.getString(Constants.STEP_NAME, "");
            message = stepName == null ? message : stepName + ": " + message;
            Kernel.getDecision().writeWorkflowAuditEntry(
                    ((WorkOrderContextConfig)config).getCallingContext(),
                    ((WorkOrderContextConfig)config).getWorkerUri(),
                    message, isError);
        }
    }

    private String buildMessage(String message, Throwable t) {
        if(t instanceof NotifiableException) {
            t = t.getCause();
        }
        if(t == null) return message;
        return message +
                System.lineSeparator() +
                System.lineSeparator() +
                ExceptionToString.summary(t);
    }
}
