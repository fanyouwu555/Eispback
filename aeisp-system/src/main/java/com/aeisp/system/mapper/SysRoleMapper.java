package com.aeisp.system.mapper;

import com.aeisp.common.constant.CommonConstants;
import com.aeisp.system.entity.SysRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 角色 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface SysRoleMapper extends BaseMapper<SysRole> {

    /**
     * 根据用户 ID 查询角色编码列表。
     *
     * @param userId 用户 ID
     * @return 角色编码列表
     */
    @Select("SELECT r.role_code FROM sys_role r " +
            "INNER JOIN sys_user_role ur ON r.id = ur.role_id " +
            "WHERE ur.user_id = #{userId} AND r.deleted = 0 AND r.status = " + CommonConstants.STATUS_ENABLED)
    List<String> selectRoleCodesByUserId(@Param("userId") Long userId);
}
