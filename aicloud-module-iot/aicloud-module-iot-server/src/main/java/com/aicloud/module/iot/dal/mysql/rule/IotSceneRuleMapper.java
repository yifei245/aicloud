package com.aicloud.module.iot.dal.mysql.rule;

import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.iot.controller.admin.rule.vo.scene.IotSceneRulePageReqVO;
import com.aicloud.module.iot.dal.dataobject.rule.IotSceneRuleDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * IoT 场景联动 Mapper
 *
 * @author HUIHUI
 */
@Mapper
public interface IotSceneRuleMapper extends BaseMapperX<IotSceneRuleDO> {

    default PageResult<IotSceneRuleDO> selectPage(IotSceneRulePageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<IotSceneRuleDO>()
                .likeIfPresent(IotSceneRuleDO::getName, reqVO.getName())
                .likeIfPresent(IotSceneRuleDO::getDescription, reqVO.getDescription())
                .eqIfPresent(IotSceneRuleDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(IotSceneRuleDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(IotSceneRuleDO::getId));
    }

    default List<IotSceneRuleDO> selectListByStatus(Integer status) {
        return selectList(IotSceneRuleDO::getStatus, status);
    }

}
