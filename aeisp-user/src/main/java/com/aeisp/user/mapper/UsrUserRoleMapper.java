package com.aeisp.user.mapper;

import com.aeisp.user.entity.UsrUserRole;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 前端用户角色关联 Mapper 接口。
 *
 * @author AEISP Team
 */
public interface UsrUserRoleMapper extends BaseMapper<UsrUserRole> {

    /**
     * 根据用户ID查询角色ID列表。
     *
     * @param userId 用户ID
     * @return 角色ID列表
     */
    List<Long> selectRoleIdsByUserId(@Param("userId") Long userId);

    /**
     * 批量插入用户角色关联。
     *
     * @param list 关联列表
     * @return 插入记录数
     */
    int batchInsert(@Param("list") List<UsrUserRole> list);
}
