package com.nonu1l.msgboxcore.dto;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

/**
 * 通道配置 DTO
 *
 * @author nonu1l
 * @date 2024/12/28
 */
public class SendReceiveConfigDto {

    @Data
    public static class SendConfig {
        @NotBlank(message = "邮箱不能为空")
        private String email;
        @NotBlank(message = "密码不能为空")
        private String password;
        @NotBlank(message = "发件服务器不能为空")
        private String serverId;
        private String serverName;
        private Integer emailStatus;
        private String emailStatusTip;
    }

    @Data
    public static class ReceiveConfig {
        private String groupId;
        private String channelCode;
        @NotBlank
        private List<String> receiveMode;
        @NotBlank
        private String groupName;
        private String userId;
        private String description;
        private List<String> receiveEmails;
    }


} 