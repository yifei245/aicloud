package com.aicloud.module.mp.biz.service;

import com.aicloud.module.mp.biz.entity.AiMpMaterial;
import com.aicloud.module.mp.biz.entity.AiMpMenu;
import com.aicloud.module.mp.biz.entity.AiMpMessageLog;
import com.aicloud.module.mp.biz.entity.AiMpMessageTemplate;
import com.aicloud.module.mp.biz.entity.AiMpUserBind;
import com.aicloud.module.mp.biz.mapper.MpMaterialMapper;
import com.aicloud.module.mp.biz.mapper.MpMenuMapper;
import com.aicloud.module.mp.biz.mapper.MpMessageLogMapper;
import com.aicloud.module.mp.biz.mapper.MpMessageTemplateMapper;
import com.aicloud.module.mp.biz.mapper.MpUserBindMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * AICloud generated source.
 *
 * @author yifei
 */
@Service
public class MpPlatformService {

    private final MpMenuMapper menuMapper;
    private final MpMaterialMapper materialMapper;
    private final MpMessageTemplateMapper templateMapper;
    private final MpMessageLogMapper messageLogMapper;
    private final MpUserBindMapper userBindMapper;

    public MpPlatformService(MpMenuMapper menuMapper, MpMaterialMapper materialMapper,
                             MpMessageTemplateMapper templateMapper, MpMessageLogMapper messageLogMapper,
                             MpUserBindMapper userBindMapper) {
        this.menuMapper = menuMapper;
        this.materialMapper = materialMapper;
        this.templateMapper = templateMapper;
        this.messageLogMapper = messageLogMapper;
        this.userBindMapper = userBindMapper;
    }

    public List<AiMpMenu> listMenus(Long tenantId) {
        return menuMapper.selectList(new LambdaQueryWrapper<AiMpMenu>()
                .eq(AiMpMenu::getTenantId, tenantId)
                .orderByAsc(AiMpMenu::getParentId)
                .orderByAsc(AiMpMenu::getSort)
                .orderByAsc(AiMpMenu::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiMpMenu saveMenu(Long tenantId, AiMpMenu menu) {
        LocalDateTime now = LocalDateTime.now();
        menu.setTenantId(tenantId);
        if (menu.getParentId() == null) {
            menu.setParentId(0L);
        }
        if (menu.getSort() == null) {
            menu.setSort(0);
        }
        if (menu.getStatus() == null) {
            menu.setStatus(1);
        }
        menu.setUpdateTime(now);
        if (menu.getId() == null) {
            menu.setCreateTime(now);
            menuMapper.insert(menu);
        } else {
            menuMapper.updateById(menu);
        }
        return menu;
    }

    public Map<String, Object> publishMenu(Long tenantId) {
        List<AiMpMenu> menus = listMenus(tenantId).stream()
                .filter(menu -> menu.getStatus() != null && menu.getStatus() == 1)
                .toList();
        Map<String, Object> data = new HashMap<>(8);
        data.put("tenantId", tenantId);
        data.put("menuCount", menus.size());
        data.put("menus", menus);
        data.put("publishTime", LocalDateTime.now());
        data.put("published", true);
        return data;
    }

    public List<AiMpMaterial> listMaterials(Long tenantId, String materialType) {
        return materialMapper.selectList(new LambdaQueryWrapper<AiMpMaterial>()
                .eq(AiMpMaterial::getTenantId, tenantId)
                .eq(StringUtils.hasText(materialType), AiMpMaterial::getMaterialType, materialType)
                .orderByDesc(AiMpMaterial::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiMpMaterial saveMaterial(Long tenantId, AiMpMaterial material) {
        LocalDateTime now = LocalDateTime.now();
        material.setTenantId(tenantId);
        if (material.getStatus() == null) {
            material.setStatus(1);
        }
        material.setUpdateTime(now);
        if (material.getId() == null) {
            material.setCreateTime(now);
            materialMapper.insert(material);
        } else {
            materialMapper.updateById(material);
        }
        return material;
    }

    public List<AiMpMessageTemplate> listTemplates(Long tenantId) {
        return templateMapper.selectList(new LambdaQueryWrapper<AiMpMessageTemplate>()
                .eq(AiMpMessageTemplate::getTenantId, tenantId)
                .orderByDesc(AiMpMessageTemplate::getId));
    }

    @Transactional(rollbackFor = Exception.class)
    public AiMpMessageTemplate saveTemplate(Long tenantId, AiMpMessageTemplate template) {
        LocalDateTime now = LocalDateTime.now();
        template.setTenantId(tenantId);
        if (template.getStatus() == null) {
            template.setStatus(1);
        }
        template.setUpdateTime(now);
        if (template.getId() == null) {
            template.setCreateTime(now);
            templateMapper.insert(template);
        } else {
            templateMapper.updateById(template);
        }
        return template;
    }

    @Transactional(rollbackFor = Exception.class)
    public AiMpMessageLog sendMessage(Long tenantId, Long userId, String templateCode, String content) {
        AiMpMessageTemplate template = templateMapper.selectOne(new LambdaQueryWrapper<AiMpMessageTemplate>()
                .eq(AiMpMessageTemplate::getTenantId, tenantId)
                .eq(AiMpMessageTemplate::getTemplateCode, templateCode)
                .eq(AiMpMessageTemplate::getStatus, 1)
                .last("LIMIT 1"));
        if (template == null) {
            throw new IllegalArgumentException("消息模板不存在或已停用");
        }
        AiMpUserBind bind = userBindMapper.selectOne(new LambdaQueryWrapper<AiMpUserBind>()
                .eq(AiMpUserBind::getTenantId, tenantId)
                .eq(AiMpUserBind::getUserId, userId)
                .last("LIMIT 1"));
        if (bind == null) {
            throw new IllegalArgumentException("用户未绑定小程序 openId");
        }
        AiMpMessageLog log = new AiMpMessageLog();
        log.setTenantId(tenantId);
        log.setUserId(userId);
        log.setTemplateCode(templateCode);
        log.setOpenId(bind.getOpenId());
        log.setContent(StringUtils.hasText(content) ? content : template.getContent());
        log.setStatus(1);
        log.setSendTime(LocalDateTime.now());
        log.setCreateTime(LocalDateTime.now());
        messageLogMapper.insert(log);
        return log;
    }

    public List<AiMpMessageLog> listMessageLogs(Long tenantId, Long userId) {
        return messageLogMapper.selectList(new LambdaQueryWrapper<AiMpMessageLog>()
                .eq(AiMpMessageLog::getTenantId, tenantId)
                .eq(userId != null, AiMpMessageLog::getUserId, userId)
                .orderByDesc(AiMpMessageLog::getId)
                .last("LIMIT 100"));
    }
}
