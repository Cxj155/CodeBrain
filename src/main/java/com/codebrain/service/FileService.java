package com.codebrain.service;

import com.codebrain.domain.dto.FileSaveDTO;
import com.codebrain.domain.dto.FileUpdateDTO;
import com.codebrain.domain.entity.File;
import com.codebrain.domain.vo.FileTagVO;
import java.util.List;

public interface FileService {
    File createFile(FileSaveDTO dto);
    File getById(Long id);
    File update(Long id, FileUpdateDTO dto);
    void delete(Long id);
    List<File> listAll();

    FileTagVO getFileWithTag(Long fileId);
}