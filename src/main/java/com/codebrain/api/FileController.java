package com.codebrain.api;

import com.codebrain.common.Result;
import com.codebrain.domain.dto.FileSaveDTO;
import com.codebrain.domain.entity.File;
import com.codebrain.service.FileService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/file")
public class FileController {

    @Autowired
    private FileService fileService;

    @PostMapping
    public Result<File> create(@Valid @RequestBody FileSaveDTO dto) {
        File file = fileService.createFile(dto);
        return Result.success(file);
    }
}