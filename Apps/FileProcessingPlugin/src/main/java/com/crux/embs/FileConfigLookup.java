package com.crux.embs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FileConfigLookup {

    private final String CONFIG = "document://configs/crux/embs//workflow/file_config";

    List<FileConfig> fileConfigList = new ArrayList<>();

    public void init()  {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = Kernel.getDoc().getDoc(ContextFactory.getKernelUser(), CONFIG);
            Map<String, Object> map =  mapper.readValue(
                    json,
                    new TypeReference<Map<String, Object>>() {
                    });

            fileConfigList = mapper.readValue(
                    mapper.writeValueAsString(map.get("config")),
                    new TypeReference<List<FileConfig>>() {
                    });
        }catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public FileConfig getByFileName(String fileName) {
        String type = fileName.substring(0, fileName.indexOf('.'));
        if("FNM_MODD".equals(type)
                || "FNM_MODM".equals(type)
                || fileName.indexOf("_") < 0) {
            return getByFileType(type);
        } else {
            type = fileName.substring(fileName.indexOf("_") + 1, fileName.indexOf("."));
            return getByFileType(type);
        }
    }

    public FileConfig getByFileType(String fileType) {
        List<FileConfig> ret = fileConfigList.stream()
                .filter( e -> e.getFileType().equals(fileType))
                .collect(Collectors.toList());
        Preconditions.checkState(
                ret.size() == 1,
                "Expecting 1 FileConfig with fileType = '%s'. Found %s", fileType, ret.size());
        return ret.get(0);
    }
}
