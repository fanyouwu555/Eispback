package com.aeisp.system.mapper;

import com.aeisp.system.entity.SysUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 后台管理员账号 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface SysUserMapper extends BaseMapper<SysUser> {

    /**
     * 根据用户名查询用户。
     *
     * @param username 登录用户名
     * @return 用户实体，不存在则返回 {@code null}
     */
    @Select("SELECT * FROM sys_user WHERE username = #{username} AND deleted = 0 LIMIT 1")
    SysUser selectByUsername(@Param("username") String username);
}
