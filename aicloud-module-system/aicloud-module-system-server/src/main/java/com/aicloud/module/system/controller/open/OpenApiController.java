package com.aicloud.module.system.controller.open;

import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.common.enums.UserTypeEnum;
import com.aicloud.framework.security.core.util.SecurityFrameworkUtils;
import com.aicloud.module.system.service.dict.DictDataService;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.access.AccessDeniedException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/open-api/v1/system")
@Tag(name = "第三方 API - 字典")
public class OpenApiController {
    private final DictDataService dictDataService;
    public record DictionaryValue(String label, String value) {}

    @GetMapping("/dictionaries/{type}")
    @PreAuthorize("@ss.hasScope('dictionary.read')")
    @Operation(summary = "按类型读取启用的字典值", description = "仅 client_credentials 应用身份可访问，需要 dictionary.read scope")
    public CommonResult<List<DictionaryValue>> dictionaries(@PathVariable String type) {
        if (!UserTypeEnum.APPLICATION.getValue().equals(SecurityFrameworkUtils.getLoginUser().getUserType())) {
            throw new AccessDeniedException("此接口只接受第三方应用令牌");
        }
        return CommonResult.success(dictDataService.getDictDataList(0, type).stream()
                .map(item -> new DictionaryValue(item.getLabel(), item.getValue())).toList());
    }
}
