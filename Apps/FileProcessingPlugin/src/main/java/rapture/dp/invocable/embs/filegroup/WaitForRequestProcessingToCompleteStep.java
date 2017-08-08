package rapture.dp.invocable.embs.filegroup;

import com.crux.embs.FileGroupProcessingRequest;
import com.crux.embs.FileGroupProcessingRequestLookup;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.base.Preconditions;
import rapture.common.CallingContext;
import rapture.dp.invocable.embs.AbstractSingleOutcomeEmbsStep;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class WaitForRequestProcessingToCompleteStep extends AbstractSingleOutcomeEmbsStep {

    public WaitForRequestProcessingToCompleteStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext callingContext) throws Exception {
        final FileGroupProcessingRequest requestGroup = FileGroupProcessingRequestLookup.get(this.ctx, getContextValue("requestURI"));

        List<String> sourceFileNames = requestGroup.getFileProcessingRequests().stream().map(r -> r.getSourceFileColumnVal()).collect(Collectors.toList());

        while(!allFilesInGroupLoaded(sourceFileNames, requestGroup.getTempTable())) {
            log.info("Waiting for all files in group to be loaded....");
            try {
                Thread.sleep(10_000);
            } catch(InterruptedException e) {
                log.error("Ignoring InterruptedException.", e);
                //just ignore!
            }
        }
        log.info("All files in group loaded");
    }

    private boolean allFilesInGroupLoaded(List<String> sourceFileNames, String table) {
        Preconditions.checkArgument(getCruxApi().tableExists(getDefaultCruxDatasetId(), table) , "table does not exist [%s]", table);
        final String query = "select count(*) row_count from $%s where source_file = '%s'";

        for(String fileName : sourceFileNames) {
            List<Map<String, Object>> rows = (List<Map<String, Object>>) getCruxApi().runAdhocQuery(
                    getDefaultCruxDatasetId(),
                    String.format(query, table, fileName),
                    new TypeReference<List<Map<String, Object>>>() {
                    });
            if(0 == (Integer)rows.get(0).get("row_count")) {
                log.info(fileName + " not yet loaded");
                return false;
            }
        }
       return true;
    }
}
