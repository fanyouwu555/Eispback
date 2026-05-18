package com.aeisp.recharge.service;

import com.aeisp.common.PageResult;
import com.aeisp.recharge.dto.PackageDTO;
import com.aeisp.recharge.dto.PackageQueryRequest;
import com.aeisp.recharge.dto.PackageVO;

import java.util.List;

/**
 * 时长套餐服务接口。
 *
 * <p>提供套餐的增删改查及前端有效套餐列表能力。</p>
 *
 * @author AEISP Team
 */
public interface PackageService {

    /**
     * 创建套餐。
     *
     * @param dto 套餐数据
     * @return 是否成功
     */
    boolean createPackage(PackageDTO dto);

    /**
     * 更新套餐。
     *
     * @param id  套餐 ID
     * @param dto 套餐数据
     * @return 是否成功
     */
    boolean updatePackage(Long id, PackageDTO dto);

    /**
     * 删除套餐。
     *
     * @param id 套餐 ID
     * @return 是否成功
     */
    boolean deletePackage(Long id);

    /**
     * 查询前端展示的有效套餐列表。
     *
     * @return 有效套餐列表
     */
    List<PackageVO> listActivePackages();

    /**
     * 分页查询套餐列表。
     *
     * @param request 查询条件
     * @return 分页结果
     */
    PageResult<PackageVO> listPackages(PackageQueryRequest request);
}
