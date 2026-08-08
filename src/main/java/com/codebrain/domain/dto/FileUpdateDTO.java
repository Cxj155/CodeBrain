package com.codebrain.domain.dto;
import lombok.Data;

@Data
public class FileUpdateDTO {
    private String path;
    private String sha256;
    private Long mtime;
    private String language;
    private Long sizeBytes;
}