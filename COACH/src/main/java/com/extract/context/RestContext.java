package com.extract.context;

import java.util.HashMap;
import java.util.Map;

public class RestContext {
    private Map<String, String> headerMap = new HashMap<>();

    private String traceId;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public Map<String, String> getHeaderMap() {
        return headerMap;
    }

    public void initHeaderMapper(Map<String, String> mapper) {
        if (mapper == null) {
            return;
        }
        headerMap.clear();
        headerMap.putAll(mapper);
    }
}
