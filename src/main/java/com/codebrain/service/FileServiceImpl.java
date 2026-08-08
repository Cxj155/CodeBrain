package com.codebrain.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codebrain.domain.dto.FileSaveDTO;
import com.codebrain.domain.dto.FileUpdateDTO;
import com.codebrain.domain.entity.File;
import com.codebrain.domain.entity.FileTagRel;
import com.codebrain.domain.entity.Tag;
import com.codebrain.mapper.FileMapper;
import com.codebrain.mapper.FileTagRelMapper;
import com.codebrain.mapper.TagMapper;
import com.codebrain.domain.vo.FileTagVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FileServiceImpl extends ServiceImpl<FileMapper, File> implements FileService {

    @Autowired
    private FileTagRelMapper fileTagRelMapper;

    @Autowired
    private TagMapper tagMapper;

    @Override
    public File createFile(FileSaveDTO dto) {
        File entity = new File();
        BeanUtils.copyProperties(dto, entity);
        save(entity);
        return entity;
    }

    @Override
    public File getById(Long id) {
        return super.getById(id);
    }

    @Override
    public File update(Long id, FileUpdateDTO dto) {
        File file = getById(id);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }
        BeanUtils.copyProperties(dto, file);
        updateById(file);
        return file;
    }

    @Transactional
    @Override
    public void delete(Long id) {
        LambdaQueryWrapper<FileTagRel> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(FileTagRel::getFileId, id);
        fileTagRelMapper.delete(relWrapper);
        removeById(id);
    }

    @Override
    public List<File> listAll() {
        return list();
    }

    @Override
    public FileTagVO getFileWithTag(Long fileId) {
        File file = getById(fileId);
        if (file == null) {
            throw new RuntimeException("文件不存在");
        }

        LambdaQueryWrapper<FileTagRel> relWrapper = new LambdaQueryWrapper<>();
        relWrapper.eq(FileTagRel::getFileId, fileId);
        List<FileTagRel> relList = fileTagRelMapper.selectList(relWrapper);

        List<Long> tagIdList = relList.stream()
                .map(FileTagRel::getTagId)
                .collect(Collectors.toList());

        List<Tag> tagList = List.of();
        if (!tagIdList.isEmpty()) {
            tagList = tagMapper.selectBatchIds(tagIdList);
        }

        FileTagVO vo = new FileTagVO();
        BeanUtils.copyProperties(file, vo);
        vo.setTagList(tagList);
        return vo;
    }
}