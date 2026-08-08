package com.codebrain.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.codebrain.domain.dto.TagSaveDTO;
import com.codebrain.domain.dto.TagUpdateDTO;
import com.codebrain.domain.entity.Tag;
import com.codebrain.mapper.TagMapper;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public Tag create(TagSaveDTO dto) {
        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setDescription(dto.getDescription());
        save(tag);
        return tag;
    }

    @Override
    public Tag getById(Long id) {
        return super.getById(id);
    }

    @Override
    public Tag update(Long id, TagUpdateDTO dto) {
        Tag tag = getById(id);
        tag.setName(dto.getName());
        tag.setDescription(dto.getDescription());
        updateById(tag);
        return tag;
    }

    @Override
    public void delete(Long id) {
        removeById(id);
    }

    @Override
    public List<Tag> listAll() {
        return list();
    }
}