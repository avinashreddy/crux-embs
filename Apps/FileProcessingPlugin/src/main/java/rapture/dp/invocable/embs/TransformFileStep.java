package rapture.dp.invocable.embs;

import com.crux.embs.CSVLineTransformer;
import com.crux.embs.FileSplitter2;
import com.crux.embs.Gzip;
import com.crux.embs.LineTransformer;
import com.crux.embs.Unzip;
import com.google.common.base.Preconditions;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import org.apache.commons.lang3.StringUtils;
import rapture.common.CallingContext;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TransformFileStep extends AbstractSingleOutcomeStep {

    private final Unzip unzip = new Unzip();

    private final FileSplitter2 fileSplitter;

    private final Gzip gzip = new Gzip();

    public TransformFileStep(String workerUri, String stepName) {
        super(workerUri, stepName);
        fileSplitter =  new FileSplitter2(this.log);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        final String zipFilePath = getContextValue("ZIP_FILE_PATH");
        final String workDir = getContextValue("FILE_DIR");

        log.info(String.format("Processing ZIP file [%s]. Work Dir is [%s]", zipFilePath, workDir));
        final String unzipDir = Paths.get(workDir, "unzip").toString();
        final List<String> files = unzip.unzip(zipFilePath, unzipDir);

        Preconditions.checkState(files.size() == 1,
                String.format("Expected 1 file in %s. Found %s", zipFilePath, files.size()));

        final String datFile = files.get(0);
        log.info(String.format("Unzipped file [%s] to [%s]", zipFilePath, datFile));
        String csv = transform(datFile,  Paths.get(workDir, "tx").toString(), zipFilePath.toString());
        log.info(String.format("Created [%s] for file [%s]", csv, datFile));
        final String csvgzipFile = gzip.gzip(csv, Paths.get(workDir, "gzip").toString());
        setContextLiteral("GZIP_FILE_PATH", csvgzipFile);
    }

    private String transform(String file, String targetDir, String sourceFileName) throws IOException {
        LineTransformer lt = new MetadataAddingLineTransformer(
                "," + getContextValue("REQUEST_TIME_UTC") + "," + new File(sourceFileName).getName());
        Map<String, Object> ret = fileSplitter.split(
                file, targetDir, lt);
        setContextLiteral("FILE_LINE_COUNT", String.valueOf(ret.get("lineCount")));
        return (String) ret.get("fileName");
    }

    public static class MetadataAddingLineTransformer implements LineTransformer {

        private final LineTransformer lineTransformer = new CSVLineTransformer();

        private final String suffix;

        public MetadataAddingLineTransformer(String suffix) {
            Preconditions.checkArgument(StringUtils.isNoneEmpty(suffix), "suffix is null/empty");
            this.suffix = suffix;
        }

        @Override
        public String transFormLine(String line) {
            return lineTransformer.transFormLine(line).concat(suffix);
//            return suffix.concat(line);
        }
    }
}
