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

        final boolean requiresUpdate;
        if(!getCruxApi().tableExists(getDefaultCruxDatasetId(), table)) {
            log.info(String.format("Table [%s] does not exist.", table));
            getCruxApi().createTable(getDefaultCruxDatasetId(), table, requestGroup.getTableSchema());
            requiresUpdate = false;
        } else {
            final int rowCount = getCruxApi().getRowCount(getDefaultCruxDatasetId(), table);
            requiresUpdate = true;
            if(getCruxApi().tableExists(getDefaultCruxDatasetId(), requestGroup.getTempTable())) {
                getCruxApi().deleteResource(getDefaultCruxDatasetId(), requestGroup.getTempTable());
            }
            getCruxApi().createTable(getDefaultCruxDatasetId(), requestGroup.getTempTable(), requestGroup.getTableSchema());
            if(rowCount > 0) {
                log.info(String.format("[%s] has [%s] rows. Table requires an update.", table, requestGroup));
            }
        }

        for (FileProcessingRequest request : requestGroup.getFileProcessingRequests()) {
            if(requiresUpdate) {
                request = request.clone();
                request.getTableConfig().setTableName(requestGroup.getTempTable());
                request.setPartOfGroupUpdate(true);
            }
            //TODO: code copied from FilePoller
            RapturePipelineTask task = new RapturePipelineTask();
            task.setContentType("text/vnd.rapture.ftp.embs.file");
            task.setContent(request.toJSON());
            task.setCategoryList(Lists.newArrayList("embs"));
            log.info("Publishing file processing request " + request.toJSON());
            Kernel.getPipeline().publishMessageToCategory(ContextFactory.getKernelUser(), task);
        }
        if(requiresUpdate) {
            return Steps.NEXT;
        } else {
            log.info("No tables updates required. End of workflow.");
            return Steps.FINISH;
        }
    }

}
