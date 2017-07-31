package rapture.dp.invocable.embs;

import rapture.common.CallingContext;

public abstract class AbstractSingleOutcomeEmbsStep extends AbstractEmbsStep {

    public AbstractSingleOutcomeEmbsStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    protected final String doProcess(CallingContext ctx) throws Exception {
        this.execute(ctx);
        return "next";
    }

    protected abstract void execute(CallingContext var1) throws Exception;
}
