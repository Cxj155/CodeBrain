package com.codebrain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codebrain.common.BusinessException;
import com.codebrain.domain.dto.FileTagBindDTO;
import com.codebrain.domain.entity.FileTagRel;
import com.codebrain.mapper.FileTagRelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.annotation.Resource;

import java.util.List;

@Service
public class FileTagRelServiceImpl extends ServiceImpl<FileTagRelMapper, FileTagRel>
        implements FileTagRelService {

    @Resource
    private FileTagRelMapper fileTagRelMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileTagRel bind(FileTagBindDTO dto) {
        Long fileId = dto.getFileId();
        Long tagId = dto.getTagId();

        LambdaQueryWrapper<FileTagRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileTagRel::getFileId, fileId)
                .eq(FileTagRel::getTagId, tagId);

        long count = fileTagRelMapper.selectCount(wrapper);

        if (count > 0) {
            throw new BusinessException(40901, "标签已绑定该文件");
        }

        FileTagRel rel = new FileTagRel();
        rel.setFileId(fileId);
        rel.setTagId(tagId);
        save(rel);
        return rel;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FileTagRel bindWithRollbackTest(FileTagBindDTO dto) {
        FileTagRel rel = bind(dto);
        int err = 1 / 0;
        return rel;
    }

    @Override
    public List<FileTagRel> listByFileId(Long fileId) {
        LambdaQueryWrapper<FileTagRel> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileTagRel::getFileId, fileId);
        return list(wrapper);
    }
}