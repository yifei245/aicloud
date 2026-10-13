package com.aicloud.module.system.service.mail;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.lang.Validator;
import cn.hutool.core.util.StrUtil;
import com.aicloud.framework.common.enums.CommonStatusEnum;
import com.aicloud.framework.common.enums.UserTypeEnum;
import com.aicloud.module.system.dal.dataobject.mail.MailAccountDO;
import com.aicloud.module.system.dal.dataobject.mail.MailTemplateDO;
import com.aicloud.module.system.dal.dataobject.user.AdminUserDO;
import com.aicloud.module.system.mq.message.mail.MailSendMessage;
import com.aicloud.module.system.mq.producer.mail.MailProducer;
import com.aicloud.module.system.service.member.MemberService;
import com.aicloud.module.system.service.user.AdminUserService;
import com.google.common.annotations.VisibleForTesting;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.core.io.FileSystemResource;
import jakarta.mail.internet.MimeMessage;

import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

import java.io.File;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;

import static com.aicloud.framework.common.exception.util.ServiceExceptionUtil.exception;
import static com.aicloud.module.system.enums.ErrorCodeConstants.*;

/**
 * 邮箱发送 Service 实现类
 *
 * @author wangjingyi
 * @since 2022-03-21
 */
@Service
@Validated
@Slf4j
public class MailSendServiceImpl implements MailSendService {

    @Resource
    private AdminUserService adminUserService;
    @Resource
    private MemberService memberService;

    @Resource
    private MailAccountService mailAccountService;
    @Resource
    private MailTemplateService mailTemplateService;

    @Resource
    private MailLogService mailLogService;
    @Resource
    private MailProducer mailProducer;

    @Override
    public Long sendSingleMail(Collection<String> toMails, Collection<String> ccMails, Collection<String> bccMails,
                               Long userId, Integer userType,
                               String templateCode, Map<String, Object> templateParams,
                               File... attachments) {
        // 1.1 校验邮箱模版是否合法
        MailTemplateDO template = validateMailTemplate(templateCode);
        // 1.2 校验邮箱账号是否合法
        MailAccountDO account = validateMailAccount(template.getAccountId());
        // 1.3 校验邮件参数是否缺失
        validateTemplateParams(template, templateParams);

        // 2. 组装邮箱
        String userMail = getUserMail(userId, userType);
        Collection<String> toMailSet = new LinkedHashSet<>();
        Collection<String> ccMailSet = new LinkedHashSet<>();
        Collection<String> bccMailSet = new LinkedHashSet<>();
        if (Validator.isEmail(userMail)) {
            toMailSet.add(userMail);
        }
        if (CollUtil.isNotEmpty(toMails)) {
            toMails.stream().filter(Validator::isEmail).forEach(toMailSet::add);
        }
        if (CollUtil.isNotEmpty(ccMails)) {
            ccMails.stream().filter(Validator::isEmail).forEach(ccMailSet::add);
        }
        if (CollUtil.isNotEmpty(bccMails)) {
            bccMails.stream().filter(Validator::isEmail).forEach(bccMailSet::add);
        }
        if (CollUtil.isEmpty(toMailSet)) {
            throw exception(MAIL_SEND_MAIL_NOT_EXISTS);
        }

        // 创建发送日志。如果模板被禁用，则不发送短信，只记录日志
        Boolean isSend = CommonStatusEnum.ENABLE.getStatus().equals(template.getStatus());
        String title = mailTemplateService.formatMailTemplateContent(template.getTitle(), templateParams);
        String content = mailTemplateService.formatMailTemplateContent(template.getContent(), templateParams);
        Long sendLogId = mailLogService.createMailLog(userId, userType, toMailSet, ccMailSet, bccMailSet,
                account, template, content, templateParams, isSend);
        // 发送 MQ 消息，异步执行发送短信
        if (isSend) {
            mailProducer.sendMailSendMessage(sendLogId, toMailSet, ccMailSet, bccMailSet,
                    account.getId(), template.getNickname(), title, content, attachments);
        }
        return sendLogId;
    }

    private String getUserMail(Long userId, Integer userType) {
        if (userId == null || userType == null) {
            return null;
        }
        if (UserTypeEnum.ADMIN.getValue().equals(userType)) {
            AdminUserDO user = adminUserService.getUser(userId);
            if (user != null) {
                return user.getEmail();
            }
        }
        if (UserTypeEnum.MEMBER.getValue().equals(userType)) {
            return memberService.getMemberUserEmail(userId);
        }
        return null;
    }

    @Override
    public void doSendMail(MailSendMessage message) {
        // 1. 创建发送账号
        MailAccountDO account = validateMailAccount(message.getAccountId());
        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(account.getHost());
            sender.setPort(account.getPort());
            sender.setUsername(account.getUsername());
            sender.setPassword(account.getPassword());
            sender.getJavaMailProperties().setProperty("mail.smtp.auth", "true");
            sender.getJavaMailProperties().setProperty("mail.smtp.ssl.enable", String.valueOf(account.getSslEnable()));
            sender.getJavaMailProperties().setProperty("mail.smtp.starttls.enable", String.valueOf(account.getStarttlsEnable()));
            sender.getJavaMailProperties().setProperty("mail.smtp.connectiontimeout", "10000");
            sender.getJavaMailProperties().setProperty("mail.smtp.timeout", "10000");
            MimeMessage mime = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, true, "UTF-8");
            helper.setFrom(account.getMail(), StrUtil.blankToDefault(message.getNickname(), account.getMail()));
            helper.setTo(message.getToMails().toArray(String[]::new));
            if (message.getCcMails() != null) helper.setCc(message.getCcMails().toArray(String[]::new));
            if (message.getBccMails() != null) helper.setBcc(message.getBccMails().toArray(String[]::new));
            helper.setSubject(message.getTitle());
            helper.setText(message.getContent(), true);
            if (message.getAttachments() != null) {
                for (java.io.File file : message.getAttachments()) {
                    helper.addAttachment(file.getName(), new FileSystemResource(file));
                }
            }
            sender.send(mime);
            mailLogService.updateMailSendResult(message.getLogId(), mime.getMessageID(), null);
        } catch (Exception e) {
            mailLogService.updateMailSendResult(message.getLogId(), null, e);
        }
    }

    @VisibleForTesting
    MailTemplateDO validateMailTemplate(String templateCode) {
        // 获得邮件模板。考虑到效率，从缓存中获取
        MailTemplateDO template = mailTemplateService.getMailTemplateByCodeFromCache(templateCode);
        // 邮件模板不存在
        if (template == null) {
            throw exception(MAIL_TEMPLATE_NOT_EXISTS);
        }
        return template;
    }

    @VisibleForTesting
    MailAccountDO validateMailAccount(Long accountId) {
        // 获得邮箱账号。考虑到效率，从缓存中获取
        MailAccountDO account = mailAccountService.getMailAccountFromCache(accountId);
        // 邮箱账号不存在
        if (account == null) {
            throw exception(MAIL_ACCOUNT_NOT_EXISTS);
        }
        return account;
    }

    /**
     * 校验邮件参数是否缺失
     *
     * @param template 邮箱模板
     * @param templateParams 参数列表
     */
    @VisibleForTesting
    void validateTemplateParams(MailTemplateDO template, Map<String, Object> templateParams) {
        template.getParams().forEach(key -> {
            Object value = templateParams.get(key);
            if (value == null) {
                throw exception(MAIL_SEND_TEMPLATE_PARAM_MISS, key);
            }
        });
    }

}
