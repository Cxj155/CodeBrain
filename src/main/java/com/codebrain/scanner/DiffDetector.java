package com.codebrain.scanner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.codebrain.domain.entity.File;
import com.codebrain.mapper.FileMapper;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DiffDetector {

    @Autowired
    private FileMapper fileMapper;

    public DiffResult detect(Long repositoryId, Map<String, String> currentFileMap) {
        LambdaQueryWrapper<File> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(File::getRepositoryId, repositoryId);
        wrapper.eq(File::getStatus, "NORMAL"); // 只查询正常未删除文件
        List<File> dbFileList = fileMapper.selectList(wrapper);

        Map<String, File> dbPathMap = new HashMap<>();
        for (File file : dbFileList) {
            String absolutePath = Paths.get(file.getPath()).toAbsolutePath().toString();
            dbPathMap.put(absolutePath, file);
        }

        DiffResult result = new DiffResult();
        for (Map.Entry<String, String> entry : currentFileMap.entrySet()) {
            String filePathStr = entry.getKey();
            String sha = entry.getValue();

            if (!dbPathMap.containsKey(filePathStr)) {
                result.getNewFileList().add(filePathStr);
            } else {
                File dbFile = dbPathMap.get(filePathStr);
                if (!sha.equals(dbFile.getSha256())) {
                    result.getChangedFileList().add(filePathStr);
                }
                dbPathMap.remove(filePathStr);
            }
        }

        List<Long> deleteIds = dbPathMap.values().stream()
                .map(File::getId)
                .collect(Collectors.toList());
        result.setDeletedFileIdList(deleteIds);

        return result;
    }
}