package com.crux.embs.ftp;

import com.beust.jcommander.internal.Lists;
import com.crux.embs.FTPConfigLoader;
import com.crux.embs.FileProcessingRequest;
import com.google.common.base.Preconditions;
import com.matrix.common.Config;
import com.matrix.logging.NotifyingLogger2;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import rapture.common.RapturePipelineTask;
import rapture.common.exception.ExceptionToString;
import rapture.common.impl.jackson.JacksonUtil;
import rapture.exchange.QueueHandler;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Map;

import static java.lang.String.format;

public class FilePoller implements QueueHandler {

    private static final Logger log = NotifyingLogger2.getLogger(FilePoller.class, Level.ERROR);
    public static final String FTP_CODE = "ftpCode";

    @Override
    public boolean handleMessage(String tag, String routing, String contentType, RapturePipelineTask task) {
        log.info(format("Processing FTPPollRequest message [%s]", new String(task.getContent())));
        try {
            process(FtpPollRequest.fromJSON(task.getContent()));
            return true;
        } catch (Exception e) {
            log.error(format("Error processing RapturePipelineTask [%s]. %s", task, ExceptionToString.format(e)));
            //Must return false if there is an error in processing the task.
            return false;
        }
    }


    private void process(FtpPollRequest ftpPollRequest) throws Exception {
        Config ftpConfig = FTPConfigLoader.load(ftpPollRequest.getFtpCode());

        FTPClient client = new FTPClient();
        String runtimeUTC = ZonedDateTime.now(ZoneOffset.UTC).toString();
        try {
            client.connect(ftpConfig.getString("host"));
//        client.enterLocalPassiveMode();
            client.login(ftpConfig.getString("user"), ftpConfig.getString("password"));
            FTPFile[] files = client.listFiles("/Signal", file -> ftpPollRequest.getFiles().contains(file.getName().substring(0, file.getName().indexOf("."))));
            log.info(String.format("Found %s .SIG file(s)", files.length));
            for (FTPFile file : files) {
                processSignalFile(client, file, runtimeUTC);
            }
        }finally {
            client.disconnect();
        }
    }

    private void processSignalFile(FTPClient client, FTPFile file, String runtimeUTC) throws IOException {
        log.info("Processing " + file.getName());
        final String productFileName = file.getName().substring(0, file.getName().indexOf(".")) + ".ZIP";

        FTPFile[] files = client.listFiles("/Products",  f -> f.getName().equals(productFileName));

        Preconditions.checkState(files.length == 1,
                "Expecting one Product file with name '%s' for Signal file '%s'. Found %s.",
                productFileName, file.getName(), files.length);

        FileProcessingRequest request = new FileProcessingRequest();
        request.setSignalFileName("/Signal/" + file.getName());
        request.setProductFileName("/Products/" + productFileName);
        request.setRequestTimeUTC(runtimeUTC);

        RapturePipelineTask task = new RapturePipelineTask();
        task.setContentType("text/vnd.rapture.ftp.embs.file");
        task.setContent(request.toJSON());
        task.setCategoryList(Lists.newArrayList("embs"));
        log.info("Publishing file processing request " +  request.toJSON());
        Kernel.getPipeline().publishMessageToCategory(ContextFactory.getKernelUser(), task);
    }

}
