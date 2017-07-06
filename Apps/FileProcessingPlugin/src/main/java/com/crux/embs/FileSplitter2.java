package com.crux.embs;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import rapture.dp.invocable.embs.TransformFileStep;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class FileSplitter2 {

    final Logger log;

    public FileSplitter2(Logger logger) {
        this.log = logger;
    }

    public Map<String, Object> split(String filePath, String targetDir, LineTransformer lineTransformer, String extension) {
        log.info(String.format("Processing file %s", filePath));
        final LineIterator it;
        try {
            it = FileUtils.lineIterator(new File(filePath), "UTF-8");
        } catch (IOException e) {
            throw new IllegalStateException("Error building LineIterator " + filePath);
        }


        BufferedWriter buffer = null;
        final String targetFile = getTargetFile(filePath, targetDir, extension);
        int lineCount = 0;
        try {
            if(new File(targetFile).exists()) {
                FileUtils.forceDelete(new File(targetFile));
            }
            buffer = new BufferedWriter(openWriter(new File(targetFile), true));
            int colCount = -1;
            while (it.hasNext()) {
                String line = it.nextLine();
                int lineColCount = getColCount(line, '|');
                if (colCount == -1) {
                    colCount = lineColCount;
                } else {
                    Preconditions.checkState(colCount == lineColCount, "Expected " + colCount + ". Found " + lineColCount);
                }
                IOUtils.write(lineTransformer.transFormLine(line), buffer);
                IOUtils.write(IOUtils.LINE_SEPARATOR, buffer);
                if(++lineCount % 100000 == 0) {
                    log.info(lineCount + " lines written to file " + targetFile);
                }
            }
            log.info("File transformed - " + targetFile);
            buffer.flush();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            IOUtils.closeQuietly(buffer);
            LineIterator.closeQuietly(it);
        }
        return ImmutableMap.of("fileName", targetFile, "lineCount", lineCount);
    }

    private int getColCount(String line, char s) {
        //TODO: don't count escaped chars
        return StringUtils.countMatches(line, s);
    }

    private String getTargetFile(String filePath, String targetDir, String extension) {
        File f = new File(filePath);
        return targetDir + "/" + f.getName() + extension;
    }

    private void writeToFile_(String filePath, List<String> lines) {
        log.info("writing to file " + filePath);
        try {
            FileUtils.writeLines(new File(filePath), lines, true);
        } catch (IOException e) {
            throw new IllegalStateException("Error writing to file " + filePath);
        }
    }

    public static FileWriter openWriter(final File file, final boolean append) throws IOException {
        if (file.exists()) {
            if (file.isDirectory()) {
                throw new IOException("File '" + file + "' exists but is a directory");
            }
            if (file.canWrite() == false) {
                throw new IOException("File '" + file + "' cannot be written to");
            }
        } else {
            final File parent = file.getParentFile();
            if (parent != null) {
                if (!parent.mkdirs() && !parent.isDirectory()) {
                    throw new IOException("Directory '" + parent + "' could not be created");
                }
            }
        }
        return new FileWriter(file, append);
    }

    public static void main(String a[]) {
        long start = System.currentTimeMillis();
        FileSplitter2 splitter2 = new FileSplitter2(Logger.getLogger(FileSplitter2.class));
        splitter2.split(
                "/Users/avinash.palicharla/embs-ftp-emulator/Products/GNM_LDST.DAT",
                "/Users/avinash.palicharla/embs-ftp-emulator/Products/temp",
                new TransformFileStep.MetadataAddingLineTransformer("aaaaaaaaaa,bbbbbbbb,"),
//                new LineTransformer() {
//                    final String x = "aaaaaaaaaa|bbbbbbbb|";
//
//                    @Override
//                    public String transFormLine(String line) {
//                        return x.concat(line);
//                    }
//                },
                ".DAT"
        );
        System.out.println((System.currentTimeMillis() - start) / 1000);
    }
}
