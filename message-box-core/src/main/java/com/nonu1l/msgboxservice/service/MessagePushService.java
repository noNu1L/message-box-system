package com.nonu1l.msgboxservice.service;


import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.lang.UUID;
import com.nonu1l.commons.exception.CustomException;
import com.nonu1l.commons.utils.Result;
import com.nonu1l.commons.utils.ValidateUtils;
import com.nonu1l.msgboxservice.bo.SendBo;
import com.nonu1l.msgboxservice.dto.SendByGroupOriginalMsgDto;
import com.nonu1l.msgboxservice.entity.SendRecord;
import com.nonu1l.msgboxservice.entity.SmtpServer;
import com.nonu1l.msgboxservice.entity.UserExt;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.angus.mail.util.MailSSLSocketFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.GeneralSecurityException;
import java.sql.Timestamp;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Objects;

/**
 * 消息推送 Service
 *
 * @author nonu1l
 * @date 2024年10月25日 16:20
 */

@Slf4j
@Service
public class MessagePushService {

    private final ReceiveGroupService receiveGroupService;

    private final UserService userService;

    private final SmtpServerService smtpServerService;

    private final SendRecordService sendRecordService;

    private final ExecutorService executorService;

    private final ClientWebSocketHandler clientWebSocketHandler;

    public MessagePushService(ReceiveGroupService receiveGroupService, UserService userService, SmtpServerService smtpServerService, SendRecordService sendRecordService, ClientWebSocketHandler clientWebSocketHandler, @Value("${ex.thread-pool.message-push-size:10}") int threadPoolSize) {
        this.receiveGroupService = receiveGroupService;
        this.userService = userService;
        this.smtpServerService = smtpServerService;
        this.sendRecordService = sendRecordService;
        this.clientWebSocketHandler = clientWebSocketHandler;
        this.executorService = Executors.newFixedThreadPool(threadPoolSize);
    }

    /**
     * 发送电子邮件（按组）
     *
     * @param sendByGroupOriginalMsgDto 原始消息 DTO
     */
    public Result<String> sendByGroup(SendByGroupOriginalMsgDto sendByGroupOriginalMsgDto) {
        // 将整个处理过程异步化，立即返回，避免阻塞上游调用
        executorService.execute(() -> {
            String channelCode = sendByGroupOriginalMsgDto.getChannelCode();
            String subject = sendByGroupOriginalMsgDto.getSubject();
            String content = sendByGroupOriginalMsgDto.getContent();

            String sql = "select" +
                    " u.user_id         userId," +
                    " u.nickname        nickname," +
                    " u.email           sendEmail," +
                    " u.email_password  emailPassword," +
                    " es.server_name    emailServerName," +
                    " es.server_address emailServerAddress," +
                    " es.server_port    emailServerPort," +
                    " erg.group_id      exEmailReceiveGroupId," +
                    " erg.channel_code    channelCode," +
                    " erg.receive_mode    receiveMode," +
                    " erg.name          groupName," +
                    " erg.receive_emails receiveEmails" +
                    " from ex_receive_group erg" +
                    " left join ex_user u on u.user_id = erg.user_id" +
                    " left join ex_smtp_server es on es.email_server_id = u.email_server_id" +
                    " @QueryParams";
            HashMap<String, Object> findParams = new HashMap<>();
            findParams.put("EQ_erg.channel_code", channelCode);
            SendBo sendBo = receiveGroupService.findOneBySql(sql, findParams, null, false, SendBo.class);

            if (ValidateUtils.isEmpty(sendBo)) {
                // 在异步任务中，我们直接记录日志而不是抛出异常
                log.error("通道代码无效或未找到: {}", channelCode);
                return; // 结束任务
            }

            // 清理和准备数据
            List<String> receiveMode = sendBo.getReceiveMode().stream()
                    .map(item -> item.replaceAll("\"", "")).toList();
            sendBo.setSubject(subject);
            sendBo.setContent(content);

            String mailFailMsg = null;
            String receiveMachineIds = null;

            // 1. 邮件发送逻辑
            if (receiveMode.contains("email")) {
                List<String> receiverEmail = sendBo.getReceiveEmails().stream()
                        .map(item -> item.replaceAll("\"", "")).toList();
                if (ValidateUtils.isNotEmpty(receiverEmail)) {
                    sendBo.setReceiveEmails(receiverEmail);
                    mailFailMsg = this.sendMail(sendBo, null);
                } else {
                    log.warn("通道 [{}] 的邮件发送已跳过：接收邮箱列表为空。", channelCode);
                }
            }

            // 2. 客户端发送逻辑
            if (receiveMode.contains("client")) {
                receiveMachineIds = clientWebSocketHandler.sendMessageToChannel(channelCode, subject, content);
                if (receiveMachineIds == null) {
                    log.info("通道 [{}] 没有活跃的客户端，WebSocket 消息未发送。", channelCode);
                }
            }

            // 3. 保存发送记录
            saveSendRecord(sendBo, mailFailMsg, receiveMachineIds);
        });

        return Result.ok("消息已投递，正在后台处理");
    }

    /**
     * 保存发送记录到数据库
     */
    private void saveSendRecord(SendBo sendBo, String mailFailMsg, String receiveMachineIds) {
        SendRecord sendRecord = new SendRecord();
        sendRecord.setId(UUID.fastUUID().toString());
        sendRecord.setUserId(sendBo.getUserId());
        sendRecord.setSendEmail(sendBo.getSendEmail());

        if (ValidateUtils.isNotEmpty(sendBo.getReceiveEmails())) {
            sendRecord.setReceiveEmails(CollUtil.join(sendBo.getReceiveEmails(), ","));
        }

        if (ValidateUtils.isNotEmpty(sendBo.getReceiveMode())) {
            sendRecord.setReceiveMode(CollUtil.join(sendBo.getReceiveMode(), ","));
        }
        sendRecord.setReceiveMachineId(receiveMachineIds); // 直接保存handler返回的结果
        sendRecord.setChannelCode(sendBo.getChannelCode());
        sendRecord.setGroupName(sendBo.getGroupName());
        sendRecord.setSubject(sendBo.getSubject());

        sendRecord.setFailMsg(mailFailMsg == null ? "" : mailFailMsg);
        sendRecordService.save(sendRecord);
        log.info("已为通道 [{}] 保存发送记录。邮件发送成功: {}, 触达设备: [{}].",
                sendBo.getChannelCode(), mailFailMsg == null, Objects.toString(receiveMachineIds, "N/A"));
    }

    /**
     * 测试 send config
     *
     * @param userId  用户 ID
     * @param email   电子邮件
     * @param subject 主题
     * @param content 内容
     * @return {@link Result }<{@link String }>
     */
    public Result<String> testSendConfig(String userId, String email, String subject, String content) {
        UserExt user = userService.getById(userId);
        SmtpServer smtpServer = smtpServerService.getById(user.getEmailServerId());
        DateTime nowTime = new DateTime();
        Timestamp timestamp = nowTime.toTimestamp();

        executorService.execute(() -> {
            SendBo sendBo = new SendBo();
            sendBo.setSendEmail(user.getEmail());
            sendBo.setUserId(userId);
            sendBo.setEmailPassword(user.getEmailPassword());
            sendBo.setEmailServerPort(smtpServer.getServerPort());
            sendBo.setEmailServerName(smtpServer.getServerName());
            sendBo.setEmailServerAddress(smtpServer.getServerAddress());
            sendBo.setNickname(user.getNickname());
            sendBo.setSubject(subject);
            sendBo.setContent(content);
            String msg = this.sendMail(sendBo, null);

            // 复用保存记录方法
            saveSendRecord(sendBo, msg, null);
        });

        return Result.ok("消息投送成功");
    }


    /**
     * 发送邮件
     *
     * @param sendBo 邮件发送 bo
     * @param msgId  消息 ID
     * @return boolean
     */
    public String sendMail(SendBo sendBo, String msgId) {

        String userId = sendBo.getUserId();
        String username = sendBo.getNickname();
        String password = sendBo.getEmailPassword();
        List<String> receiveEmails = sendBo.getReceiveEmails();
        String sendEmail = sendBo.getSendEmail();
        String subject = sendBo.getSubject();
        String content = sendBo.getContent();
        Integer emailServerPort = sendBo.getEmailServerPort();
        String emailServerName = sendBo.getEmailServerName();
        String emailServerAddress = sendBo.getEmailServerAddress();
        String exEmailReceiveGroupId = sendBo.getExEmailReceiveGroupId();
        //配置
        Properties properties = new Properties();
        properties.setProperty("mail.host", emailServerAddress);
        properties.setProperty("mail.transport.protocol", "smtp");
        properties.setProperty("mail.smtp.auth", "true");

        //SSL加密
        MailSSLSocketFactory mailSSLSocketFactory = null;
        try {
            mailSSLSocketFactory = new MailSSLSocketFactory();
            mailSSLSocketFactory.setTrustAllHosts(true);
            properties.put("mail.smtp.ssl.enable", "true");
            properties.put("mail.smtp.ssl.socketFactory", mailSSLSocketFactory);
        } catch (GeneralSecurityException e) {
            log.error("创建MailSSLSocketFactory失败", e);
            return "创建SSL连接工厂失败: " + e.getMessage();
        }

        //创建一个session对象
        Session session = Session.getInstance(properties, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(sendEmail, password);
            }
        });

        //debug模式
        session.setDebug(false);

        try (Transport transport = session.getTransport()) {

            //连接服务器
            transport.connect(emailServerAddress, emailServerPort, sendEmail, password);
            //创建邮件对象
            MimeMessage mimeMessage = new MimeMessage(session);
            //邮件发送人
            mimeMessage.setFrom(new InternetAddress(sendEmail, username));

            for (String address : receiveEmails) {
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(address));
            }

            //邮件标题
            mimeMessage.setSubject(subject);

            //邮件内容 / 正文
            mimeMessage.setContent(content, "text/html;charset=UTF-8");

            //发送
            transport.sendMessage(mimeMessage, mimeMessage.getAllRecipients());

        } catch (Exception e) {
            log.error("邮件发送失败, sendBo: {}", sendBo, e);
            return "邮件发送异常: " + e.getMessage();
        }

        return null;
    }
} 