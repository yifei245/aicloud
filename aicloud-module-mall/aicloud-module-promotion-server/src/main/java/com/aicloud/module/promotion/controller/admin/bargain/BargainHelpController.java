package com.aicloud.module.promotion.controller.admin.bargain;

import cn.hutool.core.collection.CollUtil;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.module.member.api.user.MemberUserApi;
import com.aicloud.module.member.api.user.dto.MemberUserRespDTO;
import com.aicloud.module.promotion.controller.admin.bargain.vo.help.BargainHelpPageReqVO;
import com.aicloud.module.promotion.controller.admin.bargain.vo.help.BargainHelpRespVO;
import com.aicloud.module.promotion.convert.bargain.BargainHelpConvert;
import com.aicloud.module.promotion.dal.dataobject.bargain.BargainHelpDO;
import com.aicloud.module.promotion.service.bargain.BargainHelpService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

import static com.aicloud.framework.common.pojo.CommonResult.success;
import static com.aicloud.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 砍价助力")
@RestController
@RequestMapping("/promotion/bargain-help")
@Validated
public class BargainHelpController {

    @Resource
    private BargainHelpService bargainHelpService;

    @Resource
    private MemberUserApi memberUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得砍价助力分页")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-help:query')")
    public CommonResult<PageResult<BargainHelpRespVO>> getBargainHelpPage(@Valid BargainHelpPageReqVO pageVO) {
        PageResult<BargainHelpDO> pageResult = bargainHelpService.getBargainHelpPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(
                convertSet(pageResult.getList(), BargainHelpDO::getUserId));
        return success(BargainHelpConvert.INSTANCE.convertPage(pageResult, userMap));
    }

}
