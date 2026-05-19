package com.aeisp.recharge.mapper;

import com.aeisp.recharge.entity.UsrBalanceChangeLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 余额变更日志 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface UsrBalanceChangeLogMapper extends BaseMapper<UsrBalanceChangeLog> {

    /**
     * 查询用户的余额变更记录。
     *
     * @param userId 用户 ID
     * @return 变更记录列表
     */
    List<UsrBalanceChangeLog> selectByUserId(@Param("userId") Long userId);
}
