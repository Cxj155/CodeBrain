package com.codebrain.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codebrain.domain.entity.FileTask;
import com.codebrain.mapper.FileTaskMapper;
import org.springframework.stereotype.Service;

@Service
public class FileTaskServiceImpl extends ServiceImpl<FileTaskMapper, FileTask> implements FileTaskService {
}