package rapture.dp.invocable.embs;

import com.crux.embs.Crux;
import com.crux.embs.CruxConfiguration;
import com.google.common.base.Preconditions;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rapture.common.CallingContext;

import java.io.File;

public class UploadFileStep extends AbstractSingleOutcomeStep {

    private final Logger log = LoggerFactory.getLogger(UploadFileStep.class);

    public UploadFileStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        final String gzipFilePath = getContextValue("GZIP_FILE_PATH");
        log.info("Uploading GZIP file {}. ", gzipFilePath);

        CruxConfiguration cruxConfiguration = new CruxConfiguration(
                getContextValue("cruxApiurl"),
                getContextValue("cruxApiKey"),
                getContextValue("cruxDatasetId"));

        Crux crux = new Crux(cruxConfiguration);

        log.info("Loading file to CRUX - " + gzipFilePath);

        File file = new File(gzipFilePath);
        Preconditions.checkState(file.exists(), "filePath does not exist");

        log.info("Uploading file " + file.getAbsolutePath());
        //TODO: upload to sub dir, to allow for multiple uploads of same file.
        crux.uploadFile(getContextValue("cruxDatasetId"), file.getName(), file.getAbsolutePath());
        log.info("Uploaded file " + file.getAbsolutePath());

        setContextLiteral("CRUX_FILE_PATH", file.getName());
    }
}
