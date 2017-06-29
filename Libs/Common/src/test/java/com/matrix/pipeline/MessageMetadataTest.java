package com.matrix.pipeline;

import org.json.JSONException;
import org.junit.Assert;
import org.junit.Test;
import org.skyscreamer.jsonassert.JSONAssert;

public class MessageMetadataTest {

    MessageMetadata messageMetadata;

    @Test
    public void toJson() throws JSONException {
        messageMetadata = new MessageMetadata("blob://a/b/c", "text/fix", "ExecutionReport");
        String json = messageMetadata.toJson();
        JSONAssert.assertEquals("{\"uri\":\"blob://a/b/c\",\"contentType\":\"text/fix\",\"messageType\":\"ExecutionReport\",\"properties\":[]}", json, false);
    }

    @Test
    public void toJsonWithProperties() throws JSONException {
        messageMetadata = new MessageMetadata("blob://a/b/c", "text/fix", String.class.getSimpleName());
        messageMetadata.addProperty(new MessageMetadata.Property("dataDictionary", "FIX44"));
        messageMetadata.addProperty(new MessageMetadata.Property("source", "ABC"));
        messageMetadata.addProperty(new MessageMetadata.Property("price", 10.001));


        String json = messageMetadata.toJson();
        JSONAssert.assertEquals("{\"uri\":\"blob://a/b/c\",\"contentType\":\"text/fix\",\"messageType\":\"String\",\"properties\":[{\"name\":\"dataDictionary\",\"value\":\"FIX44\"},{\"name\":\"source\",\"value\":\"ABC\"}, {\"name\":\"price\",\"value\": 10.001}]}", json, false);
    }

    @Test
    public void fromJSON() {
        messageMetadata = MessageMetadata.fromJSON("{\"uri\":\"blob://a/b/c\",\"contentType\":\"text/fix\",\"messageType\":\"String\",\"properties\":[{\"name\":\"dataDictionary\",\"value\":\"FIX44\"},{\"name\":\"source\",\"value\":\"ABC\"}]}");
        Assert.assertEquals(messageMetadata.getMessageType(), "String");
        Assert.assertEquals(messageMetadata.getContentType(), "text/fix");
        Assert.assertEquals(messageMetadata.getUri(), "blob://a/b/c");


    }
}
