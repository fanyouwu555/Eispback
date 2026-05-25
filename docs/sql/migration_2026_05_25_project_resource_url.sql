-- 项目模块：为 prj_project 表添加资源存储 URL 字段
ALTER TABLE prj_project ADD COLUMN IF NOT EXISTS resource_url VARCHAR(512) DEFAULT NULL;
COMMENT ON COLUMN prj_project.resource_url IS '项目资源存储URL，如 http://192.168.50.215/EISP/Users/{userId}/{projectId}/';