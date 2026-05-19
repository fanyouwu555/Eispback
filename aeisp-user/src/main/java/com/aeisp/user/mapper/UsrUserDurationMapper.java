package com.aeisp.user.mapper;

import com.aeisp.user.entity.UsrUserDuration;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户时长 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface UsrUserDurationMapper extends BaseMapper<UsrUserDuration> {

    /**
     * 根据用户ID查询时长记录。
     *
     * @param userId 用户ID
     * @return 时长实体，不存在则返回 null
     */
    UsrUserDuration selectByUserId(@Param("userId") Long userId);
}
