package com.crux.embs.ftp;

import com.crux.embs.FileProcessingRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;

import java.util.List;

public class FtpPollRequest {

    private String ftpCode;

    private List<String> files;

    public String getFtpCode() {
        return ftpCode;
    }

    public void setFtpCode(String ftpCode) {
        this.ftpCode = ftpCode;
    }

    public List<String> getFiles() {
        return files;
    }

    public void setFiles(List<String> files) {
        this.files = files;
    }

    public static FtpPollRequest fromJSON(String json) {
        Preconditions.checkNotNull(json, "json is null");
        try {
            return (new ObjectMapper()).readValue(json, FtpPollRequest.class);
        } catch (Exception e) {
            throw new IllegalStateException(String.format("Error marshalling from JSON [%s]", json), e);
        }
    }
}
