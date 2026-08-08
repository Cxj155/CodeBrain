package com.codebrain.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("file_tag_rel")
public class FileTagRel {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fileId;
    private Long tagId;
    private LocalDateTime createdAt;
}