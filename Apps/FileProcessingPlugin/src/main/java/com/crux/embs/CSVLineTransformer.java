package com.crux.embs;


public class CSVLineTransformer implements LineTransformer {

    public String transFormLine(String line) {
        return (String) transFormLine(line, false);
    }

    private Object transFormLine(String line, boolean returnResultObj) {
        String[] cols = line.split("\\|");
        StringBuilder sb = new StringBuilder();
        for(int i = 0; i< cols.length; i++) {
            sb.append("\"").append(cols[i].replaceAll("\"", "\\\\\"")).append("\"");
            if(i < cols.length - 1) {
                sb.append(",");
            }
        }
        if(line.endsWith("|")) {
            sb.append(",\"\"");
        }
        if(returnResultObj)  {
            return new TransformResult(sb.toString(), cols);
        }

        return  sb.toString();
    }

    @Override
    public TransformResult transFormLineResult(String line) {
        return null;
    }

    public static void main(String a[]) {
        String line = "1|101|0|DET|FNM|LOAN|FIX|FNM30|ALL|7.5|1980| 0|20170701|FNM     1|279758125.91|28|0.|0|0.|0.|0.|0.|0.|0.|0.|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.0|0.0|0.0|-999.0|-999|-999|-999.|-999.|0.|-999.|-999|-999|84.7109|-999.|0.|-999.|-999|-999|43.219|-999.|0.|-999.|-999|-999|23.903|-999.|0.|-999.|-999|-999|14.8668|-999.|0.|-999.|-999|-999|10.0738|-999.|0.|-999.|-999|-999|7.2485|-999.|0.|-999.|-999|-999|5.4611|-999.|0.|-999.|-999|-999|-999.|-999.|-999.|-999.|-999.|1980|1980|20170711 18:15:00|";
        new CSVLineTransformer().transFormLine(line);
    }
}

