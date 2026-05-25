package com.aeisp.recharge.code;

import com.aeisp.common.code.ErrorCode;

public enum RechargeErrorCode implements ErrorCode {
    PACKAGE_NOT_FOUND(7001, "套餐不存在"),
    ORDER_NOT_FOUND(7011, "订单不存在"),
    ORDER_STATUS_PAY_INVALID(7012, "订单状态不正确，无法支付"),
    ORDER_STATUS_REFUND_INVALID(7013, "订单状态不正确，无法退款"),
    ORDER_STATUS_CANCEL_INVALID(7014, "订单状态不正确，无法取消"),
    BALANCE_INSUFFICIENT(7021, "余额不足"),
    BALANCE_INSUFFICIENT_RECHARGE(7022, "余额不足，请先充值"),
    BALANCE_RECHARGE_AMOUNT_INVALID(7023, "充值金额必须大于0"),
    BALANCE_DEDUCT_AMOUNT_INVALID(7024, "抵扣金额必须大于0"),
    BALANCE_ADJUST_ZERO(7025, "调整金额不能为0"),
    BALANCE_ADJUST_NEGATIVE(7026, "调整后余额不能为负数"),
    DURATION_CONSUME_INVALID(7031, "消耗时长必须大于0"),
    DURATION_INSUFFICIENT(7032, "剩余时长不足"),
    DURATION_ADD_INVALID(7033, "增加时长必须大于0");

    private final int code;
    private final String message;

    RechargeErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
