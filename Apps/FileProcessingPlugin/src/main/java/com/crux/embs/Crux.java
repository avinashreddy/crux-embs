package com.crux.embs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.log4j.Logger;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Crux {

    Logger log;

    private final String apiurl;

    private final String apiKey;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public Crux(CruxConfiguration cruxConfiguration, Logger logger) {
        this(cruxConfiguration.getApiurl(), cruxConfiguration.getApiKey(), logger);
    }

    public Crux(String apiurl, String apiKey, Logger logger) {
        this.apiurl = apiurl;
        this.apiKey = apiKey;
        this.log = logger;
        if(apiurl.contains("https:")) {
            CloseableHttpClient httpClient
                    = HttpClients.custom()
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory
                    = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
        }
        restTemplate = new RestTemplate();
    }

    public Map<String, Object> getTable(String datasetId, String tableName) {
        String obj = restTemplate.getForObject(
                apiurl + "/datasets/{datasetId}/tables/{tableName}?apikey={apikey}",
                String.class,
                datasetId,  tableName, apiKey);
        if(StringUtils.hasText(obj)) {
            try {
                return objectMapper.readValue(obj, new TypeReference<Map<String, Object>>(){});
            } catch (IOException e) {
                throw new IllegalStateException("Error parsing json - " + obj, e);
            }
        }
        return null;
    }

    public boolean tableExists(String datasetId, String tableName) {
        return getTable(datasetId, tableName) != null;
    }

    public boolean fileExists(String datasetId, String tableName) {
        //API is same for table and files and all resources
        return getTable(datasetId, tableName) != null;
    }


    public void ensureTableExists(String datasetId, String tableName, String schema) {
        //TODO: distributed lock required. Or make table creation a separate process.
        log.info("Checking if table "+ tableName +" exists");
        synchronized(Long.class) {
            if (!tableExists(datasetId, tableName)) {
                log.info("Table " + tableName + " does not exist. Creating table");
                createTable(datasetId, tableName, schema);
            } else {
                log.info("Table " + tableName + " exists.");

            }

            while (!tableExists(datasetId, tableName)) {
                log.info("Waiting for table to be created " + tableName);
                sleep(5000);
            }
        }
    }


    public void createTable(String datasetId, String tableName, String schema) {
        createTable(datasetId, tableName, schema, null);
    }

    public void createTable(String datasetId, String tableName, String schema, String description) {
        try {
            Preconditions.checkState(!tableExists(datasetId, tableName), "Table with name %s exists", tableName);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<String> entity = new HttpEntity<>(toJson(ImmutableMap.of("name", tableName, "schema", schema)), headers);
            String table = restTemplate.postForObject(
                    apiurl + "/datasets/{datasetId}/tables?apikey={apikey}",
                    entity,
                    String.class,
                    datasetId, apiKey);
        }catch(Exception e) {
            throw new IllegalStateException(String.format("Error creating table [name : '%s', schema : '%s']", tableName, schema), e);
        }

    }

    public void uploadFile(String datasetId, String fileName, String targetDir, String filePath) {
        log.info(String.format("Uploading file [%s] to [%s]", filePath, targetDir));
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add(fileName, new FileSystemResource(filePath));
        map.add("folder", targetDir);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);
        try {
            ResponseEntity<String> result = restTemplate.postForEntity(apiurl + "/pushfile/{datasetId}?apikey={apikey}",
                    entity,
                    String.class,
                    datasetId, apiKey);
        }catch(ResourceAccessException e) {

        }

        log.info(String.format("Uploaded file [%s] to [%s]", filePath, targetDir));

    }

    public void loadFileToTable(String datasetId, String fileName, String tableName, char delimiter) {
        Preconditions.checkState(tableExists(datasetId, tableName), "No Table with name %s", tableName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(toJson(ImmutableMap.of("fileName", fileName, "delimiter", delimiter, "maxBadRecords", 0)) ,headers);
        sleep(1000);
        ResponseEntity<String> result  = restTemplate.postForEntity(
                apiurl + "/datasets/{datasetId}/tables/{tableName}/loadfile?apikey={apikey}",
                entity,
                String.class,
                datasetId,  tableName, apiKey);
    }

    private void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            // Ignore
        }
    }


    private String toJson(Object o) {
        try {
            return objectMapper.writeValueAsString(o);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }



    public static void main(String[] args) {
        String datsetid = "WyJQcm9maWxlIiwidU9tTFhtRGYydWJXdDRabkJzamR3dlFYVlB2MSIsIkRhdGFTZXQiLDU2Mjk0OTk1MzQyMTMxMjBd";
        Crux crux = new Crux("http://localhost:8082/api", "a84f086e70b6b14a85c47a9dc1f6da88", Logger.getLogger(Crux.class));
//        System.out.println(crux.tableExists(datsetid, "t_01"));
//        System.out.println(crux.tableExists(datsetid, "a_01"));
//        crux.createTable(datsetid, "xxx01", "a:string", "");

//        crux.uploadFile(datsetid, "abcdef.csv", "/Users/avinash.palicharla/src/EMBS/work/03/csvgzip/ARMINDEX.DAT.0.csv.gz");
        for(int i = 0; i< 10; i++) {
            crux.loadFileToTable(datsetid, "a", "t_01", ',');
        }
    }


}
