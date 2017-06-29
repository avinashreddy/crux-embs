package rapture.dp.invocable.embs;

import com.crux.embs.CSVLineTransformer;
import com.crux.embs.FTPConfigLoader;
import com.crux.embs.FileProcessingRequest;
import com.crux.embs.FileProcessingRequestLookup;
import com.crux.embs.FileSplitter2;
import com.crux.embs.Gzip;
import com.crux.embs.LineTransformer;
import com.crux.embs.Unzip;
import com.google.common.base.Preconditions;
import com.matrix.common.Config;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import org.apache.commons.lang3.StringUtils;
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
import java.util.List;

public class TransformFileStep extends AbstractSingleOutcomeStep {

    private final Logger log = LoggerFactory.getLogger(TransformFileStep.class);

    private final Unzip unzip = new Unzip();

    private final FileSplitter2 fileSplitter = new FileSplitter2();

    private final Gzip gzip = new Gzip();

    public TransformFileStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        final String zipFilePath = getContextValue("ZIP_FILE_PATH");
        final String workDir = getContextValue("FILE_DIR");

        log.info("Processing ZIP file {}. Work Dir is '{}'", zipFilePath, workDir);
        final String unzipDir = Paths.get(workDir, "unzip").toString();
        final List<String> files = unzip.unzip(zipFilePath, unzipDir);

        Preconditions.checkState(files.size() == 1,
                String.format("Expected 1 file in %s. Found %s", zipFilePath, files.size()));

        final String datFile = files.get(0);
        log.info("Unzipped file {} to {}", zipFilePath, datFile);
        String csv = transform(datFile,  Paths.get(workDir, "csv").toString(), zipFilePath.toString());
        log.info("Created CSV {} for file {}", csv, datFile);
        final String csvgzipFile = gzip.gzip(csv, Paths.get(workDir, "gzip").toString());
        setContextLiteral("GZIP_FILE_PATH", csvgzipFile);
    }

    private String transform(String file, String targetDir, String sourceFileName) throws IOException {
        LineTransformer lt = new MetadataAddingLineTransformer(sourceFileName);
        List<String> ret = fileSplitter.split(
                file, targetDir,
                lt, ".csv",
                Integer.MAX_VALUE);
        return ret.get(0);
    }

    private class MetadataAddingLineTransformer implements LineTransformer {

        private final String originalFile;

        private final LineTransformer lineTransformer = new CSVLineTransformer();

        private final String requestTimeUTC;

        private MetadataAddingLineTransformer(String originalFile) {
            Preconditions.checkArgument(StringUtils.isNoneEmpty(originalFile), "originalFile is null/empty");
            this.originalFile = originalFile;
            this.requestTimeUTC = TransformFileStep.this.getContextValue("REQUEST_TIME_UTC");
        }

        @Override
        public String transFormLine(String line) {
            return requestTimeUTC + "," + originalFile + "," + lineTransformer.transFormLine(line);
        }
    }
}
