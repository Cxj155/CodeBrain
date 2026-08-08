package com.codebrain.api;

import com.codebrain.common.Result;
import com.codebrain.domain.dto.TagSaveDTO;
import com.codebrain.domain.dto.TagUpdateDTO;
import com.codebrain.domain.entity.Tag;
import com.codebrain.service.TagService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tag")
public class TagController {

    @Autowired
    private TagService tagService;

    @PostMapping
    public Result<Tag> create(@Valid @RequestBody TagSaveDTO dto) {
        Tag tag = tagService.create(dto);
        return Result.success(tag);
    }

    @GetMapping("/{id}")
    public Result<Tag> getById(@PathVariable Long id) {
        Tag tag = tagService.getById(id);
        return Result.success(tag);
    }

    @PutMapping("/{id}")
    public Result<Tag> update(@PathVariable Long id, @Valid @RequestBody TagUpdateDTO dto) {
        Tag tag = tagService.update(id, dto);
        return Result.success(tag);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<List<Tag>> listAll() {
        List<Tag> list = tagService.listAll();
        return Result.success(list);
    }
}