package rapture.dp.invocable.embs;

import com.crux.embs.CruxApi;
import com.crux.embs.CruxApiImpl;
import com.crux.embs.CruxConfiguration;
import com.crux.embs.FileProcessingRequest;
import com.crux.embs.FileProcessingRequestLookup;
import com.crux.embs.TableConfig;
import com.crux.embs.TableConfigLookup;
import com.matrix.workflow.AbstractTemplateStep;

public abstract class AbstractEmbsStep extends AbstractTemplateStep {

    protected final TableConfigLookup tableConfigLookup = new TableConfigLookup();

    private CruxApi cruxApi;

    public AbstractEmbsStep(String workerUri, String stepName) {
        super(workerUri, stepName);

        tableConfigLookup.init();
    }

    protected CruxApi getCruxApi() {

        if(cruxApi == null) {
            CruxConfiguration cruxConfiguration = new CruxConfiguration(
                    getContextValue("cruxApiurl"),
                    getContextValue("cruxApiKey"),
                    getContextValue("cruxDatasetId"));
            cruxApi = new CruxApiImpl(cruxConfiguration, log);
        }
        return cruxApi;
    }

    protected String getRequestUri() {
       return getContextValue("requestURI");
    }

    protected TableConfig getTableConfig(String tableName) {
        return tableConfigLookup.get(tableName);
    }

    protected String getDefaultCruxDatasetId() {
        return getContextValue("cruxDatasetId");
    }

    protected FileProcessingRequest getFileProcessingRequest() {
        return FileProcessingRequestLookup.get(this.ctx, getRequestUri());
    }
}
