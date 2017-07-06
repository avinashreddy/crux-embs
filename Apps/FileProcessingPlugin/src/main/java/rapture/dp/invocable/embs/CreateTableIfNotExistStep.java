package rapture.dp.invocable.embs;

import com.crux.embs.Crux;
import com.crux.embs.CruxConfiguration;
import com.crux.embs.FileConfig;
import com.crux.embs.FileConfigLookup;
import com.crux.embs.FileProcessingRequest;
import com.crux.embs.FileProcessingRequestLookup;
import com.crux.embs.TableConfig;
import com.crux.embs.TableConfigLookup;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import rapture.common.CallingContext;

import java.io.File;

public class CreateTableIfNotExistStep extends AbstractSingleOutcomeStep {

    private FileConfigLookup fileConfigLookup = new FileConfigLookup();

    private TableConfigLookup tableConfigLookup = new TableConfigLookup();

    public CreateTableIfNotExistStep(String workerUri, String stepName) {
        super(workerUri, stepName);
        fileConfigLookup.init();
        tableConfigLookup.init();
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        synchronized (Integer.class) {
            final String requestUri = getContextValue("requestURI");
            final FileProcessingRequest request = FileProcessingRequestLookup.get(this.ctx, requestUri);

            CruxConfiguration cruxConfiguration = new CruxConfiguration(
                    getContextValue("cruxApiurl"),
                    getContextValue("cruxApiKey"),
                    getContextValue("cruxDatasetId"));

            Crux crux = new Crux(cruxConfiguration, log);
            FileConfig fileConfig = fileConfigLookup.getByFileName(new File(request.getProductFileName()).getName());
            TableConfig tableConfig = tableConfigLookup.get(fileConfig.getTable());
            crux.ensureTableExists(getContextValue("cruxDatasetId"), tableConfig.getTableName(), tableConfig.getSchema().trim());
        }
    }
}
