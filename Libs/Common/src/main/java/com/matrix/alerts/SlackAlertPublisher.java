package com.matrix.alerts;

import com.google.common.net.MediaType;
import com.matrix.common.Config;
import org.apache.log4j.Logger;
import rapture.common.impl.jackson.JacksonUtil;

import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * An alert publisher that sends slack alerts.
 */
public class SlackAlertPublisher implements AlertPublisher {

    private void sendSlack(String webhook, String message) throws IOException {
        URL url = new URL(webhook);
        Map<String, String> slackNotification = new HashMap<>();
        slackNotification.put("text", message);
        int response = doPost(url, JacksonUtil.bytesJsonFromObject(slackNotification));
        if (response != 200) {
            Logger.getRootLogger().fatal("slack notification failed with HTTP error code " + response);
        }
    }

    //TODO: use spring rest template
     private int doPost(URL url, byte[] body) throws IOException {
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setFixedLengthStreamingMode(body.length);
        http.setRequestProperty("Content-Type", MediaType.JSON_UTF_8.toString());
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.connect();
        try (OutputStream stream = http.getOutputStream()) {
            stream.write(body);
        }
        int response = http.getResponseCode();
        http.disconnect();
        return response;
    }

    @Override
    public void sendAlert(Config config, String message)  {
        try {
            sendSlack(config.getString("SLACK_WEBHOOK"), message);
        } catch (IOException e) {
            throw new IllegalStateException("Error sending slack alert", e);
        }
    }
}
