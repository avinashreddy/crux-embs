package com.matrix.workflow;

import rapture.common.CallingContext;
import rapture.common.dp.Steps;

/**
 * Steps that always return {@link Steps#NEXT} can extend this class.
 */
public abstract class AbstractSingleOutcomeStep extends AbstractTemplateStep {


    public AbstractSingleOutcomeStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    protected final String doProcess(CallingContext ctx) throws Exception {
        execute(ctx);
        return Steps.NEXT;
    }

    protected abstract void execute(CallingContext ctx) throws Exception;
}
