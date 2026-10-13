package com.aicloud.module.oa.dal.mysql.announcement;

import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.framework.mybatis.core.query.MPJLambdaWrapperX;
import com.aicloud.module.oa.controller.admin.announcement.vo.OaAnnouncementPageReqVO;
import com.aicloud.module.oa.dal.dataobject.announcement.OaAnnouncementDO;
import com.aicloud.module.oa.dal.dataobject.announcement.OaAnnouncementReceiverDO;
import org.apache.ibatis.annotations.Mapper;


/**
 * OA 公告 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaAnnouncementMapper extends BaseMapperX<OaAnnouncementDO> {

    default OaAnnouncementDO selectByIdForUpdate(Long id) {
        return selectOneForUpdate(OaAnnouncementDO::getId, id);
    }

    default PageResult<OaAnnouncementDO> selectPage(OaAnnouncementPageReqVO pageReqVO, Long publisherUserId) {
        LambdaQueryWrapperX<OaAnnouncementDO> query = new LambdaQueryWrapperX<OaAnnouncementDO>()
                .eq(OaAnnouncementDO::getCreator, publisherUserId.toString())
                .likeIfPresent(OaAnnouncementDO::getTitle, pageReqVO.getTitle())
                .eqIfPresent(OaAnnouncementDO::getType, pageReqVO.getType())
                .eqIfPresent(OaAnnouncementDO::getPriority, pageReqVO.getPriority())
                .betweenIfPresent(OaAnnouncementDO::getCreateTime, pageReqVO.getCreateTime());
        query.orderByDesc(OaAnnouncementDO::getTop)
                .orderByDesc(OaAnnouncementDO::getUpdateTime)
                .orderByDesc(OaAnnouncementDO::getId);
        return selectPage(pageReqVO, query);
    }

    default PageResult<OaAnnouncementDO> selectReceivedPage(OaAnnouncementPageReqVO pageReqVO, Long receiverUserId) {
        MPJLambdaWrapperX<OaAnnouncementDO> query = new MPJLambdaWrapperX<OaAnnouncementDO>()
                .selectAll(OaAnnouncementDO.class)
                .innerJoin(OaAnnouncementReceiverDO.class, OaAnnouncementReceiverDO::getAnnouncementId,OaAnnouncementDO::getId)
                .eq(OaAnnouncementReceiverDO::getReceiverUserId, receiverUserId)
                .eq(OaAnnouncementReceiverDO::getDeleted, false)
                .eqIfPresent(OaAnnouncementReceiverDO::getReadStatus, pageReqVO.getReadStatus())
                .likeIfPresent(OaAnnouncementDO::getTitle, pageReqVO.getTitle())
                .eqIfPresent(OaAnnouncementDO::getType, pageReqVO.getType())
                .eqIfPresent(OaAnnouncementDO::getPriority, pageReqVO.getPriority())
                .betweenIfPresent(OaAnnouncementDO::getCreateTime, pageReqVO.getCreateTime());
        query.orderByDesc(OaAnnouncementDO::getTop)
                .orderByAsc(OaAnnouncementReceiverDO::getReadStatus)
                .orderByDesc(OaAnnouncementDO::getUpdateTime)
                .orderByDesc(OaAnnouncementDO::getId);
        return selectJoinPage(pageReqVO, OaAnnouncementDO.class, query);
    }

}
