package com.crux.embs;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.io.File;
import java.net.URISyntaxException;

@Ignore
public class CruxApiIntegrationTest {

    private String datasetid = "WyJQcm9maWxlIiwidU9tTFhtRGYydWJXdDRabkJzamR3dlFYVlB2MSIsIkRhdGFTZXQiLDU2NjIxNTc1OTMxMTY2NzJd";

    CruxApiImpl cruxApi = new CruxApiImpl(
            "http://localhost:8082/api",
            "a84f086e70b6b14a85c47a9dc1f6da88",
            Logger.getLogger(CruxApiImpl.class));

    @Test
    public void fileExists() {
        cruxApi.uploadFile(datasetid, "country_pop.csv", "/", getClasspathFile("TEST_FILE.CSV"), "text/csv");
        Assert.assertEquals(true, cruxApi.resourceExists(datasetid, "country_pop.csv"));
        deleteResources("country_pop.csv");
    }

    @Test
    public void getQueryRowCount() {
        cruxApi.createTable(datasetid, "country_pop", "cont:string,country:string,pop:integer");
        cruxApi.uploadFile(datasetid, "country_pop.csv", "/", getClasspathFile("TEST_FILE.CSV"), "text/csv");
        cruxApi.loadFileToTable(datasetid, "/country_pop.csv", "country_pop", ',');
        Assert.assertEquals(5, cruxApi.getRowCount(datasetid, "country_pop"));
        Assert.assertEquals(5, cruxApi.getQueryRowCount(datasetid, "select * from $country_pop"));
        deleteResources("country_pop.csv", "country_pop");
    }

    @Test
    public void loadTableFromQuery() {
        safeDeleteResources("country_pop", "country_pop.csv");
        cruxApi.createTable(datasetid, "country_pop", "cont:string,country:string,pop:integer");
        cruxApi.uploadFile(datasetid, "country_pop.csv", "/", getClasspathFile("TEST_FILE.CSV"), "text/csv");
        cruxApi.loadFileToTable(datasetid, "/country_pop.csv", "country_pop", ',');
        cruxApi.loadQueryResultsToTable(datasetid,"select * from $country_pop", "country_pop", false, "WRITE_APPEND");
        Assert.assertEquals(10, cruxApi.getRowCount(datasetid, "country_pop"));
        deleteResources("country_pop.csv", "country_pop");
    }

    @Test
    public void mergeTable() {
        safeDeleteResources("country_pop", "country_pop.csv", "country_pop_tmp", "country_pop_update.csv");

        cruxApi.createTable(datasetid, "country_pop", "cont:string,country:string,pop:integer");
        cruxApi.uploadFile(datasetid, "country_pop.csv", "/", getClasspathFile("TEST_FILE.CSV"), "text/csv");
        cruxApi.loadFileToTable(datasetid, "/country_pop.csv", "country_pop", ',');

        cruxApi.createTable(datasetid, "country_pop_tmp", "cont:string,country:string,pop:integer");
        cruxApi.uploadFile(datasetid, "country_pop_update.csv", "/", getClasspathFile("TEST_FILE_UPDATE.CSV"), "text/csv");
        cruxApi.loadFileToTable(datasetid, "/country_pop_update.csv", "country_pop_tmp", ',');

        cruxApi.mergeTable(datasetid, "country_pop_tmp", "country_pop", "country");
        Assert.assertEquals(6, cruxApi.getRowCount(datasetid, "country_pop"));
        deleteResources("country_pop", "country_pop.csv", "country_pop_tmp", "country_pop_update.csv");
    }

    public String getClasspathFile(String path) {
        try {
            return new File(this.getClass().getClassLoader().getResource(path).toURI()).getAbsolutePath();
        } catch (URISyntaxException e) {
            throw new IllegalStateException(e);
        }
    }

    private void safeDeleteResources(String... resources) {
        for(String res : resources) {
            if(cruxApi.resourceExists(datasetid, res)) {
                cruxApi.deleteResource(datasetid, res);
            }
        }
    }

    private void deleteResources(String... resources) {
        for(String res : resources) {
            cruxApi.deleteResource(datasetid, res);
            Assert.assertEquals(false, cruxApi.resourceExists(datasetid, res));
        }
    }

    @Test
    public void mergeTable2() {
        String datasetid = "WyJQcm9maWxlIiwiZU1CUyIsIkRhdGFTZXQiLDU2Mjk0OTk1MzQyMTMxMjBd";

        cruxApi.createTable(datasetid, "loandist3_v1", "rectype:string,agency:string,poolnumber:string,cusip:string,issueid:integer,effdt:string,value1:string,value2:string,value3:string,rpb:float,pctrpb:float,loans:integer,updsrc:string,upddt:string,load_time:timestamp,source_file:string");
        cruxApi.loadQueryResultsToTable(datasetid,
                "SELECT *  FROM $loandist3 \n" +
                        "\n" +
                        "where issueid in (\n" +
                        "3583250,\n" +
                        "3577055,\n" +
                        "3579863,\n" +
                        "3574811,\n" +
                        "3575883,\n" +
                        "3573763,\n" +
                        "3578194,\n" +
                        "3582427,\n" +
                        "3572800,\n" +
                        "3581756,\n" +
                        "3586044,\n" +
                        "3588427,\n" +
                        "3584332,\n" +
                        "3571693,\n" +
                        "3579246,\n" +
                        "3570461,\n" +
                        "3580549,\n" +
                        "3586833,\n" +
                        "3575884\n" +
                        ")", "loandist3_v1", true, "WRITE_TRUNCATE");

        cruxApi.mergeTable(datasetid, "loandist3_v1", "loandist3", "issueid");

        Assert.assertEquals(11181496, cruxApi.getRowCount(datasetid, "loandist3"));
    }
}
