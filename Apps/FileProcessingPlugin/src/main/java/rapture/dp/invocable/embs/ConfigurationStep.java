package rapture.dp.invocable.embs;

import com.matrix.workflow.AbstractSingleOutcomeStep;
import org.apache.commons.lang3.StringUtils;
import rapture.common.CallingContext;
import rapture.common.dp.Steps;

public class ConfigurationStep extends AbstractSingleOutcomeStep {

    private rapture.dp.invocable.configuration.steps.ConfigurationStep configurationStep;

    public ConfigurationStep(String workerUri, String stepName) {
        super(workerUri, stepName);
        configurationStep = new rapture.dp.invocable.configuration.steps.ConfigurationStep(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext callingContext) throws Exception {
        String ret =  configurationStep.invoke(callingContext);
        if(!Steps.NEXT.equalsIgnoreCase(ret)) {
            String error = getContextValue(this.getErrName());
            String message = getContextValue(this.getStepName());
            throw new RuntimeException(String.format("Configuration error. \n%s\n%s", message, error));
        }
        String errorCode = getContextValue("WAREHOUSE_ERROR_CODE", true);
        if(StringUtils.isNoneBlank(errorCode)) {
            setContextLiteral("ERROR_CODE", errorCode);
        }
    }


}
