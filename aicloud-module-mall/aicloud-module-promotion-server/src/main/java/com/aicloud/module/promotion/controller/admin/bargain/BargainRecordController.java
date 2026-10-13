package com.aicloud.module.promotion.controller.admin.bargain;

import cn.hutool.core.collection.CollUtil;
import com.aicloud.framework.common.pojo.CommonResult;
import com.aicloud.framework.common.pojo.PageResult;
import com.aicloud.module.member.api.user.MemberUserApi;
import com.aicloud.module.member.api.user.dto.MemberUserRespDTO;
import com.aicloud.module.promotion.controller.admin.bargain.vo.recrod.BargainRecordPageItemRespVO;
import com.aicloud.module.promotion.controller.admin.bargain.vo.recrod.BargainRecordPageReqVO;
import com.aicloud.module.promotion.convert.bargain.BargainRecordConvert;
import com.aicloud.module.promotion.dal.dataobject.bargain.BargainActivityDO;
import com.aicloud.module.promotion.dal.dataobject.bargain.BargainRecordDO;
import com.aicloud.module.promotion.service.bargain.BargainActivityService;
import com.aicloud.module.promotion.service.bargain.BargainHelpService;
import com.aicloud.module.promotion.service.bargain.BargainRecordService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

import static com.aicloud.framework.common.pojo.CommonResult.success;
import static com.aicloud.framework.common.util.collection.CollectionUtils.convertSet;

@Tag(name = "管理后台 - 砍价记录")
@RestController
@RequestMapping("/promotion/bargain-record")
@Validated
public class BargainRecordController {

    @Resource
    private BargainRecordService bargainRecordService;
    @Resource
    private BargainActivityService bargainActivityService;
    @Resource
    private BargainHelpService bargainHelpService;

    @Resource
    private MemberUserApi memberUserApi;

    @GetMapping("/page")
    @Operation(summary = "获得砍价记录分页")
    @PreAuthorize("@ss.hasPermission('promotion:bargain-record:query')")
    public CommonResult<PageResult<BargainRecordPageItemRespVO>> getBargainRecordPage(@Valid BargainRecordPageReqVO pageVO) {
        PageResult<BargainRecordDO> pageResult = bargainRecordService.getBargainRecordPage(pageVO);
        if (CollUtil.isEmpty(pageResult.getList())) {
            return success(PageResult.empty(pageResult.getTotal()));
        }

        // 拼接数据
        Map<Long, MemberUserRespDTO> userMap = memberUserApi.getUserMap(
                convertSet(pageResult.getList(), BargainRecordDO::getUserId));
        List<BargainActivityDO> activityList = bargainActivityService.getBargainActivityList(
                convertSet(pageResult.getList(), BargainRecordDO::getActivityId));
        Map<Long, Integer> helpCountMap = bargainHelpService.getBargainHelpUserCountMapByRecord(
                convertSet(pageResult.getList(), BargainRecordDO::getId));
        return success(BargainRecordConvert.INSTANCE.convertPage(pageResult, helpCountMap, activityList, userMap));
    }

}
