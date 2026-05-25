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
     * @return 上线的模板列表
     */
    List<TplTemplate> selectOnlineList();

    /**
     * 查询当前日期最大的模板编码。
     *
     * @param prefix 编码前缀，如 "TPL20260525"
     * @return 最大的模板编码
     */
    String selectMaxTemplateCode(@Param("prefix") String prefix);

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
