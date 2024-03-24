package org.webflow.utils.http;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class HttpParamsUtils {
    private static final Logger LOGGER = LoggerFactory.getLogger(HttpParamsUtils.class.getCanonicalName());
    private HttpParamsUtils() {
    }

    public static Map<String, List<String>> getRawParamsMapByQueryStr(String queryStr) {
        LOGGER.info("Build raw params map of qs: {}", queryStr);
        if (queryStr == null) return null;
        Map<String, List<String>> params = new HashMap<>();
        String[] paramsPairs = queryStr.split("&");
        for (String paramsPair : paramsPairs) {
            String[] paramKeyVal = paramsPair.split("=");
            if (paramKeyVal.length == 2) {
                String paramKey = paramKeyVal[0];
                String paramVal = paramKeyVal[1];
                if (params.get(paramKey) == null) {
                    params.put(paramKey,
                            new ArrayList<>(Collections.singletonList(paramVal)));
                } else {
                    params.get(paramKey).add(paramVal);
                }
            }
        }
        return params;
    }
}
