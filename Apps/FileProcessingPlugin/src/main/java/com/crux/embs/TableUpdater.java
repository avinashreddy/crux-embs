package com.crux.embs;

import com.google.common.base.Preconditions;
import org.apache.log4j.Logger;

public class TableUpdater {

    private final Logger log;

    private final CruxApi cruxApi;

    private final String datasetId;

    public TableUpdater(CruxApi cruxApi, String datasetId, Logger log) {
        this.log = log;
        this.cruxApi = cruxApi;
        this.datasetId = datasetId;
    }

    public void loadCruxFileToTable(String cruxFilePath, int fileLineCount, TableConfig tableConfig, boolean truncate) {
        log.info(String.format("Loading crux file [%s] to table [%s]", cruxFilePath, tableConfig.getTableName()));


        Preconditions.checkState(cruxApi.tableExists(datasetId, tableConfig.getTableName()),
                "Table does not exist '%s'", tableConfig.getTableName());

        final int tableRowCount = cruxApi.getRowCount(datasetId, tableConfig.getTableName());
        log.info(String.format("Row count on table - [%s]", tableRowCount));
        log.info(String.format("Record count on file - [%s]", fileLineCount));


        if (truncate || tableRowCount == 0) {
            cruxApi.loadFileToTable(datasetId, cruxFilePath, tableConfig.getTableName(), ',', truncate);
        } else {
            String tempTable = tableConfig.getTempTableName();
            log.info(String.format("Table [%s] has rows that might be updated. Loading file [%s] to temp table [%s]",
                    tableConfig.getTableName(), cruxFilePath, tempTable));
            if (!cruxApi.tableExists(datasetId, tempTable)) {
                log.info("Temp table " + tempTable + " does not exist. Creating table.");
                cruxApi.createTable(datasetId, tempTable, tableConfig.getSchema());
            }
            cruxApi.loadFileToTable(datasetId, cruxFilePath, tempTable, ',', true);
            cruxApi.mergeTable(datasetId, tempTable, tableConfig.getTableName(), tableConfig.getTableUpdateKey());
        }
        log.info("Loaded file " + cruxFilePath + " to table " + tableConfig.getTableName());
        log.info(String.format("Table row count after load - [%s]", cruxApi.getRowCount(datasetId, tableConfig.getTableName())));
    }

}
