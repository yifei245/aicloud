package com.aicloud.module.hrm.dal.mysql.employee.experience;

import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.hrm.dal.dataobject.employee.experience.HrmEmployeeEducationExperienceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmEmployeeEducationExperienceMapper extends BaseMapperX<HrmEmployeeEducationExperienceDO> {

    default List<HrmEmployeeEducationExperienceDO> selectListByEmployeeId(Long employeeId) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeEducationExperienceDO>()
                .eq(HrmEmployeeEducationExperienceDO::getEmployeeId, employeeId)
                .orderByAsc(HrmEmployeeEducationExperienceDO::getSort)
                .orderByDesc(HrmEmployeeEducationExperienceDO::getId));
    }

}
