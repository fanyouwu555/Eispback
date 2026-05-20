package com.aeisp.template.mapper;

import com.aeisp.template.entity.TplTemplate;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

/**
 * 模板主表 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface TplTemplateMapper extends BaseMapper<TplTemplate> {

    /**
     * 查询上线的模板列表。
     *
     * @param scenario 适用场景筛选（可选）
     * @return 上线的模板列表
     */
    List<TplTemplate> selectOnlineList(@Param("scenario") String scenario);

    /**
     * 使用次数 +1。
     *
     * @param id 模板 ID
     * @return 影响行数
     */
    int incrementUsageCount(@Param("id") Long id);

    /**
     * 查询热门模板 Top N。
     */
    List<Map<String, Object>> selectTopHotTemplates(@Param("limit") int limit);
}
