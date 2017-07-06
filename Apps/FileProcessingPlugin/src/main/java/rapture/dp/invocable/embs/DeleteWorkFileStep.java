package rapture.dp.invocable.embs;

import com.matrix.workflow.AbstractSingleOutcomeStep;
import org.apache.commons.io.FileUtils;
import rapture.common.CallingContext;

import java.io.File;

public class DeleteWorkFileStep extends AbstractSingleOutcomeStep {

    public DeleteWorkFileStep(String workerUri, String stepName) {
        super(workerUri, stepName);
    }

    @Override
    protected void execute(CallingContext ctx) throws Exception {
        String dir = getContextValue("FILE_DIR");
        log.info("Deletring directory " + dir);
        FileUtils.deleteDirectory(new File(dir));
    }
}
