package rapture.dp.invocable.embs;

import com.crux.embs.Crux;
import com.crux.embs.CruxConfiguration;
import com.crux.embs.CruxFileLoader;
import com.crux.embs.FileConfig;
import com.crux.embs.FileConfigLookup;
import com.crux.embs.FileProcessingRequest;
import com.crux.embs.FileProcessingRequestLookup;
import com.crux.embs.TableConfig;
import com.crux.embs.TableConfigLookup;
import com.google.common.base.Preconditions;
import com.matrix.RaptureUriUtils;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rapture.common.CallingContext;

import java.io.File;

public class LoadFileToTableStep extends AbstractSingleOutcomeStep {

    private final Logger log = LoggerFactory.getLogger(LoadFileToTableStep.class);

    private FileConfigLookup fileConfigLookup = new FileConfigLookup();

    private TableConfigLookup tableConfigLookup = new TableConfigLookup();

    public LoadFileToTableStep(String workerUri, String stepName) {
        super(workerUri, stepName);
        fileConfigLookup.init();
        tableConfigLookup.init();
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        final String gzipFilePath = getContextValue("GZIP_FILE_PATH");
        final String cruxFilePath = getContextValue("CRUX_FILE_PATH");
        log.info("Loading crux file {}. ", cruxFilePath);

        CruxConfiguration cruxConfiguration = new CruxConfiguration(
                getContextValue("cruxApiurl"),
                getContextValue("cruxApiKey"),
                getContextValue("cruxDatasetId"));

        Crux crux = new Crux(cruxConfiguration);
        FileConfig fileConfig = fileConfigLookup.getByFileName(new File(gzipFilePath).getName());
        TableConfig tableConfig = tableConfigLookup.get(fileConfig.getTable());

        crux.ensureTableExists(getContextValue("cruxDatasetId"), tableConfig.getTableName(), tableConfig.getSchema());

        log.info("Loading file " + cruxFilePath + " to table " + tableConfig.getTableName());
        crux.loadFileToTable(getContextValue("cruxDatasetId"), cruxFilePath, tableConfig.getTableName(), ',');
        log.info("Loaded file " + cruxFilePath + " to table " + tableConfig.getTableName());

        setContextLiteral("MESSAGE_BODY",
                String.format("Uploaded file '%s' to table '%s'",
                        getContextValue("GZIP_FILE_PATH"),
                        tableConfig.getTableName()));
    }
}
