package com.aicloud.module.bpm.framework.rpc.config;

import com.aicloud.module.bpm.api.event.CrmContractStatusListener;
import com.aicloud.module.bpm.api.event.CrmReceivableStatusListener;
import com.aicloud.module.bpm.api.event.OaLeaveApplyStatusListener;
import com.aicloud.module.bpm.api.event.OaMeetingRoomBookingStatusListener;
import com.aicloud.module.bpm.api.event.OaOfficialDocReceiveStatusListener;
import com.aicloud.module.bpm.api.event.OaOfficialDocSendStatusListener;
import com.aicloud.module.bpm.api.event.OaOvertimeApplyStatusListener;
import com.aicloud.module.bpm.api.event.OaRegularApplyStatusListener;
import com.aicloud.module.bpm.api.event.OaReimbursementStatusListener;
import com.aicloud.module.bpm.api.event.OaResignApplyStatusListener;
import com.aicloud.module.bpm.api.event.OaSealApplyStatusListener;
import com.aicloud.module.bpm.api.event.OaSupplyApplyStatusListener;
import com.aicloud.module.bpm.api.event.OaTravelApplyStatusListener;
import com.aicloud.module.bpm.api.event.OaTravelReimbursementStatusListener;
import com.aicloud.module.bpm.api.event.OaVehicleApplyStatusListener;
import com.aicloud.module.bpm.api.event.OaVehicleReturnStatusListener;
import com.aicloud.module.system.api.dept.DeptApi;
import com.aicloud.module.system.api.dept.PostApi;
import com.aicloud.module.system.api.dict.DictDataApi;
import com.aicloud.module.system.api.permission.PermissionApi;
import com.aicloud.module.system.api.permission.RoleApi;
import com.aicloud.module.system.api.sms.SmsSendApi;
import com.aicloud.module.system.api.user.AdminUserApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(value = "bpmRpcConfiguration", proxyBeanMethods = false)
@EnableFeignClients(clients = {RoleApi.class, DeptApi.class, PostApi.class, AdminUserApi.class, SmsSendApi.class, DictDataApi.class,
        PermissionApi.class})
public class RpcConfiguration {

    // ========== 特殊：解决微 aicloud-cloud 微服务场景下，跨服务（进程）无法 Listener 的问题 ==========

    @Bean
    @ConditionalOnMissingBean(name = "crmReceivableStatusListener")
    public CrmReceivableStatusListener crmReceivableStatusListener() {
        return new CrmReceivableStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "crmContractStatusListener")
    public CrmContractStatusListener crmContractStatusListener() {
        return new CrmContractStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaLeaveApplyStatusListener")
    public OaLeaveApplyStatusListener oaLeaveApplyStatusListener() {
        return new OaLeaveApplyStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaMeetingRoomBookingStatusListener")
    public OaMeetingRoomBookingStatusListener oaMeetingRoomBookingStatusListener() {
        return new OaMeetingRoomBookingStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaOfficialDocReceiveStatusListener")
    public OaOfficialDocReceiveStatusListener oaOfficialDocReceiveStatusListener() {
        return new OaOfficialDocReceiveStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaOfficialDocSendStatusListener")
    public OaOfficialDocSendStatusListener oaOfficialDocSendStatusListener() {
        return new OaOfficialDocSendStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaOvertimeApplyStatusListener")
    public OaOvertimeApplyStatusListener oaOvertimeApplyStatusListener() {
        return new OaOvertimeApplyStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaRegularApplyStatusListener")
    public OaRegularApplyStatusListener oaRegularApplyStatusListener() {
        return new OaRegularApplyStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaReimbursementStatusListener")
    public OaReimbursementStatusListener oaReimbursementStatusListener() {
        return new OaReimbursementStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaResignApplyStatusListener")
    public OaResignApplyStatusListener oaResignApplyStatusListener() {
        return new OaResignApplyStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaSealApplyStatusListener")
    public OaSealApplyStatusListener oaSealApplyStatusListener() {
        return new OaSealApplyStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaSupplyApplyStatusListener")
    public OaSupplyApplyStatusListener oaSupplyApplyStatusListener() {
        return new OaSupplyApplyStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaTravelApplyStatusListener")
    public OaTravelApplyStatusListener oaTravelApplyStatusListener() {
        return new OaTravelApplyStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaTravelReimbursementStatusListener")
    public OaTravelReimbursementStatusListener oaTravelReimbursementStatusListener() {
        return new OaTravelReimbursementStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaVehicleApplyStatusListener")
    public OaVehicleApplyStatusListener oaVehicleApplyStatusListener() {
        return new OaVehicleApplyStatusListener();
    }

    @Bean
    @ConditionalOnMissingBean(name = "oaVehicleReturnStatusListener")
    public OaVehicleReturnStatusListener oaVehicleReturnStatusListener() {
        return new OaVehicleReturnStatusListener();
    }

}
