package com.codebrain.domain.vo;

import com.codebrain.domain.entity.Tag;
import lombok.Data;
import java.util.List;

@Data
public class FileTagVO {
    private Long id;
    private Long repositoryId;
    private String path;
    private String language;
    private String sha256;
    private Long sizeBytes;
    private List<Tag> tagList;
}