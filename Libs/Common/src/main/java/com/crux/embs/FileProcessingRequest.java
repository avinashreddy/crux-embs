package com.crux.embs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import java.io.IOException;

public class FileProcessingRequest {

    private String signalFileName;

    private String productFileName;

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

    public String toJSON() {
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
