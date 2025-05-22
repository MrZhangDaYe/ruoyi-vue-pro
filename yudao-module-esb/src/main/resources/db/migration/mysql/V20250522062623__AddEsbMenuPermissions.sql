-- ========== ESB - Integration Hub (Top-level Menu) ==========
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, always_show, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2500, 'ESB - Integration Hub', '', 0, 100, 0, 'esb-hub', 'el-icon-set-up', NULL, 0, true, true, true, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- ========== Interface Management Menu and Permissions ==========
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2501, 'Interface Management', '', 1, 1, 2500, 'interface', 'el-icon-postcard', 'esb/interface/index', 0, true, true, 'admin', NOW(), 'admin', NOW(), 0, 1);

INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2502, 'Query Interfaces', 'esb:interface:query', 2, 1, 2501, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2503, 'Create Interface', 'esb:interface:create', 2, 2, 2501, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2504, 'Update Interface', 'esb:interface:update', 2, 3, 2501, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2505, 'Delete Interface', 'esb:interface:delete', 2, 4, 2501, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2506, 'Export Interfaces', 'esb:interface:export', 2, 5, 2501, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2507, 'Start Route', 'esb:interface:start', 2, 6, 2501, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2508, 'Stop Route', 'esb:interface:stop', 2, 7, 2501, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2509, 'Refresh Route', 'esb:interface:refresh', 2, 8, 2501, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- ========== Data Mapping Menu and Permissions ==========
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2510, 'Data Mapping', '', 1, 2, 2500, 'mapping', 'el-icon-copy-document', 'esb/mapping/index', 0, true, true, 'admin', NOW(), 'admin', NOW(), 0, 1);

INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2511, 'Query Mappings', 'esb:mapping:query', 2, 1, 2510, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2512, 'Create Mapping', 'esb:mapping:create', 2, 2, 2510, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2513, 'Update Mapping', 'esb:mapping:update', 2, 3, 2510, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2514, 'Delete Mapping', 'esb:mapping:delete', 2, 4, 2510, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- ========== Interface Logs Menu and Permissions ==========
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2515, 'Interface Logs', '', 1, 3, 2500, 'log', 'el-icon-document', 'esb/log/index', 0, true, true, 'admin', NOW(), 'admin', NOW(), 0, 1);

INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2516, 'Query Logs', 'esb:log:query', 2, 1, 2515, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);

-- ========== Interface Monitoring Menu and Permissions ==========
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, path, icon, component, status, visible, keep_alive, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2517, 'Interface Monitoring', '', 1, 4, 2500, 'monitoring', 'el-icon-data-line', 'esb/monitoring/index', 0, true, true, 'admin', NOW(), 'admin', NOW(), 0, 1);

INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2518, 'Query Monitoring', 'esb:monitoring:query', 2, 1, 2517, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);
INSERT INTO system_menu(id, name, permission, type, sort, parent_id, status, creator, create_time, updater, update_time, deleted, tenant_id)
VALUES (2519, 'Manual Check', 'esb:monitoring:check', 2, 2, 2517, 0, 'admin', NOW(), 'admin', NOW(), 0, 1);


-- ========== Assign ESB Menus/Permissions to Super Admin (role_id = 1) ==========
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2500, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2501, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2502, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2503, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2504, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2505, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2506, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2507, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2508, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2509, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2510, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2511, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2512, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2513, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2514, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2515, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2516, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2517, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2518, 1, 'admin', NOW(), 'admin', NOW(), 0);
INSERT INTO system_role_menu(role_id, menu_id, tenant_id, creator, create_time, updater, update_time, deleted) VALUES (1, 2519, 1, 'admin', NOW(), 'admin', NOW(), 0);
