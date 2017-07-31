package com.crux.embs;

public interface LineTransformer {

    String transFormLine(String line);

    TransformResult transFormLineResult(String line);

    static class TransformResult {
        public final String line;
        public final String[] cols;

        public TransformResult(String line, String[] cols) {
            this.line = line;
            this.cols = cols;
        }
    }
}
