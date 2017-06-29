package com.crux.embs;


public class CSVLineTransformer implements LineTransformer {

    public String transFormLine(String line) {
        String[] cols = line.split("\\|");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i< cols.length; i++) {
            sb.append("\"").append(cols[i].replaceAll("\"", "\\\\\"")).append("\"");
            if(i < cols.length - 1) {
                sb.append(",");
            }
        }
        return  sb.toString();
    }
}
