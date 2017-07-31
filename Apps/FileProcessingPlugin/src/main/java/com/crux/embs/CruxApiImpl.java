package com.crux.embs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
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
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class CruxApiImpl implements CruxApi {

    Logger log;

    private final String apiurl;

    private final String apiKey;

    private final RestTemplate restTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public CruxApiImpl(CruxConfiguration cruxConfiguration, Logger logger) {
        this(cruxConfiguration.getApiurl(), cruxConfiguration.getApiKey(), logger);
    }

    public CruxApiImpl(String apiurl, String apiKey, Logger logger) {
        this.apiurl = apiurl;
        this.apiKey = apiKey;
        this.log = logger;
        if (apiurl.contains("https:")) {
            CloseableHttpClient httpClient
                    = HttpClients.custom()
                    .setSSLHostnameVerifier(new NoopHostnameVerifier())
                    .build();
            HttpComponentsClientHttpRequestFactory requestFactory
                    = new HttpComponentsClientHttpRequestFactory();
            requestFactory.setHttpClient(httpClient);
        }
        restTemplate = new RestTemplate();
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {

        });
    }

    @Override
    public Map<String, Object> getTable(String datasetId, String tableName) {
        String obj = restTemplate.getForObject(
                apiurl + "/datasets/{datasetId}/tables/{tableName}?apikey={apikey}",
                String.class,
                datasetId, encodeUrl(tableName), apiKey);
        if (StringUtils.hasText(obj)) {
            return (Map<String, Object>) jsonToMap(obj, new TypeReference<Map<String, Object>>() {
            });
        }
        return null;
    }

    private Object jsonToMap(String obj, TypeReference typeReference) {
        try {
            return objectMapper.readValue(obj, typeReference);
        } catch (IOException e) {
            throw new IllegalStateException("Error parsing json - " + obj, e);
        }
    }

    @Override
    public boolean tableExists(String datasetId, String tableName) {
        boolean ret =  getTable(datasetId, tableName) != null;
        log.info(String.format("Table [%s] %s", tableName, ret ? "exists" : "does not exist"));
        return ret;
    }

    @Override
    public boolean resourceExists(String datasetId, String tableName) {
        //API is same for table and files and all resources
        return getTable(datasetId, tableName) != null;
    }


    @Override
    public void ensureTableExists(String datasetId, String tableName, String schema) {
        log.info("Checking if table " + tableName + " exists");
        synchronized (Long.class) {
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

    @Override
    public void createTable(String datasetId, String tableName, String schema) {
        createTable(datasetId, tableName, schema, null);
    }

    @Override
    public void createTable(String datasetId, String tableName, String schema, String description) {
        log.info(String.format("creating table [%s] with schema [%s]", tableName, schema));
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
            log.info(String.format("created table [%s]", tableName));

        } catch (Exception e) {
            throw new IllegalStateException(String.format("Error creating table [name : '%s', schema : '%s']", tableName, schema), e);
        }
    }

    @Override
    public void uploadFile(String datasetId, String fileName, String targetDir, String filePath, String mimeType) {
        log.info(String.format("Uploading file [%s] to [%s]", filePath, targetDir));
        LinkedMultiValueMap<String, Object> map = new LinkedMultiValueMap<>();
        map.add(fileName, new FileSystemResource(filePath));
        map.add("folder", targetDir);
        map.add("mimetype", mimeType);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        HttpEntity<LinkedMultiValueMap<String, Object>> entity = new HttpEntity<>(map, headers);
        ResponseEntity<String> result = restTemplate.postForEntity(apiurl + "/pushfile/{datasetId}?apikey={apikey}",
                entity,
                String.class,
                datasetId, apiKey);

        while (!resourceExists(datasetId, targetDir == "/" ?  fileName : targetDir + "/" + fileName)) {
            log.info(String.format("Waiting for [%s] to upload to [%s]", filePath, targetDir + "/" + fileName));
            sleep(5000);
        }
        log.info(String.format("Uploaded file [%s] to [%s]", filePath, targetDir + "/" + fileName));

    }

    @Override
    public void loadFileToTable(String datasetId, String fileName, String tableName, char delimiter, boolean truncate) {
        Preconditions.checkState(tableExists(datasetId, tableName), "No Table with name %s", tableName);
        if(truncate) {
            log.info("Overwriting table " + tableName + " with file " + fileName);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(toJson(ImmutableMap.of(
                "fileName", fileName,
                "delimiter", delimiter,
                "maxBadRecords", 0,
                "truncate", truncate)), headers);
        ResponseEntity<String> result = restTemplate.postForEntity(
                apiurl + "/datasets/{datasetId}/tables/{tableName}/loadfile?apikey={apikey}",
                entity,
                String.class,
                datasetId, tableName, apiKey);
        //TODO: check if all rows are loaded. Use FileRef as a param and not string fileName
    }

    @Override
    public void loadQueryResultsToTable(String datasetId, String query, String tableName, boolean allowLargeResults, String writeDisposition) {
        final Map<String, Object> table = getTable(datasetId, tableName);
        Preconditions.checkState(table != null, "No Table with name %s", tableName);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        log.info(String.format("Load table query is [%s]", query));
        HttpEntity<String> entity = new HttpEntity<>(toJson(ImmutableMap.of("query", query, "allowLargeResults", allowLargeResults, "writeDisposition", writeDisposition)), headers);

        final int tableRowCount = getRowCount(datasetId, tableName);

        log.info(String.format("[%s] rows in table [%s] ",getRowCount(datasetId, tableName),  tableName));

        int queryRowCount = getQueryRowCount(datasetId, query);
        log.info(String.format("[%s] rows form Query [%s]", queryRowCount, query));

        if(queryRowCount == 0) {
            log.info(String.format("[%s] rows form Query [%s]. Nothing to insert into [%s]", queryRowCount, query, tableName));
            return;
        }

        log.info(String.format("inserting [%s] rows into [%s] ",queryRowCount,  tableName));

        ResponseEntity<String> result = restTemplate.postForEntity(
                apiurl + "/table/{resourceid}/loadqueryresults?apikey={apikey}",
                entity,
                String.class,
                table.get("idPath"),
                apiKey);
        log.info(String.format("[%s] rows in table [%s] after insert.",getRowCount(datasetId, tableName),  tableName));

        final int expectedRowsInTable;
        if(WRITE_TRUNCATE.equals(writeDisposition)) {
            expectedRowsInTable = queryRowCount;
        } else {
            expectedRowsInTable = tableRowCount + queryRowCount;
        }

        while(getRowCount(datasetId, tableName) != expectedRowsInTable) {
            log.info("Waiting for 'query load to table' to complete");
            sleep(5000);
        }

    }

    public int getQueryRowCount(String datasetId, String query) {
        query = "select count (*) row_count from ( " + query + " )";
        List<Map<String, Object>> rows =  (List<Map<String, Object>>) runAdhocQuery(datasetId, query, new TypeReference<List<Map<String, Object>>>() {
        });
        log.info("getQueryRowCount() results - " + rows);
        return (Integer) rows.get(0).get("row_count");
    }

    @Override
    public String runAdhocQuery(String datasetId, String query) {
        HttpHeaders headers = new HttpHeaders();
        headers.setAccept(Lists.newArrayList(MediaType.APPLICATION_JSON));
        ResponseEntity<String> result = restTemplate.getForEntity(
                apiurl + "/datasets/{datasetId}/query?apikey={apikey}&query={query}",
                String.class,
                datasetId, apiKey, query);
        return result.getBody();
    }

    @Override
    public Object runAdhocQuery(String datasetId, String query, TypeReference targetType) {
        String res = runAdhocQuery(datasetId, query);
        return jsonToMap(res, targetType);
    }

    @Override
    public boolean isTableEmpty(String datasetId, String tableName) {
        return getRowCount(datasetId, tableName) == 0;
    }

    @Override
    public int getRowCount(String datasetId, String tableName) {
        Preconditions.checkState(tableExists(datasetId, tableName), "No Table with name %s", tableName);
        String res = runAdhocQuery(datasetId, "select count(*) rowcount from $" + tableName);
        List<Map<String, Object>> rows = (List<Map<String, Object>>) jsonToMap(res, new TypeReference<List<Map<String, Object>>>() {
        });
        Preconditions.checkState(rows.size() == 1, "Expecting one element in array - ", res);
        return (Integer) rows.get(0).get("rowcount");
    }

    @Override
    public void deleteResource(String datasetId, String resource) {
        Map<String, Object> res = getTable(datasetId, resource);
        Preconditions.checkState(res != null, "No Resource with name %s", resource);
        restTemplate.delete(apiurl + "/resource/{resourceid}?apikey={apikey}", res.get("idPath"), apiKey);
        log.info(String.format("Deleted resource [%s]", resource));
    }

    @Override
    public void renameResource(String datasetId, String resource, String newName) {
        Map<String, Object> res = getTable(datasetId, resource);
        Preconditions.checkState(res != null, "No Resource with name %s", resource);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>(toJson(ImmutableMap.of("name", newName)), headers);
        log.info(String.format("renaming resource [%s] to [%s]", res.get("name"), newName));
        ResponseEntity<String> result = restTemplate.postForEntity(
                apiurl + "/resource/{resourceid}/move?apikey={apikey}",
                entity,
                String.class,
                res.get("idPath"),
                apiKey);
        log.info(String.format("renamed resource [%s] to [%s]", res.get("name"), newName));

    }

    @Override
    public void mergeTable(String datasetId, String srcTable, String targetTable, String pk) {
        final Map<String, Object> table = getTable(datasetId, targetTable);
        Preconditions.checkState(table != null, "No Table with name %s", targetTable);
        Preconditions.checkState(tableExists(datasetId, targetTable), "No Table with name %s", targetTable);
        if (isTableEmpty(datasetId, srcTable)) {
            return;
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        log.info(String.format("Inserting all rows from [%s] that are not in [%s] into [%s]", targetTable, srcTable, srcTable));
        //BQ does not support updates.
        loadQueryResultsToTable(datasetId,
                String.format("select * from $%s where %s not in (select %s from $%s)", targetTable, pk, pk, srcTable),
                srcTable, true, WRITE_APPEND);
        log.info(String.format("Replacing rows of [%s] with [%s]", targetTable, srcTable));
        loadQueryResultsToTable(datasetId,
                String.format("select * from $%s", srcTable),
                targetTable, true, WRITE_TRUNCATE);
        deleteResource(datasetId, srcTable);
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


    private String encodeUrl(String tableName) {
        try {
            tableName = URLEncoder.encode(tableName, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
        return tableName;
    }

    public static void main(String[] args) {
        String datsetid = "WyJQcm9maWxlIiwidU9tTFhtRGYydWJXdDRabkJzamR3dlFYVlB2MSIsIkRhdGFTZXQiLDU3NTA5NzUzNjkzMTQzMDRd";
        CruxApi cruxApi = new CruxApiImpl("http://localhost:8082/api", "a84f086e70b6b14a85c47a9dc1f6da88", Logger.getLogger(CruxApiImpl.class));


//        System.out.println(cruxApi.isTableEmpty(datsetid, "hist"));

//        cruxApi.uploadFile(datsetid, "GNM_ARM.SIG", "/", "/Users/avinash.palicharla/embs-ftp-emulator/Signal/GNM_ARM.SIG");

//        cruxApi.uploadFile(datsetid, "GNM_ARM.ZIP.01", "/", "/Users/avinash.palicharla/embs-ftp-emulator/Products/GNM_ARM.ZIP", "text/csv");
//        cruxApi.loadFileToTable(datsetid, "/files/2017-07-27/GNM_ARM.DAT.1501163559914.gzip", "adjrate", ',');
//        System.out.println(cruxApi.resourceExists(datsetid, "adjrate"));

       System.out.println(cruxApi.resourceExists(datsetid, "/files/2017-07-27/GNM_ARM.DAT.1501163559914.gzip"));

    }
}
