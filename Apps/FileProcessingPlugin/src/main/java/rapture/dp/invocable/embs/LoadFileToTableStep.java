package rapture.dp.invocable.embs;

import com.crux.embs.FileConfig;
import com.crux.embs.FileProcessingRequest;
import com.crux.embs.TableConfig;
import com.crux.embs.TableUpdater;
import com.google.common.base.Preconditions;
import rapture.common.CallingContext;

public class LoadFileToTableStep extends AbstractSingleOutcomeEmbsStep {

    public LoadFileToTableStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
            doExecute(ctx);
    }

    protected void doExecute(CallingContext ctx) throws Exception {
        final String cruxFilePath = getContextValue(Constants.CRUX_FILE_PATH);
        final FileProcessingRequest request = getFileProcessingRequest();
        final TableConfig tableConfig = request.getTableConfig();
        final FileConfig fileConfig = request.getFileConfig();
        final boolean truncate = fileConfig.isReload();
        final boolean isPartOfGroup = request.isPartOfGroupUpdate();
        final int fileLineCount = Integer.parseInt(getContextValue(Constants.FILE_LINE_COUNT));

        Preconditions.checkState(!(truncate && isPartOfGroup),
                "A reload file cannot be part of a group - ", fileConfig);

        new TableUpdater(getCruxApi(), getDefaultCruxDatasetId(), log)
                .loadCruxFileToTable(cruxFilePath, fileLineCount, tableConfig, truncate, isPartOfGroup);

        setContextLiteral("MESSAGE_BODY",
                String.format("Uploaded file '%s' to table '%s'. \n Line count : %s",
                        getContextValue(Constants.GZIP_FILE_PATH),
                        tableConfig.getTableName(),
                        getContextValue(Constants.FILE_LINE_COUNT)));
    }
}
