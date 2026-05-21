package com.aicloud.module.system.biz.service;

import com.aicloud.module.system.biz.entity.AiMenu;
import com.aicloud.module.system.biz.mapper.MenuMapper;
import com.aicloud.module.system.biz.model.MenuNode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class MenuService {

    private final MenuMapper menuMapper;

    public MenuService(MenuMapper menuMapper) {
        this.menuMapper = menuMapper;
    }

    public List<MenuNode> getMenuTree(Long tenantId, Long userId) {
        List<AiMenu> menus = menuMapper.listMenusByUserId(tenantId, userId);
        List<MenuNode> nodes = new ArrayList<>();
        for (AiMenu menu : menus) {
            nodes.add(new MenuNode(
                    menu.getId(),
                    menu.getParentId(),
                    menu.getName(),
                    menu.getType(),
                    menu.getPath(),
                    menu.getComponent(),
                    menu.getPermission()
            ));
        }
        return buildTree(nodes);
    }

    public List<String> getButtonPermissions(Long tenantId, Long userId) {
        return menuMapper.listButtonPermissionsByUserId(tenantId, userId);
    }

    public List<MenuNode> getAllMenus(Long tenantId) {
        List<AiMenu> menus = menuMapper.listAllMenus(tenantId);
        List<MenuNode> nodes = new ArrayList<>();
        for (AiMenu menu : menus) {
            nodes.add(new MenuNode(
                    menu.getId(),
                    menu.getParentId(),
                    menu.getName(),
                    menu.getType(),
                    menu.getPath(),
                    menu.getComponent(),
                    menu.getPermission()
            ));
        }
        return buildTree(nodes);
    }

    private List<MenuNode> buildTree(List<MenuNode> list) {
        Map<Long, MenuNode> idMap = new HashMap<>();
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
