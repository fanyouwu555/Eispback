package com.aeisp.user.mapper;

import com.aeisp.user.entity.UsrUserPermissionLog;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 用户权限变更日志 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface UsrUserPermissionLogMapper extends BaseMapper<UsrUserPermissionLog> {

    @Select("SELECT * FROM usr_user_permission_log WHERE user_id = #{userId} ORDER BY created_at DESC")
    List<UsrUserPermissionLog> selectByUserId(@Param("userId") Long userId);
}
