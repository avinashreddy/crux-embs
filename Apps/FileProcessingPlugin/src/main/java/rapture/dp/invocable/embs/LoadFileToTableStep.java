package rapture.dp.invocable.embs;

import com.crux.embs.Crux;
import com.crux.embs.CruxConfiguration;
import com.crux.embs.FileConfig;
import com.crux.embs.FileConfigLookup;
import com.crux.embs.TableConfig;
import com.crux.embs.TableConfigLookup;
import com.google.common.base.Preconditions;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import rapture.common.CallingContext;

import java.io.File;

public class LoadFileToTableStep extends AbstractSingleOutcomeStep {

    private FileConfigLookup fileConfigLookup = new FileConfigLookup();

    private TableConfigLookup tableConfigLookup = new TableConfigLookup();

    public LoadFileToTableStep(String workerUri, String stepName) {
        super(workerUri, stepName);
        fileConfigLookup.init();
        tableConfigLookup.init();
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        try {
            doExecute(ctx);
        }catch(Exception e) {
            if(1 < 2) {
                int i = 0;
                System.out.print(i);
            }
        }
    }

    protected void doExecute(CallingContext ctx) throws Exception {
        final String gzipFilePath = getContextValue("GZIP_FILE_PATH");
        final String cruxFilePath = getContextValue("CRUX_FILE_PATH");
        log.info(String.format("Loading crux file [%s]. ", cruxFilePath));

        CruxConfiguration cruxConfiguration = new CruxConfiguration(
                getContextValue("cruxApiurl"),
                getContextValue("cruxApiKey"),
                getContextValue("cruxDatasetId"));

        Crux crux = new Crux(cruxConfiguration, log);

        FileConfig fileConfig = fileConfigLookup.getByFileName(new File(gzipFilePath).getName());
        TableConfig tableConfig = tableConfigLookup.get(fileConfig.getTable());
        Preconditions.checkState(crux.tableExists(getContextValue("cruxDatasetId"), tableConfig.getTableName()),
                "Table does not exist '%s'", tableConfig.getTableName());

        log.info("Loading file " + cruxFilePath + " to table " + tableConfig.getTableName());
        crux.loadFileToTable(getContextValue("cruxDatasetId"), cruxFilePath, tableConfig.getTableName(), '|');
        log.info("Loaded file " + cruxFilePath + " to table " + tableConfig.getTableName());

        setContextLiteral("MESSAGE_BODY",
                String.format("Uploaded file '%s' to table '%s'. \n Line count : %s",
                        getContextValue("GZIP_FILE_PATH"),
                        tableConfig.getTableName(),
                        getContextValue("FILE_LINE_COUNT")));
    }
}
