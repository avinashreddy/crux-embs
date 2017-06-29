package com.matrix.workflow;

import com.google.common.base.Preconditions;
import com.matrix.common.Config;
import com.matrix.common.ConfigList;
import com.matrix.common.ServiceLocator;
import org.apache.commons.lang3.StringUtils;

/**
 * An exception that can be sent as an alert.
 *
 * @see AbstractTemplateStep
 * @see com.matrix.alerts.AlertUtils
 */
public class NotifiableException extends RuntimeException {

    /**
     * The error code is used to determine the error message and in the case of email alerts the subject and recipient list.
     */
    private final String errorCode;

    private Config config;

    public NotifiableException(String code, String reason, Throwable cause) {
        super(reason, cause);
        Preconditions.checkArgument(StringUtils.isNotBlank(code), "code is null/empty");
        this.errorCode = code;
    }

    public NotifiableException(String code) {
        this(code, null, null);
    }

    public NotifiableException(String code, String reason) {
        this(code, reason, null);
    }

    public NotifiableException(String code, Throwable cause) {
        this(code, null, cause);
    }

    public String getErrorCode() {
        return errorCode;
    }

    public NotifiableException setConfig(Config config) {
        if(this.config == null) {
            this.config = ServiceLocator.getDefaultConfig();
        }
        this.config = new ConfigList(config, ServiceLocator.getDefaultConfig());
        return this;
    }

    public Config getConfig() {
        return config;
    }

}