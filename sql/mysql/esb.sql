-- ----------------------------
-- Table structure for esb_interface
-- ----------------------------
DROP TABLE IF EXISTS `esb_interface`;
CREATE TABLE `esb_interface` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `name` varchar(255) NOT NULL COMMENT '接口名称',
  `code` varchar(255) NOT NULL COMMENT '接口编码',
  `path` varchar(255) NOT NULL COMMENT '接口路径',
  `protocol` varchar(50) NOT NULL COMMENT '接口协议, e.g., HTTP, HTTPS, MLLP',
  `camel_route_id` varchar(255) DEFAULT NULL COMMENT 'Camel路由ID',
  `forward_interface_name` varchar(255) DEFAULT NULL COMMENT '转发接口名称',
  `forward_interface_address` varchar(1000) DEFAULT NULL COMMENT '转发接口地址',
  `forward_protocol` varchar(50) DEFAULT NULL COMMENT '转发接口协议',
  `retry_count` int DEFAULT '0' COMMENT '重试次数',
  `timeout_milliseconds` int DEFAULT '60000' COMMENT '超时时间 (毫秒)',
  `is_synchronous` tinyint(1) DEFAULT '1' COMMENT '同步或异步 (true: 同步, false: 异步)',
  `success_flag_json_path` varchar(255) DEFAULT NULL COMMENT '接口成功标识 (JSONPath expression to check in response)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 (0: 禁用, 1: 启用)',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_code` (`code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ESB接口配置表';

-- ----------------------------
-- Table structure for esb_mapping
-- ----------------------------
DROP TABLE IF EXISTS `esb_mapping`;
CREATE TABLE `esb_mapping` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `interface_id` bigint NOT NULL COMMENT '接口ID',
  `type` varchar(50) NOT NULL COMMENT '映射类型 (REQUEST, RESPONSE)',
  `content_type` varchar(100) DEFAULT NULL COMMENT '内容类型, e.g., application/json, application/xml',
  `mapping_template` text COMMENT '参数转换模板 (e.g., FreeMarker, Velocity, XSLT)',
  `status` tinyint NOT NULL DEFAULT '0' COMMENT '状态 (0: 禁用, 1: 启用)',
  `creator` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '创建者',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updater` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT '' COMMENT '更新者',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` bit(1) NOT NULL DEFAULT b'0' COMMENT '是否删除',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`id`),
  KEY `idx_interface_id` (`interface_id`),
  CONSTRAINT `fk_mapping_interface_id` FOREIGN KEY (`interface_id`) REFERENCES `esb_interface` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ESB参数映射表';

-- ----------------------------
-- Table structure for esb_log
-- ----------------------------
DROP TABLE IF EXISTS `esb_log`;
CREATE TABLE `esb_log` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `interface_id` bigint NOT NULL COMMENT '接口ID',
  `interface_code` varchar(255) NOT NULL COMMENT '接口编码',
  `request_time` datetime NOT NULL COMMENT '请求时间',
  `request_headers` text COMMENT '请求头',
  `request_body` text COMMENT '请求体',
  `response_time` datetime DEFAULT NULL COMMENT '响应时间',
  `response_headers` text COMMENT '响应头',
  `response_body` text COMMENT '响应体',
  `forward_request_time` datetime DEFAULT NULL COMMENT '转发请求时间',
  `forward_request_headers` text COMMENT '转发请求头',
  `forward_request_body` text COMMENT '转发请求体',
  `forward_response_time` datetime DEFAULT NULL COMMENT '转发响应时间',
  `forward_response_headers` text COMMENT '转发响应头',
  `forward_response_body` text COMMENT '转发响应体',
  `success` tinyint(1) DEFAULT NULL COMMENT '是否成功',
  `error_message` text COMMENT '错误信息',
  `duration_milliseconds` int DEFAULT NULL COMMENT '总耗时 (毫秒)',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  KEY `idx_interface_id` (`interface_id`),
  KEY `idx_interface_code` (`interface_code`),
  KEY `idx_request_time` (`request_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ESB请求日志表';

-- ----------------------------
-- Table structure for esb_monitoring
-- ----------------------------
DROP TABLE IF EXISTS `esb_monitoring`;
CREATE TABLE `esb_monitoring` (
  `id` bigint NOT NULL AUTO_INCREMENT COMMENT 'ID',
  `interface_id` bigint NOT NULL COMMENT '接口ID',
  `last_check_time` datetime DEFAULT NULL COMMENT '上次检查时间',
  `is_available` tinyint(1) DEFAULT NULL COMMENT '是否可用',
  `status_code` int DEFAULT NULL COMMENT 'HTTP状态码或其他状态标识',
  `error_message` text COMMENT '错误信息 (如果不可用)',
  `response_time_milliseconds` int DEFAULT NULL COMMENT '响应时间 (毫秒)',
  `tenant_id` bigint NOT NULL DEFAULT '0' COMMENT '租户编号',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_interface_id` (`interface_id`),
  CONSTRAINT `fk_monitoring_interface_id` FOREIGN KEY (`interface_id`) REFERENCES `esb_interface` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='ESB接口监控表';
