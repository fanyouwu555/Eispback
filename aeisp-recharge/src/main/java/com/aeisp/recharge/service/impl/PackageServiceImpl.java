package com.aeisp.recharge.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.recharge.dto.PackageDTO;
import com.aeisp.recharge.dto.PackageQueryRequest;
import com.aeisp.recharge.dto.PackageVO;
import com.aeisp.recharge.service.PackageService;
import org.springframework.stereotype.Service;

import java.util.Collections;

/**
 * 时长套餐服务实现类。
 *
 * <p>待实现真实业务逻辑，当前所有方法抛出 {@link UnsupportedOperationException}。</p>
 *
 * @author AEISP Team
 */
@Service
public class PackageServiceImpl implements PackageService {

    @Override
    public boolean createPackage(PackageDTO dto) {
        throw new UnsupportedOperationException("createPackage 待实现");
    }

    @Override
    public boolean updatePackage(Long id, PackageDTO dto) {
        throw new UnsupportedOperationException("updatePackage 待实现");
    }

    @Override
    public boolean deletePackage(Long id) {
        throw new UnsupportedOperationException("deletePackage 待实现");
    }

    @Override
    public java.util.List<PackageVO> listActivePackages() {
        throw new UnsupportedOperationException("listActivePackages 待实现");
    }

    @Override
    public PageResult<PackageVO> listPackages(PackageQueryRequest request) {
        return PageResult.<PackageVO>builder()
                .list(Collections.emptyList())
                .total(0L)
                .pageNum(request.getPageNum())
                .pageSize(request.getPageSize())
                .totalPages(0L)
                .build();
    }
}
