package com.crux.embs.ftp;

import com.matrix.logging.NotifyingLogger2;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import rapture.common.RapturePipelineTask;
import rapture.common.exception.ExceptionToString;
import rapture.exchange.QueueHandler;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.format;

//TODO: cleanup - largely a copy paste of EmbsFileProcessor
public class EmbsFileGroupProcessor implements QueueHandler {

    private static final Logger log = NotifyingLogger2.getLogger(EmbsFileGroupProcessor.class, Level.ERROR);

    private static final String WORKFLOW_URI = "workflow://workflows/crux/embs/processFileGroup";

    public static final String MESSAGE_CONTENT_TYPE = "text/vnd.rapture.ftp.embs.file.group";

    public static final String REQUEST_URL_ROOT = "blob://archive/crux/embs/requests/";

    @Override
    public boolean handleMessage(String tag, String routing, String contentType, RapturePipelineTask task) {
        log.info(format("Processing FileProcessingGroupRequest message [%s]", new String(task.getContent())));
        try {
            process(task.getContent());
            return true;
        } catch (Exception e) {
            log.error(format("Error processing RapturePipelineTask [%s]. %s", task, ExceptionToString.format(e)));
            //Must return false if there is an error in processing the task.
            return false;
        }
    }

    private void process(String json) throws Exception {
        final String storageUri = getRequestUri(REQUEST_URL_ROOT);

        putBlob(storageUri, json);

        log.info("Starting workflow " + WORKFLOW_URI);

        final Map<String, String> params = new HashedMap();
        params.put("requestURI", storageUri);
        params.put("requestJson", json);

        String workOrderUri = Kernel.getDecision().createWorkOrder(ContextFactory.getKernelUser(), WORKFLOW_URI, params);

        log.info("Started workflow. Work order is " + workOrderUri);
    }

    public String getRequestUri(String uriRoot) {
        return String.format(uriRoot + "%s/%s",
                ZonedDateTime.now(ZoneOffset.UTC).format(DateTimeFormatter.ofPattern("yyyy/MM/dd")),
                UUID.randomUUID().toString());
    }

    private void putBlob(String uri, String json) {
        Kernel.getBlob().putBlob(ContextFactory.getKernelUser(), uri, json.getBytes(), MESSAGE_CONTENT_TYPE);
        log.info(String.format("Successfully stored FileProcessingRequest at uri: [%s]", uri));

    }

}
