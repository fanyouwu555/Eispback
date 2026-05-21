package com.aeisp.system.vo;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

@Data
public class MenuRoutesVO implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 导航菜单树（menuType IN (0,1,3), isVisible=1, 按角色权限过滤）。
     */
    private List<MenuVO> menus;

    /**
     * 当前用户全部权限标识列表（供前端 v-permission 指令使用）。
     */
    private List<String> permissions;
}