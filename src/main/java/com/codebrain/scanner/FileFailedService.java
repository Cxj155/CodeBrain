package com.codebrain.scanner;

public interface FileFailedService {

    boolean shouldSkip(Long fileId);

    void recordFail(Long fileId, String reason, String errorMsg);
}

