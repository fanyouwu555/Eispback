package com.aeisp.recharge.mapper;

import com.aeisp.recharge.entity.RechargeOrder;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 充值订单 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface RechargeOrderMapper extends BaseMapper<RechargeOrder> {

    /**
     * 根据订单号查询订单。
     *
     * @param orderNo 订单号
     * @return 订单信息
     */
    RechargeOrder selectByOrderNo(@Param("orderNo") String orderNo);

    /**
     * 查询用户的订单列表。
     *
     * @param userId 用户 ID
     * @return 订单列表
     */
    List<RechargeOrder> selectByUserId(@Param("userId") Long userId);

    /**
     * 查询指定时间范围内已支付订单的总金额（分）。
     */
    Long selectDailyRechargeSum(@Param("start") LocalDateTime start,
                                @Param("end") LocalDateTime end);

    /**
     * 查询所有已支付订单的总金额（分）。
     */
    Long selectTotalRechargeSum();
}
