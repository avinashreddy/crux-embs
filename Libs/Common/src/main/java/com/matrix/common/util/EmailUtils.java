package com.matrix.common.util;

import java.util.Map;

import javax.activation.DataSource;

import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.MultiPartEmail;
import org.apache.log4j.Logger;

import rapture.common.CallingContext;
import rapture.common.impl.jackson.JacksonUtil;
import rapture.kernel.Kernel;

public class EmailUtils {

    private static final Logger log = Logger.getLogger(EmailUtils.class);

    public static boolean sendEmail(CallingContext ctx, String to, String subject, String msg, DataSource ds, String attachmentName,
            String attachmentDescription) {
        Map<String, Object> configs = JacksonUtil.getMapFromJson(Kernel.getDoc().getDoc(ctx, "document://configs/matrix/prfs/emailConfig"));
        String emailFrom = (String) configs.get("emailfrom");
        String emailSmtp = (String) configs.get("emailsmtp");
        Integer emailSmtpPort = (Integer) configs.get("emailsmtpport");
        String emailSmtpUser = (String) configs.get("emailsmtpuser");
        String emailSmtpPassword = (String) configs.get("emailsmtppassword");

        MultiPartEmail email = new MultiPartEmail();
        email.setHostName(emailSmtp);
        email.setSmtpPort(emailSmtpPort);
        email.setAuthentication(emailSmtpUser, emailSmtpPassword);
        try {
            email.addTo(to);
            email.setFrom(emailFrom);
            email.setSubject(subject);
            email.setMsg(msg);
            if (ds != null) {
                email.attach(ds, attachmentName, attachmentDescription);
            }
            email.send();
        } catch (EmailException e) {
            log.error(e);
            return false;
        }
        return true;
    }
}