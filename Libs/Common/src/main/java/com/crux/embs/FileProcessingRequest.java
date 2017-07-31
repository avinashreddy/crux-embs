package com.crux.embs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

public class FileProcessingRequest {

    private String signalFileName;

    private String productFileName;

    private FileConfig fileConfig;

    private TableConfig tableConfig;

    private boolean partOfGroupUpdate;

    private String requestTimeUTC;

    public String getSignalFileName() {
        return signalFileName;
    }

    public void setSignalFileName(String signalFileName) {
        this.signalFileName = signalFileName;
    }

    public String getProductFileName() {
        return productFileName;
    }

    public void setProductFileName(String productFileName) {
        this.productFileName = productFileName;
    }

    public String getRequestTimeUTC() {
        return requestTimeUTC;
    }

    public void setRequestTimeUTC(String requestTimeUTC) {
        this.requestTimeUTC = requestTimeUTC;
    }

    public FileConfig getFileConfig() {
        return fileConfig;
    }

    public void setFileConfig(FileConfig fileConfig) {
        this.fileConfig = fileConfig;
    }

    public TableConfig getTableConfig() {
        return tableConfig;
    }

    public void setTableConfig(TableConfig tableConfig) {
        this.tableConfig = tableConfig;
    }

    public boolean isPartOfGroupUpdate() {
        return partOfGroupUpdate;
    }

    public void setPartOfGroupUpdate(boolean partOfGroupUpdate) {
        this.partOfGroupUpdate = partOfGroupUpdate;
    }

    @JsonIgnore
    public String getSourceFileColumnVal() {
        return productFileName.substring(productFileName.lastIndexOf("/") + 1);
    }

    public FileProcessingRequest clone() {
        return fromJSON(toJSON());
    }

    public void validate() {
        Preconditions.checkState(StringUtils.isNoneEmpty(signalFileName), "signalFileName is null/empty");
        Preconditions.checkState(StringUtils.isNoneEmpty(productFileName), "productFileName is null/empty");
        Preconditions.checkState(fileConfig != null, "fileConfig is null");
        Preconditions.checkState(tableConfig != null, "tableConfig is null");
        fileConfig.validate();
        tableConfig.validate();
    }

    public String toJSON() {
        //TODO: validate fields - mandatory etc.
        try {
            return new ObjectMapper().writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static FileProcessingRequest fromJSON(String json) {
        Preconditions.checkNotNull(json, "json is null");
        try {
            return (new ObjectMapper()).readValue(json, FileProcessingRequest.class);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Error marshalling from JSON [%s]", json), e);
        }
    }
}
