package com.aicloud.module.pms.dal.mysql.pm.project;

import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.pms.dal.dataobject.pm.project.PmsProjectFavoriteDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.aicloud.framework.common.util.collection.CollectionUtils.convertList;

@Mapper
public interface PmsProjectFavoriteMapper extends BaseMapperX<PmsProjectFavoriteDO> {

    default PmsProjectFavoriteDO selectByProjectIdAndUserId(Long projectId, Long userId) {
        return selectFirstOne(PmsProjectFavoriteDO::getProjectId, projectId,
                PmsProjectFavoriteDO::getUserId, userId);
    }

    default List<Long> selectProjectIdListByUserId(Long userId) {
        return convertList(selectList(new LambdaQueryWrapperX<PmsProjectFavoriteDO>()
                .eq(PmsProjectFavoriteDO::getUserId, userId)
                .orderByDesc(PmsProjectFavoriteDO::getCreateTime)), PmsProjectFavoriteDO::getProjectId);
    }

    default void deleteByProjectIds(Collection<Long> projectIds) {
        delete(new LambdaQueryWrapperX<PmsProjectFavoriteDO>()
                .in(PmsProjectFavoriteDO::getProjectId, projectIds));
    }

}
