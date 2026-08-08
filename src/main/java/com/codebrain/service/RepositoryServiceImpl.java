package com.codebrain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codebrain.domain.dto.RepositorySaveDTO;
import com.codebrain.domain.dto.RepositoryUpdateDTO;
import com.codebrain.domain.entity.File;
import com.codebrain.domain.entity.Repository;
import com.codebrain.mapper.FileMapper;
import com.codebrain.mapper.RepositoryMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RepositoryServiceImpl extends ServiceImpl<RepositoryMapper, Repository> implements RepositoryService {

    @Autowired
    private FileMapper fileMapper;

    @Override
    public Repository create(RepositorySaveDTO dto) {
        Repository repo = new Repository();
        BeanUtils.copyProperties(dto, repo);
        repo.setStatus("ACTIVE");
        save(repo);
        return repo;
    }

    @Override
    public Repository getById(Long id) {
        return super.getById(id);
    }

    @Override
    public Repository update(Long id, RepositoryUpdateDTO dto) {
        Repository repo = getById(id);
        if (repo == null) {
            throw new RuntimeException("仓库不存在");
        }
        BeanUtils.copyProperties(dto, repo);
        updateById(repo);
        return repo;
    }

    @Override
    public void delete(Long id) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getRepositoryId, id);
        Long count = fileMapper.selectCount(wrapper);
        if (count > 0) {
            throw new RuntimeException("仓库下存在文件，不能删除！");
        }
        removeById(id);
    }

    @Override
    public List<Repository> listAll() {
        return list();
    }
}