package com.aeisp.user.mapper;

import com.aeisp.user.entity.UsrUser;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 前端用户 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface UsrUserMapper extends BaseMapper<UsrUser> {

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户实体，不存在则返回 null
     */
    UsrUser selectByUsername(@Param("username") String username);

    /**
     * 根据手机号查询用户。
     *
     * @param phone 手机号
     * @return 用户实体，不存在则返回 null
     */
    UsrUser selectByPhone(@Param("phone") String phone);

    /**
     * 根据邮箱查询用户。
     *
     * @param email 邮箱
     * @return 用户实体，不存在则返回 null
     */
    UsrUser selectByEmail(@Param("email") String email);

    /**
     * 批量插入用户（用于导入）。
     *
     * @param list 用户列表
     * @return 插入记录数
     */
    int batchInsert(@Param("list") List<UsrUser> list);
}
