package com.codebrain.domain.dto;
import lombok.Data;

@Data
public class RepositorySaveDTO {
    private String name;
    private String localPath;
    private String description;
}