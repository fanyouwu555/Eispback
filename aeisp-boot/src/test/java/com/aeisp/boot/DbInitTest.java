package com.aeisp.boot;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.jdbc.datasource.init.ScriptUtils;

import javax.sql.DataSource;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;

/**
 * 数据库初始化测试类。
 *
 * <p>用于在开发/测试环境快速重新初始化PostgreSQL数据库。
 */
@SpringBootTest
class DbInitTest {

    @Autowired
    private DataSource dataSource;

    /**
     * 使用完整初始化脚本初始化数据库。
     *
     * <p>包含所有表结构和初始数据，推荐用于新环境。
     */
    @Test
    void initDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            FileSystemResource fileResource = new FileSystemResource("../docs/sql/init-postgresql-complete.sql");
            EncodedResource resource = new EncodedResource(fileResource, StandardCharsets.UTF_8);
            ScriptUtils.executeSqlScript(conn, resource, true, true,
                    ScriptUtils.DEFAULT_COMMENT_PREFIX, ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
                    ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER, ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
        }
    }

    /**
     * 使用基础初始化脚本初始化数据库（不含后来新增的表）。
     *
     * <p>保留用于向后兼容，不推荐用于新环境。
     */
    @Test
    void initDatabaseBasic() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            FileSystemResource fileResource = new FileSystemResource("../docs/sql/init-postgresql.sql");
            EncodedResource resource = new EncodedResource(fileResource, StandardCharsets.UTF_8);
            ScriptUtils.executeSqlScript(conn, resource, true, true,
                    ScriptUtils.DEFAULT_COMMENT_PREFIX, ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
                    ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER, ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
        }
    }

    /**
     * 运行迁移脚本（仅适用于已经初始化过的数据库）。
     *
     * <p>用于给现有数据库添加缺失的表和字段。
     */
    @Test
    void runMigrations() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            FileSystemResource fileResource = new FileSystemResource("../docs/sql/migration-postgresql.sql");
            EncodedResource resource = new EncodedResource(fileResource, StandardCharsets.UTF_8);
            ScriptUtils.executeSqlScript(conn, resource, true, true,
                    ScriptUtils.DEFAULT_COMMENT_PREFIX, ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
                    ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER, ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
        }
    }
}
