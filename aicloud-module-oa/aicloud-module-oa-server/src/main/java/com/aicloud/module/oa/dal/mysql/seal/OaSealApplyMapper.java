package com.aicloud.module.oa.dal.mysql.seal;

import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.oa.controller.admin.seal.vo.apply.OaSealApplyPageReqVO;
import com.aicloud.module.oa.dal.dataobject.seal.OaSealApplyDO;
import org.apache.ibatis.annotations.Mapper;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Collection;

import com.aicloud.module.bpm.enums.task.BpmProcessInstanceStatusEnum;
import com.aicloud.module.oa.enums.seal.OaSealUseModeEnum;
import com.aicloud.module.oa.enums.seal.OaSealUseStatusEnum;

/**
 * OA 用印申请 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaSealApplyMapper extends BaseMapperX<OaSealApplyDO> {

    default Long selectCountBySealId(Long id) {
        return selectCount(OaSealApplyDO::getSealId, id);
    }

    default OaSealApplyDO selectByNo(String no) {
        return selectOne(OaSealApplyDO::getNo, no);
    }

    default OaSealApplyDO selectByIdForUpdate(Long id) {
        return selectOne(new LambdaQueryWrapperX<OaSealApplyDO>().eq(OaSealApplyDO::getId, id).last("FOR UPDATE"));
    }

    default PageResult<OaSealApplyDO> selectPage(Long userId, OaSealApplyPageReqVO reqVO) {
        return selectPage(reqVO, buildPageQuery(reqVO).eq(OaSealApplyDO::getUserId, userId));
    }

    default PageResult<OaSealApplyDO> selectRegisterPage(Collection<Long> sealIds, OaSealApplyPageReqVO reqVO) {
        return selectPage(reqVO, buildPageQuery(reqVO).in(OaSealApplyDO::getSealId, sealIds)
                .eq(OaSealApplyDO::getStatus, BpmProcessInstanceStatusEnum.APPROVE.getStatus()));
    }

    private LambdaQueryWrapperX<OaSealApplyDO> buildPageQuery(OaSealApplyPageReqVO reqVO) {
        return new LambdaQueryWrapperX<OaSealApplyDO>()
                .likeIfPresent(OaSealApplyDO::getNo, reqVO.getNo())
                .eqIfPresent(OaSealApplyDO::getSealId, reqVO.getSealId())
                .likeIfPresent(OaSealApplyDO::getSealName, reqVO.getSealName())
                .eqIfPresent(OaSealApplyDO::getDeptId, reqVO.getDeptId())
                .betweenIfPresent(OaSealApplyDO::getCreateTime, reqVO.getCreateTime())
                .betweenIfPresent(OaSealApplyDO::getExpectedUseTime, reqVO.getExpectedUseTime())
                .eqIfPresent(OaSealApplyDO::getStatus, reqVO.getStatus())
                .eqIfPresent(OaSealApplyDO::getType, reqVO.getType())
                .eqIfPresent(OaSealApplyDO::getMode, reqVO.getMode())
                .eqIfPresent(OaSealApplyDO::getUrgent, reqVO.getUrgent())
                .eqIfPresent(OaSealApplyDO::getUseStatus, reqVO.getUseStatus())
                .orderByDesc(OaSealApplyDO::getId);
    }

    default List<OaSealApplyDO> selectListBySealIdAndBorrowTimeConflict(
            Long sealId, Long excludeId, LocalDateTime beginTime, LocalDateTime endTime) {
        return selectList(new LambdaQueryWrapperX<OaSealApplyDO>().eq(OaSealApplyDO::getSealId, sealId)
                .ne(OaSealApplyDO::getId, excludeId)
                .eq(OaSealApplyDO::getMode, OaSealUseModeEnum.BORROW.getMode())
                .le(OaSealApplyDO::getExpectedUseTime, endTime)
                .ge(OaSealApplyDO::getExpectedReturnTime, beginTime)
                .in(OaSealApplyDO::getStatus, BpmProcessInstanceStatusEnum.RUNNING.getStatus(),
                        BpmProcessInstanceStatusEnum.APPROVE.getStatus())
                .notIn(OaSealApplyDO::getUseStatus, OaSealUseStatusEnum.RETURNED.getStatus(), OaSealUseStatusEnum.COMPLETED.getStatus())
                .orderByAsc(OaSealApplyDO::getId));
    }
}
