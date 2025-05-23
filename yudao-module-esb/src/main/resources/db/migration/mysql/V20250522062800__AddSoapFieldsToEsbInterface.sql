ALTER TABLE `esb_interface`
    ADD COLUMN `wsdl_url` VARCHAR(1024) NULL COMMENT 'WSDL文件的URL (SOAP)' AFTER `success_flag_json_path`,
    ADD COLUMN `soap_service_name` VARCHAR(255) NULL COMMENT 'SOAP服务名称 (SOAP)' AFTER `wsdl_url`,
    ADD COLUMN `soap_port_name` VARCHAR(255) NULL COMMENT 'SOAP端口名称 (SOAP)' AFTER `soap_service_name`,
    ADD COLUMN `soap_operation_name` VARCHAR(255) NULL COMMENT 'SOAP操作名称 (SOAP)' AFTER `soap_port_name`,
    ADD COLUMN `data_format` VARCHAR(50) NULL DEFAULT 'PAYLOAD' COMMENT 'CXF数据格式 (SOAP), 例如: PAYLOAD, POJO' AFTER `soap_operation_name`;

-- Note: The `status` and `remark` columns in the original esb_interface table
-- would come after these new SOAP fields if we strictly followed the previous
-- ALTER statement's `AFTER` clause logic for every field.
-- However, the current script correctly chains the `AFTER` clauses for the new fields.
-- The final order will be: ... success_flag_json_path, wsdl_url, soap_service_name, soap_port_name, soap_operation_name, data_format, status, creator, ...
-- This should be acceptable.
