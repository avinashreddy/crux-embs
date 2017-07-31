package rapture.dp.invocable.embs;

import com.google.common.base.Preconditions;
import rapture.common.CallingContext;

import java.io.File;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class UploadFileStep extends AbstractSingleOutcomeEmbsStep {

    public UploadFileStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
         final String gzipFilePath = getContextValue(Constants.GZIP_FILE_PATH);
        log.info(String.format("Uploading GZIP file [%s]. ", gzipFilePath));

        File file = new File(gzipFilePath);
        Preconditions.checkState(file.exists(), "[%s] does not exist", gzipFilePath);

        log.info(String.format("Uploading file [%s]", file.getAbsolutePath()));
        final String targetDir = getTargerDir(getContextValue(Constants.REQUEST_TIME_UTC));
        getCruxApi().uploadFile(getDefaultCruxDatasetId(), file.getName(), targetDir, file.getAbsolutePath(), "text/csv");
        log.info("Uploaded file " + file.getAbsolutePath());
//        setContextLiteral("CRUX_FILE_PATH", Paths.get("/", file.getName()).toString());
        setContextLiteral(Constants.CRUX_FILE_PATH, Paths.get(targetDir, file.getName()).toString());
    }

    private static String getTargerDir(String utcDateTime) throws ParseException {
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
        Date date = df.parse(utcDateTime.substring(0, utcDateTime.indexOf("T")));
        return  "/files/" + new SimpleDateFormat("yyyy-MM-dd").format(date);
    }
}
