package com.aeisp.recharge.service.impl;

import com.aeisp.common.PageResult;
import com.aeisp.common.exception.BizException;
import com.aeisp.recharge.dto.PackageDTO;
import com.aeisp.recharge.dto.PackageQueryRequest;
import com.aeisp.recharge.dto.PackageVO;
import com.aeisp.recharge.entity.DurationPackage;
import com.aeisp.recharge.mapper.DurationPackageMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * PackageServiceImpl 单元测试。
 */
@ExtendWith(MockitoExtension.class)
class PackageServiceImplTest {

    @Mock
    private DurationPackageMapper packageMapper;

    @InjectMocks
    private PackageServiceImpl packageService;

    @Test
    void testCreatePackageSuccess() {
        when(packageMapper.insert(any(DurationPackage.class))).thenAnswer(inv -> {
            DurationPackage p = inv.getArgument(0);
            p.setId(1L);
            return 1;
        });

        PackageDTO dto = new PackageDTO();
        dto.setPackageName("Basic");
        dto.setPrice(9900);
        dto.setDurationHours(100L);
        dto.setValidDays(365);
        dto.setStatus(1);
        dto.setSortOrder(1);

        assertTrue(packageService.createPackage(dto));
        verify(packageMapper).insert(argThat((DurationPackage p) ->
                p.getPackageName().equals("Basic")
                        && p.getPriceCents() == 9900
                        && p.getDurationMinutes() == 6000L));
    }

    @Test
    void testUpdatePackageSuccess() {
        DurationPackage existing = new DurationPackage();
        existing.setId(1L);
        existing.setPackageName("Old");
        when(packageMapper.selectById(1L)).thenReturn(existing);
        when(packageMapper.updateById(any(DurationPackage.class))).thenReturn(1);

        PackageDTO dto = new PackageDTO();
        dto.setPackageName("New");
        dto.setPrice(5000);
        dto.setDurationHours(50L);
        dto.setStatus(1);

        assertTrue(packageService.updatePackage(1L, dto));
        verify(packageMapper).updateById(argThat((DurationPackage p) ->
                p.getPackageName().equals("New") && p.getDurationMinutes() == 3000L));
    }

    @Test
    void testUpdatePackageNotFound() {
        when(packageMapper.selectById(1L)).thenReturn(null);
        BizException ex = assertThrows(BizException.class,
                () -> packageService.updatePackage(1L, new PackageDTO()));
        assertEquals("套餐不存在", ex.getMessage());
    }

    @Test
    void testDeletePackageSuccess() {
        DurationPackage existing = new DurationPackage();
        existing.setId(1L);
        when(packageMapper.selectById(1L)).thenReturn(existing);

        assertTrue(packageService.deletePackage(1L));
        verify(packageMapper).deleteById(1L);
    }

    @Test
    void testDeletePackageNotFound() {
        when(packageMapper.selectById(1L)).thenReturn(null);
        assertThrows(BizException.class, () -> packageService.deletePackage(1L));
    }

    @Test
    void testListActivePackages() {
        DurationPackage p1 = new DurationPackage();
        p1.setId(1L);
        p1.setPackageName("Basic");
        p1.setPriceCents(9900);
        p1.setDurationMinutes(6000L);
        when(packageMapper.selectActiveList()).thenReturn(List.of(p1));

        List<PackageVO> list = packageService.listActivePackages();
        assertEquals(1, list.size());
        assertEquals("Basic", list.get(0).getPackageName());
        assertEquals(9900, list.get(0).getPrice());
        assertEquals(100L, list.get(0).getDurationHours());
    }

    @Test
    void testListPackages() {
        DurationPackage p1 = new DurationPackage();
        p1.setId(1L);
        p1.setPackageName("Basic");
        Page<DurationPackage> page = new Page<>(1, 10);
        page.setRecords(List.of(p1));
        page.setTotal(1);
        when(packageMapper.selectPage(any(Page.class), any())).thenReturn(page);

        PackageQueryRequest request = new PackageQueryRequest();
        request.setPageNum(1L);
        request.setPageSize(10L);
        PageResult<PackageVO> result = packageService.listPackages(request);

        assertEquals(1, result.getTotal());
        assertEquals("Basic", result.getList().get(0).getPackageName());
    }
}
