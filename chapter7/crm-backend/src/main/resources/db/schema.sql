CREATE DATABASE IF NOT EXISTS crm DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE crm;

CREATE TABLE IF NOT EXISTS customer (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL COMMENT '客户名称',
    phone           VARCHAR(20) COMMENT '电话',
    email           VARCHAR(100) COMMENT '邮箱',
    company         VARCHAR(200) COMMENT '公司',
    address         VARCHAR(500) COMMENT '地址',
    source          VARCHAR(50) COMMENT '来源: REFERRAL/WEBSITE/AD/COLD_CALL/OTHER',
    level           VARCHAR(20) COMMENT '等级: VIP/IMPORTANT/NORMAL/POTENTIAL',
    industry        VARCHAR(100) COMMENT '行业',
    website         VARCHAR(200) COMMENT '网站',
    contact_person  VARCHAR(100) COMMENT '联系人',
    last_follow_up  DATETIME COMMENT '最后跟进时间',
    status          VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' COMMENT '状态: ACTIVE/INACTIVE/LOST',
    remark          TEXT COMMENT '备注',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';
