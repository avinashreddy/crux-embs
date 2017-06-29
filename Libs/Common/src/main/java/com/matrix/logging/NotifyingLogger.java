package com.matrix.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.mail.MessagingException;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Appender;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import com.google.common.net.MediaType;

import rapture.common.CallingContext;
import rapture.common.CreateResponse;
import rapture.common.api.DecisionApi;
import rapture.common.dp.Workflow;
import rapture.common.impl.jackson.JacksonUtil;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;
import rapture.mail.Mailer;

/**
 * Use {@link NotifyingLogger2} instead.
 */
@Deprecated
public class NotifyingLogger {

    static Level alertLevel = Level.ERROR;
    static Map<String, String> configuration = new HashMap<>();

    public NotifyingLogger(Level level, Map<String, Object> configuration) {
        setConfiguration(configuration);
        setAlertLevel(level);
    }

    public static Logger getLogger(Class<?> class1) {
        return getLogger(class1.getName());
    }

    public static Logger getLogger(String name) {
        Logger logger = Logger.getLogger(name);
        register(logger);
        return logger;
    }

    static Appender app;
    static {
        app = new AppenderSkeleton() {
            @Override
            protected void append(LoggingEvent event) {
                Level level = event.getLevel();
                String message = (String) event.getMessage();
                boolean success = false;
                if (level.isGreaterOrEqual(alertLevel)) {
                    ThrowableInformation ti = event.getThrowableInformation();

                    String types = stripToNull(configuration.get("NOTIFY_TYPE"));
                    if (types != null) for (String type : types.split("[, ]+")) {
                        try {
                            if (type.equalsIgnoreCase("SLACK")) {
                                success = sendSlack(message, ti);
                                if (!success) Logger.getRootLogger().log(level, "Cannot send slack notification ");
                            }
                            if (type.equalsIgnoreCase("WORKFLOW")) {
                                success = runWorkflow(message, ti);
                                if (!success) Logger.getRootLogger().log(level, "Cannot create workflow notification ");
                            }
                            if (type.equalsIgnoreCase("EMAIL")) {
                                success = sendEmail(message, ti);
                                if (!success) Logger.getRootLogger().log(level, "Cannot send email notification ");
                            }
                        } catch (IOException e) {
                            Logger.getRootLogger().fatal("Cannot send notification " + e);
                        }
                    }
                }
                Logger.getRootLogger().log(level, message);
            }

            @Override
            public void close() {
            }

            @Override
            public boolean requiresLayout() {
                return false;
            }
        };
        app.setName("NotifyingLogger");
    }

    public static void register(Logger logger) {
        // Remove any previous appender with the same name
        logger.removeAppender(app.getName());
        logger.addAppender(app);
    }

    public static Level getAlertLevel() {
        return alertLevel;
    }

    public static void setAlertLevel(Level aLevel) {
        alertLevel = aLevel;
    }

    public Map<String, String> getConfiguration() {
        return configuration;
    }

    /**
     * You can either pass the configuration as a map or pass in the CalliongContext and Worker URI and I'll read the values from the Context
     **/
    public static void setConfiguration(Map<String, Object> conf) {
        if (conf != null) for (Entry<String, Object> entry : conf.entrySet())
            configuration.put(entry.getKey(), stripToNull(entry.getValue()));
    }

    public static void setConfiguration(CallingContext ctx, String uri) {
        configuration.put("NOTIFY_TYPE", Kernel.getDecision().getContextValue(ctx, uri, "NOTIFY_TYPE"));
        configuration.put("SLACK_WEBHOOK", Kernel.getDecision().getContextValue(ctx, uri, "SLACK_WEBHOOK"));
        configuration.put("NOTIFY_WORKFLOW", Kernel.getDecision().getContextValue(ctx, uri, "NOTIFY_WORKFLOW"));
        configuration.put("MESSAGE_SUBJECT", Kernel.getDecision().getContextValue(ctx, uri, "MESSAGE_SUBJECT"));
        configuration.put("EMAIL_RECIPIENTS", Kernel.getDecision().getContextValue(ctx, uri, "EMAIL_RECIPIENTS"));
    }

    static String stripToNull(Object o) {
        if (o == null) return null;
        return StringUtils.stripToNull(o.toString());
    }

    static private int doPost(URL url, byte[] body) throws IOException {
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setFixedLengthStreamingMode(body.length);
        http.setRequestProperty("Content-Type", MediaType.JSON_UTF_8.toString());
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.connect();
        try (OutputStream stream = http.getOutputStream()) {
            stream.write(body);
        }
        int response = http.getResponseCode();
        http.disconnect();
        return response;
    }

    static boolean sendSlack(String message, ThrowableInformation ti) throws IOException {
        String webhook = stripToNull(configuration.get("SLACK_WEBHOOK"));
        if (webhook == null) {
            Logger.getRootLogger().fatal("No webhook specified");
            return false;
        }

        URL url = new URL(webhook);
        Map<String, String> slackNotification = new HashMap<>();
        slackNotification.put("text", message);
        int response = doPost(url, JacksonUtil.bytesJsonFromObject(slackNotification));
        if (response != 200) {
            Logger.getRootLogger().fatal("slack notification failed with HTTP error code " + response);
            return false;
        }
        return true;
    }

    static boolean sendEmail(String message, ThrowableInformation ti) {
        String subject = stripToNull(configuration.get("MESSAGE_SUBJECT"));
        String recipientList = stripToNull(configuration.get("EMAIL_RECIPIENTS"));

        if (recipientList == null) {
            Logger.getRootLogger().fatal("No recipient specified");
            return false;
        }

        try {
            Mailer.email(recipientList.split("[, ]+"), (subject == null) ? "Error from Rapture" : subject, message);
        } catch (MessagingException e) {
            Logger.getRootLogger().fatal("Unable to send email", e);
            return false;
        }
        return true;
    }

    public static final String NOTIFY_WORKFLOW_DEFAULT = "workflow://workflows/notification/general";

    static boolean runWorkflow(String message, ThrowableInformation ti) {

        DecisionApi decisionApi = Kernel.getDecision();
        CallingContext context = ContextFactory.getKernelUser();
        String notifyWorkflow = stripToNull(configuration.get("NOTIFY_WORKFLOW"));
        if (notifyWorkflow == null) notifyWorkflow = NOTIFY_WORKFLOW_DEFAULT;
        
        Map<String, String> args = new HashMap<>(configuration);
        args.put("Message", message);
        args.put("MESSAGE_BODY", message);

        Workflow flow = decisionApi.getWorkflow(context, notifyWorkflow);
        if (flow == null) {
            Logger.getRootLogger().fatal("Unable to find workflow " + notifyWorkflow);
            return false;
        }

        CreateResponse response = decisionApi.createWorkOrderP(context, notifyWorkflow, args, null);
        return response.getIsCreated();
    }

}
