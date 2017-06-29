package com.matrix.pipeline;

import com.google.common.collect.Maps;
import rapture.common.mime.MimeDecisionProcessAdvance;
import rapture.common.mime.MimeDocumentIndexRebuild;
import rapture.common.mime.MimeIndexRebuild;
import rapture.common.mime.MimeJarCacheUpdate;
import rapture.common.mime.MimeRaptureDocument;
import rapture.common.mime.MimeReflexScript;
import rapture.common.mime.MimeReflexScriptRef;
import rapture.common.mime.MimeReflexScriptResume;
import rapture.common.mime.MimeScheduleReflexScriptRef;
import rapture.common.mime.MimeSearchUpdateObject;
import rapture.exchange.QueueHandler;
import rapture.kernel.Kernel;
import rapture.kernel.dp.RaptureDecisionProcessAdvanceHandler;
import rapture.kernel.pipeline.JarCacheUpdateHandler;
import rapture.kernel.pipeline.RaptureAlertHandler;
import rapture.kernel.pipeline.RaptureAuditHandler;
import rapture.kernel.pipeline.RaptureDocSaveHandler;
import rapture.kernel.pipeline.RaptureDocumentIndexRebuildHandler;
import rapture.kernel.pipeline.RaptureIndexRebuildHandler;
import rapture.kernel.pipeline.RaptureReflexScriptHandler;
import rapture.kernel.pipeline.RaptureReflexScriptRefHandler;
import rapture.kernel.pipeline.RaptureReflexScriptResumeHandler;
import rapture.kernel.pipeline.RaptureScheduleReflexScriptRefHandler;
import rapture.kernel.pipeline.RaptureSearchUpdateHandler;
import rapture.kernel.pipeline.TextPlainHandler;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @see rapture.kernel.pipeline.PipelineQueueHandler
 *
 */
public class PipelineQueueHandlerRegistrar {

    private static final Map<String, QueueHandler> defaultHandlers;

    private PipelineQueueHandlerRegistrar() {}

    static {
        Map<String, QueueHandler> setupMap = new HashMap<String, QueueHandler>();
        setupMap.put("text/plain", new TextPlainHandler()); //$NON-NLS-1$
        setupMap.put(MimeReflexScript.getMimeType(), new RaptureReflexScriptHandler());
        setupMap.put(MimeReflexScriptRef.getMimeType(), new RaptureReflexScriptRefHandler());
        setupMap.put(MimeRaptureDocument.getMimeType(), new RaptureDocSaveHandler());
        setupMap.put("application/vnd.rapture.audit", new RaptureAuditHandler()); //$NON-NLS-1$
        setupMap.put(MimeIndexRebuild.getMimeType(), new RaptureIndexRebuildHandler());
        setupMap.put(MimeDocumentIndexRebuild.getMimeType(), new RaptureDocumentIndexRebuildHandler());
        setupMap.put(MimeReflexScriptResume.getMimeType(), new RaptureReflexScriptResumeHandler());
        setupMap.put(MimeScheduleReflexScriptRef.getMimeType(), new RaptureScheduleReflexScriptRefHandler());
        setupMap.put(MimeDecisionProcessAdvance.getMimeType(), new RaptureDecisionProcessAdvanceHandler());
        setupMap.put("application/vnd.rapture.event.alert", new RaptureAlertHandler());
        setupMap.put(MimeSearchUpdateObject.getMimeType(), new RaptureSearchUpdateHandler());
        setupMap.put(MimeJarCacheUpdate.getMimeType(), new JarCacheUpdateHandler());
        defaultHandlers = Collections.unmodifiableMap(setupMap);
    }

    /**
     *
     * @see rapture.kernel.Kernel#setCategoryMembership(String, Map)
     * @see rapture.kernel.Kernel#setCategoryMembership(String)
     *
     * @param category
     * @param customHandlers
     */
    public static void setCategoryMembershipWithDefaults(String category, Map<String, QueueHandler> customHandlers) {
        Map<String, QueueHandler> map = Maps.newHashMap(defaultHandlers);
        map.putAll(customHandlers);
        Kernel.setCategoryMembership(category, map);
    }

}
