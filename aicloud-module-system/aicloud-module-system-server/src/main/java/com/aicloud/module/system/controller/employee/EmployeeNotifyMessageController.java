package com.aicloud.module.system.controller.employee;

import com.aicloud.framework.common.pojo.*;
import com.aicloud.module.system.controller.admin.notify.NotifyMessageController;
import com.aicloud.module.system.controller.admin.notify.vo.message.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@Validated
@RequestMapping("/system/notifications")
@Tag(name="员工端 - 本人站内信")
public class EmployeeNotifyMessageController {
    @Resource private NotifyMessageController messages;
    @GetMapping("/page") @Operation(summary="本人通知分页")
    public CommonResult<PageResult<NotifyMessageRespVO>> page(@Valid NotifyMessageMyPageReqVO request) { return messages.getMyMyNotifyMessagePage(request); }
    @PutMapping("/read") @Operation(summary="将本人通知标记为已读")
    public CommonResult<Boolean> read(@RequestParam @Size(min=1,max=100) List<Long> ids) { return messages.updateNotifyMessageRead(ids); }
    @PutMapping("/read-all") @Operation(summary="将本人全部通知标记为已读")
    public CommonResult<Boolean> readAll() { return messages.updateAllNotifyMessageRead(); }
    @GetMapping("/unread-count") @Operation(summary="本人未读通知数量")
    public CommonResult<Long> count() { return messages.getUnreadNotifyMessageCount(); }
    @GetMapping("/unread") @Operation(summary="本人最近未读通知")
    public CommonResult<List<NotifyMessageRespVO>> unread(@RequestParam(defaultValue="10") @Min(1) @Max(100) int size) { return messages.getUnreadNotifyMessageList(size); }
}
