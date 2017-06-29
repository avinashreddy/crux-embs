package rapture.dp.invocable.embs;

import com.crux.embs.FTPConfigLoader;
import com.crux.embs.FileProcessingRequest;
import com.crux.embs.FileProcessingRequestLookup;
import com.google.common.base.Preconditions;
import com.matrix.common.Config;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rapture.common.CallingContext;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

public class DownloadFileStep extends AbstractSingleOutcomeStep {

    private final Logger log = LoggerFactory.getLogger(DownloadFileStep.class);


    public DownloadFileStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        final String requestUri = getContextValue("requestURI");
        log.info("Processing requestURI - " + requestUri);

        final FileProcessingRequest request = FileProcessingRequestLookup.get(this.ctx, requestUri);



        log.info("Processing request - " + request.toJSON());

        final Config ftpConfig = FTPConfigLoader.load("embs");

        FTPClient client = new FTPClient();
        OutputStream outputStream = null;
        try {
            client.connect(ftpConfig.getString("host"));
            client.login(ftpConfig.getString("user"), ftpConfig.getString("password"));
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
            log.info("Downloading file " + request.getProductFileName());
            File tempDir = createWorkDir(request.getProductFileName());
            File downloadFile = Paths.get(tempDir.getAbsolutePath(), new File(request.getProductFileName()).getName()).toFile();
            outputStream = new BufferedOutputStream(new FileOutputStream(downloadFile));
            boolean success = client.retrieveFile(request.getProductFileName(), outputStream);
            Preconditions.checkState(success,
                    "Could not download file '%s' to '%s'",
                    request.getProductFileName(),
                    downloadFile.getAbsolutePath());
            log.info(String.format("Downloaded file '%s' to '%s'", request.getProductFileName(), downloadFile.getAbsolutePath()));

            setContextLiteral("FILE_DIR", tempDir.toString());
            setContextLiteral("ZIP_FILE_PATH", downloadFile.getAbsolutePath());
            setContextLiteral("REQUEST_TIME_UTC", request.getRequestTimeUTC());
        }finally {
            if(outputStream != null) {
                outputStream.close();
            }
            client.disconnect();
        }
    }

    private File createWorkDir(String ftpFilePath) throws IOException {
        File file = new File(ftpFilePath);
        String root = file.getParent();
        File dir = Paths.get(
                getContextValue("TEMP_WORK_DIR"),
                file.getName().substring(0, file.getName().indexOf('.')),
                System.currentTimeMillis() + "",
                root == null ? "" : root
                ).toFile();
        dir.mkdirs();
        return dir;
    }
}
