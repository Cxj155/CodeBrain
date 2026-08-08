package com.codebrain.api;

import com.codebrain.common.Result;
import com.codebrain.domain.dto.RepositorySaveDTO;
import com.codebrain.domain.dto.RepositoryUpdateDTO;
import com.codebrain.domain.entity.Repository;
import com.codebrain.service.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/repository")
public class RepositoryController {

    @Autowired
    private RepositoryService repositoryService;

    @PostMapping
    public Result<Repository> create(@RequestBody RepositorySaveDTO dto) {
        Repository data = repositoryService.create(dto);
        return Result.success(data);
    }

    @GetMapping("/{id}")
    public Result<Repository> getOne(@PathVariable("id") Long id) {
        Repository data = repositoryService.getById(id);
        return Result.success(data);
    }

    @PutMapping("/{id}")
    public Result<Repository> update(@PathVariable("id") Long id, @RequestBody RepositoryUpdateDTO dto) {
        Repository data = repositoryService.update(id, dto);
        return Result.success(data);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable("id") Long id) {
        repositoryService.delete(id);
        return Result.success(null);
    }

    @GetMapping("/list")
    public Result<List<Repository>> list() {
        List<Repository> list = repositoryService.listAll();
        return Result.success(list);
    }
}