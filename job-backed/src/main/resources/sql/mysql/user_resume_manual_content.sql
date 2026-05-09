CREATE TABLE IF NOT EXISTS user_resume_manual_content (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键',
    user_id BIGINT NOT NULL COMMENT '用户ID',
    resume_id VARCHAR(64) NOT NULL COMMENT '简历业务唯一标识',
    content_json JSON DEFAULT NULL COMMENT '人工维护内容',
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态',
    create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    del_flag TINYINT NOT NULL DEFAULT 0 COMMENT '删除标识 0-未删除 1-已删除',
    UNIQUE KEY uk_resume_id (resume_id),
    KEY idx_user_id (user_id),
    KEY idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户简历人工维护内容表';
