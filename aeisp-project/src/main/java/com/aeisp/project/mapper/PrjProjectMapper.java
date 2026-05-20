package com.aeisp.project.mapper;

import com.aeisp.project.entity.PrjProject;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface PrjProjectMapper extends BaseMapper<PrjProject> {

    /**
     * 按日统计项目创建数量。
     */
    List<Map<String, Object>> selectDailyProjectStats(@Param("start") LocalDateTime start,
                                                      @Param("end") LocalDateTime end);

    /**
     * 统计标杆项目数量。
     */
    Long countBenchmarkProjects();
}