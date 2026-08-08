package com.codebrain.common;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.util.ContentCachingResponseWrapper;

public class ApiResponseWrapper extends ContentCachingResponseWrapper {
    public ApiResponseWrapper(HttpServletResponse response) {
        super(response);
    }

    public String getBody() {
        byte[] buf = getContentAsByteArray();
        return buf.length > 0 ? new String(buf) : "";
    }
}