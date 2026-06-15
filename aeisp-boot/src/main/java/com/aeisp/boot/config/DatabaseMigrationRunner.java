package com.aeisp.boot.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 数据库自动迁移执行器。
 *
 * <p>应用启动时自动检查并执行 {@code db/migration/} 目录下的 SQL 迁移脚本。
 * 通过 {@code sys_schema_migration} 表记录已执行的迁移，确保每个脚本只执行一次。
 * 仅在新环境或缺失列时补全，不影响已有数据。</p>
 *
 * @author AEISP Team
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationRunner implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(ApplicationArguments args) {
        ensureMigrationTable();
        executePendingMigrations();
    }

    private void ensureMigrationTable() {
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS sys_schema_migration (
                    id SERIAL PRIMARY KEY,
                    filename VARCHAR(255) NOT NULL UNIQUE,
                    executed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
                )
                """);
    }

    private void executePendingMigrations() {
        List<String> executed = jdbcTemplate.queryForList(
                "SELECT filename FROM sys_schema_migration", String.class);

        // 仅包含幂等的 DDL 迁移（ADD COLUMN IF NOT EXISTS 等）。
        // 数据修改类迁移（如 URL 截断）需手动执行，不应放入此列表。
        String[] migrations = {
                "migration-add-user-apikey-tenantid.sql"
        };

        for (String filename : migrations) {
            if (executed.contains(filename)) {
                log.debug("迁移已执行，跳过: {}", filename);
                continue;
            }
            try {
                String sql = loadMigrationSql(filename);
                jdbcTemplate.execute(sql);
                jdbcTemplate.update(
                        "INSERT INTO sys_schema_migration (filename) VALUES (?)", filename);
                log.info("迁移执行成功: {}", filename);
            } catch (Exception e) {
                log.warn("迁移执行失败 (可能已手动执行过): {} — {}", filename, e.getMessage());
            }
        }
    }

    private String loadMigrationSql(String filename) {
        try {
            ClassPathResource resource = new ClassPathResource("db/migration/" + filename);
            return StreamUtils.copyToString(resource.getInputStream(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("无法加载迁移脚本: " + filename, e);
        }
    }
}
