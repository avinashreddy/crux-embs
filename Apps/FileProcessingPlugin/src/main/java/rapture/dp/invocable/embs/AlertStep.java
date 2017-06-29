package rapture.dp.invocable.embs;

import com.matrix.alerts.AlertService;
import com.matrix.common.Config;
import com.matrix.common.ConfigList;
import com.matrix.common.ConfigUtils;
import com.matrix.common.MapConfig;
import com.matrix.common.ServiceLocator;
import com.matrix.common.WorkOrderContextConfig;
import com.matrix.workflow.AbstractTemplateStep;
import org.apache.commons.lang3.StringUtils;
import rapture.common.CallingContext;

import java.util.HashMap;
import java.util.Map;

public class AlertStep extends AbstractTemplateStep {

    private final AlertService alertService;

    public AlertStep(String workerUri, String stepName) {
        super(workerUri, stepName);
        alertService = ServiceLocator.getAlertService();
    }

    @Override
    protected String doProcess(CallingContext ctx) throws Exception {
        try {

            String message = getContextValue("MESSAGE_BODY");

            String code = StringUtils.stripToNull(getContextValue("EMBS_SUCCESS_CODE", true));

            Map<String, Object> mapConfig = new HashMap();
            if(code != null) {
                String subject =  getContextValue(code + "_SUBJECT", true);
                String recipients =  getContextValue(code + "_EMAIL_RECIPIENTS", true);
                if(StringUtils.isNoneBlank(subject)) {
                    mapConfig.put("MESSAGE_SUBJECT", subject);
                }
                if(StringUtils.isNoneBlank(recipients)) {
                    mapConfig.put("EMAIL_RECIPIENTS", recipients);
                }
            }

            Config config = new ConfigList(new MapConfig(mapConfig), new WorkOrderContextConfig(ctx, getWorkerURI()));

            message =  ConfigUtils.evalTemplateECF(new WorkOrderContextConfig(ctx, this.getWorkerURI()), message);

            alertService.sendAlert(config, message);
            return getNextTransition();
        } catch(Exception e) {
            log.error("Error sending alert", e);
            return getErrorTransition();
        }
    }
}