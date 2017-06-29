package com.crux.embs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.zip.GZIPOutputStream;

public class Gzip {

    Logger log = LoggerFactory.getLogger(Gzip.class);

    public String gzip(String file, String gzipDir ) {
        File dir = new File(gzipDir);
        if(!dir.exists()) {
            dir.mkdirs();
        }
        String gzipFile = Paths.get(gzipDir, new File(file).getName() + ".gzip").toString(); //gzipDir + new File(file).getName() + ".gz";

        byte[] buffer = new byte[1024];

        try{
            GZIPOutputStream gzos =
                    new GZIPOutputStream(new FileOutputStream(gzipFile));

            FileInputStream in = new FileInputStream(new File(file));

            int len;
            while ((len = in.read(buffer)) > 0) {
                gzos.write(buffer, 0, len);
            }

            in.close();

            gzos.finish();
            gzos.close();

            log.info("gzipped file {} to {}", file, gzipFile);

            return gzipFile;

        }catch(IOException ex){
            throw new IllegalStateException(ex);
        }
    }

}
