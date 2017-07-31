package com.matrix.workflow;

import com.google.common.base.Preconditions;
import com.matrix.Constants;
import com.matrix.alerts.AlertService;
import com.matrix.alerts.AlertUtils;
import com.matrix.common.Config;
import com.matrix.common.ConfigList;
import com.matrix.common.ConfigUtils;
import com.matrix.common.MapConfig;
import com.matrix.common.ServiceLocator;
import com.matrix.common.WorkOrderContextConfig;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.stringtemplate.v4.ST;
import rapture.common.BlobContainer;
import rapture.common.CallingContext;
import rapture.common.RaptureURI;
import rapture.common.dp.Steps;
import rapture.common.dp.Worker;
import rapture.common.dp.WorkerPathBuilder;
import rapture.common.dp.WorkerStorage;
import rapture.common.exception.ExceptionToString;
import rapture.dp.AbstractStep;
import rapture.kernel.DecisionApiImplWrapper;
import rapture.kernel.Kernel;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Template method for steps.
 */
public abstract class AbstractTemplateStep extends AbstractStep {

    static final DecisionApiImplWrapper decisionApi = Kernel.getDecision();

    public AbstractTemplateStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    //A Step by design is not multithreaded. So holding on to the context is safe. We can still use a thread local to allow mutli threaded acces.
    protected CallingContext ctx;

    protected AlertService alertService;

    @Override
    public void preInvoke(CallingContext ctx) {
        super.preInvoke(ctx);
        this.ctx = ctx;
        alertService = ServiceLocator.getAlertService();
    }

    @Override
    public final String invoke(CallingContext ctx) {
        Preconditions.checkArgument(ctx != null, "callingContext is null");
        this.ctx = ctx;
        try {
            setContextLiteral(Constants.STEP_NAME, getStepName());
            log.info("Running STEP : " + getStepName());
            String ret = doProcess(ctx);
            log.info("STEP Completed : " + getStepName() + ". Return val is " + ret);
            return ret;
        } catch (Exception e) {
            log.error("Error in " + getStepName(), e);
            handleException(e);
            return Steps.ERROR;
        } catch (Throwable t) {
            log.error("Error in " + getStepName(), t);
            throw t;
        }
    }

    //All steps that have two or more outcome will override this method.
    protected abstract String doProcess(CallingContext ctx) throws Exception;

    protected void handleException(Exception e) {
        try {
            writeWorkflowAuditEntry(String.format("Problem in %s : %s", getStepName(), ExceptionToString.format(e)), true);
            sendAlert(e);
        }catch(Exception ex) {
            log.error("Error sending alert", ex);
        }
    }


    protected void sendAlert(Exception e) {
        //The error detail is used in email/alert template.
        setContextLiteral("ERRORDETAIL", e.getMessage());
        String errorCode;
        Config config = null;
        if(e instanceof NotifiableException) {
            errorCode = ((NotifiableException) e).getErrorCode();
            config = ((NotifiableException) e).getConfig();
        } else {
            errorCode = getContextValue("ERROR_CODE", true);
        }
        if(config == null) {
            config = new WorkOrderContextConfig(ctx, getWorkerURI());
        }
        final List<String> prefixes = new ArrayList<>();
        if(StringUtils.isNoneBlank(errorCode)) {
            prefixes.add(errorCode);
        }
        prefixes.add(getStepName() + "_ERROR");
        prefixes.add("DEFAULT_ERROR");
        final String[] prefixArr = prefixes.toArray(new String[] {});
        AlertUtils alertUtils = new AlertUtils(config);
        String subject = alertUtils.getEmailSubject(prefixArr);
        final String recipients = alertUtils.getEmailRecipients(prefixArr);
        if(StringUtils.isBlank(subject)) {
            subject = getContextValue("JOBNAME") + " Failure";
        }
        String message = getContextValue("MESSAGE_BODY", true);//TODO: RENAME TO ERROR_MESSAGE_BODY
        if(StringUtils.isBlank(message)) {
            message = buildMessageBody(prefixArr, alertUtils);
        }

        final Map<String, Object> map = new HashedMap();
        map.put("MESSAGE_SUBJECT", subject);
        map.put("EMAIL_RECIPIENTS", recipients);
        alertService.sendAlert(new ConfigList(new MapConfig(map), config), message, e);
    }

    private String buildMessageBody(String[] prefixArr, AlertUtils alertUtils) {
        try {
            InputStream inputStream = this.getClassLoader().getResourceAsStream("com/matrix/alerts/defaultErrorAlert.txt");
            Preconditions.checkState(inputStream != null, "classpath resource 'com/matrix/alerts/defaultErrorAlert.txt' not found.");
            String template = IOUtils.toString(inputStream, StandardCharsets.UTF_8.name());
            ST st = new ST(template, '$', '$');
            st.add(Constants.SERVER_HOST_NAME, ServiceLocator.getDefaultConfig().getString(Constants.SERVER_HOST_NAME, "NA"));
            st.add("MESSAGE_PREFIX", alertUtils.getSummary(prefixArr));
            st.add("JOB_NAME", getContextValue(Constants.JOB_NAME));
            st.add("STEP_NAME", getStepName());
            st.add("WORKORDER_URI", getContextValue("EXTERNALRIMWORKORDERURL"));
            st.add("ERROR_RESOLUTION", alertUtils.getErrorResolution(prefixArr));
            st.add("ERROR_MESSAGE", getContextValue("ERRORDETAIL"));
            return st.render();
        }catch(Exception e) {
            log.warn("Ignoring error in building error message.", e);
            return null;
        }
    }

    public Worker getWorker() {
        return getWorker(getWorkerURI());
    }

    public static Worker getWorker(String workerUri) {
        String workOrderURI = workerUri.substring(0, workerUri.lastIndexOf('#'));
        String id = workerUri.substring(workOrderURI.length() + 1);
        RaptureURI storageLocation
                = new WorkerPathBuilder()
                .workOrderURI(workOrderURI)
                .id(id)
                .buildStorageLocation();

        return WorkerStorage.readByStorageLocation(storageLocation);
    }

    protected String getContextValue(String key) {
        return getContextValue(key, false);
    }

    protected String getContextValue(String key, boolean allowNull) {
        String ret = Kernel.getDecision().getContextValue(ctx, getWorkerURI(), key);
        if(!allowNull) {
            Preconditions.checkState(ret != null, "null value for key " + key);
        }
        return ret;
    }

    protected void setContextLiteral(String varAlias, String literalValue) {
        decisionApi.setContextLiteral(ctx, getWorkerURI(), varAlias, literalValue);
    }

    public void putBlob(String blobUri, byte[] content, String contentType) {
        Kernel.getBlob().putBlob(ctx, blobUri, content, contentType);
    }

    public BlobContainer getBlob(String blobUri) {
        BlobContainer b = Kernel.getBlob().getBlob(ctx, blobUri);
        Preconditions.checkState(b != null, "Null value for BLOB at " + blobUri);
        return b;
    }

    public void writeWorkflowAuditEntry(String message, Boolean error) {
        decisionApi.writeWorkflowAuditEntry(ctx, getWorkerURI(), message, error);
    }
}
