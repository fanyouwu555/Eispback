package com.aeisp.common.typehandler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.ibatis.type.BaseTypeHandler;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.MappedJdbcTypes;
import org.apache.ibatis.type.MappedTypes;

import java.sql.CallableStatement;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

/**
 * PostgreSQL JSONB 类型处理器。
 *
 * <p>支持将 Object/Map/String 与 PostgreSQL JSONB 类型互相转换。</p>
 *
 * @author AEISP Team
 */
@MappedTypes({Object.class, Map.class, String.class})
@MappedJdbcTypes(JdbcType.OTHER)
public class JsonbTypeHandler extends BaseTypeHandler<Object> {

    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final Class<?> type;

    public JsonbTypeHandler() {
        this(Object.class);
    }

    public JsonbTypeHandler(Class<?> type) {
        this.type = type;
    }

    @Override
    public void setNonNullParameter(PreparedStatement ps, int i, Object parameter, JdbcType jdbcType) throws SQLException {
        String json;
        try {
            if (parameter instanceof String) {
                // 如果已经是String，直接使用
                json = (String) parameter;
                // 验证是否是有效的JSON（可选）
                if (!isValidJson(json)) {
                    // 如果不是有效JSON，将其包装为JSON字符串
                    json = objectMapper.writeValueAsString(json);
                }
            } else {
                // 其他类型，转换为JSON
                json = objectMapper.writeValueAsString(parameter);
            }
        } catch (JsonProcessingException e) {
            throw new SQLException("Error converting object to JSON: " + e.getMessage(), e);
        }
        // 使用 setObject 并指定类型为 OTHER
        ps.setObject(i, json, java.sql.Types.OTHER);
    }

    @Override
    public Object getNullableResult(ResultSet rs, String columnName) throws SQLException {
        return parseJson(rs.getString(columnName));
    }

    @Override
    public Object getNullableResult(ResultSet rs, int columnIndex) throws SQLException {
        return parseJson(rs.getString(columnIndex));
    }

    @Override
    public Object getNullableResult(CallableStatement cs, int columnIndex) throws SQLException {
        return parseJson(cs.getString(columnIndex));
    }

    private Object parseJson(String json) throws SQLException {
        if (json == null) {
            return null;
        }
        // 如果目标类型是String，直接返回
        if (String.class.isAssignableFrom(type)) {
            return json;
        }
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            // 如果解析失败，返回原始字符串作为备选方案
            if (Object.class.isAssignableFrom(type)) {
                return json;
            }
            throw new SQLException("Error parsing JSON: " + e.getMessage(), e);
        }
    }

    private boolean isValidJson(String json) {
        if (json == null || json.trim().isEmpty()) {
            return false;
        }
        String trimmed = json.trim();
        return (trimmed.startsWith("{") && trimmed.endsWith("}")) ||
               (trimmed.startsWith("[") && trimmed.endsWith("]"));
    }
}
