package com.aicloud.module.oa.dal.mysql.overtime;

import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.oa.controller.admin.overtime.vo.OaOvertimeApplyPageReqVO;
import com.aicloud.module.oa.dal.dataobject.overtime.OaOvertimeApplyDO;
import org.apache.ibatis.annotations.Mapper;

/**
 * OA 加班申请 Mapper
 *
 * @author 芋道源码
 */
@Mapper
public interface OaOvertimeApplyMapper extends BaseMapperX<OaOvertimeApplyDO> {

    default PageResult<OaOvertimeApplyDO> selectPage(Long userId, OaOvertimeApplyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<OaOvertimeApplyDO>()
                .eq(OaOvertimeApplyDO::getCreator, userId.toString())
                .likeIfPresent(OaOvertimeApplyDO::getTitle, reqVO.getTitle())
                .eqIfPresent(OaOvertimeApplyDO::getStatus, reqVO.getStatus())
                .orderByDesc(OaOvertimeApplyDO::getCreateTime, OaOvertimeApplyDO::getId));
    }

}
