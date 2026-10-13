package com.aicloud.module.iot.dal.mysql.device;

import com.aicloud.module.iot.controller.admin.device.vo.property.IotDevicePropertyHistoryListReqVO;
import com.aicloud.module.iot.controller.admin.device.vo.property.IotDevicePropertyRespVO;
import com.aicloud.module.iot.dal.dataobject.device.IotDeviceDO;
import com.baomidou.mybatisplus.annotation.InterceptorIgnore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
@InterceptorIgnore(tenantLine = "true") // 自定义 SQL 显式限制 tenant_id。
public interface IotDevicePropertyMapper {

    void insert(@Param("device") IotDeviceDO device,
                @Param("properties") Map<String, String> properties,
                @Param("ts") Long ts,
                @Param("reportTime") Long reportTime);

    List<IotDevicePropertyRespVO> selectListByHistory(@Param("reqVO") IotDevicePropertyHistoryListReqVO reqVO);
}
