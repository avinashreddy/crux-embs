package com.crux.embs.ftp;

import com.beust.jcommander.internal.Lists;
import com.beust.jcommander.internal.Maps;
import com.crux.embs.FTPConfigLoader;
import com.crux.embs.FileConfig;
import com.crux.embs.FileConfigLookup;
import com.crux.embs.FileGroupProcessingRequest;
import com.crux.embs.FileProcessingRequest;
import com.crux.embs.TableConfigLookup;
import com.google.common.base.Preconditions;
import com.matrix.common.Config;
import com.matrix.logging.NotifyingLogger2;
import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import rapture.common.RapturePipelineTask;
import rapture.common.exception.ExceptionToString;
import rapture.exchange.QueueHandler;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.lang.String.format;

public class FilePoller implements QueueHandler {

    private static final Logger log = NotifyingLogger2.getLogger(FilePoller.class, Level.ERROR);
    public static final String FTP_CODE = "ftpCode";

    private FileConfigLookup fileConfigLookup = new FileConfigLookup();
    private TableConfigLookup tableConfigLookup = new TableConfigLookup();

    public FilePoller() {
        fileConfigLookup.init();
        tableConfigLookup.init();
    }

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
        log.info("Polling for new SIG files");
        FTPClient client = new FTPClient();
        String runtimeUTC = ZonedDateTime.now(ZoneOffset.UTC).toString();
        try {
            client.connect(ftpConfig.getString("host"));
            client.enterLocalPassiveMode();
            client.login(ftpConfig.getString("user"), ftpConfig.getString("password"));
            FTPFile[] files = client.listFiles(
                    ftpConfig.getString("home_dir", "/") + "Signal", file
                            -> ftpPollRequest.getFiles().contains(file.getName().substring(0, file.getName().indexOf("."))));

            log.info(String.format("Found %s .SIG file(s)", files.length));
            List<FileProcessingRequest> fileProcessingRequests = Lists.newArrayList();
            for (FTPFile file : files) {
                fileProcessingRequests.add(buildFileProcessingRequest(client, file, runtimeUTC, ftpConfig));
            }
            processGroups(groupFileProcessingRequest(fileProcessingRequests));
        } finally {
            client.disconnect();
        }
    }


    private Collection<List<FileProcessingRequest>> groupFileProcessingRequest(List<FileProcessingRequest> fileProcessingRequests) {
        Map<String, List<FileProcessingRequest>> fileGroups = Maps.newHashMap();
        for (FileProcessingRequest request : fileProcessingRequests) {
            FileConfig fileConfig = fileConfigLookup.getByFileName(request.getSignalFileName());
            List<FileProcessingRequest> requests = fileGroups.get(fileConfig.getTable());
            if (requests == null) {
                requests = new ArrayList<>();
                fileGroups.put(fileConfig.getTable(), requests);
            }
            requests.add(request);
        }
        return fileGroups.values();
    }

    private void processGroups(Collection<List<FileProcessingRequest>> requestGroups) {
        for(List<FileProcessingRequest> requests : requestGroups) {
            if(requests.size() == 1) {
                processRequest(requests.get(0));
            } else {
                processRequestGroup(requests);
            }
        }
    }

    private void processRequestGroup(List<FileProcessingRequest> requests) {
        FileGroupProcessingRequest request = new FileGroupProcessingRequest();
        request.setFileProcessingRequests(requests);
        RapturePipelineTask task = new RapturePipelineTask();
        task.setContentType("text/vnd.rapture.ftp.embs.file.group");
        task.setContent(request.toJSON());
        task.setCategoryList(Lists.newArrayList("embs"));
        log.info("Publishing file processing request " + request.toJSON());
        Kernel.getPipeline().publishMessageToCategory(ContextFactory.getKernelUser(), task);
    }

    private void processRequest(FileProcessingRequest request) {
        RapturePipelineTask task = new RapturePipelineTask();
        task.setContentType("text/vnd.rapture.ftp.embs.file");
        task.setContent(request.toJSON());
        task.setCategoryList(Lists.newArrayList("embs"));
        log.info("Publishing file processing request " + request.toJSON());
        Kernel.getPipeline().publishMessageToCategory(ContextFactory.getKernelUser(), task);
    }


    private FileProcessingRequest buildFileProcessingRequest(FTPClient client, FTPFile file, String runtimeUTC, Config ftpConfig) throws IOException {
        log.info("Processing " + file.getName());
        final String productFileName = file.getName().substring(0, file.getName().indexOf(".")) + ".ZIP";

        FTPFile[] files = client.listFiles(ftpConfig.getString("home_dir", "/") + "Products", f -> f.getName().equals(productFileName));

        Preconditions.checkState(files.length == 1,
                "Expecting one Product file with name '%s' for Signal file '%s'. Found %s.",
                productFileName, file.getName(), files.length);

        FileConfig fileConfig = fileConfigLookup.getByFileName(file.getName());
        FileProcessingRequest request = new FileProcessingRequest();
        request.setSignalFileName(ftpConfig.getString("home_dir", "/") + "Signal/" + file.getName());
        request.setProductFileName(ftpConfig.getString("home_dir", "/") + "Products/" + productFileName);
        request.setFileConfig(fileConfig);
        request.setTableConfig(tableConfigLookup.get(fileConfig.getTable()));
        request.setRequestTimeUTC(runtimeUTC);
        return request;
    }
}
