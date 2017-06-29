package com.matrix.alerts;

import com.google.common.base.Preconditions;
import com.matrix.Constants;
import com.matrix.common.ConfigUtils;
import com.matrix.common.Config;
import com.matrix.common.WorkOrderContextConfig;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import rapture.mail.Mailer;

import javax.mail.MessagingException;
import static com.matrix.Constants.*;
/**
 * An alert publisher that sends email alerts.
 */
public class EMailAlertPublisher implements AlertPublisher {

    static void sendEmail(String subject, String recipientList, String message) {
        Preconditions.checkArgument(StringUtils.isNotBlank(subject), "subject is null/empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(recipientList), "recipientList is null/empty");
        Preconditions.checkArgument(StringUtils.isNotBlank(message), "message is null/empty");

        try {
            Mailer.email(recipientList.split("[, ]+"), (subject == null) ? "Alert from Rapture" : subject, message);
        } catch (MessagingException e) {
            throw new IllegalStateException("Unable to send email", e);
        }
    }

    @Override
    public void sendAlert(Config config, String message) {
        Preconditions.checkArgument(config != null, "config is null");
        if(message == null) {
            message = "";
        }

        String recipientList = ConfigUtils.getTemplateVal(config, "EMAIL_RECIPIENTS");
        String subject = ConfigUtils.getTemplateVal(config, "MESSAGE_SUBJECT");

        if(isTrue(config, PREFIX_ENV_TO_MAIL_SUBJECT, true)) {
           String env = config.getString(SERVER_ENV, null);
           if(env != null) {
                subject = "(" + env + ") " + subject;
           }
        }
        if(isTrue(config, INJECT_HOST_NAME_IN_MESSAGE, true)) {
            String hostName = config.getString(SERVER_HOST_NAME, null);
            if(hostName != null && !message.contains(hostName)) {
                message = String.format("Host : %s\n%s", hostName, message);
            }
        }

        sendEmail(StringUtils.isNoneBlank(subject) ? subject : "Alert from Rapture", recipientList, message);
    }

    private boolean isTrue(Config config, String varName, boolean defaultVal) {
        return config.<Boolean>get(varName, Boolean.class, defaultVal);
    }

}
