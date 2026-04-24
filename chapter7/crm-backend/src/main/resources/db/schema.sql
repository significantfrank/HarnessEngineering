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
    cc_sync_status  VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT 'center同步状态: PENDING/SYNCED/FAILED',
    id_type         VARCHAR(20) COMMENT '证件类型',
    id_number       VARCHAR(50) COMMENT '证件号码',
    remark          TEXT COMMENT '备注',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_id_number (id_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户表';

CREATE TABLE IF NOT EXISTS lead (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(100) NOT NULL COMMENT '线索名称',
    phone           VARCHAR(20) COMMENT '电话',
    email           VARCHAR(100) COMMENT '邮箱',
    company         VARCHAR(200) COMMENT '公司',
    source          VARCHAR(50) COMMENT '来源: REFERRAL/WEBSITE/AD/COLD_CALL/OTHER',
    status          VARCHAR(20) NOT NULL DEFAULT 'NEW' COMMENT '状态: NEW/CONTACTED/QUALIFIED/UNQUALIFIED/CONVERTED',
    customer_id     BIGINT COMMENT '转化后关联的客户ID',
    id_type         VARCHAR(20) COMMENT '证件类型',
    id_number       VARCHAR(50) COMMENT '证件号码',
    owner_name      VARCHAR(100) COMMENT '负责人',
    remark          TEXT COMMENT '备注',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='线索表';

CREATE TABLE IF NOT EXISTS opportunity (
    id                 BIGINT PRIMARY KEY AUTO_INCREMENT,
    title              VARCHAR(200) NOT NULL COMMENT '机会标题',
    customer_id        BIGINT NOT NULL COMMENT '所属客户ID',
    amount             DECIMAL(12,2) COMMENT '预计金额',
    stage              VARCHAR(20) NOT NULL DEFAULT 'PROSPECTING' COMMENT '阶段: PROSPECTING/QUALIFYING/PROPOSAL/NEGOTIATION/WON/LOST',
    probability        INT COMMENT '赢单概率(0-100)',
    expected_close_date DATE COMMENT '预计结单日期',
    lead_id            BIGINT COMMENT '来源线索ID',
    owner_name         VARCHAR(100) COMMENT '负责人',
    remark             TEXT COMMENT '备注',
    create_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time        DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='机会表';

CREATE TABLE IF NOT EXISTS `order` (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    order_no        VARCHAR(50) NOT NULL COMMENT '订单号',
    customer_id     BIGINT NOT NULL COMMENT '所属客户ID',
    opportunity_id  BIGINT NOT NULL COMMENT '所属机会ID',
    total_amount    DECIMAL(12,2) COMMENT '订单金额',
    status          VARCHAR(20) NOT NULL DEFAULT 'PENDING' COMMENT '状态: PENDING/CONFIRMED/PROCESSING/COMPLETED/CANCELLED',
    owner_name      VARCHAR(100) COMMENT '负责人',
    remark          TEXT COMMENT '备注',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_order_no (order_no)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='订单表';

CREATE TABLE IF NOT EXISTS customer_note (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id     BIGINT NOT NULL COMMENT '所属客户ID',
    category        VARCHAR(20) NOT NULL COMMENT '类型: PHONE_CALL/VISIT/EMAIL/WECHAT/OTHER',
    content         TEXT NOT NULL COMMENT '小记内容',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_customer_id (customer_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户小记表';

CREATE TABLE IF NOT EXISTS tag (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    name            VARCHAR(50) NOT NULL COMMENT '标签名称',
    color           VARCHAR(20) NOT NULL COMMENT '颜色(HEX)',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='标签表';

CREATE TABLE IF NOT EXISTS customer_tag (
    id              BIGINT PRIMARY KEY AUTO_INCREMENT,
    customer_id     BIGINT NOT NULL COMMENT '客户ID',
    tag_id          BIGINT NOT NULL COMMENT '标签ID',
    create_time     DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    UNIQUE KEY uk_customer_tag (customer_id, tag_id),
    INDEX idx_tag_id (tag_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='客户标签关联表';
