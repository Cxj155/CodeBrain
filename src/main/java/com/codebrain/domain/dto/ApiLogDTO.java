package com.codebrain.domain.dto;

import lombok.Data;
import java.util.Map;

@Data
public class ApiLogDTO {
    private String traceId;
    private String method;
    private String uri;
    private Integer status;
    private Long duration;
    private Map<String, String> headers;
    private String requestBody;
    private String responseBody;
    private Long timestamp;
}