package rapture.dp.invocable.embs;

import com.crux.embs.Crux;
import com.crux.embs.CruxConfiguration;
import com.google.common.base.Preconditions;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rapture.common.CallingContext;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Date;

public class UploadFileStep extends AbstractSingleOutcomeStep {

    public UploadFileStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
         final String gzipFilePath = getContextValue("GZIP_FILE_PATH");
        log.info(String.format("Uploading GZIP file [%s]. ", gzipFilePath));

        CruxConfiguration cruxConfiguration = new CruxConfiguration(
                getContextValue("cruxApiurl"),
                getContextValue("cruxApiKey"),
                getContextValue("cruxDatasetId"));

        Crux crux = new Crux(cruxConfiguration, log);

        File file = new File(gzipFilePath);
        Preconditions.checkState(file.exists(), "[%s] does not exist", gzipFilePath);

        log.info(String.format("Uploading file [%s]", file.getAbsolutePath()));
        final String targetDir = getTargerDir(getContextValue("REQUEST_TIME_UTC"));
        crux.uploadFile(getContextValue("cruxDatasetId"), file.getName(), targetDir, file.getAbsolutePath());
        log.info("Uploaded file " + file.getAbsolutePath());
//        setContextLiteral("CRUX_FILE_PATH", Paths.get("/", file.getName()).toString());
        setContextLiteral("CRUX_FILE_PATH", Paths.get(targetDir, file.getName()).toString());
    }

    private static String getTargerDir(String utcDateTime) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = df.parse(utcDateTime.substring(0, utcDateTime.indexOf("T")));
        return  "/files/" + new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
