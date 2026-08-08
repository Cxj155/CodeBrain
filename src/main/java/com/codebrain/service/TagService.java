package com.codebrain.service;

import com.codebrain.domain.dto.TagSaveDTO;
import com.codebrain.domain.dto.TagUpdateDTO;
import com.codebrain.domain.entity.Tag;
import java.util.List;

public interface TagService {
    Tag create(TagSaveDTO dto);
    Tag getById(Long id);
    Tag update(Long id, TagUpdateDTO dto);
    void delete(Long id);
    List<Tag> listAll();
}