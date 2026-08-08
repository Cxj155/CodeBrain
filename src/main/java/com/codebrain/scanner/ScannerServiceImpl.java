package com.codebrain.scanner;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.codebrain.parse.CachingChunker;
import com.codebrain.parse.ChunkService;
import com.codebrain.parse.CodeParserFactory;
import com.codebrain.domain.entity.File;
import com.codebrain.domain.entity.Chunk;
import com.codebrain.domain.entity.Repository;
import com.codebrain.mapper.FileMapper;
import com.codebrain.mapper.ChunkMapper;
import com.codebrain.mapper.RepositoryMapper;
import com.codebrain.embedding.EmbeddingService;
import com.codebrain.search.ChunkIndexService;
import com.codebrain.util.FileUtil;
import com.codebrain.util.IgnoreRules;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Service
public class ScannerServiceImpl implements ScannerService {
    private static final Logger log = LoggerFactory.getLogger(ScannerServiceImpl.class);
    private final RepositoryMapper repositoryMapper;
    private final FileMapper fileMapper;
    private final ChunkMapper chunkMapper;
    private final FileFailedService fileFailedService;
    private final DiffDetector diffDetector;
    private final FileUtil fileUtil;
    private final IgnoreRules ignoreRules;
    private final CodeParserFactory parserFactory;
    private final EmbeddingService embeddingService;
    private final ChunkService chunkService;
    private final ChunkIndexService chunkIndexService;
    private final CachingChunker cachingChunker;

    public ScannerServiceImpl(RepositoryMapper repositoryMapper,
                              FileMapper fileMapper,
                              ChunkMapper chunkMapper,
                              FileFailedService fileFailedService,
                              DiffDetector diffDetector,
                              FileUtil fileUtil,
                              IgnoreRules ignoreRules,
                              CodeParserFactory parserFactory,
                              EmbeddingService embeddingService,
                              ChunkService chunkService,
                              ChunkIndexService chunkIndexService,
                              CachingChunker cachingChunker) {
        this.repositoryMapper = repositoryMapper;
        this.fileMapper = fileMapper;
        this.chunkMapper = chunkMapper;
        this.fileFailedService = fileFailedService;
        this.diffDetector = diffDetector;
        this.fileUtil = fileUtil;
        this.ignoreRules = ignoreRules;
        this.parserFactory = parserFactory;
        this.embeddingService = embeddingService;
        this.chunkService = chunkService;
        this.chunkIndexService = chunkIndexService;
        this.cachingChunker = cachingChunker;
    }

    private ScanReport buildEmptyReport() {
        ScanReport report = new ScanReport();
        report.setInserted(0);
        report.setUpdated(0);
        report.setDeleted(0);
        report.setFailed(1);
        return report;
    }

    @Override
    public ScanReport scanRepository(Long repositoryId) {
        int inserted = 0;
        int updated = 0;
        int deleted = 0;
        int failed = 0;
        log.info("======== 开始扫描仓库，repositoryId:{} ========", repositoryId);
        Repository repo;
        try {
            repo = repositoryMapper.selectById(repositoryId);
            if (repo == null) {
                log.warn("仓库不存在,repositoryId={}", repositoryId);
                return buildEmptyReport();
            }

            Path rootPath = Paths.get(repo.getLocalPath());
            log.info("rootPath = {}", rootPath.toAbsolutePath());
            log.info("目录存在={}, 是否文件夹={}", Files.exists(rootPath), Files.isDirectory(rootPath));
            ignoreRules.loadGitIgnore(rootPath);

            Map<String, String> currentFileMap = new HashMap<>();
            log.info("开始递归遍历目录");
            try (Stream<Path> walkStream = Files.walk(rootPath)) {
                walkStream.filter(Files::isRegularFile)
                        .filter(FileUtil::isCodeFile)
                        .filter(path -> !ignoreRules.isIgnored(path, rootPath))
                        .forEach(path -> {
                            try {
                                String sha = FileUtil.calcSha256(path);
                                String stdPath = path.toAbsolutePath().toString();
                                currentFileMap.put(stdPath, sha);
                                log.info("扫描收集文件:{} | sha:{}", stdPath, sha);
                            } catch (Exception e) {
                                log.error("计算文件sha失败 path={}", path, e);
                            }
                        });
            }

            DiffResult diffResult = diffDetector.detect(repositoryId, currentFileMap);
            log.info("【Diff输出】磁盘扫描到的文件总数:{}", currentFileMap.size());
            log.info("【Diff输出】待新增文件数量:{}", diffResult.getNewFileList().size());
            log.info("【Diff输出】待变更文件数量:{}", diffResult.getChangedFileList().size());
            log.info("【Diff输出】待删除文件数量:{}", diffResult.getDeletedFileIdList().size());
            log.info("【Diff输出】newFileList内容:{}", diffResult.getNewFileList());

            deleted = diffResult.getDeletedFileIdList().size();
            for (String stdPath : diffResult.getNewFileList()) {
                Path path = Paths.get(stdPath);
                try {
                    log.info("=====>> 准备调用processNew，路径：{}", path);
                    processNew(path, repositoryId);
                    inserted++;
                    log.info("=====<< processNew执行完成，inserted计数={}", inserted);
                } catch (Exception e) {
                    failed++;
                    log.error("新增文件扫描失败 path={}", path, e);
                }
            }

            for (String stdPath : diffResult.getChangedFileList()) {
                Path path = Paths.get(stdPath);
                try {
                    processChange(path, repositoryId);
                    updated++;
                } catch (Exception e) {
                    failed++;
                    log.error("变更文件扫描失败 path={}", path, e);
                }
            }

            for (Long delId : diffResult.getDeletedFileIdList()) {
                try {
                    markDeleted(delId);
                } catch (Exception e) {
                    failed++;
                    log.error("标记文件删除失败 fileId={}", delId, e);
                }
            }
        } catch (Exception e) {
            failed++;
            log.error("仓库扫描整体异常 repositoryId={}", repositoryId, e);
        }
        ScanReport report = new ScanReport();
        report.setInserted(inserted);
        report.setUpdated(updated);
        report.setDeleted(deleted);
        report.setFailed(failed);
        log.info("扫描结束 report inserted={},updated={},deleted={},failed={}",
                report.getInserted(),report.getUpdated(),report.getDeleted(),report.getFailed());
        return report;
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void processNew(Path filePath, Long repoId) throws Exception {
        log.info("=====1【processNew入参】filePath={}, repoId={}", filePath, repoId);
        cachingChunker.clearCache(filePath);
        log.info("=====2 clearCache执行完毕");

        LambdaQueryWrapper<File> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(File::getRepositoryId, repoId)
                .eq(File::getPath, filePath.toAbsolutePath().toString());
        File exist = fileMapper.selectOne(existWrapper);
        log.info("=====3 查询旧记录完成，exist={}", exist);

        if (exist != null) {
            log.warn("本仓库内文件已存在数据库，不再执行新增 path={}", filePath);
            return;
        }
        log.info("=====4 数据库无旧记录，继续新增逻辑");

        String sha256;
        try {
            sha256 = FileUtil.calcSha256(filePath);
            log.info("=====5 SHA计算完成，sha256={}", sha256);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("SHA算法异常", e);
        }

        String language = parserFactory.detectLanguage(filePath);
        log.info("=====6 detectLanguage语言识别完成，language={}", language);

        long size = Files.size(filePath);
        log.info("=====7 获取文件size完成 size={}", size);

        long mtime = Files.getLastModifiedTime(filePath).toMillis();
        log.info("=====8 获取mtime完成 mtime={}", mtime);

        File fileEntity = new File();
        fileEntity.setRepositoryId(repoId);
        fileEntity.setPath(filePath.toAbsolutePath().toString());
        fileEntity.setSha256(sha256);
        fileEntity.setLanguage(language);
        fileEntity.setSizeBytes(size);
        fileEntity.setMtime(mtime);
        fileEntity.setStatus("NORMAL");
        log.info("=====9 File实体组装完毕 path={}", fileEntity.getPath());

        log.info("准备入库文件：{}", fileEntity.getPath());
        int insertRow = fileMapper.insert(fileEntity);
        log.info("=====10 入库影响行数：{}，mybatisPlus回写ID={}", insertRow, fileEntity.getId());
        Long fileId = fileEntity.getId();
        chunkIndexService.indexFile(filePath, repoId, fileId, language);
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void processChange(Path filePath, Long repoId) throws Exception {
        cachingChunker.clearCache(filePath);
        LambdaQueryWrapper<File> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(File::getRepositoryId, repoId).eq(File::getPath, filePath.toAbsolutePath().toString());
        File existFile = fileMapper.selectOne(queryWrapper);
        if (existFile == null) {
            log.warn("变更文件数据库找不到记录 path={}", filePath);
            return;
        }
        Long fileId = existFile.getId();
        String newSha;
        try {
            newSha = FileUtil.calcSha256(filePath);
        } catch (NoSuchAlgorithmException e) {
            throw new Exception("SHA算法异常", e);
        }
        String language = parserFactory.detectLanguage(filePath);
        long newMtime = Files.getLastModifiedTime(filePath).toMillis();
        LambdaUpdateWrapper<File> updateFileWrapper = new LambdaUpdateWrapper<>();
        updateFileWrapper.eq(File::getId, fileId)
                .set(File::getSha256, newSha)
                .set(File::getLanguage, language)
                .set(File::getMtime, newMtime);
        fileMapper.update(null, updateFileWrapper);
        chunkIndexService.reindexFile(filePath, repoId, fileId, language);
    }

    @Override
    @Transactional(propagation = org.springframework.transaction.annotation.Propagation.REQUIRES_NEW, noRollbackFor = Exception.class)
    public void markDeleted(Long fileId) {
        LambdaQueryWrapper<File> fileQw = new LambdaQueryWrapper<>();
        fileQw.eq(File::getId, fileId);
        File file = fileMapper.selectOne(fileQw);
        if(file != null){
            Path filePath = Paths.get(file.getPath());
            cachingChunker.clearCache(filePath);
        }
        File updateEntity = new File();
        updateEntity.setStatus("DELETED");
        LambdaUpdateWrapper<File> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(File::getId, fileId);
        fileMapper.update(updateEntity, updateWrapper);
        LambdaQueryWrapper<Chunk> chunkDelWrapper = new LambdaQueryWrapper<>();
        chunkDelWrapper.eq(Chunk::getFileId, fileId);
        chunkMapper.delete(chunkDelWrapper);
        try {
            chunkIndexService.deleteByFileId(fileId);
        } catch (Exception e) {
            log.error("删除文件ES文档失败 fileId={}", fileId, e);
            fileFailedService.recordFail(fileId, "ES删除分片失败", e.getMessage());
        }
    }
}