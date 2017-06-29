package com.matrix.common;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import rapture.common.CallingContext;
import rapture.common.api.DecisionApi;
import rapture.kernel.Kernel;

/**
 * A convenient type safe wrapper for work order configurations.
 */
public class WorkOrderContextConfig extends AbstractConfig {

    private final CallingContext callingContext;

    private final String workerUri;

    private DecisionApi decisionApi;

    public WorkOrderContextConfig(CallingContext callingContext, String workerUri) {
        Preconditions.checkArgument(callingContext != null, "callingContext is null");
        Preconditions.checkArgument(StringUtils.isNotBlank(workerUri), "workerUri is null/empty");
        this.callingContext = callingContext;
        this.workerUri = workerUri;
        decisionApi = Kernel.getDecision();
    }

    @Override
    public Object get(String key) {
        return decisionApi.getContextValue(callingContext, workerUri, key);
    }

    public CallingContext getCallingContext() {
        return callingContext;
    }

    public String getWorkerUri() {
        return workerUri;
    }
}
