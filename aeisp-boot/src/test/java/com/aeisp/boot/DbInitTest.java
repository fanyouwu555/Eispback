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
 * <p>用于在开发/测试环境快速重新初始化 PostgreSQL 数据库。</p>
 */
@SpringBootTest
class DbInitTest {

    @Autowired
    private DataSource dataSource;

    @Test
    void initDatabase() throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            FileSystemResource fileResource = new FileSystemResource("../docs/sql/init-postgresql.sql");
            EncodedResource resource = new EncodedResource(fileResource, StandardCharsets.UTF_8);
            ScriptUtils.executeSqlScript(conn, resource, true, true,
                    ScriptUtils.DEFAULT_COMMENT_PREFIX, ScriptUtils.DEFAULT_STATEMENT_SEPARATOR,
                    ScriptUtils.DEFAULT_BLOCK_COMMENT_START_DELIMITER, ScriptUtils.DEFAULT_BLOCK_COMMENT_END_DELIMITER);
        }
    }
}
