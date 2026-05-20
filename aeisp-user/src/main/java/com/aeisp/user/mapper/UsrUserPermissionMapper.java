package com.aeisp.user.mapper;

import com.aeisp.user.entity.UsrUserPermission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

public interface UsrUserPermissionMapper extends BaseMapper<UsrUserPermission> {

    @Select("SELECT * FROM usr_user_permission WHERE user_id = #{userId} AND deleted = 0")
    List<UsrUserPermission> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT * FROM usr_user_permission WHERE user_id = #{userId} AND perm_key = #{permKey} AND deleted = 0 LIMIT 1")
    UsrUserPermission selectByUserIdAndKey(@Param("userId") Long userId, @Param("permKey") String permKey);
}