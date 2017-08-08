package rapture.dp.invocable.embs.filegroup;

import com.crux.embs.FileGroupProcessingRequest;
import com.crux.embs.FileGroupProcessingRequestLookup;
import com.crux.embs.FileProcessingRequest;
import com.google.common.collect.Lists;
import rapture.common.CallingContext;
import rapture.common.RapturePipelineTask;
import rapture.common.dp.Steps;
import rapture.dp.invocable.embs.AbstractEmbsStep;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;

public class PublishFileProcessingRequestsStep extends AbstractEmbsStep {

    public PublishFileProcessingRequestsStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected String doProcess(CallingContext callingContext) throws Exception {

        final FileGroupProcessingRequest requestGroup = FileGroupProcessingRequestLookup.get(this.ctx, getContextValue("requestURI"));

        final String table = requestGroup.getTable();
        log.info(String.format("Target table is [%s]", table));
        int rowCount = 0;

        if (!getCruxApi().tableExists(getDefaultCruxDatasetId(), table)) {
            log.info(String.format("Table [%s] does not exist.", table));
            getCruxApi().createTable(getDefaultCruxDatasetId(), table, requestGroup.getTableSchema());
        } else {
            rowCount = getCruxApi().getRowCount(getDefaultCruxDatasetId(), table);
        }
        log.info(String.format("[%s] has [%s] rows.", table, rowCount));

        if(rowCount > 0) {
            createTempTable(requestGroup.getTempTable(), requestGroup.getTableSchema());
        }

        for (FileProcessingRequest request : requestGroup.getFileProcessingRequests()) {
            request = request.clone();
            request.setPartOfGroup(true);
            if(rowCount > 0) {
                request.getTableConfig().setTableName(requestGroup.getTempTable());
            }

            //TODO: code copied from FilePoller
            RapturePipelineTask task = new RapturePipelineTask();
            task.setContentType("text/vnd.rapture.ftp.embs.file");
            task.setContent(request.toJSON());
            task.setCategoryList(Lists.newArrayList("embs"));
            log.info("Publishing file processing request " + request.toJSON());
            Kernel.getPipeline().publishMessageToCategory(ContextFactory.getKernelUser(), task);
        }
        if (rowCount > 0) {
            return Steps.NEXT;
        } else {
            log.info("No tables updates required. End of workflow.");
            return Steps.FINISH;
        }
    }

    private void createTempTable(String tableName, String schema) {
        if (getCruxApi().tableExists(getDefaultCruxDatasetId(), tableName)) {
            getCruxApi().deleteResource(getDefaultCruxDatasetId(), tableName);
        }
        getCruxApi().createTable(getDefaultCruxDatasetId(), tableName, schema);
    }
}
