package com.aeisp.recharge.service;

import com.aeisp.common.PageResult;
import com.aeisp.recharge.dto.DurationConsumeVO;
import com.aeisp.recharge.dto.DurationQueryRequest;
import com.aeisp.recharge.dto.DurationStatVO;

/**
 * 时长服务接口。
 *
 * <p>提供时长消耗、增加、查询及统计能力。</p>
 *
 * @author AEISP Team
 */
public interface DurationService {

    /**
     * 消耗时长。
     *
     * @param userId      用户 ID
     * @param minutes     消耗分钟数
     * @param consumeType 消耗类型
     * @param projectId   关联项目 ID
     * @param description 描述信息
     * @return 是否成功
     */
    boolean consumeDuration(Long userId, Long minutes, String consumeType, Long projectId, String description);

    /**
     * 增加时长。
     *
     * @param userId  用户 ID
     * @param minutes 增加分钟数
     * @param reason  原因
     * @return 是否成功
     */
    boolean addDuration(Long userId, Long minutes, String reason);

    /**
     * 分页查询时长消耗记录。
     *
     * @param userId  用户 ID
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<DurationConsumeVO> listConsumes(Long userId, DurationQueryRequest request);

    /**
     * 获取用户时长统计。
     *
     * @param userId 用户 ID
     * @return 时长统计视图对象
     */
    DurationStatVO getDurationStats(Long userId);
}
