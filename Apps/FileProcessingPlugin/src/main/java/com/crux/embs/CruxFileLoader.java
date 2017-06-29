package com.crux.embs;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class CruxFileLoader implements FileLoader {

    Logger log = LoggerFactory.getLogger(CruxFileLoader.class);

    private FileConfigLookup fileConfigLookup = new FileConfigLookup();

    private TableConfigLookup tableConfigLookup = new TableConfigLookup();

    private final Crux crux;

    private final String datasetId;

    public CruxFileLoader(CruxConfiguration cruxConfiguration, String datasetId) {
        Preconditions.checkNotNull(cruxConfiguration, "cruxConfiguration is null");
        Preconditions.checkState(StringUtils.isNoneEmpty(datasetId), "datasetId is null");

        this.crux = new Crux(cruxConfiguration);
        this.datasetId = datasetId;
    }

    @Override
    public long load(String filePath) {
        log.info("Loading file to CRUX - " + filePath);

        File file = new File(filePath);
        Preconditions.checkState(file.exists(), "filePath does not exist");
        FileConfig fileConfig = fileConfigLookup.getByFileName(file.getName());
        TableConfig tableConfig = tableConfigLookup.get(fileConfig.getTable());

        crux.ensureTableExists(datasetId, tableConfig.getTableName(), tableConfig.getSchema());

        log.info("Uploading file " + filePath);
        crux.uploadFile(datasetId, file.getName(), filePath);
        log.info("Uploaded file " + filePath);

        log.info("Loading file " + file.getName() + " to table " + tableConfig.getTableName());
        crux.loadFileToTable(datasetId, file.getName(), tableConfig.getTableName(), ',');
        log.info("Loaded file " + file.getName() + " to table " + tableConfig.getTableName());

        return 0L;
    }


}
