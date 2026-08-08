package com.codebrain.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.codebrain.domain.entity.Chunk;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import java.util.List;

public interface ChunkMapper extends BaseMapper<Chunk> {
    @Insert("<script>" +
            "INSERT INTO chunk(file_id, language, kind, name, start_line, end_line, content, content_hash) " +
            "VALUES " +
            "<foreach collection='list' item='item' separator=','>" +
            "(#{item.fileId},#{item.language},#{item.kind},#{item.name},#{item.startLine},#{item.endLine},#{item.content},#{item.contentHash})" +
            "</foreach> " +
            "ON DUPLICATE KEY UPDATE " +
            "language=VALUES(language), kind=VALUES(kind), name=VALUES(name), start_line=VALUES(start_line), " +
            "end_line=VALUES(end_line), content=VALUES(content), content_hash=VALUES(content_hash)" +
            "</script>")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int batchInsertOrUpdate(@Param("list") List<Chunk> chunkList);
}
