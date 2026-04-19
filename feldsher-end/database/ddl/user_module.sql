-- =============================================
-- 用户模块数据库设计
-- 数据库: feldsher
-- 字符集: utf8mb4
-- 排序规则: utf8mb4_unicode_ci
-- =============================================

-- 创建数据库
CREATE DATABASE IF NOT EXISTS `feldsher` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE `feldsher`;

-- =============================================
-- 1. 角色表 (sys_role)
-- =============================================
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `role_name` varchar(50) NOT NULL COMMENT '角色名称',
    `role_code` varchar(50) NOT NULL COMMENT '角色编码',
    `description` varchar(200) DEFAULT NULL COMMENT '角色描述',
    `status` tinyint(1) DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除: 1-删除, 0-未删除',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_role_code` (`role_code`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='角色表';

-- 初始化角色数据
INSERT INTO `sys_role` (`role_name`, `role_code`, `description`) VALUES 
('医生', 'DOCTOR', '医生角色，可查看患者信息并进行问诊'),
('患者', 'PATIENT', '患者角色，可进行AI问诊和咨询医生');

-- =============================================
-- 2. 用户表 (sys_user)
-- =============================================
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `phone` varchar(11) NOT NULL COMMENT '手机号（登录账号）',
    `password` varchar(255) NOT NULL COMMENT '密码（BCrypt加密）',
    `nickname` varchar(50) DEFAULT NULL COMMENT '昵称',
    `avatar` varchar(500) DEFAULT NULL COMMENT '头像URL',
    `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
    `gender` tinyint(1) DEFAULT 0 COMMENT '性别: 0-未知, 1-男, 2-女',
    `birthday` date DEFAULT NULL COMMENT '生日',
    `id_card` varchar(18) DEFAULT NULL COMMENT '身份证号',
    `real_name` varchar(50) DEFAULT NULL COMMENT '真实姓名',
    `department` varchar(100) DEFAULT NULL COMMENT '科室（医生专用）',
    `title` varchar(100) DEFAULT NULL COMMENT '职称（医生专用）',
    `introduction` text DEFAULT NULL COMMENT '个人简介',
    `status` tinyint(1) DEFAULT 1 COMMENT '状态: 1-启用, 0-禁用',
    `deleted` tinyint(1) DEFAULT 0 COMMENT '逻辑删除: 1-删除, 0-未删除',
    `last_login_time` datetime DEFAULT NULL COMMENT '最后登录时间',
    `last_login_ip` varchar(50) DEFAULT NULL COMMENT '最后登录IP',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_phone` (`phone`),
    UNIQUE KEY `uk_id_card` (`id_card`),
    KEY `idx_status` (`status`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户表';

-- =============================================
-- 3. 用户角色关联表 (sys_user_role)
-- =============================================
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) NOT NULL COMMENT '用户ID',
    `role_id` bigint(20) NOT NULL COMMENT '角色ID',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_user_role` (`user_id`, `role_id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_role_id` (`role_id`),
    CONSTRAINT `fk_user_role_user` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE,
    CONSTRAINT `fk_user_role_role` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='用户角色关联表';

-- =============================================
-- 4. 操作日志表 (sys_operation_log)
-- =============================================
DROP TABLE IF EXISTS `sys_operation_log`;
CREATE TABLE `sys_operation_log` (
    `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
    `user_id` bigint(20) DEFAULT NULL COMMENT '操作用户ID',
    `username` varchar(50) DEFAULT NULL COMMENT '操作用户名',
    `operation` varchar(100) DEFAULT NULL COMMENT '操作描述',
    `method` varchar(200) DEFAULT NULL COMMENT '请求方法',
    `params` text DEFAULT NULL COMMENT '请求参数',
    `ip` varchar(50) DEFAULT NULL COMMENT '请求IP',
    `location` varchar(100) DEFAULT NULL COMMENT '请求地点',
    `status` tinyint(1) DEFAULT 1 COMMENT '操作状态: 1-成功, 0-失败',
    `error_msg` text DEFAULT NULL COMMENT '错误信息',
    `operation_time` bigint(20) DEFAULT NULL COMMENT '执行耗时（毫秒）',
    `create_time` datetime DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    PRIMARY KEY (`id`),
    KEY `idx_user_id` (`user_id`),
    KEY `idx_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='操作日志表';
