package com.crux.embs;


import com.fasterxml.jackson.core.type.TypeReference;

import java.util.Map;

public interface CruxApi {


     String WRITE_APPEND = "WRITE_APPEND";
     String WRITE_TRUNCATE = "WRITE_TRUNCATE";


    Map<String, Object> getTable(String datasetId, String tableName);

    boolean tableExists(String datasetId, String tableName);

    boolean resourceExists(String datasetId, String tableName);

    void ensureTableExists(String datasetId, String tableName, String schema);

    void createTable(String datasetId, String tableName, String schema);

    void createTable(String datasetId, String tableName, String schema, String description);

    void uploadFile(String datasetId, String fileName, String targetDir, String filePath, String mimetype);

    void loadFileToTable(String datasetId, String fileName, String tableName, char delimiter, boolean truncate);

    void loadQueryResultsToTable(String datasetId, String query, String tableName, boolean allowLargeResults, String writeDisposition);

    String runAdhocQuery(String datasetId, String query);

    Object runAdhocQuery(String datasetId, String query, TypeReference targetType);

    boolean isTableEmpty(String datasetId, String resource);

    int getRowCount(String datasetId, String table);

    void deleteResource(String datasetId, String resource);

    void renameResource(String datasetId, String resource, String newName);

    void mergeTable(String datasetId, String srcTable, String targetTable, String pk);
}
