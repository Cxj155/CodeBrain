DROP TABLE IF EXISTS file_tag;
DROP TABLE IF EXISTS tag;
DROP TABLE IF EXISTS index_task;
DROP TABLE IF EXISTS `file`;
DROP TABLE IF EXISTS repository;

CREATE TABLE repository (
                            id              BIGINT         NOT NULL AUTO_INCREMENT,
                            name            VARCHAR(128)   NOT NULL,
                            local_path      VARCHAR(1024)  NOT NULL
                                COMMENT '本地代码目录绝对路径（Linux/Mac 例：/Users/me/code/my-project；Windows 例：D:/code/my-project）。最长 1024 字符。',
                            description     VARCHAR(512),
                            status          VARCHAR(16)    NOT NULL DEFAULT 'ACTIVE'
                                COMMENT 'ACTIVE / DISABLED / INDEXING / FAILED',
                            last_indexed_at INT UNSIGNED   DEFAULT NULL
                    COMMENT '最近一次索引完成时间，epoch 秒（10 位）',
                            created_at      INT UNSIGNED   NOT NULL DEFAULT 0
                    COMMENT '创建时间，epoch 秒（10 位）；应用层写入 UNIX_TIMESTAMP()',
                            updated_at      INT UNSIGNED   NOT NULL DEFAULT 0
                    COMMENT '更新时间，epoch 秒（10 位）；应用层在每次 UPDATE 写入 UNIX_TIMESTAMP()',
                            PRIMARY KEY (id),
                            UNIQUE KEY uk_repo_local_path (local_path(700)),
                            KEY idx_repo_status (status),
                            KEY idx_repo_last_indexed (last_indexed_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码仓库注册表（v1.9 起只接本地目录，local_path 唯一）';

CREATE TABLE `file` (
                        id              BIGINT         NOT NULL AUTO_INCREMENT,
                        repository_id   BIGINT         NOT NULL,
                        path            VARCHAR(512)   NOT NULL
                            COMMENT '相对于 repository.local_path 的文件路径（如 src/main/java/Foo.java）',
                        sha256          CHAR(64)       NOT NULL,
                        mtime           BIGINT         NOT NULL DEFAULT 0
                            COMMENT '文件最后修改时间（epoch millis）；与 sha256 共同作为增量检测双信号（v1.9 起替代 Git commit diff）',
                        language        VARCHAR(16),
                        size_bytes      BIGINT,
                        status          VARCHAR(16)    NOT NULL DEFAULT 'PENDING'
                            COMMENT 'PENDING / INDEXED / FAILED',
                        indexed_at      INT UNSIGNED   DEFAULT NULL
                    COMMENT '索引完成时间，epoch 秒（10 位）',
                        created_at      INT UNSIGNED   NOT NULL DEFAULT 0
                    COMMENT '创建时间，epoch 秒（10 位）；应用层写入 UNIX_TIMESTAMP()',
                        updated_at      INT UNSIGNED   NOT NULL DEFAULT 0
                    COMMENT '更新时间，epoch 秒（10 位）；应用层在每次 UPDATE 写入 UNIX_TIMESTAMP()',
                        PRIMARY KEY (id),
                        UNIQUE KEY uk_repo_path (repository_id, path),
                        KEY idx_file_sha256 (sha256),
                        KEY idx_file_repo_status (repository_id, status),
                        CONSTRAINT fk_file_repo FOREIGN KEY (repository_id)
                            REFERENCES repository(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='代码文件表';

CREATE TABLE index_task (
                            id              BIGINT         NOT NULL AUTO_INCREMENT,
                            repository_id   BIGINT         NOT NULL,
                            status          VARCHAR(16)    NOT NULL DEFAULT 'PENDING'
                                COMMENT 'PENDING / RUNNING / SUCCESS / FAILED',
                            trigger_type    VARCHAR(16)    NOT NULL DEFAULT 'MANUAL'
                                COMMENT 'MANUAL（手动触发）/ SCHEDULED（定时扫描）/ WATCHER（WatchService 触发）',
                            total_files     INT            NOT NULL DEFAULT 0,
                            processed_files INT            NOT NULL DEFAULT 0,
                            error_message   TEXT,
                            started_at      INT UNSIGNED   DEFAULT NULL
                    COMMENT '任务开始时间，epoch 秒（10 位）',
                            finished_at     INT UNSIGNED   DEFAULT NULL
                    COMMENT '任务结束时间，epoch 秒（10 位）',
                            created_at      INT UNSIGNED   NOT NULL DEFAULT 0
                    COMMENT '创建时间，epoch 秒（10 位）；应用层写入 UNIX_TIMESTAMP()',
                            updated_at      INT UNSIGNED   NOT NULL DEFAULT 0
                    COMMENT '更新时间，epoch 秒（10 位）；应用层在每次 UPDATE 写入 UNIX_TIMESTAMP()',
                            PRIMARY KEY (id),
                            KEY idx_task_repo_created (repository_id, created_at),
                            KEY idx_task_status (status),
                            CONSTRAINT fk_task_repo FOREIGN KEY (repository_id)
                                REFERENCES repository(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='索引任务表';

CREATE TABLE tag (
                     id          BIGINT         NOT NULL AUTO_INCREMENT,
                     name        VARCHAR(64)    NOT NULL,
                     description VARCHAR(256),
                     created_at  INT UNSIGNED   NOT NULL DEFAULT 0
                COMMENT '创建时间，epoch 秒（10 位）；应用层写入 UNIX_TIMESTAMP()',
                     updated_at  INT UNSIGNED   NOT NULL DEFAULT 0
                COMMENT '更新时间，epoch 秒（10 位）；应用层在每次 UPDATE 写入 UNIX_TIMESTAMP()',
                     PRIMARY KEY (id),
                     UNIQUE KEY uk_tag_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='标签表';

CREATE TABLE file_tag (
                          file_id    BIGINT         NOT NULL,
                          tag_id     BIGINT         NOT NULL,
                          created_at INT UNSIGNED   NOT NULL DEFAULT 0
               COMMENT '创建时间，epoch 秒（10 位）；应用层写入 UNIX_TIMESTAMP()',
                          PRIMARY KEY (file_id, tag_id),
                          KEY idx_filetag_tag (tag_id),
                          CONSTRAINT fk_filetag_file FOREIGN KEY (file_id)
                              REFERENCES file(id) ON DELETE CASCADE,
                          CONSTRAINT fk_filetag_tag  FOREIGN KEY (tag_id)
                              REFERENCES tag(id)  ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='文件-标签关联表';