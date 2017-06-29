package rapture.dp.invocable.notification.steps;

import com.matrix.alerts.AlertService;
import com.matrix.common.Config;
import com.matrix.common.ConfigUtils;
import com.matrix.common.ServiceLocator;
import com.matrix.common.WorkOrderContextConfig;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import com.matrix.workflow.AbstractTemplateStep;
import org.apache.commons.lang3.StringUtils;
import rapture.common.CallingContext;
import rapture.common.exception.ExceptionToString;

/**
 * Analogous to  NotificationStep, but uses {{@link AlertService}}
 */
public class AlertStep extends AbstractTemplateStep {

    private final AlertService alertService;

    public AlertStep(String workerUri, String stepName) {
        super(workerUri, stepName);
        alertService = ServiceLocator.getAlertService();
    }

    @Override
    protected String doProcess(CallingContext ctx) throws Exception {
        try {
            Config config = new WorkOrderContextConfig(ctx, getWorkerURI());
            String message = StringUtils.stripToNull(getContextValue("MESSAGE_BODY"));
            message =  ConfigUtils.evalTemplateECF(new WorkOrderContextConfig(ctx, this.getWorkerURI()), message);

            alertService.sendAlert(config, message);
            return getNextTransition();
        }catch(Exception e) {
            log.error("Error sending alert", e);
            return getErrorTransition();
        }
    }
}
