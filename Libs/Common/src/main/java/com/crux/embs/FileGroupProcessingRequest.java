package com.crux.embs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.collections.CollectionUtils;

import java.util.List;

public class FileGroupProcessingRequest {

    private List<FileProcessingRequest> fileProcessingRequests;

    public List<FileProcessingRequest> getFileProcessingRequests() {
        return fileProcessingRequests;
    }

    public void setFileProcessingRequests(List<FileProcessingRequest> fileProcessingRequests) {
        this.fileProcessingRequests = fileProcessingRequests;
    }

    @JsonIgnore
    public String getTableSchema() {
        return fileProcessingRequests.get(0).getTableConfig().getSchema();
    }

    @JsonIgnore
    public String getTable() {
        return fileProcessingRequests.get(0).getTableConfig().getTableName();
    }

    @JsonIgnore
    public String getTempTable() {
        return getFileProcessingRequests().get(0).getTableConfig().getTempTableName();
    }

    @JsonIgnore
    public String getTableUpdateKey() {
        return fileProcessingRequests.get(0).getTableConfig().getTableUpdateKey();
    }

    @JsonIgnore
    public void validate() {
        Preconditions.checkState(CollectionUtils.isNotEmpty(fileProcessingRequests), "fileProcessingRequests is null/empty");
        fileProcessingRequests.get(0).validate();
        final String tableName = fileProcessingRequests.get(0).getTableConfig().getTableName();
        for(FileProcessingRequest request : getFileProcessingRequests()) {
            request.validate();
            Preconditions.checkState(tableName.equalsIgnoreCase(request.getFileConfig().getTable()),
                    "Invalid file grouping. Files mapped to different tables - '%s' and '%s'.",
                    tableName, request.getFileConfig().getTable());
        }
    }

    public String toJSON() {
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static FileGroupProcessingRequest fromJSON(String json) {
        Preconditions.checkNotNull(json, "json is null");
        try {
            return (new ObjectMapper()).readValue(json, FileGroupProcessingRequest.class);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Error marshalling from JSON [%s]", json), e);
        }
    }
}
