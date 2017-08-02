package com.crux.embs;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TableConfig {

    public static final String COMPOUND_PK_COL = "__pk";
    private String tableName;
    private String schema;
    private String pks;

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public String getPks() {
        return pks;
    }

    public void setPks(String pks) {
        this.pks = pks;
    }

    @JsonIgnore
    public List<String> getColNames() {
        return Arrays.stream(schema.split(",")).map(s -> s.split(":")[0].toLowerCase()).collect(Collectors.toList());
    }

    @JsonIgnore
    public List<String> getPkColNames() {
        hasPksCheck();
        if(pks.indexOf(",") > 0) {
            return Arrays.stream(pks.split(",")).map(s -> s.toLowerCase()).collect(Collectors.toList());
        } else {
            return Lists.newArrayList(pks);
        }
    }

    private void hasPksCheck() {
        Preconditions.checkState(hasPks(), "No pks defined for " + this.getTableName());
    }

    @JsonIgnore
    public boolean hasPks() {
        return !StringUtils.isEmpty(pks);
    }

    @JsonIgnore
    public String getTableUpdateKey() {
        hasPksCheck();
        if(getPkColNames().size() == 1) {
            return getColNames().get(0);
        }
        return COMPOUND_PK_COL;
    }

    @JsonIgnore
    public int[] getPkColIndices() {
        hasPksCheck();
        List<String> colNames = getColNames();
        List<String> pkCols = getPkColNames();
        int[] pkIndices = new int[pkCols.size()];

        for(int i = 0, j = 0;i < colNames.size(); i++) {
            if(pkCols.contains(colNames.get(i))) {
                pkIndices[j++] = i;
            }
        }
        return pkIndices;
    }

    @JsonIgnore
    public String getTempTableName() {
        Preconditions.checkState(StringUtils.isNoneEmpty(tableName), "tableName is null empty");
        return tableName + "_temp" ;
    }

    public void validate() {
        Preconditions.checkState(StringUtils.isNoneEmpty(tableName), "tableName is null/empty");
        Preconditions.checkState(StringUtils.isNoneEmpty(schema), "schema is null/empty");
        if( hasPks() && getPkColNames().size() > 1) {
            Preconditions.checkState(schema.trim().endsWith("," + COMPOUND_PK_COL + ":string"),
                    "Compound Pk. Add column " + COMPOUND_PK_COL + ":string to schema. Table is " + this.getTableName());
        }
    }
}
