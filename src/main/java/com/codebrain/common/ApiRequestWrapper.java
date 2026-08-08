package com.codebrain.common;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.util.ContentCachingRequestWrapper;

public class ApiRequestWrapper extends ContentCachingRequestWrapper {
    public ApiRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public String getBody() {
        byte[] contentAsByteArray = getContentAsByteArray();
        return contentAsByteArray.length > 0 ? new String(contentAsByteArray) : "";
    }
}