package com.codebrain.scanner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.codebrain.domain.entity.FileFailed;
import com.codebrain.mapper.FileFailedMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import java.time.LocalDateTime;

@Service
public class FileFailedServiceImpl implements FileFailedService {

    @Autowired
    private FileFailedMapper fileFailedMapper;

    @Override
    public boolean shouldSkip(Long fileId) {
        LambdaQueryWrapper<FileFailed> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FileFailed::getFileId, fileId);
        FileFailed record = fileFailedMapper.selectOne(wrapper);
        if (record == null) {
            return false;
        }
        return record.getRetryCount() >= 3;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void recordFail(Long fileId, String reason, String errorMsg) {
        LambdaQueryWrapper<FileFailed> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(FileFailed::getFileId, fileId);
        FileFailed existRecord = fileFailedMapper.selectOne(queryWrapper);
        LocalDateTime now = LocalDateTime.now();

        if (existRecord == null) {
            FileFailed newRecord = new FileFailed();
            newRecord.setFileId(fileId);
            newRecord.setReason(reason);
            newRecord.setErrorMessage(errorMsg);
            newRecord.setLastFailedAt(now);
            if ("SYNTAX".equals(reason)) {
                newRecord.setRetryCount(3);
            } else {
                newRecord.setRetryCount(1);
            }
            fileFailedMapper.insert(newRecord);
        } else {
            LambdaUpdateWrapper<FileFailed> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(FileFailed::getId, existRecord.getId());
            updateWrapper.set(FileFailed::getErrorMessage, errorMsg);
            updateWrapper.set(FileFailed::getLastFailedAt, now);

            if ("SYNTAX".equals(reason)) {
                updateWrapper.set(FileFailed::getRetryCount, 3);
                updateWrapper.set(FileFailed::getReason, reason);
            } else {
                updateWrapper.setSql("retry_count = retry_count + 1");
            }
            fileFailedMapper.update(null, updateWrapper);
        }
    }
}

