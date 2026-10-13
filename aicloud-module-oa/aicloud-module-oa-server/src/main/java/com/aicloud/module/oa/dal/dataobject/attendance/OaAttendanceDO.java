package com.aicloud.module.oa.dal.dataobject.attendance;

import com.aicloud.framework.mybatis.core.dataobject.BaseDO;
import com.aicloud.module.oa.enums.attendance.OaAttendanceStatusEnum;
import com.aicloud.module.oa.enums.attendance.OaAttendanceTypeEnum;
import com.aicloud.module.system.api.user.dto.AdminUserRespDTO;
import com.baomidou.mybatisplus.annotation.KeySequence;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

/**
 * OA 考勤 DO
 *
 * @author 芋道源码
 */
@TableName("oa_attendance")
@KeySequence("oa_attendance_seq")
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
public class OaAttendanceDO extends BaseDO {

    /**
     * 考勤编号
     */
    @TableId
    private Long id;
    /**
     * 用户编号
     *
     * 关联 {@link AdminUserRespDTO#getId()}
     */
    private Long userId;
    /**
     * 考勤类型
     *
     * 枚举 {@link OaAttendanceTypeEnum}
     */
    private Integer type;
    /**
     * 考勤状态
     *
     * 枚举 {@link OaAttendanceStatusEnum}
     */
    private Integer status;
    /**
     * 考勤时间
     */
    private LocalDateTime attendanceTime;
    /**
     * 考勤 IP
     */
    private String attendanceIp;
    /**
     * 考勤备注
     */
    private String remark;

}
