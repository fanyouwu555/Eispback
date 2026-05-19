package com.aeisp.model.mapper;

import com.aeisp.model.entity.AiModel;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * AI 大模型配置 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface AiModelMapper extends BaseMapper<AiModel> {

    /**
     * 查询启用的模型列表。
     *
     * @return 启用的模型列表
     */
    List<AiModel> selectEnabledList();

    /**
     * 使用次数 +1。
     *
     * @param id 模型 ID
     * @return 影响行数
     */
    int incrementUsageCount(@Param("id") Long id);
}
