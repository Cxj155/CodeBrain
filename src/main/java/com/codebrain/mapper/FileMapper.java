package com.codebrain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codebrain.domain.entity.File;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface FileMapper extends BaseMapper<File> {
}