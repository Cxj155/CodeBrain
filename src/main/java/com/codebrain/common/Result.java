package com.codebrain.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.UUID;

public class Result<T> {
    private Integer code;
    private String message;
    private T data;

    @JsonProperty("traceId")
    private String traceId;

    public static <T> Result<T> success(T data) {
        Result<T> r = new Result<>();
        r.code = 0;
        r.message = "ok";
        r.data = data;
        r.traceId = UUID.randomUUID().toString().replace("-", "");
        return r;
    }

    public static <T> Result<T> error(String msg, Exception e) {
        return build(500, msg, null);
    }

    public static <T> Result<T> build(Integer code, String msg, T data) {
        Result<T> r = new Result<>();
        r.code = code;
        r.message = msg;
        r.data = data;
        r.traceId = UUID.randomUUID().toString().replace("-", "");
        return r;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}