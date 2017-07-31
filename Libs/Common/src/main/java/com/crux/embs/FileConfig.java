package com.crux.embs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;

public class FileConfig {

    public static final String UPDATE_TYPE_RL = "RL";
    public static final String UPDATE_TYPE_UI = "UI";


    private String fileType;
    private String table;
    private String updateType;


    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getUpdateType() {
        return updateType;
    }

    public void setUpdateType(String updateType) {
        this.updateType = updateType;
    }

    @JsonIgnore
    public boolean isReload() {
        validateUpdateType();
        return UPDATE_TYPE_RL.equalsIgnoreCase(updateType);
    }

    @JsonIgnore
    public void validate() {
        Preconditions.checkState(StringUtils.isNoneEmpty(fileType), "fileType is null empty");
        Preconditions.checkState(StringUtils.isNoneEmpty(table), "table is null empty");
        validateUpdateType();
    }

    private void validateUpdateType() {
        Preconditions.checkState(UPDATE_TYPE_RL.equalsIgnoreCase(updateType) || UPDATE_TYPE_UI.equalsIgnoreCase(updateType),
                "Unknown updateType [%s]. Expecting one of %s and %s", updateType, UPDATE_TYPE_RL, UPDATE_TYPE_UI);
    }

}
