package com.aicloud.module.hrm.dal.mysql.employee.info;

import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.hrm.dal.dataobject.employee.info.HrmEmployeeContactDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmEmployeeContactMapper extends BaseMapperX<HrmEmployeeContactDO> {

    default List<HrmEmployeeContactDO> selectListByEmployeeId(Long employeeId) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeContactDO>()
                .eq(HrmEmployeeContactDO::getEmployeeId, employeeId)
                .orderByAsc(HrmEmployeeContactDO::getSort)
                .orderByDesc(HrmEmployeeContactDO::getId));
    }

}
