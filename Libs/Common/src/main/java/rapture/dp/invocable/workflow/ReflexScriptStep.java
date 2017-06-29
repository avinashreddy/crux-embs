package rapture.dp.invocable.workflow;

import com.google.common.collect.Maps;
import com.matrix.workflow.AbstractTemplateStep;
import rapture.common.CallingContext;
import rapture.common.RaptureScript;
import rapture.common.RaptureURI;
import rapture.common.dp.ContextVariables;
import rapture.common.dp.StepRecord;
import rapture.common.dp.Worker;
import rapture.common.exception.RaptureExceptionFactory;
import rapture.dp.InvocableUtils;
import rapture.kernel.Kernel;
import rapture.script.reflex.ReflexRaptureScript;

import java.util.Map;

public class ReflexScriptStep extends AbstractTemplateStep {

    public ReflexScriptStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected String doProcess(CallingContext ctx) throws Exception {
        Worker worker = getWorker();
        String executable = getContextValue("REFLEX_SCRIPT");

        String workerAuditUri = InvocableUtils.getWorkflowAuditUri(getWorkerURI());
        RaptureScript script = Kernel.getScript().getScript(ctx, executable);
        if (script == null) {
            throw RaptureExceptionFactory.create(String.format("Executable [%s] not found for step [%s]", executable, getStepName()));
        }
        ReflexRaptureScript rScript = new ReflexRaptureScript();
        if (workerAuditUri != null) {
            rScript.setAuditLogUri(workerAuditUri);
        }
        String auditLogUri = InvocableUtils.getWorkflowAuditLog(InvocableUtils.getAppStatusName(worker), worker.getWorkOrderURI(), getStepName());
        Object result = rScript.runProgram(ctx, null, script, createScriptValsMap(worker, getWorkerURI(), getStepName(), auditLogUri), -1);
        return (result == null) ? "" : result.toString(); //TODO: can we throw an error from reflex.
    }

    private Map<String, Object> createScriptValsMap(Worker worker, String workerUriString, String stepName, String auditLogUri) {
        Map<String, Object> extraVals = Maps.newHashMap();

        String workOrderUri = worker.getWorkOrderURI();
        extraVals.put(ContextVariables.DP_WORK_ORDER_URI, workOrderUri);

        RaptureURI workerURI = new RaptureURI(workerUriString);
        extraVals.put(ContextVariables.DP_WORKER_URI, workerURI.toString());
        extraVals.put(ContextVariables.DP_WORKER_ID, workerURI.getElement());
        extraVals.put(ContextVariables.DP_AUDITLOG_URI, auditLogUri);

        extraVals.put(ContextVariables.DP_STEP_NAME, stepName);
//        extraVals.put(ContextVariables.DP_STEP_START_TIME, stepRecord.getStartTime());
        return extraVals;
    }
}
