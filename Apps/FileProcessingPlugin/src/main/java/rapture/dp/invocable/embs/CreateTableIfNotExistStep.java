package rapture.dp.invocable.embs;

import com.crux.embs.FileProcessingRequest;
import rapture.common.CallingContext;

public class CreateTableIfNotExistStep extends AbstractSingleOutcomeEmbsStep {

    public CreateTableIfNotExistStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        synchronized (Integer.class) { //TODO: use lock api
            final FileProcessingRequest request = getFileProcessingRequest();
            getCruxApi().ensureTableExists(getDefaultCruxDatasetId(),
                    request.getTableConfig().getTableName(), request.getTableConfig().getSchema().trim());
        }
    }
}
