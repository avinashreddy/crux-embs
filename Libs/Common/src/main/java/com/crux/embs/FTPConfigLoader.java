package com.crux.embs;

import com.google.common.base.Preconditions;
import com.matrix.common.Config;
import com.matrix.common.MapConfig;
import org.apache.commons.lang3.StringUtils;
import rapture.common.impl.jackson.JacksonUtil;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;

import java.util.Map;

public class FTPConfigLoader {
    private static final String FTP_CONFIG = "document://configs/crux/ftp/config";


    public static Config load(String code) {
        String configJson = Kernel.getDoc().getDoc(ContextFactory.getKernelUser(), FTP_CONFIG);
        if (StringUtils.isBlank(configJson)) {
            throw new RuntimeException(String.format("Failed to load config file from Rapture at uri [%s]. Is the FTPConfig plugin deployed.", FTP_CONFIG));
        }
        Map<String, Object> config = JacksonUtil.getMapFromJson(configJson);
        Preconditions.checkState(config.containsKey(code), "No config with code '%s' found", code);

        return new MapConfig((Map<String, Object>) config.get(code));
    }

}
