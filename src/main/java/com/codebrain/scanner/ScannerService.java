package com.codebrain.scanner;

import java.nio.file.Path;

public interface ScannerService {

    ScanReport scanRepository(Long repositoryId);

    void processNew(Path filePath, Long repoId) throws Exception;

    void processChange(Path filePath, Long repoId) throws Exception;

    void markDeleted(Long fileId) throws Exception;
}