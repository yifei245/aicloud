package com.aicloud.module.hrm.dal.mysql.employee.experience;

import com.aicloud.framework.mybatis.core.mapper.BaseMapperX;
import com.aicloud.framework.mybatis.core.query.LambdaQueryWrapperX;
import com.aicloud.module.hrm.dal.dataobject.employee.experience.HrmEmployeeTrainingExperienceDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface HrmEmployeeTrainingExperienceMapper extends BaseMapperX<HrmEmployeeTrainingExperienceDO> {

    default List<HrmEmployeeTrainingExperienceDO> selectListByEmployeeId(Long employeeId) {
        return selectList(new LambdaQueryWrapperX<HrmEmployeeTrainingExperienceDO>()
                .eq(HrmEmployeeTrainingExperienceDO::getEmployeeId, employeeId)
                .orderByAsc(HrmEmployeeTrainingExperienceDO::getSort)
                .orderByDesc(HrmEmployeeTrainingExperienceDO::getId));
    }

}
