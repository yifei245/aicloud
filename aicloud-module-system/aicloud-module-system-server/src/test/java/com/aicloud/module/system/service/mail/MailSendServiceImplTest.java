package com.aicloud.module.system.service.mail;

import cn.hutool.core.map.MapUtil;
import com.aicloud.framework.common.enums.CommonStatusEnum;
import com.aicloud.framework.common.enums.UserTypeEnum;
import com.aicloud.framework.test.core.ut.BaseMockitoUnitTest;
import com.aicloud.framework.test.core.util.RandomUtils;
import com.aicloud.module.system.dal.dataobject.mail.MailAccountDO;
import com.aicloud.module.system.dal.dataobject.mail.MailTemplateDO;
import com.aicloud.module.system.dal.dataobject.user.AdminUserDO;
import com.aicloud.module.system.mq.message.mail.MailSendMessage;
import com.aicloud.module.system.mq.producer.mail.MailProducer;
import com.aicloud.module.system.service.member.MemberService;
import com.aicloud.module.system.service.user.AdminUserService;
import org.assertj.core.util.Lists;


import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static cn.hutool.core.util.RandomUtil.randomEle;
import static com.aicloud.framework.test.core.util.AssertUtils.assertServiceException;
import static com.aicloud.framework.test.core.util.RandomUtils.*;
import static com.aicloud.module.system.enums.ErrorCodeConstants.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class MailSendServiceImplTest extends BaseMockitoUnitTest {

    @InjectMocks
    private MailSendServiceImpl mailSendService;

    @Mock
    private AdminUserService adminUserService;
    @Mock
    private MemberService memberService;
    @Mock
    private MailAccountService mailAccountService;
    @Mock
    private MailTemplateService mailTemplateService;
    @Mock
    private MailLogService mailLogService;
    @Mock
    private MailProducer mailProducer;

    @Test
    public void testSendSingleMail_success() {
        // 准备参数
        Long userId = randomLongId();
        String templateCode = RandomUtils.randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        Collection<String> toMails = Lists.newArrayList("admin@test.com");
        Collection<String> ccMails = Lists.newArrayList("cc@test.com");
        Collection<String> bccMails = Lists.newArrayList("bcc@test.com");

        // mock adminUserService 的方法
        AdminUserDO user = randomPojo(AdminUserDO.class, o -> o.setEmail("admin@example.com"));
        when(adminUserService.getUser(eq(userId))).thenReturn(user);

        // mock MailTemplateService 的方法
        MailTemplateDO template = randomPojo(MailTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(mailTemplateService.getMailTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String title = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getTitle()), eq(templateParams)))
                .thenReturn(title);
        String content = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock MailAccountService 的方法
        MailAccountDO account = randomPojo(MailAccountDO.class);
        when(mailAccountService.getMailAccountFromCache(eq(template.getAccountId()))).thenReturn(account);
        // mock MailLogService 的方法
        Long mailLogId = randomLongId();
        when(mailLogService.createMailLog(eq(userId), eq(UserTypeEnum.ADMIN.getValue()),
                argThat(toMailSet -> toMailSet.contains(user.getEmail()) && toMailSet.contains("admin@test.com")),
                argThat(ccMailSet -> ccMailSet.contains("cc@test.com")),
                argThat(bccMailSet -> bccMailSet.contains("bcc@test.com")),
                eq(account), eq(template), eq(content), eq(templateParams), eq(true))).thenReturn(mailLogId);

        // 调用
        Long resultMailLogId = mailSendService.sendSingleMail(toMails, ccMails, bccMails, userId,
                UserTypeEnum.ADMIN.getValue(), templateCode, templateParams, (File[]) null);
        // 断言
        assertEquals(mailLogId, resultMailLogId);
        // 断言调用
        verify(mailProducer).sendMailSendMessage(eq(mailLogId),
                argThat(toMailSet -> toMailSet.contains(user.getEmail()) && toMailSet.contains("admin@test.com")),
                argThat(ccMailSet -> ccMailSet.contains("cc@test.com")),
                argThat(bccMailSet -> bccMailSet.contains("bcc@test.com")),
                eq(account.getId()), eq(template.getNickname()), eq(title), eq(content), isNull());
    }

    /**
     * 发送成功，当邮件模板开启时
     */
    @Test
    public void testSendSingleMail_successWhenMailTemplateEnable() {
        // 准备参数
        String mail = randomEmail();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = RandomUtils.randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        Collection<String> toMails = Lists.newArrayList(mail);

        // mock MailTemplateService 的方法
        MailTemplateDO template = randomPojo(MailTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(mailTemplateService.getMailTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String title = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getTitle()), eq(templateParams)))
                .thenReturn(title);
        String content = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock MailAccountService 的方法
        MailAccountDO account = randomPojo(MailAccountDO.class);
        when(mailAccountService.getMailAccountFromCache(eq(template.getAccountId()))).thenReturn(account);
        // mock MailLogService 的方法
        Long mailLogId = randomLongId();
        when(mailLogService.createMailLog(eq(userId), eq(userType),
                argThat(toMailSet -> toMailSet.contains(mail)),
                argThat(Collection::isEmpty),
                argThat(Collection::isEmpty),
                eq(account), eq(template), eq(content), eq(templateParams), eq(true))).thenReturn(mailLogId);

        // 调用
        Long resultMailLogId = mailSendService.sendSingleMail(toMails, null, null, userId, userType, templateCode, templateParams, (java.io.File[]) null);
        // 断言
        assertEquals(mailLogId, resultMailLogId);
        // 断言调用
        verify(mailProducer).sendMailSendMessage(eq(mailLogId),
                argThat(toMailSet -> toMailSet.contains(mail)),
                argThat(Collection::isEmpty),
                argThat(Collection::isEmpty),
                eq(account.getId()), eq(template.getNickname()), eq(title), eq(content), isNull());
    }

    /**
     * 发送成功，当邮件模板关闭时
     */
    @Test
    public void testSendSingleMail_successWhenMailTemplateDisable() {
        // 准备参数
        String mail = randomEmail();
        Long userId = randomLongId();
        Integer userType = randomEle(UserTypeEnum.values()).getValue();
        String templateCode = RandomUtils.randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        Collection<String> toMails = Lists.newArrayList(mail);

        // mock MailTemplateService 的方法
        MailTemplateDO template = randomPojo(MailTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.DISABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(mailTemplateService.getMailTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);
        String title = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getTitle()), eq(templateParams)))
                .thenReturn(title);
        String content = RandomUtils.randomString();
        when(mailTemplateService.formatMailTemplateContent(eq(template.getContent()), eq(templateParams)))
                .thenReturn(content);
        // mock MailAccountService 的方法
        MailAccountDO account = randomPojo(MailAccountDO.class);
        when(mailAccountService.getMailAccountFromCache(eq(template.getAccountId()))).thenReturn(account);
        // mock MailLogService 的方法
        Long mailLogId = randomLongId();
        when(mailLogService.createMailLog(eq(userId), eq(userType),
                argThat(toMailSet -> toMailSet.contains(mail)),
                argThat(Collection::isEmpty),
                argThat(Collection::isEmpty),
                eq(account), eq(template), eq(content), eq(templateParams), eq(false))).thenReturn(mailLogId);

        // 调用
        Long resultMailLogId = mailSendService.sendSingleMail(toMails, null, null, userId, userType, templateCode, templateParams, (java.io.File[]) null);
        // 断言
        assertEquals(mailLogId, resultMailLogId);
        // 断言调用
        verify(mailProducer, times(0)).sendMailSendMessage(anyLong(), any(), any(), any(),
                anyLong(), anyString(), anyString(), anyString(), any());
    }

    @Test
    public void testValidateMailTemplateValid_notExists() {
        // 准备参数
        String templateCode = RandomUtils.randomString();
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> mailSendService.validateMailTemplate(templateCode),
                MAIL_TEMPLATE_NOT_EXISTS);
    }

    @Test
    public void testValidateTemplateParams_paramMiss() {
        // 准备参数
        MailTemplateDO template = randomPojo(MailTemplateDO.class,
                o -> o.setParams(Lists.newArrayList("code")));
        Map<String, Object> templateParams = new HashMap<>();
        // mock 方法

        // 调用，并断言异常
        assertServiceException(() -> mailSendService.validateTemplateParams(template, templateParams),
                MAIL_SEND_TEMPLATE_PARAM_MISS, "code");
    }

    @Test
    public void testSendSingleMail_noValidEmail() {
        // 准备参数
        Long userId = randomLongId();
        String templateCode = RandomUtils.randomString();
        Map<String, Object> templateParams = MapUtil.<String, Object>builder().put("code", "1234")
                .put("op", "login").build();
        Collection<String> toMails = Lists.newArrayList("invalid-email"); // 非法邮箱

        // mock MailTemplateService 的方法
        MailTemplateDO template = randomPojo(MailTemplateDO.class, o -> {
            o.setStatus(CommonStatusEnum.ENABLE.getStatus());
            o.setContent("验证码为{code}, 操作为{op}");
            o.setParams(Lists.newArrayList("code", "op"));
        });
        when(mailTemplateService.getMailTemplateByCodeFromCache(eq(templateCode))).thenReturn(template);

        // mock MailAccountService 的方法
        MailAccountDO account = randomPojo(MailAccountDO.class);
        when(mailAccountService.getMailAccountFromCache(eq(template.getAccountId()))).thenReturn(account);

        // 调用，并断言异常
        assertServiceException(() -> mailSendService.sendSingleMail(toMails, null, null, userId,
                UserTypeEnum.ADMIN.getValue(), templateCode, templateParams, (java.io.File[]) null),
                MAIL_SEND_MAIL_NOT_EXISTS);
    }

    @Test
    public void testDoSendMail_success() throws Exception {
        MailSendMessage message = new MailSendMessage().setAccountId(1L).setLogId(2L)
                .setToMails(java.util.List.of("recipient@example.invalid"))
                .setNickname("aicloud").setTitle("Test").setContent("<p>Content</p>");
        MailAccountDO account = new MailAccountDO().setMail("sender@example.invalid")
                .setUsername("sender").setPassword("local-test").setHost("localhost")
                .setPort(25).setSslEnable(false).setStarttlsEnable(false);
        when(mailAccountService.getMailAccountFromCache(1L)).thenReturn(account);
        var mime = new jakarta.mail.internet.MimeMessage(jakarta.mail.Session.getInstance(new java.util.Properties()));
        try (var mocked = mockConstruction(org.springframework.mail.javamail.JavaMailSenderImpl.class,
                (sender, context) -> {
                    when(sender.getJavaMailProperties()).thenReturn(new java.util.Properties());
                    when(sender.createMimeMessage()).thenReturn(mime);
                })) {
            mailSendService.doSendMail(message);
            verify(mocked.constructed().get(0)).send(mime);
            assertEquals("Test", mime.getSubject());
            assertEquals("recipient@example.invalid", mime.getAllRecipients()[0].toString());
            verify(mailLogService).updateMailSendResult(eq(2L), nullable(String.class), isNull());
        }
    }

    @Test
    public void testDoSendMail_exception() {
        MailSendMessage message = new MailSendMessage().setAccountId(1L).setLogId(2L);
        when(mailAccountService.getMailAccountFromCache(1L)).thenReturn(new MailAccountDO().setPort(25));
        mailSendService.doSendMail(message);
        verify(mailLogService).updateMailSendResult(eq(2L), isNull(), any(Exception.class));
    }
}
