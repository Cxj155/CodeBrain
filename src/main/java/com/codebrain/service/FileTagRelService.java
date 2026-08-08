package com.codebrain.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.codebrain.domain.dto.FileTagBindDTO;
import com.codebrain.domain.entity.FileTagRel;

import java.util.List;

public interface FileTagRelService extends IService<FileTagRel> {
    FileTagRel bind(FileTagBindDTO dto);

    FileTagRel bindWithRollbackTest(FileTagBindDTO dto);

    List<FileTagRel> listByFileId(Long fileId);


}