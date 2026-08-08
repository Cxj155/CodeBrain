package com.codebrain.service;
import com.codebrain.domain.dto.RepositorySaveDTO;
import com.codebrain.domain.dto.RepositoryUpdateDTO;
import com.codebrain.domain.entity.Repository;
import java.util.List;

public interface RepositoryService {
    Repository create(RepositorySaveDTO dto);
    Repository getById(Long id);
    Repository update(Long id, RepositoryUpdateDTO dto);
    void delete(Long id);
    List<Repository> listAll();
}