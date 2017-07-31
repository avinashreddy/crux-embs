package com.crux.embs;

import com.crux.embs.ftp.EmbsFileGroupProcessor;
import com.crux.embs.ftp.EmbsFileProcessor;
import com.crux.embs.ftp.FilePoller;
import com.matrix.AbstractRaptureServer;
import com.matrix.pipeline.PipelineQueueHandlerRegistrar;
import rapture.config.ConfigLoader;
import rapture.exchange.QueueHandler;

import java.util.HashMap;
import java.util.Map;

public class EmbsServer extends AbstractRaptureServer {

    public static void main(String[] args) {
        new EmbsServer().start(args);
    }

    @Override
    protected void setCategoryMembership() {
        Map<String, QueueHandler> queueHandlers = new HashMap<>();
        queueHandlers.put("text/vnd.rapture.ftp.poll", new FilePoller());
        queueHandlers.put("text/vnd.rapture.ftp.embs.file", new EmbsFileProcessor());
        queueHandlers.put("text/vnd.rapture.ftp.embs.file.group", new EmbsFileGroupProcessor());

        PipelineQueueHandlerRegistrar.setCategoryMembershipWithDefaults(ConfigLoader.getConf().Categories, queueHandlers);
    }

}
