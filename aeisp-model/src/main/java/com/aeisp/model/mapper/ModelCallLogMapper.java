package com.aeisp.model.mapper;

import com.aeisp.model.entity.ModelCallLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 模型调用日志 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface ModelCallLogMapper extends BaseMapper<ModelCallLog> {

    /**
     * 统计指定时间范围内某模型的调用次数。
     *
     * @param modelId 模型 ID
     * @param start   起始时间
     * @param end     截止时间
     * @return 调用次数
     */
    Long countCallsByModelId(@Param("modelId") Long modelId,
                             @Param("start") LocalDateTime start,
                             @Param("end") LocalDateTime end);

    /**
     * 统计全量调用次数。
     */
    Long countTotalCalls();

    /**
     * 统计成功调用次数。
     */
    Long countSuccessCalls();

    /**
     * 按日统计调用量和 Token 消耗。
     */
    List<Map<String, Object>> selectDailyCallStats(@Param("start") LocalDateTime start,
                                                   @Param("end") LocalDateTime end);
}
