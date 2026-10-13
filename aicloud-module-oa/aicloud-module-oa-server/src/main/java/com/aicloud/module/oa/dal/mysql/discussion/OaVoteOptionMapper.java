package com.aicloud.module.oa.dal.mysql.discussion;

import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.oa.dal.dataobject.discussion.OaVoteOptionDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.Collection;
import java.util.List;

/**
 * OA 投票选项 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaVoteOptionMapper extends BaseMapperX<OaVoteOptionDO> {

    default List<OaVoteOptionDO> selectListByDiscussionIds(Collection<Long> discussionIds) {
        return selectList(new LambdaQueryWrapperX<OaVoteOptionDO>()
                .in(OaVoteOptionDO::getDiscussionId, discussionIds)
                .orderByAsc(OaVoteOptionDO::getSort)
                .orderByAsc(OaVoteOptionDO::getId));
    }

    default void deleteByDiscussionId(Long discussionId) {
        delete(OaVoteOptionDO::getDiscussionId, discussionId);
    }

}
