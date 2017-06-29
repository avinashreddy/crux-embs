package com.matrix.alerts;

import com.google.common.base.Preconditions;
import com.matrix.common.Config;
import com.matrix.common.ConfigUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

public class AlertUtils {

    Logger log = Logger.getLogger(AlertUtils.class);

    private final Config config;

    private String separator = "_";

    public AlertUtils(Config config) {
        Preconditions.checkNotNull(config, "config is null");
        this.config = config;
    }

    public String getMessage(String... prefix) {
        return getVal("MESSAGE", prefix);
    }

    public String getSummary(String... prefix) {
        return getVal("SUMMARY", prefix);
    }

    public String getEmailSubject(String... prefix) {
        String ret =  getVal("SUBJECT", prefix);
        if(StringUtils.isBlank(ret)) {
            //this is for compatibility with messages that have not been updated
            ret = config.getString("MESSAGE_SUBJECT", null);
        }
        return ret;
    }

    public String getEmailRecipients(String... prefix) {
        return getVal("EMAIL_RECIPIENTS", prefix);
    }

    public String getErrorResolution(String... prefix) {
        String ret = getVal("RESOLUTION", prefix );
        if(StringUtils.isBlank(ret)) {
            //this is for compatibility with messages that have not been updated
            return config.getString("ERROR_RESOLUTION", null);
        }
        return ret;
    }

    private String getVal(String key, String... prefix) {
        String ret = null;
        for(String p : prefix) {
            ret = config.getString(p + separator + key, null);
            if(StringUtils.isNoneBlank(ret)) {
                break;
            }
        }
        if(ret == null) {
            ret = config.getString(key, null);
        }
        if(ret != null) {
            try {
                ret = ConfigUtils.evalTemplateECF(config, ret);
            } catch (Exception e) {
                log.debug(String.format("Ignoring error in parsing template '%s'", ret), e);
            }
        }
        return ret;
    }

    public void setSeparator(String separator) {
        if(StringUtils.isNoneBlank()) {
            this.separator = separator;
        }
    }
}
