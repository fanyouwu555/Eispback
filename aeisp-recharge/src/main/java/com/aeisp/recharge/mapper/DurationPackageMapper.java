package com.aeisp.recharge.mapper;

import com.aeisp.recharge.entity.DurationPackage;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;

/**
 * 时长套餐 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface DurationPackageMapper extends BaseMapper<DurationPackage> {

    /**
     * 查询上架的套餐列表。
     *
     * @return 上架套餐列表
     */
    List<DurationPackage> selectActiveList();
}
