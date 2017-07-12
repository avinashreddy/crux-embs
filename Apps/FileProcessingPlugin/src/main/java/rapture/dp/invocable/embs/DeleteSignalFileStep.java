package rapture.dp.invocable.embs;

import com.crux.embs.FTPConfigLoader;
import com.crux.embs.FileProcessingRequest;
import com.crux.embs.FileProcessingRequestLookup;
import com.matrix.common.Config;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import org.apache.commons.io.FileUtils;
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import rapture.common.CallingContext;

import java.io.File;
import java.io.OutputStream;

public class DeleteSignalFileStep extends AbstractSingleOutcomeStep {

    public DeleteSignalFileStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        final FileProcessingRequest request = FileProcessingRequestLookup.get(this.ctx, getContextValue("requestURI"));
        final Config ftpConfig = FTPConfigLoader.load("embs");

        FTPClient client = new FTPClient();
        try {
            client.connect(ftpConfig.getString("host"));
            client.login(ftpConfig.getString("user"), ftpConfig.getString("password"));
            client.enterLocalPassiveMode();
            client.setFileType(FTP.BINARY_FILE_TYPE);
            log.info("Deleting Signal file " + request.getSignalFileName());
            if(!client.deleteFile(request.getSignalFileName())) {
                throw new IllegalStateException("Error deleting Signal file " + request.getSignalFileName());
            }
        } finally {
            client.disconnect();
        }
    }
}
