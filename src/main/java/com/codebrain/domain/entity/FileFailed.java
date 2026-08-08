package com.codebrain.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_failed")
public class FileFailed {
    @TableId(type = IdType.AUTO)
    private Long id;

    private Long fileId;

    private String reason;

    private Integer retryCount;

    private LocalDateTime lastFailedAt;

    private String errorMessage;
}
