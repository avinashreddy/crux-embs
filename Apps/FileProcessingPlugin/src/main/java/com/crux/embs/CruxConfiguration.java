package com.crux.embs;

public class CruxConfiguration {

    private final String apiurl;

    private final String apiKey;

    private final String dataSetId;

    public CruxConfiguration(String apiurl, String apiKey, String dataSetId) {
        this.apiurl = apiurl;
        this.apiKey = apiKey;
        this.dataSetId = dataSetId;
    }

    public String getApiurl() {
        return apiurl;
    }

    public String getApiKey() {
        return apiKey;
    }

    public String getDataSetId() {
        return dataSetId;
    }
}
