package com.aeisp.user.mapper;

import com.aeisp.user.entity.UsrLoginLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户登录日志 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface UsrLoginLogMapper extends BaseMapper<UsrLoginLog> {

    /**
     * 统计指定时间范围内有成功登录记录的独立用户数量。
     *
     * @param start 起始时间
     * @param end   截止时间
     * @return 活跃用户数
     */
    Long countActiveUsers(@Param("start") java.time.LocalDateTime start,
                          @Param("end") java.time.LocalDateTime end);

    /**
     * 统计全量有过成功登录记录的独立用户数量。
     */
    Long countAllActiveUsers();
}
