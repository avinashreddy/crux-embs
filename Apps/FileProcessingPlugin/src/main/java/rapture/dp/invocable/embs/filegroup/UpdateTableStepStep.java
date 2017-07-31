package rapture.dp.invocable.embs.filegroup;

import com.crux.embs.FileGroupProcessingRequest;
import com.crux.embs.FileGroupProcessingRequestLookup;
import com.crux.embs.TableConfig;
import rapture.common.CallingContext;
import rapture.dp.invocable.embs.AbstractSingleOutcomeEmbsStep;

public class UpdateTableStepStep extends AbstractSingleOutcomeEmbsStep {


    public UpdateTableStepStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext callingContext) throws Exception {

        final FileGroupProcessingRequest requestGroup = FileGroupProcessingRequestLookup.get(this.ctx, getContextValue("requestURI"));

        getCruxApi().mergeTable(getDefaultCruxDatasetId(),
                requestGroup.getTempTable(),
                requestGroup.getTable(), requestGroup.getTableUpdateKey());

    }

}
