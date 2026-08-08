package com.codebrain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codebrain.domain.entity.Repository;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RepositoryMapper extends BaseMapper<Repository> {
}