package com.codebrain.api;

import com.codebrain.common.Result;
import com.codebrain.domain.dto.FileTagBindDTO;
import com.codebrain.domain.entity.FileTagRel;
import com.codebrain.service.FileTagRelService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/file-tag")
public class FileTagRelController {

    @Autowired
    private FileTagRelService fileTagRelService;

    @PostMapping
    public Result<FileTagRel> bind(@Valid @RequestBody FileTagBindDTO dto) {
        FileTagRel rel = fileTagRelService.bind(dto);
        return Result.success(rel);
    }

    @GetMapping("/file/{fileId}/tag-links")
    public Result<List<FileTagRel>> getFileTagList(@PathVariable Long fileId) {
        List<FileTagRel> list = fileTagRelService.listByFileId(fileId);
        return Result.success(list);
    }
}