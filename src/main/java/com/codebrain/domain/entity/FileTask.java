package com.codebrain.domain.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("file_task")
public class FileTask {
    @TableId(type = IdType.AUTO)
    private Long id;
    private Long fileId;
    private String taskStatus;
    private String vectorIndex;
    private Long createTime;
    private Long finishTime;
}