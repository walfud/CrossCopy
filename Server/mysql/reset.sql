# schema
DROP SCHEMA IF EXISTS 'cc';
CREATE SCHEMA 'cc';

# user
CREATE USER 'cc'@'%' IDENTIFIED BY '....';
GRANT ALL ON *.* TO 'cc'@'%';

# table
DROP TABLE IF EXISTS 'user';
CREATE TABLE 'user'
(
    id          CHAR(36) PRIMARY KEY,

    create_time CHAR(19),
    update_time CHAR(19)
);

DROP TABLE IF EXISTS 'thing';
CREATE TABLE 'thing'
(
    id          CHAR(36) PRIMARY KEY,

    user_id_ref CHAR(36),
    type        INT,            # 0: 文字, 1: 文件
    content     LONGTEXT,       # 内容

    create_time CHAR(19),
    update_time CHAR(19),

    INDEX user_id(user_id_ref), userIdRef_content(user_id_ref, content)
);