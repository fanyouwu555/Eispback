package com.aeisp.system.mapper;

import com.aeisp.system.entity.SysUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户角色关联 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface SysUserRoleMapper extends BaseMapper<SysUserRole> {

    /**
     * 根据用户 ID 删除关联记录。
     *
     * @param userId 用户 ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);

    /**
     * 批量插入用户角色关联。
     *
     * @param userId  用户 ID
     * @param roleIds 角色 ID 列表
     * @return 影响行数
     */
    int batchInsert(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);
}
