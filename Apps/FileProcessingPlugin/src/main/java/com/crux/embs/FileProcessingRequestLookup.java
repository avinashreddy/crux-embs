package com.crux.embs;

import com.google.common.base.Preconditions;
import rapture.common.BlobContainer;
import rapture.common.CallingContext;
import rapture.kernel.Kernel;

import java.nio.charset.Charset;

public class FileProcessingRequestLookup {

    public static FileProcessingRequest get(CallingContext ctx, String bloburi) {

        BlobContainer blob = getBlob(ctx, bloburi);
        Preconditions.checkNotNull(blob, "No blob at URI [%s]", bloburi);

        return FileProcessingRequest.fromJSON(new String(blob.getContent(), Charset.forName("UTF-8")));
    }

    private static BlobContainer getBlob(CallingContext ctx, String blobUri) {
        BlobContainer b = Kernel.getBlob().getBlob(ctx, blobUri);
        Preconditions.checkState(b != null, "Null value for BLOB at " + blobUri);
        return b;
    }
}
