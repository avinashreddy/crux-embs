package rapture.dp.invocable.embs;

import com.crux.embs.CSVLineTransformer;
import com.crux.embs.FileProcessingRequest;
import com.crux.embs.FileProcessingRequestLookup;
import com.crux.embs.FileSplitter2;
import com.crux.embs.Gzip;
import com.crux.embs.LineTransformer;
import com.crux.embs.Unzip;
import com.google.common.base.Preconditions;
import com.matrix.workflow.AbstractSingleOutcomeStep;
import org.apache.commons.lang3.StringUtils;
import rapture.common.CallingContext;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

public class TransformFileStep extends AbstractSingleOutcomeEmbsStep {

    private final Unzip unzip = new Unzip();

    private final FileSplitter2 fileSplitter;

    private final Gzip gzip = new Gzip();

    public TransformFileStep(String workerUri, String stepName) {
        super(workerUri, stepName);
        fileSplitter =  new FileSplitter2(this.log);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        final FileProcessingRequest request = FileProcessingRequestLookup.get(this.ctx, getRequestUri());

        final String zipFilePath = getContextValue(Constants.ZIP_FILE_PATH);
        final String workDir = getContextValue(Constants.FILE_DIR);

        log.info(String.format("Processing ZIP file [%s]. Work Dir is [%s]", zipFilePath, workDir));
        final String unzipDir = Paths.get(workDir, "unzip").toString();
        final List<String> files = unzip.unzip(zipFilePath, unzipDir);

        Preconditions.checkState(files.size() == 1,
                String.format("Expected 1 file in %s. Found %s", zipFilePath, files.size()));

        final String datFile = files.get(0);
        log.info(String.format("Unzipped file [%s] to [%s]", zipFilePath, datFile));
        String csv = transform(datFile,  Paths.get(workDir, "tx").toString(), request);
        log.info(String.format("Created [%s] for file [%s]", csv, datFile));
        final String csvgzipFile = gzip.gzip(csv, Paths.get(workDir, "gzip").toString());
        setContextLiteral(Constants.GZIP_FILE_PATH, csvgzipFile);
    }

    private String transform(String file, String targetDir, FileProcessingRequest request) throws IOException {
        final String suffix = "," + getContextValue(Constants.REQUEST_TIME_UTC) + "," + request.getSourceFileColumnVal();

        LineTransformer lt = null;

        if(request.getTableConfig().getPkColNames().size() == 1) {

            lt = new MetadataAddingLineTransformer(suffix, null);

        } else {

            lt = new MetadataAddingLineTransformer(suffix, new PKGenerator(request.getTableConfig().getPkColIndices(), ','));
        }
        Map<String, Object> ret = fileSplitter.split(
                file, targetDir, lt);
        setContextLiteral(Constants.FILE_LINE_COUNT, String.valueOf(ret.get("lineCount")));
        return (String) ret.get("fileName");
    }

    public static class MetadataAddingLineTransformer implements LineTransformer {

        private final LineTransformer lineTransformer = new CSVLineTransformer();

        private final String suffix;

        private final PKGenerator pkGenerator;

        public MetadataAddingLineTransformer(String suffix, PKGenerator pkGenerator) {
            Preconditions.checkArgument(StringUtils.isNoneEmpty(suffix), "suffix is null/empty");
            this.suffix = suffix;
            this.pkGenerator = pkGenerator;
        }

        @Override
        public String transFormLine(String line) {
            if(pkGenerator == null) {
                return lineTransformer.transFormLine(line).concat(suffix);
            } else {
                TransformResult result = lineTransformer.transFormLineResult(line);
                return result.line.concat(suffix).concat(pkGenerator.generate(result.cols));
            }
        }

        @Override
        public TransformResult transFormLineResult(String line) {
            throw new UnsupportedOperationException();
        }
    }

    public static class PKGenerator {
        private final int[] pkIndices;

        private final String delimiter;

        public PKGenerator(int[] pkIndices, char delimiter) {
            this.pkIndices = pkIndices;
            this.delimiter = delimiter + "";
        }

        String generate(String[] cols) {
            String ret =  delimiter;
            for(int i : pkIndices) {
                ret.concat(cols[i]);
            }
            return ret;
        }
    }
}
