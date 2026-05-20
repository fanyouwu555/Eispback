package com.aeisp.user.mapper;

import com.aeisp.user.entity.UsrUserBalance;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

/**
 * 用户余额 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface UsrUserBalanceMapper extends BaseMapper<UsrUserBalance> {

    /**
     * 根据用户ID查询余额记录。
     *
     * @param userId 用户ID
     * @return 余额实体，不存在则返回 null
     */
    UsrUserBalance selectByUserId(@Param("userId") Long userId);

    /**
     * 查询平台总充值金额（分）。
     */
    Long selectTotalRechargeSum();

    /**
     * 查询平台总消费金额（分）。
     */
    Long selectTotalConsumedSum();

    /**
     * 查询用户平均余额（分）。
     */
    Long selectAvgBalance();

    /**
     * 查询平台当前总余额（分）。
     */
    Long selectTotalBalanceSum();
}
