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
 * <p>用于在开发/测试环境快速重新初始化 PostgreSQL 数据库。
 * 唯一脚本：init-postgresql-complete.sql（包含全量建表 + 种子数据）。
 */
@SpringBootTest
class DbInitTest {

    @Autowired
    private DataSource dataSource;

    /**
     * 使用完整初始化脚本初始化数据库。
     *
     * <p>包含所有表结构和初始数据，新旧环境均适用。
     * 执行前会清空所有表数据，请谨慎使用。
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
}
