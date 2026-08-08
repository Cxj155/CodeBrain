package com.codebrain.common;

import com.codebrain.domain.dto.ApiLogDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiLogFilter implements Filter {
    private static final ExecutorService ASYNC_LOG_POOL = Executors.newSingleThreadExecutor();
    private static final Logger apiLogger = LogManager.getLogger("ApiLogFile");
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private boolean isSkipPath(String uri) {
        return uri.startsWith("/swagger-ui") || uri.equals("/actuator/health");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest rawRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse rawResponse = (HttpServletResponse) servletResponse;
        String requestUri = rawRequest.getRequestURI();

        final String fixedTraceId;
        String tempTraceId = rawRequest.getHeader("X-Trace-Id");
        if (tempTraceId == null || tempTraceId.isBlank()) {
            fixedTraceId = UUID.randomUUID().toString();
        } else {
            fixedTraceId = tempTraceId;
        }

        if (isSkipPath(requestUri)) {
            filterChain.doFilter(rawRequest, rawResponse);
            return;
        }

        ApiRequestWrapper requestWrapper = new ApiRequestWrapper(rawRequest);
        ApiResponseWrapper responseWrapper = new ApiResponseWrapper(rawResponse);

        long startTime = System.currentTimeMillis();
        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long costTime = System.currentTimeMillis() - startTime;
            final String respBody = responseWrapper.getBody();
            final int httpStatus = responseWrapper.getStatus();
            final String reqBody = requestWrapper.getBody();
            final Map<String, String> headerMap = getHeaderMap(requestWrapper);

            ASYNC_LOG_POOL.submit(() -> {
                ApiLogDTO logDto = new ApiLogDTO();
                logDto.setTraceId(fixedTraceId);
                logDto.setMethod(requestWrapper.getMethod());
                logDto.setUri(requestWrapper.getRequestURI());
                logDto.setStatus(httpStatus);
                logDto.setDuration(costTime);
                logDto.setTimestamp(System.currentTimeMillis());
                logDto.setHeaders(headerMap);
                logDto.setRequestBody(reqBody);
                logDto.setResponseBody(respBody);

                try {
                    String json = OBJECT_MAPPER.writeValueAsString(logDto);
                    apiLogger.info(json);
                } catch (Exception e) {
                    apiLogger.error("日志序列化失败", e);
                }
            });
            responseWrapper.copyBodyToResponse();
        }
    }

    private Map<String, String> getHeaderMap(HttpServletRequest req) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = req.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            headerMap.put(key, req.getHeader(key));
        }
        return headerMap;
    }
}
