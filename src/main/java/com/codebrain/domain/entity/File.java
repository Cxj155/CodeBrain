package com.codebrain.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file")
public class File {
    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("repo_id")
    private Long repositoryId;
    private String path;
    private String sha256;
    private Long mtime;
    private String language;
    private Long sizeBytes;
    private String status;
    private Integer indexedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;


}