package com.extract.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.MDC;
import org.springframework.cglib.core.Local;

import com.extract.context.ContextUtil;
import com.extract.context.RestContext;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@WebFilter(urlPatterns = {"/v1/iCode"})
public class ApiResrFilter implements Filter {
    private String HEADER_TRACE_ID_KEY = "x-trace-id";
    private static final String UNIQUE_ID = "traceId";

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        ContextUtil.initCurrent();
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        initContext(request);
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        MDC.remove(UNIQUE_ID);
        ContextUtil.clearCurrent();
    }

    private void initContext(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        getRequestHeader(request, map);
        RestContext restContext = ContextUtil.getOrSetCurrent();
        restContext.initHeaderMapper(map);
        restContext.setTraceId(map.get(HEADER_TRACE_ID_KEY));
        MDC.put(UNIQUE_ID, map.get(HEADER_TRACE_ID_KEY));
    }

    private void getRequestHeader(HttpServletRequest request, Map<String, String> map) {
        Enumeration headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = (String) headerNames.nextElement();
            String value = request.getHeader(key);
            String lowerCaseKey = key.toLowerCase(Locale.ENGLISH);
            map.put(lowerCaseKey, value);
        }
        String traceId = map.get(HEADER_TRACE_ID_KEY);
        if (StringUtils.isEmpty(traceId)) {
            traceId = UUID.randomUUID().toString();
            map.put(HEADER_TRACE_ID_KEY, traceId);
        }
    }
}
