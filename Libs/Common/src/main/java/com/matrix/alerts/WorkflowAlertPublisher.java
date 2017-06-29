package com.matrix.alerts;

import com.google.common.base.Preconditions;
import com.matrix.common.Config;
import com.matrix.common.KernelServices;
import com.matrix.common.ServiceLocator;
import rapture.common.CallingContext;
import rapture.common.CreateResponse;
import rapture.common.api.DecisionApi;
import rapture.common.dp.Workflow;
import rapture.kernel.ContextFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * An alert publisher that sends alerts via the notification workflow.
 */
public class WorkflowAlertPublisher implements AlertPublisher {

    public static final String NOTIFY_WORKFLOW_DEFAULT = "workflow://workflows/notification/general";

    private void runWorkflow(String notifyWorkflow, String message) {

        DecisionApi decisionApi = KernelServices.getDecision();
        CallingContext context = ContextFactory.getKernelUser();
        if (notifyWorkflow == null) notifyWorkflow = NOTIFY_WORKFLOW_DEFAULT;

        Map<String, String> args = new HashMap<>();
        args.put("message", message);

        Workflow flow = decisionApi.getWorkflow(context, notifyWorkflow);
        Preconditions.checkState(flow != null, "Unable to find workflow " + notifyWorkflow);

        CreateResponse response = decisionApi.createWorkOrderP(context, notifyWorkflow, args, null);
        Preconditions.checkState(response.getIsCreated(), "Unable to create workorder for " + notifyWorkflow);

    }

    @Override
    public void sendAlert(Config config, String message) {
        runWorkflow(config.getString("NOTIFY_WORKFLOW", NOTIFY_WORKFLOW_DEFAULT), message);
    }

}
