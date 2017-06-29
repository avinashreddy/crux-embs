package com.crux.embs;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TableConfigLookup {

    private final String CONFIG = "document://configs/crux/embs//workflow/table_config";

    List<TableConfig> configList = new ArrayList<>();

    public void init()  {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String json = Kernel.getDoc().getDoc(ContextFactory.getKernelUser(), CONFIG);
            Map<String, Object> map =  mapper.readValue(
                    json,
                    new TypeReference<Map<String, Object>>() {
                    });
            configList = mapper.readValue(
                    mapper.writeValueAsString(map.get("config")),
                    new TypeReference<List<TableConfig>>() {
                    });
        } catch(Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public TableConfig get(String tableName) {
        List<TableConfig> ret = configList.stream()
                .filter( e -> e.getTableName().equalsIgnoreCase(tableName))
                .collect(Collectors.toList());
        Preconditions.checkState(
                ret.size() == 1,
                "Expecting 1 TableConfig with tableName = '%s'. Found %s", tableName, ret.size());
        return ret.get(0);
    }
}
