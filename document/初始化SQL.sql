CREATE TABLE ex_user
(
    user_id         VARCHAR(64)                         NOT NULL PRIMARY KEY COMMENT '用户主键ID',
    username        VARCHAR(255)                        NOT NULL COMMENT '登录用户名，应建立唯一索引以防重复',
    password        VARCHAR(255)                        NOT NULL COMMENT '加密后的用户密码，严禁明文存储',
    nickname        VARCHAR(255)                        NOT NULL COMMENT '用户昵称，用于界面显示',
    email           VARCHAR(255)                        NULL COMMENT '用户绑定的个人电子邮箱地址（非发件邮箱）',
    email_server_id VARCHAR(255)                        NULL COMMENT '默认发件邮箱服务ID, 关联 ex_smtp_server 表的主键',
    email_password  VARCHAR(255)                        NULL COMMENT '加密后的发件邮箱授权码/密码',
    status          TINYINT   DEFAULT 1                 NOT NULL COMMENT '用户账户状态, 0:禁用, 1:启用',
    is_admin        TINYINT   DEFAULT 0                 NOT NULL COMMENT '是否为管理员, 0:普通用户, 1:管理员',
    last_login_time TIMESTAMP                           NULL COMMENT '用户最后一次成功登录的时间',
    created_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL COMMENT '记录创建时间',
    updated_at      TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
    email_status    TINYINT   DEFAULT 0                 NOT NULL COMMENT '发件邮箱配置状态, 0:未配置, 1:验证成功, 2:验证失败'
)
    COMMENT '用户表，存储系统用户的基本信息、登录凭证和发件配置';

CREATE TABLE ex_send_record
(
    id                 VARCHAR(64)                         NOT NULL COMMENT '主键ID'
        PRIMARY KEY,
    user_id            BIGINT                              NOT NULL COMMENT '发送用户ID，关联到用户表的主键',
    send_email         VARCHAR(255)                        NOT NULL COMMENT '发件人的电子邮箱地址',
    receive_emails     VARCHAR(255)                        NULL COMMENT '收件人的电子邮箱地址，多个地址时可能用特定分隔符隔开',
    receive_machine_id VARCHAR(255)                        NULL COMMENT '接收设备的ID，用于非邮件方式的推送（如WebSocket客户端标识）',
    receive_mode       VARCHAR(50)                         NULL COMMENT '接收模式/方式（如：email, client等）',
    subject            VARCHAR(255)                        NOT NULL COMMENT '消息或邮件的主题',
    fail_msg           VARCHAR(255)                        NULL COMMENT '发送失败时的错误信息记录',
    created_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL COMMENT '记录创建时间',
    updated_at         TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
    channel_code       VARCHAR(16)                         NULL COMMENT '通道代码，用于区分不同的发送通道或业务场景',
    group_name         VARCHAR(50)                         NULL COMMENT '接收组名称，当按分组发送时记录'
)
    COMMENT '发送记录表，用于存储每一次消息或邮件的发送详情';


CREATE TABLE ex_smtp_server
(
    email_server_id BIGINT AUTO_INCREMENT COMMENT 'SMTP服务主键ID'
        PRIMARY KEY,
    server_name     VARCHAR(255)         NOT NULL COMMENT '服务名称，用于前端显示和区分 (例如: 网易邮箱, QQ邮箱)',
    server_address  VARCHAR(255)         NOT NULL COMMENT 'SMTP服务器地址 (例如: smtp.163.com)',
    server_port     INT                  NOT NULL COMMENT 'SMTP服务器端口 (例如: 465)',
    use_ssl         TINYINT(1) DEFAULT 1 NOT NULL COMMENT '是否使用SSL/TLS加密, 1:是, 0:否',
    status          TINYINT(1) DEFAULT 1 NOT NULL COMMENT '该配置的状态, 1:启用, 0:禁用'
)
    COMMENT 'SMTP服务器配置表，存储可用的邮件服务器信息';


CREATE TABLE ex_receive_group
(
    group_id       VARCHAR(32)                          NOT NULL COMMENT '分组主键ID，建议使用UUID或自定义生成'
        PRIMARY KEY,
    user_id        BIGINT                               NOT NULL COMMENT '所属用户的ID，关联 ex_user 表的主键',
    channel_code   VARCHAR(64)                          NOT NULL COMMENT '通道代码，与发送逻辑关联，用于标识特定业务或来源的唯一代码',
    receive_mode   VARCHAR(50)                          NULL COMMENT '接收模式/方式 (例如: email, client)',
    name           VARCHAR(255)                         NOT NULL COMMENT '分组名称，方便用户识别和管理',
    status         TINYINT(1) DEFAULT 1                 NOT NULL COMMENT '分组状态, 1:启用, 0:禁用',
    created_at     TIMESTAMP  DEFAULT CURRENT_TIMESTAMP NULL COMMENT '记录创建时间',
    updated_at     TIMESTAMP  DEFAULT CURRENT_TIMESTAMP NULL ON UPDATE CURRENT_TIMESTAMP COMMENT '记录最后更新时间',
    receive_emails JSON                                 NULL COMMENT '接收邮箱列表，以JSON数组格式存储，例如：["a@example.com", "b@example.com"]',
    description    VARCHAR(50)                          NULL COMMENT '对该接收组的简短描述信息'
)
    COMMENT '接收组表，用于定义一组固定的接收者（如邮件列表），方便按组发送消息';

-- 默认用户 用户名：super  密码：super@ex
INSERT INTO ex_user (user_id, nickname, email, email_server_id, email_password, status, is_admin, last_login_time, created_at, updated_at, username, password, email_status) VALUES ('1', 'super', '', '1', 'dbumpngkusedbibe', 1, 1, '2025-06-21 07:23:14', '2024-11-18 19:02:30', '2025-06-21 00:58:01', 'super', '$2a$10$Hp9G2cpmo9yOBt0Jt8fWEe4uN55FaHNLAGfElQJjn91rBvjl5kARO', 1);

INSERT INTO ex_smtp_server (email_server_id, server_name, server_address, server_port, use_ssl, status) VALUES (1, 'QQ邮箱', 'smtp.qq.com', 465, 1, 1);
INSERT INTO ex_smtp_server (email_server_id, server_name, server_address, server_port, use_ssl, status) VALUES (2, 'Gmail', 'smtp.gmail.com', 465, 1, 1);
INSERT INTO ex_smtp_server (email_server_id, server_name, server_address, server_port, use_ssl, status) VALUES (3, 'Outlook', 'smtp.office365.com', 587, 1, 1);
INSERT INTO ex_smtp_server (email_server_id, server_name, server_address, server_port, use_ssl, status) VALUES (4, '阿里云邮箱', 'smtp.aliyun.com', 465, 1, 1);
INSERT INTO ex_smtp_server (email_server_id, server_name, server_address, server_port, use_ssl, status) VALUES (5, '腾讯企业邮箱', 'smtp.exmail.qq.com', 465, 1, 1);
INSERT INTO ex_smtp_server (email_server_id, server_name, server_address, server_port, use_ssl, status) VALUES (6, '网易企业邮箱', 'smtp.mxhichina.com', 465, 1, 1);
INSERT INTO ex_smtp_server (email_server_id, server_name, server_address, server_port, use_ssl, status) VALUES (7, '新浪邮箱', 'smtp.sina.com', 465, 1, 1);
INSERT INTO ex_smtp_server (email_server_id, server_name, server_address, server_port, use_ssl, status) VALUES (8, '搜狐邮箱', 'smtp.sohu.com', 465, 1, 1);
INSERT INTO ex_smtp_server (email_server_id, server_name, server_address, server_port, use_ssl, status) VALUES (9, '网易邮箱', 'smtp.163.com', 465, 1, 1);
