package com.aicloud.module.system.biz.service;

import com.aicloud.module.system.biz.entity.AiMenu;
import com.aicloud.module.system.biz.entity.AiRoleMenu;
import com.aicloud.module.system.biz.mapper.MenuMapper;
import com.aicloud.module.system.biz.mapper.RoleMenuMapper;
import com.aicloud.module.system.biz.model.MenuNode;
import com.aicloud.module.system.biz.model.menu.MenuSaveRequest;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author AICloud
 */
@Service
public class MenuService {

    private static final int MENU_TYPE_DIR = 1;
    private static final int MENU_TYPE_BUTTON = 3;

    private final MenuMapper menuMapper;
    private final RoleMenuMapper roleMenuMapper;

    public MenuService(MenuMapper menuMapper, RoleMenuMapper roleMenuMapper) {
        this.menuMapper = menuMapper;
        this.roleMenuMapper = roleMenuMapper;
    }

    public List<MenuNode> getMenuTree(Long tenantId, Long userId) {
        return buildTree(toNodes(menuMapper.listMenusByUserId(tenantId, userId)));
    }

    public List<String> getButtonPermissions(Long tenantId, Long userId) {
        return menuMapper.listButtonPermissionsByUserId(tenantId, userId);
    }

    public List<MenuNode> getAllMenus(Long tenantId) {
        return buildTree(toNodes(menuMapper.listAllMenus(tenantId)));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiMenu save(Long tenantId, MenuSaveRequest request) {
        if (!StringUtils.hasText(request.getName())) {
            throw new IllegalArgumentException("菜单名称不能为空");
        }
        Integer type = request.getType() == null ? 2 : request.getType();
        if (type < MENU_TYPE_DIR || type > MENU_TYPE_BUTTON) {
            throw new IllegalArgumentException("菜单类型非法");
        }
        AiMenu menu = request.getId() == null ? new AiMenu() : menuMapper.selectById(request.getId());
        boolean tenantMismatch = menu != null && menu.getTenantId() != null && !tenantId.equals(menu.getTenantId());
        if (menu == null || tenantMismatch) {
            throw new IllegalArgumentException("菜单不存在");
        }
        if (request.getId() == null) {
            menu.setTenantId(tenantId);
        }
        menu.setParentId(request.getParentId() == null ? 0L : request.getParentId());
        menu.setName(request.getName());
        menu.setType(type);
        menu.setPath(request.getPath());
        menu.setComponent(request.getComponent());
        menu.setPermission(request.getPermission());
        menu.setIcon(request.getIcon());
        menu.setVisible(request.getVisible() == null ? 1 : request.getVisible());
        menu.setSort(request.getSort() == null ? 0 : request.getSort());
        menu.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        if (request.getId() == null) {
            menuMapper.insert(menu);
        } else {
            menuMapper.updateById(menu);
        }
        return menu;
    }

    @Transactional(rollbackFor = Exception.class)
    public void delete(Long tenantId, Long id) {
        if (menuMapper.countChildren(tenantId, id) > 0) {
            throw new IllegalArgumentException("存在子菜单，不能删除");
        }
        roleMenuMapper.delete(new LambdaQueryWrapper<AiRoleMenu>()
                .eq(AiRoleMenu::getTenantId, tenantId)
                .eq(AiRoleMenu::getMenuId, id));
        menuMapper.delete(new LambdaQueryWrapper<AiMenu>()
                .eq(AiMenu::getTenantId, tenantId)
                .eq(AiMenu::getId, id));
    }

    private List<MenuNode> toNodes(List<AiMenu> menus) {
        List<MenuNode> nodes = new ArrayList<>();
        for (AiMenu menu : menus) {
            nodes.add(new MenuNode(menu.getId(), menu.getParentId(), menu.getName(), menu.getType(), menu.getPath(),
                    menu.getComponent(), menu.getPermission(), menu.getIcon(), menu.getVisible(), menu.getSort(), menu.getStatus()));
        }
        return nodes;
    }

    private List<MenuNode> buildTree(List<MenuNode> list) {
        Map<Long, MenuNode> idMap = new HashMap<>(list.size());
        for (MenuNode node : list) {
            idMap.put(node.getId(), node);
        }
        List<MenuNode> roots = new ArrayList<>();
        for (MenuNode node : list) {
            if (node.getParentId() == 0) {
                roots.add(node);
            } else {
                MenuNode parent = idMap.get(node.getParentId());
                if (parent != null) {
                    parent.getChildren().add(node);
                }
            }
        }
        return roots;
    }
}
