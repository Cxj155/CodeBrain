package com.codebrain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codebrain.domain.entity.Tag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TagMapper extends BaseMapper<Tag> {
}