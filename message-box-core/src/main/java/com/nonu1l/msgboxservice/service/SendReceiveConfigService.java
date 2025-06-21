package com.nonu1l.msgboxservice.service;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.nonu1l.commons.utils.JsonUtils;
import com.nonu1l.commons.utils.ValidateUtils;
import com.nonu1l.msgboxservice.dto.SendReceiveConfigDto;
import com.nonu1l.msgboxservice.entity.ReceiveGroup;
import com.nonu1l.msgboxservice.entity.SmtpServer;
import com.nonu1l.msgboxservice.entity.UserExt;
import com.nonu1l.msgboxservice.utils.ResourceUtil;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * 通道配置 Service
 *
 * @author nonu1l
 * @date 2024/12/28
 */

@Service
public class SendReceiveConfigService {

    private final ReceiveGroupService receiveGroupService;
    private final UserService userService;
    private final SmtpServerService smtpServerService;
    private final SecurityService securityService;
    private final ChannelCodeService channelCodeService;

    public SendReceiveConfigService(ReceiveGroupService receiveGroupService, UserService userService, SmtpServerService smtpServerService, SecurityService securityService, ChannelCodeService channelCodeService) {
        this.receiveGroupService = receiveGroupService;
        this.userService = userService;
        this.smtpServerService = smtpServerService;
        this.securityService = securityService;
        this.channelCodeService = channelCodeService;
    }


    /**
     * 获取发送配置
     *
     * @return {@link SendReceiveConfigDto.SendConfig }
     */
    public SendReceiveConfigDto.SendConfig getSendConfig() {
        UserExt sessionUser = ResourceUtil.getSessionUser();
        String userId = sessionUser.getUserId();
        UserExt user = userService.getById(userId);
        String emailServerId = user.getEmailServerId();
        SendReceiveConfigDto.SendConfig sendConfig = new SendReceiveConfigDto.SendConfig();

        if (ValidateUtils.isNotEmpty(emailServerId)) {
            SmtpServer smtpServer = smtpServerService.getById(user.getEmailServerId());
            sendConfig.setServerId(emailServerId);
            sendConfig.setEmail(user.getEmail());
            sendConfig.setPassword(user.getEmailPassword());
            sendConfig.setServerName(smtpServer.getServerName());
            sendConfig.setEmailStatus(user.getEmailStatus());
            sendConfig.setEmailStatusTip(convertTip(user.getEmailStatus()));
        }


        return sendConfig;
    }

    /**
     * 转换提示
     *
     * @param status 地位
     * @return {@link String }
     */
    private String convertTip(Integer status) {
        return switch (status) {
            case -1 -> "发送异常，请检查配置是否正确或存在频繁发送";
            case 0 -> "邮件发送状态未知";
            case 1 -> "邮件发送正常";
            default -> "";
        };
    }


    /**
     * 获取接收配置
     *
     * @return {@link List }<{@link SendReceiveConfigDto.ReceiveConfig }>
     */
    @SuppressWarnings("all")
    public List<SendReceiveConfigDto.ReceiveConfig> getReceiveConfig() {
        UserExt sessionUser = ResourceUtil.getSessionUser();
        String userId = sessionUser.getUserId();
        LambdaQueryWrapper<ReceiveGroup> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(ReceiveGroup::getUserId, userId)
                .orderByDesc(ReceiveGroup::getCreatedAt);

        List<ReceiveGroup> receiveGroups =
                receiveGroupService.list(lambdaQueryWrapper);

        List<SendReceiveConfigDto.ReceiveConfig> list = receiveGroups.stream().map(item -> {
            SendReceiveConfigDto.ReceiveConfig receiveConfig = new SendReceiveConfigDto.ReceiveConfig();
            String groupId = item.getGroupId();
            receiveConfig.setGroupId(groupId);
            receiveConfig.setGroupName(item.getName());
            receiveConfig.setChannelCode(item.getChannelCode());
            receiveConfig.setDescription(item.getDescription());

            if (ValidateUtils.isNotEmpty(item.getReceiveMode())) {
                List<String> receiveModeList = JsonUtils.parse(item.getReceiveMode(), List.class);
                receiveConfig.setReceiveMode(receiveModeList);
            }

            if (ValidateUtils.isNotEmpty(item.getReceiveEmails())) {
                List<String> parse = JsonUtils.parse(item.getReceiveEmails(), List.class);
                receiveConfig.setReceiveEmails(parse);
            }
            return receiveConfig;
        }).toList();

        return list;
    }


    /**
     * 更新发送配置
     *
     * @param sendConfig 发送配置
     */
    public void updateSendConfig(SendReceiveConfigDto.SendConfig sendConfig) {
        String email = sendConfig.getEmail();
        String password = sendConfig.getPassword();
        String serverId = sendConfig.getServerId();
        UserExt sessionUser = ResourceUtil.getSessionUser();
        sessionUser.setEmail(email);
        sessionUser.setEmailPassword(password);
        sessionUser.setEmailServerId(serverId);
        userService.updateById(sessionUser);
        securityService.refreshSession(sessionUser);
    }


    /**
     * 更新接收配置
     *
     * @param receiveConfig 接收配置
     */
    public void updateReceiveConfig(SendReceiveConfigDto.ReceiveConfig receiveConfig) {

        List<String> receiveEmails = receiveConfig.getReceiveEmails();
        String channelCode = receiveConfig.getChannelCode();
        String groupName = receiveConfig.getGroupName();
        String userId = ResourceUtil.getSessionUser().getUserId();
        String groupId = receiveConfig.getGroupId();
        String description = receiveConfig.getDescription();
//        String channelCode = receiveConfig.getChannelCode();
        List<String> receiveMode = receiveConfig.getReceiveMode();

        ReceiveGroup receiveGroup = new ReceiveGroup();
        receiveGroup.setUserId(userId);
//        receiveGroup.setReceiveMode(receiveMode);
        receiveGroup.setName(groupName);
        receiveGroup.setDescription(description);

        if (ValidateUtils.isNotEmpty(receiveEmails)) {
            String receiveEmailsJson = JsonUtils.stringify(receiveEmails);
            receiveGroup.setReceiveEmails(receiveEmailsJson);
        }else {
            receiveGroup.setReceiveEmails("[]");
        }

        if (ValidateUtils.isNotEmpty(receiveMode)) {
            String receiveModeJson = JsonUtils.stringify(receiveMode);
            receiveGroup.setReceiveMode(receiveModeJson);
        }else {
            receiveGroup.setReceiveMode("[]");
        }

        if (ValidateUtils.isEmpty(groupId)) {
            String newChannelCode = channelCodeService.getNewChannelCode();

            receiveGroup.setGroupId(newChannelCode);
            receiveGroup.setChannelCode(newChannelCode);
            receiveGroupService.save(receiveGroup);
        } else {
            receiveGroup.setGroupId(groupId);
            receiveGroup.setChannelCode(channelCode);
            receiveGroupService.updateById(receiveGroup);
        }
    }

    /**
     * 删除接收配置
     *
     * @param receiveConfig 接收配置
     */
    public void deleteReceiveConfig(SendReceiveConfigDto.ReceiveConfig receiveConfig) {
        String groupId = receiveConfig.getGroupId();
        receiveGroupService.removeById(groupId);
    }



    /**
     * 获取SMTP服务器
     *
     * @return {@link List }<{@link HashMap }<{@link String }, {@link String }>>
     */
    public List<HashMap<String, String>> getSmtpServer() {
        List<SmtpServer> smtpServerList = smtpServerService.list();

        return smtpServerList.stream().map(item -> {
            HashMap<String, String> map = new HashMap<>();
            map.put("serverId", item.getEmailServerId());
            map.put("serverName", item.getServerName());
            return map;
        }).toList();
    }
} 