package com.aeisp.system.mapper;

import com.aeisp.system.entity.SysRolePermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 角色权限关联 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface SysRolePermissionMapper extends BaseMapper<SysRolePermission> {

    /**
     * 根据角色 ID 删除关联记录。
     *
     * @param roleId 角色 ID
     * @return 影响行数
     */
    int deleteByRoleId(@Param("roleId") Long roleId);

    /**
     * 批量插入角色权限关联。
     *
     * @param roleId        角色 ID
     * @param permissionIds 权限 ID 列表
     * @return 影响行数
     */
    int batchInsert(@Param("roleId") Long roleId, @Param("permissionIds") List<Long> permissionIds);
}
