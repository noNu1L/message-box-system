package com.nonu1l.msgboxcore.bo;

import lombok.Data;

import java.util.List;

/**
 * 发送 Bo 对象
 *
 * @author zhonghanbo
 * @date 2024年11月09日 10:53
 */

@Data
public class SendBo {

    /**
     * 用户 ID
     */
    private String userId;
    /**
     * 用户名
     */
    private String nickname;
    /**
     * 用户电子邮件
     */
    private String sendEmail;
    /**
     * 密码
     */
    private String emailPassword;
    /**
     * 主题
     */
    private String subject;
    /**
     * 内容
     */
    private String content;

    /**
     * 通道代码
     */
    private String channelCode;
    /**
     * 发送组名称
     */
    private String groupName;
    /**
     * 收件人电子邮件
     */
    private List<String> receiveEmails;

    /**
     * 接收模式
     */
    private List<String> receiveMode;

    /**
     * 电子邮件服务器名称
     */
    private String emailServerName;
    /**
     * 电子邮件服务器地址
     */
    private String emailServerAddress;
    /**
     * 电子邮件服务器端口
     */
    private Integer emailServerPort;

    /**
     * 电子邮件接收组 ID
     */
    private String exEmailReceiveGroupId;
}
