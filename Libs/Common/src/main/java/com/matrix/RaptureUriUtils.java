package com.matrix;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import rapture.common.RaptureURI;
import rapture.common.Scheme;

public class RaptureUriUtils {

    private static final String WORKORDER_DELIMETER = "&workorder=";

    /**
     * Builds work order url that is accessible form the browser.
     *
     * @param workOrderUri - The work order or worker uri
     * @return A url to access the work order from Rapture UI.
     */
    public static String buildWorkOrderHTTPUrl(String workOrderUri) {
        Preconditions.checkArgument(isWorkOrderUri(workOrderUri), "[%s] is not a work-order/worker uri", workOrderUri);
        StringBuilder externalUrl = new StringBuilder();

        String docPath = new RaptureURI(workOrderUri).getDocPath();
        int lio = docPath.lastIndexOf('/');
        if (lio < 0) lio = 0;

        externalUrl.append(getUIURL()).append("/process/")
                .append(docPath.substring(0, lio)).append(WORKORDER_DELIMETER).append(docPath.substring(lio + 1));
        return externalUrl.toString();
    }


    public static boolean isWorkOrderUri(String workOrderUri) {
        try {
            return new RaptureURI(workOrderUri).getScheme() == Scheme.WORKORDER;
        }catch(Exception e) {
            return false;
        }
    }

    public static String getUIURL() {
        String ret = System.getenv("UI_URL");
        Preconditions.checkArgument(StringUtils.isNoneEmpty(ret),
                "UI_URL is null/empty. Set docker environment property UI_URL (eg:- -e UI_URL=http://localhost:8000)");

        ret = ret.trim();
        if (ret.endsWith("/")) {
            ret = ret.substring(0, ret.length() - 1);
        }
        return ret;
    }


}
