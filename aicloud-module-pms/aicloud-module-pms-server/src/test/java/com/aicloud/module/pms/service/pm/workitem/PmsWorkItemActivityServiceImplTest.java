package com.aicloud.module.pms.service.pm.workitem;

import cn.hutool.core.collection.CollUtil;
import com.aicloud.framework.test.core.ut.BaseDbUnitTest;
import com.aicloud.module.pms.dal.dataobject.pm.workitem.PmsWorkItemActivityDO;
import com.aicloud.module.pms.dal.mysql.pm.workitem.PmsWorkItemActivityMapper;
import com.aicloud.module.pms.service.pm.iteration.PmsIterationService;
import com.aicloud.module.system.api.user.AdminUserApi;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static com.aicloud.framework.test.core.util.RandomUtils.randomLongId;
import static com.aicloud.module.pms.enums.pm.workitem.PmsWorkItemActivityContentEnum.WORK_ITEM_CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * {@link PmsWorkItemActivityServiceImpl} 的单元测试类
 *
 * @author 芋道源码
 */
@Import(PmsWorkItemActivityServiceImpl.class)
public class PmsWorkItemActivityServiceImplTest extends BaseDbUnitTest {

    @Resource
    private PmsWorkItemActivityServiceImpl activityService;

    @Resource
    private PmsWorkItemActivityMapper activityMapper;

    @MockitoBean
    private PmsWorkItemLabelService workItemLabelService;
    @MockitoBean
    private PmsWorkItemService workItemService;
    @MockitoBean
    private PmsIterationService iterationService;
    @MockitoBean
    private AdminUserApi adminUserApi;

    @Test
    public void testRecordAndGetActivity_success() {
        // 准备参数
        Long projectId = randomLongId();
        Long workItemId = randomLongId();
        Long userId = randomLongId();

        // 调用
        activityService.createWorkItemActivity(projectId, workItemId, userId, WORK_ITEM_CREATED);
        List<PmsWorkItemActivityDO> activities = activityService.getWorkItemActivityList(workItemId);

        // 断言
        assertEquals(1, activities.size());
        PmsWorkItemActivityDO activity = CollUtil.getFirst(activities);
        assertEquals(projectId, activity.getProjectId());
        assertEquals(userId, activity.getOperatorUserId());
        assertEquals("创建了工作项", activity.getContent());
    }

    @Test
    public void testDeleteWorkItemActivityListByProjectId_success() {
        // mock 数据
        Long projectId = randomLongId();
        PmsWorkItemActivityDO activity = new PmsWorkItemActivityDO().setProjectId(projectId)
                .setWorkItemId(randomLongId()).setOperatorUserId(randomLongId()).setContent("更新了工作项");
        activityMapper.insert(activity);

        // 调用
        activityService.deleteWorkItemActivityListByProjectId(projectId);

        // 断言
        assertEquals(0, activityService.getWorkItemActivityList(activity.getWorkItemId()).size());
    }

}
